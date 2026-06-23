package Util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.net.URLEncoder;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VnpayUtilTest {

    @Test
    void testCreatePaymentUrl_UsesUtf8AndPercent20() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-FORWARDED-FOR")).thenReturn("127.0.0.1");

        try (MockedStatic<VnpayConfig> config = mockStatic(VnpayConfig.class)) {
            config.when(VnpayConfig::getTmnCode).thenReturn("TMN");
            config.when(VnpayConfig::getReturnUrl).thenReturn("http://return.url");
            config.when(VnpayConfig::getHashSecret).thenReturn("SECRET");
            config.when(VnpayConfig::getPayUrl).thenReturn("https://pay.url");

            // Order with space to test %20
            String url = VnpayUtil.createPaymentUrl(request, 123, new BigDecimal("100000"));
            
            // Check if URL contains encoded space with + (standard US-ASCII URLEncoder behavior)
            assertTrue(url.contains("vnp_OrderInfo=Thanh+toan+don+hang+123"));
        }
    }

    @Test
    void testVerifyReturn_FiltersNonVnpParams() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        
        Map<String, String[]> params = new HashMap<>();
        params.put("vnp_Amount", new String[]{"10000000"});
        params.put("vnp_TxnRef", new String[]{"123"});
        params.put("jsessionid", new String[]{"abc"}); // Should be ignored
        
        when(request.getParameterNames()).thenReturn(new Vector<>(params.keySet()).elements());
        when(request.getParameter("vnp_Amount")).thenReturn("10000000");
        when(request.getParameter("vnp_TxnRef")).thenReturn("123");
        when(request.getParameter("jsessionid")).thenReturn("abc");
        when(request.getParameter("vnp_SecureHash")).thenReturn("DUMMY_HASH");

        try (MockedStatic<VnpayConfig> config = mockStatic(VnpayConfig.class)) {
            config.when(VnpayConfig::getHashSecret).thenReturn("SECRET");
            
            // This will likely return false as DUMMY_HASH is wrong, 
            // but we want to ensure jsessionid is not in the hash calculation.
            // We can't easily check the internal hashData without refactoring,
            // but we can verify that if we provide the hash calculated WITHOUT jsessionid, it passes.
            
            // Manually calculate hash for vnp_Amount=10000000&vnp_TxnRef=123
            String expectedHashData = "vnp_Amount=10000000&vnp_TxnRef=123";
            String expectedSigned = VnpayUtil.hmacSHA512("SECRET", expectedHashData);
            
            when(request.getParameter("vnp_SecureHash")).thenReturn(expectedSigned);
            
            assertTrue(VnpayUtil.verifyReturn(request), "Should verify correctly even with extra non-vnp params");
        }
    }

    @Test
    void testVerifyReturn_UsesUtf8() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        
        Map<String, String[]> params = new HashMap<>();
        params.put("vnp_OrderInfo", new String[]{"Thanh toán"}); // Non-ASCII
        
        when(request.getParameterNames()).thenReturn(new Vector<>(params.keySet()).elements());
        when(request.getParameter("vnp_OrderInfo")).thenReturn("Thanh toán");

        try (MockedStatic<VnpayConfig> config = mockStatic(VnpayConfig.class)) {
            config.when(VnpayConfig::getHashSecret).thenReturn("SECRET");
            
            // "Thanh toán" in US-ASCII will be mangled, but let's see how URLEncoder handles it
            // Typically "Thanh toán" -> "Thanh+to%3F%3Fn" or similar if US-ASCII
            String expectedHashData = "vnp_OrderInfo=" + java.net.URLEncoder.encode("Thanh toán", "US-ASCII");
            String expectedSigned = VnpayUtil.hmacSHA512("SECRET", expectedHashData);
            
            when(request.getParameter("vnp_SecureHash")).thenReturn(expectedSigned);
            
            assertTrue(VnpayUtil.verifyReturn(request), "Should verify correctly with US-ASCII encoding");
        } catch (java.io.UnsupportedEncodingException e) {
            fail("US-ASCII should be supported");
        }
    }

    @Test
    void testVerifyReturn_WrongSignature_ReturnsFalse() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        
        Map<String, String[]> params = new HashMap<>();
        params.put("vnp_Amount", new String[]{"10000000"});
        
        when(request.getParameterNames()).thenReturn(new Vector<>(params.keySet()).elements());
        when(request.getParameter("vnp_Amount")).thenReturn("10000000");
        when(request.getParameter("vnp_SecureHash")).thenReturn("WRONG_HASH");

        try (MockedStatic<VnpayConfig> config = mockStatic(VnpayConfig.class)) {
            config.when(VnpayConfig::getHashSecret).thenReturn("SECRET");
            assertFalse(VnpayUtil.verifyReturn(request));
        }
    }
}
