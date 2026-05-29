-- Real product seed generated from public Paddy product data.

-- Source: https://paddy.vn/products.json

SET NAMES utf8mb4;

INSERT INTO pet_types (code, name, icon, display_order, is_active) VALUES

('dog', 'Chó', 'bxs-dog', 1, 1),

('cat', 'Mèo', 'bxs-cat', 2, 1)

ON DUPLICATE KEY UPDATE name = VALUES(name), icon = VALUES(icon), display_order = VALUES(display_order), is_active = 1;

UPDATE products
SET name = 'Bánh Thưởng Cho Mèo Ức Gà Hấp Nước Cốt Gà Cattyman',
    image = 'products/paddy_001_banh-thuong-cho-meo-uc-ga-hap-nuoc-cot-ga-cattyman.jpg',
    price = 17000,
    discount = 0,
    description = 'Bánh Thưởng Cho Mèo Ức Gà Hấp Nước Cốt Gà Cattyman Thương hiệu: CattyMan Phù hợp cho: Mèo mọi lứa tuổi Bánh thưởng cho mèo Cattyman được làm từ phi lê ức gà nguyên miếng hấp chín, giữ trọn vị ngọt tự nhiên, kết hợp cùng nước cốt cá ngừ hoặc cua giúp tăng độ...

Thương hiệu: CattyMan.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-meo-uc-ga-hap-nuoc-cot-ga-cattyman',
    stock = 8,
    weight = 100,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_001_banh-thuong-cho-meo-uc-ga-hap-nuoc-cot-ga-cattyman.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/banh-thuong-cho-meo-uc-ga-hap-nuoc-cot-ga-cattyman_4.jpg?v=1777352253');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Cho Mèo Ức Gà Hấp Nước Cốt Gà Cattyman', 'products/paddy_001_banh-thuong-cho-meo-uc-ga-hap-nuoc-cot-ga-cattyman.jpg', 17000, 0,
       'Bánh Thưởng Cho Mèo Ức Gà Hấp Nước Cốt Gà Cattyman Thương hiệu: CattyMan Phù hợp cho: Mèo mọi lứa tuổi Bánh thưởng cho mèo Cattyman được làm từ phi lê ức gà nguyên miếng hấp chín, giữ trọn vị ngọt tự nhiên, kết hợp cùng nước cốt cá ngừ hoặc cua giúp tăng độ...

Thương hiệu: CattyMan.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-meo-uc-ga-hap-nuoc-cot-ga-cattyman', 8, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_001_banh-thuong-cho-meo-uc-ga-hap-nuoc-cot-ga-cattyman.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/banh-thuong-cho-meo-uc-ga-hap-nuoc-cot-ga-cattyman_4.jpg?v=1777352253'));

UPDATE products
SET name = 'Bánh Thưởng Que Gân Bò Cho Chó Doggyman 100g',
    image = 'products/paddy_002_que-gan-bo-cho-cho-doggyman-100g.jpg',
    price = 72000,
    discount = 0,
    description = 'Bánh Thưởng Que Gân Bò Cho Chó Doggyman 100g Thương hiệu: Doggyman Phù hợp cho: Chó từ 3 tháng tuổi trở lên Bánh thưởng cho chó sự kết hợp giữa da bò và gân bò giàu chondroitin, sản phẩm mang lại độ đàn hồi lý tưởng giúp cún cưng tận hưởng cảm giác nhai phấ...

Thương hiệu: DoggyMan.

Nguồn tham khảo: https://paddy.vn/products/que-gan-bo-cho-cho-doggyman-100g',
    stock = 41,
    weight = 150,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_002_que-gan-bo-cho-cho-doggyman-100g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/que-gan-bo-cho-cho-doggyman-100g.jpg?v=1776934943');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Que Gân Bò Cho Chó Doggyman 100g', 'products/paddy_002_que-gan-bo-cho-cho-doggyman-100g.jpg', 72000, 0,
       'Bánh Thưởng Que Gân Bò Cho Chó Doggyman 100g Thương hiệu: Doggyman Phù hợp cho: Chó từ 3 tháng tuổi trở lên Bánh thưởng cho chó sự kết hợp giữa da bò và gân bò giàu chondroitin, sản phẩm mang lại độ đàn hồi lý tưởng giúp cún cưng tận hưởng cảm giác nhai phấ...

Thương hiệu: DoggyMan.

Nguồn tham khảo: https://paddy.vn/products/que-gan-bo-cho-cho-doggyman-100g', 41, 150, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_002_que-gan-bo-cho-cho-doggyman-100g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/que-gan-bo-cho-cho-doggyman-100g.jpg?v=1776934943'));

UPDATE products
SET name = 'Vòng Cổ Và Dây Dắt Cho Chó Police',
    image = 'products/paddy_003_vong-co-va-day-dat-cho-cho-police.jpg',
    price = 35000,
    discount = 0,
    description = 'Vòng Cổ Và Dây Dắt Cho Chó Police Thương hiệu: Paddy Phù hợp cho: Chó mọi lứa tuổi Vòng Cổ Và Dây Dắt Cho Chó Police có thiết kế vòng đệm êm ái, chắc chắn với dòng chữ Police Dog nổi bật. Chi tiết phát quang hỗ trợ nhận diện trong điều kiện thiếu sáng, giúp...

Thương hiệu: Paddy Pet Shop.

Nguồn tham khảo: https://paddy.vn/products/vong-co-va-day-dat-cho-cho-police',
    stock = 8,
    weight = 100,
    category = 'Phụ Kiện Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_003_vong-co-va-day-dat-cho-cho-police.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/vong-co-va-day-dat-cho-cho-police_5.jpg?v=1776835827');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Vòng Cổ Và Dây Dắt Cho Chó Police', 'products/paddy_003_vong-co-va-day-dat-cho-cho-police.jpg', 35000, 0,
       'Vòng Cổ Và Dây Dắt Cho Chó Police Thương hiệu: Paddy Phù hợp cho: Chó mọi lứa tuổi Vòng Cổ Và Dây Dắt Cho Chó Police có thiết kế vòng đệm êm ái, chắc chắn với dòng chữ Police Dog nổi bật. Chi tiết phát quang hỗ trợ nhận diện trong điều kiện thiếu sáng, giúp...

Thương hiệu: Paddy Pet Shop.

Nguồn tham khảo: https://paddy.vn/products/vong-co-va-day-dat-cho-cho-police', 8, 100, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_003_vong-co-va-day-dat-cho-cho-police.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/vong-co-va-day-dat-cho-cho-police_5.jpg?v=1776835827'));

UPDATE products
SET name = 'Xúc xích Cho Chó Gà Mini Doggyman',
    image = 'products/paddy_004_xuc-xich-cho-cho-ga-mini-doggyman.jpg',
    price = 30000,
    discount = 0,
    description = 'Xúc Xích Cho Chó Gà Mini Doggyman Thương hiệu: Doggyman Phù hợp cho: Chó mọi lứa tuổi Bánh thưởng cho chó Doggyman có hương vị ngon ngọt của thịt gà chất lượng cao, đậm đà trong từng miếng cắn. Kết cấu mềm mịn, dễ ăn, phù hợp cho cả chó con và chó già có lự...

Thương hiệu: DoggyMan.

Nguồn tham khảo: https://paddy.vn/products/xuc-xich-cho-cho-ga-mini-doggyman',
    stock = 8,
    weight = 100,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_004_xuc-xich-cho-cho-ga-mini-doggyman.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/xuc-xich-cho-cho-ga-mini-doggyman_3.jpg?v=1776766196');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Xúc xích Cho Chó Gà Mini Doggyman', 'products/paddy_004_xuc-xich-cho-cho-ga-mini-doggyman.jpg', 30000, 0,
       'Xúc Xích Cho Chó Gà Mini Doggyman Thương hiệu: Doggyman Phù hợp cho: Chó mọi lứa tuổi Bánh thưởng cho chó Doggyman có hương vị ngon ngọt của thịt gà chất lượng cao, đậm đà trong từng miếng cắn. Kết cấu mềm mịn, dễ ăn, phù hợp cho cả chó con và chó già có lự...

Thương hiệu: DoggyMan.

Nguồn tham khảo: https://paddy.vn/products/xuc-xich-cho-cho-ga-mini-doggyman', 8, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_004_xuc-xich-cho-cho-ga-mini-doggyman.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/xuc-xich-cho-cho-ga-mini-doggyman_3.jpg?v=1776766196'));

UPDATE products
SET name = 'Hạt Siêu Topping Cho Mèo Kings Pet',
    image = 'products/paddy_005_hat-sieu-topping-cho-meo-kings-pet.jpg',
    price = 155000,
    discount = 0,
    description = 'Hạt Siêu Topping Cho Mèo Kings Pet Thương hiệu: King''s Pet Phù hợp cho: Mèo trưởng thành Hạt cho mèo siêu topping King’s Pet là dòng thức ăn cao cấp cho mèo trưởng thành, nổi bật với topping cá ngừ sấy thăng hoa giữ trọn hương vị và dinh dưỡng như đồ tươi....

Thương hiệu: Kings Pet.

Nguồn tham khảo: https://paddy.vn/products/hat-sieu-topping-cho-meo-kings-pet',
    stock = 8,
    weight = 1200,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_005_hat-sieu-topping-cho-meo-kings-pet.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-sieu-topping-cho-meo-kings-pet_5.jpg?v=1776760567');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Siêu Topping Cho Mèo Kings Pet', 'products/paddy_005_hat-sieu-topping-cho-meo-kings-pet.jpg', 155000, 0,
       'Hạt Siêu Topping Cho Mèo Kings Pet Thương hiệu: King''s Pet Phù hợp cho: Mèo trưởng thành Hạt cho mèo siêu topping King’s Pet là dòng thức ăn cao cấp cho mèo trưởng thành, nổi bật với topping cá ngừ sấy thăng hoa giữ trọn hương vị và dinh dưỡng như đồ tươi....

Thương hiệu: Kings Pet.

Nguồn tham khảo: https://paddy.vn/products/hat-sieu-topping-cho-meo-kings-pet', 8, 1200, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_005_hat-sieu-topping-cho-meo-kings-pet.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-sieu-topping-cho-meo-kings-pet_5.jpg?v=1776760567'));

UPDATE products
SET name = 'Lồng Vận Chuyển Hàng Không Cho Chó Mon Ami Carrier',
    image = 'products/paddy_006_long-van-chuyen-hang-khong-cho-cho-mon-ami-carrier.jpg',
    price = 730000,
    discount = 0,
    description = 'Lồng Vận Chuyển Hàng Không Cho Chó Mon Ami Carrier Thương hiệu: Mon Ami Phù hợp cho: Chó mọi lứa tuổi Lồng vận chuyển Mon Ami là giải pháp tiện lợi và an toàn giúp bạn đưa thú cưng di chuyển dễ dàng. Thiết kế chắc chắn, thông thoáng, đạt tiêu chuẩn hàng khô...

Thương hiệu: Mon Ami.

Nguồn tham khảo: https://paddy.vn/products/long-van-chuyen-hang-khong-cho-cho-mon-ami-carrier',
    stock = 8,
    weight = 100,
    category = 'Phụ Kiện Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_006_long-van-chuyen-hang-khong-cho-cho-mon-ami-carrier.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/long-van-chuyen-hang-khong-cho-cho-mon-ami-carrier_7b50fb1f-bc3b-4d6d-a0c7-77c979e238cf.jpg?v=1776764107');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Lồng Vận Chuyển Hàng Không Cho Chó Mon Ami Carrier', 'products/paddy_006_long-van-chuyen-hang-khong-cho-cho-mon-ami-carrier.jpg', 730000, 0,
       'Lồng Vận Chuyển Hàng Không Cho Chó Mon Ami Carrier Thương hiệu: Mon Ami Phù hợp cho: Chó mọi lứa tuổi Lồng vận chuyển Mon Ami là giải pháp tiện lợi và an toàn giúp bạn đưa thú cưng di chuyển dễ dàng. Thiết kế chắc chắn, thông thoáng, đạt tiêu chuẩn hàng khô...

Thương hiệu: Mon Ami.

Nguồn tham khảo: https://paddy.vn/products/long-van-chuyen-hang-khong-cho-cho-mon-ami-carrier', 8, 100, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_006_long-van-chuyen-hang-khong-cho-cho-mon-ami-carrier.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/long-van-chuyen-hang-khong-cho-cho-mon-ami-carrier_7b50fb1f-bc3b-4d6d-a0c7-77c979e238cf.jpg?v=1776764107'));

UPDATE products
SET name = 'Hạt Cho Mèo 5Plus Catmix Ruốc Gà Sấy Khô',
    image = 'products/paddy_007_hat-cho-meo-5plus-catmix-ruoc-ga-say-kho.jpg',
    price = 70000,
    discount = 0,
    description = 'Hạt Cho Mèo 5Plus Catmix Ruốc Gà Sấy Khô Thương hiệu: CatChy Phù hợp cho: Mèo mọi lứa tuổi Hạt cho mèo 5Plus Catmix là dòng hạt dinh dưỡng cho mèo kết hợp hoàn hảo giữa chất lượng và giá thành hợp lý, phù hợp cho các “sen” muốn tối ưu chi phí nhưng vẫn đảm...

Thương hiệu: Catchy.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-5plus-catmix-ruoc-ga-say-kho',
    stock = 8,
    weight = 100,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_007_hat-cho-meo-5plus-catmix-ruoc-ga-say-kho.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-meo-5plus-catmix-ruoc-ga-say-kho_2.jpg?v=1776337696');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo 5Plus Catmix Ruốc Gà Sấy Khô', 'products/paddy_007_hat-cho-meo-5plus-catmix-ruoc-ga-say-kho.jpg', 70000, 0,
       'Hạt Cho Mèo 5Plus Catmix Ruốc Gà Sấy Khô Thương hiệu: CatChy Phù hợp cho: Mèo mọi lứa tuổi Hạt cho mèo 5Plus Catmix là dòng hạt dinh dưỡng cho mèo kết hợp hoàn hảo giữa chất lượng và giá thành hợp lý, phù hợp cho các “sen” muốn tối ưu chi phí nhưng vẫn đảm...

Thương hiệu: Catchy.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-5plus-catmix-ruoc-ga-say-kho', 8, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_007_hat-cho-meo-5plus-catmix-ruoc-ga-say-kho.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-meo-5plus-catmix-ruoc-ga-say-kho_2.jpg?v=1776337696'));

UPDATE products
SET name = 'Bột Vi Sinh Khử Mùi Cho Chó Mèo Joycat 54g',
    image = 'products/paddy_008_bot-vi-sinh-khu-mui-cho-cho-meo-joycat-54g.jpg',
    price = 39000,
    discount = 0,
    description = 'Bột Vi Sinh Khử Mùi Cho Chó Mèo Joycat 54g Thương hiệu: Joycat Phù hợp cho: Chó/Mèo mọi lứa tuổi Khử mùi chó mèo JOYCAT là giải pháp kiểm soát mùi hôi thùng cát hiệu quả, ứng dụng công nghệ Dual E.M Bio-Technology™ giúp loại bỏ mùi và diệt khuẩn đến 99%. Sả...

Thương hiệu: Joycat.

Nguồn tham khảo: https://paddy.vn/products/bot-vi-sinh-khu-mui-cho-cho-meo-joycat-54g',
    stock = 8,
    weight = 100,
    category = 'Vệ Sinh Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_008_bot-vi-sinh-khu-mui-cho-cho-meo-joycat-54g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/bot-vi-sinh-khu-mui-cho-cho-meo-joycat_2.jpg?v=1776326018');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bột Vi Sinh Khử Mùi Cho Chó Mèo Joycat 54g', 'products/paddy_008_bot-vi-sinh-khu-mui-cho-cho-meo-joycat-54g.jpg', 39000, 0,
       'Bột Vi Sinh Khử Mùi Cho Chó Mèo Joycat 54g Thương hiệu: Joycat Phù hợp cho: Chó/Mèo mọi lứa tuổi Khử mùi chó mèo JOYCAT là giải pháp kiểm soát mùi hôi thùng cát hiệu quả, ứng dụng công nghệ Dual E.M Bio-Technology™ giúp loại bỏ mùi và diệt khuẩn đến 99%. Sả...

Thương hiệu: Joycat.

Nguồn tham khảo: https://paddy.vn/products/bot-vi-sinh-khu-mui-cho-cho-meo-joycat-54g', 8, 100, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_008_bot-vi-sinh-khu-mui-cho-cho-meo-joycat-54g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/bot-vi-sinh-khu-mui-cho-cho-meo-joycat_2.jpg?v=1776326018'));

UPDATE products
SET name = 'Xịt Vi Sinh Khử Mùi Cho Chó Mèo Joycat 450ml',
    image = 'products/paddy_009_xit-vi-sinh-khu-mui-cho-cho-meo-joycat-450ml.jpg',
    price = 129000,
    discount = 0,
    description = 'Xịt Vi Sinh Khử Mùi Cho Chó Mèo Joycat 450ml Thương hiệu: JoyCat Phù hợp cho: Chó/Mèo mọi lứa tuổi Xịt khử mùi là giải pháp làm sạch và khử mùi hiệu quả cho không gian sống của thú cưng, ứng dụng công nghệ vi sinh E.M Elimination Formula™ giúp loại bỏ mùi h...

Thương hiệu: Joycat.

Nguồn tham khảo: https://paddy.vn/products/xit-vi-sinh-khu-mui-cho-cho-meo-joycat-450ml',
    stock = 8,
    weight = 700,
    category = 'Vệ Sinh Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_009_xit-vi-sinh-khu-mui-cho-cho-meo-joycat-450ml.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/xit-vi-sinh-khu-mui-cho-cho-meo-joycat_2.jpg?v=1776323981');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Xịt Vi Sinh Khử Mùi Cho Chó Mèo Joycat 450ml', 'products/paddy_009_xit-vi-sinh-khu-mui-cho-cho-meo-joycat-450ml.jpg', 129000, 0,
       'Xịt Vi Sinh Khử Mùi Cho Chó Mèo Joycat 450ml Thương hiệu: JoyCat Phù hợp cho: Chó/Mèo mọi lứa tuổi Xịt khử mùi là giải pháp làm sạch và khử mùi hiệu quả cho không gian sống của thú cưng, ứng dụng công nghệ vi sinh E.M Elimination Formula™ giúp loại bỏ mùi h...

Thương hiệu: Joycat.

Nguồn tham khảo: https://paddy.vn/products/xit-vi-sinh-khu-mui-cho-cho-meo-joycat-450ml', 8, 700, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_009_xit-vi-sinh-khu-mui-cho-cho-meo-joycat-450ml.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/xit-vi-sinh-khu-mui-cho-cho-meo-joycat_2.jpg?v=1776323981'));

UPDATE products
SET name = 'Hạt Cho Mèo Nutri Plan PLUS 1.5kg',
    image = 'products/paddy_010_hat-cho-meo-nutri-plan-plus-1-5kg.jpg',
    price = 190000,
    discount = 0,
    description = 'Hạt Cho Mèo Nutri Plan PLUS 1.5kg Thương hiệu: Nutri Plan Phù hợp cho: Mèo mọi lứa tuổi Nutri Plan Plus là sản phẩm thức ăn cho mèo cao cấp, phù hợp với tất cả các lứa tuổi mèo. Sản phẩm được sản xuất từ nguồn nguyên liệu tươi ngon, đảm bảo chất lượng và an...

Thương hiệu: Nutri Plan.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-nutri-plan-plus-1-5kg',
    stock = 8,
    weight = 1700,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_010_hat-cho-meo-nutri-plan-plus-1-5kg.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-meo-nutri-plan-plus-1-5kg_3.jpg?v=1776313433');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo Nutri Plan PLUS 1.5kg', 'products/paddy_010_hat-cho-meo-nutri-plan-plus-1-5kg.jpg', 190000, 0,
       'Hạt Cho Mèo Nutri Plan PLUS 1.5kg Thương hiệu: Nutri Plan Phù hợp cho: Mèo mọi lứa tuổi Nutri Plan Plus là sản phẩm thức ăn cho mèo cao cấp, phù hợp với tất cả các lứa tuổi mèo. Sản phẩm được sản xuất từ nguồn nguyên liệu tươi ngon, đảm bảo chất lượng và an...

Thương hiệu: Nutri Plan.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-nutri-plan-plus-1-5kg', 8, 1700, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_010_hat-cho-meo-nutri-plan-plus-1-5kg.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-meo-nutri-plan-plus-1-5kg_3.jpg?v=1776313433'));

UPDATE products
SET name = 'Cát Vệ Sinh Mèo JoyCat Ultimate Health IQ Giúp Theo Dõi Sức Khỏe',
    image = 'products/paddy_011_cat-ve-sinh-cho-meo-joycat.jpg',
    price = 159000,
    discount = 0,
    description = 'Cát Vệ Sinh Mèo JoyCat Ultimate Health IQ Giúp Theo Dõi Sức Khỏe Thương hiệu: Joycat Phù hợp cho: Mèo mọi lứa tuổi Sản phẩm cát vệ sinh mèo kết hợp cát khoáng tự nhiên và đậu nành theo công thức tối ưu mang lại hiệu quả sử dụng vượt trội và tiết kiệm Lợi íc...

Thương hiệu: Joycat.

Nguồn tham khảo: https://paddy.vn/products/cat-ve-sinh-cho-meo-joycat',
    stock = 8,
    weight = 100,
    category = 'Vệ Sinh Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_011_cat-ve-sinh-cho-meo-joycat.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/cat-ve-sinh-cho-meo-joycat_4.jpg?v=1776413233');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Cát Vệ Sinh Mèo JoyCat Ultimate Health IQ Giúp Theo Dõi Sức Khỏe', 'products/paddy_011_cat-ve-sinh-cho-meo-joycat.jpg', 159000, 0,
       'Cát Vệ Sinh Mèo JoyCat Ultimate Health IQ Giúp Theo Dõi Sức Khỏe Thương hiệu: Joycat Phù hợp cho: Mèo mọi lứa tuổi Sản phẩm cát vệ sinh mèo kết hợp cát khoáng tự nhiên và đậu nành theo công thức tối ưu mang lại hiệu quả sử dụng vượt trội và tiết kiệm Lợi íc...

Thương hiệu: Joycat.

Nguồn tham khảo: https://paddy.vn/products/cat-ve-sinh-cho-meo-joycat', 8, 100, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_011_cat-ve-sinh-cho-meo-joycat.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/cat-ve-sinh-cho-meo-joycat_4.jpg?v=1776413233'));

UPDATE products
SET name = 'Hạt Cho Mèo Wanpy Mix Thịt Viên',
    image = 'products/paddy_012_hat-cho-meo-wanpy-mix-thit-vien.jpg',
    price = 70000,
    discount = 0,
    description = 'Hạt Cho Mèo Wanpy Mix Thịt Viên Thương hiệu: Wanpy Phù hợp cho: Mèo mọi lứa tuổi Thức ăn cho mèo Wanpy được bào chế với công thức không chứa ngũ cốc và tỷ lệ đạm động vật lên tới 89%, mang đến bữa ăn tự nhiên và lành mạnh cho mèo. Sự kết hợp độc đáo giữa hạ...

Thương hiệu: Wanpy Premium.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-wanpy-mix-thit-vien',
    stock = 8,
    weight = 100,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_012_hat-cho-meo-wanpy-mix-thit-vien.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-meo-wanpy-mix-thit-vien_4.jpg?v=1776143261');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo Wanpy Mix Thịt Viên', 'products/paddy_012_hat-cho-meo-wanpy-mix-thit-vien.jpg', 70000, 0,
       'Hạt Cho Mèo Wanpy Mix Thịt Viên Thương hiệu: Wanpy Phù hợp cho: Mèo mọi lứa tuổi Thức ăn cho mèo Wanpy được bào chế với công thức không chứa ngũ cốc và tỷ lệ đạm động vật lên tới 89%, mang đến bữa ăn tự nhiên và lành mạnh cho mèo. Sự kết hợp độc đáo giữa hạ...

Thương hiệu: Wanpy Premium.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-wanpy-mix-thit-vien', 8, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_012_hat-cho-meo-wanpy-mix-thit-vien.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-meo-wanpy-mix-thit-vien_4.jpg?v=1776143261'));

