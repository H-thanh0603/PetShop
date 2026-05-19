-- Real product seed generated from public Paddy product data.

-- Source: https://paddy.vn/products.json

SET NAMES utf8mb4;

INSERT IGNORE INTO pet_types (code, name, icon, display_order, is_active) VALUES

('dog', 'Chó', 'bxs-dog', 1, 1),

('cat', 'Mèo', 'bxs-cat', 2, 1);

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Cho Mèo Ức Gà Hấp Nước Cốt Gà Cattyman', 'products/paddy_001_banh-thuong-cho-meo-uc-ga-hap-nuoc-cot-ga-cattyman.jpg', 17000, 0,
       'Bánh Thưởng Cho Mèo Ức Gà Hấp Nước Cốt Gà Cattyman Thương hiệu: CattyMan Phù hợp cho: Mèo mọi lứa tuổi Bánh thưởng cho mèo Cattyman được làm từ phi lê ức gà nguyên miếng hấp chín, giữ trọn vị ngọt tự nhiên, kết hợp cùng nước cốt cá ngừ hoặc cua giúp tăng độ thơm ngon và hấp dẫn. Kết cấu mềm ẩm, dễ ăn, phù hợp cho cả mèo con và mèo già, có thể dùng trực tiếp như bánh thưởng hoặc làm topping cho bữa chính. Sản phẩm còn bổ sung Taurine hỗ trợ sức khỏe tim mạch, thị lực và phát triển toàn diện cho mèo. Lợi ích Giàu đạm từ ức gà thật, hỗ trợ phát triển và duy trì cơ bắp Mềm ẩm, dễ ăn, phù hợp cho mèo con, mèo già hoặc mèo răng yếu Tăng độ ngon miệng, kích thích mèo kén ăn nhờ hương vị tự nhiên từ thịt gà và nước cốt cá/cua Bổ sung Taurine, hỗ trợ tim mạch, thị lực và sức khỏe tổng thể Giữ độ ẩm tốt, giúp hỗ trợ tiêu hóa và cấp nước nhẹ Có thể dùng linh hoạt như snack thưởng hoặc topping cho…

Thương hiệu: CattyMan.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-meo-uc-ga-hap-nuoc-cot-ga-cattyman', 8, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Bánh Thưởng Cho Mèo Ức Gà Hấp Nước Cốt Gà Cattyman');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Que Gân Bò Cho Chó Doggyman 100g', 'products/paddy_002_que-gan-bo-cho-cho-doggyman-100g.jpg', 72000, 0,
       'Bánh Thưởng Que Gân Bò Cho Chó Doggyman 100g Thương hiệu: Doggyman Phù hợp cho: Chó từ 3 tháng tuổi trở lên Bánh thưởng cho chó sự kết hợp giữa da bò và gân bò giàu chondroitin, sản phẩm mang lại độ đàn hồi lý tưởng giúp cún cưng tận hưởng cảm giác nhai phấn khích với vị ngon tự nhiên trong từng miếng cắn. Lợi ích Giúp thỏa mãn nhu cầu nhai tự nhiên, giảm stress và hạn chế cắn phá đồ đạc Hỗ trợ sức khỏe khớp, nhờ chondroitin từ gân bò Tăng cường răng miệng khỏe mạnh, giúp làm sạch mảng bám khi nhai Độ đàn hồi tốt, tạo cảm giác nhai lâu, không nhanh nát Giàu protein, hỗ trợ phát triển và duy trì cơ bắp Hương vị tự nhiên từ bò giúp tăng độ hấp dẫn, kích thích ăn ngon Hướng dẫn sử dụng Cho ăn trực tiếp như đồ gặm thưởng hoặc giải trí Phù hợp cho chó từ trên 3 tháng tuổi Nên giám sát khi chó đang nhai để đảm bảo an toàn Điều chỉnh lượng dùng tùy theo kích thước và sức nhai của chó Luôn cung…

Thương hiệu: DoggyMan.

Nguồn tham khảo: https://paddy.vn/products/que-gan-bo-cho-cho-doggyman-100g', 41, 150, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Bánh Thưởng Que Gân Bò Cho Chó Doggyman 100g');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Vòng Cổ Và Dây Dắt Cho Chó Police', 'products/paddy_003_vong-co-va-day-dat-cho-cho-police.jpg', 35000, 0,
       'Vòng Cổ Và Dây Dắt Cho Chó Police Thương hiệu: Paddy Phù hợp cho: Chó mọi lứa tuổi Vòng Cổ Và Dây Dắt Cho Chó Police có thiết kế vòng đệm êm ái, chắc chắn với dòng chữ Police Dog nổi bật. Chi tiết phát quang hỗ trợ nhận diện trong điều kiện thiếu sáng, giúp thú cưng an toàn hơn khi di chuyển ban đêm. Lợi ích Chất liệu vải dù dày, chịu lực tốt, độ bền cao Khóa kim loại chắc chắn, an toàn khi sử dụng Dễ điều chỉnh kích thước, phù hợp nhiều size cổ Hỗ trợ kiểm soát chó hiệu quả khi dắt đi dạo hoặc huấn luyện Thành phần Vải dù cao cấp, khóa kim loại, đinh tán gia cố Hướng dẫn sử dụng Điều chỉnh vòng cổ vừa vặn, gắn dây dắt vào khoen kim loại, kiểm tra trước khi sử dụng. Vệ sinh bằng khăn ẩm, tránh ngâm nước lâu. 👉 Xem thêm sản phẩm khác tại Paddy.vn #vongcochomeo #daydatcho #petshop #dogaccessories #paddypetshop #phukienchomeo #daydatchomeo

Thương hiệu: Paddy Pet Shop.

Nguồn tham khảo: https://paddy.vn/products/vong-co-va-day-dat-cho-cho-police', 8, 100, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Vòng Cổ Và Dây Dắt Cho Chó Police');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Xúc xích Cho Chó Gà Mini Doggyman', 'products/paddy_004_xuc-xich-cho-cho-ga-mini-doggyman.jpg', 30000, 0,
       'Xúc Xích Cho Chó Gà Mini Doggyman Thương hiệu: Doggyman Phù hợp cho: Chó mọi lứa tuổi Bánh thưởng cho chó Doggyman có hương vị ngon ngọt của thịt gà chất lượng cao, đậm đà trong từng miếng cắn. Kết cấu mềm mịn, dễ ăn, phù hợp cho cả chó con và chó già có lực nhai yếu. Kích thước vừa vặn: miếng nhỏ, vừa ăn hết, kích cỡ hợp lý. Tiện lợi khi sử dụng, mỗi thanh được đóng gói riêng lẻ, giúp giữ trọn hương vị tươi mới mỗi khi mở bao bì. Lợi ích Tăng độ ngon miệng, hương vị thịt gà hấp dẫn phù hợp cả thú cưng kén ăn Dễ nhai, dể tiêu hoá, phù hợp cho chó con, chó già hoặc cho có răng yếu Cung cấp protein chất lượng cao, hỗ trợ phát triển và duy trì cơ bắp Kích thước nhỏ gọn, tiện lợi cho ăn hàng ngày hoặc làm phần thưởng Giữ độ tươi ngon lâu hơn nhờ đóng gói riêng từng thanh Hỗ trợ bổ sung dinh dưỡng nhẹ nhàng giữa các bữa chính Thành phần Thịt gà, tinh bột ngô, đạm đậu nành cô lập, chất xơ thự…

Thương hiệu: DoggyMan.

Nguồn tham khảo: https://paddy.vn/products/xuc-xich-cho-cho-ga-mini-doggyman', 8, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Xúc xích Cho Chó Gà Mini Doggyman');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Siêu Topping Cho Mèo Kings Pet', 'products/paddy_005_hat-sieu-topping-cho-meo-kings-pet.jpg', 155000, 0,
       'Hạt Siêu Topping Cho Mèo Kings Pet Thương hiệu: King''s Pet Phù hợp cho: Mèo trưởng thành Hạt cho mèo siêu topping King’s Pet là dòng thức ăn cao cấp cho mèo trưởng thành, nổi bật với topping cá ngừ sấy thăng hoa giữ trọn hương vị và dinh dưỡng như đồ tươi. Sản phẩm không chỉ giúp mèo ăn ngon hơn mà còn hỗ trợ toàn diện từ tiêu hóa, giảm búi lông đến bảo vệ hệ tiết niệu nhờ công thức bổ sung DL-Methionine và chất xơ đặc biệt. Với thiết kế topping tách riêng độc đáo, mỗi bữa ăn của “boss” trở nên hấp dẫn và chất lượng hơn bao giờ hết. Lợi ích Tăng độ ngon miệng vượt trội với topping cá ngừ sấy thăng hoa, giữ trọn hương vị và dưỡng chất Hỗ trợ hệ tiết niệu, hạn chế nguy cơ sỏi nhờ DL-Methionine (duy trì pH nước tiểu ổn định) Giảm búi lông hiệu quả với Lignocellulose, hỗ trợ đào thải lông qua đường tiêu hóa Phát triển cơ bắp & duy trì thể trạng khỏe mạnh với hàm lượng protein ~32% Tốt cho t…

Thương hiệu: Kings Pet.

Nguồn tham khảo: https://paddy.vn/products/hat-sieu-topping-cho-meo-kings-pet', 8, 1200, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Hạt Siêu Topping Cho Mèo Kings Pet');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Lồng Vận Chuyển Hàng Không Cho Chó Mon Ami Carrier', 'products/paddy_006_long-van-chuyen-hang-khong-cho-cho-mon-ami-carrier.jpg', 730000, 0,
       'Lồng Vận Chuyển Hàng Không Cho Chó Mon Ami Carrier Thương hiệu: Mon Ami Phù hợp cho: Chó mọi lứa tuổi Lồng vận chuyển Mon Ami là giải pháp tiện lợi và an toàn giúp bạn đưa thú cưng di chuyển dễ dàng. Thiết kế chắc chắn, thông thoáng, đạt tiêu chuẩn hàng không, kết hợp bánh xe linh hoạt giúp việc di chuyển nhẹ nhàng hơn. Sản phẩm phù hợp cho cả đi chơi, đi xa và vận chuyển thú cưng một cách gọn gàng và an tâm. Lợi ích An toàn khi di chuyển: Thiết kế chắc chắn, giúp bảo vệ thú cưng trong suốt quá trình vận chuyển Đạt tiêu chuẩn hàng không: Phù hợp khi đi máy bay hoặc di chuyển đường dài Dễ dàng di chuyển: Trang bị bánh xe giúp kéo đẩy nhẹ nhàng, tiện lợi Thông thoáng, thoải mái: Các khe thoáng khí giúp thú cưng không bị bí, giảm stress Đa năng: Dùng được khi đi chơi, đi khám, đi xa hoặc gửi thú cưng Hướng dẫn bảo quản Vệ sinh lồng định kỳ bằng khăn ẩm hoặc dung dịch nhẹ, lau khô hoàn toàn…

Thương hiệu: Mon Ami.

Nguồn tham khảo: https://paddy.vn/products/long-van-chuyen-hang-khong-cho-cho-mon-ami-carrier', 8, 100, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Lồng Vận Chuyển Hàng Không Cho Chó Mon Ami Carrier');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo 5Plus Catmix Ruốc Gà Sấy Khô', 'products/paddy_007_hat-cho-meo-5plus-catmix-ruoc-ga-say-kho.jpg', 70000, 0,
       'Hạt Cho Mèo 5Plus Catmix Ruốc Gà Sấy Khô Thương hiệu: CatChy Phù hợp cho: Mèo mọi lứa tuổi Hạt cho mèo 5Plus Catmix là dòng hạt dinh dưỡng cho mèo kết hợp hoàn hảo giữa chất lượng và giá thành hợp lý, phù hợp cho các “sen” muốn tối ưu chi phí nhưng vẫn đảm bảo bữa ăn đầy đủ cho boss. Với nguồn đạm động vật chiếm tỷ lệ cao cùng ruốc gà thật giúp tăng độ ngon miệng, sản phẩm hỗ trợ mèo phát triển khỏe mạnh, lông mượt, tiêu hóa tốt và giảm mùi phân hiệu quả. Lợi ích Cung cấp nguồn đạm động vật cao (86%) giúp phát triển cơ bắp khỏe mạnh Tăng độ ngon miệng nhờ ruốc gà thật, phù hợp mèo kén ăn Đảm bảo năng lượng và dinh dưỡng hằng ngày (protein 28%) Hỗ trợ da khỏe – lông bóng mượt Giúp mắt sáng, thị lực tốt Hỗ trợ tiêu hóa và đường tiết niệu ổn định Giảm mùi phân nhờ chiết xuất Yucca Hạn chế búi lông, tốt cho mèo trong giai đoạn thay lông Hướng dẫn sử dụng Cho mèo ăn trực tiếp, không cần chế…

Thương hiệu: Catchy.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-5plus-catmix-ruoc-ga-say-kho', 8, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Hạt Cho Mèo 5Plus Catmix Ruốc Gà Sấy Khô');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bột Vi Sinh Khử Mùi Cho Chó Mèo Joycat 54g', 'products/paddy_008_bot-vi-sinh-khu-mui-cho-cho-meo-joycat-54g.jpg', 39000, 0,
       'Bột Vi Sinh Khử Mùi Cho Chó Mèo Joycat 54g Thương hiệu: Joycat Phù hợp cho: Chó/Mèo mọi lứa tuổi Khử mùi chó mèo JOYCAT là giải pháp kiểm soát mùi hôi thùng cát hiệu quả, ứng dụng công nghệ Dual E.M Bio-Technology™ giúp loại bỏ mùi và diệt khuẩn đến 99%. Sản phẩm an toàn tuyệt đối với tiêu chí không chứa hóa chất, không chứa hương liệu, phù hợp cho cả thú cưng và người dùng. Chỉ cần trộn trực tiếp vào cát vệ sinh, bột giúp duy trì môi trường sạch sẽ, khô thoáng và khử mùi kéo dài đến 21 ngày. Lợi ích Khử mùi hôi hiệu quả trong thùng cát mỗi ngày Diệt khuẩn đến 99%, hạn chế vi khuẩn gây mùi Duy trì hiệu quả lâu dài lên đến 21 ngày Giúp thùng cát luôn khô thoáng, sạch sẽ An toàn tuyệt đối cho thú cưng và người sử dụng (không hóa chất, không kích ứng) Hướng dẫn sử dụng Làm sạch thùng cát trước khi sử dụng Đổ cát mới với độ cao khoảng 7–9cm Trộn đều bột theo tỷ lệ ~50–54g / 5kg cát Dọn chất…

Thương hiệu: Joycat.

Nguồn tham khảo: https://paddy.vn/products/bot-vi-sinh-khu-mui-cho-cho-meo-joycat-54g', 8, 100, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Bột Vi Sinh Khử Mùi Cho Chó Mèo Joycat 54g');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Xịt Vi Sinh Khử Mùi Cho Chó Mèo Joycat 450ml', 'products/paddy_009_xit-vi-sinh-khu-mui-cho-cho-meo-joycat-450ml.jpg', 129000, 0,
       'Xịt Vi Sinh Khử Mùi Cho Chó Mèo Joycat 450ml Thương hiệu: JoyCat Phù hợp cho: Chó/Mèo mọi lứa tuổi Xịt khử mùi là giải pháp làm sạch và khử mùi hiệu quả cho không gian sống của thú cưng, ứng dụng công nghệ vi sinh E.M Elimination Formula™ giúp loại bỏ mùi hôi tận gốc và diệt khuẩn đến 99%. Sản phẩm an toàn với tiêu chí 6 không (không cồn, không hóa chất, không kích ứng…), có thể sử dụng trực tiếp trên lông thú cưng và các khu vực như thùng cát, sofa, nệm… mang lại hiệu quả khử mùi kéo dài đến 12 giờ, giúp không gian luôn sạch sẽ và dễ chịu. Lợi ích Khử sạch mùi ngay lập tức Diệt khuẩn, nấm mốc đến 99% Hiệu quả kéo dài đến 12 giờ An toàn, thân thiện với sức khỏe của bạn và thú cưng Hướng dẫn sử dụng Lắc đều trước khi sử dụng Xịt trực tiếp vào khu vực vệ sinh Có thể xịt lên lông thú cưng Xịt lên các vị trí phát sinh mùi 👉 Xem thêm sản phẩm khác tại Paddy.vn #vesinhthucung #khukhuan #cham…

Thương hiệu: Joycat.

Nguồn tham khảo: https://paddy.vn/products/xit-vi-sinh-khu-mui-cho-cho-meo-joycat-450ml', 8, 700, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Xịt Vi Sinh Khử Mùi Cho Chó Mèo Joycat 450ml');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo Nutri Plan PLUS 1.5kg', 'products/paddy_010_hat-cho-meo-nutri-plan-plus-1-5kg.jpg', 190000, 0,
       'Hạt Cho Mèo Nutri Plan PLUS 1.5kg Thương hiệu: Nutri Plan Phù hợp cho: Mèo mọi lứa tuổi Nutri Plan Plus là sản phẩm thức ăn cho mèo cao cấp, phù hợp với tất cả các lứa tuổi mèo. Sản phẩm được sản xuất từ nguồn nguyên liệu tươi ngon, đảm bảo chất lượng và an toàn cho sức khỏe của mèo, bổ sung thêm các chất dinh dưỡng thiết yếu khác như vitamin, khoáng chất, axit amin… giúp mèo phát triển khỏe mạnh và toàn diện. Lợi ích Thêm vị - Thêm lựa chọn: Ngoài vị cá ngừ truyền thống, nay đã có thêm vị cá hồi và thịt gà thơm ngon. Khỏe mạnh hơn: Bổ sung nguyên liệu miễn dịch PF-21 độc quyền do các chuyên gia Dongwon nghiên cứu. Giàu dưỡng chất thiết yếu: Bổ sung Taurine, Magie và phức hợp chất xơ hỗ trợ sức khỏe thị lực, tim mạch và hệ tiêu hóa. Cam kết "Sạch" (Clean Label): Hoàn toàn không chứa chất tạo màu, hương liệu tổng hợp hay chất chống nấm mốc. An toàn tuyệt đối: Sản xuất tại hệ thống nhà má…

Thương hiệu: Nutri Plan.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-nutri-plan-plus-1-5kg', 8, 1700, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Hạt Cho Mèo Nutri Plan PLUS 1.5kg');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Cát Vệ Sinh Mèo JoyCat Ultimate Health IQ Giúp Theo Dõi Sức Khỏe', 'products/paddy_011_cat-ve-sinh-cho-meo-joycat.jpg', 159000, 0,
       'Cát Vệ Sinh Mèo JoyCat Ultimate Health IQ Giúp Theo Dõi Sức Khỏe Thương hiệu: Joycat Phù hợp cho: Mèo mọi lứa tuổi Sản phẩm cát vệ sinh mèo kết hợp cát khoáng tự nhiên và đậu nành theo công thức tối ưu mang lại hiệu quả sử dụng vượt trội và tiết kiệm Lợi ích Công thức mix hiệu suất cao, mang lại hiệu quả tốt nhất Thành phần tự nhiên 100% thân thiện 16 bước lọc bụi hoàn toàn, sạch bụi lên đến 98%, an toàn cho hệ hô hấp của mèo & gia đình Thấm hút tốt hơn, vón cục hoàn hảo Dễ dàng dọn vệ sinh, kiểm soát mùi tốt Nâng cấp công nghệ Ultimate Health IQ kết hợp Smart pH & Blood indicator, theo dõi sức khỏe cho mèo hằng ngày tại nhà Khả năng khử mùi & diệt khuẩn toàn diện Hướng dẫn sử dụng Đổ cát JOYCAT vào thùng sạch đến độ cao 7-9cm Dọn sạch chất thải của mèo hằng ngày Đổ thêm cát mới, duy trì độ cao 7-9cm Thay mới toàn bộ cát, mỗi 30 ngày / lần 👉 Xem thêm sản phẩm khác tại Paddy.vn #catvesi…

Thương hiệu: Joycat.

Nguồn tham khảo: https://paddy.vn/products/cat-ve-sinh-cho-meo-joycat', 8, 100, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Cát Vệ Sinh Mèo JoyCat Ultimate Health IQ Giúp Theo Dõi Sức Khỏe');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo Wanpy Mix Thịt Viên', 'products/paddy_012_hat-cho-meo-wanpy-mix-thit-vien.jpg', 70000, 0,
       'Hạt Cho Mèo Wanpy Mix Thịt Viên Thương hiệu: Wanpy Phù hợp cho: Mèo mọi lứa tuổi Thức ăn cho mèo Wanpy được bào chế với công thức không chứa ngũ cốc và tỷ lệ đạm động vật lên tới 89%, mang đến bữa ăn tự nhiên và lành mạnh cho mèo. Sự kết hợp độc đáo giữa hạt truyền thống và hạt thịt viên nướng lò giúp mỗi bữa ăn trở nên hấp dẫn, kích thích vị giác của thú cưng. Lợi ích Da lông khỏe mạnh: Công thức giàu Omega-3, 6 giúp nuôi dưỡng da và lông óng mượt. Tiết niệu khỏe mạnh (dòng cho mèo): Duy trì độ pH phù hợp, hỗ trợ hệ tiết niệu hoạt động ổn định. Xương khớp chắc khỏe (dòng cho chó): Bổ sung Glucosamine và Chondroitin giúp hỗ trợ sức khỏe xương khớp. Tăng cường miễn dịch: Cung cấp đầy đủ vitamin, khoáng chất và lợi khuẩn cần thiết. Thành phần Nguyên liệu chính: Đạm động vật từ thịt gà, cá hồi, cá ngừ, thịt bò, thịt vịt, mỡ động vật. Dưỡng chất bổ sung: omega-3, omega-6, vitamin, khoáng ch…

