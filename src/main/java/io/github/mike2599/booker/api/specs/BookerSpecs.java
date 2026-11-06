package io.github.mike2599.booker.api.specs;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.mike2599.booker.config.Environment;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.LogConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.Filter;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.List;

/**
 * Especificación base de todas las peticiones (DRY): URL del ambiente, JSON de ida y vuelta,
 * mapeo con Jackson, tiempos de espera, logs solo cuando una validación falla y adjuntos en Allure.
 * No usa la configuración estática de RestAssured, así cada actor tiene su propia especificación
 * y las pruebas pueden correr en paralelo sin compartir estado.
 */
public final class BookerSpecs {

    /** Headers que nunca se imprimen en los logs ni en el reporte (llevan el token de sesión). */
    private static final String[] SENSITIVE_HEADERS = {"Cookie", "Set-Cookie", "Authorization"};

    private BookerSpecs() {
    }

    public static RequestSpecification request(Environment environment, Filter... filters) {
        return new RequestSpecBuilder()
                .setBaseUri(environment.baseUrl())
                .setContentType(ContentType.JSON)
                // Restful-Booker responde 418 si Accept no es exactamente application/json
                // (ContentType.JSON enviaría una lista de cuatro tipos).
                .setAccept("application/json")
                .setConfig(config(environment))
                .addFilters(List.of(filters))
                .addFilter(new AllureRestAssured())
                .build();
    }

    private static RestAssuredConfig config(Environment environment) {
        return RestAssuredConfig.config()
                .objectMapperConfig(ObjectMapperConfig.objectMapperConfig()
                        .jackson2ObjectMapperFactory((type, charset) -> jsonMapper()))
                .logConfig(LogConfig.logConfig()
                        .enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)
                        .enablePrettyPrinting(true)
                        .blacklistHeaders(List.of(SENSITIVE_HEADERS)))
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", environment.connectTimeoutMs())
                        .setParam("http.socket.timeout", environment.readTimeoutMs()));
    }

    /** Fechas como "yyyy-MM-dd" (no como arreglos ni timestamps). */
    public static JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }
}
