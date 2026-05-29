import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const rootDir = path.resolve(__dirname, "..");
const dataDir = path.join(rootDir, "data", "real-products");
const jsonFile = path.join(dataDir, "paddy-products.json");
const csvFile = path.join(dataDir, "paddy-products.csv");
const sqlFile = path.join(dataDir, "paddy-products.sql");
const imageUrlSqlFile = path.join(dataDir, "use-source-image-urls.sql");

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

function csvCell(value) {
  return `"${String(value ?? "").replace(/"/g, '""')}"`;
}

function descriptionFor(product) {
  return [
    truncate(product.description || product.name, 260),
    product.brand ? `Thương hiệu: ${product.brand}.` : "",
    `Nguồn tham khảo: ${product.source_url}`
  ].filter(Boolean).join("\n\n");
}

function buildCsv(products) {
  const columns = [
    "name", "price", "discount", "category", "pet_type_code", "stock", "weight",
    "image", "brand", "product_type", "sku", "source_url", "image_url", "description"
  ];
  const rows = products.map((product) => columns.map((column) => csvCell(product[column])).join(","));
  return `${columns.join(",")}\n${rows.join("\n")}\n`;
}

function buildImportSql(products) {
  const statements = [
    "-- Real product seed generated from public Paddy product data.",
    "-- Source: https://paddy.vn/products.json",
    "SET NAMES utf8mb4;",
    "INSERT INTO pet_types (code, name, icon, display_order, is_active) VALUES",
    "('dog', 'Chó', 'bxs-dog', 1, 1),",
    "('cat', 'Mèo', 'bxs-cat', 2, 1)",
    "ON DUPLICATE KEY UPDATE name = VALUES(name), icon = VALUES(icon), display_order = VALUES(display_order), is_active = 1;"
  ];

  for (const product of products) {
    const localImage = product.image;
    const sourceImage = product.image_url || product.image;
    const imageMatch = `image IN (${sqlString(localImage)}, ${sqlString(sourceImage)})`;

    statements.push(`
UPDATE products
SET name = ${sqlString(product.name)},
    image = ${sqlString(localImage)},
    price = ${Math.round(Number(product.price) || 0)},
    discount = ${Math.round(Number(product.discount) || 0)},
    description = ${sqlString(descriptionFor(product))},
    stock = ${Math.round(Number(product.stock) || 8)},
    weight = ${Math.max(Math.round(Number(product.weight) || 100), 1)},
    category = ${sqlString(product.category)},
    pet_type_id = (SELECT id FROM pet_types WHERE code = ${sqlString(product.pet_type_code)} LIMIT 1),
    is_active = 1
WHERE ${imageMatch};

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT ${sqlString(product.name)}, ${sqlString(localImage)}, ${Math.round(Number(product.price) || 0)}, ${Math.round(Number(product.discount) || 0)},
       ${sqlString(descriptionFor(product))}, ${Math.round(Number(product.stock) || 8)}, ${Math.max(Math.round(Number(product.weight) || 100), 1)}, ${sqlString(product.category)},
       (SELECT id FROM pet_types WHERE code = ${sqlString(product.pet_type_code)} LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE ${imageMatch});`.trim());
  }

  return `${statements.join("\n\n")}\n`;
}

function buildImageUrlSql(products) {
  const lines = [
    "-- Use source CDN image URLs when the deployed app cannot serve local product assets yet.",
    "SET NAMES utf8mb4;"
  ];

  for (const product of products) {
    lines.push(
      `UPDATE products SET image = ${sqlString(product.image_url)} WHERE image = ${sqlString(product.image)} OR name = ${sqlString(product.name)};`
    );
  }

  return `${lines.join("\n")}\n`;
}

const products = normalizeValue(JSON.parse(fs.readFileSync(jsonFile, "utf8")))
  .filter((product) => product.name && product.image)
  .sort((left, right) => left.image.localeCompare(right.image));

fs.writeFileSync(jsonFile, `${JSON.stringify(products, null, 2)}\n`, "utf8");
fs.writeFileSync(csvFile, buildCsv(products), "utf8");
fs.writeFileSync(sqlFile, buildImportSql(products), "utf8");
fs.writeFileSync(imageUrlSqlFile, buildImageUrlSql(products), "utf8");

console.log(`Repaired ${products.length} Paddy products.`);
