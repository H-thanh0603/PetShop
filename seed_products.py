"""
PetShop Seed Script
Generates and inserts pet_types + products into the petvaccine DB.
Usage: python seed_products.py
Requires: pip install mysql-connector-python
"""
import mysql.connector

DB = dict(host="localhost", port=3306, user="root", password="MySQL Root Password", database="petvaccine")

# --------------------------------------------------------------------------
# pet_types  (code, name, icon, display_order)
# --------------------------------------------------------------------------
PET_TYPES = [
    ("dog",    "Chó",      "bxs-dog",  1),
    ("cat",    "Mèo",      "bxs-cat",  2),
    ("fish",   "Cá",       "bxs-fish", 3),
    ("bird",   "Chim",     "bx-bird",  4),
    ("hamster","Hamster",  "bxs-bug",  5),
    ("rabbit", "Thỏ",      "bxs-leaf", 6),
]

# --------------------------------------------------------------------------
# Products  (name, image_url, price, discount, description, stock, weight_g, category, pet_type_code)
# Using picsum.photos seeds for reliable placeholder images
# --------------------------------------------------------------------------
def img(seed, w=400, h=400):
    import hashlib
    # Extract keywords by removing numbers: 'dog-food-1' -> 'dog,food'
    keywords = ",".join([word for word in seed.split('-') if not word.isdigit()])
    lock_id = int(hashlib.md5(seed.encode()).hexdigest(), 16) % 10000 + 1
    return f"https://loremflickr.com/{w}/{h}/{keywords}/all?lock={lock_id}"