UPDATE products
SET name = 'Hạt Cho Chó Smartheart Gold Indoor',
    image = 'products/paddy_013_hat-cho-cho-smartheart-gold-indoor.jpg',
    price = 110000,
    discount = 0,
    description = 'Hạt Cho Chó Smartheart Gold Indoor Thương hiệu: Smartheart Phù hợp cho: Chó mọi lứa tuổi Xu hướng nuôi chó nhỏ tại nhà đang dần trở nên phổ biến. Hạt cho chó SmartHeart Gold Indoor là sản phẩm giúp tăng cường sức khỏe hệ tiêu hóa, cải thiện miễn dịch, nuôi...

Thương hiệu: Smartheart.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-smartheart-gold-indoor',
    stock = 52,
    weight = 100,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_013_hat-cho-cho-smartheart-gold-indoor.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-cho-smartheart-gold-indoor_2.jpg?v=1775731651');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Chó Smartheart Gold Indoor', 'products/paddy_013_hat-cho-cho-smartheart-gold-indoor.jpg', 110000, 0,
       'Hạt Cho Chó Smartheart Gold Indoor Thương hiệu: Smartheart Phù hợp cho: Chó mọi lứa tuổi Xu hướng nuôi chó nhỏ tại nhà đang dần trở nên phổ biến. Hạt cho chó SmartHeart Gold Indoor là sản phẩm giúp tăng cường sức khỏe hệ tiêu hóa, cải thiện miễn dịch, nuôi...

Thương hiệu: Smartheart.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-smartheart-gold-indoor', 52, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_013_hat-cho-cho-smartheart-gold-indoor.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-cho-smartheart-gold-indoor_2.jpg?v=1775731651'));

UPDATE products
SET name = 'Hạt Cho Chó Trưởng Thành Happy Tummy',
    image = 'products/paddy_014_hat-cho-cho-truong-thanh-happy-tummy.jpg',
    price = 61000,
    discount = 0,
    description = 'Hạt Cho Chó Trưởng Thành Happy Tummy Thương hiệu: Happy Tummy Phù hợp cho: Chó trưởng thành Hạt cho chó Happy Tummy là thức ăn hạt chuyên biệt cho chó trưởng thành. Sản phẩm tập trung tối ưu hệ tiêu hóa, giảm mùi hôi chất thải, đồng thời nuôi dưỡng lông bón...

Thương hiệu: Happy Tummy.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-truong-thanh-happy-tummy',
    stock = 53,
    weight = 1000,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_014_hat-cho-cho-truong-thanh-happy-tummy.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-cho-truong-thanh-happy-tummy_2.jpg?v=1775213374');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Chó Trưởng Thành Happy Tummy', 'products/paddy_014_hat-cho-cho-truong-thanh-happy-tummy.jpg', 61000, 0,
       'Hạt Cho Chó Trưởng Thành Happy Tummy Thương hiệu: Happy Tummy Phù hợp cho: Chó trưởng thành Hạt cho chó Happy Tummy là thức ăn hạt chuyên biệt cho chó trưởng thành. Sản phẩm tập trung tối ưu hệ tiêu hóa, giảm mùi hôi chất thải, đồng thời nuôi dưỡng lông bón...

Thương hiệu: Happy Tummy.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-truong-thanh-happy-tummy', 53, 1000, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_014_hat-cho-cho-truong-thanh-happy-tummy.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-cho-truong-thanh-happy-tummy_2.jpg?v=1775213374'));

UPDATE products
SET name = 'Hạt Cho Mèo Trưởng Thành Happy Tummy',
    image = 'products/paddy_015_hat-cho-meo-truong-thanh-happy-tummy.jpg',
    price = 69000,
    discount = 0,
    description = 'Hạt Cho Mèo Trưởng Thành Happy Tummy Thương hiệu: Happy Tummy Phù hợp cho: Mèo trưởng thành Hạt cho mèo Happy Tummy là dòng hạt chuyên biệt cho mèo trưởng thành. Với công thức tối ưu cho hệ tiêu hóa, sản phẩm giúp giảm mùi hôi chất thải, nuôi dưỡng lông bón...

Thương hiệu: Happy Tummy.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-truong-thanh-happy-tummy',
    stock = 8,
    weight = 1000,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_015_hat-cho-meo-truong-thanh-happy-tummy.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-meo-truong-thanh-happy-tummy_2.jpg?v=1775211384');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo Trưởng Thành Happy Tummy', 'products/paddy_015_hat-cho-meo-truong-thanh-happy-tummy.jpg', 69000, 0,
       'Hạt Cho Mèo Trưởng Thành Happy Tummy Thương hiệu: Happy Tummy Phù hợp cho: Mèo trưởng thành Hạt cho mèo Happy Tummy là dòng hạt chuyên biệt cho mèo trưởng thành. Với công thức tối ưu cho hệ tiêu hóa, sản phẩm giúp giảm mùi hôi chất thải, nuôi dưỡng lông bón...

Thương hiệu: Happy Tummy.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-truong-thanh-happy-tummy', 8, 1000, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_015_hat-cho-meo-truong-thanh-happy-tummy.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-meo-truong-thanh-happy-tummy_2.jpg?v=1775211384'));

UPDATE products
SET name = 'Hạt Cho Mèo Excel Vị Cá Thơm Ngon',
    image = 'products/paddy_016_hat-cho-meo-excel-vi-ca-thom-ngon.jpg',
    price = 48000,
    discount = 0,
    description = 'Hạt Cho Mèo Excel Vị Cá Thơm Ngon Thương hiệu: Excel Phù hợp cho: Từ mèo con đến mèo trưởng thành Hạt cho mèo Excel từ Japfa Pet Food Việt Nam là giải pháp dinh dưỡng toàn diện. Với công thức giàu dưỡng chất, sản phẩm giúp mèo cưng phát triển khỏe mạnh, sở...

Thương hiệu: Excel.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-excel-vi-ca-thom-ngon',
    stock = 55,
    weight = 700,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_016_hat-cho-meo-excel-vi-ca-thom-ngon.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-meo-excel-vi-ca-thom-ngon_3.jpg?v=1775192699');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo Excel Vị Cá Thơm Ngon', 'products/paddy_016_hat-cho-meo-excel-vi-ca-thom-ngon.jpg', 48000, 0,
       'Hạt Cho Mèo Excel Vị Cá Thơm Ngon Thương hiệu: Excel Phù hợp cho: Từ mèo con đến mèo trưởng thành Hạt cho mèo Excel từ Japfa Pet Food Việt Nam là giải pháp dinh dưỡng toàn diện. Với công thức giàu dưỡng chất, sản phẩm giúp mèo cưng phát triển khỏe mạnh, sở...

Thương hiệu: Excel.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-excel-vi-ca-thom-ngon', 55, 700, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_016_hat-cho-meo-excel-vi-ca-thom-ngon.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-meo-excel-vi-ca-thom-ngon_3.jpg?v=1775192699'));

UPDATE products
SET name = 'Bánh Thưởng Cho Chó Mèo Mọi Lứa Tuổi Dexinbone',
    image = 'products/paddy_017_banh-thuong-cho-cho-meo-moi-lua-tuoi-dexinbone.jpg',
    price = 55000,
    discount = 0,
    description = 'Bánh Thưởng Cho Chó Mèo Mọi Lứa Tuổi Dexinbone Thương hiệu: INU Fonti Phù hợp cho: Chó/Mèo mọi lứa tuổi Bánh thưởng cho chó mèo Dexinbone là dòng sản phẩm đồ ăn vặt được thiết kế không chỉ để thỏa mãn vị giác mà còn mang lại nhiều lợi ích vượt trội cho sức...

Thương hiệu: INU Fonti.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-cho-meo-moi-lua-tuoi-dexinbone',
    stock = 8,
    weight = 100,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_017_banh-thuong-cho-cho-meo-moi-lua-tuoi-dexinbone.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/banh-thuong-cho-cho-meo-moi-lua-tuoi-dexinbone.jpg?v=1774949702');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Cho Chó Mèo Mọi Lứa Tuổi Dexinbone', 'products/paddy_017_banh-thuong-cho-cho-meo-moi-lua-tuoi-dexinbone.jpg', 55000, 0,
       'Bánh Thưởng Cho Chó Mèo Mọi Lứa Tuổi Dexinbone Thương hiệu: INU Fonti Phù hợp cho: Chó/Mèo mọi lứa tuổi Bánh thưởng cho chó mèo Dexinbone là dòng sản phẩm đồ ăn vặt được thiết kế không chỉ để thỏa mãn vị giác mà còn mang lại nhiều lợi ích vượt trội cho sức...

Thương hiệu: INU Fonti.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-cho-meo-moi-lua-tuoi-dexinbone', 8, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_017_banh-thuong-cho-cho-meo-moi-lua-tuoi-dexinbone.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/banh-thuong-cho-cho-meo-moi-lua-tuoi-dexinbone.jpg?v=1774949702'));

UPDATE products
SET name = 'Pate Cho Chó Mọi Lứa Tuổi LaPaw 375g',
    image = 'products/paddy_018_pate-cho-cho-moi-lua-tuoi-lapaw-375g.webp',
    price = 33000,
    discount = 0,
    description = 'Pate Cho Chó Mọi Lứa Tuổi LaPaw 375g Thương hiệu: LaPaw Phù hợp cho: Chó mọi lứa tuổi Pate Cho Chó LaPaw là một sản phẩm thức ăn cho chó được sản xuất bởi thương hiệu LaPaw của Việt Nam. Pate được làm từ thịt gà, thịt bò và các nguyên liệu dinh dưỡng khác,...

Thương hiệu: LaPaw.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-cho-moi-lua-tuoi-lapaw-375g',
    stock = 8,
    weight = 375,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_018_pate-cho-cho-moi-lua-tuoi-lapaw-375g.webp', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/lapaw-dog-375g-beef-1693897152959.webp?v=1757567290');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Chó Mọi Lứa Tuổi LaPaw 375g', 'products/paddy_018_pate-cho-cho-moi-lua-tuoi-lapaw-375g.webp', 33000, 0,
       'Pate Cho Chó Mọi Lứa Tuổi LaPaw 375g Thương hiệu: LaPaw Phù hợp cho: Chó mọi lứa tuổi Pate Cho Chó LaPaw là một sản phẩm thức ăn cho chó được sản xuất bởi thương hiệu LaPaw của Việt Nam. Pate được làm từ thịt gà, thịt bò và các nguyên liệu dinh dưỡng khác,...

Thương hiệu: LaPaw.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-cho-moi-lua-tuoi-lapaw-375g', 8, 375, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_018_pate-cho-cho-moi-lua-tuoi-lapaw-375g.webp', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/lapaw-dog-375g-beef-1693897152959.webp?v=1757567290'));

UPDATE products
SET name = 'Pate Cho Mèo Mọi Lứa Tuổi On25 80g',
    image = 'products/paddy_019_pate-cho-meo-on25-80g.jpg',
    price = 14000,
    discount = 0,
    description = 'Pate Cho Mèo Mọi Lứa Tuổi On25 80g Thương hiệu: Cat''s On Phù hợp cho: Mèo mọi lứa tuổi Pate mèo ON25 là thức ăn ướt cao cấp với kết cấu mềm mịn, giàu đạm từ cá ngừ và thịt tươi, giúp mèo ăn ngon miệng mỗi ngày. Sản phẩm dễ tiêu hóa, hỗ trợ da lông khỏe mạnh...

Thương hiệu: Cat''s On.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-on25-80g',
    stock = 58,
    weight = 100,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_019_pate-cho-meo-on25-80g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pate-cho-meo-on25_4_fe41dcdd-2cc3-4215-86f6-f7bbb25a9d40.jpg?v=1773914579');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Mèo Mọi Lứa Tuổi On25 80g', 'products/paddy_019_pate-cho-meo-on25-80g.jpg', 14000, 0,
       'Pate Cho Mèo Mọi Lứa Tuổi On25 80g Thương hiệu: Cat''s On Phù hợp cho: Mèo mọi lứa tuổi Pate mèo ON25 là thức ăn ướt cao cấp với kết cấu mềm mịn, giàu đạm từ cá ngừ và thịt tươi, giúp mèo ăn ngon miệng mỗi ngày. Sản phẩm dễ tiêu hóa, hỗ trợ da lông khỏe mạnh...

Thương hiệu: Cat''s On.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-on25-80g', 58, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_019_pate-cho-meo-on25-80g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pate-cho-meo-on25_4_fe41dcdd-2cc3-4215-86f6-f7bbb25a9d40.jpg?v=1773914579'));

UPDATE products
SET name = 'Hạt Cho Chó Mọi Lứa Tuổi Nutrience Subzero Prairier Red',
    image = 'products/paddy_020_hat-cho-cho-moi-lua-tuoi-nutrience-subzero-prairier-red.jpg',
    price = 180000,
    discount = 0,
    description = 'Hạt Cho Chó Mọi Lứa Tuổi Nutrience Subzero Prairier Red Thương hiệu: Nutrience Phù hợp cho: Chó mọi lứa tuổi Hạt cho chó Nutrience SubZero sử dụng nguồn nguyên liệu tự nhiên giàu dinh dưỡng. Công thức giàu đạm kết hợp cùng các dưỡng chất thiết yếu giúp hỗ t...

Thương hiệu: Nutrience.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-moi-lua-tuoi-nutrience-subzero-prairier-red',
    stock = 59,
    weight = 100,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_020_hat-cho-cho-moi-lua-tuoi-nutrience-subzero-prairier-red.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-cho-moi-lua-tuoi-nutrience-subzero_0a6825dc-946c-4d51-b0c3-671265ebd743.jpg?v=1773301635');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Chó Mọi Lứa Tuổi Nutrience Subzero Prairier Red', 'products/paddy_020_hat-cho-cho-moi-lua-tuoi-nutrience-subzero-prairier-red.jpg', 180000, 0,
       'Hạt Cho Chó Mọi Lứa Tuổi Nutrience Subzero Prairier Red Thương hiệu: Nutrience Phù hợp cho: Chó mọi lứa tuổi Hạt cho chó Nutrience SubZero sử dụng nguồn nguyên liệu tự nhiên giàu dinh dưỡng. Công thức giàu đạm kết hợp cùng các dưỡng chất thiết yếu giúp hỗ t...

Thương hiệu: Nutrience.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-moi-lua-tuoi-nutrience-subzero-prairier-red', 59, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_020_hat-cho-cho-moi-lua-tuoi-nutrience-subzero-prairier-red.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-cho-moi-lua-tuoi-nutrience-subzero_0a6825dc-946c-4d51-b0c3-671265ebd743.jpg?v=1773301635'));

UPDATE products
SET name = 'Hạt Cho Chó Mọi Lứa Tuổi ON25 DOG',
    image = 'products/paddy_021_hat-cho-cho-on25-dog.jpg',
    price = 20000,
    discount = 0,
    description = 'Hạt Cho Chó Mọi Lứa Tuổi ON25 DOG Thương hiệu: Cat''s On Phù hợp cho: Chó mọi lứa tuổi Thức ăn hạt cho chó ON25 Dog, với công thức dinh dưỡng cân đối, giàu đạm động vật, hỗ trợ tiêu hóa, giúp chó ăn ngon miệng, khỏe mạnh từ bên trong và bóng đẹp bên ngoài mà...

Thương hiệu: Cat''s On.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-on25-dog',
    stock = 8,
    weight = 600,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_021_hat-cho-cho-on25-dog.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-cho-on25-dog.jpg?v=1773286394');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Chó Mọi Lứa Tuổi ON25 DOG', 'products/paddy_021_hat-cho-cho-on25-dog.jpg', 20000, 0,
       'Hạt Cho Chó Mọi Lứa Tuổi ON25 DOG Thương hiệu: Cat''s On Phù hợp cho: Chó mọi lứa tuổi Thức ăn hạt cho chó ON25 Dog, với công thức dinh dưỡng cân đối, giàu đạm động vật, hỗ trợ tiêu hóa, giúp chó ăn ngon miệng, khỏe mạnh từ bên trong và bóng đẹp bên ngoài mà...

Thương hiệu: Cat''s On.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-on25-dog', 8, 600, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_021_hat-cho-cho-on25-dog.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-cho-on25-dog.jpg?v=1773286394'));

UPDATE products
SET name = 'Sữa Dê Cho Mèo Kit Cat Không Lactose',
    image = 'products/paddy_022_sua-de-cho-meo-kit-cat-khong-lactose.jpg',
    price = 120000,
    discount = 0,
    description = 'Sữa Dê Cho Mèo Kit Cat Không Lactose Thương hiệu: Kit Cat Phù hợp cho: Mèo mọi lứa tuổi Sữa cho mèo Kit Cat là nguồn dinh dưỡng bổ sung phù hợp cho cả mèo con và mèo trưởng thành. Sản phẩm được làm từ sữa dê không chứa lactose nên dễ tiêu hóa, đặc biệt phù...

Thương hiệu: Kit Cat.

Nguồn tham khảo: https://paddy.vn/products/sua-de-cho-meo-kit-cat-khong-lactose',
    stock = 61,
    weight = 100,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_022_sua-de-cho-meo-kit-cat-khong-lactose.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/sua-de-cho-meo-kit-cat-khong-lactose_3.jpg?v=1773118221');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Sữa Dê Cho Mèo Kit Cat Không Lactose', 'products/paddy_022_sua-de-cho-meo-kit-cat-khong-lactose.jpg', 120000, 0,
       'Sữa Dê Cho Mèo Kit Cat Không Lactose Thương hiệu: Kit Cat Phù hợp cho: Mèo mọi lứa tuổi Sữa cho mèo Kit Cat là nguồn dinh dưỡng bổ sung phù hợp cho cả mèo con và mèo trưởng thành. Sản phẩm được làm từ sữa dê không chứa lactose nên dễ tiêu hóa, đặc biệt phù...

Thương hiệu: Kit Cat.

Nguồn tham khảo: https://paddy.vn/products/sua-de-cho-meo-kit-cat-khong-lactose', 61, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_022_sua-de-cho-meo-kit-cat-khong-lactose.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/sua-de-cho-meo-kit-cat-khong-lactose_3.jpg?v=1773118221'));

UPDATE products
SET name = 'Bánh Thưởng Cho Chó Natural Core Sụn Gà 55g',
    image = 'products/paddy_023_banh-thuong-cho-cho-natural-core-sun-ga-55g.png',
    price = 58000,
    discount = 0,
    description = 'Bánh Thưởng Cho Chó Natural Core Sụn Gà 55g Thương hiệu: Natural Core Phù hợp cho: Chó mọi lứa tuổi (từ 2 tháng tuổi trở lên) Bánh thưởng cho chó Natural Core Fresh & Tasty Sụn Gà 55g chính là lựa chọn hoàn hảo đến từ Hàn Quốc, được chế biến từ sụn gà tươi...

Thương hiệu: Natural Core.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-cho-natural-core-sun-ga-55g',
    stock = 62,
    weight = 100,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_023_banh-thuong-cho-cho-natural-core-sun-ga-55g.png', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/banh-thuong-cho-cho-natural-core-sun-ga-55g.png?v=1772793785');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Cho Chó Natural Core Sụn Gà 55g', 'products/paddy_023_banh-thuong-cho-cho-natural-core-sun-ga-55g.png', 58000, 0,
       'Bánh Thưởng Cho Chó Natural Core Sụn Gà 55g Thương hiệu: Natural Core Phù hợp cho: Chó mọi lứa tuổi (từ 2 tháng tuổi trở lên) Bánh thưởng cho chó Natural Core Fresh & Tasty Sụn Gà 55g chính là lựa chọn hoàn hảo đến từ Hàn Quốc, được chế biến từ sụn gà tươi...

Thương hiệu: Natural Core.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-cho-natural-core-sun-ga-55g', 62, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_023_banh-thuong-cho-cho-natural-core-sun-ga-55g.png', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/banh-thuong-cho-cho-natural-core-sun-ga-55g.png?v=1772793785'));

UPDATE products
SET name = 'Dây Xích Cho Chó Mon Ami 3x120cm',
    image = 'products/paddy_024_day-xich-cho-cho-mon-ami-3x120cm.png',
    price = 65000,
    discount = 0,
    description = 'Dây Xích Cho Chó Mon Ami 3x120cm Thương hiệu: Mon Ami Phù hợp cho: Chó mọi lứa tuổi Dây Xích Mon Ami 3x120cm là một phụ kiện thú cưng thiết yếu, đáp ứng tối đa nhu cầu vận động và an toàn cho thú cưng của bạn. Với thiết kế chắc chắn và tiện lợi, sản phẩm nà...

Thương hiệu: Mon Ami.

Nguồn tham khảo: https://paddy.vn/products/day-xich-cho-cho-mon-ami-3x120cm',
    stock = 63,
    weight = 400,
    category = 'Phụ Kiện Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_024_day-xich-cho-cho-mon-ami-3x120cm.png', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/day-xich-cho-cho-mon-ami-3x120cm-1740376868767.png?v=1747191512');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Dây Xích Cho Chó Mon Ami 3x120cm', 'products/paddy_024_day-xich-cho-cho-mon-ami-3x120cm.png', 65000, 0,
       'Dây Xích Cho Chó Mon Ami 3x120cm Thương hiệu: Mon Ami Phù hợp cho: Chó mọi lứa tuổi Dây Xích Mon Ami 3x120cm là một phụ kiện thú cưng thiết yếu, đáp ứng tối đa nhu cầu vận động và an toàn cho thú cưng của bạn. Với thiết kế chắc chắn và tiện lợi, sản phẩm nà...

Thương hiệu: Mon Ami.

Nguồn tham khảo: https://paddy.vn/products/day-xich-cho-cho-mon-ami-3x120cm', 63, 400, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_024_day-xich-cho-cho-mon-ami-3x120cm.png', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/day-xich-cho-cho-mon-ami-3x120cm-1740376868767.png?v=1747191512'));

UPDATE products
SET name = 'Viên Dầu Cá Hồi Omega Cho Chó Mèo (Hộp 60 viên)',
    image = 'products/paddy_025_vien-dau-ca-hoi-omega-cho-meo-hop-60-vien.jpg',
    price = 80000,
    discount = 20,
    description = 'Viên Dầu Cá Hồi Omega Cho Chó Mèo (Hộp 60 viên) Thương hiệu: Kamt/ Q8 Phù hợp cho: Chó/Mèo mọi lứa tuổi Thực phẩm chức năng viên dầu cá bổ sung OMEGA-3 cho chó và mèo tăng đề kháng và cung cấp dinh dưỡng cần thiết cho tim mạch, da, lông. Lợi ích Giảm hẳn rụ...

Thương hiệu: Q8.

Nguồn tham khảo: https://paddy.vn/products/vien-dau-ca-hoi-omega-cho-meo-hop-60-vien',
    stock = 64,
    weight = 200,
    category = 'Dinh Dưỡng',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_025_vien-dau-ca-hoi-omega-cho-meo-hop-60-vien.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/vien-dau-ca-hoi-omega-cho-meo-hop-60-vien_4_b51aa5da-b637-4dd9-b3f0-bb5885ec2adf.jpg?v=1768197879');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Viên Dầu Cá Hồi Omega Cho Chó Mèo (Hộp 60 viên)', 'products/paddy_025_vien-dau-ca-hoi-omega-cho-meo-hop-60-vien.jpg', 80000, 20,
       'Viên Dầu Cá Hồi Omega Cho Chó Mèo (Hộp 60 viên) Thương hiệu: Kamt/ Q8 Phù hợp cho: Chó/Mèo mọi lứa tuổi Thực phẩm chức năng viên dầu cá bổ sung OMEGA-3 cho chó và mèo tăng đề kháng và cung cấp dinh dưỡng cần thiết cho tim mạch, da, lông. Lợi ích Giảm hẳn rụ...

Thương hiệu: Q8.

Nguồn tham khảo: https://paddy.vn/products/vien-dau-ca-hoi-omega-cho-meo-hop-60-vien', 64, 200, 'Dinh Dưỡng',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_025_vien-dau-ca-hoi-omega-cho-meo-hop-60-vien.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/vien-dau-ca-hoi-omega-cho-meo-hop-60-vien_4_b51aa5da-b637-4dd9-b3f0-bb5885ec2adf.jpg?v=1768197879'));

UPDATE products
SET name = 'Bánh Thưởng Cho Chó INU Fonti Xương Gặm Bàn Chải 90g',
    image = 'products/paddy_026_banh-thuong-cho-cho-inu-fonti-xuong-gam-ban-chai-90g.jpg',
    price = 20000,
    discount = 0,
    description = 'Bánh Thưởng Cho Chó INU Fonti Xương Gặm Bàn Chải 90g Thương hiệu: INU Fonti Phù hợp cho: Chó mọi lứa tuổi (từ 2 tháng tuổi trở lên) Bánh thưởng xương gặm cho chó giúp làm sạch răng, bổ sung canxi và hỗ trợ giảm căng thẳng, nhàm chán nhờ thỏa mãn hành vi gặm...