Thương hiệu: Wanpy Premium.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-wanpy-mix-thit-vien', 8, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Hạt Cho Mèo Wanpy Mix Thịt Viên');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Chó Smartheart Gold Indoor', 'products/paddy_013_hat-cho-cho-smartheart-gold-indoor.jpg', 110000, 0,
       'Hạt Cho Chó Smartheart Gold Indoor Thương hiệu: Smartheart Phù hợp cho: Chó mọi lứa tuổi Xu hướng nuôi chó nhỏ tại nhà đang dần trở nên phổ biến. Hạt cho chó SmartHeart Gold Indoor là sản phẩm giúp tăng cường sức khỏe hệ tiêu hóa, cải thiện miễn dịch, nuôi dưỡng da lông và xương khớp…để cún yêu của bạn dù có nuôi chủ yếu trong nhà nhưng vẫn luôn tràn đầy năng lượng và khỏe mạnh. Lợi ích Tiêu hoá khoẻ, cân bằng đường ruột (XOS, FOS, MOS) Da khoẻ, lông bóng mượt ( Omega 3 & 6) Xương răng chắc khoẻ (Canxi, Photpho, Vitamin D) Hỗ trợ tim mạch (Taurine, Omega 3) Tăng đề kháng, ít bệnh (Vitamin E, Selenium, Beta-Glucan) Hạt nhỏ, dễ ăn, phù hợp cho chó giống nhỏ Hướng dẫn sử dụng Lượng cho ăn có thể điều chỉnh dựa theo giống, trọng lượng và mức độ hoạt động của chó để duy trì cân nặng lý tưởng. Luôn chuẩn bị sẵn nước sạch cho chó mọi lúc. Để tránh rối loạn tiêu hóa liên quan đến việc thay đổi…

Thương hiệu: Smartheart.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-smartheart-gold-indoor', 52, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Hạt Cho Chó Smartheart Gold Indoor');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Chó Trưởng Thành Happy Tummy', 'products/paddy_014_hat-cho-cho-truong-thanh-happy-tummy.jpg', 61000, 0,
       'Hạt Cho Chó Trưởng Thành Happy Tummy Thương hiệu: Happy Tummy Phù hợp cho: Chó trưởng thành Hạt cho chó Happy Tummy là thức ăn hạt chuyên biệt cho chó trưởng thành. Sản phẩm tập trung tối ưu hệ tiêu hóa, giảm mùi hôi chất thải, đồng thời nuôi dưỡng lông bóng mượt và hệ xương răng chắc khỏe, giúp cún yêu luôn duy trì năng lượng và vóc dáng săn chắc. Lợi ích: Hệ tiêu hóa khỏe mạnh: Công thức chuyên biệt giúp tối ưu hóa quá trình hấp thụ dinh dưỡng, nuôi dưỡng hệ tiêu hóa bền bỉ Giảm mùi hôi chất thải: Chiết xuất từ cây Yucca giúp ức chế mùi hôi khó chịu trong phân và nước tiểu, giữ không gian sống sạch thoáng Xương & răng chắc khỏe: Cung cấp hàm lượng Canxi và Phốt pho (min 1%) cân đối, giúp hệ khung xương vững chãi và răng chắc khỏe Lông bóng mượt, da khỏe: Các dưỡng chất thiết yếu hỗ trợ nuôi dưỡng lớp lông óng ả và giảm thiểu các tình trạng viêm da thường gặp ở chó Tăng cường cơ bắp &…

Thương hiệu: Happy Tummy.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-truong-thanh-happy-tummy', 53, 1000, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Hạt Cho Chó Trưởng Thành Happy Tummy');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo Trưởng Thành Happy Tummy', 'products/paddy_015_hat-cho-meo-truong-thanh-happy-tummy.jpg', 69000, 0,
       'Hạt Cho Mèo Trưởng Thành Happy Tummy Thương hiệu: Happy Tummy Phù hợp cho: Mèo trưởng thành Hạt cho mèo Happy Tummy là dòng hạt chuyên biệt cho mèo trưởng thành. Với công thức tối ưu cho hệ tiêu hóa, sản phẩm giúp giảm mùi hôi chất thải, nuôi dưỡng lông bóng mượt và hỗ trợ xương chắc khỏe, giúp mèo cưng luôn năng động và khỏe mạnh mỗi ngày. Lợi ích: Hệ tiêu hóa khỏe mạnh: Sản phẩm tối ưu hóa khả năng hấp thụ, giúp bụng khỏe, tiêu hóa tốt Giảm mùi hôi hiệu quả: Chiết xuất cây Yucca giúp kiểm soát và giảm thiểu mùi hôi từ phân và nước tiểu của mèo Vóc dáng săn chắc: Hàm lượng đạm (min 26%) cùng protein thủy phân giúp tăng khối lượng cơ bắp và duy trì sức bền cho mèo năng động Xương & răng chắc khỏe: Tỉ lệ Canxi và Phốt pho (min 1%) được cân bằng chuẩn xác để hỗ trợ hệ khung xương vững chắc Da khỏe, lông bóng mượt: Chứa các dưỡng chất thiết yếu giúp giảm viêm da và nuôi dưỡng bộ lông mềm m…

Thương hiệu: Happy Tummy.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-truong-thanh-happy-tummy', 8, 1000, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Hạt Cho Mèo Trưởng Thành Happy Tummy');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo Excel Vị Cá Thơm Ngon', 'products/paddy_016_hat-cho-meo-excel-vi-ca-thom-ngon.jpg', 48000, 0,
       'Hạt Cho Mèo Excel Vị Cá Thơm Ngon Thương hiệu: Excel Phù hợp cho: Từ mèo con đến mèo trưởng thành Hạt cho mèo Excel từ Japfa Pet Food Việt Nam là giải pháp dinh dưỡng toàn diện. Với công thức giàu dưỡng chất, sản phẩm giúp mèo cưng phát triển khỏe mạnh, sở hữu bộ lông bóng mượt và đặc biệt hỗ trợ giảm mùi hôi chất thải hiệu quả. Lợi ích: Hệ tiêu hóa khỏe mạnh, giảm mùi hôi: Bổ sung chất xơ tự nhiên và Prebiotics giúp tối ưu hóa khả năng hấp thụ dưỡng chất, bảo vệ đường ruột và giảm thiểu mùi hôi khó chịu của phân. Da khỏe, lông bóng mượt: Hàm lượng Omega-3 và Omega-6 cân đối nuôi dưỡng làn da khỏe mạnh từ bên trong và mang lại bộ lông mềm mượt. Sáng mắt, khỏe tim: Bổ sung Taurine – một acid amin thiết yếu mà cơ thể mèo không tự tổng hợp được, giúp đôi mắt tinh anh và cơ tim khỏe mạnh. Tăng cường sức đề kháng: Giàu Vitamin (A, E, D3... ) và khoáng chất giúp củng cố hệ miễn dịch tự nhiên…

Thương hiệu: Excel.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-excel-vi-ca-thom-ngon', 55, 700, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Hạt Cho Mèo Excel Vị Cá Thơm Ngon');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Cho Chó Mèo Mọi Lứa Tuổi Dexinbone', 'products/paddy_017_banh-thuong-cho-cho-meo-moi-lua-tuoi-dexinbone.jpg', 55000, 0,
       'Bánh Thưởng Cho Chó Mèo Mọi Lứa Tuổi Dexinbone Thương hiệu: INU Fonti Phù hợp cho: Chó/Mèo mọi lứa tuổi Bánh thưởng cho chó mèo Dexinbone là dòng sản phẩm đồ ăn vặt được thiết kế không chỉ để thỏa mãn vị giác mà còn mang lại nhiều lợi ích vượt trội cho sức khỏe. Với kết cấu đặc biệt và thành phần được nghiên cứu kỹ lưỡng, mỗi miếng bánh thưởng không chỉ là một phần thưởng mà còn là một công cụ chăm sóc răng miệng hiệu quả. Lợi ích Giảm mảng bám và cao răng Thành phần tốt cho tiêu hoá Giảm căng thẳng và chống buồn chán Chắc khoẻ cơ bắp, răng và xương Hướng dẫn bảo quản Luôn giữ sản phẩm còn lại trong túi hoặc thùng kín Bảo quản sản phẩm ở nơi khô ráo, thoáng mát, tránh tiếp xúc trực tiếp với ánh sáng mặt trời Xem thêm các sản phẩm khác tại Paddy.vn #banhthuongchochomeo #snackchomeo #thucanchochomeo #doanchomeo #chamsocrangmieng #chamsocthucung #INUFONTI

Thương hiệu: INU Fonti.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-cho-meo-moi-lua-tuoi-dexinbone', 8, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Bánh Thưởng Cho Chó Mèo Mọi Lứa Tuổi Dexinbone');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Chó Mọi Lứa Tuổi LaPaw 375g', 'products/paddy_018_pate-cho-cho-moi-lua-tuoi-lapaw-375g.webp', 33000, 0,
       'Pate Cho Chó Mọi Lứa Tuổi LaPaw 375g Thương hiệu: LaPaw Phù hợp cho: Chó mọi lứa tuổi Pate Cho Chó LaPaw là một sản phẩm thức ăn cho chó được sản xuất bởi thương hiệu LaPaw của Việt Nam. Pate được làm từ thịt gà, thịt bò và các nguyên liệu dinh dưỡng khác, cung cấp đầy đủ các dưỡng chất cần thiết cho chó mọi lứa tuổi, giúp chó phát triển toàn diện, lông mượt, da khỏe. Lợi ích: Cung cấp đầy đủ các dưỡng chất cần thiết cho chó mọi lứa tuổi, giúp chó phát triển khỏe mạnh, lông mượt, da khỏe. Giúp chó tăng cường sức đề kháng, phòng chống bệnh tật. Giúp chó tiêu hóa tốt, hấp thụ dinh dưỡng hiệu quả. Giúp chó duy trì vóc dáng cân đối. Thành phần Gà và rau tui: Ức gà, gan gà, chickenframe mud, carot, conrstarchm xanhtangun, taurine, amnioacids, vitamin A, D3, khoáng chất Bò và rau tui: Bò, ức gà, gan gà, chickenframe mud, carot, conrstarchm xanhtangun, taurine, amnioacids, vitamin A, D3, khoán…

Thương hiệu: LaPaw.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-cho-moi-lua-tuoi-lapaw-375g', 8, 375, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pate Cho Chó Mọi Lứa Tuổi LaPaw 375g');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Mèo Mọi Lứa Tuổi On25 80g', 'products/paddy_019_pate-cho-meo-on25-80g.jpg', 14000, 0,
       'Pate Cho Mèo Mọi Lứa Tuổi On25 80g Thương hiệu: Cat''s On Phù hợp cho: Mèo mọi lứa tuổi Pate mèo ON25 là thức ăn ướt cao cấp với kết cấu mềm mịn, giàu đạm từ cá ngừ và thịt tươi, giúp mèo ăn ngon miệng mỗi ngày. Sản phẩm dễ tiêu hóa, hỗ trợ da lông khỏe mạnh, phù hợp cả với mèo kén ăn và có nhiều hương vị để thay đổi khẩu phần. Lợi ích Cung cấp nguồn đạm chất lượng cao từ cá và thịt tươi Kích thích vị giác giúp mèo ăn ngon miệng Dễ tiêu hoá, phù hợp cho mèo mọi lứa tuổi Hỗ trợ da khoẻ - lông mượt Bổ sung độ ẩm, hỗ trợ tiêu hoá và tiết niệu Đa dạng hương vị, giúp mèo không bị ngán khi ăn lâu dài Hướng dẫn sử dụng Cho mèo ăn trực tiếp, không cần chế biến Mỗi ngày dùng 1-2 gói tuỳ vào cân nặng và độ tuổi của mèo Có thể trộn cùng hạt không để tăng độ ngon miệng Luôn chuẩn bị nước sạch cho mèo khi ăn Sau khi mở gói, nếu chưa dùng hết nên bảo quản trong tủ lạnh 👉 Xem thêm sản phẩm khác tại Pa…

Thương hiệu: Cat''s On.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-on25-80g', 58, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pate Cho Mèo Mọi Lứa Tuổi On25 80g');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Chó Mọi Lứa Tuổi Nutrience Subzero Prairier Red', 'products/paddy_020_hat-cho-cho-moi-lua-tuoi-nutrience-subzero-prairier-red.jpg', 180000, 0,
       'Hạt Cho Chó Mọi Lứa Tuổi Nutrience Subzero Prairier Red Thương hiệu: Nutrience Phù hợp cho: Chó mọi lứa tuổi Hạt cho chó Nutrience SubZero sử dụng nguồn nguyên liệu tự nhiên giàu dinh dưỡng. Công thức giàu đạm kết hợp cùng các dưỡng chất thiết yếu giúp hỗ trợ tiêu hóa, tăng cường miễn dịch, đồng thời giúp da khỏe và lông bóng mượt. Sản phẩm phù hợp cho chó cần chế độ dinh dưỡng chất lượng và cân bằng mỗi ngày. Lợi ích Giàu protein từ thịt thật giúp phát triển cơ bắp và duy trì thể trạng khoẻ mạnh Taurine hỗ trợ tim mạch và thị lực cho chó Glucosamine & Chondroitin giúp bảo vệ và tăng cường sức khoẻ xương khớp Vitamin và khoáng chất giúp tăng cường hệ miễn dịch Prebiotic, probiotic và omega hỗ trợ tiêu hoá, giúp da khoẻ và lông bóng mượt Thành phần Thịt bò rút xương, thịt cừu rút xương, lợn rừng rút xương, thịt lợn rút xương, bò rút xương, gan bò, gan lợn, bột thịt lợn, đậu Hà Lan, mỡ lợ…

Thương hiệu: Nutrience.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-moi-lua-tuoi-nutrience-subzero-prairier-red', 59, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Hạt Cho Chó Mọi Lứa Tuổi Nutrience Subzero Prairier Red');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Chó Mọi Lứa Tuổi ON25 DOG', 'products/paddy_021_hat-cho-cho-on25-dog.jpg', 20000, 0,
       'Hạt Cho Chó Mọi Lứa Tuổi ON25 DOG Thương hiệu: Cat''s On Phù hợp cho: Chó mọi lứa tuổi Thức ăn hạt cho chó ON25 Dog, với công thức dinh dưỡng cân đối, giàu đạm động vật, hỗ trợ tiêu hóa, giúp chó ăn ngon miệng, khỏe mạnh từ bên trong và bóng đẹp bên ngoài mà Sen nào cũng có thể yên tâm mua lâu dài. Lợi ích Cung cấp nguồn đạm động vật chất lượng cao từ thịt gà, thịt vịt, gan thuỷ phân giúp chó duy trì cơ bắp, tăng sức bền và năng lượng hoạt động mỗi ngày Công thức cân đối dinh dưỡng phù hợp cho chó ở nhiều giai đoạn phát triển Hỗ trợ tiêu hoá khoẻ mạnh nhờ bộ 3 prebiotic FOS & MOS giúp nuôi dưỡng lợi khuẩn đường ruột, giảm rối loại tiêu hoá, phân đẹp và giảm mùi hôi Chăm sóc da lông toàn diện với dầu cá, mỡ gà, vitamin và khoáng chất giúp da khoẻ, lông bóng mượt, hạn chế rụng lông Bổ sung taurine & DL-methionine hỗ trợ tim mạch, hệ thần kinh và duy trì thể trạng khỏe mạnh lâu dài. Hạt thơ…

Thương hiệu: Cat''s On.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-on25-dog', 8, 600, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Hạt Cho Chó Mọi Lứa Tuổi ON25 DOG');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Sữa Dê Cho Mèo Kit Cat Không Lactose', 'products/paddy_022_sua-de-cho-meo-kit-cat-khong-lactose.jpg', 120000, 0,
       'Sữa Dê Cho Mèo Kit Cat Không Lactose Thương hiệu: Kit Cat Phù hợp cho: Mèo mọi lứa tuổi Sữa cho mèo Kit Cat là nguồn dinh dưỡng bổ sung phù hợp cho cả mèo con và mèo trưởng thành. Sản phẩm được làm từ sữa dê không chứa lactose nên dễ tiêu hóa, đặc biệt phù hợp với mèo có hệ tiêu hóa nhạy cảm. Nhờ bổ sung vitamin, khoáng chất và protein thiết yếu, sữa giúp hỗ trợ phát triển khỏe mạnh cho mèo con, đồng thời tăng cường sức khỏe tổng thể và hệ miễn dịch, giúp xương và răng chắc khỏe và duy trì thể trạng tốt cho mèo trưởng thành. Lợi ích Công thức không chứa lactose, phù hợp cho mèo có hệ tiêu hóa nhạy cảm. Bổ sung khoáng chất giúp hỗ trợ phát triển hệ xương và răng. Cung cấp vitamin, protein và khoáng chất cần thiết cho cơ thể mèo. Giúp mèo hấp thu dinh dưỡng tốt hơn và giảm khó chịu đường ruột. Đặc biệt tốt cho mèo con trong giai đoạn tăng trưởng. Hỗ trợ hệ miễn dịch và duy trì sức khỏe tổ…

Thương hiệu: Kit Cat.

Nguồn tham khảo: https://paddy.vn/products/sua-de-cho-meo-kit-cat-khong-lactose', 61, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Sữa Dê Cho Mèo Kit Cat Không Lactose');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Cho Chó Natural Core Sụn Gà 55g', 'products/paddy_023_banh-thuong-cho-cho-natural-core-sun-ga-55g.png', 58000, 0,
       'Bánh Thưởng Cho Chó Natural Core Sụn Gà 55g Thương hiệu: Natural Core Phù hợp cho: Chó mọi lứa tuổi (từ 2 tháng tuổi trở lên) Bánh thưởng cho chó Natural Core Fresh & Tasty Sụn Gà 55g chính là lựa chọn hoàn hảo đến từ Hàn Quốc, được chế biến từ sụn gà tươi tự nhiên, giàu canxi, collagen và protein, giúp xương chắc – răng khỏe – lông mượt. Lợi ích: Thành phần tự nhiên, an toàn: Làm từ 100% sụn gà tươi hữu cơ, không chất bảo quản, không phẩm màu, không hương liệu nhân tạo. Giàu protein & canxi: Giúp xương khớp chắc khỏe, cơ bắp phát triển, đồng thời hỗ trợ răng miệng khỏe mạnh. Kết cấu dai nhẹ: Giúp làm sạch răng, giảm mảng bám và hôi miệng trong khi chó nhai. Hương vị hấp dẫn: Hương thơm tự nhiên của thịt gà kích thích vị giác, phù hợp cả với chó kén ăn. Dễ tiêu hóa: Công thức hữu cơ tốt cho hệ tiêu hóa nhạy cảm, không gây đầy bụng. Hướng dẫn sử dụng Dùng trực tiếp, cho ăn như thức ăn nh…

Thương hiệu: Natural Core.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-cho-natural-core-sun-ga-55g', 62, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Bánh Thưởng Cho Chó Natural Core Sụn Gà 55g');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Dây Xích Cho Chó Mon Ami 3x120cm', 'products/paddy_024_day-xich-cho-cho-mon-ami-3x120cm.png', 65000, 0,
       'Dây Xích Cho Chó Mon Ami 3x120cm Thương hiệu: Mon Ami Phù hợp cho: Chó mọi lứa tuổi Dây Xích Mon Ami 3x120cm là một phụ kiện thú cưng thiết yếu, đáp ứng tối đa nhu cầu vận động và an toàn cho thú cưng của bạn. Với thiết kế chắc chắn và tiện lợi, sản phẩm này sẽ trở thành người bạn đồng hành lý tưởng cho những chú thú cưng của gia đình. Lợi ích: Độ bền cao: Được làm từ chất liệu cao cấp, dây xích có độ bền và chịu lực tốt, đảm bảo an toàn cho thú cưng Độ dài linh hoạt: Với chiều dài 120cm, dây xích tạo không gian vận động thoải mái cho thú cưng đồng thời dễ dàng điều chỉnh Thiết kế thông minh: Móc khoá và khoen xích chắc chắn, giúp bạn dễ dàng gắn và tháo lắp Màu sắc nổi bật: Gam màu hồng tươi tắn, tạo điểm nhấn đáng yêu cho thú cưng An toàn và thân thiện: Chất liệu không gây hại, an toàn cho sức khoẻ của thú cưng Hướng dẫn sử dụng Gắn dây xích vào vòng cổ thú cưng, điều chỉnh độ dài vừa…

Thương hiệu: Mon Ami.

Nguồn tham khảo: https://paddy.vn/products/day-xich-cho-cho-mon-ami-3x120cm', 63, 400, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Dây Xích Cho Chó Mon Ami 3x120cm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Viên Dầu Cá Hồi Omega Cho Chó Mèo (Hộp 60 viên)', 'products/paddy_025_vien-dau-ca-hoi-omega-cho-meo-hop-60-vien.jpg', 80000, 20,
       'Viên Dầu Cá Hồi Omega Cho Chó Mèo (Hộp 60 viên) Thương hiệu: Kamt/ Q8 Phù hợp cho: Chó/Mèo mọi lứa tuổi Thực phẩm chức năng viên dầu cá bổ sung OMEGA-3 cho chó và mèo tăng đề kháng và cung cấp dinh dưỡng cần thiết cho tim mạch, da, lông. Lợi ích Giảm hẳn rụng lông sau 2 tuần sử dụng Chứa nhiều axit béo Omega3 Làm tăng hệ miễn dịch cho chó mèo, kháng viêm Giúp da và lông chắc khỏe, chống rụng lông Hỗ trợ điều trị & phòng tránh sỏi thận ở chó mèo Quy cách : 60 viên/hộp Liều Lượng Khuyên Dùng Chó: 1-2 viên /10kg thể trọng/ngày Mèo: 1 nửa viên/5kg thể trọng/ngày 👉 Xem thêm sản phẩm khác tại Paddy.vn #chamsocthucung #thucphamchucnang #daucachomeo #chamsocchomeo #Q8 #Kamt

Thương hiệu: Q8.

