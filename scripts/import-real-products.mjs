import fs from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const rootDir = path.resolve(__dirname, "..");

const STORE_URL = "https://paddy.vn";
const MAX_PRODUCTS = Number.parseInt(process.env.MAX_PRODUCTS || "100", 10);
const PAGE_LIMIT = 250;
const OUTPUT_DIR = path.join(rootDir, "data", "real-products");
const IMAGE_DIR = path.join(rootDir, "src", "main", "webapp", "assets", "images", "shop_pic", "products");
const SQL_FILE = path.join(OUTPUT_DIR, "paddy-products.sql");
const JSON_FILE = path.join(OUTPUT_DIR, "paddy-products.json");
const CSV_FILE = path.join(OUTPUT_DIR, "paddy-products.csv");

const headers = {
  "accept": "application/json,text/html;q=0.9,*/*;q=0.8",
  "user-agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) PetShopStudentProject/1.0"
};

function stripHtml(html = "") {
  return html
    .replace(/<script[\s\S]*?<\/script>/gi, " ")
    .replace(/<style[\s\S]*?<\/style>/gi, " ")
    .replace(/<[^>]+>/g, " ")
    .replace(/&nbsp;/g, " ")
    .replace(/&amp;/g, "&")
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/\s+/g, " ")
    .trim();
}

function truncate(value, maxLength) {
  if (!value) {
    return "";
  }
  return value.length <= maxLength ? value : `${value.slice(0, maxLength - 1).trim()}…`;
}

function slugify(value) {
  return value
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/đ/g, "d")
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .slice(0, 80);
}

function inferPetType(product) {
  const text = `${product.title} ${product.product_type || ""} ${(product.tags || []).join(" ")}`.toLowerCase();
  if (/(mèo|meo|cat|kitten)/i.test(text)) {
    return "cat";
  }
  if (/(chó|cho|cún|cun|dog|puppy)/i.test(text)) {
    return "dog";
  }
  return null;
}

function normalizeCategory(product) {
  const text = `${product.product_type || ""} ${product.title}`.toLowerCase();
  if (/pate|hạt|thức ăn|snack|treat|súp|sữa|food|nutrition|bánh thưởng/.test(text)) {
    return inferPetType(product) === "cat" ? "Thức Ăn Cho Mèo" : "Thức Ăn Cho Chó";
  }
  if (/cát|khay|vệ sinh|tắm|sữa tắm|khăn|lược|shampoo|toilet/.test(text)) {
    return inferPetType(product) === "cat" ? "Vệ Sinh Cho Mèo" : "Vệ Sinh Cho Chó";
  }
  if (/đồ chơi|toy|cần câu|bóng|cào móng/.test(text)) {
    return inferPetType(product) === "cat" ? "Đồ Chơi Cho Mèo" : "Đồ Chơi Cho Chó";
  }
  if (/balo|túi|dây|vòng cổ|phụ kiện|bát|ổ|nệm|chuồng|nhà|carrier|bag|leash|bowl/.test(text)) {
    return inferPetType(product) === "cat" ? "Phụ Kiện Cho Mèo" : "Phụ Kiện Cho Chó";
  }
  return product.product_type || (inferPetType(product) === "cat" ? "Sản Phẩm Cho Mèo" : "Sản Phẩm Cho Chó");
}

function pickVariant(product) {
  const variants = product.variants || [];
  const available = variants.find((variant) => variant.available);
  return available || variants[0] || null;
}

function pickImage(product, variant) {
  return variant?.featured_image?.src || product.images?.[0]?.src || null;
}

function discountPercent(price, compareAtPrice) {
  if (!compareAtPrice || compareAtPrice <= price) {
    return 0;
  }
  return Math.max(0, Math.min(90, Math.round(((compareAtPrice - price) / compareAtPrice) * 100)));
}

function sqlString(value) {
  if (value === null || value === undefined) {
    return "NULL";
  }
  return `'${String(value).replace(/\\/g, "\\\\").replace(/'/g, "''")}'`;
}

function csvCell(value) {
  return `"${String(value ?? "").replace(/"/g, '""')}"`;
}

async function fetchJson(url) {
  const response = await fetch(url, { headers });
  if (!response.ok) {
    throw new Error(`GET ${url} failed: ${response.status} ${response.statusText}`);
  }
  return response.json();
}

async function downloadImage(url, filePath) {
  const response = await fetch(url, { headers });
  if (!response.ok) {
    throw new Error(`Image download failed: ${response.status} ${url}`);
  }
  const buffer = Buffer.from(await response.arrayBuffer());
  if (buffer.length < 1024) {
    throw new Error(`Image looks too small: ${url}`);
  }
  await fs.writeFile(filePath, buffer);
}

