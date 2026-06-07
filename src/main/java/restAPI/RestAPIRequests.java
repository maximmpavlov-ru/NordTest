package restAPI;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;

public class RestAPIRequests extends SpecificationData {

    @Step ("Отправка POST запроса по ручке /endpoint")
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