Nguồn tham khảo: https://paddy.vn/products/vien-dau-ca-hoi-omega-cho-meo-hop-60-vien', 64, 200, 'Dinh Dưỡng',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Viên Dầu Cá Hồi Omega Cho Chó Mèo (Hộp 60 viên)');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Cho Chó INU Fonti Xương Gặm Bàn Chải 90g', 'products/paddy_026_banh-thuong-cho-cho-inu-fonti-xuong-gam-ban-chai-90g.jpg', 20000, 0,
       'Bánh Thưởng Cho Chó INU Fonti Xương Gặm Bàn Chải 90g Thương hiệu: INU Fonti Phù hợp cho: Chó mọi lứa tuổi (từ 2 tháng tuổi trở lên) Bánh thưởng xương gặm cho chó giúp làm sạch răng, bổ sung canxi và hỗ trợ giảm căng thẳng, nhàm chán nhờ thỏa mãn hành vi gặm tự nhiên của chó. Lợi ích: Giúp làm sạch răng, loại bỏ mảng bám, hỗ trợ ngăn ngừa hôi miệng và các bệnh răng miệng. Giàu dinh dưỡng, bổ sung thêm vitamin, khoáng chất thiết yếu cho sức khỏe tổng thể. Giúp giảm stress cho chó thông qua hành vi gặm nhai tự nhiên. Có thể dùng như phần thưởng khi huấn luyện chó. Hướng dẫn sử dụng Chỉ cho ăn như thức ăn nhẹ Khẩu phần mỗi ngày có thể điều chỉnh tuỳ thuộc vào độ tuổi, cân nặng và mức độ hoạt động của chó Dùng trực tiếp, không cho vào lò vi sóng Luôn cung cấp nước uống cho bé 👉Xem thêm các sản phẩm khác tại Paddy.vn #banhthuong #banhthuongchocho #xuonggamchocho #inufonti

Thương hiệu: INU Fonti.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-cho-inu-fonti-xuong-gam-ban-chai-90g', 65, 150, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Bánh Thưởng Cho Chó INU Fonti Xương Gặm Bàn Chải 90g');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate TƯƠI The Pet Cho Chó Mèo Biếng Ăn (1kg) - Ship Now/Grab 2H', 'products/paddy_027_pate-meo-the-pet-cho-meo-1kg.jpg', 105000, 13,
       '*Pate tươi được nhập hàng vào lúc 14h-15h hằng ngày, chủ nhật không nhập hàng (một số phân loại hết hàng chỉ có thể giao sau 15h) Pate Tươi Cho Mèo Hỗn Hợp cho Chó Mèo Biếng Ăn được làm từ hỗn hợp cá biển và gan gà tươi nguyên chất thích hợp dùng cho Chó Mèo. CHẤP HẾT TẤT CẢ MÈO BIẾNG ĂN, KHÓ ĂN, KÉN MỌI LOẠI THỨC ĂN. 💯 100% nguyên liệu tự nhiên, không độn rau củ, chứa độ ẩm & đạm tự nhiên cao từ 60-84%. 💯 Năng lượng cao hơn vượt trội so với các dòng sản phẩm khác trên thị trường (trung bình ở mức 400kcal/kg). 💯 Công thức siêu cấp nước, giúp ngăn ngừa sỏi thận. 💯 Với giá chỉ từ 8k/bữa ăn là Boss đã có được bữa ăn thơm ngon, tốt cho sức khỏe. 💯 Chỉ cần bảo quản sản phẩm trong ngăn mát, không cần chế biến hay hâm nóng. Paddy có sẵn có 2 mùi vị thơm ngon #BestSeller, hấp dẫn các bé kén ăn ✅ Hỗn Hợp Gà - cho Chó & Mèo ✅ Hỗn Hợp Cá - cho Mèo ✅ Hỗn Hợp Gà - cho Chó & Mèo Khối lượng: hộp…

Thương hiệu: The Pet.

Nguồn tham khảo: https://paddy.vn/products/pate-meo-the-pet-cho-meo-1kg', 66, 1000, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pate TƯƠI The Pet Cho Chó Mèo Biếng Ăn (1kg) - Ship Now/Grab 2H');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bát Ăn Cho Chó Mèo Inox BOWL Mon Ami', 'products/paddy_028_bat-an-cho-cho-meo-inox-bowl-mon-ami.png', 35000, 0,
       'Bát Ăn Cho Chó Mèo Inox BOWL Mon Ami Thương hiệu: Mon Ami Phù hợp cho: Chó/Mèo mọi lứa tuổi Sản phẩm dụng cụ ăn uống không thể thiếu cho các boss bát ăn inox MON AMI Bowl được làm từ chất liệu thép không gỉ cao cấp và thiết kế giúp chó mèo dễ dàng ăn uống cũng như bảo vệ thức ăn tốt hơn từ ruồi nhặng. Lợi ích Bền và chắc chắn: Inox là một chất liệu rất bền và chắc chắn, không dễ bị vỡ như bát ăn bằng sứ hoặc nhựa. Điều này giúp bát ăn inox lâu bền hơn và ít bị hư hại hơn. Dễ vệ sinh: Inox là một chất liệu dễ vệ sinh, không bám bẩn và không bị nấm mốc. Điều này giúp bạn dễ dàng vệ sinh bát ăn inox sau khi chó mèo ăn xong, giúp ngăn ngừa vi khuẩn phát triển. An toàn cho chó mèo: Inox là một chất liệu an toàn cho chó mèo, không chứa các hóa chất độc hại có thể gây hại cho sức khỏe của chúng. Thành phần Chất liệu: hợp chất inox cao cấp, cao su chống trơn trượt. Hướng dẫn sử dụng Đựng thức ă…

Thương hiệu: Mon Ami.

Nguồn tham khảo: https://paddy.vn/products/bat-an-cho-cho-meo-inox-bowl-mon-ami', 67, 100, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Bát Ăn Cho Chó Mèo Inox BOWL Mon Ami');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Balo Vận Chuyển Cho Chó Mèo Da Cao Cấp Diamond', 'products/paddy_029_balo-van-chuyen-cho-cho-meo-da-cao-cap-diamond.jpg', 300000, 0,
       'Balo Vận Chuyển Cho Chó Mèo Da Cao Cấp Diamond Thương hiệu: Diamond Phù hợp cho: Mèo mọi lứa tuổi (tối đa 5kg) Balo vận chuyển cho mèo nhiều lỗ giúp thú cưng có 1 không gian thoải mái ngoài ra còn hỗ trợ lưu thông không khí tốt, giúp thú cưng luôn cảm thấy dễ chịu, đặc biệt trong những ngày hè nóng bức. Lợi ích: Giúp bạn và thú cưng dễ dàng đồng hành mọi nẻo đường Đế lót bên trong giúp cho balo chắc chắn, thú cưng có thể ngồi vững bên trong mà không lo chật. Có thể quan sát mọi hoạt động của bé trong khi đang vận chuyển. Balo có tay cầm cùng dây đeo chắc chắn, có khóa kéo bền Sản phẩm được làm từ những chất liệu thân thiện, không độc hại và nhất là dễ dàng lau chùi khi bám bẩn. Hệ thống lỗ thoát khí để giảm thiểu tối đa cảm giác bí bách cho vật nuôi mỗi khi nằm bên trong. Thành phần Chất liệu: V ải và nhựa PVC cao cấp Hướng dẫn sử dụng Mở khóa kéo và cho thú cưng của bạn vào bên trong.…

Thương hiệu: Diamond.

Nguồn tham khảo: https://paddy.vn/products/balo-van-chuyen-cho-cho-meo-da-cao-cap-diamond', 68, 500, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Balo Vận Chuyển Cho Chó Mèo Da Cao Cấp Diamond');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Thịt Sấy Cho Chó Mèo Catsdo', 'products/paddy_030_banh-thuong-thit-say-cho-meo-catsdo.jpg', 50000, 0,
       'Bánh Thưởng Thịt Sấy Cho Chó Mèo Catsdo Thương hiệu: Catsdo Phù hợp cho: Chó/Mèo mọi lứa tuổi Thịt sấy cho mèo là một loại snack được thiết kế dành cho thú cưng. Snack thường có kích thước nhỏ, nhiều hương vị khác nhau phù hợp với khẩu vị khác nhau của các bé, có thể được sử dụng như một món ăn nhẹ, phần thưởng trong quá trình huấn luyện hoặc để bổ sung dinh dưỡng cho chó mèo. Lợi ích Tăng cường sức khỏe răng miệng: có độ cứng vừa phải, giúp thú cưng nhai sạch răng, loại bỏ mảng bám và thức ăn thừa. Tăng cường năng lượng: cung cấp một lượng năng lượng vừa phải, duy trì trong suốt cả ngày. Tăng cường sự tập trung: có thể được sử dụng như một phần thưởng trong quá trình huấn luyện, giúp thú cưng tập trung và ghi nhớ các bài học tốt hơn. Cải thiện tâm trạng: có vị thơm ngon, kích thích vị giác. Thành phần Thịt gà, gan gà , DL-Methionine , Calcium, Total Phosphorus, Chất chống Oxi hóa, Kali…

Thương hiệu: Catsdo.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-thit-say-cho-meo-catsdo', 69, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Bánh Thưởng Thịt Sấy Cho Chó Mèo Catsdo');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Nệm Cho Chó Mèo Pupdy Floating Mattress', 'products/paddy_031_nem-cho-cho-meo-pupdy-floating-mattress.jpg', 250000, 0,
       'Nệm Cho Chó Mèo Pupdy Floating Mattress Thương hiệu: Pupdy Phù hợp cho: Chó/Mèo mọi lứa tuổi Nệm cho chó mèo Pupdy Floating Mattress được thiết kế với cảm giác “nằm như bay trên mây”, mang đến cho thú cưng một không gian nghỉ ngơi êm ái, thư giãn và an toàn mỗi ngày. Lõi nệm mềm mại, đàn hồi vừa phải kết hợp cùng thiết kế oval ôm trọn cơ thể giúp bé dễ dàng chìm vào giấc ngủ sâu, cảm thấy được che chở và yêu thương trọn vẹn. Lợi ích Giúp thú cưng ngủ ngon và sâu hơn nhờ cảm giác êm ái như nằm trên mây Nâng đỡ cơ thể đúng cách, không quá lún, không quá cứng, hạn chế áp lực lên xương khớp Thiết kế aval ôm trọn cơ thể, tạo cảm giác an toàn và được che chở khi ngủ Phù hợp nhiều tư thế ngủ Giữ ấm cơ thể, hạn chế thú cưng nằm trực tiếp dưới sàn lạnh Chất liệu vải mềm mịn, thân thiện với da, không gây kích ứng Dễ vệ sinh, dễ giặt, giúp không gian nghỉ ngơi luôn sạch sẽ Giúp thú cưng thư giãn t…

Thương hiệu: Pupdy.

Nguồn tham khảo: https://paddy.vn/products/nem-cho-cho-meo-pupdy-floating-mattress', 8, 3500, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Nệm Cho Chó Mèo Pupdy Floating Mattress');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Que Gặm Sạch Răng Cho Chó Jireho', 'products/paddy_032_banh-thuong-cho-cho-jireho.jpg', 45000, 0,
       'Bánh Thưởng Que Gặm Sạch Răng Cho Chó Jireho Thương hiệu: Jireho Phù hợp cho: Chó mọi lứa tuổi Bánh thưởng cho chó được làm từ thịt gà thật kết hợp thịt heo, sữa, phô mai, bơ đậu phộng và rau củ tự nhiên, mang đến hương vị thơm ngon cùng nguồn protein chất lượng cao hỗ trợ cơ bắp và năng lượng mỗi ngày. Công thức dễ tiêu, bổ sung chất xơ, vitamin và prebiotic, phù hợp cho cả chó có hệ tiêu hóa nhạy cảm. Kết cấu dai mềm vừa phải giúp cún nhai ngon, hỗ trợ làm sạch răng và giảm mảng bám, lý tưởng để dùng hằng ngày như món thưởng an toàn và dinh dưỡng. Lợi ích Bổ sung protein chất lượng cao Dễ tiêu hoá, thân thiện với dạ dày Hỗ trợ da khoẻ, lông mượt Bổ sung vitamin & khoáng chất thiết yếu Kết cấu dai mềm vừa phải Hương vị đa dạng, hấp dẫn Thành phần Tinh bột (bắp, khoai mì, khoai tây), glycerine, sorbitol, gelatin, thịt gà/ức gà, thịt heo, đạm đậu Hà Lan, bột đậu Hà Lan, bột cà rốt, bột k…

Thương hiệu: JirehO.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-cho-jireho', 8, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Bánh Thưởng Que Gặm Sạch Răng Cho Chó Jireho');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Mèo Alpha Pet 70G', 'products/paddy_033_pate-cho-meo-alpha-pet-70g.jpg', 9000, 0,
       'Pate Cho Mèo Alpha Pet 70G Thương hiệu: Alpha Pet Phù hợp cho: Mèo (mọi lứa tuổi) Pate mèo Peptide Alpha Pet là sản phẩm pate cho mèo thế hệ mới, lần đầu tiên mang Peptide tôm - loại đạm thủy phân siêu nhỏ vào khẩu phần ăn của mèo. Khác với protein thô khó hấp thu, peptide giúp cơ thể mèo tiêu hóa nhanh, dễ hấp thu dinh dưỡng và đặc biệt phù hợp với những bé mèo có hệ tiêu hóa nhạy cảm hoặc thường xuyên gặp tình trạng kén ăn. Lợi ích: Tăng cường thị lực và sức khỏe não bộ Hỗ trợ tim mạch và hệ miễn dịch Duy trì làn da khỏe mạnh, bộ lông mượt mà Công nghệ peptide: đạm thủy phân từ tôm, sinh khả dụng cao, dễ hấp thu Kích thích vị giác: mùi vị thơm ngon, phù hợp cả mèo kén ăn Cân bằng dinh dưỡng: giàu đạm, ít béo, tốt cho hệ tiêu hóa và tim mạch Hướng dẫn sử dụng Có thể cho ăn trực tiếp và chỉ cần bổ sung thêm nước là có thể duy trì sức khỏe bình thường của thú cưng. Điều chỉnh liều lượng…

Thương hiệu: Alpha Pet.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-alpha-pet-70g', 72, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pate Cho Mèo Alpha Pet 70G');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Mèo Real & Raw Catidea Lon 170g', 'products/paddy_034_pate-cho-meo-real-raw-catidea-170g.jpg', 42000, 0,
       'Pate Cho Mèo Real & Raw Catidea 170g Thương hiệu: Catidea Phù hợp cho: Mèo mọi lứa tuổi Pate mèo Catidea Real & Raw là dòng thức ăn cao cấp dành cho mèo, được chế biến từ các loại thịt tươi sống chất lượng cao như đà điểu, nai, thỏ, cá mập kết hợp cùng thịt bò, gà, vịt và cá hồi, mang đến hương vị phong phú mà mèo khó cưỡng. Lợi ích Giàu protein cao cấp Cung cấp năng lượng tối ưu Nguyên liệu thịt tươi sống đa dạng Bổ sung taurine thiết yếu Giàu vitamin & khoáng chất Dễ tiêu hóa – hấp thu tốt Hương vị tự nhiên, hấp dẫn Thành phần Thịt tươi đông lạnh gồm đà điểu / nai / thỏ/cá mập, ức gà, ức vịt, thịt bò, cá hồi; gà và vịt có xương; nội tạng giàu dinh dưỡng (gan, tim, phổi từ gà, vịt, bò); nước dùng thịt, bột trứng, dầu cá cơm; các thành phần tự nhiên hỗ trợ sức khỏe gồm nam việt quất, nghệ, rong biển, việt quất, táo, rau bina, bí ngô, cà rốt (dạng bột/khô). Thành phần dinh dưỡng Protein…

Thương hiệu: Catidea.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-real-raw-catidea-170g', 73, 200, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pate Cho Mèo Real & Raw Catidea Lon 170g');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Cào Móng Giấy Cho Mèo Mọi Lứa Tuổi FOFOS', 'products/paddy_035_cao-mong-giay-cho-meo-moi-lua-tuoi-fofos.jpg', 285000, 0,
       'Cào Móng Giấy Cho Mèo Mọi Lứa Tuổi FOFOS Thương hiệu: Fofos Phù hợp cho: Mèo mọi lứa tuổi Bàn cào móng cho mèo là món đồ chơi kết hợp giữa giải trí và nghỉ ngơi cho mèo cưng. Thiết kế sáng tạo, ngộ nghĩnh không chỉ giúp mèo có không gian riêng thoải mái mà còn góp phần tạo điểm nhấn độc đáo cho không gian sống của bạn. Lợi ích Giúp mèo mài móng tự nhiên, giảm tình trạng móng dài và bong tróc Tạo không gian trú ẩn kín đáo, mang lại cảm giác an toàn cho mèo Hạn chế mèo cào phá sofa, rèm cửa và đồ nội thất Khuyến khích mèo vận động, chơi đùa và giảm buồn chán Thiết kế bắt mắt, có thể dùng như vật trang trí trong nhà Thành phần Giấy carton nén chất lượng cao, chắc chắn và thân thiện môi trường 👉Xem thêm các sản phẩm khác tại Paddy.vn #bancaomong #bancaomongmeo #fofos

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/cao-mong-giay-cho-meo-mọi-lua-tuoi-fofos', 74, 600, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Cào Móng Giấy Cho Mèo Mọi Lứa Tuổi FOFOS');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Đồ Chơi Dây Thừng Cho Chó FOFOS', 'products/paddy_036_do-choi-day-thung-cho-cho-fofos.jpg', 98000, 0,
       'Đồ Chơi Dây Thừng Cho Chó FOFOS Thương hiệu: Fofos Phù hợp cho: Chó mọi lứa tuổi Đồ chơi cho chó dây thừng là sản phẩm hỗ trợ chó giải trí và vận động mỗi ngày, đặc biệt phù hợp với các bé có thói quen cắn gặm. Sản phẩm giúp chó giảm buồn chán, hạn chế hành vi phá đồ đạc và góp phần chăm sóc răng miệng hiệu quả. Lợi ích Giúp chó giảm căng thảng, giải toả năng lượng dư thừa Hạn chế việc cắn phá đồ đạc trong nhà Hỗ trợ làm sạch răng, loại bỏ cặn thức ăn bám trong miệng Giảm mùi hôi miệng cho chó Thiết kế dây thừng chắc chắn, chịu lực cắn tốt Kích thước lớn, phù hợp cho nhiều độ tuổi và kích cỡ chó Thành phần Dây thừng bền bỉ, an toàn cho thú cưng Phân loại Dây Thắt 3 Đầu: 45x5,5x4,5cm Pastel: 22x6x6cm Hot Dog 30x6cm Phô mai 30x6cm Hướng dẫn sử dụng Đưa đồ chơi cho chó cắn gặm hoặc hoặc chơi kéo co Nên giám sát chó trong quá trình chơi để đảm bảo an toàn Kiểm tra đồ chơi thường xuyên, ngưn…

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/do-choi-day-thung-cho-cho-fofos', 75, 300, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Đồ Chơi Dây Thừng Cho Chó FOFOS');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Đồ Chơi Cho Mèo FOFOS Set 6 Món', 'products/paddy_037_do-choi-meo-fofos-set-6-mon.jpg', 110000, 0,
       'Đồ Chơi Cho Mèo FOFOS Set 6 Món Thương hiệu: Fofos Phù hợp cho: Mèo mọi lứa tuổi Đồ chơi cho mèo FOFOS là bộ đồ chơi dành cho mèo được thiết kế nhằm mang đến những giờ phút vui chơi thú vị và bổ ích mỗi ngày. Sản phẩm giúp mèo vận động nhiều hơn, giải tỏa căng thẳng và hạn chế cảm giác buồn chán khi ở nhà một mình, đồng thời góp phần duy trì sức khỏe và sự linh hoạt cho mèo cưng. Lợi ích Đa dạng cách chơi với 6 món đồ khác nhau Kích thích bản năng săn mồi Giải toả căng thẳng, giảm stress Tăng cường vận động Tăng tương tác Thành phần Vải bông mềm Lông vũ tự nhiên Nhựa an toàn cho thú cưng Giấy kính tạo tiếng động Hướng dẫn sử dụng Mở bao bì và cho mèo làm quen từng món đồ chơi Ném hoặc di chuyển đồ chơi để mèo đuổi bắt và vận động Có thể giấu đồ chơi ở nhiều vị trí trong nhà để tạo trò chơi tìm kiếm Vệ sinh định kỳ và thay mới khi đồ chơi bị hư hỏng để đảm bảo an toàn cho mèo 👉Xem thêm…

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/do-choi-meo-fofos-set-6-mon', 76, 300, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Đồ Chơi Cho Mèo FOFOS Set 6 Món');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Mèo Whiskas 80g [Hộp 6 gói]', 'products/paddy_038_pate-cho-meo-whiskas-80g-hop-6-goi.jpg', 65000, 0,
       'Pate Cho Mèo Whiskas 80g [Hộp 6 gói] Thương hiệu: Whiskas Phù hợp cho: Mèo từ 2 đến 12 tháng tuổi vào mèo từ 1 tuổi trở lên Pate mèo whiskas được chế biến đặc biệt để đáp ứng nhu cầu dinh dưỡng của mèo ở mọi giai đoạn phát triển. Khi mèo con bước sang tháng 12, chúng chính thức chuyển sang giai đoạn trưởng thành và cần một tỷ lệ dinh dưỡng hoàn toàn khác để duy trì vóc dáng săn chắc, năng động và khoẻ mạnh Lợi ích Làm từ cá thật tươi ngon, mèo mê ngay từ miếng đầu tiên Giàu Omega 3, Omega 6 và Kẽm, cho bộ lông óng mượt, khỏe mạnh. Bổ sung Vitamin A & Taurine, hỗ trợ thị lực sáng khỏe Protein chất lượng cao từ cá thật, vitamin & khoáng chất Giúp mèo duy trì cơ thể săn chắc, năng động, và luôn tràn đầy năng lượng. Chất chống oxy hóa (Vitamin E & Selenium) – Tăng cường hệ miễn dịch Thành phần Nước, thịt gà, xương gà, cá ngừ, phụ phẩm cá thu, Surimi cá trắng, chất tạo hương, axit amin, phụ…

