package io.github.mike2599.booker.support;

import io.github.mike2599.booker.config.Environment;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Se registra sola (autodetección de extensiones de JUnit 5) y, una vez por ejecución, deja en los resultados
 * de Allure el ambiente usado y las categorías de fallo propias.
 */
public class AllureEnvironmentExtension implements BeforeAllCallback {

    private static final AtomicBoolean WRITTEN = new AtomicBoolean();

    @Override
    public void beforeAll(ExtensionContext context) {
        if (!WRITTEN.compareAndSet(false, true)) {
            return;
        }
        Environment environment = Environment.current();
        Path results = Path.of(System.getProperty("allure.results.directory", "target/allure-results"));
        try {
            Files.createDirectories(results);
            Files.writeString(results.resolve("environment.properties"), String.join(System.lineSeparator(),
                    "Ambiente=" + environment.name(),
                    "URL.base=" + environment.baseUrl(),
                    "SLA.ms=" + environment.slaResponseTimeMs(),
                    "Java=" + System.getProperty("java.version"),
                    "Hilos.paralelos=" + context.getConfigurationParameter("junit.jupiter.execution.parallel.config.fixed.parallelism").orElse("-"),
                    "SO=" + System.getProperty("os.name")));
            try (InputStream categories = getClass().getResourceAsStream("/allure/categories.json")) {
                if (categories != null) {
                    Files.copy(categories, results.resolve("categories.json"), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
