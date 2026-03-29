USE `petvaccine`;

-- Bỏ qua ALTER TABLE vì cột category đã tồn tại
-- Cập nhật category cho sản phẩm hiện có
UPDATE `products` SET `category` = 'Thức Ăn Cho Mèo' WHERE id IN (1,2,3,4,5);

-- Thêm sản phẩm mẫu cho các danh mục
INSERT INTO `products` (`name`, `image`, `price`, `old_price`, `discount`, `description`, `category`) VALUES
-- Thức Ăn Cho Chó
('Thức Ăn Hạt Cho Chó Trưởng Thành Pedigree 1.5kg', 'prod_dog_food1.jpg', 95000, 120000, 20, 'Thức ăn hạt dinh dưỡng cho chó trưởng thành', 'Thức Ăn Cho Chó'),
('Thức Ăn Hạt SmartHeart Cho Chó Con 1.5kg', 'prod_dog_food2.jpg', 85000, 0, 0, 'Thức ăn hạt cho chó con từ 2-12 tháng', 'Thức Ăn Cho Chó'),
('Thức Ăn Hạt Royal Canin Mini Adult 2kg', 'prod_dog_food3.jpg', 245000, 280000, 12, 'Dành cho chó nhỏ trưởng thành', 'Thức Ăn Cho Chó'),

-- Sữa Cho Chó
('Sữa Bột Bio Milk Cho Chó Con 100g', 'prod_dog_milk1.jpg', 65000, 0, 0, 'Sữa bột dinh dưỡng cho chó con', 'Sữa Cho Chó'),
('Sữa Tươi Pet Milk Cho Chó 200ml', 'prod_dog_milk2.jpg', 35000, 45000, 22, 'Sữa tươi bổ sung canxi cho chó', 'Sữa Cho Chó'),

-- Chăm Sóc Sức Khoẻ Cho Chó
('Vitamin Bổ Sung Cho Chó Nutri-Vet 60 viên', 'prod_dog_health1.jpg', 180000, 0, 0, 'Vitamin tổng hợp cho chó', 'Chăm Sóc Sức Khoẻ Cho Chó'),
('Thuốc Xổ Giun Cho Chó Drontal Plus', 'prod_dog_health2.jpg', 45000, 55000, 18, 'Thuốc xổ giun hiệu quả cho chó', 'Chăm Sóc Sức Khoẻ Cho Chó'),

-- Dụng Cụ Ăn Uống Cho Chó
('Bát Ăn Inox Chống Lật Cho Chó', 'prod_dog_bowl1.jpg', 55000, 0, 0, 'Bát ăn inox chống lật, dễ vệ sinh', 'Dụng Cụ Ăn Uống Cho Chó'),
('Bình Nước Tự Động Cho Chó 2.5L', 'prod_dog_bowl2.jpg', 120000, 150000, 20, 'Bình nước tự động tiện lợi', 'Dụng Cụ Ăn Uống Cho Chó'),

-- Đồ Chơi - Huấn Luyện Cho Chó
('Bóng Cao Su Cho Chó Gặm', 'prod_dog_toy1.jpg', 35000, 0, 0, 'Bóng cao su bền, an toàn cho chó', 'Đồ Chơi - Huấn Luyện Cho Chó'),
('Dây Dắt Chó Tự Cuốn 5m', 'prod_dog_toy2.jpg', 150000, 180000, 17, 'Dây dắt tự cuốn tiện lợi khi dạo phố', 'Đồ Chơi - Huấn Luyện Cho Chó'),

-- Sữa Tắm - Dụng Cụ Tắm Vệ Sinh (Chó)
('Sữa Tắm SOS Cho Chó 530ml', 'prod_dog_shampoo1.jpg', 89000, 110000, 19, 'Sữa tắm khử mùi, mượt lông cho chó', 'Sữa Tắm - Dụng Cụ Vệ Sinh Cho Chó'),
('Lược Chải Lông Cho Chó Mèo', 'prod_dog_shampoo2.jpg', 45000, 0, 0, 'Lược chải lông chuyên dụng', 'Sữa Tắm - Dụng Cụ Vệ Sinh Cho Chó'),

