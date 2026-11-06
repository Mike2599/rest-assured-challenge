package io.github.mike2599.booker.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Ambiente de ejecución elegido con {@code -Denv=<nombre>} (por defecto {@code qa}).
 * Lee {@code application.conf}, donde cada ambiente define su URL base, credenciales, SLA y tiempos de espera.
 */
public record Environment(
        String name,
        String baseUrl,
        String username,
        String password,
        long slaResponseTimeMs,
        int connectTimeoutMs,
        int readTimeoutMs,
        int nonexistentBookingId) {

    public static final String ENV_PROPERTY = "env";
    public static final String DEFAULT_ENV = "qa";

    private static final Environment CURRENT = load(System.getProperty(ENV_PROPERTY, DEFAULT_ENV));

    /** Ambiente activo para toda la ejecución. */
    public static Environment current() {
        return CURRENT;
    }

    static Environment load(String name) {
        Config root = ConfigFactory.load();
        String path = "environments." + name;
        if (!root.hasPath(path)) {
            throw new IllegalArgumentException("El ambiente '%s' no existe en application.conf. Disponibles: %s"
                    .formatted(name, root.getObject("environments").keySet()));
        }
        Config env = root.getConfig(path);
        return new Environment(
                name,
                env.getString("base-url"),
                env.getString("credentials.username"),
                env.getString("credentials.password"),
                env.getLong("sla.response-time-ms"),
                env.getInt("http.connect-timeout-ms"),
                env.getInt("http.read-timeout-ms"),
                env.getInt("test-data.nonexistent-booking-id"));
    }

    /** Las credenciales no se imprimen en logs ni reportes. */
    @Override
    public String toString() {
        return "Environment[name=%s, baseUrl=%s, username=%s, password=****]".formatted(name, baseUrl, username);
    }
}
