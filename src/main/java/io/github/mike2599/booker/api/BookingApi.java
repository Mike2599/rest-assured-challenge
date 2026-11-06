package io.github.mike2599.booker.api;

import static io.restassured.RestAssured.given;

import io.github.mike2599.booker.models.Booking;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Map;

/** API Object de /booking: una operación por endpoint, sin aserciones. */
public class BookingApi {

    private final RequestSpecification spec;

    public BookingApi(RequestSpecification spec) {
        this.spec = spec;
    }

    public Response create(Booking booking) {
        return given().spec(spec).log().ifValidationFails()
                .body(booking)
                .when().post(Endpoints.BOOKINGS);
    }

    public Response get(int id) {
        return given().spec(spec).log().ifValidationFails()
                .pathParam("id", id)
                .when().get(Endpoints.BOOKING_BY_ID);
    }

    public Response list(Map<String, ?> filters) {
        return given().spec(spec).log().ifValidationFails()
                .queryParams(filters)
                .when().get(Endpoints.BOOKINGS);
    }

    public Response update(int id, Booking booking) {
        return given().spec(spec).log().ifValidationFails()
                .pathParam("id", id)
                .body(booking)
                .when().put(Endpoints.BOOKING_BY_ID);
    }

    public Response delete(int id) {
        return given().spec(spec).log().ifValidationFails()
                .pathParam("id", id)
                .when().delete(Endpoints.BOOKING_BY_ID);
    }
}