-- Chăm Sóc Sức Khoẻ Cho Mèo
('Vitamin Lysine Cho Mèo 60 viên', 'prod_cat_health1.jpg', 120000, 0, 0, 'Hỗ trợ miễn dịch cho mèo', 'Chăm Sóc Sức Khoẻ Cho Mèo'),
('Thuốc Nhỏ Gáy Trị Ve Rận Cho Mèo', 'prod_cat_health2.jpg', 75000, 90000, 17, 'Thuốc nhỏ gáy hiệu quả cho mèo', 'Chăm Sóc Sức Khoẻ Cho Mèo'),

-- Dụng Cụ Ăn Uống Cho Mèo
('Bát Ăn Đôi Cho Mèo Có Giá Đỡ', 'prod_cat_bowl1.jpg', 85000, 100000, 15, 'Bát ăn đôi nghiêng 15 độ bảo vệ cổ mèo', 'Dụng Cụ Ăn Uống Cho Mèo'),
('Máy Lọc Nước Tự Động Cho Mèo 2L', 'prod_cat_bowl2.jpg', 250000, 320000, 22, 'Máy lọc nước tuần hoàn cho mèo', 'Dụng Cụ Ăn Uống Cho Mèo'),

-- Đồ Chơi - Huấn Luyện Cho Mèo
('Cần Câu Đồ Chơi Cho Mèo', 'prod_cat_toy1.jpg', 25000, 0, 0, 'Cần câu lông vũ kích thích mèo vận động', 'Đồ Chơi - Huấn Luyện Cho Mèo'),
('Tháp Bóng 3 Tầng Cho Mèo', 'prod_cat_toy2.jpg', 95000, 120000, 21, 'Đồ chơi tháp bóng giải trí cho mèo', 'Đồ Chơi - Huấn Luyện Cho Mèo'),

-- Sữa Tắm - Dụng Cụ Tắm Vệ Sinh (Mèo)
('Sữa Tắm SOS Cho Mèo 530ml', 'prod_cat_shampoo1.jpg', 89000, 0, 0, 'Sữa tắm dưỡng lông cho mèo', 'Sữa Tắm - Dụng Cụ Vệ Sinh Cho Mèo'),
('Khăn Ướt Vệ Sinh Cho Mèo 80 tờ', 'prod_cat_shampoo2.jpg', 45000, 55000, 18, 'Khăn ướt lau chân, mặt cho mèo', 'Sữa Tắm - Dụng Cụ Vệ Sinh Cho Mèo'),

-- Cát Vệ Sinh Cho Mèo
('Cát Vệ Sinh Đậu Nành Cho Mèo 6L', 'prod_cat_sand1.jpg', 75000, 95000, 21, 'Cát đậu nành khử mùi tốt, thân thiện môi trường', 'Cát Vệ Sinh Cho Mèo'),
('Cát Bentonite Cho Mèo 10L', 'prod_cat_sand2.jpg', 65000, 0, 0, 'Cát bentonite vón cục nhanh', 'Cát Vệ Sinh Cho Mèo'),

-- Dụng Cụ Vệ Sinh Cho Mèo
('Nhà Vệ Sinh Cho Mèo Có Nắp', 'prod_cat_toilet1.jpg', 195000, 250000, 22, 'Nhà vệ sinh kín chống bắn cát', 'Dụng Cụ Vệ Sinh Cho Mèo'),
('Xẻng Xúc Cát Vệ Sinh Cho Mèo', 'prod_cat_toilet2.jpg', 25000, 0, 0, 'Xẻng xúc cát bền, tiện lợi', 'Dụng Cụ Vệ Sinh Cho Mèo');
