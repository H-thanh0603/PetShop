import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const rootDir = path.resolve(__dirname, "..");
const catalogFile = path.join(rootDir, "data", "real-products", "paddy-products.json");
const seedFile = path.join(rootDir, "deploy", "seed-melipet-200-products.sql");
const imageDir = path.join(rootDir, "src", "main", "webapp", "assets", "images", "shop_pic");

function slugify(value) {
  return value
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/đ/g, "d")
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(message);
  }
}

function sqlString(value) {
  return `'${String(value ?? "").replace(/\\/g, "\\\\").replace(/'/g, "''")}'`;
}

const products = JSON.parse(fs.readFileSync(catalogFile, "utf8"));
const seedSql = fs.readFileSync(seedFile, "utf8");
const mojibakePattern = /(Ã|Â|Ä|Æ|áº|á»|â€)/;

assert(products.length > 0, "No products found in Paddy catalog.");
assert(!seedSql.includes("gói tiêu chuẩn"), "Deploy seed still contains generated template variants.");
assert((seedSql.match(/INSERT INTO products/g) || []).length === products.length, "Deploy seed insert count does not match catalog size.");
assert(seedSql.includes("UPDATE products"), "Deploy seed must repair existing rows by image before insert.");

for (const product of products) {
  const searchableText = [
    product.name,
    product.description,
    product.category,
    product.brand,
    product.product_type
  ].join(" ");
  const handle = product.source_url.split("/").pop();
  const imagePath = path.join(imageDir, product.image);

  assert(!mojibakePattern.test(searchableText), `Mojibake text remains in ${product.image}.`);
  assert(fs.existsSync(imagePath), `Missing local image for ${product.image}.`);
  assert(product.image.includes(slugify(handle).slice(0, 25)), `Image filename does not match source URL for ${product.source_url}.`);
  assert(seedSql.includes(product.image), `Deploy seed does not include ${product.image}.`);
  assert(seedSql.includes(sqlString(product.name)), `Deploy seed does not include product name "${product.name}".`);
}

console.log(`Validated ${products.length} real Paddy products.`);
