import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const rootDir = path.resolve(__dirname, "..");
const catalogFile = path.join(rootDir, "data", "real-products", "paddy-products.json");
const outputFile = path.join(rootDir, "deploy", "seed-melipet-200-products.sql");

const windows1252 = new Map([
  [0x20ac, 0x80],
  [0x201a, 0x82],
  [0x0192, 0x83],
  [0x201e, 0x84],
  [0x2026, 0x85],
  [0x2020, 0x86],
  [0x2021, 0x87],
  [0x02c6, 0x88],
  [0x2030, 0x89],
  [0x0160, 0x8a],
  [0x2039, 0x8b],
  [0x0152, 0x8c],
  [0x017d, 0x8e],
  [0x2018, 0x91],
  [0x2019, 0x92],
  [0x201c, 0x93],
  [0x201d, 0x94],
  [0x2022, 0x95],
  [0x2013, 0x96],
  [0x2014, 0x97],
  [0x02dc, 0x98],
  [0x2122, 0x99],
  [0x0161, 0x9a],
  [0x203a, 0x9b],
  [0x0153, 0x9c],
  [0x017e, 0x9e],
  [0x0178, 0x9f]
]);

function encodeWindows1252(value) {
  const bytes = [];
  for (const char of value) {
    const codePoint = char.codePointAt(0);
    if (windows1252.has(codePoint)) {
      bytes.push(windows1252.get(codePoint));
    } else if (codePoint <= 0xff) {
      bytes.push(codePoint);
    } else {
      return null;
    }
  }
  return Buffer.from(bytes);
}

function repairMojibake(value) {
  let repaired = value;
  for (let i = 0; i < 2 && /(Ã|Â|Ä|Æ|áº|á»|â€)/.test(repaired); i += 1) {
    const encoded = encodeWindows1252(repaired);
    if (!encoded) {
      break;
    }
    const decoded = encoded.toString("utf8");
    if (decoded === repaired || decoded.includes("\uFFFD")) {
      break;
    }
    repaired = decoded;
  }
  return repaired;
}

function normalizeValue(value) {
  if (typeof value === "string") {
    return repairMojibake(value);
  }
  if (Array.isArray(value)) {
    return value.map(normalizeValue);
  }
  if (value && typeof value === "object") {
    return Object.fromEntries(Object.entries(value).map(([key, child]) => [key, normalizeValue(child)]));
  }
  return value;
}

function truncate(value, maxLength) {
  if (!value) {
    return "";
  }
  return value.length <= maxLength ? value : `${value.slice(0, maxLength - 3).trim()}...`;
}

function sqlString(value) {
  return `'${String(value ?? "").replace(/\\/g, "\\\\").replace(/'/g, "''")}'`;
}

function sqlNumber(value, fallback = 0) {
  const number = Number(value);
  return Number.isFinite(number) ? Math.max(0, Math.round(number)) : fallback;
}

function productDescription(product) {
  const chunks = [
    truncate(product.description || product.name, 260),
    product.brand ? `Thương hiệu: ${product.brand}.` : "",
    product.source_url ? `Nguồn tham khảo: ${product.source_url}` : ""
  ].filter(Boolean);
  return chunks.join("\n\n");
}

function buildSql(products) {
  const statements = [
    "-- Melipet product restore seed generated from real Paddy catalog data.",
    "-- Repairs existing Paddy rows by image path so names/descriptions match their product photos.",
    "SET NAMES utf8mb4;",
    "",
    "INSERT INTO pet_types (code, name, icon, display_order, is_active) VALUES",
    "('dog', 'Chó', 'bxs-dog', 1, 1),",
    "('cat', 'Mèo', 'bxs-cat', 2, 1)",
    "ON DUPLICATE KEY UPDATE name = VALUES(name), icon = VALUES(icon), display_order = VALUES(display_order), is_active = 1;",
    "",
    "UPDATE products",
    "SET is_active = 0",
    "WHERE image LIKE 'products/paddy_%';"
  ];

  for (const product of products) {
    const localImage = product.image;
    const sourceImage = product.image_url || product.image;
    const name = truncate(product.name, 255);
    const description = productDescription(product);
    const price = sqlNumber(product.price);
    const discount = sqlNumber(product.discount);
    const stock = sqlNumber(product.stock, 8);
    const weight = Math.max(sqlNumber(product.weight, 100), 1);
    const category = truncate(product.category, 100);
    const petType = product.pet_type_code === "dog" ? "dog" : "cat";
    const matchByImage = `image IN (${sqlString(localImage)}, ${sqlString(sourceImage)})`;

    statements.push(`
UPDATE products
SET name = ${sqlString(name)},
    image = ${sqlString(localImage)},
    price = ${price},
    discount = ${discount},
    description = ${sqlString(description)},
    stock = ${stock},
    weight = ${weight},
    category = ${sqlString(category)},
    pet_type_id = (SELECT id FROM pet_types WHERE code = ${sqlString(petType)} LIMIT 1),
    is_active = 1
WHERE ${matchByImage};

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT ${sqlString(name)}, ${sqlString(localImage)}, ${price}, ${discount},
       ${sqlString(description)}, ${stock}, ${weight}, ${sqlString(category)},
       (SELECT id FROM pet_types WHERE code = ${sqlString(petType)} LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE ${matchByImage});`.trim());
  }

  return `${statements.join("\n\n")}\n`;
}

const products = normalizeValue(JSON.parse(fs.readFileSync(catalogFile, "utf8")))
  .filter((product) => product.image && product.name)
  .sort((left, right) => left.image.localeCompare(right.image));

fs.writeFileSync(outputFile, buildSql(products), "utf8");
console.log(`Wrote ${products.length} real products to ${path.relative(rootDir, outputFile)}`);
