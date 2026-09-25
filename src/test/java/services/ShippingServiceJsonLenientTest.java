package services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;   // Task 8 Step 2 sẽ đổi sang JsonNode — assertion giữ nguyên

class ShippingServiceJsonLenientTest {

    private static Object parse(String json) throws Exception {
        Method m = ShippingService.class.getDeclaredMethod("parseJsonLenient", String.class);
        m.setAccessible(true);
        return m.invoke(null, json);
    }

    @Test
    void parsesStrictJson() throws Exception {
        JsonObject node = (JsonObject) parse("{\"ok\":true,\"n\":1}");
        assertEquals("1", node.get("n").getAsString());
    }

    @Test
    void parsesLenientJsonThatStrictRejects() throws Exception {
        // GHN từng trả JSON lỗi; Gson lenient vẫn đọc được
        JsonObject node = (JsonObject) parse("{ok:true,'note':'xà'}");
        assertEquals("xà", node.get("note").getAsString());
    }

    @Test
    void nonJsonStillThrowsWithContext() {
        assertThrows(Exception.class, () -> parse("<html>502</html>"));
    }
}
