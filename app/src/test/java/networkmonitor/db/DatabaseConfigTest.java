package networkmonitor.db;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the pure {@link DatabaseConfig#resolveOverrides(Map, Properties)} resolver.
 * None of these need a database, a file or the network: the environment map and the local
 * properties are constructed in-memory for each case.
 */
class DatabaseConfigTest {

    private static Map<String, String> env(String... keyValuePairs) {
        Map<String, String> env = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            env.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return env;
    }

    private static Properties props(String... keyValuePairs) {
        Properties properties = new Properties();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            properties.setProperty(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return properties;
    }

    @Test
    void environmentVariablesTakePrecedenceOverTheLocalFile() {
        Map<String, String> env = env(
                "NETMON_DB_USER", "env_user",
                "NETMON_DB_PASSWORD", "env_password",
                "NETMON_DB_URL", "jdbc:sqlserver://env-host:1433;databaseName=env;");
        Properties local = props(
                "db.user", "file_user",
                "db.password", "file_password",
                "db.url", "jdbc:sqlserver://file-host:1433;databaseName=file;");

        Map<String, String> overrides = DatabaseConfig.resolveOverrides(env, local);

        assertEquals("env_user", overrides.get(DatabaseConfig.JPA_USER));
        assertEquals("env_password", overrides.get(DatabaseConfig.JPA_PASSWORD));
        assertEquals("jdbc:sqlserver://env-host:1433;databaseName=env;", overrides.get(DatabaseConfig.JPA_URL));
    }

    @Test
    void localFileIsUsedWhenTheEnvironmentDoesNotProvideAValue() {
        Map<String, String> env = env();
        Properties local = props(
                "db.user", "file_user",
                "db.password", "file_password",
                "db.url", "jdbc:sqlserver://file-host:1433;databaseName=file;");

        Map<String, String> overrides = DatabaseConfig.resolveOverrides(env, local);

        assertEquals("file_user", overrides.get(DatabaseConfig.JPA_USER));
        assertEquals("file_password", overrides.get(DatabaseConfig.JPA_PASSWORD));
        assertEquals("jdbc:sqlserver://file-host:1433;databaseName=file;", overrides.get(DatabaseConfig.JPA_URL));
    }

    @Test
    void urlFallsBackToThePersistenceXmlDefaultWhenNeitherSourceProvidesOne() {
        Map<String, String> env = env("NETMON_DB_USER", "env_user", "NETMON_DB_PASSWORD", "env_password");
        Properties local = props("db.user", "file_user", "db.password", "file_password");

        Map<String, String> overrides = DatabaseConfig.resolveOverrides(env, local);

        assertFalse(overrides.containsKey(DatabaseConfig.JPA_URL),
                "no URL override was supplied by either source, so persistence.xml's own default must be left alone");
    }

    @Test
    void missingPasswordThrowsMissingCredentialsExceptionWithAnActionableMessage() {
        Map<String, String> env = env();
        Properties local = props("db.user", "file_user");

        MissingCredentialsException ex = assertThrows(MissingCredentialsException.class,
                () -> DatabaseConfig.resolveOverrides(env, local));

        assertTrue(ex.getMessage().contains("NETMON_DB_PASSWORD"));
        assertTrue(ex.getMessage().contains("db.local.properties"));
    }

    @Test
    void missingUserThrowsMissingCredentialsException() {
        Map<String, String> env = env();
        Properties local = props("db.password", "file_password");

        assertThrows(MissingCredentialsException.class, () -> DatabaseConfig.resolveOverrides(env, local));
    }

    @Test
    void exceptionMessageNeverContainsAResolvedCredentialValue() {
        Map<String, String> env = env("NETMON_DB_USER", "someone"); // password left unset on purpose
        Properties local = props();

        MissingCredentialsException ex = assertThrows(MissingCredentialsException.class,
                () -> DatabaseConfig.resolveOverrides(env, local));

        assertFalse(ex.getMessage().contains("someone"), "the message must not leak a resolved value either");
    }
}