Thương hiệu: INU Fonti.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-cho-inu-fonti-xuong-gam-ban-chai-90g',
    stock = 65,
    weight = 150,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_026_banh-thuong-cho-cho-inu-fonti-xuong-gam-ban-chai-90g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/banh-thuong-cho-cho-inu-fonti-xuong-gam-ban-chai_2.jpg?v=1772525387');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Cho Chó INU Fonti Xương Gặm Bàn Chải 90g', 'products/paddy_026_banh-thuong-cho-cho-inu-fonti-xuong-gam-ban-chai-90g.jpg', 20000, 0,
       'Bánh Thưởng Cho Chó INU Fonti Xương Gặm Bàn Chải 90g Thương hiệu: INU Fonti Phù hợp cho: Chó mọi lứa tuổi (từ 2 tháng tuổi trở lên) Bánh thưởng xương gặm cho chó giúp làm sạch răng, bổ sung canxi và hỗ trợ giảm căng thẳng, nhàm chán nhờ thỏa mãn hành vi gặm...

Thương hiệu: INU Fonti.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-cho-inu-fonti-xuong-gam-ban-chai-90g', 65, 150, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_026_banh-thuong-cho-cho-inu-fonti-xuong-gam-ban-chai-90g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/banh-thuong-cho-cho-inu-fonti-xuong-gam-ban-chai_2.jpg?v=1772525387'));

UPDATE products
SET name = 'Pate TƯƠI The Pet Cho Chó Mèo Biếng Ăn (1kg) - Ship Now/Grab 2H',
    image = 'products/paddy_027_pate-meo-the-pet-cho-meo-1kg.jpg',
    price = 105000,
    discount = 13,
    description = '*Pate tươi được nhập hàng vào lúc 14h-15h hằng ngày, chủ nhật không nhập hàng (một số phân loại hết hàng chỉ có thể giao sau 15h) Pate Tươi Cho Mèo Hỗn Hợp cho Chó Mèo Biếng Ăn được làm từ hỗn hợp cá biển và gan gà tươi nguyên chất thích hợp dùng cho Chó Mè...

Thương hiệu: The Pet.

Nguồn tham khảo: https://paddy.vn/products/pate-meo-the-pet-cho-meo-1kg',
    stock = 66,
    weight = 1000,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_027_pate-meo-the-pet-cho-meo-1kg.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/products/pate-tuoi-the-pet-cho-cho-meo-bieng-an-1kg-ship-nowgrab-2h-paddy-5.jpg?v=1760327236');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate TƯƠI The Pet Cho Chó Mèo Biếng Ăn (1kg) - Ship Now/Grab 2H', 'products/paddy_027_pate-meo-the-pet-cho-meo-1kg.jpg', 105000, 13,
       '*Pate tươi được nhập hàng vào lúc 14h-15h hằng ngày, chủ nhật không nhập hàng (một số phân loại hết hàng chỉ có thể giao sau 15h) Pate Tươi Cho Mèo Hỗn Hợp cho Chó Mèo Biếng Ăn được làm từ hỗn hợp cá biển và gan gà tươi nguyên chất thích hợp dùng cho Chó Mè...

Thương hiệu: The Pet.

Nguồn tham khảo: https://paddy.vn/products/pate-meo-the-pet-cho-meo-1kg', 66, 1000, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_027_pate-meo-the-pet-cho-meo-1kg.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/products/pate-tuoi-the-pet-cho-cho-meo-bieng-an-1kg-ship-nowgrab-2h-paddy-5.jpg?v=1760327236'));

UPDATE products
SET name = 'Bát Ăn Cho Chó Mèo Inox BOWL Mon Ami',
    image = 'products/paddy_028_bat-an-cho-cho-meo-inox-bowl-mon-ami.png',
    price = 35000,
    discount = 0,
    description = 'Bát Ăn Cho Chó Mèo Inox BOWL Mon Ami Thương hiệu: Mon Ami Phù hợp cho: Chó/Mèo mọi lứa tuổi Sản phẩm dụng cụ ăn uống không thể thiếu cho các boss bát ăn inox MON AMI Bowl được làm từ chất liệu thép không gỉ cao cấp và thiết kế giúp chó mèo dễ dàng ăn uống c...

Thương hiệu: Mon Ami.

Nguồn tham khảo: https://paddy.vn/products/bat-an-cho-cho-meo-inox-bowl-mon-ami',
    stock = 67,
    weight = 100,
    category = 'Phụ Kiện Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_028_bat-an-cho-cho-meo-inox-bowl-mon-ami.png', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/bat-an-cho-cho-meo-inox-bowl-mon-ami_3.png?v=1770714142');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bát Ăn Cho Chó Mèo Inox BOWL Mon Ami', 'products/paddy_028_bat-an-cho-cho-meo-inox-bowl-mon-ami.png', 35000, 0,
       'Bát Ăn Cho Chó Mèo Inox BOWL Mon Ami Thương hiệu: Mon Ami Phù hợp cho: Chó/Mèo mọi lứa tuổi Sản phẩm dụng cụ ăn uống không thể thiếu cho các boss bát ăn inox MON AMI Bowl được làm từ chất liệu thép không gỉ cao cấp và thiết kế giúp chó mèo dễ dàng ăn uống c...

Thương hiệu: Mon Ami.

Nguồn tham khảo: https://paddy.vn/products/bat-an-cho-cho-meo-inox-bowl-mon-ami', 67, 100, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_028_bat-an-cho-cho-meo-inox-bowl-mon-ami.png', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/bat-an-cho-cho-meo-inox-bowl-mon-ami_3.png?v=1770714142'));

UPDATE products
SET name = 'Balo Vận Chuyển Cho Chó Mèo Da Cao Cấp Diamond',
    image = 'products/paddy_029_balo-van-chuyen-cho-cho-meo-da-cao-cap-diamond.jpg',
    price = 300000,
    discount = 0,
    description = 'Balo Vận Chuyển Cho Chó Mèo Da Cao Cấp Diamond Thương hiệu: Diamond Phù hợp cho: Mèo mọi lứa tuổi (tối đa 5kg) Balo vận chuyển cho mèo nhiều lỗ giúp thú cưng có 1 không gian thoải mái ngoài ra còn hỗ trợ lưu thông không khí tốt, giúp thú cưng luôn cảm thấy...

Thương hiệu: Diamond.

Nguồn tham khảo: https://paddy.vn/products/balo-van-chuyen-cho-cho-meo-da-cao-cap-diamond',
    stock = 68,
    weight = 500,
    category = 'Phụ Kiện Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_029_balo-van-chuyen-cho-cho-meo-da-cao-cap-diamond.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/balo-van-chuyen-cho-cho-meo-da-cao-cap-diamond_3.jpg?v=1770706448');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Balo Vận Chuyển Cho Chó Mèo Da Cao Cấp Diamond', 'products/paddy_029_balo-van-chuyen-cho-cho-meo-da-cao-cap-diamond.jpg', 300000, 0,
       'Balo Vận Chuyển Cho Chó Mèo Da Cao Cấp Diamond Thương hiệu: Diamond Phù hợp cho: Mèo mọi lứa tuổi (tối đa 5kg) Balo vận chuyển cho mèo nhiều lỗ giúp thú cưng có 1 không gian thoải mái ngoài ra còn hỗ trợ lưu thông không khí tốt, giúp thú cưng luôn cảm thấy...

Thương hiệu: Diamond.

Nguồn tham khảo: https://paddy.vn/products/balo-van-chuyen-cho-cho-meo-da-cao-cap-diamond', 68, 500, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_029_balo-van-chuyen-cho-cho-meo-da-cao-cap-diamond.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/balo-van-chuyen-cho-cho-meo-da-cao-cap-diamond_3.jpg?v=1770706448'));

UPDATE products
SET name = 'Bánh Thưởng Thịt Sấy Cho Chó Mèo Catsdo',
    image = 'products/paddy_030_banh-thuong-thit-say-cho-meo-catsdo.jpg',
    price = 50000,
    discount = 0,
    description = 'Bánh Thưởng Thịt Sấy Cho Chó Mèo Catsdo Thương hiệu: Catsdo Phù hợp cho: Chó/Mèo mọi lứa tuổi Thịt sấy cho mèo là một loại snack được thiết kế dành cho thú cưng. Snack thường có kích thước nhỏ, nhiều hương vị khác nhau phù hợp với khẩu vị khác nhau của các...

Thương hiệu: Catsdo.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-thit-say-cho-meo-catsdo',
    stock = 69,
    weight = 100,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_030_banh-thuong-thit-say-cho-meo-catsdo.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/banh-thuong-thit-say-cho-meo-catsdo.jpg?v=1770280337');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Thịt Sấy Cho Chó Mèo Catsdo', 'products/paddy_030_banh-thuong-thit-say-cho-meo-catsdo.jpg', 50000, 0,
       'Bánh Thưởng Thịt Sấy Cho Chó Mèo Catsdo Thương hiệu: Catsdo Phù hợp cho: Chó/Mèo mọi lứa tuổi Thịt sấy cho mèo là một loại snack được thiết kế dành cho thú cưng. Snack thường có kích thước nhỏ, nhiều hương vị khác nhau phù hợp với khẩu vị khác nhau của các...

Thương hiệu: Catsdo.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-thit-say-cho-meo-catsdo', 69, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_030_banh-thuong-thit-say-cho-meo-catsdo.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/banh-thuong-thit-say-cho-meo-catsdo.jpg?v=1770280337'));

UPDATE products
SET name = 'Nệm Cho Chó Mèo Pupdy Floating Mattress',
    image = 'products/paddy_031_nem-cho-cho-meo-pupdy-floating-mattress.jpg',
    price = 250000,
    discount = 0,
    description = 'Nệm Cho Chó Mèo Pupdy Floating Mattress Thương hiệu: Pupdy Phù hợp cho: Chó/Mèo mọi lứa tuổi Nệm cho chó mèo Pupdy Floating Mattress được thiết kế với cảm giác “nằm như bay trên mây”, mang đến cho thú cưng một không gian nghỉ ngơi êm ái, thư giãn và an toàn...

Thương hiệu: Pupdy.

Nguồn tham khảo: https://paddy.vn/products/nem-cho-cho-meo-pupdy-floating-mattress',
    stock = 8,
    weight = 3500,
    category = 'Phụ Kiện Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_031_nem-cho-cho-meo-pupdy-floating-mattress.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/nem-cho-cho-meo-pupdy_3.jpg?v=1770109566');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Nệm Cho Chó Mèo Pupdy Floating Mattress', 'products/paddy_031_nem-cho-cho-meo-pupdy-floating-mattress.jpg', 250000, 0,
       'Nệm Cho Chó Mèo Pupdy Floating Mattress Thương hiệu: Pupdy Phù hợp cho: Chó/Mèo mọi lứa tuổi Nệm cho chó mèo Pupdy Floating Mattress được thiết kế với cảm giác “nằm như bay trên mây”, mang đến cho thú cưng một không gian nghỉ ngơi êm ái, thư giãn và an toàn...

Thương hiệu: Pupdy.

Nguồn tham khảo: https://paddy.vn/products/nem-cho-cho-meo-pupdy-floating-mattress', 8, 3500, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_031_nem-cho-cho-meo-pupdy-floating-mattress.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/nem-cho-cho-meo-pupdy_3.jpg?v=1770109566'));

UPDATE products
SET name = 'Bánh Thưởng Que Gặm Sạch Răng Cho Chó Jireho',
    image = 'products/paddy_032_banh-thuong-cho-cho-jireho.jpg',
    price = 45000,
    discount = 0,
    description = 'Bánh Thưởng Que Gặm Sạch Răng Cho Chó Jireho Thương hiệu: Jireho Phù hợp cho: Chó mọi lứa tuổi Bánh thưởng cho chó được làm từ thịt gà thật kết hợp thịt heo, sữa, phô mai, bơ đậu phộng và rau củ tự nhiên, mang đến hương vị thơm ngon cùng nguồn protein chất...

Thương hiệu: JirehO.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-cho-jireho',
    stock = 8,
    weight = 100,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_032_banh-thuong-cho-cho-jireho.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/banh-thuong-cho-cho-jireho_7.jpg?v=1770096150');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Que Gặm Sạch Răng Cho Chó Jireho', 'products/paddy_032_banh-thuong-cho-cho-jireho.jpg', 45000, 0,
       'Bánh Thưởng Que Gặm Sạch Răng Cho Chó Jireho Thương hiệu: Jireho Phù hợp cho: Chó mọi lứa tuổi Bánh thưởng cho chó được làm từ thịt gà thật kết hợp thịt heo, sữa, phô mai, bơ đậu phộng và rau củ tự nhiên, mang đến hương vị thơm ngon cùng nguồn protein chất...

Thương hiệu: JirehO.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-cho-jireho', 8, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_032_banh-thuong-cho-cho-jireho.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/banh-thuong-cho-cho-jireho_7.jpg?v=1770096150'));

UPDATE products
SET name = 'Pate Cho Mèo Alpha Pet 70G',
    image = 'products/paddy_033_pate-cho-meo-alpha-pet-70g.jpg',
    price = 9000,
    discount = 0,
    description = 'Pate Cho Mèo Alpha Pet 70G Thương hiệu: Alpha Pet Phù hợp cho: Mèo (mọi lứa tuổi) Pate mèo Peptide Alpha Pet là sản phẩm pate cho mèo thế hệ mới, lần đầu tiên mang Peptide tôm - loại đạm thủy phân siêu nhỏ vào khẩu phần ăn của mèo. Khác với protein thô khó...

Thương hiệu: Alpha Pet.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-alpha-pet-70g',
    stock = 72,
    weight = 100,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_033_pate-cho-meo-alpha-pet-70g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pate-cho-meo-alpha-pet-70g_7.jpg?v=1769673387');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Mèo Alpha Pet 70G', 'products/paddy_033_pate-cho-meo-alpha-pet-70g.jpg', 9000, 0,
       'Pate Cho Mèo Alpha Pet 70G Thương hiệu: Alpha Pet Phù hợp cho: Mèo (mọi lứa tuổi) Pate mèo Peptide Alpha Pet là sản phẩm pate cho mèo thế hệ mới, lần đầu tiên mang Peptide tôm - loại đạm thủy phân siêu nhỏ vào khẩu phần ăn của mèo. Khác với protein thô khó...

Thương hiệu: Alpha Pet.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-alpha-pet-70g', 72, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_033_pate-cho-meo-alpha-pet-70g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pate-cho-meo-alpha-pet-70g_7.jpg?v=1769673387'));

UPDATE products
SET name = 'Pate Cho Mèo Real & Raw Catidea Lon 170g',
    image = 'products/paddy_034_pate-cho-meo-real-raw-catidea-170g.jpg',
    price = 42000,
    discount = 0,
    description = 'Pate Cho Mèo Real & Raw Catidea 170g Thương hiệu: Catidea Phù hợp cho: Mèo mọi lứa tuổi Pate mèo Catidea Real & Raw là dòng thức ăn cao cấp dành cho mèo, được chế biến từ các loại thịt tươi sống chất lượng cao như đà điểu, nai, thỏ, cá mập kết hợp cùng thịt...

Thương hiệu: Catidea.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-real-raw-catidea-170g',
    stock = 73,
    weight = 200,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_034_pate-cho-meo-real-raw-catidea-170g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pate-cho-meo-real-raw-catidea_4.jpg?v=1772532665');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Mèo Real & Raw Catidea Lon 170g', 'products/paddy_034_pate-cho-meo-real-raw-catidea-170g.jpg', 42000, 0,
       'Pate Cho Mèo Real & Raw Catidea 170g Thương hiệu: Catidea Phù hợp cho: Mèo mọi lứa tuổi Pate mèo Catidea Real & Raw là dòng thức ăn cao cấp dành cho mèo, được chế biến từ các loại thịt tươi sống chất lượng cao như đà điểu, nai, thỏ, cá mập kết hợp cùng thịt...

Thương hiệu: Catidea.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-real-raw-catidea-170g', 73, 200, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_034_pate-cho-meo-real-raw-catidea-170g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pate-cho-meo-real-raw-catidea_4.jpg?v=1772532665'));

UPDATE products
SET name = 'Cào Móng Giấy Cho Mèo Mọi Lứa Tuổi FOFOS',
    image = 'products/paddy_035_cao-mong-giay-cho-meo-moi-lua-tuoi-fofos.jpg',
    price = 285000,
    discount = 0,
    description = 'Cào Móng Giấy Cho Mèo Mọi Lứa Tuổi FOFOS Thương hiệu: Fofos Phù hợp cho: Mèo mọi lứa tuổi Bàn cào móng cho mèo là món đồ chơi kết hợp giữa giải trí và nghỉ ngơi cho mèo cưng. Thiết kế sáng tạo, ngộ nghĩnh không chỉ giúp mèo có không gian riêng thoải mái mà...

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/cao-mong-giay-cho-meo-mọi-lua-tuoi-fofos',
    stock = 74,
    weight = 600,
    category = 'Đồ Chơi Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_035_cao-mong-giay-cho-meo-moi-lua-tuoi-fofos.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/cao-mong-giay-cho-meo-m_i-lua-tuoi-fofos_4.jpg?v=1769486287');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Cào Móng Giấy Cho Mèo Mọi Lứa Tuổi FOFOS', 'products/paddy_035_cao-mong-giay-cho-meo-moi-lua-tuoi-fofos.jpg', 285000, 0,
       'Cào Móng Giấy Cho Mèo Mọi Lứa Tuổi FOFOS Thương hiệu: Fofos Phù hợp cho: Mèo mọi lứa tuổi Bàn cào móng cho mèo là món đồ chơi kết hợp giữa giải trí và nghỉ ngơi cho mèo cưng. Thiết kế sáng tạo, ngộ nghĩnh không chỉ giúp mèo có không gian riêng thoải mái mà...

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/cao-mong-giay-cho-meo-mọi-lua-tuoi-fofos', 74, 600, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_035_cao-mong-giay-cho-meo-moi-lua-tuoi-fofos.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/cao-mong-giay-cho-meo-m_i-lua-tuoi-fofos_4.jpg?v=1769486287'));

UPDATE products
SET name = 'Đồ Chơi Dây Thừng Cho Chó FOFOS',
    image = 'products/paddy_036_do-choi-day-thung-cho-cho-fofos.jpg',
    price = 98000,
    discount = 0,
    description = 'Đồ Chơi Dây Thừng Cho Chó FOFOS Thương hiệu: Fofos Phù hợp cho: Chó mọi lứa tuổi Đồ chơi cho chó dây thừng là sản phẩm hỗ trợ chó giải trí và vận động mỗi ngày, đặc biệt phù hợp với các bé có thói quen cắn gặm. Sản phẩm giúp chó giảm buồn chán, hạn chế hành...

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/do-choi-day-thung-cho-cho-fofos',
    stock = 75,
    weight = 300,
    category = 'Đồ Chơi Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_036_do-choi-day-thung-cho-cho-fofos.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/do-choi-day-thung-cho-cho-fofos_2.jpg?v=1769069878');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Đồ Chơi Dây Thừng Cho Chó FOFOS', 'products/paddy_036_do-choi-day-thung-cho-cho-fofos.jpg', 98000, 0,
       'Đồ Chơi Dây Thừng Cho Chó FOFOS Thương hiệu: Fofos Phù hợp cho: Chó mọi lứa tuổi Đồ chơi cho chó dây thừng là sản phẩm hỗ trợ chó giải trí và vận động mỗi ngày, đặc biệt phù hợp với các bé có thói quen cắn gặm. Sản phẩm giúp chó giảm buồn chán, hạn chế hành...

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/do-choi-day-thung-cho-cho-fofos', 75, 300, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_036_do-choi-day-thung-cho-cho-fofos.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/do-choi-day-thung-cho-cho-fofos_2.jpg?v=1769069878'));

UPDATE products
SET name = 'Đồ Chơi Cho Mèo FOFOS Set 6 Món',
    image = 'products/paddy_037_do-choi-meo-fofos-set-6-mon.jpg',
    price = 110000,
    discount = 0,
    description = 'Đồ Chơi Cho Mèo FOFOS Set 6 Món Thương hiệu: Fofos Phù hợp cho: Mèo mọi lứa tuổi Đồ chơi cho mèo FOFOS là bộ đồ chơi dành cho mèo được thiết kế nhằm mang đến những giờ phút vui chơi thú vị và bổ ích mỗi ngày. Sản phẩm giúp mèo vận động nhiều hơn, giải tỏa c...

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/do-choi-meo-fofos-set-6-mon',
    stock = 76,
    weight = 300,
    category = 'Đồ Chơi Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_037_do-choi-meo-fofos-set-6-mon.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/do-choi-meo-fofos_3.jpg?v=1769065371');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Đồ Chơi Cho Mèo FOFOS Set 6 Món', 'products/paddy_037_do-choi-meo-fofos-set-6-mon.jpg', 110000, 0,
       'Đồ Chơi Cho Mèo FOFOS Set 6 Món Thương hiệu: Fofos Phù hợp cho: Mèo mọi lứa tuổi Đồ chơi cho mèo FOFOS là bộ đồ chơi dành cho mèo được thiết kế nhằm mang đến những giờ phút vui chơi thú vị và bổ ích mỗi ngày. Sản phẩm giúp mèo vận động nhiều hơn, giải tỏa c...

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/do-choi-meo-fofos-set-6-mon', 76, 300, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_037_do-choi-meo-fofos-set-6-mon.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/do-choi-meo-fofos_3.jpg?v=1769065371'));

UPDATE products
SET name = 'Pate Cho Mèo Whiskas 80g [Hộp 6 gói]',
    image = 'products/paddy_038_pate-cho-meo-whiskas-80g-hop-6-goi.jpg',
    price = 65000,
    discount = 0,
    description = 'Pate Cho Mèo Whiskas 80g [Hộp 6 gói] Thương hiệu: Whiskas Phù hợp cho: Mèo từ 2 đến 12 tháng tuổi vào mèo từ 1 tuổi trở lên Pate mèo whiskas được chế biến đặc biệt để đáp ứng nhu cầu dinh dưỡng của mèo ở mọi giai đoạn phát triển. Khi mèo con bước sang tháng...

Thương hiệu: Whiskas.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-whiskas-80g-hop-6-goi',
    stock = 77,
    weight = 600,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_038_pate-cho-meo-whiskas-80g-hop-6-goi.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pate-cho-meo-whiskas-80g_2.jpg?v=1768461913');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Mèo Whiskas 80g [Hộp 6 gói]', 'products/paddy_038_pate-cho-meo-whiskas-80g-hop-6-goi.jpg', 65000, 0,
       'Pate Cho Mèo Whiskas 80g [Hộp 6 gói] Thương hiệu: Whiskas Phù hợp cho: Mèo từ 2 đến 12 tháng tuổi vào mèo từ 1 tuổi trở lên Pate mèo whiskas được chế biến đặc biệt để đáp ứng nhu cầu dinh dưỡng của mèo ở mọi giai đoạn phát triển. Khi mèo con bước sang tháng...

Thương hiệu: Whiskas.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-whiskas-80g-hop-6-goi', 77, 600, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_038_pate-cho-meo-whiskas-80g-hop-6-goi.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pate-cho-meo-whiskas-80g_2.jpg?v=1768461913'));

UPDATE products
SET name = 'Xịt Khử Mùi Cát Vệ Sinh Cho Mèo Arm & Hammer 636ml',
    image = 'products/paddy_039_xit-khu-mui-cat-ve-sinh-cho-meo-arm-hammer-636ml.jpg',
    price = 280000,
    discount = 0,
    description = 'Xịt Khử Mùi Cát Vệ Sinh Cho Mèo Arm & Hammer 636ml Thương hiệu: Arm & Hammer Phù hợp cho: Mèo mọi lứa tuổi/Mọi loại cát vệ sinh Nước xịt khử mùi cho mèo ARM & HAMMER giúp khử mùi tức thì và kéo dài tuổi thọ của cát vệ sinh. Công thức mạnh mẽ dành cho nhiều...

Thương hiệu: Arm & Hammer.

