package Util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ValidationUtilRecipientTest {

    @Test
    void validRecipientPhoneHasTenDigitsAndStartsWithZero() {
        assertNull(ValidationUtil.validateRecipientPhone("0912345678"));
    }

    @Test
    void recipientPhoneRejectsInvalidFormatsWithClearMessages() {
        assertEquals("Số điện thoại người nhận phải bắt đầu bằng số 0.",
                ValidationUtil.validateRecipientPhone("1912345678"));
        assertEquals("Số điện thoại người nhận phải có đúng 10 chữ số.",
                ValidationUtil.validateRecipientPhone("091234567"));
        assertEquals("Số điện thoại người nhận phải có đúng 10 chữ số.",
                ValidationUtil.validateRecipientPhone("09123456789"));
        assertEquals("Số điện thoại người nhận chỉ được nhập số.",
                ValidationUtil.validateRecipientPhone("09123456a8"));
        assertEquals("Số điện thoại người nhận chỉ được nhập số.",
                ValidationUtil.validateRecipientPhone("09123456-8"));
        assertEquals("Số điện thoại người nhận không được chứa khoảng trắng.",
                ValidationUtil.validateRecipientPhone("091 2345678"));
    }

    @Test
    void recipientNameIsRequiredAndBounded() {
        assertEquals("Họ và tên người nhận không được để trống.",
                ValidationUtil.validateRecipientName(" "));
        assertEquals("Họ và tên người nhận không được vượt quá 100 ký tự.",
                ValidationUtil.validateRecipientName("A".repeat(101)));
        assertNull(ValidationUtil.validateRecipientName("Nguyễn Văn A"));
    }
}