Thương hiệu: Whiskas.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-whiskas-80g-hop-6-goi', 77, 600, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pate Cho Mèo Whiskas 80g [Hộp 6 gói]');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Xịt Khử Mùi Cát Vệ Sinh Cho Mèo Arm & Hammer 636ml', 'products/paddy_039_xit-khu-mui-cat-ve-sinh-cho-meo-arm-hammer-636ml.jpg', 280000, 0,
       'Xịt Khử Mùi Cát Vệ Sinh Cho Mèo Arm & Hammer 636ml Thương hiệu: Arm & Hammer Phù hợp cho: Mèo mọi lứa tuổi/Mọi loại cát vệ sinh Nước xịt khử mùi cho mèo ARM & HAMMER giúp khử mùi tức thì và kéo dài tuổi thọ của cát vệ sinh. Công thức mạnh mẽ dành cho nhiều loại mèo chứa baking soda, giúp loại bỏ mùi hôi trong hộp cát, mang lại sự tươi mát như ngày đầu tiên mỗi khi sử dụng! Sản phẩm phù hợp với TẤT CẢ các loại cát vệ sinh và không ảnh hưởng đến khả năng vón cục Lợi ích An toàn khi sử dụng hàng ngày xung quanh mèo và chủ nuôi Hiệu quả với mọi loại cát vệ sinh Xịt dạng sương mịn, không ảnh hưởng đến khả năng vón cục Thành phần Chứa nước, bột nở ARM & HAMMER, hương liệu và các thành phần khác giúp ngăn ngừa vón cục và đóng vảy. Hướng dẫn sử dụng Xịt trực tiếp lên cát vệ sinh của mèo sau mỗi lần thay hoặc vệ sinh để duy trì hương thơm và giảm mùi hôi. 👉 Xem thêm sản phẩm khác tại Paddy.vn #…

Thương hiệu: Arm & Hammer.

Nguồn tham khảo: https://paddy.vn/products/xit-khu-mui-cat-ve-sinh-cho-meo-arm-hammer-636ml', 8, 800, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Xịt Khử Mùi Cát Vệ Sinh Cho Mèo Arm & Hammer 636ml');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo Mọi Lứa Tuổi Today''s Dinner', 'products/paddy_040_hat-cho-meo-moi-lua-tuoi-todays-dinner.jpg', 70000, 0,
       'Hạt Cho Mèo Mọi Lứa Tuổi Today''s Dinner Thương hiệu: Today Dinner Phù hợp cho: Mèo mọi lứa tuổi Hạt cho mèo Today''s Dinner là dòng thức ăn hoàn chỉnh cho mèo từ 3 tháng tuổi trở lên với công thức giàu protein động vật, vitamin, khoáng chất và taurine giúp mèo ăn ngon, tăng sức đề kháng, hỗ trợ da lông, tiêu hoá, và hệ tiết niệu. Sản phẩm đạt chuẩn an toàn và lành mạnh cho mèo sử dụng hằng ngày Lợi ích Kích thích vị giác: Hạt chứa nguồn protein động vật chất lượng cao và đã được thử nghiệm qua vật nuôi, giúp kích thích vị giác ngay cả những chú mèo kén ăn Giàu chất xơ thực vật giúp đào thải lông vón cục, kiểm soát được búi lông Cân bằng khoáng chất thích hợp cho mèo, duy trì được độ pH trong nước tiểu và ngăn ngừa bệnh sỏi thận. Tăng cường được sức khoẻ tiết niệu. Da lông bóng mượt nhờ có hàm lượng axit béo Omega 3&6 duy trì được da lông khoẻ mạnh Giảm mùi hôi của phân nhờ chiết xuất từ…

Thương hiệu: Today Dinner.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-moi-lua-tuoi-todays-dinner', 79, 1000, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Hạt Cho Mèo Mọi Lứa Tuổi Today''s Dinner');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Võng Gỗ Cho Mèo CATCA', 'products/paddy_041_vong-go-cho-meo-catca.jpg', 260000, 0,
       'Võng Gỗ Cho Mèo CATCA Thương hiệu Catca Phù hợp cho: Mèo dưới 10kg Võng Cho Mèo CATCA là giải pháp nghỉ ngơi thoải mái cho thú cưng với thiết kế nằm thoáng mát và êm ái. Sản phẩm lắp ráp dễ dàng không cần ốc vít, chắc chắn và an toàn, phù hợp cho mèo và thú cưng dưới 10kg. Lợi ích Lắp dễ dàng - Thiết kế không ốc vít, lắp ghép cực dễ Dễ vệ sinh - Tấm vải võng tháo rời dễ dàng, giặt máy vô tư Cạch gỗ bo tròn thẩm mỹ, không sắc tay Kích thước: Dài 62cm - Cao 22cm – Rộng 39cm Chịu lực tối đa: 20kg 👉 Xem thêm sản phẩm khác tại Paddy.vn #chamsocthucung #dochoichothucung #vongmeo #catca

Thương hiệu: Catca.

Nguồn tham khảo: https://paddy.vn/products/vong-go-cho-meo-catca', 80, 100, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Võng Gỗ Cho Mèo CATCA');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Chó Pedigree 80g', 'products/paddy_042_pate-cho-cho-pedigree-80g.jpg', 13000, 0,
       'Pate Cho Chó Pedigree 80g Thương hiệu: Pedigree Phù hợp cho: Chó mọi lứa tuổi (phân loại trong sản phẩm) Pate chó Pedigree là dòng thức ăn ướt cho chó được phát triển bởi các chuyên gia dinh dưỡng WALTHAM (Anh Quốc) – cơ quan hàng đầu thế giới về chăm sóc và dinh dưỡng vật nuôi. Sản phẩm mang đến chế độ dinh dưỡng đầy đủ và cân bằng, phù hợp cho cả cún con lẫn cún trưởng thành, giúp đáp ứng trọn vẹn nhu cầu dinh dưỡng ở từng giai đoạn phát triển. Với hương vị thơm ngon, đa dạng, Pedigree Pouch giúp cún ăn ngon miệng hơn mỗi ngày và luôn tràn đầy năng lượng. Lợi ích Dinh dưỡng đầy đủ & cân bằng cho cúng ở mọi độ tuổi Bổ sung chất chống oxy hoá, hỗ trợ hệ miễn dịch khoẻ mạnh Hương vị đa dạng, thơm ngon, giúp cún không bị ngán Phù hợp cho cún con và cún trưởng thành, dùng linh hoạt theo từng giai đoạn Giúp cún vui khoẻ, năng động và phát triển toàn diện mỗi ngày Thành phần Phụ phẩm thịt gà…

Thương hiệu: Pedigree.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-cho-pedigree-80g', 81, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pate Cho Chó Pedigree 80g');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Chó Trưởng Thành Gran Deli 700g', 'products/paddy_043_hat-cho-cho-truong-thanh-gran-deli-700g.jpg', 90000, 0,
       'Hạt Cho Chó Trưởng Thành Gran Deli 700g Thương hiệu: Silver Spoon Phù hợp cho: Chó trưởng thành Thức ăn hạt cho chó Gran Deli cao cấp đến từ Nhật Bản, được thiết kế với hương vị thơm ngon và thành phần dinh dưỡng cân bằng cùng với 3 loại topping hảo hạng. Sản phẩm cung cấp đầy đủ protein từ thịt bò, phi lê gà, cá và rau củ, giúp cún yêu phát triển khỏe mạnh. Đặc biệt, Gran-Deli bổ sung Canxi và Phốt pho hỗ trợ xương và răng chắc khỏe, cùng các vitamin và khoáng chất thiết yếu cho sức đề kháng tốt. Lợi ích Hương vị tự nhiên, dễ ăn, kích thích vị giác Cung cấp protein, canxi, vitamin và khoáng chất giúp cơ bắp, xương, răng khoẻ mạnh Kết cấu giòn mềm dễ nhai, phù hợp với mọi giống chó Dinh dưỡng cân bằng hỗ trợ sức khoẻ toàn thân và duy trì thể trạng lý tưởng Thành phần Ngũ cốc (Ngô, vụn bánh mì, bột mì), các loại thịt (Bột thịt gà, chiết xuất gà, thịt gà, thịt bò, bột thịt lợn, bột thịt b…

Thương hiệu: Silver Spoon.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-truong-thanh-gran-deli-700g', 82, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Hạt Cho Chó Trưởng Thành Gran Deli 700g');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Gel Dinh Dưỡng & Chức Năng Cho Mèo Kit Cat', 'products/paddy_044_gel-dinh-duong-chuc-nang-cho-meo-kit-cat.jpg', 90000, 0,
       'Gel Dinh Dưỡng & Chức Năng Cho Mèo Kit Cat Thương hiệu: Kit Cat Phù hợp cho: Mèo từ 3 tháng tuổi trở lên Gel dinh dưỡng cho mèo Kit Cat là dòng gel bổ sung tiện dụng, được thiết kế để hỗ trợ từng nhu cầu sức khỏe riêng của mèo. Kết cấu gel mềm mịn, hương vị hấp dẫn giúp mèo dễ dàng tiếp nhận, có thể cho ăn trực tiếp hoặc trộn cùng bữa ăn hằng ngày. Công thức phù hợp cho cả mèo đã triệt sản lẫn chưa triệt sản, đảm bảo bổ sung dinh dưỡng nhẹ nhàng và hiệu quả. Lợi ích: Kit Cat Supplement Gel – Weight Gain (Tăng Cân) Hỗ trợ tăng cân lành mạnh: Công thức giàu dinh dưỡng, giúp mèo lên cân đều và an toàn. Nhiều calorie – dễ hấp thu: Dạng gel thơm ngon cung cấp năng lượng cao, phù hợp cho mèo gầy, mới bệnh, biếng ăn hoặc cần cải thiện thể trạng. Tăng cơ – cải thiện vóc dáng: Bổ sung dưỡng chất giúp hỗ trợ phát triển khối cơ và cải thiện body condition. An toàn – hiệu quả từ từ: Tăng cân một cá…

Thương hiệu: Kit Cat.

Nguồn tham khảo: https://paddy.vn/products/gel-dinh-duong-chuc-nang-cho-meo-kit-cat', 83, 150, 'thực phẩm chức năng',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Gel Dinh Dưỡng & Chức Năng Cho Mèo Kit Cat');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Cho Mèo Temptations Thơm Ngon 75g', 'products/paddy_045_banh-thuong-cho-meo-temptations-thom-ngon-75g.png', 49000, 0,
       'Bánh Thưởng Cho Mèo Temptations Thơm Ngon 75g Thương hiệu: Temptations Phù hợp cho: Mèo mọi lứa tuổi Bánh Thưởng Cho Mèo Temptations có lớp vỏ giòn và nhân kem mềm được làm từ thịt và các nguyên liệu có nguồn gốc động vật, mang đến cho mèo chút bất ngờ ngon lành, bổ dưỡng. Bánh Temptations bổ sung vitamin, chất chống oxy hóa, chiết xuất thực vật, axit amin và axit béo Omega, không chỉ kích thích vị giác mà còn tăng cường sức khỏe của mèo. Lợi ích: Sự kết hợp độc đáo giữa lớp vỏ ngoài giòn rụm và lớp nhân bên trong mềm mịn với hương vị hấp dẫn khó cưỡng. Chứa ít hơn 2 calo và không có hương liệu nhân tạo, là món bánh thưởng hoàn hảo, duy trì lối sống lành mạnh cho bé mèo Được chế biến đặc biệt dành cho mèo trưởng thành từ 1 tuổi trở lên với khoáng chất, taurine và chất chống oxy hóa Túi đựng có khóa tiện lợi giúp dễ dàng cất trữ mà vẫn giữ được độ giòn của sản phẩm Hướng dẫn sử dụng Khuy…

Thương hiệu: Temptations.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-meo-temptations-thom-ngon-75g', 84, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Bánh Thưởng Cho Mèo Temptations Thơm Ngon 75g');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Máy Dọn Phân Tự Động Cho Mèo Petree Version 2', 'products/paddy_046_may-don-phan-tu-dong-cho-meo-petree-version-2.jpg', 5250000, 0,
       'Máy Dọn Phân Tự Động Cho Mèo Petree Version 2 Thương hiệu: Petree Phù hợp cho: Mèo mọi lứa tuổi Máy vệ sinh tự động cho mèo Petree là trợ thủ đắc lực giúp bạn tối ưu hóa thời gian, giải quyết mọi lo lắng về vệ sinh một cách tự động và hiệu quả. Với thiết kế sản phẩm nhỏ gọn hơn, hệ thống cảm biến và khử mùi thông minh, hiện đại hơn, lưới lọc thiết kế mới cho phép sử dụng đa dạng các loại cát. Lợi ích: Công nghệ chống kẹt mới Khử mùi tốt Chế độ dọn dẹp thông minh, sạch sẽ, yên tĩnh Có thể tháo rời thân máy để cọ rửa vệ sinh dễ dàng Dễ sử dụng, app có hỗ trợ Tiếng Anh và Tiếng Việt Thành phần Kích thước máy: 52 x 52 x 64 cm Trọng lượng: 10kg Điện áp tiêu chuẩn: 12V – 5W Kích thước khay chất thải: 6L Hướng dẫn sử dụng Đặt máy dọn phân tại nơi thuận tiện cho mèo của bạn Đổ cát vệ sinh vào nhà vệ sinh. Máy sẽ tự động làm việc để dọn phân và lọc cát vệ…

Thương hiệu: Petree.

Nguồn tham khảo: https://paddy.vn/products/may-don-phan-tu-dong-cho-meo-petree-version-2', 8, 10000, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Máy Dọn Phân Tự Động Cho Mèo Petree Version 2');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Máy Dọn Phân Tự Động Cho Mèo Neakasa', 'products/paddy_047_may-don-phan-tu-dong-cho-meo-neakasa.jpg', 7900000, 0,
       'Máy Dọn Phân Tự Động Cho Mèo Neakasa Thương hiệu: Neakasa Phù hợp cho: Mèo mọi lứa tuổi Máy vệ sinh tự động cho mèo được làm từ các chất liệu chất lượng cao như PP, ABS và POM, có khả năng chịu được va chạm tốt, giúp đảm bảo độ bền trong suốt quá trình sử dụng. Đồng thời, các chất liệu này an toàn cho sức khỏe của mèo. Lợi ích Tự động dọn sạch phân mèo, không cần sự can thiệp trực tiếp từ người nuôi, giữ sạch sẽ nơi mèo đi vệ sinh. Thiết kế mở mang đến không gian rộng rãi và thoải mái cho mèo trong quá trình sử dụng. Hệ thống xử lý chất thải “Kéo và Bọc” giúp quá trình dọn dẹp không có mùi hôi và không tiếp xúc trực tiếp. Phù hợp cho gia đình nuôi nhiều mèo, bao gồm cả mèo con và mèo đang mang thai. Trang bị nhiều cảm biến thông minh đem lại sự an toàn cho mèo khi dùng. Chất liệu Nhựa PP Nhựa ABS POM 👉 Xem thêm sản phẩm khác tại Paddy.vn #nhavesinhmeo #nhavesinhchomeo #nhavesinhthucung…

Thương hiệu: Neakasa.

Nguồn tham khảo: https://paddy.vn/products/may-don-phan-tu-dong-cho-meo-neakasa', 41, 10000, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Máy Dọn Phân Tự Động Cho Mèo Neakasa');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Súp Thưởng Cho Mèo Bite of Wild (15gx4)', 'products/paddy_048_sup-thuong-cho-meo-bite-of-wild-15gx4.jpg', 20000, 0,
       'Súp Thưởng Cho Mèo Bite of Wild (15gx4) Thương hiệu: Bite of Wild Phù hợp cho: Mèo mọi lứa tuổi Súp thưởng cho mèo Bite of Wild cung cấp nguồn protein chất lượng cao, không gelatin, không tinh bột, không chất phụ gia, bổ sung nhóm vitamin cần thiết giúp tăng cường hệ miễn dịch, mắt sáng khỏe và lông bóng mượt cho mèo. Lợi ích: Hàm lượng thịt cao giúp mèo ăn ngon và đủ dưỡng chất. Không chứa tinh bột, không gelatin, không ngũ cốc – nhẹ bụng, phù hợp cả mèo nhạy cảm. Ít chất béo giúp duy trì dáng chuẩn, hỗ trợ bộ lông mượt & mắt sáng nhờ taurine và vi khoáng. Dạng súp dễ ăn, cấp ẩm, kích thích vị giác Thành phần Vị gà: ức gà 60%, tim gà 8%, gan gà 3%. Vị cá hồi: cá hồi 45%, ức gà 20%, tim gà 5%, gan gà 3%. Ngoài ra: hạt mã đề, hạt lanh, dầu cá/gà, chiết xuất Yucca Advance. Hướng dẫn sử dụng Dùng làm thưởng cho mèo: khi huấn luyện, sau khi tắm, cắt móng hoặc mèo ít ăn. Có thể trộn vào thức…

Thương hiệu: Bite of Wild.

Nguồn tham khảo: https://paddy.vn/products/sup-thuong-cho-meo-bite-of-wild-15gx4', 8, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Súp Thưởng Cho Mèo Bite of Wild (15gx4)');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Cho Chó Gà Sấy DoggyMan Pawfect Choice 180g', 'products/paddy_049_snack-ga-say-cho-cho-doggyman-pawfect-choice-180g.jpg', 130000, 0,
       'Bánh Thưởng Cho Chó Gà Sấy DoggyMan Pawfect Choice 180g Thương hiệu: DoggyMan Phù hợp cho: Chó từ 3 tháng tuổi trở lên Que thưởng snack cho chó được làm từ nguyên liệu tự nhiên chất lượng cao, kết hợp lớp que bột bắp dễ tiêu hóa và thịt tươi sấy dẻo hoặc sấy khô giàu protein. Sản phẩm giúp bổ sung vitamin, khoáng chất và năng lượng cần thiết, hỗ trợ phát triển cơ bắp, duy trì thể trạng khỏe mạnh và tăng cường sức đề kháng. Lợi ích Cung cấp protein chất lượng cao, giúp phát triển cơ bắp và duy trì thể trạng khoẻ mạnh Bổ sung vitamin và khoáng chất thiết yếu, hỗ trợ sức đề kháng và năng lượng hằng ngày Dễ tiêu hoá, phù hợp với hệ tiêu hoá nhạy cảm của chó Giàu hương vị tự nhiên, kích thích vị giác, giúp chó ăn ngon miệng hơn Có thể dùng như phần thưởng huấn luyện hoặc món ăn vặt bổ sung dinh dưỡng Thành phần Bột bắp, thịt gà, đạm đậu Hà Lan, fructose, gelatine, khoai lang, cà rốt, đậu, si…

Thương hiệu: DoggyMan.

Nguồn tham khảo: https://paddy.vn/products/snack-ga-say-cho-cho-doggyman-pawfect-choice-180g', 43, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Bánh Thưởng Cho Chó Gà Sấy DoggyMan Pawfect Choice 180g');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo Mọi Lứa Tuổi Cattyman', 'products/paddy_050_hat-cho-meo-moi-lua-tuoi-cattyman.jpg', 30000, 0,
       'Hạt Cho Mèo Mọi Lứa Tuổi Cattyman Thương hiệu: Cattyman Phù hợp cho: Mèo mọi lứa tuổi ( Từ 2 tháng tuổi trở lên) Thức ăn hạt cho mèo CattyMan được nghiên cứu dành cho nhu cầu dinh dưỡng của mèo ở mọi lứa tuổi. Sản phẩm cung cấp tỷ lệ cân bằng giữa đạm, chất béo, vitamin và khoáng chất, giúp mèo phát triển khỏe mạnh, duy trì thể trạng lý tưởng và bộ lông mềm mượt. Lợi ích Hỗ trợ hô hấp, chống virus, khoẻ mạnh hơn Giảm căng thẳng, ngủ ngon, tinh thần ổn định Lông bónng mượt, bảo vệ gan và tiết niệu Ngăn mù loà, tim khoẻ, sinh sản tốt Thành phần Ngô, thịt(bột gà, bột gan gà, bột thịt bò), bột gluten ngô, bột mì, đậu nành đã tách béo, mỡ động vật, bột cá, chiết xuất cá, bột bã đậu nành, hạt lanh rang, oligosaccharides, vi khuẩn axit lactic, khoáng chất( Na, Cl, Ca, P, K, Zn, Fe, Mn, Cu, Co, l), vitamin (A, B1, B2, B6, B12, C, D3, E, K3 choline, axit nicitinic, axit pantothenic, axit folic,…

Thương hiệu: CattyMan.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-moi-lua-tuoi-cattyman', 44, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Hạt Cho Mèo Mọi Lứa Tuổi Cattyman');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo Mọi Lứa Tuổi S2Pet V1-V2-V3', 'products/paddy_051_hat-cho-meo-moi-lua-tuoi-s2pet-v1-v2-v3.jpg', 90000, 0,
       'Hạt Cho Mèo Mọi Lứa Tuổi S2Pet V1-V2-V3 Thương hiệu: S2Pet Phù hợp cho: Mèo mọi lứa tuổi Thức ăn hạt cho mèo được chế biến từ nguồn nguyên liệu tự nhiên và giàu dinh dưỡng, mang đến bữa ăn thơm ngon và cân bằng cho thú cưng. Với công thức đặc biệt 30% topping hấp dẫn gồm trứng gà, gà tươi,... không chỉ kích thích vị giác mà còn tăng cường hàm lượng đạm và dưỡng chất cho thú cưng. Lợi ích V1: Siêu Topping (30% topping) Cung cấp dinh dưỡng toàn diện, giúp thú cưng khỏe mạnh, năng động. Hỗ trợ phát triển cơ bắp, duy trì sự dẻo dai và sức đề kháng. Giúp bộ lông bóng mượt, làn da khỏe mạnh. Cung cấp năng lượng cho các hoạt động thể chất của thú cưng. V2: HairBall (70% topping) Cung cấp dinh dưỡng toàn diện cho thú cưng với các thành phần chất lượng cao. Giúp tăng cường sức đề kháng và bảo vệ sức khỏe lâu dài. Duy trì một bộ lông bóng mượt, da khỏe mạnh. Cung cấp năng lượng cho các hoạt động…

