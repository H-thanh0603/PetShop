import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const rootDir = path.resolve(__dirname, "..");
const imageDir = path.join(rootDir, "src", "main", "webapp", "assets", "images", "shop_pic", "products");
const outputFile = path.join(rootDir, "deploy", "seed-melipet-200-products.sql");

const productImages = fs
  .readdirSync(imageDir)
  .filter((name) => /\.(jpg|jpeg|png|webp)$/i.test(name))
  .sort();

if (productImages.length === 0) {
  throw new Error(`No product images found in ${imageDir}`);
}

const brands = [
  "Royal Canin",
  "Whiskas",
  "Pedigree",
  "SmartHeart",
  "Monge",
  "Ganador",
  "Me-O",
  "Kit Cat",
  "Ciao",
  "Trixie",
  "FOFOS",
  "Doggyman",
  "Natural Core",
  "Joycat",
  "Melipet"
];

const productTemplates = [
  { pet: "cat", category: "Thức Ăn Cho Mèo", name: "Hạt dinh dưỡng cho mèo trưởng thành", price: 145000, weight: 1500 },
  { pet: "cat", category: "Thức Ăn Cho Mèo", name: "Hạt cho mèo con vị cá hồi", price: 132000, weight: 1200 },
  { pet: "cat", category: "Thức Ăn Cho Mèo", name: "Pate mèo vị cá ngừ", price: 25000, weight: 85 },
  { pet: "cat", category: "Thức Ăn Cho Mèo", name: "Súp thưởng cho mèo vị gà", price: 39000, weight: 60 },
  { pet: "cat", category: "Thức Ăn Cho Mèo", name: "Snack thưởng mềm cho mèo", price: 49000, weight: 75 },
  { pet: "cat", category: "Thức Ăn Cho Mèo", name: "Sữa không lactose cho mèo", price: 42000, weight: 200 },
  { pet: "cat", category: "Vệ Sinh Cho Mèo", name: "Cát vệ sinh đậu nành khử mùi", price: 125000, weight: 2500 },
  { pet: "cat", category: "Vệ Sinh Cho Mèo", name: "Cát vệ sinh bentonite vón cục", price: 89000, weight: 5000 },
  { pet: "cat", category: "Vệ Sinh Cho Mèo", name: "Xịt khử mùi khu vực vệ sinh mèo", price: 99000, weight: 450 },
  { pet: "cat", category: "Vệ Sinh Cho Mèo", name: "Khay vệ sinh mèo thành cao", price: 185000, weight: 1000 },
  { pet: "cat", category: "Đồ Chơi Cho Mèo", name: "Cần câu lông vũ cho mèo", price: 35000, weight: 80 },
  { pet: "cat", category: "Đồ Chơi Cho Mèo", name: "Bàn cào móng giấy cho mèo", price: 149000, weight: 600 },
  { pet: "cat", category: "Đồ Chơi Cho Mèo", name: "Bóng chuông tương tác cho mèo", price: 29000, weight: 60 },
  { pet: "cat", category: "Đồ Chơi Cho Mèo", name: "Trụ cào móng dây thừng", price: 235000, weight: 900 },
  { pet: "cat", category: "Phụ Kiện Cho Mèo", name: "Bát ăn inox chống trượt cho mèo", price: 55000, weight: 250 },
  { pet: "cat", category: "Phụ Kiện Cho Mèo", name: "Nệm nằm êm ái cho mèo", price: 189000, weight: 800 },
  { pet: "cat", category: "Phụ Kiện Cho Mèo", name: "Balo vận chuyển mèo thoáng khí", price: 385000, weight: 1200 },
  { pet: "cat", category: "Phụ Kiện Cho Mèo", name: "Vòng cổ mèo có chuông an toàn", price: 45000, weight: 90 },
  { pet: "cat", category: "Chăm Sóc Sức Khỏe Cho Mèo", name: "Gel dinh dưỡng hỗ trợ búi lông", price: 115000, weight: 120 },
  { pet: "cat", category: "Chăm Sóc Sức Khỏe Cho Mèo", name: "Viên dầu cá hồi omega cho mèo", price: 89000, weight: 200 },
  { pet: "dog", category: "Thức Ăn Cho Chó", name: "Hạt dinh dưỡng cho chó trưởng thành", price: 165000, weight: 1500 },
  { pet: "dog", category: "Thức Ăn Cho Chó", name: "Hạt cho chó con giống nhỏ", price: 158000, weight: 1300 },
  { pet: "dog", category: "Thức Ăn Cho Chó", name: "Pate chó vị bò và rau củ", price: 28000, weight: 130 },
  { pet: "dog", category: "Thức Ăn Cho Chó", name: "Snack que gặm sạch răng cho chó", price: 59000, weight: 120 },
  { pet: "dog", category: "Thức Ăn Cho Chó", name: "Xúc xích thưởng vị gà cho chó", price: 36000, weight: 100 },
  { pet: "dog", category: "Thức Ăn Cho Chó", name: "Thịt sấy thưởng huấn luyện cho chó", price: 78000, weight: 100 },
  { pet: "dog", category: "Vệ Sinh Cho Chó", name: "Sữa tắm dưỡng lông cho chó", price: 135000, weight: 355 },
  { pet: "dog", category: "Vệ Sinh Cho Chó", name: "Xịt khử mùi lông chó", price: 99000, weight: 450 },
  { pet: "dog", category: "Vệ Sinh Cho Chó", name: "Dung dịch vệ sinh tai cho chó", price: 85000, weight: 120 },
  { pet: "dog", category: "Vệ Sinh Cho Chó", name: "Tã lót vệ sinh cho chó cái", price: 115000, weight: 300 },
  { pet: "dog", category: "Đồ Chơi Cho Chó", name: "Bóng cao su luyện vận động cho chó", price: 45000, weight: 180 },
  { pet: "dog", category: "Đồ Chơi Cho Chó", name: "Dây thừng gặm nhai cho chó", price: 65000, weight: 250 },
  { pet: "dog", category: "Đồ Chơi Cho Chó", name: "Thú bông có chuông cho chó", price: 89000, weight: 300 },
  { pet: "dog", category: "Đồ Chơi Cho Chó", name: "Xương gặm đồ chơi cao su", price: 72000, weight: 220 },
  { pet: "dog", category: "Phụ Kiện Cho Chó", name: "Vòng cổ và dây dắt cho chó", price: 85000, weight: 250 },
  { pet: "dog", category: "Phụ Kiện Cho Chó", name: "Dây dắt tự động cho chó", price: 195000, weight: 450 },
  { pet: "dog", category: "Phụ Kiện Cho Chó", name: "Bát ăn inox chống trượt cho chó", price: 69000, weight: 300 },
  { pet: "dog", category: "Phụ Kiện Cho Chó", name: "Lồng vận chuyển chó mèo", price: 520000, weight: 1800 },
  { pet: "dog", category: "Chăm Sóc Sức Khỏe Cho Chó", name: "Viên bổ sung canxi cho chó", price: 125000, weight: 180 },
  { pet: "dog", category: "Chăm Sóc Sức Khỏe Cho Chó", name: "Dầu cá hồi omega cho chó", price: 99000, weight: 200 }
];