Nguồn tham khảo: https://paddy.vn/products/xit-khu-mui-cat-ve-sinh-cho-meo-arm-hammer-636ml',
    stock = 8,
    weight = 800,
    category = 'Vệ Sinh Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_039_xit-khu-mui-cat-ve-sinh-cho-meo-arm-hammer-636ml.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/xit-khu-mui-cat-ve-sinh.jpg?v=1768274926');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Xịt Khử Mùi Cát Vệ Sinh Cho Mèo Arm & Hammer 636ml', 'products/paddy_039_xit-khu-mui-cat-ve-sinh-cho-meo-arm-hammer-636ml.jpg', 280000, 0,
       'Xịt Khử Mùi Cát Vệ Sinh Cho Mèo Arm & Hammer 636ml Thương hiệu: Arm & Hammer Phù hợp cho: Mèo mọi lứa tuổi/Mọi loại cát vệ sinh Nước xịt khử mùi cho mèo ARM & HAMMER giúp khử mùi tức thì và kéo dài tuổi thọ của cát vệ sinh. Công thức mạnh mẽ dành cho nhiều...

Thương hiệu: Arm & Hammer.

Nguồn tham khảo: https://paddy.vn/products/xit-khu-mui-cat-ve-sinh-cho-meo-arm-hammer-636ml', 8, 800, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_039_xit-khu-mui-cat-ve-sinh-cho-meo-arm-hammer-636ml.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/xit-khu-mui-cat-ve-sinh.jpg?v=1768274926'));

UPDATE products
SET name = 'Hạt Cho Mèo Mọi Lứa Tuổi Today''s Dinner',
    image = 'products/paddy_040_hat-cho-meo-moi-lua-tuoi-todays-dinner.jpg',
    price = 70000,
    discount = 0,
    description = 'Hạt Cho Mèo Mọi Lứa Tuổi Today''s Dinner Thương hiệu: Today Dinner Phù hợp cho: Mèo mọi lứa tuổi Hạt cho mèo Today''s Dinner là dòng thức ăn hoàn chỉnh cho mèo từ 3 tháng tuổi trở lên với công thức giàu protein động vật, vitamin, khoáng chất và taurine giúp m...

Thương hiệu: Today Dinner.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-moi-lua-tuoi-todays-dinner',
    stock = 79,
    weight = 1000,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_040_hat-cho-meo-moi-lua-tuoi-todays-dinner.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-meo-moi-lua-tuoi-todays-dinner_3.jpg?v=1767844933');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo Mọi Lứa Tuổi Today''s Dinner', 'products/paddy_040_hat-cho-meo-moi-lua-tuoi-todays-dinner.jpg', 70000, 0,
       'Hạt Cho Mèo Mọi Lứa Tuổi Today''s Dinner Thương hiệu: Today Dinner Phù hợp cho: Mèo mọi lứa tuổi Hạt cho mèo Today''s Dinner là dòng thức ăn hoàn chỉnh cho mèo từ 3 tháng tuổi trở lên với công thức giàu protein động vật, vitamin, khoáng chất và taurine giúp m...

Thương hiệu: Today Dinner.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-moi-lua-tuoi-todays-dinner', 79, 1000, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_040_hat-cho-meo-moi-lua-tuoi-todays-dinner.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-meo-moi-lua-tuoi-todays-dinner_3.jpg?v=1767844933'));

UPDATE products
SET name = 'Võng Gỗ Cho Mèo CATCA',
    image = 'products/paddy_041_vong-go-cho-meo-catca.jpg',
    price = 260000,
    discount = 0,
    description = 'Võng Gỗ Cho Mèo CATCA Thương hiệu Catca Phù hợp cho: Mèo dưới 10kg Võng Cho Mèo CATCA là giải pháp nghỉ ngơi thoải mái cho thú cưng với thiết kế nằm thoáng mát và êm ái. Sản phẩm lắp ráp dễ dàng không cần ốc vít, chắc chắn và an toàn, phù hợp cho mèo và thú...

Thương hiệu: Catca.

Nguồn tham khảo: https://paddy.vn/products/vong-go-cho-meo-catca',
    stock = 80,
    weight = 100,
    category = 'Đồ Chơi Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_041_vong-go-cho-meo-catca.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/vong-go-cho-meo-catca_4.jpg?v=1767692043');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Võng Gỗ Cho Mèo CATCA', 'products/paddy_041_vong-go-cho-meo-catca.jpg', 260000, 0,
       'Võng Gỗ Cho Mèo CATCA Thương hiệu Catca Phù hợp cho: Mèo dưới 10kg Võng Cho Mèo CATCA là giải pháp nghỉ ngơi thoải mái cho thú cưng với thiết kế nằm thoáng mát và êm ái. Sản phẩm lắp ráp dễ dàng không cần ốc vít, chắc chắn và an toàn, phù hợp cho mèo và thú...

Thương hiệu: Catca.

Nguồn tham khảo: https://paddy.vn/products/vong-go-cho-meo-catca', 80, 100, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_041_vong-go-cho-meo-catca.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/vong-go-cho-meo-catca_4.jpg?v=1767692043'));

UPDATE products
SET name = 'Pate Cho Chó Pedigree 80g',
    image = 'products/paddy_042_pate-cho-cho-pedigree-80g.jpg',
    price = 13000,
    discount = 0,
    description = 'Pate Cho Chó Pedigree 80g Thương hiệu: Pedigree Phù hợp cho: Chó mọi lứa tuổi (phân loại trong sản phẩm) Pate chó Pedigree là dòng thức ăn ướt cho chó được phát triển bởi các chuyên gia dinh dưỡng WALTHAM (Anh Quốc) – cơ quan hàng đầu thế giới về chăm sóc v...

Thương hiệu: Pedigree.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-cho-pedigree-80g',
    stock = 81,
    weight = 100,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_042_pate-cho-cho-pedigree-80g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pedigree-cho-80g_2.jpg?v=1765876415');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Chó Pedigree 80g', 'products/paddy_042_pate-cho-cho-pedigree-80g.jpg', 13000, 0,
       'Pate Cho Chó Pedigree 80g Thương hiệu: Pedigree Phù hợp cho: Chó mọi lứa tuổi (phân loại trong sản phẩm) Pate chó Pedigree là dòng thức ăn ướt cho chó được phát triển bởi các chuyên gia dinh dưỡng WALTHAM (Anh Quốc) – cơ quan hàng đầu thế giới về chăm sóc v...

Thương hiệu: Pedigree.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-cho-pedigree-80g', 81, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_042_pate-cho-cho-pedigree-80g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pedigree-cho-80g_2.jpg?v=1765876415'));

UPDATE products
SET name = 'Hạt Cho Chó Trưởng Thành Gran Deli 700g',
    image = 'products/paddy_043_hat-cho-cho-truong-thanh-gran-deli-700g.jpg',
    price = 90000,
    discount = 0,
    description = 'Hạt Cho Chó Trưởng Thành Gran Deli 700g Thương hiệu: Silver Spoon Phù hợp cho: Chó trưởng thành Thức ăn hạt cho chó Gran Deli cao cấp đến từ Nhật Bản, được thiết kế với hương vị thơm ngon và thành phần dinh dưỡng cân bằng cùng với 3 loại topping hảo hạng. S...

Thương hiệu: Silver Spoon.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-truong-thanh-gran-deli-700g',
    stock = 82,
    weight = 100,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_043_hat-cho-cho-truong-thanh-gran-deli-700g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-cho-truong-thanh-gran-deli-700g_2.jpg?v=1761819765');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Chó Trưởng Thành Gran Deli 700g', 'products/paddy_043_hat-cho-cho-truong-thanh-gran-deli-700g.jpg', 90000, 0,
       'Hạt Cho Chó Trưởng Thành Gran Deli 700g Thương hiệu: Silver Spoon Phù hợp cho: Chó trưởng thành Thức ăn hạt cho chó Gran Deli cao cấp đến từ Nhật Bản, được thiết kế với hương vị thơm ngon và thành phần dinh dưỡng cân bằng cùng với 3 loại topping hảo hạng. S...

Thương hiệu: Silver Spoon.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-truong-thanh-gran-deli-700g', 82, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_043_hat-cho-cho-truong-thanh-gran-deli-700g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-cho-truong-thanh-gran-deli-700g_2.jpg?v=1761819765'));

UPDATE products
SET name = 'Gel Dinh Dưỡng & Chức Năng Cho Mèo Kit Cat',
    image = 'products/paddy_044_gel-dinh-duong-chuc-nang-cho-meo-kit-cat.jpg',
    price = 90000,
    discount = 0,
    description = 'Gel Dinh Dưỡng & Chức Năng Cho Mèo Kit Cat Thương hiệu: Kit Cat Phù hợp cho: Mèo từ 3 tháng tuổi trở lên Gel dinh dưỡng cho mèo Kit Cat là dòng gel bổ sung tiện dụng, được thiết kế để hỗ trợ từng nhu cầu sức khỏe riêng của mèo. Kết cấu gel mềm mịn, hương vị...

Thương hiệu: Kit Cat.

Nguồn tham khảo: https://paddy.vn/products/gel-dinh-duong-chuc-nang-cho-meo-kit-cat',
    stock = 83,
    weight = 150,
    category = 'thực phẩm chức năng',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_044_gel-dinh-duong-chuc-nang-cho-meo-kit-cat.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/gel-dinh-duong-chuc-nang-cho-meo-kit-cat_2.jpg?v=1763639938');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Gel Dinh Dưỡng & Chức Năng Cho Mèo Kit Cat', 'products/paddy_044_gel-dinh-duong-chuc-nang-cho-meo-kit-cat.jpg', 90000, 0,
       'Gel Dinh Dưỡng & Chức Năng Cho Mèo Kit Cat Thương hiệu: Kit Cat Phù hợp cho: Mèo từ 3 tháng tuổi trở lên Gel dinh dưỡng cho mèo Kit Cat là dòng gel bổ sung tiện dụng, được thiết kế để hỗ trợ từng nhu cầu sức khỏe riêng của mèo. Kết cấu gel mềm mịn, hương vị...

Thương hiệu: Kit Cat.

Nguồn tham khảo: https://paddy.vn/products/gel-dinh-duong-chuc-nang-cho-meo-kit-cat', 83, 150, 'thực phẩm chức năng',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_044_gel-dinh-duong-chuc-nang-cho-meo-kit-cat.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/gel-dinh-duong-chuc-nang-cho-meo-kit-cat_2.jpg?v=1763639938'));

UPDATE products
SET name = 'Bánh Thưởng Cho Mèo Temptations Thơm Ngon 75g',
    image = 'products/paddy_045_banh-thuong-cho-meo-temptations-thom-ngon-75g.png',
    price = 49000,
    discount = 0,
    description = 'Bánh Thưởng Cho Mèo Temptations Thơm Ngon 75g Thương hiệu: Temptations Phù hợp cho: Mèo mọi lứa tuổi Bánh Thưởng Cho Mèo Temptations có lớp vỏ giòn và nhân kem mềm được làm từ thịt và các nguyên liệu có nguồn gốc động vật, mang đến cho mèo chút bất ngờ ngon...

Thương hiệu: Temptations.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-meo-temptations-thom-ngon-75g',
    stock = 84,
    weight = 100,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_045_banh-thuong-cho-meo-temptations-thom-ngon-75g.png', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/temptations-5-1761810182735.png?v=1763454909');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Cho Mèo Temptations Thơm Ngon 75g', 'products/paddy_045_banh-thuong-cho-meo-temptations-thom-ngon-75g.png', 49000, 0,
       'Bánh Thưởng Cho Mèo Temptations Thơm Ngon 75g Thương hiệu: Temptations Phù hợp cho: Mèo mọi lứa tuổi Bánh Thưởng Cho Mèo Temptations có lớp vỏ giòn và nhân kem mềm được làm từ thịt và các nguyên liệu có nguồn gốc động vật, mang đến cho mèo chút bất ngờ ngon...

Thương hiệu: Temptations.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-meo-temptations-thom-ngon-75g', 84, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_045_banh-thuong-cho-meo-temptations-thom-ngon-75g.png', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/temptations-5-1761810182735.png?v=1763454909'));

UPDATE products
SET name = 'Máy Dọn Phân Tự Động Cho Mèo Petree Version 2',
    image = 'products/paddy_046_may-don-phan-tu-dong-cho-meo-petree-version-2.jpg',
    price = 5250000,
    discount = 0,
    description = 'Máy Dọn Phân Tự Động Cho Mèo Petree Version 2 Thương hiệu: Petree Phù hợp cho: Mèo mọi lứa tuổi Máy vệ sinh tự động cho mèo Petree là trợ thủ đắc lực giúp bạn tối ưu hóa thời gian, giải quyết mọi lo lắng về vệ sinh một cách tự động và hiệu quả. Với thiết...

Thương hiệu: Petree.

Nguồn tham khảo: https://paddy.vn/products/may-don-phan-tu-dong-cho-meo-petree-version-2',
    stock = 8,
    weight = 10000,
    category = 'Vệ Sinh Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_046_may-don-phan-tu-dong-cho-meo-petree-version-2.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/may-don-phan-tu-dong-cho-meo-petree-version-2-d85c2bee5142.jpg?v=1747191567');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Máy Dọn Phân Tự Động Cho Mèo Petree Version 2', 'products/paddy_046_may-don-phan-tu-dong-cho-meo-petree-version-2.jpg', 5250000, 0,
       'Máy Dọn Phân Tự Động Cho Mèo Petree Version 2 Thương hiệu: Petree Phù hợp cho: Mèo mọi lứa tuổi Máy vệ sinh tự động cho mèo Petree là trợ thủ đắc lực giúp bạn tối ưu hóa thời gian, giải quyết mọi lo lắng về vệ sinh một cách tự động và hiệu quả. Với thiết...

Thương hiệu: Petree.

Nguồn tham khảo: https://paddy.vn/products/may-don-phan-tu-dong-cho-meo-petree-version-2', 8, 10000, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_046_may-don-phan-tu-dong-cho-meo-petree-version-2.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/may-don-phan-tu-dong-cho-meo-petree-version-2-d85c2bee5142.jpg?v=1747191567'));

UPDATE products
SET name = 'Máy Dọn Phân Tự Động Cho Mèo Neakasa',
    image = 'products/paddy_047_may-don-phan-tu-dong-cho-meo-neakasa.jpg',
    price = 7900000,
    discount = 0,
    description = 'Máy Dọn Phân Tự Động Cho Mèo Neakasa Thương hiệu: Neakasa Phù hợp cho: Mèo mọi lứa tuổi Máy vệ sinh tự động cho mèo được làm từ các chất liệu chất lượng cao như PP, ABS và POM, có khả năng chịu được va chạm tốt, giúp đảm bảo độ bền trong suốt quá trình sử d...

Thương hiệu: Neakasa.

Nguồn tham khảo: https://paddy.vn/products/may-don-phan-tu-dong-cho-meo-neakasa',
    stock = 41,
    weight = 10000,
    category = 'Vệ Sinh Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_047_may-don-phan-tu-dong-cho-meo-neakasa.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/may-don-phan-tu-dong-cho-meo-neakasa_2.jpg?v=1763437696');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Máy Dọn Phân Tự Động Cho Mèo Neakasa', 'products/paddy_047_may-don-phan-tu-dong-cho-meo-neakasa.jpg', 7900000, 0,
       'Máy Dọn Phân Tự Động Cho Mèo Neakasa Thương hiệu: Neakasa Phù hợp cho: Mèo mọi lứa tuổi Máy vệ sinh tự động cho mèo được làm từ các chất liệu chất lượng cao như PP, ABS và POM, có khả năng chịu được va chạm tốt, giúp đảm bảo độ bền trong suốt quá trình sử d...

Thương hiệu: Neakasa.

Nguồn tham khảo: https://paddy.vn/products/may-don-phan-tu-dong-cho-meo-neakasa', 41, 10000, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_047_may-don-phan-tu-dong-cho-meo-neakasa.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/may-don-phan-tu-dong-cho-meo-neakasa_2.jpg?v=1763437696'));

UPDATE products
SET name = 'Súp Thưởng Cho Mèo Bite of Wild (15gx4)',
    image = 'products/paddy_048_sup-thuong-cho-meo-bite-of-wild-15gx4.jpg',
    price = 20000,
    discount = 0,
    description = 'Súp Thưởng Cho Mèo Bite of Wild (15gx4) Thương hiệu: Bite of Wild Phù hợp cho: Mèo mọi lứa tuổi Súp thưởng cho mèo Bite of Wild cung cấp nguồn protein chất lượng cao, không gelatin, không tinh bột, không chất phụ gia, bổ sung nhóm vitamin cần thiết giúp tăn...

Thương hiệu: Bite of Wild.

Nguồn tham khảo: https://paddy.vn/products/sup-thuong-cho-meo-bite-of-wild-15gx4',
    stock = 8,
    weight = 100,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_048_sup-thuong-cho-meo-bite-of-wild-15gx4.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/sup-thuong-cho-meo-bite-of-wild_2.jpg?v=1761812137');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Súp Thưởng Cho Mèo Bite of Wild (15gx4)', 'products/paddy_048_sup-thuong-cho-meo-bite-of-wild-15gx4.jpg', 20000, 0,
       'Súp Thưởng Cho Mèo Bite of Wild (15gx4) Thương hiệu: Bite of Wild Phù hợp cho: Mèo mọi lứa tuổi Súp thưởng cho mèo Bite of Wild cung cấp nguồn protein chất lượng cao, không gelatin, không tinh bột, không chất phụ gia, bổ sung nhóm vitamin cần thiết giúp tăn...

Thương hiệu: Bite of Wild.

Nguồn tham khảo: https://paddy.vn/products/sup-thuong-cho-meo-bite-of-wild-15gx4', 8, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_048_sup-thuong-cho-meo-bite-of-wild-15gx4.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/sup-thuong-cho-meo-bite-of-wild_2.jpg?v=1761812137'));

UPDATE products
SET name = 'Bánh Thưởng Cho Chó Gà Sấy DoggyMan Pawfect Choice 180g',
    image = 'products/paddy_049_snack-ga-say-cho-cho-doggyman-pawfect-choice-180g.jpg',
    price = 130000,
    discount = 0,
    description = 'Bánh Thưởng Cho Chó Gà Sấy DoggyMan Pawfect Choice 180g Thương hiệu: DoggyMan Phù hợp cho: Chó từ 3 tháng tuổi trở lên Que thưởng snack cho chó được làm từ nguyên liệu tự nhiên chất lượng cao, kết hợp lớp que bột bắp dễ tiêu hóa và thịt tươi sấy dẻo hoặc sấ...

Thương hiệu: DoggyMan.

Nguồn tham khảo: https://paddy.vn/products/snack-ga-say-cho-cho-doggyman-pawfect-choice-180g',
    stock = 43,
    weight = 100,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_049_snack-ga-say-cho-cho-doggyman-pawfect-choice-180g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/snack-ga-say-cho-cho-doggyman-pawfect-choice-180g_4.jpg?v=1761809961');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Cho Chó Gà Sấy DoggyMan Pawfect Choice 180g', 'products/paddy_049_snack-ga-say-cho-cho-doggyman-pawfect-choice-180g.jpg', 130000, 0,
       'Bánh Thưởng Cho Chó Gà Sấy DoggyMan Pawfect Choice 180g Thương hiệu: DoggyMan Phù hợp cho: Chó từ 3 tháng tuổi trở lên Que thưởng snack cho chó được làm từ nguyên liệu tự nhiên chất lượng cao, kết hợp lớp que bột bắp dễ tiêu hóa và thịt tươi sấy dẻo hoặc sấ...

Thương hiệu: DoggyMan.

Nguồn tham khảo: https://paddy.vn/products/snack-ga-say-cho-cho-doggyman-pawfect-choice-180g', 43, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_049_snack-ga-say-cho-cho-doggyman-pawfect-choice-180g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/snack-ga-say-cho-cho-doggyman-pawfect-choice-180g_4.jpg?v=1761809961'));

UPDATE products
SET name = 'Hạt Cho Mèo Mọi Lứa Tuổi Cattyman',
    image = 'products/paddy_050_hat-cho-meo-moi-lua-tuoi-cattyman.jpg',
    price = 30000,
    discount = 0,
    description = 'Hạt Cho Mèo Mọi Lứa Tuổi Cattyman Thương hiệu: Cattyman Phù hợp cho: Mèo mọi lứa tuổi ( Từ 2 tháng tuổi trở lên) Thức ăn hạt cho mèo CattyMan được nghiên cứu dành cho nhu cầu dinh dưỡng của mèo ở mọi lứa tuổi. Sản phẩm cung cấp tỷ lệ cân bằng giữa đạm, chất...

Thương hiệu: CattyMan.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-moi-lua-tuoi-cattyman',
    stock = 44,
    weight = 100,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_050_hat-cho-meo-moi-lua-tuoi-cattyman.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-meo-moi-lua-tuoi-cattyman_4.jpg?v=1762321948');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo Mọi Lứa Tuổi Cattyman', 'products/paddy_050_hat-cho-meo-moi-lua-tuoi-cattyman.jpg', 30000, 0,
       'Hạt Cho Mèo Mọi Lứa Tuổi Cattyman Thương hiệu: Cattyman Phù hợp cho: Mèo mọi lứa tuổi ( Từ 2 tháng tuổi trở lên) Thức ăn hạt cho mèo CattyMan được nghiên cứu dành cho nhu cầu dinh dưỡng của mèo ở mọi lứa tuổi. Sản phẩm cung cấp tỷ lệ cân bằng giữa đạm, chất...

Thương hiệu: CattyMan.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-moi-lua-tuoi-cattyman', 44, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_050_hat-cho-meo-moi-lua-tuoi-cattyman.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-meo-moi-lua-tuoi-cattyman_4.jpg?v=1762321948'));

UPDATE products
SET name = 'Hạt Cho Mèo Mọi Lứa Tuổi S2Pet V1-V2-V3',
    image = 'products/paddy_051_hat-cho-meo-moi-lua-tuoi-s2pet-v1-v2-v3.jpg',
    price = 90000,
    discount = 0,
    description = 'Hạt Cho Mèo Mọi Lứa Tuổi S2Pet V1-V2-V3 Thương hiệu: S2Pet Phù hợp cho: Mèo mọi lứa tuổi Thức ăn hạt cho mèo được chế biến từ nguồn nguyên liệu tự nhiên và giàu dinh dưỡng, mang đến bữa ăn thơm ngon và cân bằng cho thú cưng. Với công thức đặc biệt 30% toppi...

Thương hiệu: S2Pet.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-moi-lua-tuoi-s2pet-v1-v2-v3',
    stock = 45,
    weight = 100,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_051_hat-cho-meo-moi-lua-tuoi-s2pet-v1-v2-v3.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-meo-moi-lua-tuoi-s2pet-v1-v2-v3_ca1df0f6-346f-46f7-90ef-bb2258aba57b.jpg?v=1763982335');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo Mọi Lứa Tuổi S2Pet V1-V2-V3', 'products/paddy_051_hat-cho-meo-moi-lua-tuoi-s2pet-v1-v2-v3.jpg', 90000, 0,
       'Hạt Cho Mèo Mọi Lứa Tuổi S2Pet V1-V2-V3 Thương hiệu: S2Pet Phù hợp cho: Mèo mọi lứa tuổi Thức ăn hạt cho mèo được chế biến từ nguồn nguyên liệu tự nhiên và giàu dinh dưỡng, mang đến bữa ăn thơm ngon và cân bằng cho thú cưng. Với công thức đặc biệt 30% toppi...

Thương hiệu: S2Pet.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-moi-lua-tuoi-s2pet-v1-v2-v3', 45, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_051_hat-cho-meo-moi-lua-tuoi-s2pet-v1-v2-v3.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-meo-moi-lua-tuoi-s2pet-v1-v2-v3_ca1df0f6-346f-46f7-90ef-bb2258aba57b.jpg?v=1763982335'));

UPDATE products
SET name = 'Pate Cho Mèo Cao Cấp Bite of Wild Túi 70G',
    image = 'products/paddy_052_pate-cho-meo-cao-cap-bite-of-wild-tui-70g.jpg',
    price = 17000,
    discount = 0,
    description = 'Pate Cho Mèo Cao Cấp Bite of Wild Túi 70G Thương hiệu: Bite of Wild Phù hợp cho: Mèo mọi lứa tuổi Pate Cho Mèo cao cấp Bite of Wild là sự kết hợp đạm tự nhiên và sữa dê New Zealand 0% lactose – công thức gần nhất với sữa mẹ, hỗ trợ tiêu hóa & tăng đề kháng....