Thương hiệu: S2Pet.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-moi-lua-tuoi-s2pet-v1-v2-v3', 45, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Hạt Cho Mèo Mọi Lứa Tuổi S2Pet V1-V2-V3');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Mèo Cao Cấp Bite of Wild Túi 70G', 'products/paddy_052_pate-cho-meo-cao-cap-bite-of-wild-tui-70g.jpg', 17000, 0,
       'Pate Cho Mèo Cao Cấp Bite of Wild Túi 70G Thương hiệu: Bite of Wild Phù hợp cho: Mèo mọi lứa tuổi Pate Cho Mèo cao cấp Bite of Wild là sự kết hợp đạm tự nhiên và sữa dê New Zealand 0% lactose – công thức gần nhất với sữa mẹ, hỗ trợ tiêu hóa & tăng đề kháng. Lợi ích: Bổ sung dinh dưỡng cân bằng, phù hợp mọi lứa tuổi. Tăng miễn dịch & giúp lông – da khỏe đẹp. Cung cấp protein nạc, dễ hấp thu. Kiểm soát cân nặng, tốt cho mèo nhạy cảm tiêu hóa. Giúp cơ bắp phát triển, hệ tim mạch & tiêu hóa khỏe. Thành phần Goat Milk – Tuna (50%): ít béo, giàu Omega-3 cho cơ bắp & lông bóng mượt. Goat Milk – Salmon (50%): tăng trí não, tim khỏe, lông óng mượt. Goat Milk – Chicken (50%): dễ tiêu, hạn chế dị ứng. FOS & Lactoferrin: cân bằng vi sinh đường ruột, tăng đề kháng. Cà rốt, Bí đỏ, Dứa: giàu chất xơ, enzyme, beta-carotene. Taurine & Vitamin A, B: tốt cho tim mạch, thị lực & năng lượng. Hướng dẫn sử dụ…

Thương hiệu: Bite of Wild.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-cao-cap-bite-of-wild-tui-70g', 8, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pate Cho Mèo Cao Cấp Bite of Wild Túi 70G');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Sữa Tươi Kyushu Cho Chó Mèo Không Chứa Lactose 200ml', 'products/paddy_053_sua-tuoi-kyushu-cho-cho-meo-khong-chua-lactose-200ml.jpg', 32000, 0,
       'Sữa Tươi Kyushu Cho Chó Mèo Không Chứa Lactose 200ml Thương hiệu: DoggyMan Phù hợp cho: Chó/Mèo mọi lứa tuổi Sữa cho chó mèo Kyushu chứa protein chất lượng cao, bổ sung dinh dưỡng hằng ngày, chăm sóc sức khỏe cho thú cưng. Đặc biệt, sản phẩm không chứa lactose, không chứa chất tạo màu, không sử dụng chất bảo quản đảm bảo an toàn cho thú cưng. Lợi ích Bổ sung protein tinh khiếp, giúp phát triển cơ bắp và tăng cường sức khoẻ Chứa tảuine, hỗ trợ thị lực và chức năng tim mạch Không chứa lactose - phù hợp cho thú cưng có hệ tiêu hoá nhạy cảm Cung cấp năng lượng, vitamin và khoáng chất cần thiết mỗi ngày Hỗ trợ duy trì làn da khoẻ mạnh, bộ lông bóng mượt Giúp thú cưng ăn ngon miệng hơn, đặc biệt thích hợp cho chó mèo biếng ăn hoặc sau ốm Thành phần Sữa tươi, enzyme lactase, chất chống oxy hoá (sulfate), taurine. 👉Xem thêm các sản phẩm khác tại Paddy.vn #suachochomeo #suatuoichomeo #suachocho…

Thương hiệu: DoggyMan.

Nguồn tham khảo: https://paddy.vn/products/sua-tuoi-kyushu-cho-cho-meo-khong-chua-lactose-200ml', 47, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Sữa Tươi Kyushu Cho Chó Mèo Không Chứa Lactose 200ml');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo Cature Easy Farm Topping 1.5kg', 'products/paddy_054_hat-cho-meo-cature-easy-farm-topping-1-5kg.jpg', 195000, 0,
       'Hạt Cho Mèo Cature Easy Farm Topping 1.5kg Thương hiệu: Cature Phù hợp cho: Mèo mọi lứa tuổi Thức ăn hạt cho mèo Cature Easy Farm là sự lựa chọn hoàn hảo giúp thú cưng khoẻ mạnh từ trong ra ngoài. Với 90% protein động vật, kết hợp cùng 7 loại siêu thực phẩm chống oxy hoá ( việt quất, nghệ, hạt nho, aronia berry, acai, nam việt quất, mâm xôi đen) và quy trình "phủ lạnh" giữ được trọn dinh dưỡng, sản phẩm mang đến nguồn năng lượng tự nhiên, hỗ trợ hê miễn dịch, cải thiệu tiêu hoá và duy trì bộ lông óng mượt Lợi ích Tăng cường hệ miễn dịch nhờ 7 loại siêu thực phẩm chống oxy hoá 90% protein từ thịt động vật, giúp phát triển cơ bắp và duy trì năng lượng Tốt cho tiêu hoá với chất xơ tự nhiên từ khoai lang và rễ rau diếp xoăn Ngăn ngừa mảng bám, bảo vệ răng miệng nhờ tảo nâu bổ sung độc quyền Dưỡng da, lông với nguồn đạm và chất béo tốt Không chất bảo quản, không hương vị nhân tạo, khong glut…

Thương hiệu: Cature.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-cature-easy-farm-topping-1-5kg', 8, 1700, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Hạt Cho Mèo Cature Easy Farm Topping 1.5kg');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Khay Vệ Sinh Cho Mèo Mon Ami Cao Cấp', 'products/paddy_055_khay-ve-sinh-cho-meo-mon-ami-cao-cap.jpg', 200000, 0,
       'Khay Vệ Sinh Cho Mèo Mon Ami Cao Cấp Thương hiệu: Mon Ami Phù hợp cho: Mèo mọi lứa tuổi Khay Vệ Sinh là sản phẩm dùng để đựng cát vệ sinh cho mèo, giúp mèo đi vệ sinh đúng chỗ. Sản phẩm được làm từ chất liệu nhựa PP không độc hại, an toàn cho người và vật nuôi. Khay Vệ Sinh có hình dáng oval, thành cao rất hiện đại, có nhiều màu sắc đáng yêu. Lợi ích: Giúp thú cưng đi vệ sinh được dễ dàng hơn. Phần nắp cong vào bên trong giúp cho cát vệ sinh không bị bắn tung tóe khi thú cưng đi vệ sinh. Bạn có thể đổ cát vệ sinh hoặc tấm lót hút khử mùi bên dưới khay để loại bỏ mùi hôi của phân và nước tiểu. Sản phẩm Khay vệ sinh cho mèo rất tiện lợi cho những gia đình ở thành phố không có nhiều không gian. Thiết kế thông minh, dễ tháo rời, vệ sinh và chùi rửa. Thành phần Chất liệu: Nhựa PP Hướng dẫn sử dụng Cho cát vệ sinh vào khay để trở thành nơi vệ sinh cho mèo. Để nệm vào khay để trở thành giường…

Thương hiệu: Mon Ami.

Nguồn tham khảo: https://paddy.vn/products/khay-ve-sinh-cho-meo-mon-ami-cao-cap', 49, 100, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Khay Vệ Sinh Cho Mèo Mon Ami Cao Cấp');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Súp Thưởng Cho Chó Mèo VFcore Chức Năng', 'products/paddy_056_sup-thuong-cho-cho-meo-vfcore-chuc-nang.jpg', 17000, 0,
       'Súp Thưởng Cho Chó Mèo VFcore Chức Năng Thương hiệu: VFcore Phù hợp cho: Chó/Mèo mọi lứa tuổi Súp thưởng cho chó mèo VF+Core là dòng thực phẩm chức năng dạng súp thưởng cao cấp đến từ thương hiệu VetSynova (Thái Lan). Sản phẩm được nghiên cứu chuyên sâu nhằm bổ sung dinh dưỡng, tăng cường miễn dịch và cải thiện sức khỏe toàn diện cho chó mèo. Lợi ích: Feline Vitality: Tăng sức khoẻ tổng thể Amino Acids: Tăng cơ bắp, hỗ trợ thể trạng kém Bio Postbiotics & Prebiotics: Hỗ trợ tiêu hóa, tăng miễn dịch Fiber: Hỗ trợ đường ruột, giảm búi lông, giảm táo bón Joint Care Complex: Hỗ trợ khớp, vận động KC - Complex: Hỗ trợ thận, giảm sự suy giảm chức năng thận LS - Lysine: Tăng đề kháng, bảo vệ hệ miễn dịch, giảm stress RB - Iron & Copper Multi-Vitamins: Hỗ trợ tạo máu, tăng hấp thu sắt & đồng, cải thiện thể trạng kém SK: Hỗ trợ da – lông đẹp mượt UC Uninary and Calming: Hỗ trợ phòng sỏi mật Thành…

Thương hiệu: VFcore.

Nguồn tham khảo: https://paddy.vn/products/sup-thuong-cho-cho-meo-vfcore-chuc-nang', 50, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Súp Thưởng Cho Chó Mèo VFcore Chức Năng');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Dung Dịch Vệ Sinh Tai Cho Chó Mèo Jungle Monster Chamomile 120ml', 'products/paddy_057_dung-dich-ve-sinh-tai-cho-cho-meo-jungle-monster-chamomile-120ml.jpg', 400000, 0,
       'Dung Dịch Vệ Sinh Tai Cho Chó Mèo Jungle Monster Chamomile 120ml Thương hiệu: Jungle Monster Phù hợp cho: Chó/Mèo mọi lứa tuổi Dung dịch vệ sinh tai cho chó mèo Jungle Monster Ear Cleaner là sản phẩm làm sạch tai dịu nhẹ cho chó và mèo, giúp loại bỏ ráy tai, mùi hôi và hỗ trợ giảm ngứa, đỏ tai do viêm. Với 60% chiết xuất hoa cúc, Madecassic Acid và các thành phần thiên nhiên an toàn (chuẩn EWG Green), sản phẩm giúp làm sạch, khử mùi và dưỡng ẩm vùng tai hiệu quả. Không chứa hương liệu, phù hợp cho thú cưng có khứu giác nhạy cảm. Lợi ích Làm sạch hiệu quả loại bỏ ráy tai vè bụi bẩn nhẹ nhàng nhờ thành phần enzyme thực vật Khử mùi hôi Cyclodextrin giúp loại bỏ mùi khó chịu do viêm tai Dưỡng ẩm và làm dịu: 60% chiết xuất hoa cúc và madecassic Acid giúp làm dịu, giảm khô nứt và kích ứng Thành phần tự nhiên, an toàn, tất cả nguyên liệu đạt chuẩn EWG Green, không gây kích ứng Không mùi, không…

Thương hiệu: Jungle Monster.

Nguồn tham khảo: https://paddy.vn/products/dung-dich-ve-sinh-tai-cho-cho-meo-jungle-monster-chamomile-120ml', 51, 100, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Dung Dịch Vệ Sinh Tai Cho Chó Mèo Jungle Monster Chamomile 120ml');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Xịt Thơm Miệng Cho Chó Mèo Jungle Monster Dental Spray 30ml', 'products/paddy_058_xit-thom-mieng-cho-cho-meo-jungle-monster-dental-spray-30ml.jpg', 390000, 0,
       'Xịt Thơm Miệng Cho Chó Mèo Jungle Monster Dental Spray 30ml Thương hiệu: Jungle Monster Phù hợp cho: Chó/Mèo từ 3 tháng tuổi trở lên Thú cưng mới bắt đầu chăm sóc răng miệng Thú cưng khó chịu khi chải răng Thú cưng có hơi thở hôi Thú cưng cần ngăn ngừa cao răng & duy trì răng miệng khỏe mạnh Xịt thơm miệng cho chó mèo là dung dịch chăm sóc răng miệng tiện lợi, giúp loại bỏ mùi hôi miệng, ngăn ngừa cao răng và viêm nướu chỉ với vài lần xịt mỗi ngày. Lợi ích Vòi xịt tối ưu phù hợp cấu trúc miệng thú cưng Hương thơm dễ chịu, thân thiện với thú cưng (hương chuối) Enzyme tự nhiên đôi (dual enzymes) giúp ngăn ngừa cao răng Ngăn khô miệng và làm thơm hơi thở Thành phần Thành phần an toàn và dịu nhẹ Hạn sử dụng: 24 tháng kể từ ngày sản xuất Sản xuất tại Hàn Quốc Hướng dẫn sử dụng Xịt trực tiếp lên răng của chó/mèo 1 đến 3 lần/ngày để đạt hiệu quả tốt nhất 👉Xem thêm các sản phẩm khác tại Paddy.…

Thương hiệu: Jungle Monster.

Nguồn tham khảo: https://paddy.vn/products/xit-thom-mieng-cho-cho-meo-jungle-monster-dental-spray-30ml', 52, 100, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Xịt Thơm Miệng Cho Chó Mèo Jungle Monster Dental Spray 30ml');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Mèo Trưởng Thành Me-O Delite Gói 70g', 'products/paddy_059_pate-cho-meo-truong-thanh-me-o-delite-goi-70g.jpg', 18000, 0,
       'Pate Cho Mèo Trưởng Thành Me-O Delite Gói 70g Thương hiệu: Me-O Phù hợp cho: Mèo từ 1 tuổi trở lên Pate cho mèo Me-o là dòng thức ăn ướt dạng túi dành cho mèo trưởng thành (từ 1 tuổi trở lên) với thành phần chứa cá ngừ kết hợp cá ngừ sọc dưa, kèm topping thơm ngon, hấp dẫn. Sản phẩm được sản xuất với công thức đặc biệt nhằm giúp cung cấp dinh dưỡng đầy đ, hỗ trợ duy trì sức khỏe tổng thể và vẻ ngoài xinh đẹp cho các chú mèo Lợi ích Grain Free - công thức hoàn toàn KHÔNG NGŨ CỐC, hạn chế nguy cơ dị ứng ngũ cốc ở mèo. Thịt cá trắng thơm ngon, có thớ thịt cá thật. Thêm topping hấp dẫn, kích thích mèo ăn ngon miệng hơn. Hỗ trợ cung cấp nước cho mèo. Bổ sung Taurine giúp tăng cường hệ miễn dịch và thị giác. Biotin và Kẽm hỗ trợ nuôi dưỡng làn da và bộ lông khỏe mạnh. Vitamin C giúp tăng cường hệ miễn dịch Thành phần Cá ngừ tươi, cá ngừ sọc, chất tạo đông, chất điều vị, taurin, các vitamin và…

Thương hiệu: Me-O.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-truong-thanh-me-o-delite-goi-70g', 53, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pate Cho Mèo Trưởng Thành Me-O Delite Gói 70g');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Dây Dắt Bấm Tự Động Cho Chó Flexi', 'products/paddy_060_day-dat-bam-tu-dong-cho-cho-flexi.jpg', 440000, 0,
       'Dây Dắt Bấm Tự Động Cho Chó Flexi Thương hiệu: Mon Ami Phù hợp cho: Chó mọi lứa tuổi Dây dắt chó là phụ kiện thú cưng giúp thu gọn Flexi Fun lựa chọn tiện lợi và an toàn giúp thú cưng có thể thoải mái di chuyển trong khi bạn vẫn kiểm soát được. Với thiết kế hiện đại, hệ thống phanh thông minh và tay cầm mềm mại, sản phẩm mang lại sự thoải mái cho cả chủ và thú cưng trong mỗi chuyến đi dạo. Lợi ích Cho phép chó tự do di chuyển trong phạm vi an toàn Hệ thống phanh và khoá dễ dàng, giưps kiểm soát tốt hơn khi cần thiết Tay cầm mềm mại, thoải mái khi cầm lâu Thiết kế gọn nhẹ, có thể tự động thu dây, trnash rối và vướng Đảm bảo an toàn khi dắt chó đi dạo, ngăn ngừa việc chạy lung tung Hướng dẫn sử dụng Gắn móc khoá vào vòng cổ của thú cưng Thả dây để thú cưng tự do di chuyển, dây sẽ tự động thu gọn Sử dụng nút phanh hoặc hoặc nút khoá để điều chỉnh chiều dài khi cần 👉Xem thêm các sản phẩm k…

Thương hiệu: Mon Ami.

Nguồn tham khảo: https://paddy.vn/products/day-dat-bam-tu-dong-cho-cho-flexi', 54, 600, 'Phụ Kiện Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Dây Dắt Bấm Tự Động Cho Chó Flexi');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo Smartheart', 'products/paddy_061_hat-cho-meo-smartheart-cat.jpg', 120000, 0,
       'Hạt Cho Mèo Smartheart Thương hiệu: Smartheart Phù hợp cho: Mèo (tùy loại sản phẩm) Thức ăn hạt cho mèo SmartHeart đạt chuẩn AAFCO, với công thức gấp 3 lần DHA giúp phát triển trí não. Đồng thời, bổ sung các dưỡng chất thiết yếu như chất Đạm, Omega 3, các Vitamin và chất chống oxy hóa giúp phát triển cơ bắp, nuôi dưỡng da lông, tốt cho tiêu hóa và bảo vệ tim mạch, cũng như duy trì sức khỏe toàn diện cho mèo trưởng thành. Lợi ích Thúc đẩy phát triển chức năng não bộ, cải thiện trí nhớ, sự thông minh và nhanh nhẹn cho mèo trưởng thành với 3 lần DHA từ dầu cá biển, bột DHA và bột cá kết hợp với Choline giúp hấp thu DHA tốt hơn. Hỗ trợ sức khỏe tim mạch với Omega 3 từ dầu cá biển. Prebiotics từ men bia sấy khô hỗ trợ sức khỏe hệ tiêu hóa và tăng cường miễn dịch tự nhiên. Chiết xuất Yucca giúp giảm mùi hôi chất thải. Công thức hạt màu tự nhiên, không sử dụng màu nhân tạo. Thành phần Gà, cá n…

Thương hiệu: Smartheart.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-smartheart-cat', 55, 1500, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Hạt Cho Mèo Smartheart');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Đồ Chơi Cho Mèo Fofos Lông Vũ', 'products/paddy_062_do-choi-cho-meo-fofos-long-vu.webp', 140000, 0,
       'Đồ Chơi Cho Mèo Fofos Lông Vũ Thương hiệu: Fofos Phù hợp cho: mèo mọi lứa tuổi Đồ chơi mèo Fofos lông vũ được thiết kế độc đáo với màu sắc nổi bật và chùm lông vũ mềm mại, thu hút bản năng săn mồi tự nhiên của mèo. Sản phẩm có thể tự động di chuyển, kết hợp lông vũ rung lắc kích thích sự tò mò, giúp mèo vận động nhiều hơn, giảm căng thẳng và ngăn ngừa béo phì. Đây không chỉ là món đồ chơi thú vị mà còn là công cụ giúp mèo rèn luyện sức khỏe và tinh thần mỗi ngày. Lợi ích Kích thích bản năng săn mồi Tăng cường vận động Giải tỏa căng thẳng, buồn chán Rèn luyện phản xạ và sự linh hoạt Tạo sự gắn kết Hướng dẫn sử dụng Lựa chọn đồ chơi có kích thước phù hợp với thú cưng Kiểm tra sản phẩm trước khi cho thú cưng chơi Tránh tiếp xúc với lửa Không sử dụng với mục đích khác ngoài đồ chơi cho mèo 👉Xem thêm các sản phẩm khác tại Paddy.vn #dochoichomeo #dochomeo #phukienthucung #meoanhlongngan #cha…

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/do-choi-cho-meo-fofos-long-vu', 56, 300, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Đồ Chơi Cho Mèo Fofos Lông Vũ');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Cào Móng Mèo FOFOS Hình Ly Cafe 42x35cm', 'products/paddy_063_cao-mong-meo-fofos-hinh-ly-cafe-42x35cm.webp', 460000, 0,
       'Cào Móng Mèo FOFOS Hình Ly Cafe 42x35cm Thương hiệu: FOFOS Phù hợp cho: Mèo mọi lứa tuổi FOFOS là thương hiệu đồ chơi cho mèo nổi tiếng. Các sản phẩm của FOFOS được thiết kế dựa trên nhu cầu và sở thích của chó mèo, với mục đích giúp chúng vui chơi, giải trí và phát triển toàn diện. Các sản phẩm đồ chơi FOFOS có nhiều mẫu mã đa dạng, từ đồ chơi nhai, đồ chơi đuổi bắt, đồ chơi thông minh,... được làm từ chất liệu an toàn, thân thiện với môi trường. Lợi ích: Giúp thú cưng vui chơi, giải trí, giảm căng thẳng, mệt mỏi. Kích thích mọc răng, nướu, phát triển xương khớp. Giúp tăng cường vận động, phát triển thể chất. Giảm nguy cơ thú cưng nghịch phá đồ đạc trong nhà. Thành phần Chất liệu: gỗ phủ dây thừng chắc chắn Hướng dẫn sử dụng Lựa chọn đồ chơi có kích thước phù hợp với thú cưng Kiểm tra sản phẩm trước khi cho thú cưng chơi Tránh tiếp xúc với lửa Không sử dụng với mục đích khác ngoài đồ c…

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/cao-mong-meo-fofos-hinh-ly-cafe-42x35cm', 57, 3200, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Cào Móng Mèo FOFOS Hình Ly Cafe 42x35cm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Cào Móng Mèo FOFOS Hình Trái Dâu 35x30cm', 'products/paddy_064_cao-mong-meo-fofos-hinh-trai-dau-35x30cm.jpg', 420000, 0,
       'Cào Móng Mèo FOFOS Hình Trái Dâu 35x30cm Thương hiệu: FOFOS Phù hợp cho: Mèo mọi lứa tuổi FOFOS là thương hiệu đồ chơi cho mèo nổi tiếng. Các sản phẩm của FOFOS được thiết kế dựa trên nhu cầu và sở thích của chó mèo, với mục đích giúp chúng vui chơi, giải trí và phát triển toàn diện. Các sản phẩm đồ chơi FOFOS có nhiều mẫu mã đa dạng, từ đồ chơi nhai, đồ chơi đuổi bắt, đồ chơi thông minh,... được làm từ chất liệu an toàn, thân thiện với môi trường. Lợi ích: Giúp thú cưng vui chơi, giải trí, giảm căng thẳng, mệt mỏi. Kích thích mọc răng, nướu, phát triển xương khớp. Tăng cường vận động, phát triển thể chất. Giảm nguy cơ thú cưng nghịch phá đồ đạc trong nhà. Thành phần Chất liệu: gỗ phủ dây thừng chắc chắn Kích thước: 35x30cm Hướng dẫn sử dụng Lựa chọn đồ chơi có kích thước phù hợp với thú cưng Kiểm tra sản phẩm trước khi cho thú cưng chơi Tránh tiếp xúc với lửa Không sử dụng với mục đích…

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/cao-mong-meo-fofos-hinh-trai-dau-35x30cm', 58, 3200, 'Đồ Chơi Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Cào Móng Mèo FOFOS Hình Trái Dâu 35x30cm');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Chó Wanpy Không Ngũ Cốc Mix Thịt Viên', 'products/paddy_065_hat-cho-cho-wanpy-khong-ngu-coc-mix-thit-vien.jpg', 80000, 0,
       'Hạt Cho Chó Wanpy Không Ngũ Cốc Mix Thịt Viên Thương hiệu: Wanpy Premium Phù hợp cho: Chó tuỳ theo độ tuổi Thức ăn hạt cho chó wanpy là dòng hạt cao cấp cho chó, được thiết kế với công thức không chứa ngũ cốc nhằm giảm gánh nặng tiêu hóa, giúp bé cưng ăn ngon miệng và hấp thu dinh dưỡng tối ưu. Sản phẩm kết hợp độc đáo giữa hạt truyền thống và thịt viên nướng lò đối lưu (oven-baked bites), mang đến trải nghiệm bữa ăn mới lạ, thơm ngon và bổ dưỡng. Lợi ích Không bắp, lúa mì, đậu nành, đậu lăng giúp nhẹ bụng cho cún Nguồn protein chất lượng cao đến từ thịt gà, vịt, bò Bổ sung hoạt chất hỗ trợ cơ xương Mùi vị hấp dẫn nhờ kết hợp với 60g thịt viên nướng lò Hướng dẫn sử dụng Cho ăn trực tiếp, chia khẩu phần theo độ tuổi, cân nặng và mức độ vận động hàng ngày Có thể kết hợp với thức ăn ướt hoặc pate để tăng hương vị Luôn chuẩn bị nước sạch cho chó uống kèm Đây là thức ăn hằng này, có thể dùng…

