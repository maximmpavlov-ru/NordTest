package restAPI;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public class SpecificationData {
    protected static RequestSpecification getBaseReqSpec() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.URLENC)
                .addHeader("X-Api-Key", "qazWSXedc")
                .build();
    }
}
