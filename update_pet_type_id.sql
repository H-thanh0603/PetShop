USE `petvaccine`;

-- =============================================
-- CẬP NHẬT pet_type_id CHO CÁC SẢN PHẨM
-- Dựa trên category để gán đúng loại thú cưng
-- =============================================

-- Lấy ID của pet_types
SET @dog_id = (SELECT id FROM pet_types WHERE code = 'dog' LIMIT 1);
SET @cat_id = (SELECT id FROM pet_types WHERE code = 'cat' LIMIT 1);

-- Cập nhật sản phẩm CHÓ (category chứa "Chó")
UPDATE products SET pet_type_id = @dog_id 
WHERE category LIKE '%Chó%' OR category LIKE '%cho chó%' OR category LIKE '%Cho Chó%';

-- Cập nhật sản phẩm MÈO (category chứa "Mèo")
UPDATE products SET pet_type_id = @cat_id 
WHERE category LIKE '%Mèo%' OR category LIKE '%cho mèo%' OR category LIKE '%Cho Mèo%';

-- Kiểm tra kết quả
SELECT 
    pt.name as pet_type,
    COUNT(p.id) as product_count
FROM products p
LEFT JOIN pet_types pt ON p.pet_type_id = pt.id
GROUP BY p.pet_type_id, pt.name;

-- Hiển thị sản phẩm chưa được gán pet_type_id
SELECT id, name, category, pet_type_id 
FROM products 
WHERE pet_type_id IS NULL;