Thương hiệu: Wanpy Premium.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-wanpy-khong-ngu-coc-mix-thit-vien', 8, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Hạt Cho Chó Wanpy Không Ngũ Cốc Mix Thịt Viên');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Súp Thưởng Cho Mèo Silver Spoon 100% Từ Thịt Cá Thật 56g', 'products/paddy_066_sup-thuong-cho-meo-silver-spoon-thailand-56g-4-tuyp.jpg', 27000, 0,
       'Súp Thưởng Cho Mèo Silver Spoon 100% Từ Thịt Cá Thật 56g (4 tuýp) Thương hiệu: Silver Spoon Phù hợp cho: Mèo mọi lứa tuổi Súp thưởng cho mèo Silver Spoon dạng thanh được làm từ 100% thịt cá tươi thật, mang đến hương vị thơm ngon, bổ dưỡng và an toàn cho sức khỏe mèo. Sản phẩm được sản xuất tại Thái Lan theo nguyên liệu, quy trình, tiêu chuẩn Nhật Bản. Với 6 hương vị đa dạng, dễ dàng chiều lòng mọi khẩu vị, đóng gói tiện lợi 4 thanh/túi, là món thưởng hấp dẫn và tốt cho mèo cưng mỗi ngày. Lợi ích Ngon miệng, dễ ăn: kết cấu súp mềm mịn, mèo nào cũng thích Đa dạng lựa chọn với 6 hương vị khách nhau, phù hợp với nhiều khẩu vị Nguyên liệu an toàn được làm từ thịt cá thật, không hương liệu nhân tạo, không thêm muối Bổ sung dinh dưỡng cung cấp đạm và dưỡng chất từ cá tươi cho sức khoẻ của mèo Sản phẩm được đóng gói dạng thanh 14g dễ dàng cho mèo ăn trực tiếp hoặc trộn và thức ăn hàng ngày…

Thương hiệu: Silver Spoon.

Nguồn tham khảo: https://paddy.vn/products/sup-thuong-cho-meo-silver-spoon-thailand-56g-4-tuyp', 60, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Súp Thưởng Cho Mèo Silver Spoon 100% Từ Thịt Cá Thật 56g');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Cho Chó Que Dai Da Bò Doggyman White Dental 120g', 'products/paddy_067_banh-thuong-cho-cho-que-dai-da-bo-doggyman-white-dental-120g.jpg', 50000, 0,
       'Bánh Thưởng Cho Chó Que Dai Da Bò Doggyman White Dental 120g Thương hiệu: DoggyMan Phù hợp cho: Mọi lứa tuổi Bánh thưởng cho chó Que dai da bò được làm từ da bò tự nhiên, có kết cấu chắc chắn và đàn hồi, giúp thỏa mãn bản năng nhai gặm của chó và kéo dài thời gian thưởng thức. Sản phẩm mang hương thơm tự nhiên kết hợp phô mai, hương sữa hấp dẫn, dễ tiêu hóa nhờ da bò được nghiền nhỏ trước khi tạo hình, vừa ngon miệng vừa an toàn cho thú cưng. Lợi ích Kết hợp phô mai hoặc sữa híup kích thích vị giác Bổ sung Chlorophyll giúp khử mùi hiệu quả Được làm từ da bò tự nhiên, kết cấu chắc chắn Đáp ứng bản năng nhai gặm tự nhiên Làm sạch răng miệng hiệu quả Thành phần Da bò, bột bắp, đạm đậu Hà Lan, đường fructose, maltose, gelatine, sorbitol, glycerin, bột sữa, chất điều vị (bột gan gà), potassium sorbate, sodium propionate, chlorophyll, hydroxy apatite calcium. Thông số Kích thước (mm): W140 x…

Thương hiệu: DoggyMan.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-cho-que-dai-da-bo-doggyman-white-dental-120g', 61, 300, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Bánh Thưởng Cho Chó Que Dai Da Bò Doggyman White Dental 120g');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Chó Gran Deli Dạng Thạch 80g', 'products/paddy_068_pate-cho-cho-gran-deli-dang-thach-80g.jpg', 13000, 0,
       'Pate Cho Chó Gran Deli Dạng Thạch 80g Thương hiệu: Silver Spoon Phù hợp cho: Chó trưởng thành Pate cho chó Gran-Deli mang đến bữa ăn thơm ngon từ 100% thịt gà Nhật Bản kết hợp cùng rau củ giàu chất xơ và vitamin, giúp hỗ trợ hệ tiêu hóa khỏe mạnh. Với kết cấu thạch mềm mịn, sản phẩm dễ ăn, dễ kết hợp và là lựa chọn dinh dưỡng trọn vẹn cho cún yêu mỗi ngày. Lợi ích Thành phần có chứa ức gà được tuyển chọn từ nguồn thịt gà cao cấp tạo thành hương vị hấp dẫn cho cún cưng Chứa ít chất béo giúp duy trì cân nặng phù hợp với chó trưởng thành Dạng thạch dễ trộn với các loại thức ăn khô đã qua xơ chế khác mà cún cưng yêu thích Thành phần Các loại thịt (ức gà, phi lê gà), các loại rau (cà rốt, đậu xanh, ngô ngọt), nước tương từ đậu nành, chất làm đặc,... 👉Xem thêm các sản phẩm khác tại Paddy.vn #patecho #thucanuot #thucanuotchocho #pateuotchocho #patechocho #silverspoon

Thương hiệu: Gran Deli.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-cho-gran-deli-dang-thach-80g', 8, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pate Cho Chó Gran Deli Dạng Thạch 80g');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Lược Chải Lông Cho Chó Mèo Bella', 'products/paddy_069_luoc-chai-long-cho-cho-meo-bella.jpg', 75000, 0,
       'Lược Chải Lông Cho Chó Mèo Bella Thương hiệu: Mon Ami Phù hợp cho: Chó/Mèo mọi lứa tuổi Chải lông thường xuyên giúp thú cưng sạch sẽ, khỏe mạnh và tránh rối lông gây viêm da. Lược chải lông – phụ kiện chăm sóc thú cưng cần có – giúp loại bỏ lông rụng, bụi bẩn, đồng thời làm lông mềm mượt và thoải mái hơn cho bé yêu. Lợi ích: Loại bỏ lông rụng, lông rối, lông chết, và các ký sinh trùng trên da thú cưng. Điều tiết lượng dầu trên da, cung cấp độ ẩm và làm bóng mượt lông. Kích thích mọc lông mới và hạn chế lông rụng vào mùa đông. Giúp giảm stress, giải tỏa căng thẳng, và tăng cường sự gắn kết giữa chủ và thú cưng. Thành phần Chất liệu: Lược chải lông được thiết kế với các răng cưa bằng thép không gỉ, cán cầm bằng nhựa không trơn trượt khi sử dụng, tạo cảm giác thoải mái cho bạn và thú cưng.. Hướng dẫn sử dụng Chọn loại lược phù hợp với kích thước, độ dài và độ dày của lông thú cưng. Chải lô…

Thương hiệu: Mon Ami.

Nguồn tham khảo: https://paddy.vn/products/luoc-chai-long-cho-cho-meo-bella', 63, 400, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Lược Chải Lông Cho Chó Mèo Bella');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Nệm Gối Cho Chó Mèo Hình Chim Cánh Cụt', 'products/paddy_070_nem-goi-cho-cho-meo-hinh-chim-canh-cut.jpg', 140000, 0,
       'Nệm Gối Cho Chó Mèo Hình Chim Cánh Cụt Thương hiệu: Paddy Phù hợp cho: Chó/Mèo mọi độ tuổi Nệm Gối cho chó mèo hình chim cánh cụt là sản phẩm độc đáo có tích hợp gối. Với thiết kế hình dáng dễ thương, màu sắc tươi sáng và chất liệu an toàn, nệm gối này sẽ mang đến sự thoải mái và dễ chịu cho giấc ngủ cho thú cưng của bạn. Lợi ích: Tạo hình đáng yêu với tông màu trắng cho cảm giác cao cấp sang trọng Đệm thoáng khi ở bên trong, không ngột ngạt Kết hợp decor không gian phòng Thành phần Chất liệu: Polyester, Cotton. Hướng dẫn sử dụng Đặt sản phẩm ở nơi phù hợp cho thú cưng Dùng máy hút bụi để làm sạch hoặc lau nhẹ các vết bẩn bằng khăn ấm 👉Xem thêm các sản phẩm khác tại Paddy.vn #dodungchomeo #dodungthucung #phukien #phukienthucung #phukienchomeo #nemgoichomeo #nemgoithucung

Thương hiệu: Paddy Pet Shop.

Nguồn tham khảo: https://paddy.vn/products/nem-goi-cho-cho-meo-hinh-chim-canh-cut', 64, 600, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Nệm Gối Cho Chó Mèo Hình Chim Cánh Cụt');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Sữa Tắm Cho Chó Bossen Fortis Derm Chống Ngứa 250ml', 'products/paddy_071_sua-tam-cho-cho-bossen-fortis-derm-chong-ngua-250ml.jpg', 120000, 0,
       'Sữa Tắm Cho Chó Bossen Fortis Derm Chống Ngứa 250ml Thương hiệu: Bossen Phù hợp cho: Chó từ 3 tháng tuổi trở lên Sữa tắm cho chó Fortis Derm Anti-Itch là giải pháp chăm sóc da và lông dành cho thú cưng, đặc biệt là những bé hay gặp tình trạng ngứa ngáy, kích ứng. Sản phẩm giúp làm sạch bụi bẩn nhẹ nhàng, khử mùi hôi khó chịu, đồng thời bổ sung dưỡng chất để da khỏe mạnh và lông mềm mượt hơn sau mỗi lần tắm. Lợi ích Khử mùi - Giảm ngứa - Làm mát da Hỗ trợ điều trị viêm da dị ứng, gàu, rụng lông bất thường Giúp da boss khoẻ mạnh, lông mềm mượt, thơm tho dễ chịu Thành phần Cocoyl Glutamate, Sodium Cocoyl Glutamate, Sodium Lauroyl Sarcosinate, ZnO, Cocamidopropyl Betaine, Propanediol, Glycerin, Tocopheryl Acetate, Dexpanthenol, Chiết xuất hạt Avena Sativa, Chiết xuất hoa/lá/thân cây Agastache Mexicana, Nước. Hướng dẫn sử dụng Làm ướt toàn bộ cơ thể. Thoa đều dầu gội lên toàn thân (trực tiếp…

Thương hiệu: Bossen.

Nguồn tham khảo: https://paddy.vn/products/sua-tam-cho-cho-bossen-fortis-derm-chong-ngua-250ml', 65, 300, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Sữa Tắm Cho Chó Bossen Fortis Derm Chống Ngứa 250ml');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Cát Đậu Nành Cho Mèo ON25 Mixed Tofu', 'products/paddy_072_cat-dau-nanh-cho-meo-on25-mixed-tofu.jpg', 99000, 0,
       'Cát Đậu Nành Cho Mèo ON25 Mixed Tofu Thương hiệu: Cat''s On Phù hợp cho: Mèo mọi lứa tuổi Cát Vệ Sinh Cho Mèo ON25 Tofu được làm từ đậu nành tự nhiên, an toàn và lành tính, với công thức 5 trong 1: khử mùi hiệu quả, vón cục nhanh, không bụi, an toàn sinh học và dễ dàng dọn dẹp. Sản phẩm giúp giữ cho ngôi nhà luôn sạch sẽ, thơm tho, mang lại sự thoải mái cho mèo cưng và sự an tâm cho cả gia đình. Lợi ích Hút ẩm & vón cục nhanh: 70% đậu nành tự nhiên kết hợp khoáng sét giúp thấm hút vượt trội, vón cục chắc, dễ dọn dẹp Khử mùi kép: Baking soda + khoáng sét khử mùi hiệu quả, giữ không gian luôn thơm tho Không bụi - an toàn hô hấp: Công thức gần như không bụi, bảo vệ sức khoẻ cho mèo và chủ nuôi Khô thoáng tối đa: SAP siêu thấm ngăn bết đáy, giữ khay vệ sinh sạch sẽ Thân thiện môi trường: Thành phần tự nhiên, dễ phân huỷ sinh học, có thể xả bồn cầu tiện lợi Mùi hương dễ chịu: Hương tự nhiên,…

Thương hiệu: Cat''s On.

Nguồn tham khảo: https://paddy.vn/products/cat-dau-nanh-cho-meo-on25-mixed-tofu', 66, 2700, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Cát Đậu Nành Cho Mèo ON25 Mixed Tofu');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Chó Mr Vet  D1 Chăm Sóc Hệ Tiêu Hoá', 'products/paddy_073_hat-cho-cho-mr-vet-d1-cham-soc-he-tieu-hoa.jpg', 130000, 0,
       'Hạt Cho Chó Mr Vet D1 Chăm Sóc Hệ Tiêu Hoá Thương hiệu: Mr Vet Phù hợp cho: Chó mọi lứa tuổi Thức ăn hạt cho chó Mr.vet D1 là dòng hạt Holistic cao cấp được làm từ thịt cừu non kết hợp rau củ quả và vitamin thiết yếu, mang đến nguồn dinh dưỡng cân bằng và dễ hấp thu. Công thức an toàn, ít hoặc không có chất phụ gia, giúp tăng cường sức khỏe, hỗ trợ tiêu hóa, cải thiện miễn dịch và nuôi dưỡng da lông bóng mượt. Lợi ích Bổ dưỡng: Giàu protein, axit amin, vitamin và khoáng chất, giúp phát triển cơ bắp và duy trì sức khỏe toàn diện. Tăng cường miễn dịch: Thịt cừu và rau củ quả giàu vitamin giúp nâng cao sức đề kháng, giảm nguy cơ bệnh tật. Hỗ trợ tiêu hóa: Thịt cừu dễ hấp thu, chất xơ từ rau củ giúp tiêu hóa tốt, ngăn ngừa táo bón. Giữ ấm cơ thể: Thịt cừu có tính ấm, giúp duy trì nhiệt độ, chống cảm lạnh. Da khỏe – Lông bóng mượt: Các dưỡng chất và vitamin từ rau củ hỗ trợ chăm sóc da, cải…

Thương hiệu: Mr Vet.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-mr-vet-d1-cham-soc-he-tieu-hoa', 67, 1500, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Hạt Cho Chó Mr Vet  D1 Chăm Sóc Hệ Tiêu Hoá');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Chó Giống Nhỏ Dị Ứng Royal Canin Hypoallergenic Small', 'products/paddy_074_hat-cho-cho-giong-nho-di-ung-royal-canin-hypoallergenic-small.jpg', 399000, 0,
       'Hạt Cho Chó Giống Nhỏ Dị Ứng Royal Canin Hypoallergenic Small Thương hiệu: Royal Canin Phù hợp cho: Chó trưởng thành giống nhỏ (&lt;10kg) ROYAL CANIN Hypoallergenic Small Dog là thức ăn hạt cho chó trưởng thành giống nhỏ, được thiết kế đặc biệt để hỗ trợ chó bị dị ứng hoặc không dung nạp dinh dưỡng. Công thức dùng nguồn protein và carbohydrate chọn lọc, giúp hạn chế nguy cơ dị ứng và đảm bảo dinh dưỡng an toàn cho thú cưng. Lợi ích: Protein thủy phân phân tử thấp: giảm nguy cơ dị ứng, dễ hấp thu. Chỉ số RSS thấp: hỗ trợ giảm hình thành tinh thể Bổ sung dưỡng chất răng miệng: giúp duy trì hơi thở thơm mát, hạn chế mảng bám. Hỗ trợ hàng rào da tự nhiên: bảo vệ và nuôi dưỡng làn da khỏe mạnh. Công thức chuyên biệt với kích thước hạt phù hợp, dễ nhai, đáp ứng nhu cầu năng lượng của chó giống nhỏ. Thành phần Gạo, protein đậu nành thủy phân cô lập, mỡ động vật, khoáng chất, gan gia cầm thủy p…

Thương hiệu: Royal Canin.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-cho-giong-nho-di-ung-royal-canin-hypoallergenic-small', 8, 1200, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Hạt Cho Chó Giống Nhỏ Dị Ứng Royal Canin Hypoallergenic Small');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Mèo Lapaw Dạng Thạch 70g', 'products/paddy_075_pate-cho-meo-lapaw-dang-thach-70g.jpg', 11000, 0,
       'Pate Cho Mèo Lapaw Dạng Thạch 70g Thương hiệu: LaPaw Phù hợp cho: Mèo mọi lứa tuổi Pate cho mèo LaPaw 70g là thức ăn ướt dạng thạch giàu protein tự nhiên dễ hấp thu, giúp mèo khỏe mạnh, tăng đề kháng và hỗ trợ hệ tiêu hóa. Với kết cấu mềm mịn, hương vị đa dạng sản phẩm phù hợp cho mèo ở mọi lứa tuổi, kể cả mèo kén ăn có thể cho ăn trực tiếp hoặc trộn cùng hạt khô để tăng hương vị. Lợi ích Giàu protein tự nhiên dễ hấp thu Không muối - Không chất bảo quản - Không phụ gia Hỗ trợ tiêu hoá khoẻ mạnh, tăng đề kháng, lông mượt Dạng thạch mềm mịn, dễ ăn, kích thích mèo thèm ăn Thành phần Thịt gà, gan gà, tim gà, dầu gà, nước tinh khiết, taurine, chiết xuất rong biển, vitaminA(D3,E.B1,B2,B6,B12],niacin,canxi pantothenate, D-biotin,axit folic,choline,sắt hữu cơ,đồng hữu cơ ,kẽm hữu cơ, mangan hữu cơ, iốt, selen hữu cơ. Hướng dẫn sử dụng Có thể ăn trực tiếp hoặc trộn chung với hạt theo bữa 👉 Xem…

Thương hiệu: LaPaw.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-lapaw-dang-thach-70g', 69, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pate Cho Mèo Lapaw Dạng Thạch 70g');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo Mọi Lứa Tuổi On25 Cat Đạm Cao 32%', 'products/paddy_076_hat-cho-meo-on25-cat-dam-cao-32-danh-cho-meo-moi-do-tuoi.jpg', 250000, 0,
       'Hạt Cho Mèo Mọi Lứa Tuổi On25 Cat Đạm Cao 32% Thương hiệu: Cat''s On Phù hợp cho: Mèo mọi lứa tuổi Thức ăn hạt cho mèo On25 Cat được nghiên cứu bởi các chuyên gia dinh dưỡng thú cưng nhằm mang đến giải pháp chăm sóc tối ưu cho mèo. Nhờ thành phần chọn lọc từ thịt gà, bột cá, dầu cá và vitamin. Sản phẩm giúp tăng cường hệ miễn dịch, hỗ trợ hệ tiêu hóa và cải thiện sức khỏe da lông một cách hiệu quả. Lợi ích Duy trì thể trạng khỏe mạnh: Nhờ hàm lượng đạm cao và chất béo vừa đủ, sản phẩm đáp ứng tốt nhu cầu năng lượng của cả mèo con và mèo trưởng thành. Tốt cho tiêu hóa: Prebiotic kép và chất xơ tự nhiên giúp cải thiện đường ruột, làm giảm mùi phân và hạn chế rối loạn tiêu hóa. Lông mượt – da khỏe: Sự kết hợp giữa Omega-3 và Astaxanthin hỗ trợ chăm sóc da lông từ sâu bên trong, phù hợp cho cả mèo có làn da nhạy cảm. Hạt nhỏ dễ nhai: Thiết kế kích thước hạt nhỏ vừa miệng, phù hợp với cấu trú…

