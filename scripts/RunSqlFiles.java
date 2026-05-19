import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RunSqlFiles {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("Pass at least one SQL file path.");
        }

        Properties db = loadDbProperties();
        String url = "jdbc:mysql://" + db.getProperty("db.host", "localhost").trim()
                + ":" + db.getProperty("db.port", "3306").trim()
                + "/" + db.getProperty("db.dbname", "petvaccine").trim()
                + "?" + db.getProperty("db.option", "useUnicode=true&characterEncoding=utf-8").trim();
        String user = db.getProperty("db.username", "root").trim();
        String password = resolvePassword(db.getProperty("db.password", ""));

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                for (String arg : args) {
                    Path sqlFile = Path.of(arg);
                    String sql = stripLineComments(Files.readString(sqlFile, StandardCharsets.UTF_8));
                    int executed = 0;
                    for (String statement : splitStatements(sql)) {
                        String trimmed = statement.trim();
                        if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                            continue;
                        }
                        stmt.execute(trimmed);
                        executed++;
                    }
                    System.out.println("Executed " + executed + " statements from " + sqlFile);
                }
                conn.commit();

                printCount(stmt, "Active Paddy products", "SELECT COUNT(*) FROM products WHERE is_active = 1 AND image LIKE 'products/paddy_%'");
                printCount(stmt, "Active non-Paddy products", "SELECT COUNT(*) FROM products WHERE is_active = 1 AND (image IS NULL OR image NOT LIKE 'products/paddy_%')");
                printCount(stmt, "All active products", "SELECT COUNT(*) FROM products WHERE is_active = 1");
                printCount(stmt, "Imported descriptions over 260 chars", "SELECT COUNT(*) FROM products WHERE image LIKE 'products/paddy_%' AND CHAR_LENGTH(description) > 260");
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private static Properties loadDbProperties() throws IOException {
        Properties properties = new Properties();
        try (var input = Files.newInputStream(Path.of("src/main/resources/db.properties"))) {
            properties.load(input);
        }
        return properties;
    }

    private static String resolvePassword(String filePassword) {
        String normalized = filePassword == null ? "" : filePassword.trim();
        if (!normalized.isEmpty() && !"YOUR_PASSWORD_HERE".equals(normalized)) {
            return normalized;
        }
        String envPassword = System.getenv("PETSHOP_DB_PASSWORD");
        if (envPassword == null || envPassword.isBlank()) {
            envPassword = System.getenv("MYSQL_PASSWORD");
        }
        return envPassword == null ? "" : envPassword.trim();
    }

    private static List<String> splitStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean escaped = false;

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            current.append(c);

            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
                continue;
            }
            if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                continue;
            }
            if (c == ';' && !inSingleQuote && !inDoubleQuote) {
                statements.add(current.toString());
                current.setLength(0);
            }
        }

        if (!current.toString().trim().isEmpty()) {
            statements.add(current.toString());
        }
        return statements;
    }

    private static String stripLineComments(String sql) {
        StringBuilder cleaned = new StringBuilder();
        for (String line : sql.split("\\R")) {
            String trimmed = line.trim();
            if (!trimmed.startsWith("--")) {
                cleaned.append(line).append('\n');
            }
        }
        return cleaned.toString();
    }

    private static void printCount(Statement stmt, String label, String sql) throws Exception {
        try (ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                System.out.println(label + ": " + rs.getInt(1));
            }
        }
    }
}