Thương hiệu: Bite of Wild.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-cao-cap-bite-of-wild-tui-70g',
    stock = 8,
    weight = 100,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_052_pate-cho-meo-cao-cap-bite-of-wild-tui-70g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pate-cho-meo-cao-cap-bite-of-wild-tui-70g_ca5af7c3-6196-41c5-ac07-bc6aa72c9de2.jpg?v=1776919011');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Mèo Cao Cấp Bite of Wild Túi 70G', 'products/paddy_052_pate-cho-meo-cao-cap-bite-of-wild-tui-70g.jpg', 17000, 0,
       'Pate Cho Mèo Cao Cấp Bite of Wild Túi 70G Thương hiệu: Bite of Wild Phù hợp cho: Mèo mọi lứa tuổi Pate Cho Mèo cao cấp Bite of Wild là sự kết hợp đạm tự nhiên và sữa dê New Zealand 0% lactose – công thức gần nhất với sữa mẹ, hỗ trợ tiêu hóa & tăng đề kháng....

Thương hiệu: Bite of Wild.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-cao-cap-bite-of-wild-tui-70g', 8, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_052_pate-cho-meo-cao-cap-bite-of-wild-tui-70g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pate-cho-meo-cao-cap-bite-of-wild-tui-70g_ca5af7c3-6196-41c5-ac07-bc6aa72c9de2.jpg?v=1776919011'));

UPDATE products
SET name = 'Sữa Tươi Kyushu Cho Chó Mèo Không Chứa Lactose 200ml',
    image = 'products/paddy_053_sua-tuoi-kyushu-cho-cho-meo-khong-chua-lactose-200ml.jpg',
    price = 32000,
    discount = 0,
    description = 'Sữa Tươi Kyushu Cho Chó Mèo Không Chứa Lactose 200ml Thương hiệu: DoggyMan Phù hợp cho: Chó/Mèo mọi lứa tuổi Sữa cho chó mèo Kyushu chứa protein chất lượng cao, bổ sung dinh dưỡng hằng ngày, chăm sóc sức khỏe cho thú cưng. Đặc biệt, sản phẩm không chứa lact...

Thương hiệu: DoggyMan.

Nguồn tham khảo: https://paddy.vn/products/sua-tuoi-kyushu-cho-cho-meo-khong-chua-lactose-200ml',
    stock = 47,
    weight = 100,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_053_sua-tuoi-kyushu-cho-cho-meo-khong-chua-lactose-200ml.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/sua-tuoi-kyushu-cho-cho-meo-khong-chua-lactose-200ml_2.jpg?v=1762322273');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Sữa Tươi Kyushu Cho Chó Mèo Không Chứa Lactose 200ml', 'products/paddy_053_sua-tuoi-kyushu-cho-cho-meo-khong-chua-lactose-200ml.jpg', 32000, 0,
       'Sữa Tươi Kyushu Cho Chó Mèo Không Chứa Lactose 200ml Thương hiệu: DoggyMan Phù hợp cho: Chó/Mèo mọi lứa tuổi Sữa cho chó mèo Kyushu chứa protein chất lượng cao, bổ sung dinh dưỡng hằng ngày, chăm sóc sức khỏe cho thú cưng. Đặc biệt, sản phẩm không chứa lact...

Thương hiệu: DoggyMan.

Nguồn tham khảo: https://paddy.vn/products/sua-tuoi-kyushu-cho-cho-meo-khong-chua-lactose-200ml', 47, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_053_sua-tuoi-kyushu-cho-cho-meo-khong-chua-lactose-200ml.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/sua-tuoi-kyushu-cho-cho-meo-khong-chua-lactose-200ml_2.jpg?v=1762322273'));

UPDATE products
SET name = 'Hạt Cho Mèo Cature Easy Farm Topping 1.5kg',
    image = 'products/paddy_054_hat-cho-meo-cature-easy-farm-topping-1-5kg.jpg',
    price = 195000,
    discount = 0,
    description = 'Hạt Cho Mèo Cature Easy Farm Topping 1.5kg Thương hiệu: Cature Phù hợp cho: Mèo mọi lứa tuổi Thức ăn hạt cho mèo Cature Easy Farm là sự lựa chọn hoàn hảo giúp thú cưng khoẻ mạnh từ trong ra ngoài. Với 90% protein động vật, kết hợp cùng 7 loại siêu thực phẩm...

Thương hiệu: Cature.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-cature-easy-farm-topping-1-5kg',
    stock = 8,
    weight = 1700,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_054_hat-cho-meo-cature-easy-farm-topping-1-5kg.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-meo-cature-easy-farm-topping-1-5kg_4.jpg?v=1762322814');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo Cature Easy Farm Topping 1.5kg', 'products/paddy_054_hat-cho-meo-cature-easy-farm-topping-1-5kg.jpg', 195000, 0,
       'Hạt Cho Mèo Cature Easy Farm Topping 1.5kg Thương hiệu: Cature Phù hợp cho: Mèo mọi lứa tuổi Thức ăn hạt cho mèo Cature Easy Farm là sự lựa chọn hoàn hảo giúp thú cưng khoẻ mạnh từ trong ra ngoài. Với 90% protein động vật, kết hợp cùng 7 loại siêu thực phẩm...

Thương hiệu: Cature.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-cature-easy-farm-topping-1-5kg', 8, 1700, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_054_hat-cho-meo-cature-easy-farm-topping-1-5kg.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-meo-cature-easy-farm-topping-1-5kg_4.jpg?v=1762322814'));

UPDATE products
SET name = 'Khay Vệ Sinh Cho Mèo Mon Ami Cao Cấp',
    image = 'products/paddy_055_khay-ve-sinh-cho-meo-mon-ami-cao-cap.jpg',
    price = 200000,
    discount = 0,
    description = 'Khay Vệ Sinh Cho Mèo Mon Ami Cao Cấp Thương hiệu: Mon Ami Phù hợp cho: Mèo mọi lứa tuổi Khay Vệ Sinh là sản phẩm dùng để đựng cát vệ sinh cho mèo, giúp mèo đi vệ sinh đúng chỗ. Sản phẩm được làm từ chất liệu nhựa PP không độc hại, an toàn cho người và vật n...

Thương hiệu: Mon Ami.

Nguồn tham khảo: https://paddy.vn/products/khay-ve-sinh-cho-meo-mon-ami-cao-cap',
    stock = 49,
    weight = 100,
    category = 'Vệ Sinh Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_055_khay-ve-sinh-cho-meo-mon-ami-cao-cap.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/khay-ve-sinh-cho-meo-mon-ami-cao-cap.jpg?v=1761195049');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Khay Vệ Sinh Cho Mèo Mon Ami Cao Cấp', 'products/paddy_055_khay-ve-sinh-cho-meo-mon-ami-cao-cap.jpg', 200000, 0,
       'Khay Vệ Sinh Cho Mèo Mon Ami Cao Cấp Thương hiệu: Mon Ami Phù hợp cho: Mèo mọi lứa tuổi Khay Vệ Sinh là sản phẩm dùng để đựng cát vệ sinh cho mèo, giúp mèo đi vệ sinh đúng chỗ. Sản phẩm được làm từ chất liệu nhựa PP không độc hại, an toàn cho người và vật n...

Thương hiệu: Mon Ami.

Nguồn tham khảo: https://paddy.vn/products/khay-ve-sinh-cho-meo-mon-ami-cao-cap', 49, 100, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_055_khay-ve-sinh-cho-meo-mon-ami-cao-cap.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/khay-ve-sinh-cho-meo-mon-ami-cao-cap.jpg?v=1761195049'));

UPDATE products
SET name = 'Súp Thưởng Cho Chó Mèo VFcore Chức Năng',
    image = 'products/paddy_056_sup-thuong-cho-cho-meo-vfcore-chuc-nang.jpg',
    price = 17000,
    discount = 0,
    description = 'Súp Thưởng Cho Chó Mèo VFcore Chức Năng Thương hiệu: VFcore Phù hợp cho: Chó/Mèo mọi lứa tuổi Súp thưởng cho chó mèo VF+Core là dòng thực phẩm chức năng dạng súp thưởng cao cấp đến từ thương hiệu VetSynova (Thái Lan). Sản phẩm được nghiên cứu chuyên sâu nhằ...

Thương hiệu: VFcore.

Nguồn tham khảo: https://paddy.vn/products/sup-thuong-cho-cho-meo-vfcore-chuc-nang',
    stock = 50,
    weight = 100,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_056_sup-thuong-cho-cho-meo-vfcore-chuc-nang.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/sup-thuong-cho-cho-meo-vfcore-chuc-nang.jpg?v=1761037893');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Súp Thưởng Cho Chó Mèo VFcore Chức Năng', 'products/paddy_056_sup-thuong-cho-cho-meo-vfcore-chuc-nang.jpg', 17000, 0,
       'Súp Thưởng Cho Chó Mèo VFcore Chức Năng Thương hiệu: VFcore Phù hợp cho: Chó/Mèo mọi lứa tuổi Súp thưởng cho chó mèo VF+Core là dòng thực phẩm chức năng dạng súp thưởng cao cấp đến từ thương hiệu VetSynova (Thái Lan). Sản phẩm được nghiên cứu chuyên sâu nhằ...

Thương hiệu: VFcore.

Nguồn tham khảo: https://paddy.vn/products/sup-thuong-cho-cho-meo-vfcore-chuc-nang', 50, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_056_sup-thuong-cho-cho-meo-vfcore-chuc-nang.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/sup-thuong-cho-cho-meo-vfcore-chuc-nang.jpg?v=1761037893'));

UPDATE products
SET name = 'Dung Dịch Vệ Sinh Tai Cho Chó Mèo Jungle Monster Chamomile 120ml',
    image = 'products/paddy_057_dung-dich-ve-sinh-tai-cho-cho-meo-jungle-monster-chamomile-120ml.jpg',
    price = 400000,
    discount = 0,
    description = 'Dung Dịch Vệ Sinh Tai Cho Chó Mèo Jungle Monster Chamomile 120ml Thương hiệu: Jungle Monster Phù hợp cho: Chó/Mèo mọi lứa tuổi Dung dịch vệ sinh tai cho chó mèo Jungle Monster Ear Cleaner là sản phẩm làm sạch tai dịu nhẹ cho chó và mèo, giúp loại bỏ ráy tai...

Thương hiệu: Jungle Monster.

Nguồn tham khảo: https://paddy.vn/products/dung-dich-ve-sinh-tai-cho-cho-meo-jungle-monster-chamomile-120ml',
    stock = 51,
    weight = 100,
    category = 'Vệ Sinh Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_057_dung-dich-ve-sinh-tai-cho-cho-meo-jungle-monster-chamomile-120ml.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/dung-dich-ve-sinh-tai-cho-cho-meo-jungle-monster-chamomile-120ml.jpg?v=1760607081');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Dung Dịch Vệ Sinh Tai Cho Chó Mèo Jungle Monster Chamomile 120ml', 'products/paddy_057_dung-dich-ve-sinh-tai-cho-cho-meo-jungle-monster-chamomile-120ml.jpg', 400000, 0,
       'Dung Dịch Vệ Sinh Tai Cho Chó Mèo Jungle Monster Chamomile 120ml Thương hiệu: Jungle Monster Phù hợp cho: Chó/Mèo mọi lứa tuổi Dung dịch vệ sinh tai cho chó mèo Jungle Monster Ear Cleaner là sản phẩm làm sạch tai dịu nhẹ cho chó và mèo, giúp loại bỏ ráy tai...

Thương hiệu: Jungle Monster.

Nguồn tham khảo: https://paddy.vn/products/dung-dich-ve-sinh-tai-cho-cho-meo-jungle-monster-chamomile-120ml', 51, 100, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_057_dung-dich-ve-sinh-tai-cho-cho-meo-jungle-monster-chamomile-120ml.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/dung-dich-ve-sinh-tai-cho-cho-meo-jungle-monster-chamomile-120ml.jpg?v=1760607081'));

UPDATE products
SET name = 'Xịt Thơm Miệng Cho Chó Mèo Jungle Monster Dental Spray 30ml',
    image = 'products/paddy_058_xit-thom-mieng-cho-cho-meo-jungle-monster-dental-spray-30ml.jpg',
    price = 390000,
    discount = 0,
    description = 'Xịt Thơm Miệng Cho Chó Mèo Jungle Monster Dental Spray 30ml Thương hiệu: Jungle Monster Phù hợp cho: Chó/Mèo từ 3 tháng tuổi trở lên Thú cưng mới bắt đầu chăm sóc răng miệng Thú cưng khó chịu khi chải răng Thú cưng có hơi thở hôi Thú cưng cần ngăn ngừa cao...

Thương hiệu: Jungle Monster.

Nguồn tham khảo: https://paddy.vn/products/xit-thom-mieng-cho-cho-meo-jungle-monster-dental-spray-30ml',
    stock = 52,
    weight = 100,
    category = 'Vệ Sinh Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_058_xit-thom-mieng-cho-cho-meo-jungle-monster-dental-spray-30ml.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/xit-thom-mieng-cho-cho-meo-jungle-monster-dental-spray-30ml_4.jpg?v=1760438508');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Xịt Thơm Miệng Cho Chó Mèo Jungle Monster Dental Spray 30ml', 'products/paddy_058_xit-thom-mieng-cho-cho-meo-jungle-monster-dental-spray-30ml.jpg', 390000, 0,
       'Xịt Thơm Miệng Cho Chó Mèo Jungle Monster Dental Spray 30ml Thương hiệu: Jungle Monster Phù hợp cho: Chó/Mèo từ 3 tháng tuổi trở lên Thú cưng mới bắt đầu chăm sóc răng miệng Thú cưng khó chịu khi chải răng Thú cưng có hơi thở hôi Thú cưng cần ngăn ngừa cao...

Thương hiệu: Jungle Monster.

Nguồn tham khảo: https://paddy.vn/products/xit-thom-mieng-cho-cho-meo-jungle-monster-dental-spray-30ml', 52, 100, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_058_xit-thom-mieng-cho-cho-meo-jungle-monster-dental-spray-30ml.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/xit-thom-mieng-cho-cho-meo-jungle-monster-dental-spray-30ml_4.jpg?v=1760438508'));

UPDATE products
SET name = 'Pate Cho Mèo Trưởng Thành Me-O Delite Gói 70g',
    image = 'products/paddy_059_pate-cho-meo-truong-thanh-me-o-delite-goi-70g.jpg',
    price = 18000,
    discount = 0,
    description = 'Pate Cho Mèo Trưởng Thành Me-O Delite Gói 70g Thương hiệu: Me-O Phù hợp cho: Mèo từ 1 tuổi trở lên Pate cho mèo Me-o là dòng thức ăn ướt dạng túi dành cho mèo trưởng thành (từ 1 tuổi trở lên) với thành phần chứa cá ngừ kết hợp cá ngừ sọc dưa, kèm topping th...

Thương hiệu: Me-O.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-truong-thanh-me-o-delite-goi-70g',
    stock = 53,
    weight = 100,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_059_pate-cho-meo-truong-thanh-me-o-delite-goi-70g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pate-cho-meo-truong-thanh-me-o-delite-goi-70g-5.jpg?v=1760428761');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Mèo Trưởng Thành Me-O Delite Gói 70g', 'products/paddy_059_pate-cho-meo-truong-thanh-me-o-delite-goi-70g.jpg', 18000, 0,
       'Pate Cho Mèo Trưởng Thành Me-O Delite Gói 70g Thương hiệu: Me-O Phù hợp cho: Mèo từ 1 tuổi trở lên Pate cho mèo Me-o là dòng thức ăn ướt dạng túi dành cho mèo trưởng thành (từ 1 tuổi trở lên) với thành phần chứa cá ngừ kết hợp cá ngừ sọc dưa, kèm topping th...

Thương hiệu: Me-O.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-truong-thanh-me-o-delite-goi-70g', 53, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_059_pate-cho-meo-truong-thanh-me-o-delite-goi-70g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pate-cho-meo-truong-thanh-me-o-delite-goi-70g-5.jpg?v=1760428761'));

UPDATE products
SET name = 'Dây Dắt Bấm Tự Động Cho Chó Flexi',
    image = 'products/paddy_060_day-dat-bam-tu-dong-cho-cho-flexi.jpg',
    price = 440000,
    discount = 0,
    description = 'Dây Dắt Bấm Tự Động Cho Chó Flexi Thương hiệu: Mon Ami Phù hợp cho: Chó mọi lứa tuổi Dây dắt chó là phụ kiện thú cưng giúp thu gọn Flexi Fun lựa chọn tiện lợi và an toàn giúp thú cưng có thể thoải mái di chuyển trong khi bạn vẫn kiểm soát được. Với thiết kế...

Thương hiệu: Mon Ami.

Nguồn tham khảo: https://paddy.vn/products/day-dat-bam-tu-dong-cho-cho-flexi',
    stock = 54,
    weight = 600,
    category = 'Phụ Kiện Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_060_day-dat-bam-tu-dong-cho-cho-flexi.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/day-dat-bam-tu-dong-cho-cho-flexi-classic_4_377dc5bb-8885-4d9f-9fc9-a62eca4fa679.jpg?v=1759204139');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Dây Dắt Bấm Tự Động Cho Chó Flexi', 'products/paddy_060_day-dat-bam-tu-dong-cho-cho-flexi.jpg', 440000, 0,
       'Dây Dắt Bấm Tự Động Cho Chó Flexi Thương hiệu: Mon Ami Phù hợp cho: Chó mọi lứa tuổi Dây dắt chó là phụ kiện thú cưng giúp thu gọn Flexi Fun lựa chọn tiện lợi và an toàn giúp thú cưng có thể thoải mái di chuyển trong khi bạn vẫn kiểm soát được. Với thiết kế...

Thương hiệu: Mon Ami.

Nguồn tham khảo: https://paddy.vn/products/day-dat-bam-tu-dong-cho-cho-flexi', 54, 600, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_060_day-dat-bam-tu-dong-cho-cho-flexi.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/day-dat-bam-tu-dong-cho-cho-flexi-classic_4_377dc5bb-8885-4d9f-9fc9-a62eca4fa679.jpg?v=1759204139'));

UPDATE products
SET name = 'Hạt Cho Mèo Smartheart',
    image = 'products/paddy_061_hat-cho-meo-smartheart-cat.jpg',
    price = 120000,
    discount = 0,
    description = 'Hạt Cho Mèo Smartheart Thương hiệu: Smartheart Phù hợp cho: Mèo (tùy loại sản phẩm) Thức ăn hạt cho mèo SmartHeart đạt chuẩn AAFCO, với công thức gấp 3 lần DHA giúp phát triển trí não. Đồng thời, bổ sung các dưỡng chất thiết yếu như chất Đạm, Omega 3, các V...

Thương hiệu: Smartheart.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-smartheart-cat',
    stock = 55,
    weight = 1500,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_061_hat-cho-meo-smartheart-cat.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-meo-smartheart-cat_13.jpg?v=1758794804');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo Smartheart', 'products/paddy_061_hat-cho-meo-smartheart-cat.jpg', 120000, 0,
       'Hạt Cho Mèo Smartheart Thương hiệu: Smartheart Phù hợp cho: Mèo (tùy loại sản phẩm) Thức ăn hạt cho mèo SmartHeart đạt chuẩn AAFCO, với công thức gấp 3 lần DHA giúp phát triển trí não. Đồng thời, bổ sung các dưỡng chất thiết yếu như chất Đạm, Omega 3, các V...

Thương hiệu: Smartheart.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-smartheart-cat', 55, 1500, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_061_hat-cho-meo-smartheart-cat.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-meo-smartheart-cat_13.jpg?v=1758794804'));

UPDATE products
SET name = 'Đồ Chơi Cho Mèo Fofos Lông Vũ',
    image = 'products/paddy_062_do-choi-cho-meo-fofos-long-vu.webp',
    price = 140000,
    discount = 0,
    description = 'Đồ Chơi Cho Mèo Fofos Lông Vũ Thương hiệu: Fofos Phù hợp cho: mèo mọi lứa tuổi Đồ chơi mèo Fofos lông vũ được thiết kế độc đáo với màu sắc nổi bật và chùm lông vũ mềm mại, thu hút bản năng săn mồi tự nhiên của mèo. Sản phẩm có thể tự động di chuyển, kết hợp...

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/do-choi-cho-meo-fofos-long-vu',
    stock = 56,
    weight = 300,
    category = 'Đồ Chơi Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_062_do-choi-cho-meo-fofos-long-vu.webp', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/do-choi-cho-meo-fofos-long-vu_3.webp?v=1758599542');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Đồ Chơi Cho Mèo Fofos Lông Vũ', 'products/paddy_062_do-choi-cho-meo-fofos-long-vu.webp', 140000, 0,
       'Đồ Chơi Cho Mèo Fofos Lông Vũ Thương hiệu: Fofos Phù hợp cho: mèo mọi lứa tuổi Đồ chơi mèo Fofos lông vũ được thiết kế độc đáo với màu sắc nổi bật và chùm lông vũ mềm mại, thu hút bản năng săn mồi tự nhiên của mèo. Sản phẩm có thể tự động di chuyển, kết hợp...

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/do-choi-cho-meo-fofos-long-vu', 56, 300, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_062_do-choi-cho-meo-fofos-long-vu.webp', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/do-choi-cho-meo-fofos-long-vu_3.webp?v=1758599542'));

UPDATE products
SET name = 'Cào Móng Mèo FOFOS Hình Ly Cafe 42x35cm',
    image = 'products/paddy_063_cao-mong-meo-fofos-hinh-ly-cafe-42x35cm.webp',
    price = 460000,
    discount = 0,
    description = 'Cào Móng Mèo FOFOS Hình Ly Cafe 42x35cm Thương hiệu: FOFOS Phù hợp cho: Mèo mọi lứa tuổi FOFOS là thương hiệu đồ chơi cho mèo nổi tiếng. Các sản phẩm của FOFOS được thiết kế dựa trên nhu cầu và sở thích của chó mèo, với mục đích giúp chúng vui chơi, giải tr...

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/cao-mong-meo-fofos-hinh-ly-cafe-42x35cm',
    stock = 57,
    weight = 3200,
    category = 'Đồ Chơi Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_063_cao-mong-meo-fofos-hinh-ly-cafe-42x35cm.webp', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/cao-mong-meo-fofos-hinh-ly-cafe-42x35cm.webp?v=1758009536');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Cào Móng Mèo FOFOS Hình Ly Cafe 42x35cm', 'products/paddy_063_cao-mong-meo-fofos-hinh-ly-cafe-42x35cm.webp', 460000, 0,
       'Cào Móng Mèo FOFOS Hình Ly Cafe 42x35cm Thương hiệu: FOFOS Phù hợp cho: Mèo mọi lứa tuổi FOFOS là thương hiệu đồ chơi cho mèo nổi tiếng. Các sản phẩm của FOFOS được thiết kế dựa trên nhu cầu và sở thích của chó mèo, với mục đích giúp chúng vui chơi, giải tr...

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/cao-mong-meo-fofos-hinh-ly-cafe-42x35cm', 57, 3200, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_063_cao-mong-meo-fofos-hinh-ly-cafe-42x35cm.webp', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/cao-mong-meo-fofos-hinh-ly-cafe-42x35cm.webp?v=1758009536'));

UPDATE products
SET name = 'Cào Móng Mèo FOFOS Hình Trái Dâu 35x30cm',
    image = 'products/paddy_064_cao-mong-meo-fofos-hinh-trai-dau-35x30cm.jpg',
    price = 420000,
    discount = 0,
    description = 'Cào Móng Mèo FOFOS Hình Trái Dâu 35x30cm Thương hiệu: FOFOS Phù hợp cho: Mèo mọi lứa tuổi FOFOS là thương hiệu đồ chơi cho mèo nổi tiếng. Các sản phẩm của FOFOS được thiết kế dựa trên nhu cầu và sở thích của chó mèo, với mục đích giúp chúng vui chơi, giải t...

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/cao-mong-meo-fofos-hinh-trai-dau-35x30cm',
    stock = 58,
    weight = 3200,
    category = 'Đồ Chơi Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_064_cao-mong-meo-fofos-hinh-trai-dau-35x30cm.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/cao-mong-meo-fofos-hinh-trai-dau-35x30cm_3.jpg?v=1757999095');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Cào Móng Mèo FOFOS Hình Trái Dâu 35x30cm', 'products/paddy_064_cao-mong-meo-fofos-hinh-trai-dau-35x30cm.jpg', 420000, 0,
       'Cào Móng Mèo FOFOS Hình Trái Dâu 35x30cm Thương hiệu: FOFOS Phù hợp cho: Mèo mọi lứa tuổi FOFOS là thương hiệu đồ chơi cho mèo nổi tiếng. Các sản phẩm của FOFOS được thiết kế dựa trên nhu cầu và sở thích của chó mèo, với mục đích giúp chúng vui chơi, giải t...

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/cao-mong-meo-fofos-hinh-trai-dau-35x30cm', 58, 3200, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_064_cao-mong-meo-fofos-hinh-trai-dau-35x30cm.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/cao-mong-meo-fofos-hinh-trai-dau-35x30cm_3.jpg?v=1757999095'));

