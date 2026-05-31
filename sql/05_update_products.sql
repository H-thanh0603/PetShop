USE `petvaccine`;

-- =============================================
-- XÓA SẢN PHẨM CŨ (image không tồn tại) và thêm lại đầy đủ
-- =============================================

-- Xóa order_items trước (foreign key)
DELETE FROM `order_items` WHERE `product_id` > 5;
-- Xóa sản phẩm cũ do update_shop.sql thêm (giữ lại 5 sản phẩm gốc)
DELETE FROM `products` WHERE `id` > 5;

-- Cập nhật 5 sản phẩm gốc: sửa image path đầy đủ + thêm category
UPDATE `products` SET 
    `image` = 'assets/images/shop_pic/prod_royal1.jpg',
    `category` = 'Thức Ăn Cho Mèo'
WHERE `id` = 1;

UPDATE `products` SET 
    `image` = 'assets/images/shop_pic/prod_royal2.jpg',
    `category` = 'Thức Ăn Cho Mèo'
WHERE `id` = 2;

UPDATE `products` SET 
    `image` = 'assets/images/shop_pic/prod_royal3.jpg',
    `category` = 'Chăm Sóc Sức Khoẻ Cho Mèo'
WHERE `id` = 3;

UPDATE `products` SET 
    `image` = 'assets/images/shop_pic/prod_royal4.jpg',
    `category` = 'Thức Ăn Cho Mèo'
WHERE `id` = 4;

UPDATE `products` SET 
    `image` = 'assets/images/shop_pic/prod_royal5.jpg',
    `category` = 'Thức Ăn Cho Mèo'
WHERE `id` = 5;

-- =============================================
-- THÊM SẢN PHẨM MỚI - ĐẦY ĐỦ 13 DANH MỤC
-- Image dùng URL từ internet (placeholder)
-- =============================================

INSERT INTO `products` (`name`, `image`, `price`, `old_price`, `discount`, `description`, `category`) VALUES

-- ========== CHÓ ==========

-- 1. Thức Ăn Cho Chó
('Thức Ăn Hạt Pedigree Cho Chó Trưởng Thành Vị Thịt Bò 1.5kg',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/t/h/thuc-an-cho-cho-truong-thanh-pedigree-vi-thit-bo-1.5kg.jpg',
 95000, 120000, 20,
 'Thức ăn hạt dinh dưỡng hoàn chỉnh cho chó trưởng thành. Giàu protein từ thịt bò thật, bổ sung Omega 6 giúp lông bóng mượt, canxi giúp xương chắc khỏe.',
 'Thức Ăn Cho Chó'),

('Thức Ăn Hạt Royal Canin Mini Adult Cho Chó Nhỏ 2kg',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/r/o/royal-canin-mini-adult-2kg.jpg',
 245000, 280000, 12,
 'Dành riêng cho chó giống nhỏ trưởng thành (1-10kg). Hạt nhỏ dễ nhai, hỗ trợ tiêu hóa tối ưu, duy trì cân nặng lý tưởng.',
 'Thức Ăn Cho Chó'),

('Thức Ăn Hạt SmartHeart Cho Chó Con Vị Thịt Gà & Trứng 1.5kg',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/s/m/smartheart-puppy-chicken-egg.jpg',
 85000, 0, 0,
 'Thức ăn hạt cho chó con từ 2-12 tháng tuổi. Giàu DHA giúp phát triển trí não, canxi và phospho giúp xương răng chắc khỏe.',
 'Thức Ăn Cho Chó'),

('Pate Cesar Cho Chó Vị Thịt Bò & Rau Củ 100g',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/c/e/cesar-beef-vegetables.jpg',
 28000, 35000, 20,
 'Pate cao cấp cho chó với thịt bò thật và rau củ tươi. Kết cấu mềm mịn, thơm ngon, phù hợp cho chó mọi lứa tuổi.',
 'Thức Ăn Cho Chó'),

-- 2. Sữa Cho Chó
('Sữa Bột Bio Milk Cho Chó Con 100g',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/s/u/sua-bot-bio-milk-cho-cho-con.jpg',
 65000, 0, 0,
 'Sữa bột dinh dưỡng thay thế sữa mẹ cho chó con từ sơ sinh. Giàu đạm, vitamin và khoáng chất thiết yếu giúp chó con phát triển toàn diện.',
 'Sữa Cho Chó'),

('Sữa Tươi Pet Milk Cho Chó Mèo 200ml',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/s/u/sua-tuoi-pet-milk.jpg',
 35000, 45000, 22,
 'Sữa tươi tiệt trùng dành riêng cho thú cưng. Bổ sung canxi, không chứa lactose, an toàn cho hệ tiêu hóa của chó mèo.',
 'Sữa Cho Chó'),

