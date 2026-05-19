import fs from "node:fs";

const products = JSON.parse(fs.readFileSync("data/real-products/paddy-products.json", "utf8"));

function sqlString(value) {
  return `'${String(value).replace(/\\/g, "\\\\").replace(/'/g, "''")}'`;
}

let sql = "-- Use source CDN image URLs so the currently deployed JSP renders images immediately.\n";
sql += "SET NAMES utf8mb4;\n";

for (const product of products) {
  sql += `UPDATE products SET image = ${sqlString(product.image_url)} WHERE name = ${sqlString(product.name)};\n`;
}

fs.writeFileSync("data/real-products/use-source-image-urls.sql", sql, "utf8");
console.log(`Generated ${products.length} image URL updates.`);