UPDATE products
SET name = 'Hạt Cho Chó Wanpy Không Ngũ Cốc Mix Thịt Viên',
    image = 'products/paddy_065_hat-cho-cho-wanpy-khong-ngu-coc-mix-thit-vien.jpg',
    price = 80000,
    discount = 0,
    description = 'Hạt Cho Chó Wanpy Không Ngũ Cốc Mix Thịt Viên Thương hiệu: Wanpy Premium Phù hợp cho: Chó tuỳ theo độ tuổi Thức ăn hạt cho chó wanpy là dòng hạt cao cấp cho chó, được thiết kế với công thức không chứa ngũ cốc nhằm giảm gánh nặng tiêu hóa, giúp bé cưng ăn ng...

Thương hiệu: Wanpy Premium.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-wanpy-khong-ngu-coc-mix-thit-vien',
    stock = 8,
    weight = 100,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_065_hat-cho-cho-wanpy-khong-ngu-coc-mix-thit-vien.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-cho-wanpy-khong-ngu-coc-mix-thit-vien_5.jpg?v=1757576211');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Chó Wanpy Không Ngũ Cốc Mix Thịt Viên', 'products/paddy_065_hat-cho-cho-wanpy-khong-ngu-coc-mix-thit-vien.jpg', 80000, 0,
       'Hạt Cho Chó Wanpy Không Ngũ Cốc Mix Thịt Viên Thương hiệu: Wanpy Premium Phù hợp cho: Chó tuỳ theo độ tuổi Thức ăn hạt cho chó wanpy là dòng hạt cao cấp cho chó, được thiết kế với công thức không chứa ngũ cốc nhằm giảm gánh nặng tiêu hóa, giúp bé cưng ăn ng...

Thương hiệu: Wanpy Premium.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-wanpy-khong-ngu-coc-mix-thit-vien', 8, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_065_hat-cho-cho-wanpy-khong-ngu-coc-mix-thit-vien.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-cho-wanpy-khong-ngu-coc-mix-thit-vien_5.jpg?v=1757576211'));

UPDATE products
SET name = 'Súp Thưởng Cho Mèo Silver Spoon 100% Từ Thịt Cá Thật 56g',
    image = 'products/paddy_066_sup-thuong-cho-meo-silver-spoon-thailand-56g-4-tuyp.jpg',
    price = 27000,
    discount = 0,
    description = 'Súp Thưởng Cho Mèo Silver Spoon 100% Từ Thịt Cá Thật 56g (4 tuýp) Thương hiệu: Silver Spoon Phù hợp cho: Mèo mọi lứa tuổi Súp thưởng cho mèo Silver Spoon dạng thanh được làm từ 100% thịt cá tươi thật, mang đến hương vị thơm ngon, bổ dưỡng và an toàn cho...

Thương hiệu: Silver Spoon.

Nguồn tham khảo: https://paddy.vn/products/sup-thuong-cho-meo-silver-spoon-thailand-56g-4-tuyp',
    stock = 60,
    weight = 100,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_066_sup-thuong-cho-meo-silver-spoon-thailand-56g-4-tuyp.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/sup-thuong-cho-meo-silver-spoon-thailand-56g-4-tuyp_7.jpg?v=1770003786');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Súp Thưởng Cho Mèo Silver Spoon 100% Từ Thịt Cá Thật 56g', 'products/paddy_066_sup-thuong-cho-meo-silver-spoon-thailand-56g-4-tuyp.jpg', 27000, 0,
       'Súp Thưởng Cho Mèo Silver Spoon 100% Từ Thịt Cá Thật 56g (4 tuýp) Thương hiệu: Silver Spoon Phù hợp cho: Mèo mọi lứa tuổi Súp thưởng cho mèo Silver Spoon dạng thanh được làm từ 100% thịt cá tươi thật, mang đến hương vị thơm ngon, bổ dưỡng và an toàn cho...

Thương hiệu: Silver Spoon.

Nguồn tham khảo: https://paddy.vn/products/sup-thuong-cho-meo-silver-spoon-thailand-56g-4-tuyp', 60, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_066_sup-thuong-cho-meo-silver-spoon-thailand-56g-4-tuyp.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/sup-thuong-cho-meo-silver-spoon-thailand-56g-4-tuyp_7.jpg?v=1770003786'));

UPDATE products
SET name = 'Bánh Thưởng Cho Chó Que Dai Da Bò Doggyman White Dental 120g',
    image = 'products/paddy_067_banh-thuong-cho-cho-que-dai-da-bo-doggyman-white-dental-120g.jpg',
    price = 50000,
    discount = 0,
    description = 'Bánh Thưởng Cho Chó Que Dai Da Bò Doggyman White Dental 120g Thương hiệu: DoggyMan Phù hợp cho: Mọi lứa tuổi Bánh thưởng cho chó Que dai da bò được làm từ da bò tự nhiên, có kết cấu chắc chắn và đàn hồi, giúp thỏa mãn bản năng nhai gặm của chó và kéo dài th...

Thương hiệu: DoggyMan.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-cho-que-dai-da-bo-doggyman-white-dental-120g',
    stock = 61,
    weight = 300,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_067_banh-thuong-cho-cho-que-dai-da-bo-doggyman-white-dental-120g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/banh-thuong-cho-cho-que-dai-da-bo-doggyman-white-dental-120g_2.jpg?v=1757407570');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Cho Chó Que Dai Da Bò Doggyman White Dental 120g', 'products/paddy_067_banh-thuong-cho-cho-que-dai-da-bo-doggyman-white-dental-120g.jpg', 50000, 0,
       'Bánh Thưởng Cho Chó Que Dai Da Bò Doggyman White Dental 120g Thương hiệu: DoggyMan Phù hợp cho: Mọi lứa tuổi Bánh thưởng cho chó Que dai da bò được làm từ da bò tự nhiên, có kết cấu chắc chắn và đàn hồi, giúp thỏa mãn bản năng nhai gặm của chó và kéo dài th...

Thương hiệu: DoggyMan.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-cho-que-dai-da-bo-doggyman-white-dental-120g', 61, 300, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_067_banh-thuong-cho-cho-que-dai-da-bo-doggyman-white-dental-120g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/banh-thuong-cho-cho-que-dai-da-bo-doggyman-white-dental-120g_2.jpg?v=1757407570'));

UPDATE products
SET name = 'Pate Cho Chó Gran Deli Dạng Thạch 80g',
    image = 'products/paddy_068_pate-cho-cho-gran-deli-dang-thach-80g.jpg',
    price = 13000,
    discount = 0,
    description = 'Pate Cho Chó Gran Deli Dạng Thạch 80g Thương hiệu: Silver Spoon Phù hợp cho: Chó trưởng thành Pate cho chó Gran-Deli mang đến bữa ăn thơm ngon từ 100% thịt gà Nhật Bản kết hợp cùng rau củ giàu chất xơ và vitamin, giúp hỗ trợ hệ tiêu hóa khỏe mạnh. Với kết c...

Thương hiệu: Gran Deli.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-cho-gran-deli-dang-thach-80g',
    stock = 8,
    weight = 100,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_068_pate-cho-cho-gran-deli-dang-thach-80g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pate-cho-cho-gran-deli-dang-thach-80g_2.jpg?v=1759743562');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Chó Gran Deli Dạng Thạch 80g', 'products/paddy_068_pate-cho-cho-gran-deli-dang-thach-80g.jpg', 13000, 0,
       'Pate Cho Chó Gran Deli Dạng Thạch 80g Thương hiệu: Silver Spoon Phù hợp cho: Chó trưởng thành Pate cho chó Gran-Deli mang đến bữa ăn thơm ngon từ 100% thịt gà Nhật Bản kết hợp cùng rau củ giàu chất xơ và vitamin, giúp hỗ trợ hệ tiêu hóa khỏe mạnh. Với kết c...

Thương hiệu: Gran Deli.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-cho-gran-deli-dang-thach-80g', 8, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_068_pate-cho-cho-gran-deli-dang-thach-80g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pate-cho-cho-gran-deli-dang-thach-80g_2.jpg?v=1759743562'));

UPDATE products
SET name = 'Lược Chải Lông Cho Chó Mèo Bella',
    image = 'products/paddy_069_luoc-chai-long-cho-cho-meo-bella.jpg',
    price = 75000,
    discount = 0,
    description = 'Lược Chải Lông Cho Chó Mèo Bella Thương hiệu: Mon Ami Phù hợp cho: Chó/Mèo mọi lứa tuổi Chải lông thường xuyên giúp thú cưng sạch sẽ, khỏe mạnh và tránh rối lông gây viêm da. Lược chải lông – phụ kiện chăm sóc thú cưng cần có – giúp loại bỏ lông rụng, bụi b...

Thương hiệu: Mon Ami.

Nguồn tham khảo: https://paddy.vn/products/luoc-chai-long-cho-cho-meo-bella',
    stock = 63,
    weight = 400,
    category = 'Vệ Sinh Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_069_luoc-chai-long-cho-cho-meo-bella.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/luoc-chai-long-cho-cho-meo-bella-9ce51d5925ae.jpg?v=1770699972');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Lược Chải Lông Cho Chó Mèo Bella', 'products/paddy_069_luoc-chai-long-cho-cho-meo-bella.jpg', 75000, 0,
       'Lược Chải Lông Cho Chó Mèo Bella Thương hiệu: Mon Ami Phù hợp cho: Chó/Mèo mọi lứa tuổi Chải lông thường xuyên giúp thú cưng sạch sẽ, khỏe mạnh và tránh rối lông gây viêm da. Lược chải lông – phụ kiện chăm sóc thú cưng cần có – giúp loại bỏ lông rụng, bụi b...

Thương hiệu: Mon Ami.

Nguồn tham khảo: https://paddy.vn/products/luoc-chai-long-cho-cho-meo-bella', 63, 400, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_069_luoc-chai-long-cho-cho-meo-bella.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/luoc-chai-long-cho-cho-meo-bella-9ce51d5925ae.jpg?v=1770699972'));

UPDATE products
SET name = 'Nệm Gối Cho Chó Mèo Hình Chim Cánh Cụt',
    image = 'products/paddy_070_nem-goi-cho-cho-meo-hinh-chim-canh-cut.jpg',
    price = 140000,
    discount = 0,
    description = 'Nệm Gối Cho Chó Mèo Hình Chim Cánh Cụt Thương hiệu: Paddy Phù hợp cho: Chó/Mèo mọi độ tuổi Nệm Gối cho chó mèo hình chim cánh cụt là sản phẩm độc đáo có tích hợp gối. Với thiết kế hình dáng dễ thương, màu sắc tươi sáng và chất liệu an toàn, nệm gối này sẽ m...

Thương hiệu: Paddy Pet Shop.

Nguồn tham khảo: https://paddy.vn/products/nem-goi-cho-cho-meo-hinh-chim-canh-cut',
    stock = 64,
    weight = 600,
    category = 'Phụ Kiện Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_070_nem-goi-cho-cho-meo-hinh-chim-canh-cut.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/nem-goi-canh-cut-93791-doggyman-00.jpg?v=1741758192');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Nệm Gối Cho Chó Mèo Hình Chim Cánh Cụt', 'products/paddy_070_nem-goi-cho-cho-meo-hinh-chim-canh-cut.jpg', 140000, 0,
       'Nệm Gối Cho Chó Mèo Hình Chim Cánh Cụt Thương hiệu: Paddy Phù hợp cho: Chó/Mèo mọi độ tuổi Nệm Gối cho chó mèo hình chim cánh cụt là sản phẩm độc đáo có tích hợp gối. Với thiết kế hình dáng dễ thương, màu sắc tươi sáng và chất liệu an toàn, nệm gối này sẽ m...

Thương hiệu: Paddy Pet Shop.

Nguồn tham khảo: https://paddy.vn/products/nem-goi-cho-cho-meo-hinh-chim-canh-cut', 64, 600, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_070_nem-goi-cho-cho-meo-hinh-chim-canh-cut.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/nem-goi-canh-cut-93791-doggyman-00.jpg?v=1741758192'));

UPDATE products
SET name = 'Sữa Tắm Cho Chó Bossen Fortis Derm Chống Ngứa 250ml',
    image = 'products/paddy_071_sua-tam-cho-cho-bossen-fortis-derm-chong-ngua-250ml.jpg',
    price = 120000,
    discount = 0,
    description = 'Sữa Tắm Cho Chó Bossen Fortis Derm Chống Ngứa 250ml Thương hiệu: Bossen Phù hợp cho: Chó từ 3 tháng tuổi trở lên Sữa tắm cho chó Fortis Derm Anti-Itch là giải pháp chăm sóc da và lông dành cho thú cưng, đặc biệt là những bé hay gặp tình trạng ngứa ngáy, kíc...

Thương hiệu: Bossen.

Nguồn tham khảo: https://paddy.vn/products/sua-tam-cho-cho-bossen-fortis-derm-chong-ngua-250ml',
    stock = 65,
    weight = 300,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_071_sua-tam-cho-cho-bossen-fortis-derm-chong-ngua-250ml.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/sua-tam-cho-cho-bossen-fortis-derm-chong-ngua-250ml.jpg?v=1756353500');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Sữa Tắm Cho Chó Bossen Fortis Derm Chống Ngứa 250ml', 'products/paddy_071_sua-tam-cho-cho-bossen-fortis-derm-chong-ngua-250ml.jpg', 120000, 0,
       'Sữa Tắm Cho Chó Bossen Fortis Derm Chống Ngứa 250ml Thương hiệu: Bossen Phù hợp cho: Chó từ 3 tháng tuổi trở lên Sữa tắm cho chó Fortis Derm Anti-Itch là giải pháp chăm sóc da và lông dành cho thú cưng, đặc biệt là những bé hay gặp tình trạng ngứa ngáy, kíc...

Thương hiệu: Bossen.

Nguồn tham khảo: https://paddy.vn/products/sua-tam-cho-cho-bossen-fortis-derm-chong-ngua-250ml', 65, 300, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_071_sua-tam-cho-cho-bossen-fortis-derm-chong-ngua-250ml.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/sua-tam-cho-cho-bossen-fortis-derm-chong-ngua-250ml.jpg?v=1756353500'));

UPDATE products
SET name = 'Cát Đậu Nành Cho Mèo ON25 Mixed Tofu',
    image = 'products/paddy_072_cat-dau-nanh-cho-meo-on25-mixed-tofu.jpg',
    price = 99000,
    discount = 0,
    description = 'Cát Đậu Nành Cho Mèo ON25 Mixed Tofu Thương hiệu: Cat''s On Phù hợp cho: Mèo mọi lứa tuổi Cát Vệ Sinh Cho Mèo ON25 Tofu được làm từ đậu nành tự nhiên, an toàn và lành tính, với công thức 5 trong 1: khử mùi hiệu quả, vón cục nhanh, không bụi, an toàn sinh học...

Thương hiệu: Cat''s On.

Nguồn tham khảo: https://paddy.vn/products/cat-dau-nanh-cho-meo-on25-mixed-tofu',
    stock = 66,
    weight = 2700,
    category = 'Vệ Sinh Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_072_cat-dau-nanh-cho-meo-on25-mixed-tofu.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/cat-dau-nanh-cho-meo-on25-mixed-tofu_4.jpg?v=1756198865');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Cát Đậu Nành Cho Mèo ON25 Mixed Tofu', 'products/paddy_072_cat-dau-nanh-cho-meo-on25-mixed-tofu.jpg', 99000, 0,
       'Cát Đậu Nành Cho Mèo ON25 Mixed Tofu Thương hiệu: Cat''s On Phù hợp cho: Mèo mọi lứa tuổi Cát Vệ Sinh Cho Mèo ON25 Tofu được làm từ đậu nành tự nhiên, an toàn và lành tính, với công thức 5 trong 1: khử mùi hiệu quả, vón cục nhanh, không bụi, an toàn sinh học...

Thương hiệu: Cat''s On.

Nguồn tham khảo: https://paddy.vn/products/cat-dau-nanh-cho-meo-on25-mixed-tofu', 66, 2700, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_072_cat-dau-nanh-cho-meo-on25-mixed-tofu.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/cat-dau-nanh-cho-meo-on25-mixed-tofu_4.jpg?v=1756198865'));

UPDATE products
SET name = 'Hạt Cho Chó Mr Vet  D1 Chăm Sóc Hệ Tiêu Hoá',
    image = 'products/paddy_073_hat-cho-cho-mr-vet-d1-cham-soc-he-tieu-hoa.jpg',
    price = 130000,
    discount = 0,
    description = 'Hạt Cho Chó Mr Vet D1 Chăm Sóc Hệ Tiêu Hoá Thương hiệu: Mr Vet Phù hợp cho: Chó mọi lứa tuổi Thức ăn hạt cho chó Mr.vet D1 là dòng hạt Holistic cao cấp được làm từ thịt cừu non kết hợp rau củ quả và vitamin thiết yếu, mang đến nguồn dinh dưỡng cân bằng và d...

Thương hiệu: Mr Vet.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-mr-vet-d1-cham-soc-he-tieu-hoa',
    stock = 67,
    weight = 1500,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_073_hat-cho-cho-mr-vet-d1-cham-soc-he-tieu-hoa.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-cho-mr-vet-d1-cham-soc-he-tieu-hoa.jpg?v=1756184984');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Chó Mr Vet  D1 Chăm Sóc Hệ Tiêu Hoá', 'products/paddy_073_hat-cho-cho-mr-vet-d1-cham-soc-he-tieu-hoa.jpg', 130000, 0,
       'Hạt Cho Chó Mr Vet D1 Chăm Sóc Hệ Tiêu Hoá Thương hiệu: Mr Vet Phù hợp cho: Chó mọi lứa tuổi Thức ăn hạt cho chó Mr.vet D1 là dòng hạt Holistic cao cấp được làm từ thịt cừu non kết hợp rau củ quả và vitamin thiết yếu, mang đến nguồn dinh dưỡng cân bằng và d...

Thương hiệu: Mr Vet.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-mr-vet-d1-cham-soc-he-tieu-hoa', 67, 1500, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_073_hat-cho-cho-mr-vet-d1-cham-soc-he-tieu-hoa.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-cho-mr-vet-d1-cham-soc-he-tieu-hoa.jpg?v=1756184984'));

UPDATE products
SET name = 'Hạt Cho Chó Giống Nhỏ Dị Ứng Royal Canin Hypoallergenic Small',
    image = 'products/paddy_074_hat-cho-cho-giong-nho-di-ung-royal-canin-hypoallergenic-small.jpg',
    price = 399000,
    discount = 0,
    description = 'Hạt Cho Chó Giống Nhỏ Dị Ứng Royal Canin Hypoallergenic Small Thương hiệu: Royal Canin Phù hợp cho: Chó trưởng thành giống nhỏ (&lt;10kg) ROYAL CANIN Hypoallergenic Small Dog là thức ăn hạt cho chó trưởng thành giống nhỏ, được thiết kế đặc biệt để hỗ trợ ch...

Thương hiệu: Royal Canin.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-giong-nho-di-ung-royal-canin-hypoallergenic-small',
    stock = 8,
    weight = 1200,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_074_hat-cho-cho-giong-nho-di-ung-royal-canin-hypoallergenic-small.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hatchochogiongnhodiung_2.jpg?v=1759741127');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Chó Giống Nhỏ Dị Ứng Royal Canin Hypoallergenic Small', 'products/paddy_074_hat-cho-cho-giong-nho-di-ung-royal-canin-hypoallergenic-small.jpg', 399000, 0,
       'Hạt Cho Chó Giống Nhỏ Dị Ứng Royal Canin Hypoallergenic Small Thương hiệu: Royal Canin Phù hợp cho: Chó trưởng thành giống nhỏ (&lt;10kg) ROYAL CANIN Hypoallergenic Small Dog là thức ăn hạt cho chó trưởng thành giống nhỏ, được thiết kế đặc biệt để hỗ trợ ch...

Thương hiệu: Royal Canin.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-giong-nho-di-ung-royal-canin-hypoallergenic-small', 8, 1200, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_074_hat-cho-cho-giong-nho-di-ung-royal-canin-hypoallergenic-small.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hatchochogiongnhodiung_2.jpg?v=1759741127'));

UPDATE products
SET name = 'Pate Cho Mèo Lapaw Dạng Thạch 70g',
    image = 'products/paddy_075_pate-cho-meo-lapaw-dang-thach-70g.jpg',
    price = 11000,
    discount = 0,
    description = 'Pate Cho Mèo Lapaw Dạng Thạch 70g Thương hiệu: LaPaw Phù hợp cho: Mèo mọi lứa tuổi Pate cho mèo LaPaw 70g là thức ăn ướt dạng thạch giàu protein tự nhiên dễ hấp thu, giúp mèo khỏe mạnh, tăng đề kháng và hỗ trợ hệ tiêu hóa. Với kết cấu mềm mịn, hương vị đa d...

Thương hiệu: LaPaw.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-lapaw-dang-thach-70g',
    stock = 69,
    weight = 100,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_075_pate-cho-meo-lapaw-dang-thach-70g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pate-cho-meo-lapaw-dang-thach-70g_5.jpg?v=1755588490');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Mèo Lapaw Dạng Thạch 70g', 'products/paddy_075_pate-cho-meo-lapaw-dang-thach-70g.jpg', 11000, 0,
       'Pate Cho Mèo Lapaw Dạng Thạch 70g Thương hiệu: LaPaw Phù hợp cho: Mèo mọi lứa tuổi Pate cho mèo LaPaw 70g là thức ăn ướt dạng thạch giàu protein tự nhiên dễ hấp thu, giúp mèo khỏe mạnh, tăng đề kháng và hỗ trợ hệ tiêu hóa. Với kết cấu mềm mịn, hương vị đa d...

Thương hiệu: LaPaw.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-lapaw-dang-thach-70g', 69, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_075_pate-cho-meo-lapaw-dang-thach-70g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pate-cho-meo-lapaw-dang-thach-70g_5.jpg?v=1755588490'));

UPDATE products
SET name = 'Hạt Cho Mèo Mọi Lứa Tuổi On25 Cat Đạm Cao 32%',
    image = 'products/paddy_076_hat-cho-meo-on25-cat-dam-cao-32-danh-cho-meo-moi-do-tuoi.jpg',
    price = 250000,
    discount = 0,
    description = 'Hạt Cho Mèo Mọi Lứa Tuổi On25 Cat Đạm Cao 32% Thương hiệu: Cat''s On Phù hợp cho: Mèo mọi lứa tuổi Thức ăn hạt cho mèo On25 Cat được nghiên cứu bởi các chuyên gia dinh dưỡng thú cưng nhằm mang đến giải pháp chăm sóc tối ưu cho mèo. Nhờ thành phần chọn lọc từ...

Thương hiệu: Cat''s On.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-on25-cat-dam-cao-32-danh-cho-meo-moi-do-tuoi',
    stock = 70,
    weight = 3200,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_076_hat-cho-meo-on25-cat-dam-cao-32-danh-cho-meo-moi-do-tuoi.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-meo-on25-cat-dam-cao-32-danh-cho-meo-moi-do-tuoi.jpg?v=1755589489');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo Mọi Lứa Tuổi On25 Cat Đạm Cao 32%', 'products/paddy_076_hat-cho-meo-on25-cat-dam-cao-32-danh-cho-meo-moi-do-tuoi.jpg', 250000, 0,
       'Hạt Cho Mèo Mọi Lứa Tuổi On25 Cat Đạm Cao 32% Thương hiệu: Cat''s On Phù hợp cho: Mèo mọi lứa tuổi Thức ăn hạt cho mèo On25 Cat được nghiên cứu bởi các chuyên gia dinh dưỡng thú cưng nhằm mang đến giải pháp chăm sóc tối ưu cho mèo. Nhờ thành phần chọn lọc từ...

Thương hiệu: Cat''s On.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-on25-cat-dam-cao-32-danh-cho-meo-moi-do-tuoi', 70, 3200, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_076_hat-cho-meo-on25-cat-dam-cao-32-danh-cho-meo-moi-do-tuoi.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/hat-cho-meo-on25-cat-dam-cao-32-danh-cho-meo-moi-do-tuoi.jpg?v=1755589489'));