-- 3. Chăm Sóc Sức Khoẻ Cho Chó
('Vitamin Tổng Hợp Nutri-Vet Cho Chó 60 Viên',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/v/i/vitamin-nutri-vet-cho-cho.jpg',
 180000, 0, 0,
 'Viên nhai bổ sung vitamin và khoáng chất tổng hợp cho chó. Hỗ trợ hệ miễn dịch, tăng cường sức khỏe xương khớp và lông da.',
 'Chăm Sóc Sức Khoẻ Cho Chó'),

('Thuốc Xổ Giun Drontal Plus Cho Chó',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/d/r/drontal-plus-cho-cho.jpg',
 45000, 55000, 18,
 'Thuốc xổ giun phổ rộng cho chó, diệt giun đũa, giun móc, giun tóc và sán dây. Dạng viên nhai vị thịt, dễ cho chó uống.',
 'Chăm Sóc Sức Khoẻ Cho Chó'),

('Thuốc Nhỏ Gáy Frontline Plus Cho Chó Dưới 10kg',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/f/r/frontline-plus-cho-cho.jpg',
 120000, 150000, 20,
 'Thuốc nhỏ gáy trị ve, rận, bọ chét cho chó. Hiệu quả trong 30 ngày, an toàn cho chó từ 8 tuần tuổi trở lên.',
 'Chăm Sóc Sức Khoẻ Cho Chó'),

-- 4. Dụng Cụ Ăn Uống Cho Chó
('Bát Ăn Inox Chống Lật Cho Chó Size L',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/b/a/bat-an-inox-chong-lat.jpg',
 55000, 0, 0,
 'Bát ăn inox 304 cao cấp, đế cao su chống trượt chống lật. Dễ vệ sinh, bền bỉ, an toàn cho thú cưng. Dung tích 900ml.',
 'Dụng Cụ Ăn Uống Cho Chó'),

('Bình Nước Tự Động Cho Chó Mèo 2.5L',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/b/i/binh-nuoc-tu-dong-2.5l.jpg',
 120000, 150000, 20,
 'Bình nước tự động trọng lực, không cần điện. Dung tích 2.5L đủ nước cho thú cưng cả ngày. Chất liệu nhựa PP an toàn.',
 'Dụng Cụ Ăn Uống Cho Chó'),

-- 5. Đồ Chơi - Huấn Luyện Cho Chó
('Bóng Cao Su Kong Classic Size M',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/b/o/bong-cao-su-kong-classic.jpg',
 185000, 220000, 16,
 'Bóng cao su tự nhiên siêu bền, nảy không đều kích thích bản năng săn mồi. Có thể nhồi snack bên trong để huấn luyện chó.',
 'Đồ Chơi - Huấn Luyện Cho Chó'),

('Dây Dắt Chó Tự Cuốn Flexi 5m',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/d/a/day-dat-cho-tu-cuon-flexi.jpg',
 150000, 180000, 17,
 'Dây dắt tự cuốn dài 5m, cho chó tự do khám phá khi đi dạo. Nút khóa an toàn, tay cầm êm ái. Chịu lực đến 20kg.',
 'Đồ Chơi - Huấn Luyện Cho Chó'),

('Đĩa Bay Frisbee Cho Chó Chơi Ngoài Trời',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/d/i/dia-bay-frisbee-cho-cho.jpg',
 35000, 0, 0,
 'Đĩa bay cao su mềm, an toàn cho răng chó. Màu sắc nổi bật dễ nhìn, phù hợp chơi ngoài trời và huấn luyện.',
 'Đồ Chơi - Huấn Luyện Cho Chó'),

-- 6. Sữa Tắm - Dụng Cụ Vệ Sinh Cho Chó
('Sữa Tắm SOS Cho Chó Lông Trắng 530ml',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/s/u/sua-tam-sos-cho-cho-long-trang.jpg',
 89000, 110000, 19,
 'Sữa tắm chuyên dụng cho chó lông trắng. Khử mùi hôi, dưỡng lông trắng sáng bóng mượt. Chiết xuất thiên nhiên an toàn.',
 'Sữa Tắm - Dụng Cụ Vệ Sinh Cho Chó'),

('Lược Chải Lông Chuyên Dụng Cho Chó Mèo',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/l/u/luoc-chai-long-cho-cho-meo.jpg',
 45000, 0, 0,
 'Lược chải lông 2 mặt: mặt thưa gỡ rối, mặt dày chải mượt. Đầu lược tròn không gây đau, tay cầm chống trượt.',
 'Sữa Tắm - Dụng Cụ Vệ Sinh Cho Chó'),