async function collectProducts() {
  const imported = [];
  const seenHandles = new Set();

  for (let page = 1; imported.length < MAX_PRODUCTS && page <= 10; page += 1) {
    const url = `${STORE_URL}/products.json?limit=${PAGE_LIMIT}&page=${page}`;
    console.log(`Fetching ${url}`);
    const payload = await fetchJson(url);
    const products = payload.products || [];
    if (products.length === 0) {
      break;
    }

    for (const product of products) {
      if (imported.length >= MAX_PRODUCTS) {
        break;
      }
      if (seenHandles.has(product.handle)) {
        continue;
      }

      const petType = inferPetType(product);
      const variant = pickVariant(product);
      const imageUrl = pickImage(product, variant);
      if (!petType || !variant || !imageUrl) {
        continue;
      }

      const price = Math.max(0, Math.round(Number.parseFloat(variant.price || "0")));
      if (!price) {
        continue;
      }

      const compareAt = Math.round(Number.parseFloat(variant.compare_at_price || "0"));
      const imageExt = path.extname(new URL(imageUrl).pathname).replace(".", "").toLowerCase() || "jpg";
      const safeExt = ["jpg", "jpeg", "png", "webp"].includes(imageExt) ? imageExt : "jpg";
      const imageName = `paddy_${String(imported.length + 1).padStart(3, "0")}_${slugify(product.handle || product.title)}.${safeExt}`;
      const imagePath = path.join(IMAGE_DIR, imageName);
      const dbImagePath = `products/${imageName}`;

      try {
        await downloadImage(imageUrl, imagePath);
      } catch (error) {
        console.warn(`Skipping image error for "${product.title}": ${error.message}`);
        continue;
      }

      seenHandles.add(product.handle);
      imported.push({
        source: "Paddy Pet Shop",
        source_url: `${STORE_URL}/products/${product.handle}`,
        name: truncate(product.title, 255),
        image: dbImagePath,
        image_url: imageUrl,
        price,
        discount: discountPercent(price, compareAt),
        description: truncate(stripHtml(product.body_html), 260),
        stock: variant.available ? 40 + (imported.length % 45) : 8,
        weight: Math.max(variant.grams || 0, 100),
        category: truncate(normalizeCategory(product), 100),
        pet_type_code: petType,
        brand: product.vendor || "",
        product_type: product.product_type || "",
        sku: variant.sku || ""
      });

      console.log(`  ${imported.length}/${MAX_PRODUCTS}: ${product.title}`);
    }
  }

  return imported;
}

function buildSql(products) {
  const statements = [
    "-- Real product seed generated from public Paddy product data.",
    "-- Source: https://paddy.vn/products.json",
    "SET NAMES utf8mb4;",
    "INSERT IGNORE INTO pet_types (code, name, icon, display_order, is_active) VALUES",
    "('dog', 'Chó', 'bxs-dog', 1, 1),",
    "('cat', 'Mèo', 'bxs-cat', 2, 1);"
  ];

  for (const product of products) {
    const description = [
      product.description,
      product.brand ? `Thương hiệu: ${product.brand}.` : "",
      `Nguồn tham khảo: ${product.source_url}`
    ].filter(Boolean).join("\n\n");

    statements.push(`
INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT ${sqlString(product.name)}, ${sqlString(product.image)}, ${product.price}, ${product.discount},
       ${sqlString(description)}, ${product.stock}, ${product.weight}, ${sqlString(product.category)},
       (SELECT id FROM pet_types WHERE code = ${sqlString(product.pet_type_code)} LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = ${sqlString(product.name)});`.trim());
  }

  return `${statements.join("\n\n")}\n`;
}

function buildCsv(products) {
  const columns = [
    "name", "price", "discount", "category", "pet_type_code", "stock", "weight",
    "image", "brand", "product_type", "sku", "source_url", "image_url", "description"
  ];
  const rows = products.map((product) => columns.map((column) => csvCell(product[column])).join(","));
  return `${columns.join(",")}\n${rows.join("\n")}\n`;
}

async function main() {
  await fs.mkdir(OUTPUT_DIR, { recursive: true });
  await fs.mkdir(IMAGE_DIR, { recursive: true });

  const products = await collectProducts();
  if (products.length < MAX_PRODUCTS) {
    console.warn(`Only collected ${products.length}/${MAX_PRODUCTS} usable products.`);
  }

  await fs.writeFile(JSON_FILE, `${JSON.stringify(products, null, 2)}\n`, "utf8");
  await fs.writeFile(CSV_FILE, buildCsv(products), "utf8");
  await fs.writeFile(SQL_FILE, buildSql(products), "utf8");

  console.log("");
  console.log(`Done. Products: ${products.length}`);
  console.log(`Images: ${path.relative(rootDir, IMAGE_DIR)}`);
  console.log(`JSON:   ${path.relative(rootDir, JSON_FILE)}`);
  console.log(`CSV:    ${path.relative(rootDir, CSV_FILE)}`);
  console.log(`SQL:    ${path.relative(rootDir, SQL_FILE)}`);
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