PRODUCTS = [
    # ===== CHÓ =====
    ("Royal Canin Adult Medium 10kg",         img("dog-food-1"), 580000, 5,  "Thức ăn hạt khô cao cấp cho chó trưởng thành giống trung, cân bằng dinh dưỡng, hỗ trợ tiêu hoá và lông bóng mượt. Xuất xứ Pháp.", 50, 10000, "Thức ăn",    "dog"),
    ("Pedigree Beef & Vegetable 3kg",          img("dog-food-2"), 185000, 10, "Thức ăn hạt Pedigree vị bò & rau củ, bổ sung canxi, vitamin tổng hợp, phù hợp chó mọi giống. Dễ tiêu, ngon miệng.", 80, 3000,  "Thức ăn",    "dog"),
    ("Whiskas Pate Gà Cá Ngừ 85g",            img("cat-food-1"), 18000,  0,  "Pate ướt Whiskas hương vị gà và cá ngừ, giàu đạm, độ ẩm cao giúp mèo uống đủ nước. Đóng gói tiện lợi.", 200, 85,   "Thức ăn",    "cat"),
    ("Me-O Dry Cat Ocean Fish 1.1kg",          img("cat-food-2"), 95000,  5,  "Thức ăn hạt Me-O vị cá biển, giàu omega-3 giúp lông mượt và tăng cường miễn dịch cho mèo trưởng thành.", 120, 1100, "Thức ăn",    "cat"),
    ("Combo 5 Gói Pate Whiskas Mèo 85g",       img("cat-food-3"), 85000,  0,  "Combo 5 gói pate Whiskas đa dạng hương vị: gà, cá ngừ, cá hồi, tôm và bò. Phù hợp thay đổi khẩu phần hàng ngày.", 100, 425,  "Thức ăn",    "cat"),
    ("Thức Ăn Cá Vàng Hikari Goldfish 100g",   img("fish-food-1"),45000,  0,  "Viên thức ăn Hikari dành riêng cho cá vàng. Làm sáng màu sắc, tăng cường sức đề kháng, không làm đục nước.", 150, 100,  "Thức ăn",    "fish"),
    ("Sera Vipan Nature 100ml",                img("fish-food-2"),55000,  5,  "Thức ăn mảnh Sera Vipan dành cho cá nhiệt đới, giàu carotinoid tự nhiên, viên nổi lâu không làm bẩn bể.", 130, 35,   "Thức ăn",    "fish"),
    ("Zupreem Fruit Blend Vẹt M 1lb",          img("bird-food-1"),210000, 0,  "Thức ăn viên ZuPreem hương trái cây cho vẹt cỡ vừa (conure, lovebird). Đủ 40 vitamin & khoáng chất.", 60,  454,  "Thức ăn",    "bird"),
    ("Versele-Laga Prestige Canary 1kg",       img("bird-food-2"),120000, 0,  "Hỗn hợp hạt cao cấp cho chim hoàng yến, chứa hạt lanh, kê, hướng dương mini. Giúp hót hay và lông đẹp.", 80,  1000, "Thức ăn",    "bird"),
    ("Hamster Lab Blocks 500g",                img("hamster-food"),75000, 0,  "Khối dinh dưỡng nén dành cho hamster, đủ protein, chất xơ và vitamin, kiểm soát mọc răng tự nhiên.", 90,  500,  "Thức ăn",    "hamster"),
    ("Supreme Selective Rabbit 1.5kg",         img("rabbit-food"), 165000,5,  "Thức ăn cỏ timothê nén dành cho thỏ trưởng thành. Hỗ trợ hệ tiêu hoá, mài mòn răng, không chất tạo màu.", 70,  1500, "Thức ăn",    "rabbit"),

    # ===== ĐỒ CHƠI =====
    ("Bóng Cao Su Squeak Cho Chó S",           img("dog-toy-1"),  35000,  0,  "Bóng cao su tự nhiên phát tiếng kêu kích thích bản năng săn mồi, an toàn nhai không vỡ mảnh. Size S dưới 10kg.", 150, 80,   "Đồ chơi",   "dog"),
    ("Rope Pull Toy Kéo Co Cho Chó",           img("dog-toy-2"),  55000,  10, "Dây thừng cotton đan xen 3 màu, chắc chắn, giúp làm sạch răng khi chơi kéo co. Phù hợp chó mọi size.", 100, 200,  "Đồ chơi",   "dog"),
    ("KONG Classic Nhồi Thức Ăn M",            img("dog-toy-3"),  185000, 0,  "Đồ chơi nhồi thức ăn KONG Classic rubber đỏ bền bỉ, giúp chó giải trí một mình, giảm lo âu khi chủ vắng nhà.", 60,  300,  "Đồ chơi",   "dog"),
    ("Cần Câu Lông Vũ Cho Mèo",               img("cat-toy-1"),  45000,  0,  "Cần câu mèo với lông vũ nhiều màu sắc, kích thích bản năng săn mồi, giúp mèo vận động và giảm stress.", 200, 50,   "Đồ chơi",   "cat"),
    ("Bóng Chuông Catnip Cho Mèo 3 Cái",      img("cat-toy-2"),  39000,  0,  "Set 3 bóng nhỏ có chuông và catnip bên trong. Mèo tự chơi được, chất liệu vải nỉ mềm mại không gây xước.", 180, 60,   "Đồ chơi",   "cat"),
    ("Tháp Scratching Post 60cm",              img("cat-toy-3"),  220000, 5,  "Cột cào móng mèo bọc sợi đay tự nhiên cao 60cm, đế ổn định 35x35cm. Tránh mèo cào sofa và rèm cửa.", 45,  2500, "Đồ chơi",   "cat"),
    ("Gương Cá Treo Bể Cho Betta",            img("fish-toy-1"), 25000,  0,  "Gương mini treo thành bể kích thích cá betta xoè vây, tăng hoạt động và màu sắc. Dễ gắn, an toàn với nước.", 200, 20,   "Đồ chơi",   "fish"),
    ("Xích Đu Chim Gỗ Tự Nhiên",             img("bird-toy-1"), 65000,  0,  "Xích đu gỗ tự nhiên an toàn cho chim, kèm chuông đồng và hạt gỗ màu. Giúp chim vận động và không buồn chán.", 120, 80,   "Đồ chơi",   "bird"),
    ("Bánh Xe Hamster Silent 21cm",            img("hamster-toy"),95000,  0,  "Bánh xe chạy bộ Hamster không tiếng ồn đường kính 21cm, khớp nối an toàn, phù hợp hamster winter white & syrian.", 80,  350,  "Đồ chơi",   "hamster"),
    ("Tunnel Thỏ 3 Đoạn Gập Được",           img("rabbit-toy"), 125000, 0,  "Đường hầm vải 3 đoạn gập gọn, nhiều màu sắc, kích thích thỏ khám phá và chui nhủi, giúp giảm stress.", 60,  400,  "Đồ chơi",   "rabbit"),

    # ===== NHÀ / CHUỒNG =====
    ("Chuồng Inox Chó Lớn 90x60cm",           img("dog-cage"),   850000, 10, "Chuồng inox 304 chắc chắn, ô lưới 3.5cm, đáy nhựa chống thấm dễ vệ sinh. Kích thước 90x60x70cm.", 20,  12000,"Nhà/Chuồng", "dog"),
    ("Nhà Vải Chó Mèo Igloo M",               img("pet-house-1"),195000, 5,  "Nhà lều hình dome vải oxford 600D kháng nước, đệm bông tháo giặt được. Ấm mùa đông, mát mùa hè.", 55,  800,  "Nhà/Chuồng", "dog"),
    ("Lồng Nhựa Mèo Hàng Không L",            img("cat-cage"),   450000, 0,  "Túi vận chuyển hàng không tiêu chuẩn IATA cho mèo đến 7kg. Cửa sắt khoá chắc, thông thoáng, đáy lót.", 30,  2500, "Nhà/Chuồng", "cat"),
    ("Bể Cá Kính Nano 30x20x25cm",            img("fish-tank"),  320000, 0,  "Bể kính cường lực trong suốt 15L kèm lọc treo mini, đèn LED 2 chế độ. Phù hợp cá betta, cá cảnh nhỏ.", 25,  6000, "Nhà/Chuồng", "fish"),
    ("Lồng Chim Sơn Ca Tròn Ø35",            img("bird-cage"),  280000, 0,  "Lồng chim sắt sơn tĩnh điện hình tròn đường kính 35cm, 2 cầu đậu gỗ, 2 cóng ăn inox. Kèm móc treo.", 40,  1800, "Nhà/Chuồng", "bird"),
    ("Chuồng Hamster Nhựa Hai Tầng",          img("hamster-cage"),275000,5,  "Chuồng 2 tầng có đường ống kết nối, buồng ngủ, khay cát riêng. Kích thước 45x30x35cm, nhựa an toàn.", 35,  2000, "Nhà/Chuồng", "hamster"),
    ("Chuồng Thỏ Gỗ Ngoài Trời 80cm",        img("rabbit-cage"),680000, 10, "Chuồng gỗ thông ngoài trời 80x50x70cm, sơn chống thấm, mái lợp tôn. Ô lưới inox chống gỉ, có khóa.", 15,  9000, "Nhà/Chuồng", "rabbit"),

    # ===== DẦU GỘI / VỆ SINH =====
    ("Sữa Tắm Chó Bio-Groom Šampon 355ml",    img("dog-shampoo"), 185000,0,  "Dầu gội dành cho chó mùi nước hoa nhẹ, pH trung tính, không chứa paraben. Làm sạch sâu và mềm lông.", 90,  355,  "Vệ sinh",   "dog"),
    ("Khăn Ướt Chó Mèo 100 Tờ",              img("pet-wipes"),   55000, 0,  "Khăn ướt kháng khuẩn không mùi, an toàn với da nhạy cảm thú cưng. Vệ sinh nhanh không cần tắm.", 150, 300,  "Vệ sinh",   "dog"),
    ("Dầu Gội Mèo TropiClean 473ml",          img("cat-shampoo"), 195000,5,  "Dầu gội mèo mùi dưa lưới, không chứa chất tẩy mạnh, chiết xuất lô hội làm êm da. Không rát mắt.", 80,  473,  "Vệ sinh",   "cat"),
    ("Cát Vệ Sinh Mèo Bentonite 5L",          img("cat-litter"),  85000, 0,  "Cát bentonite vón cục nhanh, khử mùi hiệu quả 48 giờ, bụi thấp, hương phấn nhẹ. Tiết kiệm 30% so với cát thường.", 120, 5000, "Vệ sinh",   "cat"),
    ("Máy Lọc Bể Cá Canister 500L/h",         img("fish-filter"), 420000,0,  "Máy lọc thùng ngoài công suất 500L/h, hệ thống lọc 3 tầng cơ học-sinh học-hoá học, vận hành êm.", 30,  2000, "Vệ sinh",   "fish"),

    # ===== PHỤ KIỆN =====
    ("Dây Dắt Chó Da Thật 120cm",             img("dog-leash"),   145000,0,  "Dây dắt chó da bò thuộc thật, khóa inox 304 bền, rộng 2cm, dài 120cm. Thích hợp chó từ 5-35kg.", 80,  200,  "Phụ kiện",  "dog"),
    ("Áo Mưa Chó Size L",                     img("dog-raincoat"),125000,0,  "Áo mưa chó nhựa TPU trong suốt, 4 chân chống thấm, dễ mặc/cởi. Size L vòng ngực 50-58cm.", 60,  180,  "Phụ kiện",  "dog"),
    ("Vòng Cổ GPS Theo Dõi Chó Mèo",          img("pet-gps"),     890000,5,  "Thiết bị GPS gắn vòng cổ, pin 7 ngày, chống nước IPX5, ứng dụng iOS/Android, bán kính cảnh báo tùy chỉnh.", 20,  50,   "Phụ kiện",  "dog"),
    ("Bát Ăn Inox Đôi Có Đế Cao Su",         img("dog-bowl"),    75000, 0,  "Bộ bát đôi inox 304 + đế cao su chống trượt. Dung tích 2x300ml, rửa máy rửa bát an toàn.", 150, 400,  "Phụ kiện",  "dog"),
    ("Túi Đựng Mèo Canvas 35x50cm",           img("cat-bag"),     285000,10, "Túi xách tay đựng mèo vải canvas dày, lưới thông khí 2 mặt, đai vai êm, kéo khóa YKK không tuột.", 50,  600,  "Phụ kiện",  "cat"),
    ("Cổng An Toàn Thú Cưng 75-82cm",         img("pet-gate"),    395000,0,  "Cổng chặn thú cưng điều chỉnh 75-82cm, khung thép sơn, khóa 2 lớp an toàn. Không cần khoan tường.", 25,  3000, "Phụ kiện",  "dog"),
    ("Nhiệt Kế Nước Bể Điện Tử",             img("fish-thermo"), 35000, 0,  "Nhiệt kế điện tử đo nước bể chính xác ±0.1°C, màn hình LCD, có cảnh báo nhiệt độ bất thường.", 200, 30,   "Phụ kiện",  "fish"),
    ("Máy Sục Khí Bể Cá Đôi",                img("fish-pump"),   95000, 0,  "Máy bơm oxy đôi đầu 3W, lưu lượng 2x2.5L/phút, van điều chỉnh, vận hành êm ái, kèm 2 đá sục khí.", 100, 400,  "Phụ kiện",  "fish"),
    ("Đèn UV Khử Trùng Lồng Chim",           img("bird-uv"),     175000,0,  "Đèn UVB 5.0 dành cho chim, phổ ánh sáng mô phỏng ánh nắng tự nhiên, giúp tổng hợp vitamin D3.", 45,  200,  "Phụ kiện",  "bird"),

    # ===== THUỐC / BỔ SUNG =====
    ("Nexgard Spectra Chó 3 Viên 7-15kg",     img("dog-medicine"),485000,0,  "Thuốc nhai trị ve, bọ chét, ghẻ, giun tim. Tác dụng 30 ngày, vị thịt bò, chó ăn tự nguyện. Hộp 3 viên.", 60,  30,   "Thuốc",     "dog"),
    ("Canin Denta Sticks 200g",               img("dog-dental"),  125000,0,  "Thanh nhai làm sạch răng hàng ngày, giảm 80% cao răng, hương bạc hà nhẹ, không đường. Hộp ~7 thanh.", 120, 200,  "Thuốc",     "dog"),
    ("Viên Bổ Sung Omega-3 Mèo 60 Viên",     img("cat-omega"),   175000,5,  "Viên dầu cá hồi chuẩn EPA/DHA cho mèo, làm bóng lông, giảm rụng lông theo mùa. Vị cá dễ ăn.", 90,  60,   "Thuốc",     "cat"),
    ("Revolution Plus Mèo 2.5-5kg 3 Tuýp",   img("cat-flea"),    420000,0,  "Thuốc nhỏ gáy phòng ve, bọ chét, giun lươn, ký sinh ngoài da. Hiệu quả 1 tháng. Xuất xứ Mỹ.", 50,  30,   "Thuốc",     "cat"),
    ("Canxi Lỏng Thỏ & Hamster 50ml",        img("rabbit-cal"),  65000, 0,  "Canxi lỏng bổ sung cho thỏ và hamster non, phòng ngừa còi xương. Pha vào nước uống 3-5 giọt/ngày.", 150, 50,   "Thuốc",     "rabbit"),
    ("Vitamin Tổng Hợp Chim Nhỏ 15ml",       img("bird-vit"),    55000, 0,  "Vitamin A, B, C, D, E và khoáng vi lượng dạng lỏng cho chim nhỏ. Pha nước uống, tăng sức đề kháng.", 200, 15,   "Thuốc",     "bird"),
    ("Sủi Khử Clo Bể Cá 30 Viên",           img("fish-dechlo"),  28000, 0,  "Viên sủi khử clor & cloamin nước máy tức thì, an toàn với cá và vi sinh. 1 viên cho 100L nước.", 300, 15,   "Thuốc",     "fish"),

    # ===== THÊM SẢN PHẨM ĐA DẠNG =====
    ("Royal Canin Kitten 2kg",                img("cat-kitten"),  290000,5,  "Hạt khô dành riêng cho mèo con 4-12 tháng. Đạm cao 34%, DHA hỗ trợ não bộ, hạt nhỏ vừa miệng mèo con.", 100, 2000, "Thức ăn",   "cat"),
    ("Hill's Science Adult Large Breed 14kg", img("dog-hills"),   1250000,8, "Hạt khô Hill's Science Plan chó lớn trưởng thành, glucosamine & chondroitin bảo vệ khớp. Xuất xứ Mỹ.", 25,  14000,"Thức ăn",   "dog"),
    ("Pate Chó Cesar Bò & Rau Củ 100g x4",   img("dog-pate"),    68000, 0,  "Combo 4 khay pate Cesar vị bò và rau củ, texture mềm mịn phù hợp chó lớn tuổi hoặc chó mới ốm dậy.", 120, 400,  "Thức ăn",   "dog"),
    ("Cat Grass Cỏ Mèo Tươi Kit",            img("cat-grass"),   45000, 0,  "Bộ kit trồng cỏ mèo tươi (lúa mì non), giảm búi lông trong bụng, giúp mèo tiêu hoá tốt hơn.", 150, 200,  "Phụ kiện",  "cat"),
    ("Đệm Nằm Tự Sưởi Cho Mèo 45x45cm",     img("cat-pad"),     220000,10, "Đệm tự phát nhiệt phản xạ thân nhiệt mèo, không dùng điện, bọc nhung mềm tháo giặt được.", 60,  500,  "Nhà/Chuồng","cat"),
    ("Bể Nhựa Rùa Có Đảo & Lọc 40cm",       img("turtle-tank"), 285000,5,  "Bể nhựa trong suốt 40cm có đảo ngồi sưởi nắng, bơm lọc 3W, thích hợp rùa tai đỏ dưới 15cm.", 30,  3000, "Nhà/Chuồng","fish"),
    ("Dây Kéo Chó Tự Động 5m 25kg",          img("dog-retract"), 165000,0,  "Dây dắt tự co 5m, tải trọng 25kg, thân nhựa ABS chống rơi vỡ, phanh tay 1 tay bấm tiện lợi.", 75,  300,  "Phụ kiện",  "dog"),
    ("Balo Đựng Chó Mèo Kính Bong Bóng",    img("pet-backpack"), 450000,10, "Balo phong cách astronaut trong suốt 360°, lỗ thông khí 2 bên, đệm vai êm. Phù hợp thú cưng <6kg.", 35,  1200, "Phụ kiện",  "dog"),
    ("Cối Xay Thức Ăn Ướt Tự Động 4L",      img("auto-feeder"),  520000,5,  "Máy cho ăn tự động 4L, hẹn giờ 4 bữa/ngày qua app, màn hình LCD, microphone ghi âm gọi thú cưng.", 20,  1500, "Phụ kiện",  "dog"),
    ("Nước Hoa Thú Cưng Natural Fresh 50ml", img("pet-perfume"),  85000, 0,  "Nước hoa thú cưng thành phần tự nhiên hương cam chanh, không cồn, không gây kích ứng da. Xịt lông an toàn.", 100, 50,   "Vệ sinh",   "dog"),
    ("Sand Tắm Khô Hamster Chinchilla 1kg",  img("hamster-sand"), 78000, 0,  "Cát tắm khô mịn 100% tự nhiên cho hamster và chinchilla, hút ẩm, diệt khuẩn, không bụi.", 110, 1000, "Vệ sinh",   "hamster"),
]