-- ========== MÈO ==========

-- 7. Thức Ăn Cho Mèo (thêm bên cạnh 5 sản phẩm gốc)
('Thức Ăn Hạt Whiskas Cho Mèo Trưởng Thành Vị Cá Biển 1.2kg',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/w/h/whiskas-adult-ocean-fish.jpg',
 78000, 95000, 18,
 'Thức ăn hạt cho mèo trưởng thành vị cá biển. Giàu Omega 3&6 giúp lông mượt, taurine tốt cho mắt và tim mèo.',
 'Thức Ăn Cho Mèo'),

('Thức Ăn Hạt Me-O Cho Mèo Con Vị Cá Ngừ 1.1kg',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/m/e/me-o-kitten-tuna.jpg',
 68000, 0, 0,
 'Thức ăn hạt cho mèo con từ 1-12 tháng. Giàu DHA cho trí não, canxi cho xương chắc khỏe, hạt nhỏ dễ nhai.',
 'Thức Ăn Cho Mèo'),

('Pate Sheba Cho Mèo Vị Cá Ngừ & Tôm 85g',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/s/h/sheba-tuna-shrimp.jpg',
 22000, 28000, 21,
 'Pate cao cấp Sheba với cá ngừ và tôm thật. Kết cấu mềm mịn trong nước sốt thơm ngon, mèo nào cũng mê.',
 'Thức Ăn Cho Mèo'),

-- 8. Chăm Sóc Sức Khoẻ Cho Mèo
('Vitamin Lysine Cho Mèo Vegebrand 60 Viên',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/l/y/lysine-cho-meo-vegebrand.jpg',
 120000, 0, 0,
 'Viên nhai bổ sung L-Lysine cho mèo. Hỗ trợ hệ miễn dịch, phòng ngừa viêm đường hô hấp trên, giảm triệu chứng hắt hơi sổ mũi.',
 'Chăm Sóc Sức Khoẻ Cho Mèo'),

('Thuốc Nhỏ Gáy Revolution Cho Mèo Dưới 2.5kg',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/r/e/revolution-cho-meo.jpg',
 75000, 90000, 17,
 'Thuốc nhỏ gáy trị ve, rận, giun tim, giun đũa cho mèo. An toàn cho mèo con từ 6 tuần tuổi. Hiệu quả 30 ngày.',
 'Chăm Sóc Sức Khoẻ Cho Mèo'),

('Kem Dinh Dưỡng GimCat Malt-Soft Paste 50g',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/g/i/gimcat-malt-soft-paste.jpg',
 95000, 115000, 17,
 'Kem dinh dưỡng hỗ trợ tiêu búi lông cho mèo. Chứa malt và chất xơ giúp đẩy lông ra ngoài tự nhiên, ngăn ngừa tắc ruột.',
 'Chăm Sóc Sức Khoẻ Cho Mèo'),

-- 9. Dụng Cụ Ăn Uống Cho Mèo
('Bát Ăn Đôi Cho Mèo Có Giá Đỡ Nghiêng 15°',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/b/a/bat-an-doi-cho-meo-co-gia-do.jpg',
 85000, 100000, 15,
 'Bát ăn đôi inox với giá đỡ gỗ nghiêng 15 độ, bảo vệ cổ mèo khi ăn. Thiết kế đẹp, dễ tháo rời vệ sinh.',
 'Dụng Cụ Ăn Uống Cho Mèo'),

('Máy Lọc Nước Tự Động Cho Mèo Catit 2L',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/m/a/may-loc-nuoc-catit-cho-meo.jpg',
 250000, 320000, 22,
 'Máy lọc nước tuần hoàn 3 lớp lọc, nước luôn sạch và tươi mát. Hoạt động êm ái, khuyến khích mèo uống nhiều nước hơn.',
 'Dụng Cụ Ăn Uống Cho Mèo'),

-- 10. Đồ Chơi - Huấn Luyện Cho Mèo
('Cần Câu Lông Vũ Đồ Chơi Cho Mèo',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/c/a/can-cau-long-vu-cho-meo.jpg',
 25000, 0, 0,
 'Cần câu đồ chơi với lông vũ nhiều màu sắc, kích thích bản năng săn mồi của mèo. Giúp mèo vận động, giảm stress.',
 'Đồ Chơi - Huấn Luyện Cho Mèo'),

