package com.logtechub.automation.api;

import com.logtechub.automation.config.ConfigManager;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class BaseApiClient {
    protected final ConfigManager config = ConfigManager.getInstance();

    protected RequestSpecification request() {
        return RestAssured.given()
                .baseUri(config.apiBaseUri())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .filter(new AllureRestAssured());
    }

    public Response get(String path) {
        return request().when().get(path);
    }

    public Response post(String path, Object body) {
        return request().body(body).when().post(path);
    }

    public Response put(String path, Object body) {
        return request().body(body).when().put(path);
    }

    public Response patch(String path, Object body) {
        return request().body(body).when().patch(path);
    }

    public Response delete(String path) {
        return request().when().delete(path);
    }
}
