package io.github.mike2599.booker.assertions;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.ResponseSpecification;

/**
 * Especificaciones de respuesta reutilizables (DRY): cada una describe una respuesta esperada
 * y se aplica con {@code then().spec(...)}. Así las pruebas no repiten códigos ni headers.
 */
public final class ResponseExpectations {

    public static final String JSON_UTF8 = "application/json; charset=utf-8";

    private ResponseExpectations() {
    }

    /** 200 con cuerpo JSON. */
    public static ResponseSpecification okJson() {
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType(ContentType.JSON)
                .build();
    }

    /** DELETE exitoso: Restful-Booker responde 201 Created. */
    public static ResponseSpecification deleted() {
        return new ResponseSpecBuilder()
                .expectStatusCode(201)
                .build();
    }

    public static ResponseSpecification forbidden() {
        return new ResponseSpecBuilder()
                .expectStatusCode(403)
                .expectBody(equalTo("Forbidden"))
                .build();
    }

    public static ResponseSpecification notFound() {
        return new ResponseSpecBuilder()
                .expectStatusCode(404)
                .expectBody(equalTo("Not Found"))
                .build();
    }

    /** Tipo de contenido exacto y presencia del header del servidor. */
    public static ResponseSpecification jsonHeaders() {
        return new ResponseSpecBuilder()
                .expectHeader("Content-Type", JSON_UTF8)
                .expectHeader("Server", not(emptyOrNullString()))
                .build();
    }
}
