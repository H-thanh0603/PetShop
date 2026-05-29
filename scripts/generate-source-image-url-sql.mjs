import fs from "node:fs";

const products = JSON.parse(fs.readFileSync("data/real-products/paddy-products.json", "utf8"));

function sqlString(value) {
  return `'${String(value).replace(/\\/g, "\\\\").replace(/'/g, "''")}'`;
}

let sql = "-- Use source CDN image URLs when the deployed app cannot serve local product assets yet.\n";
sql += "SET NAMES utf8mb4;\n";

for (const product of products) {
  sql += `UPDATE products SET image = ${sqlString(product.image_url)} WHERE image = ${sqlString(product.image)} OR name = ${sqlString(product.name)};\n`;
}

fs.writeFileSync("data/real-products/use-source-image-urls.sql", sql, "utf8");
console.log(`Generated ${products.length} image URL updates.`);