const variants = [
  "gói tiêu chuẩn",
  "combo tiết kiệm",
  "size mini",
  "size lớn",
  "hương vị mới"
];

function sqlString(value) {
  return `'${String(value).replace(/\\/g, "\\\\").replace(/'/g, "''")}'`;
}

function productName(template, index) {
  const brand = brands[index % brands.length];
  const variant = variants[Math.floor(index / productTemplates.length) % variants.length];
  return `${brand} ${template.name} - ${variant}`;
}

function productDescription(template, index) {
  const petLabel = template.pet === "cat" ? "mèo" : "chó";
  const variant = variants[Math.floor(index / productTemplates.length) % variants.length];
  return [
    `${template.name} thuộc nhóm ${template.category.toLowerCase()}, phù hợp cho ${petLabel} dùng hằng ngày.`,
    `Phiên bản ${variant} được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng.`,
    "Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức."
  ].join(" ");
}

function buildProduct(index) {
  const template = productTemplates[index % productTemplates.length];
  const name = productName(template, index);
  const priceStep = (index % 7) * 7000;
  const discount = [0, 0, 5, 10, 12, 15, 20][index % 7];
  const stock = 25 + ((index * 13) % 116);
  const weight = template.weight + ((index % 5) * Math.max(10, Math.round(template.weight * 0.08)));
  const image = `products/${productImages[index % productImages.length]}`;

  return {
    name,
    image,
    price: template.price + priceStep,
    discount,
    description: productDescription(template, index),
    stock,
    weight,
    category: template.category,
    pet: template.pet
  };
}

const products = Array.from({ length: 200 }, (_, index) => buildProduct(index));

const statements = [
  "-- Melipet product restore seed: 200 UTF-8 products with local images.",
  "-- Safe to run multiple times because every insert checks product name first.",
  "SET NAMES utf8mb4;",
  "",
  "INSERT INTO pet_types (code, name, icon, display_order, is_active) VALUES",
  "('dog', 'Chó', 'bxs-dog', 1, 1),",
  "('cat', 'Mèo', 'bxs-cat', 2, 1)",
  "ON DUPLICATE KEY UPDATE name = VALUES(name), icon = VALUES(icon), display_order = VALUES(display_order), is_active = 1;",
  ""
];

for (const product of products) {
  statements.push(`
INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT ${sqlString(product.name)}, ${sqlString(product.image)}, ${product.price}, ${product.discount},
       ${sqlString(product.description)}, ${product.stock}, ${product.weight}, ${sqlString(product.category)},
       (SELECT id FROM pet_types WHERE code = ${sqlString(product.pet)} LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = ${sqlString(product.name)});
`.trim());
}

fs.mkdirSync(path.dirname(outputFile), { recursive: true });
fs.writeFileSync(outputFile, `${statements.join("\n\n")}\n`, "utf8");
console.log(`Wrote ${products.length} products to ${path.relative(rootDir, outputFile)}`);
