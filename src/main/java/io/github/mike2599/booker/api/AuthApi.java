package io.github.mike2599.booker.api;

import static io.restassured.RestAssured.given;

import io.github.mike2599.booker.models.AuthRequest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/** API Object de /auth: solo sabe cómo llamar al endpoint, no qué esperar de él. */
public class AuthApi {

    private final RequestSpecification spec;

    public AuthApi(RequestSpecification spec) {
        this.spec = spec;
    }

    public Response createToken(AuthRequest credentials) {
        return given().spec(spec).log().ifValidationFails()
                .body(credentials)
                .when().post(Endpoints.AUTH);
    }
}
