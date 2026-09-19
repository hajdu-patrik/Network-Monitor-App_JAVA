package networkmonitor.db;

/**
 * Thrown when {@link DatabaseConfig} cannot resolve a database user or password
 * from either the environment or the local properties file.
 * The message only ever names the missing setting; it never contains a credential value.
 */
public class MissingCredentialsException extends RuntimeException {

    public MissingCredentialsException(String message) {
        super(message);
    }
}