package services.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

class OpenAiCompatibleProviderParseTest {

    private static ChatResponse parse(String body) throws Exception {
        OpenAiCompatibleProvider p = new OpenAiCompatibleProvider(
                "test", "https://example.test/v1", "k", "m", 5);
        Method m = OpenAiCompatibleProvider.class
                .getDeclaredMethod("parseResponse", String.class, String.class, long.class);
        m.setAccessible(true);
        return (ChatResponse) m.invoke(p, body, "req-1", 1L);
    }

    @Test
    void fullBodyParsesContentUsageAndModel() throws Exception {
        ChatResponse r = parse("{\"model\":\"m1\",\"choices\":[{\"message\":{\"content\":\"xin\"}}],"
                + "\"usage\":{\"prompt_tokens\":3,\"completion_tokens\":4}}");
        assertEquals("xin", r.getContent());
        assertEquals("m1", r.getModel());
        assertEquals(3, r.getPromptTokens());
        assertEquals(4, r.getCompletionTokens());
    }

    @Test
    void malformedBodyThrowsAiException() {
        assertThrows(Exception.class, () -> parse("<html>502</html>"));
    }

    @Test
    void missingChoicesThrowsAiException() {
        assertThrows(Exception.class, () -> parse("{\"model\":\"m1\"}"));
    }
}