UPDATE products
SET name = 'Bánh Thưởng Cho Chó Lamer Multi Chew Hỗ Trợ Xương Khớp & Răng Miệng',
    image = 'products/paddy_077_snack-cho-cho-lamer-multi-chew-ho-tro-xuong-khop-rang-mieng.jpg',
    price = 60000,
    discount = 0,
    description = 'Bánh Thưởng Cho Chó Lamer Multi Chew Hỗ Trợ Xương Khớp & Răng Miệng Thương hiệu: Lamer Phù hợp cho: Chó từ 3 tháng tuổi trở lên Snack cho chó nhỗ trợ chăm sóc răng miệng và sức khỏe xương khớp. Với công thức chứa các dưỡng chất hỗ trợ sụn khớp, giúp giảm vi...

Thương hiệu: Lamer.

Nguồn tham khảo: https://paddy.vn/products/snack-cho-cho-lamer-multi-chew-ho-tro-xuong-khop-rang-mieng',
    stock = 71,
    weight = 150,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_077_snack-cho-cho-lamer-multi-chew-ho-tro-xuong-khop-rang-mieng.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/snack-cho-cho-lamer-multi-chew-ho-tro-xuong-khop-rang-mieng.jpg?v=1754539347');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Cho Chó Lamer Multi Chew Hỗ Trợ Xương Khớp & Răng Miệng', 'products/paddy_077_snack-cho-cho-lamer-multi-chew-ho-tro-xuong-khop-rang-mieng.jpg', 60000, 0,
       'Bánh Thưởng Cho Chó Lamer Multi Chew Hỗ Trợ Xương Khớp & Răng Miệng Thương hiệu: Lamer Phù hợp cho: Chó từ 3 tháng tuổi trở lên Snack cho chó nhỗ trợ chăm sóc răng miệng và sức khỏe xương khớp. Với công thức chứa các dưỡng chất hỗ trợ sụn khớp, giúp giảm vi...

Thương hiệu: Lamer.

Nguồn tham khảo: https://paddy.vn/products/snack-cho-cho-lamer-multi-chew-ho-tro-xuong-khop-rang-mieng', 71, 150, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_077_snack-cho-cho-lamer-multi-chew-ho-tro-xuong-khop-rang-mieng.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/snack-cho-cho-lamer-multi-chew-ho-tro-xuong-khop-rang-mieng.jpg?v=1754539347'));

UPDATE products
SET name = 'Dầu Xả Dưỡng Lông Cho Chó Mèo Tropiclean Spa Nourish',
    image = 'products/paddy_078_dau-xa-duong-long-cho-cho-meo-tropiclean-spa-nourish.jpg',
    price = 250000,
    discount = 0,
    description = 'Dầu Xả Dưỡng Lông Cho Chó Mèo Tropiclean Spa Nourish Thương hiệu: Tropiclean Phù hợp cho: Chó/Mèo mọi lứa tuổi Dầu xả Tropiclean là lựa chọn hoàn hảo để chăm sóc vệ sinh chó mèo tại nhà. Với công thức dưỡng ẩm tự nhiên, sản phẩm giúp phục hồi lông mềm mượt,...

Thương hiệu: Tropiclean.

Nguồn tham khảo: https://paddy.vn/products/dau-xa-duong-long-cho-cho-meo-tropiclean-spa-nourish',
    stock = 72,
    weight = 500,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_078_dau-xa-duong-long-cho-cho-meo-tropiclean-spa-nourish.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/dau-xa-duong-long-cho-cho-meo-tropiclean-spa-nourish_3.jpg?v=1753775833');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Dầu Xả Dưỡng Lông Cho Chó Mèo Tropiclean Spa Nourish', 'products/paddy_078_dau-xa-duong-long-cho-cho-meo-tropiclean-spa-nourish.jpg', 250000, 0,
       'Dầu Xả Dưỡng Lông Cho Chó Mèo Tropiclean Spa Nourish Thương hiệu: Tropiclean Phù hợp cho: Chó/Mèo mọi lứa tuổi Dầu xả Tropiclean là lựa chọn hoàn hảo để chăm sóc vệ sinh chó mèo tại nhà. Với công thức dưỡng ẩm tự nhiên, sản phẩm giúp phục hồi lông mềm mượt,...

Thương hiệu: Tropiclean.

Nguồn tham khảo: https://paddy.vn/products/dau-xa-duong-long-cho-cho-meo-tropiclean-spa-nourish', 72, 500, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_078_dau-xa-duong-long-cho-cho-meo-tropiclean-spa-nourish.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/dau-xa-duong-long-cho-cho-meo-tropiclean-spa-nourish_3.jpg?v=1753775833'));

UPDATE products
SET name = 'Kem Đánh Răng Cho Chó Mèo Vị Gà Jungle Monster',
    image = 'products/paddy_079_kem-danh-rang-cho-cho-meo-vi-ga-jungle-monster.webp',
    price = 380000,
    discount = 0,
    description = 'Kem Đánh Răng Cho Chó Mèo Vị Gà Jungle Monster Thương hiệu: Jungle Monster Phù hợp cho: Chó/Mèo từ 6 tháng tuổi trở lên Sản phẩm chăm sóc răng miệng cho chó mèo , giúp khử mùi hôi miệng, kiểm soát mảng bám và cao răng với hương vị gà thơm ngon dễ chịu. Thàn...

Thương hiệu: Jungle Monster.

Nguồn tham khảo: https://paddy.vn/products/kem-danh-rang-cho-cho-meo-vi-ga-jungle-monster',
    stock = 73,
    weight = 150,
    category = 'Vệ Sinh Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_079_kem-danh-rang-cho-cho-meo-vi-ga-jungle-monster.webp', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/kem-danh-rang-cho-cho-meo-vi-ga-jungle-monster_2.webp?v=1753763785');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Kem Đánh Răng Cho Chó Mèo Vị Gà Jungle Monster', 'products/paddy_079_kem-danh-rang-cho-cho-meo-vi-ga-jungle-monster.webp', 380000, 0,
       'Kem Đánh Răng Cho Chó Mèo Vị Gà Jungle Monster Thương hiệu: Jungle Monster Phù hợp cho: Chó/Mèo từ 6 tháng tuổi trở lên Sản phẩm chăm sóc răng miệng cho chó mèo , giúp khử mùi hôi miệng, kiểm soát mảng bám và cao răng với hương vị gà thơm ngon dễ chịu. Thàn...

Thương hiệu: Jungle Monster.

Nguồn tham khảo: https://paddy.vn/products/kem-danh-rang-cho-cho-meo-vi-ga-jungle-monster', 73, 150, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_079_kem-danh-rang-cho-cho-meo-vi-ga-jungle-monster.webp', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/kem-danh-rang-cho-cho-meo-vi-ga-jungle-monster_2.webp?v=1753763785'));

UPDATE products
SET name = 'Xịt Thơm Mềm Lông Cho Chó Jungle Monster Pure Soap Silky Mist 150ml',
    image = 'products/paddy_080_xit-thom-mem-long-cho-cho-jungle-monster-pure-soap-silky-mist-150ml.webp',
    price = 380000,
    discount = 0,
    description = 'Xịt Thơm Mềm Lông Cho Chó Jungle Monster Pure Soap Silky Mist 150ml Thương hiệu: Jungle Monster Phù hợp cho: Chó từ 3 tháng tuổi trở lên Xịt Thơm Mềm Lông Cho Chó là sản phẩm giúp xịt khử mùi và dưỡng ẩm sâu, làm mềm mượt và giảm rối lông hiệu quả. Với hươn...

Thương hiệu: Jungle Monster.

Nguồn tham khảo: https://paddy.vn/products/xit-thom-mem-long-cho-cho-jungle-monster-pure-soap-silky-mist-150ml',
    stock = 74,
    weight = 250,
    category = 'Vệ Sinh Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_080_xit-thom-mem-long-cho-cho-jungle-monster-pure-soap-silky-mist-150ml.webp', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/xit-thom-mem-long-cho-cho-jungle-monster-pure-soap-silky-mist-150ml.webp?v=1753759736');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Xịt Thơm Mềm Lông Cho Chó Jungle Monster Pure Soap Silky Mist 150ml', 'products/paddy_080_xit-thom-mem-long-cho-cho-jungle-monster-pure-soap-silky-mist-150ml.webp', 380000, 0,
       'Xịt Thơm Mềm Lông Cho Chó Jungle Monster Pure Soap Silky Mist 150ml Thương hiệu: Jungle Monster Phù hợp cho: Chó từ 3 tháng tuổi trở lên Xịt Thơm Mềm Lông Cho Chó là sản phẩm giúp xịt khử mùi và dưỡng ẩm sâu, làm mềm mượt và giảm rối lông hiệu quả. Với hươn...

Thương hiệu: Jungle Monster.

Nguồn tham khảo: https://paddy.vn/products/xit-thom-mem-long-cho-cho-jungle-monster-pure-soap-silky-mist-150ml', 74, 250, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_080_xit-thom-mem-long-cho-cho-jungle-monster-pure-soap-silky-mist-150ml.webp', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/xit-thom-mem-long-cho-cho-jungle-monster-pure-soap-silky-mist-150ml.webp?v=1753759736'));

UPDATE products
SET name = 'Sữa Tắm & Xả Cho Chó Mèo Tropiclean 2 Trong 1 Hương Đu Đủ & Dừa 355ml',
    image = 'products/paddy_081_sua-tam-xa-cho-cho-meo-tropiclean-2-trong-1-huong-du-du-dua-355ml.jpg',
    price = 250000,
    discount = 0,
    description = 'Sữa Tắm & Xả Cho Chó Mèo Tropiclean 2 Trong 1 Hương Đu Đủ & Dừa 355ml Thương hiệu: Tropiclean Phù hợp cho: Chó/Mèo mọi lứa tuổi Chăm sóc vệ sinh chó mèo TropiClean Luxury 2 trong 1 là dòng sữa tắm xả hữu cơ dành cho chó mèo bán chạy số 1 tại Mỹ, giúp cung c...

Thương hiệu: Tropiclean.

Nguồn tham khảo: https://paddy.vn/products/sua-tam-xa-cho-cho-meo-tropiclean-2-trong-1-huong-du-du-dua-355ml',
    stock = 75,
    weight = 800,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_081_sua-tam-xa-cho-cho-meo-tropiclean-2-trong-1-huong-du-du-dua-355ml.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/sua-tam-xa-tropiclean-2-trong-1-huong-du-du-dua-355ml_3.jpg?v=1753263853');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Sữa Tắm & Xả Cho Chó Mèo Tropiclean 2 Trong 1 Hương Đu Đủ & Dừa 355ml', 'products/paddy_081_sua-tam-xa-cho-cho-meo-tropiclean-2-trong-1-huong-du-du-dua-355ml.jpg', 250000, 0,
       'Sữa Tắm & Xả Cho Chó Mèo Tropiclean 2 Trong 1 Hương Đu Đủ & Dừa 355ml Thương hiệu: Tropiclean Phù hợp cho: Chó/Mèo mọi lứa tuổi Chăm sóc vệ sinh chó mèo TropiClean Luxury 2 trong 1 là dòng sữa tắm xả hữu cơ dành cho chó mèo bán chạy số 1 tại Mỹ, giúp cung c...

Thương hiệu: Tropiclean.

Nguồn tham khảo: https://paddy.vn/products/sua-tam-xa-cho-cho-meo-tropiclean-2-trong-1-huong-du-du-dua-355ml', 75, 800, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_081_sua-tam-xa-cho-cho-meo-tropiclean-2-trong-1-huong-du-du-dua-355ml.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/sua-tam-xa-tropiclean-2-trong-1-huong-du-du-dua-355ml_3.jpg?v=1753263853'));

UPDATE products
SET name = 'Sữa Tắm Cho Chó Tropiclean PerfectFur Chăm Sóc Lông Chuyên Biệt 473ml',
    image = 'products/paddy_082_sua-tam-cho-cho-tropiclean-perfectfur-cham-soc-long-chuyen-biet-473ml.jpg',
    price = 370000,
    discount = 0,
    description = 'Sữa Tắm Cho Chó Tropiclean PerfectFur Chăm Sóc Lông Chuyên Biệt 473ml Thương hiệu: Tropiclean Phù hợp cho: Chó (từ 12 tuần tuổi trở lên) Sữa tắm cho chó là dòng sản phẩm cao cấp được thiết kế chuyên biệt cho từng loại lông chó: từ lông ngắn, lông dài, lông...

Thương hiệu: Tropiclean.

Nguồn tham khảo: https://paddy.vn/products/sua-tam-cho-cho-tropiclean-perfectfur-cham-soc-long-chuyen-biet-473ml',
    stock = 76,
    weight = 700,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_082_sua-tam-cho-cho-tropiclean-perfectfur-cham-soc-long-chuyen-biet-473ml.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/sua-tam-cho-cho-tropiclean-perfectfur-cham-soc-long-chuyen-biet-473ml_6.jpg?v=1759742125');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Sữa Tắm Cho Chó Tropiclean PerfectFur Chăm Sóc Lông Chuyên Biệt 473ml', 'products/paddy_082_sua-tam-cho-cho-tropiclean-perfectfur-cham-soc-long-chuyen-biet-473ml.jpg', 370000, 0,
       'Sữa Tắm Cho Chó Tropiclean PerfectFur Chăm Sóc Lông Chuyên Biệt 473ml Thương hiệu: Tropiclean Phù hợp cho: Chó (từ 12 tuần tuổi trở lên) Sữa tắm cho chó là dòng sản phẩm cao cấp được thiết kế chuyên biệt cho từng loại lông chó: từ lông ngắn, lông dài, lông...

Thương hiệu: Tropiclean.

Nguồn tham khảo: https://paddy.vn/products/sua-tam-cho-cho-tropiclean-perfectfur-cham-soc-long-chuyen-biet-473ml', 76, 700, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_082_sua-tam-cho-cho-tropiclean-perfectfur-cham-soc-long-chuyen-biet-473ml.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/sua-tam-cho-cho-tropiclean-perfectfur-cham-soc-long-chuyen-biet-473ml_6.jpg?v=1759742125'));

UPDATE products
SET name = 'Tã Lót Cho Chó CÁI FOFOS Diapers',
    image = 'products/paddy_083_ta-lot-cho-cho-cai-fofos-diapers.jpg',
    price = 90000,
    discount = 0,
    description = 'Tã Lót Cho Chó CÁI FOFOS Diapers Thương hiệu: FOFOS Phù hợp cho: Chó mọi lứa tuổi Không còn những khoảnh khắc khó xử khi chó cưng đi vệ sinh nơi công cộng – Tã lót cho chó FOFOS là người bạn đồng hành lý tưởng cho mọi hoạt động ngoài trời cùng thú cưng. Ứng...

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/ta-lot-cho-cho-cai-fofos-diapers',
    stock = 77,
    weight = 1500,
    category = 'Vệ Sinh Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_083_ta-lot-cho-cho-cai-fofos-diapers.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/ta-lot-cho-cho-fofos-diapers_9.jpg?v=1753169522');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Tã Lót Cho Chó CÁI FOFOS Diapers', 'products/paddy_083_ta-lot-cho-cho-cai-fofos-diapers.jpg', 90000, 0,
       'Tã Lót Cho Chó CÁI FOFOS Diapers Thương hiệu: FOFOS Phù hợp cho: Chó mọi lứa tuổi Không còn những khoảnh khắc khó xử khi chó cưng đi vệ sinh nơi công cộng – Tã lót cho chó FOFOS là người bạn đồng hành lý tưởng cho mọi hoạt động ngoài trời cùng thú cưng. Ứng...

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/ta-lot-cho-cho-cai-fofos-diapers', 77, 1500, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_083_ta-lot-cho-cho-cai-fofos-diapers.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/ta-lot-cho-cho-fofos-diapers_9.jpg?v=1753169522'));

UPDATE products
SET name = 'Pate Mèo Triệt Sản Royal Canin Indoor Sterilised 85g',
    image = 'products/paddy_084_pate-meo-triet-san-royal-canin-indoor-sterilised-85g.jpg',
    price = 35000,
    discount = 0,
    description = 'Pate Mèo Triệt Sản Royal Canin Indoor Sterilised 85g Thương hiệu: Royal Canin Phù hợp cho: Mèo trưởng thành đã triệt sản (trên 10 tháng tuổi) Pate mèo Royal Canin Indoor Sterilised là công thức được sáng tạo đặc biệt dành cho mèo nhà đã triệt sản. Sản phẩm...

Thương hiệu: Royal Canin.

Nguồn tham khảo: https://paddy.vn/products/pate-meo-triet-san-royal-canin-indoor-sterilised-85g',
    stock = 8,
    weight = 100,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_084_pate-meo-triet-san-royal-canin-indoor-sterilised-85g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pate-meo-triet-san-royal-canin-indoor-sterilised-85g.jpg?v=1755157086');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Mèo Triệt Sản Royal Canin Indoor Sterilised 85g', 'products/paddy_084_pate-meo-triet-san-royal-canin-indoor-sterilised-85g.jpg', 35000, 0,
       'Pate Mèo Triệt Sản Royal Canin Indoor Sterilised 85g Thương hiệu: Royal Canin Phù hợp cho: Mèo trưởng thành đã triệt sản (trên 10 tháng tuổi) Pate mèo Royal Canin Indoor Sterilised là công thức được sáng tạo đặc biệt dành cho mèo nhà đã triệt sản. Sản phẩm...

Thương hiệu: Royal Canin.

Nguồn tham khảo: https://paddy.vn/products/pate-meo-triet-san-royal-canin-indoor-sterilised-85g', 8, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_084_pate-meo-triet-san-royal-canin-indoor-sterilised-85g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pate-meo-triet-san-royal-canin-indoor-sterilised-85g.jpg?v=1755157086'));

UPDATE products
SET name = 'Bát Ăn Cho Chó Mèo Đế Cao Richell',
    image = 'products/paddy_085_bat-an-cho-cho-meo-cao-richell.jpg',
    price = 85000,
    discount = 0,
    description = 'Bát Ăn Cho Chó Mèo Đế Cao Richell Thương hiệu: Richell Phù hợp cho: Chó nhỏ (Từ 4kg-10kg)/ Mèo con Bát đựng thức ăn richell là dụng cụ ăn uống mang đến cho chó mèo sự thoải mái tối đa trong giờ ăn. Thiết kế nâng cao tiện dụng mang đến chiều cao lý tưởng cho...

Thương hiệu: Richell.

Nguồn tham khảo: https://paddy.vn/products/bat-an-cho-cho-meo-cao-richell',
    stock = 8,
    weight = 400,
    category = 'Phụ Kiện Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_085_bat-an-cho-cho-meo-cao-richell.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/bat-dung-thuc-an-cho-cho-meo-cao-richell_3.jpg?v=1754124739');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bát Ăn Cho Chó Mèo Đế Cao Richell', 'products/paddy_085_bat-an-cho-cho-meo-cao-richell.jpg', 85000, 0,
       'Bát Ăn Cho Chó Mèo Đế Cao Richell Thương hiệu: Richell Phù hợp cho: Chó nhỏ (Từ 4kg-10kg)/ Mèo con Bát đựng thức ăn richell là dụng cụ ăn uống mang đến cho chó mèo sự thoải mái tối đa trong giờ ăn. Thiết kế nâng cao tiện dụng mang đến chiều cao lý tưởng cho...

Thương hiệu: Richell.

Nguồn tham khảo: https://paddy.vn/products/bat-an-cho-cho-meo-cao-richell', 8, 400, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_085_bat-an-cho-cho-meo-cao-richell.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/bat-dung-thuc-an-cho-cho-meo-cao-richell_3.jpg?v=1754124739'));

UPDATE products
SET name = 'Bánh Thưởng Cho Chó Thịt Cuộn Cá Gooday 80g',
    image = 'products/paddy_086_banh-thuong-cho-cho-thit-cuon-ca-gooday-80g.webp',
    price = 100000,
    discount = 0,
    description = 'Bánh Thưởng Cho Chó Thịt Cuộn Cá Gooday 80g Thương hiệu: Gooday Phù hợp cho: Chó trên 12 tháng tuổi Bánh thưởng cho chó cuộn cá minh thái là món ăn vặt dinh dưỡng dành cho thú cưng, được chế biến từ các nguyên liệu tự nhiên như thịt gà, thịt vịt và cá minh...

Thương hiệu: Gooday.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-cho-thit-cuon-ca-gooday-80g',
    stock = 8,
    weight = 100,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_086_banh-thuong-cho-cho-thit-cuon-ca-gooday-80g.webp', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/banh-thuong-cho-cho-thit-cuon-ca-gooday-80g_4.webp?v=1754124724');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Cho Chó Thịt Cuộn Cá Gooday 80g', 'products/paddy_086_banh-thuong-cho-cho-thit-cuon-ca-gooday-80g.webp', 100000, 0,
       'Bánh Thưởng Cho Chó Thịt Cuộn Cá Gooday 80g Thương hiệu: Gooday Phù hợp cho: Chó trên 12 tháng tuổi Bánh thưởng cho chó cuộn cá minh thái là món ăn vặt dinh dưỡng dành cho thú cưng, được chế biến từ các nguyên liệu tự nhiên như thịt gà, thịt vịt và cá minh...

Thương hiệu: Gooday.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-cho-thit-cuon-ca-gooday-80g', 8, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_086_banh-thuong-cho-cho-thit-cuon-ca-gooday-80g.webp', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/banh-thuong-cho-cho-thit-cuon-ca-gooday-80g_4.webp?v=1754124724'));

UPDATE products
SET name = 'Đồ Chơi Cho Chó Thú Bông FOFOS Summer',
    image = 'products/paddy_087_do-choi-cho-cho-thu-bong-fofos-summer.webp',
    price = 105000,
    discount = 0,
    description = 'Đồ Chơi Cho Chó Thú Bông FOFOS Summer Thương hiệu: FOFOS Phù hợp cho: Chó mọi lứa tuổi FOFOS là thương hiệu đồ chơi cho chó nổi tiếng. Các sản phẩm của FOFOS được thiết kế dựa trên nhu cầu và sở thích của chó, với mục đích giúp chó vui chơi, giải trí và phá...

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/do-choi-cho-cho-thu-bong-fofos-summer',
    stock = 81,
    weight = 300,
    category = 'Đồ Chơi Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_087_do-choi-cho-cho-thu-bong-fofos-summer.webp', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/do-choi-cho-cho-thu-bong-fofos-summer_3.webp?v=1754124703');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Đồ Chơi Cho Chó Thú Bông FOFOS Summer', 'products/paddy_087_do-choi-cho-cho-thu-bong-fofos-summer.webp', 105000, 0,
       'Đồ Chơi Cho Chó Thú Bông FOFOS Summer Thương hiệu: FOFOS Phù hợp cho: Chó mọi lứa tuổi FOFOS là thương hiệu đồ chơi cho chó nổi tiếng. Các sản phẩm của FOFOS được thiết kế dựa trên nhu cầu và sở thích của chó, với mục đích giúp chó vui chơi, giải trí và phá...

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/do-choi-cho-cho-thu-bong-fofos-summer', 81, 300, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_087_do-choi-cho-cho-thu-bong-fofos-summer.webp', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/do-choi-cho-cho-thu-bong-fofos-summer_3.webp?v=1754124703'));

UPDATE products
SET name = 'Đồ Chơi Cho Chó Thú Bông FOFOS Tua Rua',
    image = 'products/paddy_088_do-choi-cho-cho-thu-bong-fofos-tua-rua.webp',
    price = 88000,
    discount = 0,
    description = 'Đồ Chơi Cho Chó Thú Bông FOFOS Tua Rua Thương hiệu: FOFOS Phù hợp cho: Chó mọi lứa tuổi FOFOS là thương hiệu đồ chơi cho chó nổi tiếng. Các sản phẩm của FOFOS được thiết kế dựa trên nhu cầu và sở thích của chó, với mục đích giúp chó vui chơi, giải trí và ph...

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/do-choi-cho-cho-thu-bong-fofos-tua-rua',
    stock = 82,
    weight = 300,
    category = 'Đồ Chơi Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_088_do-choi-cho-cho-thu-bong-fofos-tua-rua.webp', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/do-choi-cho-cho-thu-bong-fofos-tua-rua_3.webp?v=1754124689');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Đồ Chơi Cho Chó Thú Bông FOFOS Tua Rua', 'products/paddy_088_do-choi-cho-cho-thu-bong-fofos-tua-rua.webp', 88000, 0,
       'Đồ Chơi Cho Chó Thú Bông FOFOS Tua Rua Thương hiệu: FOFOS Phù hợp cho: Chó mọi lứa tuổi FOFOS là thương hiệu đồ chơi cho chó nổi tiếng. Các sản phẩm của FOFOS được thiết kế dựa trên nhu cầu và sở thích của chó, với mục đích giúp chó vui chơi, giải trí và ph...

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/do-choi-cho-cho-thu-bong-fofos-tua-rua', 82, 300, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_088_do-choi-cho-cho-thu-bong-fofos-tua-rua.webp', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/do-choi-cho-cho-thu-bong-fofos-tua-rua_3.webp?v=1754124689'));

