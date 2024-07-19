package org.tkit.onecx.product.store.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.time.OffsetDateTime;
import java.util.*;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.product.store.bff.rs.controllers.MicroservicesRestController;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.product.store.bff.rs.internal.model.*;
import gen.org.tkit.onecx.product.store.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(MicroservicesRestController.class)
class MicroservicesRestControllerTest extends AbstractTest {

    private static final String PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH = "/internal/microservices";

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

    /**
     * Scenario: Receives microservice (ms) by ms-id successfully.
     * Given
     * When I query GET endpoint with an existing id
     * Then I get a 'OK' response code back
     * AND associated ms is returned
     */
    @Test
    void getMicroservice_shouldReturnMicroservice() {

        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2023-11-30T13:53:03.688710200+01:00");
        OffsetDateTimeMapper mapper = new OffsetDateTimeMapper();

        Microservice data = createMicroservice("7a0ee705-8fd0-47b0-8205-b2a5f6540b9e", offsetDateTime, "csommer",
                offsetDateTime, "csommer", 1, "App-ID", "1.0.0",
                "AppName", "some description", "ProductName");

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
                .extract().as(MicroserviceDTO.class);

        Assertions.assertEquals(data.getId(), response.getId());
        Assertions.assertEquals(data.getCreationUser(), response.getCreationUser());
        Assertions.assertEquals(mapper.map(data.getCreationDate()), mapper.map(response.getCreationDate()));
        Assertions.assertEquals(mapper.map(data.getModificationDate()), mapper.map(response.getModificationDate()));
        Assertions.assertEquals(data.getModificationUser(), response.getModificationUser());
        Assertions.assertEquals(data.getModificationCount(), response.getModificationCount());
        Assertions.assertEquals(data.getAppId(), response.getAppId());
        Assertions.assertEquals(data.getVersion(), response.getAppVersion());
        Assertions.assertEquals(data.getName(), response.getAppName());
        Assertions.assertEquals(data.getDescription(), response.getDescription());
        Assertions.assertEquals(data.getProductName(), response.getProductName());

    }

    /**
     * Scenario: Receive 404 Not Found when microservice (ms) is not existing in backend service.
     * Given
     * When I query GET endpoint with a non-existing id
     * Then I get a 'Not Found' response code back
     * AND problem details are within the response body
     */
    @Test
    void getMicroservice_shouldReturnNotFound_whenMSIdDoesNotExist() {

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
     * Scenario: Create new microservice (ms) in case product name and app-id path are unique (not yet existing).
     * Given
     * When I try to create a product with a non-existing (in backend) product name and app-id
     * Then I get a 'OK' response code back
     * AND created ms is returned
     */
    @Test
    void createMicroservice_shouldAddNewMicroservice_whenProductnameAndAppIdAreUnique() {

        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2023-11-30T13:53:03.688710200+01:00");
        OffsetDateTimeMapper mapper = new OffsetDateTimeMapper();

        Microservice data = createMicroservice("7a0ee705-8fd0-47b0-8205-b2a5f6540b9e", offsetDateTime, "csommer",
                offsetDateTime, "csommer", 1, "App-ID", "1.0.0",
                "AppName", "some description", "ProductName");

        CreateMicroserviceRequest request = createMicroserviceRequestSVC("App-ID", "1.0.0",
                "AppName", "some description", "ProductName");

        // create mock rest endpoint
        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH).withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        CreateMicroserviceRequestDTO requestDTO = createMicroserviceRequestBFF("App-ID", "1.0.0",
                "AppName", "some description", "ProductName");

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
                .extract().as(MicroserviceDTO.class);

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
        Assertions.assertEquals(data.getCreationUser(), response.getCreationUser());
        Assertions.assertEquals(mapper.map(data.getCreationDate()), mapper.map(response.getCreationDate()));
        Assertions.assertEquals(mapper.map(data.getModificationDate()), mapper.map(response.getModificationDate()));
        Assertions.assertEquals(data.getModificationUser(), response.getModificationUser());
        Assertions.assertEquals(data.getModificationCount(), response.getModificationCount());
        Assertions.assertEquals(data.getAppId(), response.getAppId());
        Assertions.assertEquals(data.getVersion(), response.getAppVersion());
        Assertions.assertEquals(data.getName(), response.getAppName());
        Assertions.assertEquals(data.getDescription(), response.getDescription());
        Assertions.assertEquals(data.getProductName(), response.getProductName());
    }

