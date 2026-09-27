package services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.JsonNode;

class ShippingServiceJsonLenientTest {

    private static Object parse(String json) throws Exception {
        Method m = ShippingService.class.getDeclaredMethod("parseJsonLenient", String.class);
        m.setAccessible(true);
        return m.invoke(null, json);
    }

    @Test
    void parsesStrictJson() throws Exception {
        JsonNode node = (JsonNode) parse("{\"ok\":true,\"n\":1}");
        assertEquals("1", node.path("n").asString());
    }

    @Test
    void parsesLenientJsonThatStrictRejects() throws Exception {
        // GHN từng trả JSON lỗi; Gson lenient vẫn đọc được
        JsonNode node = (JsonNode) parse("{ok:true,'note':'xà'}");
        assertEquals("xà", node.path("note").asString());
    }

    @Test
    void nonJsonStillThrowsWithContext() {
        assertThrows(Exception.class, () -> parse("<html>502</html>"));
    }
}