Thương hiệu: Cat''s On.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-on25-cat-dam-cao-32-danh-cho-meo-moi-do-tuoi', 70, 3200, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Hạt Cho Mèo Mọi Lứa Tuổi On25 Cat Đạm Cao 32%');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Cho Chó Lamer Multi Chew Hỗ Trợ Xương Khớp & Răng Miệng', 'products/paddy_077_snack-cho-cho-lamer-multi-chew-ho-tro-xuong-khop-rang-mieng.jpg', 60000, 0,
       'Bánh Thưởng Cho Chó Lamer Multi Chew Hỗ Trợ Xương Khớp & Răng Miệng Thương hiệu: Lamer Phù hợp cho: Chó từ 3 tháng tuổi trở lên Snack cho chó nhỗ trợ chăm sóc răng miệng và sức khỏe xương khớp. Với công thức chứa các dưỡng chất hỗ trợ sụn khớp, giúp giảm viêm, hỗ trợ vận động linh hoạt và hạn chế tình trạng thoái hóa sớm. Đồng thời, sản phẩm còn giúp làm sạch răng, giảm mùi hôi miệng và hỗ trợ vệ sinh răng miệng hằng ngày. Phù hợp cho cả những bé có cơ địa nhạy cảm. Lợi ích Quản lý dị ứng thực phẩm + răng miệng + khớp Có thể giúp giảm phản ứng dị ứng thực phẩm như chảy nước mắt hoặc ngứa da bằng cách sử dụng protein thịt gà thủy phân Quản lý cao răng và chăm sóc hơi thở răng miệng Chứa các thành phần như trai xanh, chondroitin, glucosamine, boswellia, và MSM (lưu huỳnh thực phẩm) có thể giúp cải thiện chức năng khớp và giảm đau Giảm kích thích cho răng và nướu Dễ dàng bảo quản và cho ăn…

Thương hiệu: Lamer.

Nguồn tham khảo: https://paddy.vn/products/snack-cho-cho-lamer-multi-chew-ho-tro-xuong-khop-rang-mieng', 71, 150, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Bánh Thưởng Cho Chó Lamer Multi Chew Hỗ Trợ Xương Khớp & Răng Miệng');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Dầu Xả Dưỡng Lông Cho Chó Mèo Tropiclean Spa Nourish', 'products/paddy_078_dau-xa-duong-long-cho-cho-meo-tropiclean-spa-nourish.jpg', 250000, 0,
       'Dầu Xả Dưỡng Lông Cho Chó Mèo Tropiclean Spa Nourish Thương hiệu: Tropiclean Phù hợp cho: Chó/Mèo mọi lứa tuổi Dầu xả Tropiclean là lựa chọn hoàn hảo để chăm sóc vệ sinh chó mèo tại nhà. Với công thức dưỡng ẩm tự nhiên, sản phẩm giúp phục hồi lông mềm mượt, giảm rối, chống khô da và mang lại hương thơm dịu nhẹ. Thích hợp cho mọi giống chó mèo, đặc biệt lông dài và nhạy cảm. Lợi ích Làm mềm lông khô, rối Giúp lông dễ chải, không bị vón Dưỡng ẩm da, giảm không và bong tróc Hương hoa sứ dịu nhẹ, dễ chịu Giảm stress khi tắm cho thú cưng Phù hợp với chó mèo mọi giống lông An toàn tuyệt đối, không gây kích ứng Thành phần Chiết xuất 100% từ thiên nhiên Chưa vitamin E và dưỡng chất làm mềm lông Không chứa xà phòng, paraben, chất tẩy mạnh Dùng được cho thú cưng nhạy cảm Hướng dẫn sử dụng Tắm sạch thú cưng với dầu gội Dùng khăn lau sơ lông và để lông còn ẩm Lấy lượng dầu xả vừa đủ, thoa đều lên t…

Thương hiệu: Tropiclean.

Nguồn tham khảo: https://paddy.vn/products/dau-xa-duong-long-cho-cho-meo-tropiclean-spa-nourish', 72, 500, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Dầu Xả Dưỡng Lông Cho Chó Mèo Tropiclean Spa Nourish');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Kem Đánh Răng Cho Chó Mèo Vị Gà Jungle Monster', 'products/paddy_079_kem-danh-rang-cho-cho-meo-vi-ga-jungle-monster.webp', 380000, 0,
       'Kem Đánh Răng Cho Chó Mèo Vị Gà Jungle Monster Thương hiệu: Jungle Monster Phù hợp cho: Chó/Mèo từ 6 tháng tuổi trở lên Sản phẩm chăm sóc răng miệng cho chó mèo , giúp khử mùi hôi miệng, kiểm soát mảng bám và cao răng với hương vị gà thơm ngon dễ chịu. Thành phần an toàn, dễ sử dụng. Lợi ích Chăm sóc răng miệng toàn diện cho chó mèo mọi kích cỡ Lông bàn chải siêu mềm, nhẹ nhàng với nướu – phù hợp cho thú cưng nhỏ Tay cầm chống trượt, dễ thao tác khi đánh răng Kem đánh răng vị gà thơm ngon, giúp loại bỏ mùi hôi, kiểm soát mảng bám & cao răng Thành phần an toàn, có thể nuốt được – yên tâm khi sử dụng hàng ngày Dễ dùng: có thể bôi trực tiếp hoặc đánh răng bằng bàn chải Thành phần Nước tinh khiết, Glycerin, D-Sorbitol, Propanediol, Polyglyceryl-10 Laurate, Kẽm Gluconate, Xanthan Gum, Allantoin, Kali Sorbate, Natri Gluconate, Hương liệu, Axit Citric, Natri Citrat, Chiết xuất Nothodactylus, B…

Thương hiệu: Jungle Monster.

Nguồn tham khảo: https://paddy.vn/products/kem-danh-rang-cho-cho-meo-vi-ga-jungle-monster', 73, 150, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Kem Đánh Răng Cho Chó Mèo Vị Gà Jungle Monster');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Xịt Thơm Mềm Lông Cho Chó Jungle Monster Pure Soap Silky Mist 150ml', 'products/paddy_080_xit-thom-mem-long-cho-cho-jungle-monster-pure-soap-silky-mist-150ml.webp', 380000, 0,
       'Xịt Thơm Mềm Lông Cho Chó Jungle Monster Pure Soap Silky Mist 150ml Thương hiệu: Jungle Monster Phù hợp cho: Chó từ 3 tháng tuổi trở lên Xịt Thơm Mềm Lông Cho Chó là sản phẩm giúp xịt khử mùi và dưỡng ẩm sâu, làm mềm mượt và giảm rối lông hiệu quả. Với hương xà phòng trắng dịu nhẹ và thành phần an toàn, sản phẩm mang lại bộ lông bóng khỏe, thơm mát cho thú cưng mỗi ngày. Lợi ích Dưỡng ẩm chuyên sâu: Chứa 10 loại axit hyaluronic giúp cấp ẩm nhanh, giảm khô da và tĩnh điện, cho lông mềm mượt. Tăng độ dày & chắc khỏe: Bổ sung protein thực vật củng cố nang lông, hỗ trợ giảm rụng lông. Bóng khỏe & giữ ẩm: Dưỡng chất từ dầu argan và chiết xuất tự nhiên giúp phục hồi lông hư tổn. Khử mùi hiệu quả: Mùi xà phòng trắng sạch sẽ, dịu nhẹ, không gây khó chịu cho thú cưng. Xịt sương mịn: Dạng phun sương giúp phủ đều, không gây vón cục trên lông. An toàn tuyệt đối: Thành phần đạt chuẩn EWG Green – an…

Thương hiệu: Jungle Monster.

Nguồn tham khảo: https://paddy.vn/products/xit-thom-mem-long-cho-cho-jungle-monster-pure-soap-silky-mist-150ml', 74, 250, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Xịt Thơm Mềm Lông Cho Chó Jungle Monster Pure Soap Silky Mist 150ml');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Sữa Tắm & Xả Cho Chó Mèo Tropiclean 2 Trong 1 Hương Đu Đủ & Dừa 355ml', 'products/paddy_081_sua-tam-xa-cho-cho-meo-tropiclean-2-trong-1-huong-du-du-dua-355ml.jpg', 250000, 0,
       'Sữa Tắm & Xả Cho Chó Mèo Tropiclean 2 Trong 1 Hương Đu Đủ & Dừa 355ml Thương hiệu: Tropiclean Phù hợp cho: Chó/Mèo mọi lứa tuổi Chăm sóc vệ sinh chó mèo TropiClean Luxury 2 trong 1 là dòng sữa tắm xả hữu cơ dành cho chó mèo bán chạy số 1 tại Mỹ, giúp cung cấp độ ẩm cần thiết cho da, đồng thời nuôi dưỡng lông bóng mềm mượt tự nhiên. Với hương thơm đu đủ và dừa dễ chịu, sản phẩm mang đến trải nghiệm thư giãn, giúp giảm căng thẳng cho thú cưng và ngăn ngừa các vấn đề viêm da về lâu dài. Lợi ích Dưỡng ẩm sâu, ngăn ngừa gãy rụng và xơ rối lông Nuôi dưỡng làn da khỏe mạnh, lông bóng mềm tự nhiên Hương thơm thiên nhiên dễ chịu, lưu hương lâu và giảm stress cho thú cưng An toàn, dịu nhẹ cho da tay người dùng Thành phần Dầu dứa, lô hội, yến mạch, awapuhi, việt quất và các chiết xuất hữu cơ khác Dịu nhẹ, an toàn cho da nhạy cảm Dung tích: 473ml Hướng dẫn sử dụng Làm ướt lông thú cưng Lấy lượng sữ…

Thương hiệu: Tropiclean.

Nguồn tham khảo: https://paddy.vn/products/sua-tam-xa-cho-cho-meo-tropiclean-2-trong-1-huong-du-du-dua-355ml', 75, 800, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Sữa Tắm & Xả Cho Chó Mèo Tropiclean 2 Trong 1 Hương Đu Đủ & Dừa 355ml');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Sữa Tắm Cho Chó Tropiclean PerfectFur Chăm Sóc Lông Chuyên Biệt 473ml', 'products/paddy_082_sua-tam-cho-cho-tropiclean-perfectfur-cham-soc-long-chuyen-biet-473ml.jpg', 370000, 0,
       'Sữa Tắm Cho Chó Tropiclean PerfectFur Chăm Sóc Lông Chuyên Biệt 473ml Thương hiệu: Tropiclean Phù hợp cho: Chó (từ 12 tuần tuổi trở lên) Sữa tắm cho chó là dòng sản phẩm cao cấp được thiết kế chuyên biệt cho từng loại lông chó: từ lông ngắn, lông dài, lông xoăn đến lông dày hai lớp. Mỗi sản phẩm đều chứa công thức tối ưu giúp làm sạch sâu, giảm rối và rụng lông, dưỡng ẩm và nuôi dưỡng lông mềm mượt từ gốc đến ngọn. Dù thú cưng của bạn là Poodle, Husky, Golden hay Pug... Perfect Fur đều có giải pháp phù hợp, giúp lông luôn khỏe mạnh, sạch sẽ và vào nếp tự nhiên. Lợi ích: Lông dày 2 lớp (cam) Thấm sâu vào từng lớp lông kép, giúp làm sạch triệt để bụi bẩn, bã nhờn và tạp chất. Tẩy tế bào chết trên da, giúp nang lông thông thoáng, hạn chế tình trạng lông rụng nhiều. Làm mềm lớp lông tơ, giúp giảm gãy rụng và giữ lông dày chắc khỏe hơn. Cấp ẩm tự nhiên, ngăn ngừa khô da, giảm ngứa và kích ứn…

Thương hiệu: Tropiclean.

Nguồn tham khảo: https://paddy.vn/products/sua-tam-cho-cho-tropiclean-perfectfur-cham-soc-long-chuyen-biet-473ml', 76, 700, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Sữa Tắm Cho Chó Tropiclean PerfectFur Chăm Sóc Lông Chuyên Biệt 473ml');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Tã Lót Cho Chó CÁI FOFOS Diapers', 'products/paddy_083_ta-lot-cho-cho-cai-fofos-diapers.jpg', 90000, 0,
       'Tã Lót Cho Chó CÁI FOFOS Diapers Thương hiệu: FOFOS Phù hợp cho: Chó mọi lứa tuổi Không còn những khoảnh khắc khó xử khi chó cưng đi vệ sinh nơi công cộng – Tã lót cho chó FOFOS là người bạn đồng hành lý tưởng cho mọi hoạt động ngoài trời cùng thú cưng. Ứng dụng công nghệ hấp thụ SAP tiên tiến sản phẩm mang đến sự thông thoáng, khô ráo và thoải mái tối đa cho thú cưng, đồng thời giúp "Sen" luôn tự tin khi dắt chó ra ngoài. Lợi ích: Làm từ chất liệu tự nhiên, an toàn cho sức khỏe của thú cưng Thiết kế thông minh vừa vặn Khả năng thấm hút vượt trội - khử mùi hiệu quả Dễ sử dụng, dễ dàng vệ sinh Thành phần Polymer siêu thấm hút, bông thân thiện và an toàn với lông, da thú cưng 👉Xem thêm các sản phẩm khác tại Paddy.vn #tachocho #bimchocho #chamsoccho #thucung #chamsocthucung #talotchocho #Fofos

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/ta-lot-cho-cho-cai-fofos-diapers', 77, 1500, 'Vệ Sinh Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Tã Lót Cho Chó CÁI FOFOS Diapers');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Mèo Triệt Sản Royal Canin Indoor Sterilised 85g', 'products/paddy_084_pate-meo-triet-san-royal-canin-indoor-sterilised-85g.jpg', 35000, 0,
       'Pate Mèo Triệt Sản Royal Canin Indoor Sterilised 85g Thương hiệu: Royal Canin Phù hợp cho: Mèo trưởng thành đã triệt sản (trên 10 tháng tuổi) Pate mèo Royal Canin Indoor Sterilised là công thức được sáng tạo đặc biệt dành cho mèo nhà đã triệt sản. Sản phẩm này chứa protein dễ tiêu hoá giúp phân khoẻ mạnh, với sự cân bằng chính xác của các khoáng chất giúp duy trì sức khoẻ hệ tiết niệu của mèo Lợi ích Hồ sơ dinh dưỡng được ưa chuộng theo bản năng, thể hiện tỷ lệ năng lượng có nguồn gốc từ protein, chất béo và carbohydrate. Được bào chế với protein dễ tiêu hóa giúp phân khỏe mạnh, cùng với sự cân bằng chính xác các khoáng chất (bao gồm canxi) giúp duy trì sức khỏe hệ tiết niệu của mèo trưởng thành. Thành phần Thịt và các sản phẩm từ động vật, ngũ cốc, các sản phẩm có nguồn gốc thực vật, khoáng chất, dầu và mỡ, men. 👉 Xem thêm các sản phẩm khác tại Paddy.vn #thucanuot #thucanchomeo #chams…

Thương hiệu: Royal Canin.

Nguồn tham khảo: https://paddy.vn/products/pate-meo-triet-san-royal-canin-indoor-sterilised-85g', 8, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pate Mèo Triệt Sản Royal Canin Indoor Sterilised 85g');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bát Ăn Cho Chó Mèo Đế Cao Richell', 'products/paddy_085_bat-an-cho-cho-meo-cao-richell.jpg', 85000, 0,
       'Bát Ăn Cho Chó Mèo Đế Cao Richell Thương hiệu: Richell Phù hợp cho: Chó nhỏ (Từ 4kg-10kg)/ Mèo con Bát đựng thức ăn richell là dụng cụ ăn uống mang đến cho chó mèo sự thoải mái tối đa trong giờ ăn. Thiết kế nâng cao tiện dụng mang đến chiều cao lý tưởng cho việc ăn uống và giúp duy trì tư thế khỏe mạnh khi ăn, ăn vặt và uống nước. Viền chống tràn thông minh giúp giảm thiểu tràn đổ và giữ cho sàn nhà luôn sạch sẽ. Lợi ích Giúp thú cưng ăn uống thoải mái hơn, giảm áp lực lên cổ và khớp. Hạn chế nước/đồ ăn bị đổ ra ngoài, giữ sàn nhà luôn sạch sẽ. Đế chống trượt giữ bát cố định khi thú cưng ăn, không bị xê dịch. Dễ cầm nắm khi mang đi, đổ nước hoặc vệ sinh Thành phần: Chất liệu: Nhựa PP, nhựa TPE Kích thước và dung tích: Hình xương 17×16.4×10.5H (cm) (Chó nhỏ) + 260 ml (khoảng 185g thức ăn khô) Hình cá 14.9×14.4×10H (cm) (Mèo con) + 150 ml (khoảng 135g thức ăn ướt) 👉Xem thêm các sản phẩm…

Thương hiệu: Richell.

Nguồn tham khảo: https://paddy.vn/products/bat-an-cho-cho-meo-cao-richell', 8, 400, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Bát Ăn Cho Chó Mèo Đế Cao Richell');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Cho Chó Thịt Cuộn Cá Gooday 80g', 'products/paddy_086_banh-thuong-cho-cho-thit-cuon-ca-gooday-80g.webp', 100000, 0,
       'Bánh Thưởng Cho Chó Thịt Cuộn Cá Gooday 80g Thương hiệu: Gooday Phù hợp cho: Chó trên 12 tháng tuổi Bánh thưởng cho chó cuộn cá minh thái là món ăn vặt dinh dưỡng dành cho thú cưng, được chế biến từ các nguyên liệu tự nhiên như thịt gà, thịt vịt và cá minh thái. Sản phẩm không chỉ thơm ngon, kích thích vị giác mà còn cung cấp nguồn đạm chất lượng cao, ít béo và giàu vitamin nhóm B – hỗ trợ tăng cường sức khỏe tim mạch, cơ bắp và giảm căng thẳng cho thú cưng. Lợi ích Thịt gà là nguồn cung cấp chất đạm dồi dài, ít chất béo giúp cơ bắp được săn chắc hơn Thịt vịt là nguồn cung cấp chất đạm dồi dào, ít chất béo giúp giảm căng thẳng cho các bé cún Sự kết hợp giữa cá và gà vừa đầy đủ dưỡng chất cho 1 ngày hoạt động mà không sợ tăng cân Thành Phần Thịt gà (thịt vịt), cá minh thái khô, tinh bột bắp, đạm đậu hà lan, muối, D-sorbitol, Glycerrin Hướng dẫn sử dụng Nhỏ hơn 5kg: 1-2 miếng/ngày Từ 5-10…

Thương hiệu: Gooday.

Nguồn tham khảo: https://paddy.vn/products/banh-thuong-cho-cho-thit-cuon-ca-gooday-80g', 8, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Bánh Thưởng Cho Chó Thịt Cuộn Cá Gooday 80g');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Đồ Chơi Cho Chó Thú Bông FOFOS Summer', 'products/paddy_087_do-choi-cho-cho-thu-bong-fofos-summer.webp', 105000, 0,
       'Đồ Chơi Cho Chó Thú Bông FOFOS Summer Thương hiệu: FOFOS Phù hợp cho: Chó mọi lứa tuổi FOFOS là thương hiệu đồ chơi cho chó nổi tiếng. Các sản phẩm của FOFOS được thiết kế dựa trên nhu cầu và sở thích của chó, với mục đích giúp chó vui chơi, giải trí và phát triển toàn diện. Các sản phẩm đồ chơi FOFOS có nhiều mẫu mã đa dạng, từ đồ chơi nhai, đồ chơi đuổi bắt, đồ chơi thông minh,... được làm từ chất liệu an toàn, thân thiện với môi trường. Lợi ích: Có loa lớn kích thích bản năng săn mồi vui chơi của chó Giảm căng thẳng cho chó, giúp chó vận động nhiều hơn Bảo vệ đồ đạc trong căn nhà của bạn Vải lông nhung bền bỉ, an toàn cho cún Thành phần Vải lông nhung, bông PP mềm mịn, an toàn Kích thước: Tráu dứa: 30x17.5x9cm Dưa hấu: 25x23.5x8.5cm Bông hoa: 39x17x4cm Hướng dẫn sử dụng Lựa chọn đồ chơi có kích thước phù hợp với thú cưng Kiểm tra sản phẩm trước khi cho thú cưng chơi Tránh tiếp xúc vớ…

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/do-choi-cho-cho-thu-bong-fofos-summer', 81, 300, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Đồ Chơi Cho Chó Thú Bông FOFOS Summer');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Đồ Chơi Cho Chó Thú Bông FOFOS Tua Rua', 'products/paddy_088_do-choi-cho-cho-thu-bong-fofos-tua-rua.webp', 88000, 0,
       'Đồ Chơi Cho Chó Thú Bông FOFOS Tua Rua Thương hiệu: FOFOS Phù hợp cho: Chó mọi lứa tuổi FOFOS là thương hiệu đồ chơi cho chó nổi tiếng. Các sản phẩm của FOFOS được thiết kế dựa trên nhu cầu và sở thích của chó, với mục đích giúp chó vui chơi, giải trí và phát triển toàn diện. Các sản phẩm đồ chơi FOFOS có nhiều mẫu mã đa dạng, từ đồ chơi nhai, đồ chơi đuổi bắt, đồ chơi thông minh,... được làm từ chất liệu an toàn, thân thiện với môi trường. Lợi ích: Có loa lớn kích thích bản năng săn mồi vui chơi của chó Giảm căng thẳng cho chó, giúp chó vận động nhiều hơn Bảo vệ đồ đạc trong căn nhà của bạn Vải lông nhung bền bỉ, an toàn cho cún Thành phần Vải lông nhung, bông PP mềm mịn, an toàn Kích thước: Koala: 18x5.5x16cm Heo hồng: 16x7x14cm Gà vàng: 18x8x16cm Hướng dẫn sử dụng Lựa chọn đồ chơi có kích thước phù hợp với thú cưng Kiểm tra sản phẩm trước khi cho thú cưng chơi Tránh tiếp xúc với lửa…