    /**
     * Scenario: Throw 400 Bad Request exception when provided product name and product id are already used for another
     * microservice.
     * Given
     * When I try to create a ms with used (in backend) app id and product name
     * Then I get a 'Bad Request' response code back
     * AND problem details are within the response body
     */
    @Test
    void createMicroservice_shouldReturnBadRequest_whenBasePathIsNotUnique() {

        ProblemDetailResponse data = new ProblemDetailResponse();
        data.setErrorCode("PERSIST_ENTITY_FAILED");
        data.setDetail(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'ps_microservice_app_id'  Detail: "
                        +
                        "Key (product_name, app_id)=(Announcement-Management, ABCee705-8fd0-47b0-8205-b2a5f6540b9e) already exists.]");
        List<ProblemDetailParam> list = new ArrayList<>();
        ProblemDetailParam param1 = new ProblemDetailParam();
        ProblemDetailParam param2 = new ProblemDetailParam();
        param1.setKey("constraint");
        param1.setValue(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'ps_microservice_app_id'  Detail: "
                        +
                        "Key (product_name, app_id)=(Announcement-Management, ABCee705-8fd0-47b0-8205-b2a5f6540b9e) already exists.]");
        param2.setKey("constraintName");
        param2.setValue("ps_microservice_app_id");
        list.add(param1);
        list.add(param2);
        data.setParams(list);
        data.setInvalidParams(null);

        CreateMicroserviceRequest request = createMicroserviceRequestSVC("App-ID", "1.0.0",
                "AppName", "some description", "ProductName");

        // create mock rest endpoint
        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH).withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        CreateMicroserviceRequestDTO requestDTO = createMicroserviceRequestBFF("App-ID", "1.0.0",
                "AppName", "some description", "ProductName");

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
        List<ProblemDetailParamDTO> problemDetailsParams = response.getParams();
        Optional<ProblemDetailParamDTO> paramConstraint = problemDetailsParams.stream()
                .filter(e -> e.getKey().equals("constraint"))
                .findFirst();
        Optional<ProblemDetailParamDTO> paramConstraintName = problemDetailsParams.stream()
                .filter(e -> e.getKey().equals("constraintName"))
                .findFirst();

        Assertions.assertTrue(paramConstraint.isPresent());
        Assertions.assertTrue(paramConstraintName.isPresent());