('Tháp Bóng 3 Tầng Catit Senses',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/t/h/thap-bong-3-tang-catit.jpg',
 95000, 120000, 21,
 'Đồ chơi tháp bóng 3 tầng, bóng lăn trong rãnh kích thích mèo đuổi bắt. Giải trí hàng giờ, phù hợp mèo mọi lứa tuổi.',
 'Đồ Chơi - Huấn Luyện Cho Mèo'),

('Chuột Bông Catnip Đồ Chơi Cho Mèo (3 con)',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/c/h/chuot-bong-catnip-cho-meo.jpg',
 30000, 0, 0,
 'Bộ 3 chuột bông nhồi catnip (bạc hà mèo). Mùi catnip kích thích mèo chơi đùa, giảm căng thẳng. Kích thước vừa miệng mèo.',
 'Đồ Chơi - Huấn Luyện Cho Mèo'),

-- 11. Sữa Tắm - Dụng Cụ Vệ Sinh Cho Mèo
('Sữa Tắm SOS Cho Mèo Lông Dài 530ml',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/s/u/sua-tam-sos-cho-meo-long-dai.jpg',
 89000, 0, 0,
 'Sữa tắm dưỡng lông chuyên dụng cho mèo lông dài. Giúp lông mềm mượt không rối, khử mùi hôi, pH cân bằng cho da mèo.',
 'Sữa Tắm - Dụng Cụ Vệ Sinh Cho Mèo'),

('Khăn Ướt Vệ Sinh Cho Mèo Pet Soft 80 Tờ',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/k/h/khan-uot-ve-sinh-cho-meo.jpg',
 45000, 55000, 18,
 'Khăn ướt vệ sinh chuyên dụng cho mèo. Lau chân, mặt, tai an toàn. Không cồn, không paraben, hương thơm nhẹ nhàng.',
 'Sữa Tắm - Dụng Cụ Vệ Sinh Cho Mèo'),

-- 12. Cát Vệ Sinh Cho Mèo
('Cát Vệ Sinh Đậu Nành Cature Cho Mèo 6L',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/c/a/cat-dau-nanh-cature-6l.jpg',
 75000, 95000, 21,
 'Cát đậu nành tự nhiên, vón cục nhanh, khử mùi tốt. Có thể xả bồn cầu, thân thiện môi trường. Ít bụi, an toàn cho mèo.',
 'Cát Vệ Sinh Cho Mèo'),

('Cát Bentonite Kit Cat Cho Mèo 10L',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/c/a/cat-bentonite-kit-cat-10l.jpg',
 65000, 0, 0,
 'Cát bentonite vón cục siêu nhanh, khử mùi 7 ngày. Hạt cát mịn, ít bụi, mèo thích đào bới. Tiết kiệm, dùng được lâu.',
 'Cát Vệ Sinh Cho Mèo'),

('Cát Tofu Vệ Sinh Cho Mèo Hương Trà Xanh 7L',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/c/a/cat-tofu-tra-xanh-7l.jpg',
 85000, 105000, 19,
 'Cát tofu hương trà xanh tự nhiên, khử mùi hiệu quả. Vón cục nhanh trong 3 giây, xả được bồn cầu, 100% tự nhiên.',
 'Cát Vệ Sinh Cho Mèo'),

-- 13. Dụng Cụ Vệ Sinh Cho Mèo
('Nhà Vệ Sinh Cho Mèo Có Nắp Đậy Chống Văng Cát',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/n/h/nha-ve-sinh-cho-meo-co-nap.jpg',
 195000, 250000, 22,
 'Nhà vệ sinh kín cho mèo, chống văng cát ra ngoài. Cửa lật tiện lợi, có bộ lọc than hoạt tính khử mùi. Kích thước rộng rãi.',
 'Dụng Cụ Vệ Sinh Cho Mèo'),

('Xẻng Xúc Cát Vệ Sinh Cho Mèo Inox',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/x/e/xeng-xuc-cat-inox.jpg',
 25000, 0, 0,
 'Xẻng xúc cát inox bền bỉ, lỗ lọc vừa phải giữ lại cát sạch. Tay cầm nhựa chống trượt, dễ vệ sinh.',
 'Dụng Cụ Vệ Sinh Cho Mèo'),

('Bịch Túi Rác Vệ Sinh Khay Mèo 50 Túi',
 'https://cdn.petcity.vn/media/catalog/product/cache/image/700x700/e9c3970ab036de70892d86c6d221abfe/t/u/tui-rac-khay-meo-50-tui.jpg',
 40000, 50000, 20,
 'Túi rác chuyên dụng lót khay vệ sinh mèo. Chất liệu dày dặn chống rò rỉ, dây rút tiện lợi. 50 túi/cuộn, tiết kiệm.',
 'Dụng Cụ Vệ Sinh Cho Mèo');
