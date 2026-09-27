package Util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

class JsonBehaviorTest {

    @Test
    void readsLenientJsonWithSingleQuotesAndUnquotedNames() throws Exception {
        JsonNode node = Json.MAPPER.readTree("{name:'xà', count:2}");
        assertEquals("xà", node.path("name").asString());
        assertEquals(2, node.path("count").asInt());
    }

    @Test
    void htmlIsNotEscapedOnWrite() throws Exception {
        ObjectNode out = Json.MAPPER.createObjectNode();
        out.put("html", "<b>xà & \"y\"</b>");
        String json = Json.MAPPER.writeValueAsString(out);
        assertTrue(json.contains("<b>"), "Jackson ghi thẳng HTML, KHÔNG escape như Gson: " + json);
        assertTrue(json.contains("xà"), "Unicode tiếng Việt phải giữ nguyên: " + json);
    }

    @Test
    void bigDecimalAmountsWritePlainNotScientific() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("amount", new BigDecimal("258000"));
        payload.put("fee", new BigDecimal("0.000001"));
        String json = Json.MAPPER.writeValueAsString(payload);
        assertTrue(json.contains("258000"), json);
        assertTrue(json.contains("0.000001"), "Không được ghi dạng 1.0E-6: " + json);
    }

    @Test
    void buildsNestedPayloadLikeGsonObjectModel() throws Exception {
        ObjectNode root = Json.MAPPER.createObjectNode();
        root.put("status", "ok");
        ObjectNode nested = Json.MAPPER.createObjectNode();
        nested.put("id", 7);
        ArrayNode arr = Json.MAPPER.createArrayNode();
        arr.add(1).add(2);
        root.set("data", nested);
        root.set("list", arr);

        String json = Json.MAPPER.writeValueAsString(root);
        assertEquals("{\"status\":\"ok\",\"data\":{\"id\":7},\"list\":[1,2]}", json);
    }
}
