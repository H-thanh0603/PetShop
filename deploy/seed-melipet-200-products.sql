-- Melipet product restore seed: 200 UTF-8 products with local images.

-- Safe to run multiple times because every insert checks product name first.

SET NAMES utf8mb4;



INSERT INTO pet_types (code, name, icon, display_order, is_active) VALUES

('dog', 'Chó', 'bxs-dog', 1, 1),

('cat', 'Mèo', 'bxs-cat', 2, 1)

ON DUPLICATE KEY UPDATE name = VALUES(name), icon = VALUES(icon), display_order = VALUES(display_order), is_active = 1;



INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Royal Canin Hạt dinh dưỡng cho mèo trưởng thành - gói tiêu chuẩn', 'products/paddy_001_banh-thuong-cho-meo-uc-ga-hap-nuoc-cot-ga-cattyman.jpg', 145000, 0,
       'Hạt dinh dưỡng cho mèo trưởng thành thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 25, 1500, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Royal Canin Hạt dinh dưỡng cho mèo trưởng thành - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Whiskas Hạt cho mèo con vị cá hồi - gói tiêu chuẩn', 'products/paddy_002_que-gan-bo-cho-cho-doggyman-100g.jpg', 139000, 0,
       'Hạt cho mèo con vị cá hồi thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 38, 1296, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Whiskas Hạt cho mèo con vị cá hồi - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pedigree Pate mèo vị cá ngừ - gói tiêu chuẩn', 'products/paddy_003_vong-co-va-day-dat-cho-cho-police.jpg', 39000, 5,
       'Pate mèo vị cá ngừ thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 51, 105, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pedigree Pate mèo vị cá ngừ - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'SmartHeart Súp thưởng cho mèo vị gà - gói tiêu chuẩn', 'products/paddy_004_xuc-xich-cho-cho-ga-mini-doggyman.jpg', 60000, 10,
       'Súp thưởng cho mèo vị gà thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 64, 90, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'SmartHeart Súp thưởng cho mèo vị gà - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Monge Snack thưởng mềm cho mèo - gói tiêu chuẩn', 'products/paddy_005_hat-sieu-topping-cho-meo-kings-pet.jpg', 77000, 12,
       'Snack thưởng mềm cho mèo thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 77, 115, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Monge Snack thưởng mềm cho mèo - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ganador Sữa không lactose cho mèo - gói tiêu chuẩn', 'products/paddy_006_long-van-chuyen-hang-khong-cho-cho-mon-ami-carrier.jpg', 77000, 15,
       'Sữa không lactose cho mèo thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 90, 200, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ganador Sữa không lactose cho mèo - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Me-O Cát vệ sinh đậu nành khử mùi - gói tiêu chuẩn', 'products/paddy_007_hat-cho-meo-5plus-catmix-ruoc-ga-say-kho.jpg', 167000, 20,
       'Cát vệ sinh đậu nành khử mùi thuộc nhóm vệ sinh cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 103, 2700, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Me-O Cát vệ sinh đậu nành khử mùi - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Kit Cat Cát vệ sinh bentonite vón cục - gói tiêu chuẩn', 'products/paddy_008_bot-vi-sinh-khu-mui-cho-cho-meo-joycat-54g.jpg', 89000, 0,
       'Cát vệ sinh bentonite vón cục thuộc nhóm vệ sinh cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 116, 5800, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Kit Cat Cát vệ sinh bentonite vón cục - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ciao Xịt khử mùi khu vực vệ sinh mèo - gói tiêu chuẩn', 'products/paddy_009_xit-vi-sinh-khu-mui-cho-cho-meo-joycat-450ml.jpg', 106000, 0,
       'Xịt khử mùi khu vực vệ sinh mèo thuộc nhóm vệ sinh cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 129, 558, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ciao Xịt khử mùi khu vực vệ sinh mèo - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Trixie Khay vệ sinh mèo thành cao - gói tiêu chuẩn', 'products/paddy_010_hat-cho-meo-nutri-plan-plus-1-5kg.jpg', 199000, 5,
       'Khay vệ sinh mèo thành cao thuộc nhóm vệ sinh cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 26, 1320, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Trixie Khay vệ sinh mèo thành cao - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'FOFOS Cần câu lông vũ cho mèo - gói tiêu chuẩn', 'products/paddy_011_cat-ve-sinh-cho-meo-joycat.jpg', 56000, 10,
       'Cần câu lông vũ cho mèo thuộc nhóm đồ chơi cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 39, 80, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'FOFOS Cần câu lông vũ cho mèo - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Doggyman Bàn cào móng giấy cho mèo - gói tiêu chuẩn', 'products/paddy_012_hat-cho-meo-wanpy-mix-thit-vien.jpg', 177000, 12,
       'Bàn cào móng giấy cho mèo thuộc nhóm đồ chơi cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 52, 648, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Doggyman Bàn cào móng giấy cho mèo - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Natural Core Bóng chuông tương tác cho mèo - gói tiêu chuẩn', 'products/paddy_013_hat-cho-cho-smartheart-gold-indoor.jpg', 64000, 15,
       'Bóng chuông tương tác cho mèo thuộc nhóm đồ chơi cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 65, 80, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Natural Core Bóng chuông tương tác cho mèo - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Joycat Trụ cào móng dây thừng - gói tiêu chuẩn', 'products/paddy_014_hat-cho-cho-truong-thanh-happy-tummy.jpg', 277000, 20,
       'Trụ cào móng dây thừng thuộc nhóm đồ chơi cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 78, 1116, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Joycat Trụ cào móng dây thừng - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Melipet Bát ăn inox chống trượt cho mèo - gói tiêu chuẩn', 'products/paddy_015_hat-cho-meo-truong-thanh-happy-tummy.jpg', 55000, 0,
       'Bát ăn inox chống trượt cho mèo thuộc nhóm phụ kiện cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 91, 330, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Melipet Bát ăn inox chống trượt cho mèo - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Royal Canin Nệm nằm êm ái cho mèo - gói tiêu chuẩn', 'products/paddy_016_hat-cho-meo-excel-vi-ca-thom-ngon.jpg', 196000, 0,
       'Nệm nằm êm ái cho mèo thuộc nhóm phụ kiện cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 104, 800, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Royal Canin Nệm nằm êm ái cho mèo - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Whiskas Balo vận chuyển mèo thoáng khí - gói tiêu chuẩn', 'products/paddy_017_banh-thuong-cho-cho-meo-moi-lua-tuoi-dexinbone.jpg', 399000, 5,
       'Balo vận chuyển mèo thoáng khí thuộc nhóm phụ kiện cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 117, 1296, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Whiskas Balo vận chuyển mèo thoáng khí - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pedigree Vòng cổ mèo có chuông an toàn - gói tiêu chuẩn', 'products/paddy_018_pate-cho-cho-moi-lua-tuoi-lapaw-375g.webp', 66000, 10,
       'Vòng cổ mèo có chuông an toàn thuộc nhóm phụ kiện cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 130, 110, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pedigree Vòng cổ mèo có chuông an toàn - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'SmartHeart Gel dinh dưỡng hỗ trợ búi lông - gói tiêu chuẩn', 'products/paddy_019_pate-cho-meo-on25-80g.jpg', 143000, 12,
       'Gel dinh dưỡng hỗ trợ búi lông thuộc nhóm chăm sóc sức khỏe cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 27, 150, 'Chăm Sóc Sức Khỏe Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'SmartHeart Gel dinh dưỡng hỗ trợ búi lông - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Monge Viên dầu cá hồi omega cho mèo - gói tiêu chuẩn', 'products/paddy_020_hat-cho-cho-moi-lua-tuoi-nutrience-subzero-prairier-red.jpg', 124000, 15,
       'Viên dầu cá hồi omega cho mèo thuộc nhóm chăm sóc sức khỏe cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 40, 264, 'Chăm Sóc Sức Khỏe Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Monge Viên dầu cá hồi omega cho mèo - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ganador Hạt dinh dưỡng cho chó trưởng thành - gói tiêu chuẩn', 'products/paddy_021_hat-cho-cho-on25-dog.jpg', 207000, 20,
       'Hạt dinh dưỡng cho chó trưởng thành thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 53, 1500, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ganador Hạt dinh dưỡng cho chó trưởng thành - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Me-O Hạt cho chó con giống nhỏ - gói tiêu chuẩn', 'products/paddy_022_sua-de-cho-meo-kit-cat-khong-lactose.jpg', 158000, 0,
       'Hạt cho chó con giống nhỏ thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 66, 1404, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Me-O Hạt cho chó con giống nhỏ - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Kit Cat Pate chó vị bò và rau củ - gói tiêu chuẩn', 'products/paddy_023_banh-thuong-cho-cho-natural-core-sun-ga-55g.png', 35000, 0,
       'Pate chó vị bò và rau củ thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 79, 150, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Kit Cat Pate chó vị bò và rau củ - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ciao Snack que gặm sạch răng cho chó - gói tiêu chuẩn', 'products/paddy_024_day-xich-cho-cho-mon-ami-3x120cm.png', 73000, 5,
       'Snack que gặm sạch răng cho chó thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 92, 150, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ciao Snack que gặm sạch răng cho chó - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Trixie Xúc xích thưởng vị gà cho chó - gói tiêu chuẩn', 'products/paddy_025_vien-dau-ca-hoi-omega-cho-meo-hop-60-vien.jpg', 57000, 10,
       'Xúc xích thưởng vị gà cho chó thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 105, 140, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Trixie Xúc xích thưởng vị gà cho chó - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'FOFOS Thịt sấy thưởng huấn luyện cho chó - gói tiêu chuẩn', 'products/paddy_026_banh-thuong-cho-cho-inu-fonti-xuong-gam-ban-chai-90g.jpg', 106000, 12,
       'Thịt sấy thưởng huấn luyện cho chó thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 118, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'FOFOS Thịt sấy thưởng huấn luyện cho chó - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Doggyman Sữa tắm dưỡng lông cho chó - gói tiêu chuẩn', 'products/paddy_027_pate-meo-the-pet-cho-meo-1kg.jpg', 170000, 15,
       'Sữa tắm dưỡng lông cho chó thuộc nhóm vệ sinh cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 131, 383, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Doggyman Sữa tắm dưỡng lông cho chó - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Natural Core Xịt khử mùi lông chó - gói tiêu chuẩn', 'products/paddy_028_bat-an-cho-cho-meo-inox-bowl-mon-ami.png', 141000, 20,
       'Xịt khử mùi lông chó thuộc nhóm vệ sinh cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 28, 522, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Natural Core Xịt khử mùi lông chó - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Joycat Dung dịch vệ sinh tai cho chó - gói tiêu chuẩn', 'products/paddy_029_balo-van-chuyen-cho-cho-meo-da-cao-cap-diamond.jpg', 85000, 0,
       'Dung dịch vệ sinh tai cho chó thuộc nhóm vệ sinh cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 41, 150, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Joycat Dung dịch vệ sinh tai cho chó - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Melipet Tã lót vệ sinh cho chó cái - gói tiêu chuẩn', 'products/paddy_030_banh-thuong-thit-say-cho-meo-catsdo.jpg', 122000, 0,
       'Tã lót vệ sinh cho chó cái thuộc nhóm vệ sinh cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 54, 396, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Melipet Tã lót vệ sinh cho chó cái - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Royal Canin Bóng cao su luyện vận động cho chó - gói tiêu chuẩn', 'products/paddy_031_nem-cho-cho-meo-pupdy-floating-mattress.jpg', 59000, 5,
       'Bóng cao su luyện vận động cho chó thuộc nhóm đồ chơi cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 67, 180, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Royal Canin Bóng cao su luyện vận động cho chó - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Whiskas Dây thừng gặm nhai cho chó - gói tiêu chuẩn', 'products/paddy_032_banh-thuong-cho-cho-jireho.jpg', 86000, 10,
       'Dây thừng gặm nhai cho chó thuộc nhóm đồ chơi cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 80, 270, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Whiskas Dây thừng gặm nhai cho chó - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pedigree Thú bông có chuông cho chó - gói tiêu chuẩn', 'products/paddy_033_pate-cho-meo-alpha-pet-70g.jpg', 117000, 12,
       'Thú bông có chuông cho chó thuộc nhóm đồ chơi cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 93, 348, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pedigree Thú bông có chuông cho chó - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'SmartHeart Xương gặm đồ chơi cao su - gói tiêu chuẩn', 'products/paddy_034_pate-cho-meo-real-raw-catidea-170g.jpg', 107000, 15,
       'Xương gặm đồ chơi cao su thuộc nhóm đồ chơi cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 106, 274, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'SmartHeart Xương gặm đồ chơi cao su - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Monge Vòng cổ và dây dắt cho chó - gói tiêu chuẩn', 'products/paddy_035_cao-mong-giay-cho-meo-moi-lua-tuoi-fofos.jpg', 127000, 20,
       'Vòng cổ và dây dắt cho chó thuộc nhóm phụ kiện cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 119, 330, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Monge Vòng cổ và dây dắt cho chó - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ganador Dây dắt tự động cho chó - gói tiêu chuẩn', 'products/paddy_036_do-choi-day-thung-cho-cho-fofos.jpg', 195000, 0,
       'Dây dắt tự động cho chó thuộc nhóm phụ kiện cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 132, 450, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ganador Dây dắt tự động cho chó - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Me-O Bát ăn inox chống trượt cho chó - gói tiêu chuẩn', 'products/paddy_037_do-choi-meo-fofos-set-6-mon.jpg', 76000, 0,
       'Bát ăn inox chống trượt cho chó thuộc nhóm phụ kiện cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 29, 324, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Me-O Bát ăn inox chống trượt cho chó - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Kit Cat Lồng vận chuyển chó mèo - gói tiêu chuẩn', 'products/paddy_038_pate-cho-meo-whiskas-80g-hop-6-goi.jpg', 534000, 5,
       'Lồng vận chuyển chó mèo thuộc nhóm phụ kiện cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 42, 2088, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Kit Cat Lồng vận chuyển chó mèo - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ciao Viên bổ sung canxi cho chó - gói tiêu chuẩn', 'products/paddy_039_xit-khu-mui-cat-ve-sinh-cho-meo-arm-hammer-636ml.jpg', 146000, 10,
       'Viên bổ sung canxi cho chó thuộc nhóm chăm sóc sức khỏe cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 55, 222, 'Chăm Sóc Sức Khỏe Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ciao Viên bổ sung canxi cho chó - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Trixie Dầu cá hồi omega cho chó - gói tiêu chuẩn', 'products/paddy_040_hat-cho-meo-moi-lua-tuoi-todays-dinner.jpg', 127000, 12,
       'Dầu cá hồi omega cho chó thuộc nhóm chăm sóc sức khỏe cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản gói tiêu chuẩn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 68, 264, 'Chăm Sóc Sức Khỏe Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Trixie Dầu cá hồi omega cho chó - gói tiêu chuẩn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'FOFOS Hạt dinh dưỡng cho mèo trưởng thành - combo tiết kiệm', 'products/paddy_041_vong-go-cho-meo-catca.jpg', 180000, 15,
       'Hạt dinh dưỡng cho mèo trưởng thành thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 81, 1500, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'FOFOS Hạt dinh dưỡng cho mèo trưởng thành - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Doggyman Hạt cho mèo con vị cá hồi - combo tiết kiệm', 'products/paddy_042_pate-cho-cho-pedigree-80g.jpg', 174000, 20,
       'Hạt cho mèo con vị cá hồi thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 94, 1296, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Doggyman Hạt cho mèo con vị cá hồi - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Natural Core Pate mèo vị cá ngừ - combo tiết kiệm', 'products/paddy_043_hat-cho-cho-truong-thanh-gran-deli-700g.jpg', 25000, 0,
       'Pate mèo vị cá ngừ thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 107, 105, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Natural Core Pate mèo vị cá ngừ - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Joycat Súp thưởng cho mèo vị gà - combo tiết kiệm', 'products/paddy_044_gel-dinh-duong-chuc-nang-cho-meo-kit-cat.jpg', 46000, 0,
       'Súp thưởng cho mèo vị gà thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 120, 90, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Joycat Súp thưởng cho mèo vị gà - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Melipet Snack thưởng mềm cho mèo - combo tiết kiệm', 'products/paddy_045_banh-thuong-cho-meo-temptations-thom-ngon-75g.png', 63000, 5,
       'Snack thưởng mềm cho mèo thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 133, 115, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Melipet Snack thưởng mềm cho mèo - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Royal Canin Sữa không lactose cho mèo - combo tiết kiệm', 'products/paddy_046_may-don-phan-tu-dong-cho-meo-petree-version-2.jpg', 63000, 10,
       'Sữa không lactose cho mèo thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 30, 200, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Royal Canin Sữa không lactose cho mèo - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Whiskas Cát vệ sinh đậu nành khử mùi - combo tiết kiệm', 'products/paddy_047_may-don-phan-tu-dong-cho-meo-neakasa.jpg', 153000, 12,
       'Cát vệ sinh đậu nành khử mùi thuộc nhóm vệ sinh cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 43, 2700, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Whiskas Cát vệ sinh đậu nành khử mùi - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pedigree Cát vệ sinh bentonite vón cục - combo tiết kiệm', 'products/paddy_048_sup-thuong-cho-meo-bite-of-wild-15gx4.jpg', 124000, 15,
       'Cát vệ sinh bentonite vón cục thuộc nhóm vệ sinh cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 56, 5800, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pedigree Cát vệ sinh bentonite vón cục - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'SmartHeart Xịt khử mùi khu vực vệ sinh mèo - combo tiết kiệm', 'products/paddy_049_snack-ga-say-cho-cho-doggyman-pawfect-choice-180g.jpg', 141000, 20,
       'Xịt khử mùi khu vực vệ sinh mèo thuộc nhóm vệ sinh cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 69, 558, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'SmartHeart Xịt khử mùi khu vực vệ sinh mèo - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Monge Khay vệ sinh mèo thành cao - combo tiết kiệm', 'products/paddy_050_hat-cho-meo-moi-lua-tuoi-cattyman.jpg', 185000, 0,
       'Khay vệ sinh mèo thành cao thuộc nhóm vệ sinh cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 82, 1320, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Monge Khay vệ sinh mèo thành cao - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ganador Cần câu lông vũ cho mèo - combo tiết kiệm', 'products/paddy_051_hat-cho-meo-moi-lua-tuoi-s2pet-v1-v2-v3.jpg', 42000, 0,
       'Cần câu lông vũ cho mèo thuộc nhóm đồ chơi cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 95, 80, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ganador Cần câu lông vũ cho mèo - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Me-O Bàn cào móng giấy cho mèo - combo tiết kiệm', 'products/paddy_052_pate-cho-meo-cao-cap-bite-of-wild-tui-70g.jpg', 163000, 5,
       'Bàn cào móng giấy cho mèo thuộc nhóm đồ chơi cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 108, 648, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Me-O Bàn cào móng giấy cho mèo - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Kit Cat Bóng chuông tương tác cho mèo - combo tiết kiệm', 'products/paddy_053_sua-tuoi-kyushu-cho-cho-meo-khong-chua-lactose-200ml.jpg', 50000, 10,
       'Bóng chuông tương tác cho mèo thuộc nhóm đồ chơi cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 121, 80, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Kit Cat Bóng chuông tương tác cho mèo - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ciao Trụ cào móng dây thừng - combo tiết kiệm', 'products/paddy_054_hat-cho-meo-cature-easy-farm-topping-1-5kg.jpg', 263000, 12,
       'Trụ cào móng dây thừng thuộc nhóm đồ chơi cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 134, 1116, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ciao Trụ cào móng dây thừng - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Trixie Bát ăn inox chống trượt cho mèo - combo tiết kiệm', 'products/paddy_055_khay-ve-sinh-cho-meo-mon-ami-cao-cap.jpg', 90000, 15,
       'Bát ăn inox chống trượt cho mèo thuộc nhóm phụ kiện cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 31, 330, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Trixie Bát ăn inox chống trượt cho mèo - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'FOFOS Nệm nằm êm ái cho mèo - combo tiết kiệm', 'products/paddy_056_sup-thuong-cho-cho-meo-vfcore-chuc-nang.jpg', 231000, 20,
       'Nệm nằm êm ái cho mèo thuộc nhóm phụ kiện cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 44, 800, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'FOFOS Nệm nằm êm ái cho mèo - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Doggyman Balo vận chuyển mèo thoáng khí - combo tiết kiệm', 'products/paddy_057_dung-dich-ve-sinh-tai-cho-cho-meo-jungle-monster-chamomile-120ml.jpg', 385000, 0,
       'Balo vận chuyển mèo thoáng khí thuộc nhóm phụ kiện cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 57, 1296, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Doggyman Balo vận chuyển mèo thoáng khí - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Natural Core Vòng cổ mèo có chuông an toàn - combo tiết kiệm', 'products/paddy_058_xit-thom-mieng-cho-cho-meo-jungle-monster-dental-spray-30ml.jpg', 52000, 0,
       'Vòng cổ mèo có chuông an toàn thuộc nhóm phụ kiện cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 70, 110, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Natural Core Vòng cổ mèo có chuông an toàn - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Joycat Gel dinh dưỡng hỗ trợ búi lông - combo tiết kiệm', 'products/paddy_059_pate-cho-meo-truong-thanh-me-o-delite-goi-70g.jpg', 129000, 5,
       'Gel dinh dưỡng hỗ trợ búi lông thuộc nhóm chăm sóc sức khỏe cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 83, 150, 'Chăm Sóc Sức Khỏe Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Joycat Gel dinh dưỡng hỗ trợ búi lông - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Melipet Viên dầu cá hồi omega cho mèo - combo tiết kiệm', 'products/paddy_060_day-dat-bam-tu-dong-cho-cho-flexi.jpg', 110000, 10,
       'Viên dầu cá hồi omega cho mèo thuộc nhóm chăm sóc sức khỏe cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 96, 264, 'Chăm Sóc Sức Khỏe Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Melipet Viên dầu cá hồi omega cho mèo - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Royal Canin Hạt dinh dưỡng cho chó trưởng thành - combo tiết kiệm', 'products/paddy_061_hat-cho-meo-smartheart-cat.jpg', 193000, 12,
       'Hạt dinh dưỡng cho chó trưởng thành thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 109, 1500, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Royal Canin Hạt dinh dưỡng cho chó trưởng thành - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Whiskas Hạt cho chó con giống nhỏ - combo tiết kiệm', 'products/paddy_062_do-choi-cho-meo-fofos-long-vu.webp', 193000, 15,
       'Hạt cho chó con giống nhỏ thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 122, 1404, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Whiskas Hạt cho chó con giống nhỏ - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pedigree Pate chó vị bò và rau củ - combo tiết kiệm', 'products/paddy_063_cao-mong-meo-fofos-hinh-ly-cafe-42x35cm.webp', 70000, 20,
       'Pate chó vị bò và rau củ thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 135, 150, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pedigree Pate chó vị bò và rau củ - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'SmartHeart Snack que gặm sạch răng cho chó - combo tiết kiệm', 'products/paddy_064_cao-mong-meo-fofos-hinh-trai-dau-35x30cm.jpg', 59000, 0,
       'Snack que gặm sạch răng cho chó thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 32, 150, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'SmartHeart Snack que gặm sạch răng cho chó - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Monge Xúc xích thưởng vị gà cho chó - combo tiết kiệm', 'products/paddy_065_hat-cho-cho-wanpy-khong-ngu-coc-mix-thit-vien.jpg', 43000, 0,
       'Xúc xích thưởng vị gà cho chó thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 45, 140, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Monge Xúc xích thưởng vị gà cho chó - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ganador Thịt sấy thưởng huấn luyện cho chó - combo tiết kiệm', 'products/paddy_066_sup-thuong-cho-meo-silver-spoon-thailand-56g-4-tuyp.jpg', 92000, 5,
       'Thịt sấy thưởng huấn luyện cho chó thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 58, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ganador Thịt sấy thưởng huấn luyện cho chó - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Me-O Sữa tắm dưỡng lông cho chó - combo tiết kiệm', 'products/paddy_067_banh-thuong-cho-cho-que-dai-da-bo-doggyman-white-dental-120g.jpg', 156000, 10,
       'Sữa tắm dưỡng lông cho chó thuộc nhóm vệ sinh cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 71, 383, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Me-O Sữa tắm dưỡng lông cho chó - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Kit Cat Xịt khử mùi lông chó - combo tiết kiệm', 'products/paddy_068_pate-cho-cho-gran-deli-dang-thach-80g.jpg', 127000, 12,
       'Xịt khử mùi lông chó thuộc nhóm vệ sinh cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 84, 522, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Kit Cat Xịt khử mùi lông chó - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ciao Dung dịch vệ sinh tai cho chó - combo tiết kiệm', 'products/paddy_069_luoc-chai-long-cho-cho-meo-bella.jpg', 120000, 15,
       'Dung dịch vệ sinh tai cho chó thuộc nhóm vệ sinh cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 97, 150, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ciao Dung dịch vệ sinh tai cho chó - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Trixie Tã lót vệ sinh cho chó cái - combo tiết kiệm', 'products/paddy_070_nem-goi-cho-cho-meo-hinh-chim-canh-cut.jpg', 157000, 20,
       'Tã lót vệ sinh cho chó cái thuộc nhóm vệ sinh cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 110, 396, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Trixie Tã lót vệ sinh cho chó cái - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'FOFOS Bóng cao su luyện vận động cho chó - combo tiết kiệm', 'products/paddy_071_sua-tam-cho-cho-bossen-fortis-derm-chong-ngua-250ml.jpg', 45000, 0,
       'Bóng cao su luyện vận động cho chó thuộc nhóm đồ chơi cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 123, 180, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'FOFOS Bóng cao su luyện vận động cho chó - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Doggyman Dây thừng gặm nhai cho chó - combo tiết kiệm', 'products/paddy_072_cat-dau-nanh-cho-meo-on25-mixed-tofu.jpg', 72000, 0,
       'Dây thừng gặm nhai cho chó thuộc nhóm đồ chơi cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 136, 270, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Doggyman Dây thừng gặm nhai cho chó - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Natural Core Thú bông có chuông cho chó - combo tiết kiệm', 'products/paddy_073_hat-cho-cho-mr-vet-d1-cham-soc-he-tieu-hoa.jpg', 103000, 5,
       'Thú bông có chuông cho chó thuộc nhóm đồ chơi cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 33, 348, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Natural Core Thú bông có chuông cho chó - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Joycat Xương gặm đồ chơi cao su - combo tiết kiệm', 'products/paddy_074_hat-cho-cho-giong-nho-di-ung-royal-canin-hypoallergenic-small.jpg', 93000, 10,
       'Xương gặm đồ chơi cao su thuộc nhóm đồ chơi cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 46, 274, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Joycat Xương gặm đồ chơi cao su - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Melipet Vòng cổ và dây dắt cho chó - combo tiết kiệm', 'products/paddy_075_pate-cho-meo-lapaw-dang-thach-70g.jpg', 113000, 12,
       'Vòng cổ và dây dắt cho chó thuộc nhóm phụ kiện cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 59, 330, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Melipet Vòng cổ và dây dắt cho chó - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Royal Canin Dây dắt tự động cho chó - combo tiết kiệm', 'products/paddy_076_hat-cho-meo-on25-cat-dam-cao-32-danh-cho-meo-moi-do-tuoi.jpg', 230000, 15,
       'Dây dắt tự động cho chó thuộc nhóm phụ kiện cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 72, 450, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Royal Canin Dây dắt tự động cho chó - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Whiskas Bát ăn inox chống trượt cho chó - combo tiết kiệm', 'products/paddy_077_snack-cho-cho-lamer-multi-chew-ho-tro-xuong-khop-rang-mieng.jpg', 111000, 20,
       'Bát ăn inox chống trượt cho chó thuộc nhóm phụ kiện cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 85, 324, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Whiskas Bát ăn inox chống trượt cho chó - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pedigree Lồng vận chuyển chó mèo - combo tiết kiệm', 'products/paddy_078_dau-xa-duong-long-cho-cho-meo-tropiclean-spa-nourish.jpg', 520000, 0,
       'Lồng vận chuyển chó mèo thuộc nhóm phụ kiện cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 98, 2088, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pedigree Lồng vận chuyển chó mèo - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'SmartHeart Viên bổ sung canxi cho chó - combo tiết kiệm', 'products/paddy_079_kem-danh-rang-cho-cho-meo-vi-ga-jungle-monster.webp', 132000, 0,
       'Viên bổ sung canxi cho chó thuộc nhóm chăm sóc sức khỏe cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 111, 222, 'Chăm Sóc Sức Khỏe Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'SmartHeart Viên bổ sung canxi cho chó - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Monge Dầu cá hồi omega cho chó - combo tiết kiệm', 'products/paddy_080_xit-thom-mem-long-cho-cho-jungle-monster-pure-soap-silky-mist-150ml.webp', 113000, 5,
       'Dầu cá hồi omega cho chó thuộc nhóm chăm sóc sức khỏe cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản combo tiết kiệm được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 124, 264, 'Chăm Sóc Sức Khỏe Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Monge Dầu cá hồi omega cho chó - combo tiết kiệm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ganador Hạt dinh dưỡng cho mèo trưởng thành - size mini', 'products/paddy_081_sua-tam-xa-cho-cho-meo-tropiclean-2-trong-1-huong-du-du-dua-355ml.jpg', 166000, 10,
       'Hạt dinh dưỡng cho mèo trưởng thành thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 137, 1500, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ganador Hạt dinh dưỡng cho mèo trưởng thành - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Me-O Hạt cho mèo con vị cá hồi - size mini', 'products/paddy_082_sua-tam-cho-cho-tropiclean-perfectfur-cham-soc-long-chuyen-biet-473ml.jpg', 160000, 12,
       'Hạt cho mèo con vị cá hồi thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 34, 1296, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Me-O Hạt cho mèo con vị cá hồi - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Kit Cat Pate mèo vị cá ngừ - size mini', 'products/paddy_083_ta-lot-cho-cho-cai-fofos-diapers.jpg', 60000, 15,
       'Pate mèo vị cá ngừ thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 47, 105, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Kit Cat Pate mèo vị cá ngừ - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ciao Súp thưởng cho mèo vị gà - size mini', 'products/paddy_084_pate-meo-triet-san-royal-canin-indoor-sterilised-85g.jpg', 81000, 20,
       'Súp thưởng cho mèo vị gà thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 60, 90, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ciao Súp thưởng cho mèo vị gà - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Trixie Snack thưởng mềm cho mèo - size mini', 'products/paddy_085_bat-an-cho-cho-meo-cao-richell.jpg', 49000, 0,
       'Snack thưởng mềm cho mèo thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 73, 115, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Trixie Snack thưởng mềm cho mèo - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'FOFOS Sữa không lactose cho mèo - size mini', 'products/paddy_086_banh-thuong-cho-cho-thit-cuon-ca-gooday-80g.webp', 49000, 0,
       'Sữa không lactose cho mèo thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 86, 200, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'FOFOS Sữa không lactose cho mèo - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Doggyman Cát vệ sinh đậu nành khử mùi - size mini', 'products/paddy_087_do-choi-cho-cho-thu-bong-fofos-summer.webp', 139000, 5,
       'Cát vệ sinh đậu nành khử mùi thuộc nhóm vệ sinh cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 99, 2700, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Doggyman Cát vệ sinh đậu nành khử mùi - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Natural Core Cát vệ sinh bentonite vón cục - size mini', 'products/paddy_088_do-choi-cho-cho-thu-bong-fofos-tua-rua.webp', 110000, 10,
       'Cát vệ sinh bentonite vón cục thuộc nhóm vệ sinh cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 112, 5800, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Natural Core Cát vệ sinh bentonite vón cục - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Joycat Xịt khử mùi khu vực vệ sinh mèo - size mini', 'products/paddy_089_pate-cho-ho-tro-tieu-hoa-royal-canin-gastrointestinal-canine-lon-400g.jpg', 127000, 12,
       'Xịt khử mùi khu vực vệ sinh mèo thuộc nhóm vệ sinh cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 125, 558, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Joycat Xịt khử mùi khu vực vệ sinh mèo - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Melipet Khay vệ sinh mèo thành cao - size mini', 'products/paddy_090_long-quay-hat-thong-minh-cho-cho-meo-doggyman.webp', 220000, 15,
       'Khay vệ sinh mèo thành cao thuộc nhóm vệ sinh cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 138, 1320, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Melipet Khay vệ sinh mèo thành cao - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Royal Canin Cần câu lông vũ cho mèo - size mini', 'products/paddy_091_pate-cho-cho-soi-than-royal-canin-urinary-s-o-410g.jpg', 77000, 20,
       'Cần câu lông vũ cho mèo thuộc nhóm đồ chơi cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 35, 80, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Royal Canin Cần câu lông vũ cho mèo - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Whiskas Bàn cào móng giấy cho mèo - size mini', 'products/paddy_092_cat-san-cho-meo-purcats-miracle-2-5kg.webp', 149000, 0,
       'Bàn cào móng giấy cho mèo thuộc nhóm đồ chơi cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 48, 648, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Whiskas Bàn cào móng giấy cho mèo - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pedigree Bóng chuông tương tác cho mèo - size mini', 'products/paddy_093_do-choi-cho-cho-banh-doggyman-hinh-duoi.jpg', 36000, 0,
       'Bóng chuông tương tác cho mèo thuộc nhóm đồ chơi cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 61, 80, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pedigree Bóng chuông tương tác cho mèo - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'SmartHeart Trụ cào móng dây thừng - size mini', 'products/paddy_094_pate-cho-meo-ciao-40g-thai-lan.jpg', 249000, 5,
       'Trụ cào móng dây thừng thuộc nhóm đồ chơi cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 74, 1116, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'SmartHeart Trụ cào móng dây thừng - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Monge Bát ăn inox chống trượt cho mèo - size mini', 'products/paddy_095_thit-vien-cho-cho-natural-lab-hop-1kg.webp', 76000, 10,
       'Bát ăn inox chống trượt cho mèo thuộc nhóm phụ kiện cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 87, 330, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Monge Bát ăn inox chống trượt cho mèo - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ganador Nệm nằm êm ái cho mèo - size mini', 'products/paddy_096_bat-an-cho-cho-meo-bang-nhua-nhieu-kieu-dang.jpg', 217000, 12,
       'Nệm nằm êm ái cho mèo thuộc nhóm phụ kiện cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 100, 800, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ganador Nệm nằm êm ái cho mèo - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Me-O Balo vận chuyển mèo thoáng khí - size mini', 'products/paddy_097_snack-cho-cho-dexinbone-sach-rang.jpg', 420000, 15,
       'Balo vận chuyển mèo thoáng khí thuộc nhóm phụ kiện cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 113, 1296, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Me-O Balo vận chuyển mèo thoáng khí - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Kit Cat Vòng cổ mèo có chuông an toàn - size mini', 'products/paddy_098_pate-cho-cho-kiem-soat-can-nang-royal-canin-satiety-weight-management-lon-410g.jpg', 87000, 20,
       'Vòng cổ mèo có chuông an toàn thuộc nhóm phụ kiện cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 126, 110, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Kit Cat Vòng cổ mèo có chuông an toàn - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ciao Gel dinh dưỡng hỗ trợ búi lông - size mini', 'products/paddy_099_pate-cho-meo-miratorg-thom-ngon-80g.jpg', 115000, 0,
       'Gel dinh dưỡng hỗ trợ búi lông thuộc nhóm chăm sóc sức khỏe cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 139, 150, 'Chăm Sóc Sức Khỏe Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ciao Gel dinh dưỡng hỗ trợ búi lông - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Trixie Viên dầu cá hồi omega cho mèo - size mini', 'products/paddy_100_hat-cho-meo-truong-thanh-miratorg-nhieu-thit-thom-ngon.jpg', 96000, 0,
       'Viên dầu cá hồi omega cho mèo thuộc nhóm chăm sóc sức khỏe cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 36, 264, 'Chăm Sóc Sức Khỏe Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Trixie Viên dầu cá hồi omega cho mèo - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'FOFOS Hạt dinh dưỡng cho chó trưởng thành - size mini', 'products/paddy_001_banh-thuong-cho-meo-uc-ga-hap-nuoc-cot-ga-cattyman.jpg', 179000, 5,
       'Hạt dinh dưỡng cho chó trưởng thành thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 49, 1500, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'FOFOS Hạt dinh dưỡng cho chó trưởng thành - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Doggyman Hạt cho chó con giống nhỏ - size mini', 'products/paddy_002_que-gan-bo-cho-cho-doggyman-100g.jpg', 179000, 10,
       'Hạt cho chó con giống nhỏ thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 62, 1404, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Doggyman Hạt cho chó con giống nhỏ - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Natural Core Pate chó vị bò và rau củ - size mini', 'products/paddy_003_vong-co-va-day-dat-cho-cho-police.jpg', 56000, 12,
       'Pate chó vị bò và rau củ thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 75, 150, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Natural Core Pate chó vị bò và rau củ - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Joycat Snack que gặm sạch răng cho chó - size mini', 'products/paddy_004_xuc-xich-cho-cho-ga-mini-doggyman.jpg', 94000, 15,
       'Snack que gặm sạch răng cho chó thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 88, 150, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Joycat Snack que gặm sạch răng cho chó - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Melipet Xúc xích thưởng vị gà cho chó - size mini', 'products/paddy_005_hat-sieu-topping-cho-meo-kings-pet.jpg', 78000, 20,
       'Xúc xích thưởng vị gà cho chó thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 101, 140, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Melipet Xúc xích thưởng vị gà cho chó - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Royal Canin Thịt sấy thưởng huấn luyện cho chó - size mini', 'products/paddy_006_long-van-chuyen-hang-khong-cho-cho-mon-ami-carrier.jpg', 78000, 0,
       'Thịt sấy thưởng huấn luyện cho chó thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 114, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Royal Canin Thịt sấy thưởng huấn luyện cho chó - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Whiskas Sữa tắm dưỡng lông cho chó - size mini', 'products/paddy_007_hat-cho-meo-5plus-catmix-ruoc-ga-say-kho.jpg', 142000, 0,
       'Sữa tắm dưỡng lông cho chó thuộc nhóm vệ sinh cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 127, 383, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Whiskas Sữa tắm dưỡng lông cho chó - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pedigree Xịt khử mùi lông chó - size mini', 'products/paddy_008_bot-vi-sinh-khu-mui-cho-cho-meo-joycat-54g.jpg', 113000, 5,
       'Xịt khử mùi lông chó thuộc nhóm vệ sinh cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 140, 522, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pedigree Xịt khử mùi lông chó - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'SmartHeart Dung dịch vệ sinh tai cho chó - size mini', 'products/paddy_009_xit-vi-sinh-khu-mui-cho-cho-meo-joycat-450ml.jpg', 106000, 10,
       'Dung dịch vệ sinh tai cho chó thuộc nhóm vệ sinh cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 37, 150, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'SmartHeart Dung dịch vệ sinh tai cho chó - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Monge Tã lót vệ sinh cho chó cái - size mini', 'products/paddy_010_hat-cho-meo-nutri-plan-plus-1-5kg.jpg', 143000, 12,
       'Tã lót vệ sinh cho chó cái thuộc nhóm vệ sinh cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 50, 396, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Monge Tã lót vệ sinh cho chó cái - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ganador Bóng cao su luyện vận động cho chó - size mini', 'products/paddy_011_cat-ve-sinh-cho-meo-joycat.jpg', 80000, 15,
       'Bóng cao su luyện vận động cho chó thuộc nhóm đồ chơi cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 63, 180, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ganador Bóng cao su luyện vận động cho chó - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Me-O Dây thừng gặm nhai cho chó - size mini', 'products/paddy_012_hat-cho-meo-wanpy-mix-thit-vien.jpg', 107000, 20,
       'Dây thừng gặm nhai cho chó thuộc nhóm đồ chơi cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 76, 270, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Me-O Dây thừng gặm nhai cho chó - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Kit Cat Thú bông có chuông cho chó - size mini', 'products/paddy_013_hat-cho-cho-smartheart-gold-indoor.jpg', 89000, 0,
       'Thú bông có chuông cho chó thuộc nhóm đồ chơi cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 89, 348, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Kit Cat Thú bông có chuông cho chó - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ciao Xương gặm đồ chơi cao su - size mini', 'products/paddy_014_hat-cho-cho-truong-thanh-happy-tummy.jpg', 79000, 0,
       'Xương gặm đồ chơi cao su thuộc nhóm đồ chơi cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 102, 274, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ciao Xương gặm đồ chơi cao su - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Trixie Vòng cổ và dây dắt cho chó - size mini', 'products/paddy_015_hat-cho-meo-truong-thanh-happy-tummy.jpg', 99000, 5,
       'Vòng cổ và dây dắt cho chó thuộc nhóm phụ kiện cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 115, 330, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Trixie Vòng cổ và dây dắt cho chó - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'FOFOS Dây dắt tự động cho chó - size mini', 'products/paddy_016_hat-cho-meo-excel-vi-ca-thom-ngon.jpg', 216000, 10,
       'Dây dắt tự động cho chó thuộc nhóm phụ kiện cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 128, 450, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'FOFOS Dây dắt tự động cho chó - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Doggyman Bát ăn inox chống trượt cho chó - size mini', 'products/paddy_017_banh-thuong-cho-cho-meo-moi-lua-tuoi-dexinbone.jpg', 97000, 12,
       'Bát ăn inox chống trượt cho chó thuộc nhóm phụ kiện cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 25, 324, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Doggyman Bát ăn inox chống trượt cho chó - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Natural Core Lồng vận chuyển chó mèo - size mini', 'products/paddy_018_pate-cho-cho-moi-lua-tuoi-lapaw-375g.webp', 555000, 15,
       'Lồng vận chuyển chó mèo thuộc nhóm phụ kiện cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 38, 2088, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Natural Core Lồng vận chuyển chó mèo - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Joycat Viên bổ sung canxi cho chó - size mini', 'products/paddy_019_pate-cho-meo-on25-80g.jpg', 167000, 20,
       'Viên bổ sung canxi cho chó thuộc nhóm chăm sóc sức khỏe cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 51, 222, 'Chăm Sóc Sức Khỏe Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Joycat Viên bổ sung canxi cho chó - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Melipet Dầu cá hồi omega cho chó - size mini', 'products/paddy_020_hat-cho-cho-moi-lua-tuoi-nutrience-subzero-prairier-red.jpg', 99000, 0,
       'Dầu cá hồi omega cho chó thuộc nhóm chăm sóc sức khỏe cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size mini được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 64, 264, 'Chăm Sóc Sức Khỏe Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Melipet Dầu cá hồi omega cho chó - size mini');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Royal Canin Hạt dinh dưỡng cho mèo trưởng thành - size lớn', 'products/paddy_021_hat-cho-cho-on25-dog.jpg', 152000, 0,
       'Hạt dinh dưỡng cho mèo trưởng thành thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 77, 1500, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Royal Canin Hạt dinh dưỡng cho mèo trưởng thành - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Whiskas Hạt cho mèo con vị cá hồi - size lớn', 'products/paddy_022_sua-de-cho-meo-kit-cat-khong-lactose.jpg', 146000, 5,
       'Hạt cho mèo con vị cá hồi thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 90, 1296, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Whiskas Hạt cho mèo con vị cá hồi - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pedigree Pate mèo vị cá ngừ - size lớn', 'products/paddy_023_banh-thuong-cho-cho-natural-core-sun-ga-55g.png', 46000, 10,
       'Pate mèo vị cá ngừ thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 103, 105, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pedigree Pate mèo vị cá ngừ - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'SmartHeart Súp thưởng cho mèo vị gà - size lớn', 'products/paddy_024_day-xich-cho-cho-mon-ami-3x120cm.png', 67000, 12,
       'Súp thưởng cho mèo vị gà thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 116, 90, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'SmartHeart Súp thưởng cho mèo vị gà - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Monge Snack thưởng mềm cho mèo - size lớn', 'products/paddy_025_vien-dau-ca-hoi-omega-cho-meo-hop-60-vien.jpg', 84000, 15,
       'Snack thưởng mềm cho mèo thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 129, 115, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Monge Snack thưởng mềm cho mèo - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ganador Sữa không lactose cho mèo - size lớn', 'products/paddy_026_banh-thuong-cho-cho-inu-fonti-xuong-gam-ban-chai-90g.jpg', 84000, 20,
       'Sữa không lactose cho mèo thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 26, 200, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ganador Sữa không lactose cho mèo - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Me-O Cát vệ sinh đậu nành khử mùi - size lớn', 'products/paddy_027_pate-meo-the-pet-cho-meo-1kg.jpg', 125000, 0,
       'Cát vệ sinh đậu nành khử mùi thuộc nhóm vệ sinh cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 39, 2700, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Me-O Cát vệ sinh đậu nành khử mùi - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Kit Cat Cát vệ sinh bentonite vón cục - size lớn', 'products/paddy_028_bat-an-cho-cho-meo-inox-bowl-mon-ami.png', 96000, 0,
       'Cát vệ sinh bentonite vón cục thuộc nhóm vệ sinh cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 52, 5800, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Kit Cat Cát vệ sinh bentonite vón cục - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ciao Xịt khử mùi khu vực vệ sinh mèo - size lớn', 'products/paddy_029_balo-van-chuyen-cho-cho-meo-da-cao-cap-diamond.jpg', 113000, 5,
       'Xịt khử mùi khu vực vệ sinh mèo thuộc nhóm vệ sinh cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 65, 558, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ciao Xịt khử mùi khu vực vệ sinh mèo - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Trixie Khay vệ sinh mèo thành cao - size lớn', 'products/paddy_030_banh-thuong-thit-say-cho-meo-catsdo.jpg', 206000, 10,
       'Khay vệ sinh mèo thành cao thuộc nhóm vệ sinh cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 78, 1320, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Trixie Khay vệ sinh mèo thành cao - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'FOFOS Cần câu lông vũ cho mèo - size lớn', 'products/paddy_031_nem-cho-cho-meo-pupdy-floating-mattress.jpg', 63000, 12,
       'Cần câu lông vũ cho mèo thuộc nhóm đồ chơi cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 91, 80, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'FOFOS Cần câu lông vũ cho mèo - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Doggyman Bàn cào móng giấy cho mèo - size lớn', 'products/paddy_032_banh-thuong-cho-cho-jireho.jpg', 184000, 15,
       'Bàn cào móng giấy cho mèo thuộc nhóm đồ chơi cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 104, 648, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Doggyman Bàn cào móng giấy cho mèo - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Natural Core Bóng chuông tương tác cho mèo - size lớn', 'products/paddy_033_pate-cho-meo-alpha-pet-70g.jpg', 71000, 20,
       'Bóng chuông tương tác cho mèo thuộc nhóm đồ chơi cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 117, 80, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Natural Core Bóng chuông tương tác cho mèo - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Joycat Trụ cào móng dây thừng - size lớn', 'products/paddy_034_pate-cho-meo-real-raw-catidea-170g.jpg', 235000, 0,
       'Trụ cào móng dây thừng thuộc nhóm đồ chơi cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 130, 1116, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Joycat Trụ cào móng dây thừng - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Melipet Bát ăn inox chống trượt cho mèo - size lớn', 'products/paddy_035_cao-mong-giay-cho-meo-moi-lua-tuoi-fofos.jpg', 62000, 0,
       'Bát ăn inox chống trượt cho mèo thuộc nhóm phụ kiện cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 27, 330, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Melipet Bát ăn inox chống trượt cho mèo - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Royal Canin Nệm nằm êm ái cho mèo - size lớn', 'products/paddy_036_do-choi-day-thung-cho-cho-fofos.jpg', 203000, 5,
       'Nệm nằm êm ái cho mèo thuộc nhóm phụ kiện cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 40, 800, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Royal Canin Nệm nằm êm ái cho mèo - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Whiskas Balo vận chuyển mèo thoáng khí - size lớn', 'products/paddy_037_do-choi-meo-fofos-set-6-mon.jpg', 406000, 10,
       'Balo vận chuyển mèo thoáng khí thuộc nhóm phụ kiện cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 53, 1296, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Whiskas Balo vận chuyển mèo thoáng khí - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pedigree Vòng cổ mèo có chuông an toàn - size lớn', 'products/paddy_038_pate-cho-meo-whiskas-80g-hop-6-goi.jpg', 73000, 12,
       'Vòng cổ mèo có chuông an toàn thuộc nhóm phụ kiện cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 66, 110, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pedigree Vòng cổ mèo có chuông an toàn - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'SmartHeart Gel dinh dưỡng hỗ trợ búi lông - size lớn', 'products/paddy_039_xit-khu-mui-cat-ve-sinh-cho-meo-arm-hammer-636ml.jpg', 150000, 15,
       'Gel dinh dưỡng hỗ trợ búi lông thuộc nhóm chăm sóc sức khỏe cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 79, 150, 'Chăm Sóc Sức Khỏe Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'SmartHeart Gel dinh dưỡng hỗ trợ búi lông - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Monge Viên dầu cá hồi omega cho mèo - size lớn', 'products/paddy_040_hat-cho-meo-moi-lua-tuoi-todays-dinner.jpg', 131000, 20,
       'Viên dầu cá hồi omega cho mèo thuộc nhóm chăm sóc sức khỏe cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 92, 264, 'Chăm Sóc Sức Khỏe Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Monge Viên dầu cá hồi omega cho mèo - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ganador Hạt dinh dưỡng cho chó trưởng thành - size lớn', 'products/paddy_041_vong-go-cho-meo-catca.jpg', 165000, 0,
       'Hạt dinh dưỡng cho chó trưởng thành thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 105, 1500, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ganador Hạt dinh dưỡng cho chó trưởng thành - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Me-O Hạt cho chó con giống nhỏ - size lớn', 'products/paddy_042_pate-cho-cho-pedigree-80g.jpg', 165000, 0,
       'Hạt cho chó con giống nhỏ thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 118, 1404, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Me-O Hạt cho chó con giống nhỏ - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Kit Cat Pate chó vị bò và rau củ - size lớn', 'products/paddy_043_hat-cho-cho-truong-thanh-gran-deli-700g.jpg', 42000, 5,
       'Pate chó vị bò và rau củ thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 131, 150, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Kit Cat Pate chó vị bò và rau củ - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ciao Snack que gặm sạch răng cho chó - size lớn', 'products/paddy_044_gel-dinh-duong-chuc-nang-cho-meo-kit-cat.jpg', 80000, 10,
       'Snack que gặm sạch răng cho chó thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 28, 150, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ciao Snack que gặm sạch răng cho chó - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Trixie Xúc xích thưởng vị gà cho chó - size lớn', 'products/paddy_045_banh-thuong-cho-meo-temptations-thom-ngon-75g.png', 64000, 12,
       'Xúc xích thưởng vị gà cho chó thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 41, 140, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Trixie Xúc xích thưởng vị gà cho chó - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'FOFOS Thịt sấy thưởng huấn luyện cho chó - size lớn', 'products/paddy_046_may-don-phan-tu-dong-cho-meo-petree-version-2.jpg', 113000, 15,
       'Thịt sấy thưởng huấn luyện cho chó thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 54, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'FOFOS Thịt sấy thưởng huấn luyện cho chó - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Doggyman Sữa tắm dưỡng lông cho chó - size lớn', 'products/paddy_047_may-don-phan-tu-dong-cho-meo-neakasa.jpg', 177000, 20,
       'Sữa tắm dưỡng lông cho chó thuộc nhóm vệ sinh cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 67, 383, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Doggyman Sữa tắm dưỡng lông cho chó - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Natural Core Xịt khử mùi lông chó - size lớn', 'products/paddy_048_sup-thuong-cho-meo-bite-of-wild-15gx4.jpg', 99000, 0,
       'Xịt khử mùi lông chó thuộc nhóm vệ sinh cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 80, 522, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Natural Core Xịt khử mùi lông chó - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Joycat Dung dịch vệ sinh tai cho chó - size lớn', 'products/paddy_049_snack-ga-say-cho-cho-doggyman-pawfect-choice-180g.jpg', 92000, 0,
       'Dung dịch vệ sinh tai cho chó thuộc nhóm vệ sinh cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 93, 150, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Joycat Dung dịch vệ sinh tai cho chó - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Melipet Tã lót vệ sinh cho chó cái - size lớn', 'products/paddy_050_hat-cho-meo-moi-lua-tuoi-cattyman.jpg', 129000, 5,
       'Tã lót vệ sinh cho chó cái thuộc nhóm vệ sinh cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 106, 396, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Melipet Tã lót vệ sinh cho chó cái - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Royal Canin Bóng cao su luyện vận động cho chó - size lớn', 'products/paddy_051_hat-cho-meo-moi-lua-tuoi-s2pet-v1-v2-v3.jpg', 66000, 10,
       'Bóng cao su luyện vận động cho chó thuộc nhóm đồ chơi cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 119, 180, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Royal Canin Bóng cao su luyện vận động cho chó - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Whiskas Dây thừng gặm nhai cho chó - size lớn', 'products/paddy_052_pate-cho-meo-cao-cap-bite-of-wild-tui-70g.jpg', 93000, 12,
       'Dây thừng gặm nhai cho chó thuộc nhóm đồ chơi cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 132, 270, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Whiskas Dây thừng gặm nhai cho chó - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pedigree Thú bông có chuông cho chó - size lớn', 'products/paddy_053_sua-tuoi-kyushu-cho-cho-meo-khong-chua-lactose-200ml.jpg', 124000, 15,
       'Thú bông có chuông cho chó thuộc nhóm đồ chơi cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 29, 348, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pedigree Thú bông có chuông cho chó - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'SmartHeart Xương gặm đồ chơi cao su - size lớn', 'products/paddy_054_hat-cho-meo-cature-easy-farm-topping-1-5kg.jpg', 114000, 20,
       'Xương gặm đồ chơi cao su thuộc nhóm đồ chơi cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 42, 274, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'SmartHeart Xương gặm đồ chơi cao su - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Monge Vòng cổ và dây dắt cho chó - size lớn', 'products/paddy_055_khay-ve-sinh-cho-meo-mon-ami-cao-cap.jpg', 85000, 0,
       'Vòng cổ và dây dắt cho chó thuộc nhóm phụ kiện cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 55, 330, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Monge Vòng cổ và dây dắt cho chó - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ganador Dây dắt tự động cho chó - size lớn', 'products/paddy_056_sup-thuong-cho-cho-meo-vfcore-chuc-nang.jpg', 202000, 0,
       'Dây dắt tự động cho chó thuộc nhóm phụ kiện cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 68, 450, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ganador Dây dắt tự động cho chó - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Me-O Bát ăn inox chống trượt cho chó - size lớn', 'products/paddy_057_dung-dich-ve-sinh-tai-cho-cho-meo-jungle-monster-chamomile-120ml.jpg', 83000, 5,
       'Bát ăn inox chống trượt cho chó thuộc nhóm phụ kiện cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 81, 324, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Me-O Bát ăn inox chống trượt cho chó - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Kit Cat Lồng vận chuyển chó mèo - size lớn', 'products/paddy_058_xit-thom-mieng-cho-cho-meo-jungle-monster-dental-spray-30ml.jpg', 541000, 10,
       'Lồng vận chuyển chó mèo thuộc nhóm phụ kiện cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 94, 2088, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Kit Cat Lồng vận chuyển chó mèo - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ciao Viên bổ sung canxi cho chó - size lớn', 'products/paddy_059_pate-cho-meo-truong-thanh-me-o-delite-goi-70g.jpg', 153000, 12,
       'Viên bổ sung canxi cho chó thuộc nhóm chăm sóc sức khỏe cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 107, 222, 'Chăm Sóc Sức Khỏe Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ciao Viên bổ sung canxi cho chó - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Trixie Dầu cá hồi omega cho chó - size lớn', 'products/paddy_060_day-dat-bam-tu-dong-cho-cho-flexi.jpg', 134000, 15,
       'Dầu cá hồi omega cho chó thuộc nhóm chăm sóc sức khỏe cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản size lớn được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 120, 264, 'Chăm Sóc Sức Khỏe Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Trixie Dầu cá hồi omega cho chó - size lớn');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'FOFOS Hạt dinh dưỡng cho mèo trưởng thành - hương vị mới', 'products/paddy_061_hat-cho-meo-smartheart-cat.jpg', 187000, 20,
       'Hạt dinh dưỡng cho mèo trưởng thành thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 133, 1500, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'FOFOS Hạt dinh dưỡng cho mèo trưởng thành - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Doggyman Hạt cho mèo con vị cá hồi - hương vị mới', 'products/paddy_062_do-choi-cho-meo-fofos-long-vu.webp', 132000, 0,
       'Hạt cho mèo con vị cá hồi thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 30, 1296, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Doggyman Hạt cho mèo con vị cá hồi - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Natural Core Pate mèo vị cá ngừ - hương vị mới', 'products/paddy_063_cao-mong-meo-fofos-hinh-ly-cafe-42x35cm.webp', 32000, 0,
       'Pate mèo vị cá ngừ thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 43, 105, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Natural Core Pate mèo vị cá ngừ - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Joycat Súp thưởng cho mèo vị gà - hương vị mới', 'products/paddy_064_cao-mong-meo-fofos-hinh-trai-dau-35x30cm.jpg', 53000, 5,
       'Súp thưởng cho mèo vị gà thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 56, 90, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Joycat Súp thưởng cho mèo vị gà - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Melipet Snack thưởng mềm cho mèo - hương vị mới', 'products/paddy_065_hat-cho-cho-wanpy-khong-ngu-coc-mix-thit-vien.jpg', 70000, 10,
       'Snack thưởng mềm cho mèo thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 69, 115, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Melipet Snack thưởng mềm cho mèo - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Royal Canin Sữa không lactose cho mèo - hương vị mới', 'products/paddy_066_sup-thuong-cho-meo-silver-spoon-thailand-56g-4-tuyp.jpg', 70000, 12,
       'Sữa không lactose cho mèo thuộc nhóm thức ăn cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 82, 200, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Royal Canin Sữa không lactose cho mèo - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Whiskas Cát vệ sinh đậu nành khử mùi - hương vị mới', 'products/paddy_067_banh-thuong-cho-cho-que-dai-da-bo-doggyman-white-dental-120g.jpg', 160000, 15,
       'Cát vệ sinh đậu nành khử mùi thuộc nhóm vệ sinh cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 95, 2700, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Whiskas Cát vệ sinh đậu nành khử mùi - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pedigree Cát vệ sinh bentonite vón cục - hương vị mới', 'products/paddy_068_pate-cho-cho-gran-deli-dang-thach-80g.jpg', 131000, 20,
       'Cát vệ sinh bentonite vón cục thuộc nhóm vệ sinh cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 108, 5800, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pedigree Cát vệ sinh bentonite vón cục - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'SmartHeart Xịt khử mùi khu vực vệ sinh mèo - hương vị mới', 'products/paddy_069_luoc-chai-long-cho-cho-meo-bella.jpg', 99000, 0,
       'Xịt khử mùi khu vực vệ sinh mèo thuộc nhóm vệ sinh cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 121, 558, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'SmartHeart Xịt khử mùi khu vực vệ sinh mèo - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Monge Khay vệ sinh mèo thành cao - hương vị mới', 'products/paddy_070_nem-goi-cho-cho-meo-hinh-chim-canh-cut.jpg', 192000, 0,
       'Khay vệ sinh mèo thành cao thuộc nhóm vệ sinh cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 134, 1320, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Monge Khay vệ sinh mèo thành cao - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ganador Cần câu lông vũ cho mèo - hương vị mới', 'products/paddy_071_sua-tam-cho-cho-bossen-fortis-derm-chong-ngua-250ml.jpg', 49000, 5,
       'Cần câu lông vũ cho mèo thuộc nhóm đồ chơi cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 31, 80, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ganador Cần câu lông vũ cho mèo - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Me-O Bàn cào móng giấy cho mèo - hương vị mới', 'products/paddy_072_cat-dau-nanh-cho-meo-on25-mixed-tofu.jpg', 170000, 10,
       'Bàn cào móng giấy cho mèo thuộc nhóm đồ chơi cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 44, 648, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Me-O Bàn cào móng giấy cho mèo - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Kit Cat Bóng chuông tương tác cho mèo - hương vị mới', 'products/paddy_073_hat-cho-cho-mr-vet-d1-cham-soc-he-tieu-hoa.jpg', 57000, 12,
       'Bóng chuông tương tác cho mèo thuộc nhóm đồ chơi cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 57, 80, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Kit Cat Bóng chuông tương tác cho mèo - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ciao Trụ cào móng dây thừng - hương vị mới', 'products/paddy_074_hat-cho-cho-giong-nho-di-ung-royal-canin-hypoallergenic-small.jpg', 270000, 15,
       'Trụ cào móng dây thừng thuộc nhóm đồ chơi cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 70, 1116, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ciao Trụ cào móng dây thừng - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Trixie Bát ăn inox chống trượt cho mèo - hương vị mới', 'products/paddy_075_pate-cho-meo-lapaw-dang-thach-70g.jpg', 97000, 20,
       'Bát ăn inox chống trượt cho mèo thuộc nhóm phụ kiện cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 83, 330, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Trixie Bát ăn inox chống trượt cho mèo - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'FOFOS Nệm nằm êm ái cho mèo - hương vị mới', 'products/paddy_076_hat-cho-meo-on25-cat-dam-cao-32-danh-cho-meo-moi-do-tuoi.jpg', 189000, 0,
       'Nệm nằm êm ái cho mèo thuộc nhóm phụ kiện cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 96, 800, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'FOFOS Nệm nằm êm ái cho mèo - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Doggyman Balo vận chuyển mèo thoáng khí - hương vị mới', 'products/paddy_077_snack-cho-cho-lamer-multi-chew-ho-tro-xuong-khop-rang-mieng.jpg', 392000, 0,
       'Balo vận chuyển mèo thoáng khí thuộc nhóm phụ kiện cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 109, 1296, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Doggyman Balo vận chuyển mèo thoáng khí - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Natural Core Vòng cổ mèo có chuông an toàn - hương vị mới', 'products/paddy_078_dau-xa-duong-long-cho-cho-meo-tropiclean-spa-nourish.jpg', 59000, 5,
       'Vòng cổ mèo có chuông an toàn thuộc nhóm phụ kiện cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 122, 110, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Natural Core Vòng cổ mèo có chuông an toàn - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Joycat Gel dinh dưỡng hỗ trợ búi lông - hương vị mới', 'products/paddy_079_kem-danh-rang-cho-cho-meo-vi-ga-jungle-monster.webp', 136000, 10,
       'Gel dinh dưỡng hỗ trợ búi lông thuộc nhóm chăm sóc sức khỏe cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 135, 150, 'Chăm Sóc Sức Khỏe Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Joycat Gel dinh dưỡng hỗ trợ búi lông - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Melipet Viên dầu cá hồi omega cho mèo - hương vị mới', 'products/paddy_080_xit-thom-mem-long-cho-cho-jungle-monster-pure-soap-silky-mist-150ml.webp', 117000, 12,
       'Viên dầu cá hồi omega cho mèo thuộc nhóm chăm sóc sức khỏe cho mèo, phù hợp cho mèo dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 32, 264, 'Chăm Sóc Sức Khỏe Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Melipet Viên dầu cá hồi omega cho mèo - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Royal Canin Hạt dinh dưỡng cho chó trưởng thành - hương vị mới', 'products/paddy_081_sua-tam-xa-cho-cho-meo-tropiclean-2-trong-1-huong-du-du-dua-355ml.jpg', 200000, 15,
       'Hạt dinh dưỡng cho chó trưởng thành thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 45, 1500, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Royal Canin Hạt dinh dưỡng cho chó trưởng thành - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Whiskas Hạt cho chó con giống nhỏ - hương vị mới', 'products/paddy_082_sua-tam-cho-cho-tropiclean-perfectfur-cham-soc-long-chuyen-biet-473ml.jpg', 200000, 20,
       'Hạt cho chó con giống nhỏ thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 58, 1404, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Whiskas Hạt cho chó con giống nhỏ - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pedigree Pate chó vị bò và rau củ - hương vị mới', 'products/paddy_083_ta-lot-cho-cho-cai-fofos-diapers.jpg', 28000, 0,
       'Pate chó vị bò và rau củ thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 71, 150, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pedigree Pate chó vị bò và rau củ - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'SmartHeart Snack que gặm sạch răng cho chó - hương vị mới', 'products/paddy_084_pate-meo-triet-san-royal-canin-indoor-sterilised-85g.jpg', 66000, 0,
       'Snack que gặm sạch răng cho chó thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 84, 150, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'SmartHeart Snack que gặm sạch răng cho chó - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Monge Xúc xích thưởng vị gà cho chó - hương vị mới', 'products/paddy_085_bat-an-cho-cho-meo-cao-richell.jpg', 50000, 5,
       'Xúc xích thưởng vị gà cho chó thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 97, 140, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Monge Xúc xích thưởng vị gà cho chó - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ganador Thịt sấy thưởng huấn luyện cho chó - hương vị mới', 'products/paddy_086_banh-thuong-cho-cho-thit-cuon-ca-gooday-80g.webp', 99000, 10,
       'Thịt sấy thưởng huấn luyện cho chó thuộc nhóm thức ăn cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 110, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ganador Thịt sấy thưởng huấn luyện cho chó - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Me-O Sữa tắm dưỡng lông cho chó - hương vị mới', 'products/paddy_087_do-choi-cho-cho-thu-bong-fofos-summer.webp', 163000, 12,
       'Sữa tắm dưỡng lông cho chó thuộc nhóm vệ sinh cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 123, 383, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Me-O Sữa tắm dưỡng lông cho chó - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Kit Cat Xịt khử mùi lông chó - hương vị mới', 'products/paddy_088_do-choi-cho-cho-thu-bong-fofos-tua-rua.webp', 134000, 15,
       'Xịt khử mùi lông chó thuộc nhóm vệ sinh cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 136, 522, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Kit Cat Xịt khử mùi lông chó - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Ciao Dung dịch vệ sinh tai cho chó - hương vị mới', 'products/paddy_089_pate-cho-ho-tro-tieu-hoa-royal-canin-gastrointestinal-canine-lon-400g.jpg', 127000, 20,
       'Dung dịch vệ sinh tai cho chó thuộc nhóm vệ sinh cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 33, 150, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ciao Dung dịch vệ sinh tai cho chó - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Trixie Tã lót vệ sinh cho chó cái - hương vị mới', 'products/paddy_090_long-quay-hat-thong-minh-cho-cho-meo-doggyman.webp', 115000, 0,
       'Tã lót vệ sinh cho chó cái thuộc nhóm vệ sinh cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 46, 396, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Trixie Tã lót vệ sinh cho chó cái - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'FOFOS Bóng cao su luyện vận động cho chó - hương vị mới', 'products/paddy_091_pate-cho-cho-soi-than-royal-canin-urinary-s-o-410g.jpg', 52000, 0,
       'Bóng cao su luyện vận động cho chó thuộc nhóm đồ chơi cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 59, 180, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'FOFOS Bóng cao su luyện vận động cho chó - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Doggyman Dây thừng gặm nhai cho chó - hương vị mới', 'products/paddy_092_cat-san-cho-meo-purcats-miracle-2-5kg.webp', 79000, 5,
       'Dây thừng gặm nhai cho chó thuộc nhóm đồ chơi cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 72, 270, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Doggyman Dây thừng gặm nhai cho chó - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Natural Core Thú bông có chuông cho chó - hương vị mới', 'products/paddy_093_do-choi-cho-cho-banh-doggyman-hinh-duoi.jpg', 110000, 10,
       'Thú bông có chuông cho chó thuộc nhóm đồ chơi cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 85, 348, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Natural Core Thú bông có chuông cho chó - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Joycat Xương gặm đồ chơi cao su - hương vị mới', 'products/paddy_094_pate-cho-meo-ciao-40g-thai-lan.jpg', 100000, 12,
       'Xương gặm đồ chơi cao su thuộc nhóm đồ chơi cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 98, 274, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Joycat Xương gặm đồ chơi cao su - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Melipet Vòng cổ và dây dắt cho chó - hương vị mới', 'products/paddy_095_thit-vien-cho-cho-natural-lab-hop-1kg.webp', 120000, 15,
       'Vòng cổ và dây dắt cho chó thuộc nhóm phụ kiện cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 111, 330, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Melipet Vòng cổ và dây dắt cho chó - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Royal Canin Dây dắt tự động cho chó - hương vị mới', 'products/paddy_096_bat-an-cho-cho-meo-bang-nhua-nhieu-kieu-dang.jpg', 237000, 20,
       'Dây dắt tự động cho chó thuộc nhóm phụ kiện cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 124, 450, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Royal Canin Dây dắt tự động cho chó - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Whiskas Bát ăn inox chống trượt cho chó - hương vị mới', 'products/paddy_097_snack-cho-cho-dexinbone-sach-rang.jpg', 69000, 0,
       'Bát ăn inox chống trượt cho chó thuộc nhóm phụ kiện cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 137, 324, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Whiskas Bát ăn inox chống trượt cho chó - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pedigree Lồng vận chuyển chó mèo - hương vị mới', 'products/paddy_098_pate-cho-cho-kiem-soat-can-nang-royal-canin-satiety-weight-management-lon-410g.jpg', 527000, 0,
       'Lồng vận chuyển chó mèo thuộc nhóm phụ kiện cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 34, 2088, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pedigree Lồng vận chuyển chó mèo - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'SmartHeart Viên bổ sung canxi cho chó - hương vị mới', 'products/paddy_099_pate-cho-meo-miratorg-thom-ngon-80g.jpg', 139000, 5,
       'Viên bổ sung canxi cho chó thuộc nhóm chăm sóc sức khỏe cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 47, 222, 'Chăm Sóc Sức Khỏe Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'SmartHeart Viên bổ sung canxi cho chó - hương vị mới');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Monge Dầu cá hồi omega cho chó - hương vị mới', 'products/paddy_100_hat-cho-meo-truong-thanh-miratorg-nhieu-thit-thom-ngon.jpg', 120000, 10,
       'Dầu cá hồi omega cho chó thuộc nhóm chăm sóc sức khỏe cho chó, phù hợp cho chó dùng hằng ngày. Phiên bản hương vị mới được chọn để đa dạng danh mục sản phẩm Melipet, giúp khách dễ tìm đúng nhu cầu chăm sóc thú cưng. Thông tin sản phẩm có thể dùng cho đồ án/thực tế MVP; khi kinh doanh thật nên cập nhật tồn kho, giá bán và mô tả theo nhà cung cấp chính thức.', 60, 264, 'Chăm Sóc Sức Khỏe Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Monge Dầu cá hồi omega cho chó - hương vị mới');
