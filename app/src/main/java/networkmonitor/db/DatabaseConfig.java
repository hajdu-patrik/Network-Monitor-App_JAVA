package networkmonitor.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Resolves the database connection overrides that {@link DatabaseManager} hands to
 * {@code Persistence.createEntityManagerFactory(...)}.
 *
 * <p>{@code persistence.xml} only keeps the non-secret defaults (JDBC driver and the
 * localhost connection URL); the login and password are never committed to source
 * control. Each setting is resolved with the following precedence:
 * <ol>
 *   <li>Environment variable ({@value #ENV_URL}, {@value #ENV_USER}, {@value #ENV_PASSWORD})</li>
 *   <li>The local file {@value #LOCAL_PROPERTIES_FILE} in the working directory
 *       (keys {@value #PROP_URL}, {@value #PROP_USER}, {@value #PROP_PASSWORD}) -
 *       see {@code app/db.local.properties.example}</li>
 *   <li>The defaults already declared in {@code persistence.xml} (driver and URL only;
 *       there is no default user or password)</li>
 * </ol>
 *
 * <p>The resolver itself ({@link #resolveOverrides(Map, Properties)}) is a pure function:
 * it takes the environment and the local properties as plain inputs and returns the
 * override map, so it can be unit-tested without touching a file, a database or the network.
 */
public final class DatabaseConfig {

    /** Name of the local, gitignored properties file consulted after environment variables. */
    public static final String LOCAL_PROPERTIES_FILE = "db.local.properties";

    private static final String ENV_URL = "NETMON_DB_URL";
    private static final String ENV_USER = "NETMON_DB_USER";
    private static final String ENV_PASSWORD = "NETMON_DB_PASSWORD";

    private static final String PROP_URL = "db.url";
    private static final String PROP_USER = "db.user";
    private static final String PROP_PASSWORD = "db.password";

    /** JPA property key for the JDBC connection URL. */
    public static final String JPA_URL = "jakarta.persistence.jdbc.url";
    /** JPA property key for the database user. */
    public static final String JPA_USER = "jakarta.persistence.jdbc.user";
    /** JPA property key for the database password. */
    public static final String JPA_PASSWORD = "jakarta.persistence.jdbc.password";

    private DatabaseConfig() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Loads {@value #LOCAL_PROPERTIES_FILE} from the current working directory, if present.
     * @return the parsed properties, or empty properties when the file does not exist
     */
    public static Properties loadLocalProperties() {
        Properties properties = new Properties();
        Path path = Path.of(LOCAL_PROPERTIES_FILE);
        if (Files.isRegularFile(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                properties.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read " + LOCAL_PROPERTIES_FILE, e);
            }
        }
        return properties;
    }

    /**
     * Resolves the JPA override properties, applying the precedence documented on this class.
     *
     * @param env       environment variables to consult (normally {@code System.getenv()})
     * @param localFile parsed contents of {@value #LOCAL_PROPERTIES_FILE}; pass an empty
     *                  {@link Properties} when the file does not exist
     * @return the override map to hand to {@code Persistence.createEntityManagerFactory}
     * @throws MissingCredentialsException if a database user or password cannot be resolved
     *                                      from either the environment or the local file
     */
    public static Map<String, String> resolveOverrides(Map<String, String> env, Properties localFile) {
        Properties local = localFile != null ? localFile : new Properties();

        String url = firstNonBlank(env.get(ENV_URL), local.getProperty(PROP_URL));
        String user = firstNonBlank(env.get(ENV_USER), local.getProperty(PROP_USER));
        String password = firstNonBlank(env.get(ENV_PASSWORD), local.getProperty(PROP_PASSWORD));

        if (isBlank(user) || isBlank(password)) {
            throw new MissingCredentialsException(
                "Database credentials are not configured. Set the " + ENV_USER + " and " + ENV_PASSWORD +
                " environment variables, or create a '" + LOCAL_PROPERTIES_FILE + "' file in the working " +
                "directory (copy app/" + LOCAL_PROPERTIES_FILE + ".example) defining '" + PROP_USER +
                "' and '" + PROP_PASSWORD + "'.");
        }

        Map<String, String> overrides = new LinkedHashMap<>();
        if (!isBlank(url)) {
            overrides.put(JPA_URL, url);
        }
        overrides.put(JPA_USER, user);
        overrides.put(JPA_PASSWORD, password);
        return overrides;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (!isBlank(primary)) {
            return primary;
        }
        return isBlank(fallback) ? null : fallback;
    }
}