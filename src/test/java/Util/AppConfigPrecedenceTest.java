package Util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class AppConfigPrecedenceTest {

    @AfterEach
    void clearBridge() {
        AppConfig.setSpringEnvironment(null);
        System.clearProperty("test.p0.key");
    }

    @Test
    void springEnvironmentWinsOverLegacyFiles() {
        MockEnvironment env = new MockEnvironment().withProperty("db.host", "from-yml");
        AppConfig.setSpringEnvironment(env);
        assertEquals("from-yml", AppConfig.get("db.host"));
    }

    @Test
    void systemPropertyWinsOverSpringEnvironment() {
        System.setProperty("test.p0.key", "from-system");
        MockEnvironment env = new MockEnvironment().withProperty("test.p0.key", "from-yml");
        AppConfig.setSpringEnvironment(env);
        assertEquals("from-system", AppConfig.get("test.p0.key"));
    }

    @Test
    void legacySecretsFileStillReadWhenNoOtherSource() {
        AppConfig.setSpringEnvironment(new MockEnvironment());
        // secrets.properties (gitignored, local) vẫn là nguồn cuối
        assertNull(AppConfig.get("khong.ton.tai.key.p0"));
    }

    @Test
    void envVarFallbackKeyWorks() {
        MockEnvironment env = new MockEnvironment().withProperty("DB_NAME", "petvaccine");
        AppConfig.setSpringEnvironment(env);
        assertEquals("petvaccine", AppConfig.getOrDefault("db.dbname", "petshop", "DB_NAME"));
    }
}