# --------------------------------------------------------------------------
def run():
    conn = mysql.connector.connect(**DB)
    cur  = conn.cursor()

    # 1) Upsert pet_types
    cur.execute("SELECT code, id FROM pet_types")
    existing = {row[0]: row[1] for row in cur.fetchall()}

    for code, name, icon, order in PET_TYPES:
        if code not in existing:
            cur.execute(
                "INSERT INTO pet_types (code, name, icon, display_order, is_active) VALUES (%s,%s,%s,%s,1)",
                (code, name, icon, order)
            )
            existing[code] = cur.lastrowid
            print(f"  + pet_type: {code}")

    conn.commit()

    # Refresh mapping
    cur.execute("SELECT code, id FROM pet_types")
    pt_map = {row[0]: row[1] for row in cur.fetchall()}

    # 2) Insert products (skip if name already exists)
    cur.execute("SELECT name FROM products")
    existing_names = {row[0] for row in cur.fetchall()}

    inserted = 0
    for (name, image, price, discount, description, stock, weight, category, pet_code) in PRODUCTS:
        if name in existing_names:
            continue
        pet_type_id = pt_map.get(pet_code, 1)
        cur.execute(
            """INSERT INTO products
               (name, image, price, discount, description, stock, weight, category, pet_type_id, is_active)
               VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,1)""",
            (name, image, price, discount, description, stock, weight, category, pet_type_id)
        )
        inserted += 1

    conn.commit()
    print(f"\nDone! Inserted {inserted} products (skipped {len(PRODUCTS)-inserted} duplicates).")
    cur.close()
    conn.close()

if __name__ == "__main__":
    run()
