package restAPI;

import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;

public class RestAPIRequests extends SpecificationData {

    public static ValidatableResponse sendPostRequest(String token, String action) {
        return given()
                .spec(getBaseReqSpec())
                .formParam("token", token)
                .formParam("action", action)
                .when()
                .post("endpoint")
                .then();
    }
}