UPDATE products
SET name = 'Pate Chó Hỗ Trợ Tiêu Hóa Royal Canin Gastrointestinal Canine (Lon 400g)',
    image = 'products/paddy_089_pate-cho-ho-tro-tieu-hoa-royal-canin-gastrointestinal-canine-lon-400g.jpg',
    price = 172000,
    discount = 0,
    description = 'Pate Chó Hỗ Trợ Tiêu Hóa Royal Canin Gastrointestinal Canine (Lon 400g) Thương hiệu: Royal canin Phù hợp cho: Chó gặp vấn đề về tiêu hoá Pate cho chó Royal canin Gastrointestinal là dòng pate dinh dưỡng hoàn chỉnh dành cho chó, được thiết kế đặc biệt để hỗ...

Thương hiệu: Royal Canin.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-ho-tro-tieu-hoa-royal-canin-gastrointestinal-canine-lon-400g',
    stock = 8,
    weight = 500,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_089_pate-cho-ho-tro-tieu-hoa-royal-canin-gastrointestinal-canine-lon-400g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pate_ho_tro_tieu_hoa_cho_cho.jpg?v=1755157651');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Chó Hỗ Trợ Tiêu Hóa Royal Canin Gastrointestinal Canine (Lon 400g)', 'products/paddy_089_pate-cho-ho-tro-tieu-hoa-royal-canin-gastrointestinal-canine-lon-400g.jpg', 172000, 0,
       'Pate Chó Hỗ Trợ Tiêu Hóa Royal Canin Gastrointestinal Canine (Lon 400g) Thương hiệu: Royal canin Phù hợp cho: Chó gặp vấn đề về tiêu hoá Pate cho chó Royal canin Gastrointestinal là dòng pate dinh dưỡng hoàn chỉnh dành cho chó, được thiết kế đặc biệt để hỗ...

Thương hiệu: Royal Canin.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-ho-tro-tieu-hoa-royal-canin-gastrointestinal-canine-lon-400g', 8, 500, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_089_pate-cho-ho-tro-tieu-hoa-royal-canin-gastrointestinal-canine-lon-400g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pate_ho_tro_tieu_hoa_cho_cho.jpg?v=1755157651'));

UPDATE products
SET name = 'Đồ Chơi Cho Chó Mèo Lồng Quay Hạt Thông Minh Doggyman',
    image = 'products/paddy_090_long-quay-hat-thong-minh-cho-cho-meo-doggyman.webp',
    price = 270000,
    discount = 0,
    description = 'Đồ Chơi Cho Chó Mèo Lồng Quay Hạt Thông Minh Doggyman Thương hiệu: DoggyMan Phù hợp cho: Chó/Mèo mọi lứa tuổi Đồ chơi cho chó mèo lồng quay thúc đẩy phát triển não bộ, tạo thói quen tư duy cho thú cưng. Đặt thức ăn hạt bên trong lồng, kích thích ham muốn th...

Thương hiệu: DoggyMan.

Nguồn tham khảo: https://paddy.vn/products/long-quay-hat-thong-minh-cho-cho-meo-doggyman',
    stock = 84,
    weight = 700,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_090_long-quay-hat-thong-minh-cho-cho-meo-doggyman.webp', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/long-quay-hat-thong-minh-cho-cho-meo-doggyman_2.webp?v=1752462779');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Đồ Chơi Cho Chó Mèo Lồng Quay Hạt Thông Minh Doggyman', 'products/paddy_090_long-quay-hat-thong-minh-cho-cho-meo-doggyman.webp', 270000, 0,
       'Đồ Chơi Cho Chó Mèo Lồng Quay Hạt Thông Minh Doggyman Thương hiệu: DoggyMan Phù hợp cho: Chó/Mèo mọi lứa tuổi Đồ chơi cho chó mèo lồng quay thúc đẩy phát triển não bộ, tạo thói quen tư duy cho thú cưng. Đặt thức ăn hạt bên trong lồng, kích thích ham muốn th...

Thương hiệu: DoggyMan.

Nguồn tham khảo: https://paddy.vn/products/long-quay-hat-thong-minh-cho-cho-meo-doggyman', 84, 700, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_090_long-quay-hat-thong-minh-cho-cho-meo-doggyman.webp', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/long-quay-hat-thong-minh-cho-cho-meo-doggyman_2.webp?v=1752462779'));

UPDATE products
SET name = 'Pate Cho Chó Sỏi Thận Royal Canin Urinary S/O Lon 410g',
    image = 'products/paddy_091_pate-cho-cho-soi-than-royal-canin-urinary-s-o-410g.jpg',
    price = 155000,
    discount = 0,
    description = 'Pate Cho Chó Sỏi Thận Royal Canin Urinary S/O Lon 410g Thương hiệu: Royal canin Phù hợp cho: Chó trưởng thành đang điều trị thận Pate cho chó Royal Canin Urinary S/O là thức ăn dinh dưỡng hoàn chỉnh cho thú cưng, công thức hỗ trợ hòa tan sỏi Struvite và giả...

Thương hiệu: Royal Canin.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-cho-soi-than-royal-canin-urinary-s-o-410g',
    stock = 40,
    weight = 500,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_091_pate-cho-cho-soi-than-royal-canin-urinary-s-o-410g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pate_cho_cho_bi_soi_than.jpg?v=1755159998');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Chó Sỏi Thận Royal Canin Urinary S/O Lon 410g', 'products/paddy_091_pate-cho-cho-soi-than-royal-canin-urinary-s-o-410g.jpg', 155000, 0,
       'Pate Cho Chó Sỏi Thận Royal Canin Urinary S/O Lon 410g Thương hiệu: Royal canin Phù hợp cho: Chó trưởng thành đang điều trị thận Pate cho chó Royal Canin Urinary S/O là thức ăn dinh dưỡng hoàn chỉnh cho thú cưng, công thức hỗ trợ hòa tan sỏi Struvite và giả...

Thương hiệu: Royal Canin.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-cho-soi-than-royal-canin-urinary-s-o-410g', 40, 500, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_091_pate-cho-cho-soi-than-royal-canin-urinary-s-o-410g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pate_cho_cho_bi_soi_than.jpg?v=1755159998'));

UPDATE products
SET name = 'Cát Sắn Cho Mèo Purcats Miracle 2.5kg',
    image = 'products/paddy_092_cat-san-cho-meo-purcats-miracle-2-5kg.webp',
    price = 140000,
    discount = 0,
    description = 'Cát Sắn Cho Mèo Purcats Miracle 2.5kg Thương hiệu: Cature Phù hợp cho: Mèo mọi lứa tuổi Được nâng cấp từ dòng sản phẩm Purcat nổi tiếng, Purcat Miracle sở hữu công nghệ khóa mùi tiên tiến, giúp kiểm soát mùi hôi hiệu quả, mang lại không gian sạch sẽ, dễ chị...

Thương hiệu: Cature.

Nguồn tham khảo: https://paddy.vn/products/cat-san-cho-meo-purcats-miracle-2-5kg',
    stock = 41,
    weight = 2700,
    category = 'Vệ Sinh Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_092_cat-san-cho-meo-purcats-miracle-2-5kg.webp', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/cat-san-cho-meo-purcats-miracle-2-5kg_2.webp?v=1754040877');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Cát Sắn Cho Mèo Purcats Miracle 2.5kg', 'products/paddy_092_cat-san-cho-meo-purcats-miracle-2-5kg.webp', 140000, 0,
       'Cát Sắn Cho Mèo Purcats Miracle 2.5kg Thương hiệu: Cature Phù hợp cho: Mèo mọi lứa tuổi Được nâng cấp từ dòng sản phẩm Purcat nổi tiếng, Purcat Miracle sở hữu công nghệ khóa mùi tiên tiến, giúp kiểm soát mùi hôi hiệu quả, mang lại không gian sạch sẽ, dễ chị...

Thương hiệu: Cature.

Nguồn tham khảo: https://paddy.vn/products/cat-san-cho-meo-purcats-miracle-2-5kg', 41, 2700, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_092_cat-san-cho-meo-purcats-miracle-2-5kg.webp', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/cat-san-cho-meo-purcats-miracle-2-5kg_2.webp?v=1754040877'));

UPDATE products
SET name = 'Đồ Chơi Cho Chó Banh Doggyman Hình Đuôi',
    image = 'products/paddy_093_do-choi-cho-cho-banh-doggyman-hinh-duoi.jpg',
    price = 72000,
    discount = 0,
    description = 'Đồ Chơi Cho Chó Banh Doggyman Hình Đuôi Thương hiệu: Doggyman Phù hợp cho: Chó mọi lứa tuổi Đồ chơi cho chó nhồi bông hình đuôi mềm mại và dễ thương giúp giao tiếp với thú cưng. Có chiếc chuông bên trong tạo âm thanh khi vui đùa giúp chó cưng thích thú. Lợi...

Thương hiệu: DoggyMan.

Nguồn tham khảo: https://paddy.vn/products/do-choi-cho-cho-banh-doggyman-hinh-duoi',
    stock = 42,
    weight = 100,
    category = 'Đồ Chơi Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_093_do-choi-cho-cho-banh-doggyman-hinh-duoi.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/do-choi-cho-cho-banh-doggyman-hinh-duoi_2.jpg?v=1750914461');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Đồ Chơi Cho Chó Banh Doggyman Hình Đuôi', 'products/paddy_093_do-choi-cho-cho-banh-doggyman-hinh-duoi.jpg', 72000, 0,
       'Đồ Chơi Cho Chó Banh Doggyman Hình Đuôi Thương hiệu: Doggyman Phù hợp cho: Chó mọi lứa tuổi Đồ chơi cho chó nhồi bông hình đuôi mềm mại và dễ thương giúp giao tiếp với thú cưng. Có chiếc chuông bên trong tạo âm thanh khi vui đùa giúp chó cưng thích thú. Lợi...

Thương hiệu: DoggyMan.

Nguồn tham khảo: https://paddy.vn/products/do-choi-cho-cho-banh-doggyman-hinh-duoi', 42, 100, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_093_do-choi-cho-cho-banh-doggyman-hinh-duoi.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/do-choi-cho-cho-banh-doggyman-hinh-duoi_2.jpg?v=1750914461'));

UPDATE products
SET name = 'Pate Cho Mèo Ciao 40g (Thái Lan)',
    image = 'products/paddy_094_pate-cho-meo-ciao-40g-thai-lan.jpg',
    price = 15000,
    discount = 0,
    description = 'Pate Cho Mèo Ciao 40g (Thái Lan) Thương hiệu: Ciao Phù hợp cho: Mèo trưởng thành Mang đến trải nghiệm ẩm thực đẳng cấp cho mèo cưng, Pate Mèo Ciao là sự kết hợp hoàn hảo của những nguyên liệu cao cấp, được chế biến thành dạng súp đặc sánh mịn mà bất kỳ chú...

Thương hiệu: Ciao.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-ciao-40g-thai-lan',
    stock = 43,
    weight = 100,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_094_pate-cho-meo-ciao-40g-thai-lan.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pateciao40g_2.jpg?v=1750909891');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Mèo Ciao 40g (Thái Lan)', 'products/paddy_094_pate-cho-meo-ciao-40g-thai-lan.jpg', 15000, 0,
       'Pate Cho Mèo Ciao 40g (Thái Lan) Thương hiệu: Ciao Phù hợp cho: Mèo trưởng thành Mang đến trải nghiệm ẩm thực đẳng cấp cho mèo cưng, Pate Mèo Ciao là sự kết hợp hoàn hảo của những nguyên liệu cao cấp, được chế biến thành dạng súp đặc sánh mịn mà bất kỳ chú...

Thương hiệu: Ciao.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-ciao-40g-thai-lan', 43, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_094_pate-cho-meo-ciao-40g-thai-lan.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/pateciao40g_2.jpg?v=1750909891'));

UPDATE products
SET name = 'Bánh Thưởng Cho Chó Natural Lab Thịt Viên Hộp 1kg',
    image = 'products/paddy_095_thit-vien-cho-cho-natural-lab-hop-1kg.webp',
    price = 360000,
    discount = 0,
    description = 'Bánh Thưởng Cho Chó Natural Lab Thịt Viên Hộp 1kg Thương hiệu: Natural Lab Phù hợp cho: Chó mọi lứa tuổi Bạn có muốn bé cún luôn vui vẻ, khỏe mạnh và tràn đầy năng lượng không? Với Natural Lab, Sen không chỉ mang về một món bánh thưởng cho chó siêu hấp dẫn...

Thương hiệu: Natural Lab.

Nguồn tham khảo: https://paddy.vn/products/thit-vien-cho-cho-natural-lab-hop-1kg',
    stock = 44,
    weight = 100,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_095_thit-vien-cho-cho-natural-lab-hop-1kg.webp', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/thit-vien-cho-cho-natural-lab-hop-1kg_4.webp?v=1750845782');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Cho Chó Natural Lab Thịt Viên Hộp 1kg', 'products/paddy_095_thit-vien-cho-cho-natural-lab-hop-1kg.webp', 360000, 0,
       'Bánh Thưởng Cho Chó Natural Lab Thịt Viên Hộp 1kg Thương hiệu: Natural Lab Phù hợp cho: Chó mọi lứa tuổi Bạn có muốn bé cún luôn vui vẻ, khỏe mạnh và tràn đầy năng lượng không? Với Natural Lab, Sen không chỉ mang về một món bánh thưởng cho chó siêu hấp dẫn...

Thương hiệu: Natural Lab.

Nguồn tham khảo: https://paddy.vn/products/thit-vien-cho-cho-natural-lab-hop-1kg', 44, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_095_thit-vien-cho-cho-natural-lab-hop-1kg.webp', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/thit-vien-cho-cho-natural-lab-hop-1kg_4.webp?v=1750845782'));

UPDATE products
SET name = 'Bát Ăn Cho Chó Mèo Bằng Nhựa Nhiều Kiểu Dáng',
    image = 'products/paddy_096_bat-an-cho-cho-meo-bang-nhua-nhieu-kieu-dang.jpg',
    price = 75000,
    discount = 0,
    description = 'Bát Ăn Cho Chó Mèo Bằng Nhựa Nhiều Kiểu Dáng Thương hiệu: Paddy Phù hợp cho: Chó/Mèo mọi lứa tuổi Dụng cụ ăn uống cho chó mèo bát ăn có nhiều kích cỡ, kiểu dáng và màu sắc bắt mắt, hấp dẫn. Chất liệu nhựa PP cao cấp, chắc chắn. Bề mặt trơn láng, dễ dàng chù...

Thương hiệu: Paddy.

Nguồn tham khảo: https://paddy.vn/products/bat-an-cho-cho-meo-bang-nhua-nhieu-kieu-dang',
    stock = 45,
    weight = 300,
    category = 'Phụ Kiện Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_096_bat-an-cho-cho-meo-bang-nhua-nhieu-kieu-dang.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/bat-an-cho-meo-bang-nhua-nhieu-kieu.jpg?v=1754124801');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bát Ăn Cho Chó Mèo Bằng Nhựa Nhiều Kiểu Dáng', 'products/paddy_096_bat-an-cho-cho-meo-bang-nhua-nhieu-kieu-dang.jpg', 75000, 0,
       'Bát Ăn Cho Chó Mèo Bằng Nhựa Nhiều Kiểu Dáng Thương hiệu: Paddy Phù hợp cho: Chó/Mèo mọi lứa tuổi Dụng cụ ăn uống cho chó mèo bát ăn có nhiều kích cỡ, kiểu dáng và màu sắc bắt mắt, hấp dẫn. Chất liệu nhựa PP cao cấp, chắc chắn. Bề mặt trơn láng, dễ dàng chù...

Thương hiệu: Paddy.

Nguồn tham khảo: https://paddy.vn/products/bat-an-cho-cho-meo-bang-nhua-nhieu-kieu-dang', 45, 300, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_096_bat-an-cho-cho-meo-bang-nhua-nhieu-kieu-dang.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/bat-an-cho-meo-bang-nhua-nhieu-kieu.jpg?v=1754124801'));

UPDATE products
SET name = 'Bánh Thưởng Cho Chó Dexinbone Hỗ Trợ Sạch Răng',
    image = 'products/paddy_097_snack-cho-cho-dexinbone-sach-rang.jpg',
    price = 32000,
    discount = 0,
    description = 'Bánh Thưởng Cho Chó Dexinbone Hỗ Trợ Sạch Răng Thương hiệu: INU Fonti Phù hợp cho: Chó trưởng thành Snack cho Chó Dexinbone là dòng sản phẩm đồ ăn vặt chuyên biệt dành cho chó, được thiết kế không chỉ để thỏa mãn vị giác mà còn mang lại nhiều lợi ích vượt t...

Thương hiệu: INU Fonti.

Nguồn tham khảo: https://paddy.vn/products/snack-cho-cho-dexinbone-sach-rang',
    stock = 46,
    weight = 200,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_097_snack-cho-cho-dexinbone-sach-rang.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/snack-cho-cho-dexinbone-sach-rang_3.jpg?v=1750917229');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Cho Chó Dexinbone Hỗ Trợ Sạch Răng', 'products/paddy_097_snack-cho-cho-dexinbone-sach-rang.jpg', 32000, 0,
       'Bánh Thưởng Cho Chó Dexinbone Hỗ Trợ Sạch Răng Thương hiệu: INU Fonti Phù hợp cho: Chó trưởng thành Snack cho Chó Dexinbone là dòng sản phẩm đồ ăn vặt chuyên biệt dành cho chó, được thiết kế không chỉ để thỏa mãn vị giác mà còn mang lại nhiều lợi ích vượt t...

Thương hiệu: INU Fonti.

Nguồn tham khảo: https://paddy.vn/products/snack-cho-cho-dexinbone-sach-rang', 46, 200, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_097_snack-cho-cho-dexinbone-sach-rang.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/snack-cho-cho-dexinbone-sach-rang_3.jpg?v=1750917229'));

UPDATE products
SET name = 'Pate Cho Chó Kiểm Soát Cân Nặng Royal Canin Satiety Weight Management Lon 410g',
    image = 'products/paddy_098_pate-cho-cho-kiem-soat-can-nang-royal-canin-satiety-weight-management-lon-410g.jpg',
    price = 155000,
    discount = 0,
    description = 'Pate Cho Chó Kiểm Soát Cân Nặng Royal Canin Satiety Weight Management Lon 410g Thương hiệu: Royal Canin Phù hợp cho: Chó (trên 12 tháng tuổi bị thừa cân/ béo phì cần giảm cân) Pate cho chó ROYAL CANIN Satiety là chế độ ăn hoàn chỉnh và cân bằng, hàm lượng x...

Thương hiệu: Royal Canin.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-cho-kiem-soat-can-nang-royal-canin-satiety-weight-management-lon-410g',
    stock = 47,
    weight = 500,
    category = 'Thức Ăn Cho Chó',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_098_pate-cho-cho-kiem-soat-can-nang-royal-canin-satiety-weight-management-lon-410g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/ho_tro_kiem_soat_can_nang_cho_cho.jpg?v=1755160421');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Chó Kiểm Soát Cân Nặng Royal Canin Satiety Weight Management Lon 410g', 'products/paddy_098_pate-cho-cho-kiem-soat-can-nang-royal-canin-satiety-weight-management-lon-410g.jpg', 155000, 0,
       'Pate Cho Chó Kiểm Soát Cân Nặng Royal Canin Satiety Weight Management Lon 410g Thương hiệu: Royal Canin Phù hợp cho: Chó (trên 12 tháng tuổi bị thừa cân/ béo phì cần giảm cân) Pate cho chó ROYAL CANIN Satiety là chế độ ăn hoàn chỉnh và cân bằng, hàm lượng x...

Thương hiệu: Royal Canin.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-cho-kiem-soat-can-nang-royal-canin-satiety-weight-management-lon-410g', 47, 500, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_098_pate-cho-cho-kiem-soat-can-nang-royal-canin-satiety-weight-management-lon-410g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/ho_tro_kiem_soat_can_nang_cho_cho.jpg?v=1755160421'));

UPDATE products
SET name = 'Pate Cho Mèo Miratorg Thơm Ngon 80g',
    image = 'products/paddy_099_pate-cho-meo-miratorg-thom-ngon-80g.jpg',
    price = 25000,
    discount = 0,
    description = 'Pate Cho Mèo Miratorg Thơm Ngon 80g Thương hiệu: Miratorg Phù hợp cho: Mèo tùy từng loại sản phẩm Thức ăn cho mèo Miratorg là thương hiệu thức ăn thượng hạng từ Nga, nổi bật với tiêu chí “Thịt là thành phần cốt lõi.” Sản phẩm sử dụng nguồn thịt tươi, cung c...

Thương hiệu: Miratorg.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-miratorg-thom-ngon-80g',
    stock = 8,
    weight = 100,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_099_pate-cho-meo-miratorg-thom-ngon-80g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/miratorg-cho-meo_6_9146c2eb-7e0f-4248-ad69-78197d96f77d.jpg?v=1752638019');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Mèo Miratorg Thơm Ngon 80g', 'products/paddy_099_pate-cho-meo-miratorg-thom-ngon-80g.jpg', 25000, 0,
       'Pate Cho Mèo Miratorg Thơm Ngon 80g Thương hiệu: Miratorg Phù hợp cho: Mèo tùy từng loại sản phẩm Thức ăn cho mèo Miratorg là thương hiệu thức ăn thượng hạng từ Nga, nổi bật với tiêu chí “Thịt là thành phần cốt lõi.” Sản phẩm sử dụng nguồn thịt tươi, cung c...

Thương hiệu: Miratorg.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-miratorg-thom-ngon-80g', 8, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_099_pate-cho-meo-miratorg-thom-ngon-80g.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/miratorg-cho-meo_6_9146c2eb-7e0f-4248-ad69-78197d96f77d.jpg?v=1752638019'));

UPDATE products
SET name = 'Hạt Cho Mèo Trưởng Thành Miratorg Nhiều Thịt Thơm Ngon',
    image = 'products/paddy_100_hat-cho-meo-truong-thanh-miratorg-nhieu-thit-thom-ngon.jpg',
    price = 59000,
    discount = 0,
    description = 'Hạt Cho Mèo Trưởng Thành Miratorg Nhiều Thịt Thơm Ngon Thương hiệu: Miratorg Phù hợp cho: Mèo từ 12 tháng tuổi Thức ăn cho mèo Miratorg là thương hiệu thức ăn thượng hạng từ Nga, nổi bật với tiêu chí “Thịt là thành phần cốt lõi.” Sản phẩm sử dụng nguồn thịt...

Thương hiệu: Miratorg.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-truong-thanh-miratorg-nhieu-thit-thom-ngon',
    stock = 8,
    weight = 100,
    category = 'Thức Ăn Cho Mèo',
    pet_type_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1),
    is_active = 1
WHERE image IN ('products/paddy_100_hat-cho-meo-truong-thanh-miratorg-nhieu-thit-thom-ngon.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/miratorg-cho-meo_3.jpg?v=1752637891');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo Trưởng Thành Miratorg Nhiều Thịt Thơm Ngon', 'products/paddy_100_hat-cho-meo-truong-thanh-miratorg-nhieu-thit-thom-ngon.jpg', 59000, 0,
       'Hạt Cho Mèo Trưởng Thành Miratorg Nhiều Thịt Thơm Ngon Thương hiệu: Miratorg Phù hợp cho: Mèo từ 12 tháng tuổi Thức ăn cho mèo Miratorg là thương hiệu thức ăn thượng hạng từ Nga, nổi bật với tiêu chí “Thịt là thành phần cốt lõi.” Sản phẩm sử dụng nguồn thịt...

Thương hiệu: Miratorg.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-truong-thanh-miratorg-nhieu-thit-thom-ngon', 8, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE image IN ('products/paddy_100_hat-cho-meo-truong-thanh-miratorg-nhieu-thit-thom-ngon.jpg', 'https://cdn.shopify.com/s/files/1/0624/1746/9697/files/miratorg-cho-meo_3.jpg?v=1752637891'));
