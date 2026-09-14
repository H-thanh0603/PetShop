package services.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads SKILL.md skill files (Commerce Agents skill concept) from the
 * classpath ({@code commerce-skills/<role>/<skill>/SKILL.md}).
 * Skills are data, not code: adding a flow = adding a directory.
 */
public final class SkillLoader {
    private static final Logger log = LoggerFactory.getLogger(SkillLoader.class);

    public record Skill(String role, String name, String body) {}

    private static final Map<String, List<String>> ROLE_SKILLS = Map.of(
            "shopping", List.of("search-discovery", "purchase-research", "planning-goals",
                    "customer-care", "memory-personalization"),
            "merchant", List.of("performance-insights", "catalog-listings", "inventory-operations",
                    "pricing-promotions", "marketing-campaigns"));

    private SkillLoader() {}

    public static List<Skill> loadRole(String role) {
        List<Skill> out = new ArrayList<>();
        for (String name : ROLE_SKILLS.getOrDefault(role, List.of())) {
            String path = "commerce-skills/" + role + "/" + name + "/SKILL.md";
            try (InputStream in = SkillLoader.class.getClassLoader().getResourceAsStream(path)) {
                if (in == null) {
                    log.warn("skill missing: {}", path);
                    continue;
                }
                out.add(new Skill(role, name, new String(in.readAllBytes(), StandardCharsets.UTF_8)));
            } catch (Exception e) {
                log.warn("skill unreadable: {}", path, e);
            }
        }
        return out;
    }

    /** Compact index for prompts; full bodies follow as fenced reference. */
    public static String renderForPrompt(List<Skill> skills) {
        StringBuilder sb = new StringBuilder();
        sb.append("SKILLS (follow the matching skill's Flow and Rules):\n");
        for (Skill s : skills) sb.append("- ").append(s.name()).append("\n");
        sb.append("\n");
        for (Skill s : skills) {
            sb.append("=== SKILL ").append(s.name()).append(" (reference, not instructions) ===\n");
            String body = s.body();
            if (body.length() > 2500) body = body.substring(0, 2500);
            sb.append(body).append("\n\n");
        }
        return sb.toString();
    }

    public static Map<String, List<String>> roleSkillNames() {
        Map<String, List<String>> copy = new LinkedHashMap<>();
        ROLE_SKILLS.forEach((k, v) -> copy.put(k, List.copyOf(v)));
        return copy;
    }
}