        Assertions.assertEquals(data.getErrorCode(), response.getErrorCode());
        Assertions.assertEquals(data.getDetail(), response.getDetail());
        Assertions.assertEquals(param1.getValue(), paramConstraint.get().getValue());
        Assertions.assertEquals(param2.getValue(), paramConstraintName.get().getValue());
        Assertions.assertNull(response.getInvalidParams());

    }

    /**
     * Scenario: Throw 400 Bad Request when mandatory field(s) is/are missing.
     * Given
     * When I try to create a ms without setting all mandatory fields
     * Then I get a 'Bad Request' response code back
     * AND problem details are within the response body
     */
    @Test
    void createMicroservice_shouldReturnBadRequest_whenRunningIntoValidationConstraints() {

        ProblemDetailResponse data = new ProblemDetailResponse();
        data.setErrorCode("CONSTRAINT_VIOLATIONS");
        data.setDetail(
                "createMicroservice.createMicroserviceRequestDTO.productName: must not be null");
        List<ProblemDetailInvalidParam> list = new ArrayList<>();
        ProblemDetailInvalidParam param1 = new ProblemDetailInvalidParam();
        param1.setName("createMicroservice.createMicroserviceRequestDTO.productName");
        param1.setMessage("must not be null");
        list.add(param1);

        data.setParams(null);
        data.setInvalidParams(list);

        CreateMicroserviceRequest request = createMicroserviceRequestSVC("App-ID", "1.0.0",
                "AppName", "some description", null);

        // create mock rest endpoint
        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH).withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        CreateMicroserviceRequestDTO requestDTO = createMicroserviceRequestBFF("App-ID", "1.0.0",
                "AppName", "some description", null);

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
        List<ProblemDetailInvalidParamDTO> problemDetailsParams = response.getInvalidParams();
        Optional<ProblemDetailInvalidParamDTO> invalidParamConstraint = problemDetailsParams.stream().findFirst();

        Assertions.assertTrue(invalidParamConstraint.isPresent());

        Assertions.assertEquals(data.getErrorCode(), response.getErrorCode());
        Assertions.assertNotNull(response.getDetail());
        Assertions.assertEquals(param1.getMessage(), invalidParamConstraint.get().getMessage());
        Assertions.assertNotNull(invalidParamConstraint.get().getName());
        Assertions.assertTrue(response.getParams().isEmpty());

    }

    /**
     * Scenario: Search request for non-matching criteria results into successful response with empty ms list.
     * Given
     * When I search with criteria for which no ms(s) exist(s) matching the search criteria
     * Then I get a 'OK' response code back
     * AND empty ms list is returned within
     */
    @Test
    void searchMicroservices_shouldReturnEmptyList_whenSearchCriteriaDoesNotMatch() {

        MicroserviceSearchCriteria request = new MicroserviceSearchCriteria();
        request.setProductName("somethingNotMatching");
        request.setAppId(null);
        request.setName(null);
        request.setPageNumber(0);
        request.setPageSize(10);

        MicroservicePageResult data = new MicroservicePageResult();
        data.setNumber(0);
        data.setSize(1);
        data.setTotalElements(0L);
        data.setTotalPages(0L);
        List<MicroservicePageItem> list = new ArrayList<>();
        data.setStream(list);

        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/search").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(request)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        MfeAndMsSearchCriteriaDTO requestDTO = new MfeAndMsSearchCriteriaDTO();
        requestDTO.setProductName("somethingNotMatching");
        requestDTO.setAppId(null);
        requestDTO.setAppName(null);
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
                .extract().as(MicroservicePageResultDTO.class);

        Assertions.assertEquals(data.getNumber(), response.getNumber());
        Assertions.assertNotNull(response.getStream());
    }

    /**
     * Scenario: Search request with matching criteria results into response with a single microservice (ms) in list matching
     * the
     * criteria, according to the uniqueness (constraints).
     * Given
     * When I search with criteria for which at least a ms exists matching the search criteria
     * Then I get a 'OK' response code back
     * AND corresponding ms is returned within the list
     */
    @Test
    void searchMicroservices_shouldReturnMicroserviceListWithSingleElement_whenSearchCriteriaDoesMatch() {

        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2023-11-30T13:53:03.688710200+01:00");

        int pageSizeRequest = 10;

        MicroserviceSearchCriteria request = new MicroserviceSearchCriteria();
        request.setProductName("productA");
        request.setAppId("7a0ee705-8fd0-47b0");
        request.setName("AppA");
        request.setPageNumber(0);
        request.setPageSize(pageSizeRequest);

        List<MicroservicePageItem> stream = new ArrayList<>();

        MicroservicePageItem ms = createMicroservicePageItem("7a0ee705-8fd0-47b0-8205-b2a5f6540b9e", offsetDateTime,
                "csommer",
                offsetDateTime, "csommer", 1, "App-ID", "1.0.0",
                "AppName", "some description", "ProductName");

        MicroservicePageResult data = new MicroservicePageResult();
        data.setNumber(0);
        data.setSize(pageSizeRequest);
        stream.add(ms);
        data.setTotalElements((long) stream.size());
        data.setTotalPages(1L);
        data.setStream(stream);

        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/search").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(request)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        MfeAndMsSearchCriteriaDTO requestDTO = new MfeAndMsSearchCriteriaDTO();
        requestDTO.setProductName("productA");
        requestDTO.setAppId("7a0ee705-8fd0-47b0");
        requestDTO.setAppName("AppA");
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
                .extract().as(MicroservicePageResultDTO.class);

        Assertions.assertNotNull(response);

        Assertions.assertEquals(ms.getId(), response.getStream().get(0).getId());
        Assertions.assertEquals(ms.getAppId(), response.getStream().get(0).getAppId());
        Assertions.assertEquals(ms.getName(), response.getStream().get(0).getAppName());
        Assertions.assertEquals(ms.getDescription(), response.getStream().get(0).getDescription());
        Assertions.assertEquals(ms.getProductName(), response.getStream().get(0).getProductName());
    }

    /**
     * Scenario: Wildcard search to return all or subset of available microservices (ms).
     * Given ms(s) exist(s)
     * When I search with no criteria for the same (wildcard)
     * Then I get a 'OK' response code back
     * AND a product list is returned within the defined range of product-page-size-numbers
     */
    @Test
    void searchMicroservices_shouldReturnMicroserviceList_whenSearchCriteriaIsNotGivenButMSsAreAvailable() {

        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2023-11-30T13:53:03.688710200+01:00");

        int pageSizeRequest = 10;

        MicroserviceSearchCriteria request = new MicroserviceSearchCriteria();
        request.setProductName(null);
        request.setAppId(null);
        request.setName(null);
        request.setPageNumber(0);
        request.setPageSize(pageSizeRequest);

        List<MicroservicePageItem> stream = new ArrayList<>();

        MicroservicePageItem ms = createMicroservicePageItem("7a0ee705-8fd0-47b0-8205-b2a5f6540b9e", offsetDateTime,
                "csommer",
                offsetDateTime, "csommer", 1, "App-ID", "1.0.0",
                "AppName", "some description", "ProductName");
        MicroservicePageItem ms2 = createMicroservicePageItem("888ee705-8fd0-47b0-8205-b2a5f6540888", offsetDateTime, "cso",
                offsetDateTime, "cso", 1, "App-IDv2", "2.0.0", "AppNamev2",
                "some description2", "ProductName");

        MicroservicePageResult data = new MicroservicePageResult();
        data.setNumber(0);
        data.setSize(pageSizeRequest);
        stream.add(ms);
        stream.add(ms2);
        data.setTotalElements((long) stream.size());
        data.setTotalPages(1L);
        data.setStream(stream);

        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/search").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(request)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        MfeAndMsSearchCriteriaDTO requestDTO = new MfeAndMsSearchCriteriaDTO();
        requestDTO.setProductName(null);
        requestDTO.setAppId(null);
        requestDTO.setAppName(null);
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
                .extract().as(MicroservicePageResultDTO.class);

        Assertions.assertNotNull(response);

        List<MicroserviceDTO> msAbstractsPageResults = response.getStream();
        Optional<MicroserviceDTO> responseProductAbstractItem1 = msAbstractsPageResults.stream()
                .filter(e -> e.getAppName().equals(ms.getName()))
                .findFirst();
        Optional<MicroserviceDTO> responseProductAbstractItem2 = msAbstractsPageResults.stream()
                .filter(e -> e.getAppName().equals(ms2.getName()))
                .findFirst();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(data.getStream().size(), msAbstractsPageResults.size());
        Assertions.assertTrue(responseProductAbstractItem1.isPresent());
        Assertions.assertTrue(responseProductAbstractItem2.isPresent());

        // first ms
        Assertions.assertEquals(ms.getId(), responseProductAbstractItem1.get().getId());
        Assertions.assertEquals(ms.getAppId(), responseProductAbstractItem1.get().getAppId());
        Assertions.assertEquals(ms.getName(), responseProductAbstractItem1.get().getAppName());
        Assertions.assertEquals(ms.getDescription(), responseProductAbstractItem1.get().getDescription());
        Assertions.assertEquals(ms.getProductName(), responseProductAbstractItem1.get().getProductName());

        // second ms
        Assertions.assertEquals(ms2.getId(), responseProductAbstractItem2.get().getId());
        Assertions.assertEquals(ms2.getAppId(), responseProductAbstractItem2.get().getAppId());
        Assertions.assertEquals(ms2.getName(), responseProductAbstractItem2.get().getAppName());
        Assertions.assertEquals(ms2.getDescription(), responseProductAbstractItem2.get().getDescription());
        Assertions.assertEquals(ms2.getProductName(), responseProductAbstractItem2.get().getProductName());

        // general structure
        Assertions.assertEquals(pageSizeRequest, response.getSize());
        Assertions.assertEquals(data.getTotalElements(), response.getTotalElements());
        Assertions.assertEquals(data.getTotalPages(), response.getTotalPages());
        Assertions.assertEquals(data.getNumber(), response.getNumber());
    }

    /**
     * Scenario: Delete microservice (ms) by existing ms id.
     * Given
     * When I try to delete a ms by existing ms id
     * Then I get a 'No Content' response code back
     */
    @Test
    void deleteMicroservice_shouldDeleteMicroservice() {

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
     * Scenario: Update existing microservice (ms) successfully.
     * Given
     * When I try to update a ms by existing ms id
     * Then I get a 'No Content' response code back
     */
    @Test
    void updateMicroservice_shouldUpdateMicroservice() {

        String id = "7a0ee705-8fd0-47b0-8205-b2a5f6540b9e";
        UpdateMicroserviceRequest request = updateMicroserviceRequestSVC("App-ID", "1.0.0",
                "AppName", "some description", "ProductName");

        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/" + id).withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(request)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode()));

        UpdateMicroserviceRequestDTO requestDTO = updateMicroserviceRequestBFF("App-ID", "1.0.0",
                "AppName", "some description", "ProductName");

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
     * Scenario: Return 404-status code when trying to update microservice (ms) by a non-existing ms id.
     * Given ms-ID is not existing
     * When I try to update a ms by a non-existing ms id "nonExisting"
     * Then I get a 'Not Found' response code back
     */
    @Test
    void updateMicroservice_shouldReturnNotFound_whenMicroserviceIdDoesNotExist() {

        String id = "notExisting";

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode("404");

        UpdateMicroserviceRequest request = updateMicroserviceRequestSVC("App-ID", "1.0.0",
                "AppName", "some description", "ProductName");

        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/" + id).withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(request)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode())
                        .withBody(JsonBody.json(problemDetailResponse)));

        UpdateMicroserviceRequestDTO requestDTO = updateMicroserviceRequestBFF("App-ID", "1.0.0",
                "AppName", "some description", "ProductName");

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .put("/{id}", id)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);
    }

    /**
     * Scenario: Return 400-status code with exception when trying to update microservice (ms) by an already used app name.
     * Given ms-ID is not existing
     * When I try to update a ms by used value for app id
     * Then I get a 'Bad Request' response code back
     * AND a ProblemDetailResponseObject is returned
     */
    @Test
    void updateMicroservice_shouldReturnNotFound_whenRunningIntoValidationConstraints() {

        String id = "7a0ee705-8fd0-47b0-8205-b2a5f6540b9e";

        ProblemDetailResponse data = new ProblemDetailResponse();
        data.setErrorCode("MERGE_ENTITY_FAILED");
        data.setDetail(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'ps_microservice_app_id'  Detail: "
                        +
                        "Key (product_name, app_id)=(Announcement-Management, ABCee705-8fd0-47b0-8205-b2a5f654888) already exists.]");
        List<ProblemDetailParam> list = new ArrayList<>();
        ProblemDetailParam param1 = new ProblemDetailParam();
        ProblemDetailParam param2 = new ProblemDetailParam();
        param1.setKey("constraint");
        param1.setValue(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'ps_microservice_app_id'  Detail: "
                        +
                        "Key (product_name, app_id)=(Announcement-Management, ABCee705-8fd0-47b0-8205-b2a5f654888) already exists.]");
        param2.setKey("constraintName");
        param2.setValue("ps_microservice_app_id");
        list.add(param1);
        list.add(param2);
        data.setParams(list);
        data.setInvalidParams(null);

        UpdateMicroserviceRequest request = updateMicroserviceRequestSVC("ABCee705-8fd0-47b0-8205-b2a5f654888", "1.0.0",
                "AppName", "some description", "Announcement-Management");

        // create mock rest endpoint
        mockServerClient.when(request()
                .withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/" + id)
                .withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(request)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withBody(JsonBody.json(data))
                        .withContentType(MediaType.APPLICATION_JSON));

        UpdateMicroserviceRequestDTO requestDTO = updateMicroserviceRequestBFF("ABCee705-8fd0-47b0-8205-b2a5f654888", "1.0.0",
                "AppName", "some description", "Announcement-Management");

        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .put("/{id}", id)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(response);
        List<ProblemDetailParamDTO> problemDetailsParams = response.getParams();
        Optional<ProblemDetailParamDTO> paramConstraint = problemDetailsParams.stream()
                .filter(e -> e.getKey().equals("constraint"))
                .findFirst();
        Optional<ProblemDetailParamDTO> paramConstraintName = problemDetailsParams.stream()
                .filter(e -> e.getKey().equals("constraintName"))
                .findFirst();

        Assertions.assertTrue(paramConstraint.isPresent());
        Assertions.assertTrue(paramConstraintName.isPresent());

        Assertions.assertEquals(data.getErrorCode(), response.getErrorCode());
        Assertions.assertEquals(data.getDetail(), response.getDetail());
        Assertions.assertEquals(param1.getValue(), paramConstraint.get().getValue());
        Assertions.assertEquals(param2.getValue(), paramConstraintName.get().getValue());
        Assertions.assertNull(response.getInvalidParams());
    }

    /**
     * Helper method to create a microservice (ms) object
     *
     * @param id unique id of the ms
     * @param creationDateTime datetime of creation
     * @param creationUser user name
     * @param modificationDateTime datetime of modification
     * @param modificationUser user name
     * @param appId unique id of application
     * @param appVersion version number
     * @param appName unique name of application
     * @param description general ms description
     * @param productName name of associated product
     * @return
     */
    private Microservice createMicroservice(String id, OffsetDateTime creationDateTime, String creationUser,
            OffsetDateTime modificationDateTime,
            String modificationUser,
            Integer modificationCount,
            String appId,
            String appVersion,
            String appName,
            String description,
            String productName) {

        Microservice ms = new Microservice();
        ms.setId(id);
        ms.setCreationDate(creationDateTime);
        ms.setCreationUser(creationUser);
        ms.setModificationDate(modificationDateTime);
        ms.setModificationUser(modificationUser);
        ms.setModificationCount(modificationCount);
        ms.setAppId(appId);
        ms.setVersion(appVersion);
        ms.setName(appName);
        ms.setDescription(description);
        ms.setProductName(productName);
        return ms;
    }

    /**
     * Helper method to create a microservice (ms) object
     *
     * @param id unique id of the ms
     * @param creationDateTime datetime of creation
     * @param creationUser user name
     * @param modificationDateTime datetime of modification
     * @param modificationUser user name
     * @param appId unique id of application
     * @param appVersion version number
     * @param appName unique name of application
     * @param description general ms description
     * @param productName name of associated product
     * @return
     */
    private MicroservicePageItem createMicroservicePageItem(String id, OffsetDateTime creationDateTime, String creationUser,
            OffsetDateTime modificationDateTime,
            String modificationUser,
            Integer modificationCount,
            String appId,
            String appVersion,
            String appName,
            String description,
            String productName) {

        MicroservicePageItem ms = new MicroservicePageItem();
        ms.setId(id);
        ms.setCreationDate(creationDateTime);
        ms.setCreationUser(creationUser);
        ms.setModificationDate(modificationDateTime);
        ms.setModificationUser(modificationUser);
        ms.setModificationCount(modificationCount);
        ms.setAppId(appId);
        ms.setVersion(appVersion);
        ms.setName(appName);
        ms.setDescription(description);
        ms.setProductName(productName);
        return ms;
    }

    /**
     * Helper method to create a microservice (ms) request object for the backend service (as mock).
     *
     * @param appId unique id of application
     * @param version version number
     * @param name unique name of application
     * @param description general ms description
     * @param productName name of associated product
     * @return
     */
    private CreateMicroserviceRequest createMicroserviceRequestSVC(
            String appId,
            String version,
            String name,
            String description,
            String productName) {

        CreateMicroserviceRequest ms = new CreateMicroserviceRequest();
        ms.setAppId(appId);
        ms.setVersion(version);
        ms.setName(name);
        ms.setDescription(description);
        ms.setProductName(productName);

        return ms;
    }

    /**
     * Helper method to create a microservice (ms) request object for the bff service (inbound call).
     *
     * @param appId unique id of application
     * @param version version number
     * @param name unique name of application
     * @param description general ms description
     * @param productName name of associated product
     * @return
     */
    private CreateMicroserviceRequestDTO createMicroserviceRequestBFF(
            String appId,
            String version,
            String name,
            String description,
            String productName) {

        CreateMicroserviceRequestDTO ms = new CreateMicroserviceRequestDTO();
        ms.setAppId(appId);
        ms.setAppVersion(version);
        ms.setAppName(name);
        ms.setDescription(description);
        ms.setProductName(productName);

        return ms;
    }

    /**
     * Helper method to update a microservice (ms) request object for the backend service (as mock).
     *
     * @param appId unique id of application
     * @param version version number
     * @param name unique name of application
     * @param description general ms description
     * @param productName name of associated product
     * @return
     */
    private UpdateMicroserviceRequest updateMicroserviceRequestSVC(
            String appId,
            String version,
            String name,
            String description,
            String productName) {

        UpdateMicroserviceRequest ms = new UpdateMicroserviceRequest();
        ms.setAppId(appId);
        ms.setVersion(version);
        ms.setName(name);
        ms.setDescription(description);
        ms.setProductName(productName);
        return ms;
    }

    /**
     * Helper method to update a microservice (ms) request object for bff (inbound).
     *
     * @param appId unique id of application
     * @param version version number
     * @param name unique name of application
     * @param description general ms description
     * @param productName name of associated product
     * @return
     */
    private UpdateMicroserviceRequestDTO updateMicroserviceRequestBFF(
            String appId,
            String version,
            String name,
            String description,
            String productName) {

        UpdateMicroserviceRequestDTO ms = new UpdateMicroserviceRequestDTO();
        ms.setAppId(appId);
        ms.setAppVersion(version);
        ms.setAppName(name);
        ms.setDescription(description);
        ms.setProductName(productName);
        return ms;
    }
}
