package org.tkit.onecx.product.store.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.*;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.product.store.bff.rs.controllers.SlotsRestController;

import gen.org.tkit.onecx.product.store.bff.rs.internal.model.*;
import gen.org.tkit.onecx.product.store.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@TestHTTPEndpoint(SlotsRestController.class)
class SlotsRestControllerTest extends AbstractTest {

    private static final String PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH = "/internal/slots";

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    @InjectMockServerClient
    MockServerClient mockServerClient;

    static final String MOCK_ID = "MOCK";

    @BeforeEach
    void resetExpectation() {
        try {
            mockServerClient.clear(MOCK_ID);
        } catch (Exception ex) {
            //  mockId not existing
        }
    }

    @Test
    void getSlot_shouldReturnSlot() {
        Slot data = new Slot().appId("app1").name("slot1").id("id");

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/" + data.getId())
                        .withMethod(HttpMethod.GET))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .get(data.getId())
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(SlotDTO.class);

        Assertions.assertEquals(response.getAppId(), data.getAppId());
        Assertions.assertEquals(response.getName(), data.getName());
        Assertions.assertEquals(response.getId(), data.getId());
    }

    /**
     * Scenario: Receive 404 Not Found when slot is not existing in backend service.
     * Given
     * When I query GET endpoint with a non-existing id
     * Then I get a 'Not Found' response code back
     * AND problem details are within the response body
     */
    @Test
    void getSlot_shouldReturnNotFound_whenSlotIdDoesNotExist() {

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode(String.valueOf(Response.Status.NOT_FOUND.getStatusCode()));

        String id = "notExisting";
        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/" + id)
                        .withMethod(HttpMethod.GET))
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode())
                        .withBody(JsonBody.json(problemDetailResponse)));

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .get(id)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

    }

    /**
     * Scenario: Create new slot in case product name and app-id path are unique (not yet existing).
     * Given
     * When I try to create a product with a non-existing (in backend) product name and app-id
     * Then I get a 'OK' response code back
     * AND created mfe is returned
     */
    @Test
    void createSlot_Test() {

        CreateSlotRequest request = new CreateSlotRequest();
        request.appId("app1").name("slot1").productName("p1");
        Slot data = new Slot();
        data.appId("app1").name("slot1").productName("p1");

        // create mock rest endpoint
        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH).withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        CreateSlotRequestDTO requestDTO = new CreateSlotRequestDTO();
        requestDTO.appId("app1").name("slot1").productName("p1");
        SlotDTO dto = new SlotDTO();
        dto.appId("app1").name("slot1").productName("p1");

        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(SlotDTO.class);

        // standard USER get FORBIDDEN with only READ permission
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(USER))
                .header(APM_HEADER_PARAM, USER)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        Assertions.assertEquals(data.getId(), response.getId());
        Assertions.assertEquals(data.getName(), response.getName());
        Assertions.assertEquals(data.getAppId(), response.getAppId());

    }

    /**
     * Scenario: Throw 400 Bad Request exception when provided product name, name and appId are already used for another
     * slot.
     * Given
     * When I try to create a mfe with used (in backend) app id and product name
     * Then I get a 'Bad Request' response code back
     * AND problem details are within the response body
     */
    @Test
    void createSlot_shouldReturnBadRequest_whenSlotIsNotUnique() {

        ProblemDetailResponse data = new ProblemDetailResponse();

        CreateSlotRequest request = new CreateSlotRequest();
        request.appId("app1").name("slot1").productName("p1");

        // create mock rest endpoint
        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH).withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        CreateSlotRequestDTO requestDTO = new CreateSlotRequestDTO();
        requestDTO.appId("app1").name("slot1").productName("p1");
        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(response);
    }

    /**
     * Scenario: Throw 400 Bad Request when mandatory field(s) is/are missing.
     * Given
     * When I try to create a slot without setting all mandatory fields
     * Then I get a 'Bad Request' response code back
     */
    @Test
    void createSlot_shouldReturnBadRequest_whenRunningIntoValidationConstraints() {

        CreateSlotRequestDTO requestDTO = new CreateSlotRequestDTO();
        requestDTO.setName("slot1");

        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(response);
    }

    /**
     * Scenario: Search request for non-matching criteria results into successful response with empty slot list.
     * Given
     * When I search with criteria for which no slot(s) exist(s) matching the search criteria
     * Then I get a 'OK' response code back
     * AND empty slot list is returned within
     */
    @Test
    void searchSlots_shouldReturnEmptyList_whenSearchCriteriaDoesNotMatch() {

        SlotSearchCriteria request = new SlotSearchCriteria();
        request.setProductName("somethingNotMatching");
        request.setAppId(null);
        request.setPageNumber(0);
        request.setPageSize(10);

        SlotPageResult data = new SlotPageResult();
        data.setNumber(0);
        data.setSize(1);
        data.setTotalElements(0L);
        data.setTotalPages(0L);
        List<SlotPageItem> list = new ArrayList<>();
        data.setStream(list);

        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/search").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(request)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        SlotSearchCriteriaDTO requestDTO = new SlotSearchCriteriaDTO();
        requestDTO.setProductName("somethingNotMatching");
        requestDTO.setAppId(null);
        requestDTO.setPageNumber(0);
        requestDTO.setPageSize(10);

        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post("/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(SlotPageResultDTO.class);

        Assertions.assertEquals(1, response.getSize());
        Assertions.assertEquals(data.getTotalElements(), response.getTotalElements());
        Assertions.assertEquals(data.getTotalPages(), response.getTotalPages());
        Assertions.assertEquals(data.getNumber(), response.getNumber());
        Assertions.assertNotNull(response.getStream());
        Assertions.assertEquals(0, response.getStream().size());
    }

    /**
     * Scenario: Search request with matching criteria results into response with a single slot in list matching
     * the
     * criteria, according to the uniqueness (constraints).
     * Given
     * When I search with criteria for which at least a slot exists matching the search criteria
     * Then I get a 'OK' response code back
     * AND corresponding slot is returned within the list
     */
    @Test
    void searchSlots_shouldReturnSlotListWithSingleElement_whenSearchCriteriaDoesMatch() {

        int pageSizeRequest = 10;

        SlotSearchCriteria request = new SlotSearchCriteria();
        request.setProductName("productA");
        request.setAppId("7a0ee705-8fd0-47b0");
        request.setPageNumber(0);
        request.setPageSize(pageSizeRequest);

        List<SlotPageItem> stream = new ArrayList<>();

        SlotPageItem slot = new SlotPageItem();
        slot.appId(request.getAppId()).productName(request.getProductName());

        SlotPageResult data = new SlotPageResult();
        data.setNumber(0);
        data.setSize(pageSizeRequest);
        stream.add(slot);
        data.setTotalElements((long) stream.size());
        data.setTotalPages(1L);
        data.setStream(stream);

        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/search").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(request)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        SlotSearchCriteriaDTO requestDTO = new SlotSearchCriteriaDTO();
        requestDTO.setProductName("productA");
        requestDTO.setAppId("7a0ee705-8fd0-47b0");
        requestDTO.setPageNumber(0);
        requestDTO.setPageSize(pageSizeRequest);

        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post("/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(SlotPageResultDTO.class);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(slot.getAppId(), response.getStream().get(0).getAppId());
        Assertions.assertEquals(slot.getProductName(), response.getStream().get(0).getProductName());
    }

    /**
     * Scenario: Delete slot by existing slot id.
     * Given
     * When I try to delete a slot by existing slot id
     * Then I get a 'No Content' response code back
     */
    @Test
    void deleteSlot_shouldDeleteSlot() {

        String id = "82789c64-9473-5555-b30a-8749d784b287";

        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/" + id).withMethod(HttpMethod.DELETE))
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode()));

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", id)
                .delete("/{id}")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    /**
     * Scenario: Update existing slot successfully.
     * Given
     * When I try to update a slot by existing slot id
     * Then I get a 'No Content' response code back
     */
    @Test
    void updateSlot_shouldUpdateSlot() {

        String id = "7a0ee705-8fd0-47b0-8205-b2a5f6540b9e";
        UpdateSlotRequest request = new UpdateSlotRequest();
        request.setName("newSlotName");
        request.setAppId("newAppId");
        request.setProductName("p1");

        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/" + id).withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(request)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode()));

        UpdateSlotRequestDTO requestDTO = new UpdateSlotRequestDTO();
        requestDTO.setName("newSlotName");
        requestDTO.setAppId("newAppId");
        requestDTO.setProductName("p1");

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", id)
                .body(requestDTO)
                .put("/{id}")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

    }

    /**
     * Scenario: Return 404-status code when trying to update slot by a non-existing slot id.
     * Given mfe-ID is not existing
     * When I try to update a slot by a non-existing slot id "nonExisting"
     * Then I get a 'Not Found' response code back
     */
    @Test
    void updateSlot_shouldReturnNotFound_whenSlotIdDoesNotExist() {

        String id = "notExisting";
        UpdateSlotRequest request = new UpdateSlotRequest();
        request.setName("newName");
        request.setAppId("newAppId");
        request.setProductName("p1");

        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/" + id).withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(request)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode()));

        UpdateSlotRequestDTO requestDTO = new UpdateSlotRequestDTO();
        requestDTO.setAppId("newAppId");
        requestDTO.setName("newName");
        requestDTO.setProductName("p1");

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .put("/{id}", id)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

}