Thương hiệu: FOFOS.

Nguồn tham khảo: https://paddy.vn/products/do-choi-cho-cho-thu-bong-fofos-tua-rua', 82, 300, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Đồ Chơi Cho Chó Thú Bông FOFOS Tua Rua');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Chó Hỗ Trợ Tiêu Hóa Royal Canin Gastrointestinal Canine (Lon 400g)', 'products/paddy_089_pate-cho-ho-tro-tieu-hoa-royal-canin-gastrointestinal-canine-lon-400g.jpg', 172000, 0,
       'Pate Chó Hỗ Trợ Tiêu Hóa Royal Canin Gastrointestinal Canine (Lon 400g) Thương hiệu: Royal canin Phù hợp cho: Chó gặp vấn đề về tiêu hoá Pate cho chó Royal canin Gastrointestinal là dòng pate dinh dưỡng hoàn chỉnh dành cho chó, được thiết kế đặc biệt để hỗ trợ trong quá trình điều trị các vấn đề rối loạn hấp thu tại đường ruột. Lợi ích Công thức có độ tiêu hoá cao, cân bằng chất xơ và lợi khuẩn đường ruột, hỗ trợ cho quá trình chuyển hoá thức ăn Cung cấp năng lượng với khối lượng thức ăn ít hơn giúp giảm tải áp lực hoạt động cho đường ruột Mùi vị thơm ngon, kích thích cảm giác thèm ăn cho chó biếng ăn do gặp các vấn đề về đường ruột Thành phần Thịt và các sản phẩm phụ từ động vật, ngũ cốc, cá và sản phẩm từ cá, sản phẩm phụ từ thực vật, dầu & chất béo, khoáng chất, men. KHUYẾN CÁO Trong trường hợp chó mắc các bệnh cấp tính, để cải thiện sức khỏe đường ruột yêu cầu phải kiểm soát dinh dư…

Thương hiệu: Royal Canin.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-ho-tro-tieu-hoa-royal-canin-gastrointestinal-canine-lon-400g', 8, 500, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pate Chó Hỗ Trợ Tiêu Hóa Royal Canin Gastrointestinal Canine (Lon 400g)');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Đồ Chơi Cho Chó Mèo Lồng Quay Hạt Thông Minh Doggyman', 'products/paddy_090_long-quay-hat-thong-minh-cho-cho-meo-doggyman.webp', 270000, 0,
       'Đồ Chơi Cho Chó Mèo Lồng Quay Hạt Thông Minh Doggyman Thương hiệu: DoggyMan Phù hợp cho: Chó/Mèo mọi lứa tuổi Đồ chơi cho chó mèo lồng quay thúc đẩy phát triển não bộ, tạo thói quen tư duy cho thú cưng. Đặt thức ăn hạt bên trong lồng, kích thích ham muốn thèm ăn bằng khứa giác, thị giác và thính giác. Việc được thưởng sau một loạt hành động lặp đi lặp lại sẽ giúp thú cưng có được những thói quen tốt. Ngoài ra việc ăn chậm cũng giúp duy trì hệ tiêu hóa khỏe mạnh. Lợi ích Giảm thói quen ăn quá nhanh: Xoay mới có thức ăn → giúp bé ăn chậm, tốt cho tiêu hóa. Hỗ trợ huấn luyện & phát triển trí não: Bé học cách tự xoay để được thưởng → tăng khả năng tư duy & học hỏi. Tăng tính tò mò & ham chơi: Khi xoay thì lúc có lúc không → tạo cảm giác bất ngờ, khiến bé chơi mãi không chán. Dễ dàng vệ sinh, luôn sạch sẽ: Các bộ phận có thể tháo rời, rửa sạch toàn bộ → phù hợp với bé thích sạch sẽ. Thành ph…

Thương hiệu: DoggyMan.

Nguồn tham khảo: https://paddy.vn/products/long-quay-hat-thong-minh-cho-cho-meo-doggyman', 84, 700, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Đồ Chơi Cho Chó Mèo Lồng Quay Hạt Thông Minh Doggyman');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Chó Sỏi Thận Royal Canin Urinary S/O Lon 410g', 'products/paddy_091_pate-cho-cho-soi-than-royal-canin-urinary-s-o-410g.jpg', 155000, 0,
       'Pate Cho Chó Sỏi Thận Royal Canin Urinary S/O Lon 410g Thương hiệu: Royal canin Phù hợp cho: Chó trưởng thành đang điều trị thận Pate cho chó Royal Canin Urinary S/O là thức ăn dinh dưỡng hoàn chỉnh cho thú cưng, công thức hỗ trợ hòa tan sỏi Struvite và giảm tái phát. Lợi ích Hỗ trợ hoà tan tất cả các loại sỏi struvite Giảm khả năng hình thành các loại sỏi struvite và canxi oxalate trong nước tiểu Làm giảm nồng độ các ion có khả năng kết tinh thành sỏi Thành phần Thịt và dẫn xuất từ động vật, ngũ cốc, dầu và chất béo, protein thực vật, dẫn xuất từ thực vật, khoáng chất, các loại đường. KHUYẾN CÁO Nên hỏi ý kiến của Thú Y trước khi cho ăn. Cho chó sử dụng Urinary S/O từ 5 đến 12 tuần để hòa tan sỏi struvite và tối đa trong 6 tháng để giảm tái phát. Sau đó, chó cần được kiểm tra định kỳ tại Thú Y để nhận được lời khuyên cho chế độ ăn lâu dài. Luôn chuẩn bị sẵn nước sạch. Lưu ý: Cần kiểm t…

Thương hiệu: Royal Canin.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-cho-soi-than-royal-canin-urinary-s-o-410g', 40, 500, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pate Cho Chó Sỏi Thận Royal Canin Urinary S/O Lon 410g');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Cát Sắn Cho Mèo Purcats Miracle 2.5kg', 'products/paddy_092_cat-san-cho-meo-purcats-miracle-2-5kg.webp', 140000, 0,
       'Cát Sắn Cho Mèo Purcats Miracle 2.5kg Thương hiệu: Cature Phù hợp cho: Mèo mọi lứa tuổi Được nâng cấp từ dòng sản phẩm Purcat nổi tiếng, Purcat Miracle sở hữu công nghệ khóa mùi tiên tiến, giúp kiểm soát mùi hôi hiệu quả, mang lại không gian sạch sẽ, dễ chịu cho cả mèo cưng và sen. Cát mèo được làm từ nguyên liệu ăn được, không chứa hóa chất độc hại, không formaldehyde, không chất kết dính công nghiệp – an toàn tuyệt đối cho sức khỏe thú cưng. Lợi ích Sử dụng lâu hơn tiết kiệm hơn Nguyên liệu tự nhiên Vón cục chắc chắn Mềm mại với chân mèo Loại bỏ mùi khai Bảo vệ sức khoẻ hô hấp Kiểm soát mùi vượt trội Khả năng thấm hút tuyệt đổi Hướng dẫn sử dụng Đổ cát mèo vào nhà vệ sinh của mèo của bạn. Độ dày lớp cát khoảng 5-7cm. Giữ cho lớp cát luôn sạch sẽ bằng cách thường xuyên loại bỏ phân và vắt sạch cát thải. Nếu bạn muốn tăng hiệu quả khử mùi của cát mèo, bạn có thể sử dụng các sản phẩm khử…

Thương hiệu: Cature.

Nguồn tham khảo: https://paddy.vn/products/cat-san-cho-meo-purcats-miracle-2-5kg', 41, 2700, 'Vệ Sinh Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Cát Sắn Cho Mèo Purcats Miracle 2.5kg');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Đồ Chơi Cho Chó Banh Doggyman Hình Đuôi', 'products/paddy_093_do-choi-cho-cho-banh-doggyman-hinh-duoi.jpg', 72000, 0,
       'Đồ Chơi Cho Chó Banh Doggyman Hình Đuôi Thương hiệu: Doggyman Phù hợp cho: Chó mọi lứa tuổi Đồ chơi cho chó nhồi bông hình đuôi mềm mại và dễ thương giúp giao tiếp với thú cưng. Có chiếc chuông bên trong tạo âm thanh khi vui đùa giúp chó cưng thích thú. Lợi ích: Giảm căng thẳng và lo âu Kích thích vận động Tăng cường tương tác Chất liệu an toàn và bền bỉ Thành phần Polyester, Cotton. Kích thước (mm): 140W x 130H x 140L Hướng dẫn sử dụng Bạn có thể cho chó tự chơi hoặc cùng chơi để tăng cường sự gắn kết. Vệ sinh đồ chơi định kỳ bằng nước sạch để đảm bảo vệ sinh. Kiểm tra đồ chơi thường xuyên để đảm bảo không có bộ phận nào bị hỏng, tránh trường hợp thú cưng nuốt phải. 👉Xem thêm các sản phẩm khác tại Paddy.vn #dochoichomeo #dochomeo #phukienthucung #meoanhlongngan #chamsocthucung #phukiencho #dochoimeo #doggyman #dochoidoggyman

Thương hiệu: DoggyMan.

Nguồn tham khảo: https://paddy.vn/products/do-choi-cho-cho-banh-doggyman-hinh-duoi', 42, 100, 'Đồ Chơi Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Đồ Chơi Cho Chó Banh Doggyman Hình Đuôi');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Mèo Ciao 40g (Thái Lan)', 'products/paddy_094_pate-cho-meo-ciao-40g-thai-lan.jpg', 15000, 0,
       'Pate Cho Mèo Ciao 40g (Thái Lan) Thương hiệu: Ciao Phù hợp cho: Mèo trưởng thành Mang đến trải nghiệm ẩm thực đẳng cấp cho mèo cưng, Pate Mèo Ciao là sự kết hợp hoàn hảo của những nguyên liệu cao cấp, được chế biến thành dạng súp đặc sánh mịn mà bất kỳ chú mèo nào cũng khó lòng cưỡng lại. Lợi ích Sản phẩm có thành phần chứa chiết xuất trà xanh giúp làm giảm mùi hôi của các tạp chất trong đường ruột, mùi hôi của phân và mùi nước tiểu khi bé đi vệ sinh. Đặc biệt sản phẩm không chứa chất bảo quản nên an toàn cho thú nuôi khi sử dụng Thành phần Thịt gà, cá ngừ, chiết xuất sò điệp, đường (oligosacarit), dầu thực vật và chất béo, tinh bột biến tính, khoáng chất, polysacarit, gia vị (axit amin), vitamin E, sắc tố gạo men đỏ, chiết xuất trà xanh 👉 Xem thêm sản phẩm khác tại Paddy.vn #thucanuot #patemeo #thucanchomeo #thucanuotchomeo #patechomeo #chamsocthucung #Ciao

Thương hiệu: Ciao.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-ciao-40g-thai-lan', 43, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pate Cho Mèo Ciao 40g (Thái Lan)');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Cho Chó Natural Lab Thịt Viên Hộp 1kg', 'products/paddy_095_thit-vien-cho-cho-natural-lab-hop-1kg.webp', 360000, 0,
       'Bánh Thưởng Cho Chó Natural Lab Thịt Viên Hộp 1kg Thương hiệu: Natural Lab Phù hợp cho: Chó mọi lứa tuổi Bạn có muốn bé cún luôn vui vẻ, khỏe mạnh và tràn đầy năng lượng không? Với Natural Lab, Sen không chỉ mang về một món bánh thưởng cho chó siêu hấp dẫn mà còn là nguồn dinh dưỡng bổ sung tuyệt vời. Từng chiếc bánh không chỉ thơm ngon khó cưỡng, khiến bé cún mê tít, mà còn cung cấp năng lượng và dưỡng chất cần thiết, giúp bé luôn sẵn sàng khám phá và vui đùa mỗi ngày Lợi ích Không chứa chất phụ gia: An toàn tuyệt đối cho sức khỏe lâu dài của cún. Giàu protein, ít chất béo: Hỗ trợ phát triển cơ bắp và duy trì vóc dáng cân đối, lý tưởng cho chế độ ăn kiêng lành mạnh. Hỗ trợ hệ tiêu hóa tốt: Giúp cún hấp thu dinh dưỡng hiệu quả và duy trì đường ruột khỏe mạnh. Chăm sóc và bảo vệ răng chắc khỏe: Góp phần làm sạch răng và nướu khi cún nhai. Thành phần Thịt lợn, bột mì, thịt gà tây, tinh bộ…

Thương hiệu: Natural Lab.

Nguồn tham khảo: https://paddy.vn/products/thit-vien-cho-cho-natural-lab-hop-1kg', 44, 100, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Bánh Thưởng Cho Chó Natural Lab Thịt Viên Hộp 1kg');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bát Ăn Cho Chó Mèo Bằng Nhựa Nhiều Kiểu Dáng', 'products/paddy_096_bat-an-cho-cho-meo-bang-nhua-nhieu-kieu-dang.jpg', 75000, 0,
       'Bát Ăn Cho Chó Mèo Bằng Nhựa Nhiều Kiểu Dáng Thương hiệu: Paddy Phù hợp cho: Chó/Mèo mọi lứa tuổi Dụng cụ ăn uống cho chó mèo bát ăn có nhiều kích cỡ, kiểu dáng và màu sắc bắt mắt, hấp dẫn. Chất liệu nhựa PP cao cấp, chắc chắn. Bề mặt trơn láng, dễ dàng chùi rửa sạch sẽ sau khi sử dụng. Sản phẩm được làm từ chất liệu nhựa cao cấp không gây hại, không làm ảnh hưởng đến chất lượng thức ăn. Lợi ích: Có thể sử dụng đựng đồ ăn hoặc nước uống Đa dạng mẫu mã dễ thương Có nhiều kích thước cho sen lựa chọn Thành phần: Chất liệu nhựa PP cao cấp GIAO MÀU SẮC NGẪU NHIÊN 👉 Xem thêm sản phẩm khác tại Paddy.vn #paddypetshop #batanchomeo #batanthucung #chamsocchomeo #dungcuanuong #phukienchomeo #batancho #batanmeo #khayanchomeo

Thương hiệu: Paddy.

Nguồn tham khảo: https://paddy.vn/products/bat-an-cho-cho-meo-bang-nhua-nhieu-kieu-dang', 45, 300, 'Phụ Kiện Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Bát Ăn Cho Chó Mèo Bằng Nhựa Nhiều Kiểu Dáng');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Bánh Thưởng Cho Chó Dexinbone Hỗ Trợ Sạch Răng', 'products/paddy_097_snack-cho-cho-dexinbone-sach-rang.jpg', 32000, 0,
       'Bánh Thưởng Cho Chó Dexinbone Hỗ Trợ Sạch Răng Thương hiệu: INU Fonti Phù hợp cho: Chó trưởng thành Snack cho Chó Dexinbone là dòng sản phẩm đồ ăn vặt chuyên biệt dành cho chó, được thiết kế không chỉ để thỏa mãn vị giác mà còn mang lại nhiều lợi ích vượt trội cho sức khỏe. Với kết cấu đặc biệt và thành phần được nghiên cứu kỹ lưỡng, mỗi miếng Snack Chó Dexinbone không chỉ là một phần thưởng mà còn là một công cụ chăm sóc răng miệng hiệu quả. Lợi ích Giảm mảng bám và cao răng Thành phần tốt cho tiêu hoá Giảm căng thẳng và chống buồn chán Chắc khoẻ cơ bắp, răng và xương Hướng dẫn bảo quản Luôn giữ sản phẩm còn lại trong túi hoặc thùng kín Bảo quản sản phẩm ở nơi khô ráo, thoáng mát, tránh tiếp xúc trực tiếp với ánh sáng mặt trời Xem thêm các sản phẩm khác tại Paddy.vn #banhthuongchocho #snackcho #thucanchocho #doancho #chamsocrangmieng #chamsocthucung #INUFONTI

Thương hiệu: INU Fonti.

Nguồn tham khảo: https://paddy.vn/products/snack-cho-cho-dexinbone-sach-rang', 46, 200, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Bánh Thưởng Cho Chó Dexinbone Hỗ Trợ Sạch Răng');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Chó Kiểm Soát Cân Nặng Royal Canin Satiety Weight Management Lon 410g', 'products/paddy_098_pate-cho-cho-kiem-soat-can-nang-royal-canin-satiety-weight-management-lon-410g.jpg', 155000, 0,
       'Pate Cho Chó Kiểm Soát Cân Nặng Royal Canin Satiety Weight Management Lon 410g Thương hiệu: Royal Canin Phù hợp cho: Chó (trên 12 tháng tuổi bị thừa cân/ béo phì cần giảm cân) Pate cho chó ROYAL CANIN Satiety là chế độ ăn hoàn chỉnh và cân bằng, hàm lượng xơ cao tạo cảm giác no lâu, giúp chú chó của bạn cảm thấy hài lòng sau mỗi bữa ăn từ đó giảm hành vi xin ăn. Những chú chó sẽ ăn ít lại nhưng vẫn duy trì được cơ bắp nhờ thành phần potein chất lượng cao, đồng thời bổ sung Glucosamine và Chondroitin hỗ trợ cho xương khớp chịu áp lực bởi cân nặng quá tải. Hợp chất chống oxy hóa được cấp bằng sáng chế hỗ trợ hệ miễn dịch tự nhiên. Lợi ích: Giúp kiểm soát cân nặng hiệu quả Giúp cảm giác no lâu hơn, giảm tình trạng thèm ăn, xin ăn ở cún Duy trì lượng cơ nạc Thành phần Thịt và dẫn xuất từ động vật, dẫn xuất từ thực vật, ngũ cốc, dầu và chất béo, khoáng chất, động vật thân mềm và giáp xác. Hư…

Thương hiệu: Royal Canin.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-cho-kiem-soat-can-nang-royal-canin-satiety-weight-management-lon-410g', 47, 500, 'Thức Ăn Cho Chó',
       (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pate Cho Chó Kiểm Soát Cân Nặng Royal Canin Satiety Weight Management Lon 410g');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Pate Cho Mèo Miratorg Thơm Ngon 80g', 'products/paddy_099_pate-cho-meo-miratorg-thom-ngon-80g.jpg', 25000, 0,
       'Pate Cho Mèo Miratorg Thơm Ngon 80g Thương hiệu: Miratorg Phù hợp cho: Mèo tùy từng loại sản phẩm Thức ăn cho mèo Miratorg là thương hiệu thức ăn thượng hạng từ Nga, nổi bật với tiêu chí “Thịt là thành phần cốt lõi.” Sản phẩm sử dụng nguồn thịt tươi, cung cấp protein dồi dào, hỗ trợ phát triển cơ bắp và tăng cường sức khỏe cho thú cưng. Không chứa hương liệu nhân tạo, không GMO, đảm bảo chất lượng dinh dưỡng tối ưu. Lợi ích: Cung cấp Protein chất lượng cao Kiểm soát cân nặng, ngăn ngừa béo phì Công thức xốt thơm ngon, kích thích vị giác Bảo vệ hệ bài tiết, giảm nguy cơ sỏi thận Nguồn dinh dưỡng cân bằng, hỗ trợ sức khỏe toàn diện Thành phần dinh dưỡng Thịt và nguyên liệu thịt – 36%, bột củ cải khô, chiết xuất đạm thực vật, khoáng chất, muối, dầu cá hồi, axit amin, vitamin, c anxi cacbonat, L-Carnitine Hướng dẫn sử dụng Bảo quản nơi khô ráo, thoáng mát Tránh ánh nắng trực tiếp Sau khi mở…

Thương hiệu: Miratorg.

Nguồn tham khảo: https://paddy.vn/products/pate-cho-meo-miratorg-thom-ngon-80g', 8, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Pate Cho Mèo Miratorg Thơm Ngon 80g');

INSERT INTO products (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
SELECT 'Hạt Cho Mèo Trưởng Thành Miratorg Nhiều Thịt Thơm Ngon', 'products/paddy_100_hat-cho-meo-truong-thanh-miratorg-nhieu-thit-thom-ngon.jpg', 59000, 0,
       'Hạt Cho Mèo Trưởng Thành Miratorg Nhiều Thịt Thơm Ngon Thương hiệu: Miratorg Phù hợp cho: Mèo từ 12 tháng tuổi Thức ăn cho mèo Miratorg là thương hiệu thức ăn thượng hạng từ Nga, nổi bật với tiêu chí “Thịt là thành phần cốt lõi.” Sản phẩm sử dụng nguồn thịt tươi, cung cấp protein dồi dào, hỗ trợ phát triển cơ bắp và tăng cường sức khỏe cho thú cưng. Không chứa hương liệu nhân tạo, không GMO, đảm bảo chất lượng dinh dưỡng tối ưu. Lợi ích: Nguồn dinh dưỡng từ thịt tươi chất lượng cao Cung cấp protein chất lượng cao Dưỡng chất cân bằng cho sức khỏe toàn diện Nuôi dưỡng da lông mềm mượt và hỗ trợ tiêu hóa Thành phần dinh dưỡng Thịt và thành phần thịt – 32%, l úa mì, đạm ngô thủy phân, ngô, mỡ động vật, đạm động vật thủy phân, khoáng chất, muối, men bia, vitamin, bột củ cải khô, mỡ cá hồi Hướng dẫn sử dụng Bảo quản nơi khô ráo, thoáng mát Tránh ánh nắng trực tiếp Đóng kín miệng túi sau khi s…

Thương hiệu: Miratorg.

Nguồn tham khảo: https://paddy.vn/products/hat-cho-meo-truong-thanh-miratorg-nhieu-thit-thom-ngon', 8, 100, 'Thức Ăn Cho Mèo',
       (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1), 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Hạt Cho Mèo Trưởng Thành Miratorg Nhiều Thịt Thơm Ngon');
