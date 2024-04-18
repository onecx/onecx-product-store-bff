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
import org.tkit.onecx.product.store.bff.rs.controllers.MicrofrontendsRestController;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.product.store.bff.rs.internal.model.*;
import gen.org.tkit.onecx.product.store.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@TestHTTPEndpoint(MicrofrontendsRestController.class)
class MicrofrontendsRestControllerTest extends AbstractTest {

    private static final String PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH = "/internal/microfrontends";

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    @InjectMockServerClient
    MockServerClient mockServerClient;

    static final String mockId = "MOCK";

    @BeforeEach
    void resetExpectation() {
        try {
            mockServerClient.clear(mockId);
        } catch (Exception ex) {
            //  mockId not existing
        }
    }

    /**
     * Scenario: Receives microfrontend (mfe) by mfe-id successfully.
     * Given
     * When I query GET endpoint with an existing id
     * Then I get a 'OK' response code back
     * AND associated mfe is returned
     */
    @Test
    void getMicrofrontend_shouldReturnMicrofrontend() {

        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2023-11-30T13:53:03.688710200+01:00");
        OffsetDateTimeMapper mapper = new OffsetDateTimeMapper();
        Set<String> classificationSet = new HashSet<>();
        classificationSet.add("some");
        classificationSet.add("another");
        List<UIEndpoint> uiEndpointSet = new ArrayList<>();
        UIEndpoint uiEndpointItem = new UIEndpoint();
        uiEndpointItem.setName("Search");
        uiEndpointItem.setPath("/search");
        uiEndpointSet.add(uiEndpointItem);

        Microfrontend data = createMicrofrontend("7a0ee705-8fd0-47b0-8205-b2a5f6540b9e", offsetDateTime, "csommer",
                offsetDateTime, "csommer", 1, false, "App-ID", "1.0.0",
                "AppName", "some description", "", "https://localhost/mfe/core/ah-mgmt/",
                "https://localhost/mfe/core/ah-mgmt/remoteEntry.js", "ProductName", classificationSet,
                "developers@1000kit.org", "sun", "some notes", "/AnnouncementManagementModule", uiEndpointSet);

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
                .extract().as(MicrofrontendDTO.class);

        Assertions.assertEquals(data.getId(), response.getId());
        Assertions.assertEquals(data.getCreationUser(), response.getCreationUser());
        Assertions.assertEquals(mapper.map(data.getCreationDate()), mapper.map(response.getCreationDate()));
        Assertions.assertEquals(mapper.map(data.getModificationDate()), mapper.map(response.getModificationDate()));
        Assertions.assertEquals(data.getModificationUser(), response.getModificationUser());
        Assertions.assertEquals(data.getModificationCount(), response.getModificationCount());
        Assertions.assertEquals(data.getOperator(), response.getOperator());
        Assertions.assertEquals(data.getAppId(), response.getAppId());
        Assertions.assertEquals(data.getAppVersion(), response.getAppVersion());
        Assertions.assertEquals(data.getAppName(), response.getAppName());
        Assertions.assertEquals(data.getDescription(), response.getDescription());
        Assertions.assertEquals(data.getRemoteBaseUrl(), response.getRemoteBaseUrl());
        Assertions.assertEquals(data.getRemoteEntry(), response.getRemoteEntry());
        Assertions.assertEquals(data.getProductName(), response.getProductName());

        Assertions.assertEquals(data.getContact(), response.getContact());
        Assertions.assertEquals(data.getIconName(), response.getIconName());
        Assertions.assertEquals(data.getNote(), response.getNote());
        Assertions.assertEquals(data.getExposedModule(), response.getExposedModule());
        Assertions.assertEquals(data.getEndpoints().size(), response.getEndpoints().size());
        Assertions.assertEquals(data.getEndpoints().get(0).getName(), response.getEndpoints().get(0).getName());
        Assertions.assertEquals(data.getEndpoints().get(0).getPath(), response.getEndpoints().get(0).getPath());

    }

    /**
     * Scenario: Receive 404 Not Found when microfrontend (mfe) is not existing in backend service.
     * Given
     * When I query GET endpoint with a non-existing id
     * Then I get a 'Not Found' response code back
     * AND problem details are within the response body
     */
    @Test
    void getMicrofrontend_shouldReturnNotFound_whenMFEIdDoesNotExist() {

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
     * Scenario: Create new microfrontend (mfe) in case product name and app-id path are unique (not yet existing).
     * Given
     * When I try to create a product with a non-existing (in backend) product name and app-id
     * Then I get a 'OK' response code back
     * AND created mfe is returned
     */
    @Test
    void createMicrofrontend_shouldAddNewMicrofrontend_whenProductnameAndAppIdAreUnique() {

        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2023-11-30T13:53:03.688710200+01:00");
        OffsetDateTimeMapper mapper = new OffsetDateTimeMapper();
        Set<String> classificationSet = new HashSet<>();
        classificationSet.add("some");
        classificationSet.add("another");
        List<UIEndpoint> uiEndpointSet = new ArrayList<>();
        UIEndpoint uiEndpointItem = new UIEndpoint();
        uiEndpointItem.setName("Search");
        uiEndpointItem.setPath("/search");
        uiEndpointSet.add(uiEndpointItem);

        Microfrontend data = createMicrofrontend("7a0ee705-8fd0-47b0-8205-b2a5f6540b9e", offsetDateTime, "csommer",
                offsetDateTime, "csommer", 1, false, "App-ID", "1.0.0",
                "AppName", "some description", "", "https://localhost/mfe/core/ah-mgmt/",
                "https://localhost/mfe/core/ah-mgmt/remoteEntry.js", "ProductName", classificationSet,
                "developers@1000kit.org", "sun", "some notes", "/AnnouncementManagementModule", uiEndpointSet);

        List<CreateUIEndpoint> uiEndpointSetForRequest = new ArrayList<>();
        CreateUIEndpoint uiEndpointItemForRequest = new CreateUIEndpoint();
        uiEndpointItemForRequest.setName("Search");
        uiEndpointItemForRequest.setPath("/search");
        uiEndpointSetForRequest.add(uiEndpointItemForRequest);
        CreateMicrofrontendRequest request = createMicrofrontendRequestSVC("App-ID", "1.0.0",
                "AppName", "some description", "", "https://localhost/mfe/core/ah-mgmt/",
                "https://localhost/mfe/core/ah-mgmt/remoteEntry.js", "ProductName", classificationSet,
                "developers@1000kit.org", "sun", "some notes", "/AnnouncementManagementModule", uiEndpointSetForRequest);

        // create mock rest endpoint
        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH).withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        List<CreateUIEndpointDTO> uiEndpointSetForRequestBFF = new ArrayList<>();
        CreateUIEndpointDTO uiEndpointItemForRequestBFF = new CreateUIEndpointDTO();
        uiEndpointItemForRequestBFF.setName("Search");
        uiEndpointItemForRequestBFF.setPath("/search");
        uiEndpointSetForRequestBFF.add(uiEndpointItemForRequestBFF);
        CreateMicrofrontendRequestDTO requestDTO = createMicrofrontendRequestBFF("App-ID", "1.0.0",
                "AppName", "some description", "", "https://localhost/mfe/core/ah-mgmt/",
                "https://localhost/mfe/core/ah-mgmt/remoteEntry.js", "ProductName", classificationSet,
                "developers@1000kit.org", "sun", "some notes", "/AnnouncementManagementModule", uiEndpointSetForRequestBFF);

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
                .extract().as(MicrofrontendDTO.class);

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
        Assertions.assertEquals(data.getOperator(), response.getOperator());
        Assertions.assertEquals(data.getAppId(), response.getAppId());
        Assertions.assertEquals(data.getAppVersion(), response.getAppVersion());
        Assertions.assertEquals(data.getAppName(), response.getAppName());
        Assertions.assertEquals(data.getDescription(), response.getDescription());
        Assertions.assertEquals(data.getRemoteBaseUrl(), response.getRemoteBaseUrl());
        Assertions.assertEquals(data.getRemoteEntry(), response.getRemoteEntry());
        Assertions.assertEquals(data.getProductName(), response.getProductName());

        Assertions.assertEquals(data.getContact(), response.getContact());
        Assertions.assertEquals(data.getIconName(), response.getIconName());
        Assertions.assertEquals(data.getNote(), response.getNote());
        Assertions.assertEquals(data.getExposedModule(), response.getExposedModule());
        Assertions.assertEquals(data.getEndpoints().size(), response.getEndpoints().size());
        Assertions.assertEquals(data.getEndpoints().get(0).getName(), response.getEndpoints().get(0).getName());
        Assertions.assertEquals(data.getEndpoints().get(0).getPath(), response.getEndpoints().get(0).getPath());
    }

    /**
     * Scenario: Throw 400 Bad Request exception when provided product name and product id are already used for another
     * microfrontend.
     * Given
     * When I try to create a mfe with used (in backend) app id and product name
     * Then I get a 'Bad Request' response code back
     * AND problem details are within the response body
     */
    @Test
    void createMicrofrontend_shouldReturnBadRequest_whenBasePathIsNotUnique() {

        ProblemDetailResponse data = new ProblemDetailResponse();
        data.setErrorCode("PERSIST_ENTITY_FAILED");
        data.setDetail(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'ps_microfrontend_app_id'  Detail: "
                        +
                        "Key (product_name, app_id)=(Announcement-Management, ABCee705-8fd0-47b0-8205-b2a5f6540b9e) already exists.]");
        List<ProblemDetailParam> list = new ArrayList<>();
        ProblemDetailParam param1 = new ProblemDetailParam();
        ProblemDetailParam param2 = new ProblemDetailParam();
        param1.setKey("constraint");
        param1.setValue(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'ps_microfrontend_app_id'  Detail: "
                        +
                        "Key (product_name, app_id)=(Announcement-Management, ABCee705-8fd0-47b0-8205-b2a5f6540b9e) already exists.]");
        param2.setKey("constraintName");
        param2.setValue("ps_microfrontend_app_id");
        list.add(param1);
        list.add(param2);
        data.setParams(list);
        data.setInvalidParams(null);

        CreateMicrofrontendRequest request = createMicrofrontendRequestSVC("App-ID", "1.0.0",
                "AppName", "some description", "", "https://localhost/mfe/core/ah-mgmt/",
                "https://localhost/mfe/core/ah-mgmt/remoteEntry.js", "ProductName", null,
                "developers@1000kit.org", "sun", "some notes", "/AnnouncementManagementModule", null);

        // create mock rest endpoint
        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH).withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        CreateMicrofrontendRequestDTO requestDTO = createMicrofrontendRequestBFF("App-ID", "1.0.0",
                "AppName", "some description", "", "https://localhost/mfe/core/ah-mgmt/",
                "https://localhost/mfe/core/ah-mgmt/remoteEntry.js", "ProductName", null,
                "developers@1000kit.org", "sun", "some notes", "/AnnouncementManagementModule", null);

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
     * When I try to create a mfe without setting all mandatory fields
     * Then I get a 'Bad Request' response code back
     * AND problem details are within the response body
     */
    @Test
    void createMicrofrontend_shouldReturnBadRequest_whenRunningIntoValidationConstraints() {

        ProblemDetailResponse data = new ProblemDetailResponse();
        data.setErrorCode("CONSTRAINT_VIOLATIONS");
        data.setDetail(
                "createMicrofrontend.createMicrofrontendRequestDTO.productName: must not be null");
        List<ProblemDetailInvalidParam> list = new ArrayList<>();
        ProblemDetailInvalidParam param1 = new ProblemDetailInvalidParam();
        param1.setName("createMicrofrontend.createMicrofrontendRequestDTO.productName");
        param1.setMessage("must not be null");
        list.add(param1);

        data.setParams(null);
        data.setInvalidParams(list);

        CreateMicrofrontendRequest request = createMicrofrontendRequestSVC("App-ID", "1.0.0",
                "AppName", "some description", "", "https://localhost/mfe/core/ah-mgmt/",
                "https://localhost/mfe/core/ah-mgmt/remoteEntry.js", null, null,
                "developers@1000kit.org", "sun", "some notes", "/AnnouncementManagementModule", null);

        // create mock rest endpoint
        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH).withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withId(mockId)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        CreateMicrofrontendRequestDTO requestDTO = createMicrofrontendRequestBFF("App-ID", "1.0.0",
                "AppName", "some description", "", "https://localhost/mfe/core/ah-mgmt/",
                "https://localhost/mfe/core/ah-mgmt/remoteEntry.js", null, null,
                "developers@1000kit.org", "sun", "some notes", "/AnnouncementManagementModule", null);

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
     * Scenario: Search request for non-matching criteria results into successful response with empty mfe list.
     * Given
     * When I search with criteria for which no mfe(s) exist(s) matching the search criteria
     * Then I get a 'OK' response code back
     * AND empty mfe list is returned within
     */
    @Test
    void searchMicrofrontends_shouldReturnEmptyList_whenSearchCriteriaDoesNotMatch() {

        MicrofrontendSearchCriteria request = new MicrofrontendSearchCriteria();
        request.setProductName("somethingNotMatching");
        request.setAppId(null);
        request.setAppName(null);
        request.setPageNumber(0);
        request.setPageSize(10);

        MicrofrontendPageResult data = new MicrofrontendPageResult();
        data.setNumber(0);
        data.setSize(1);
        data.setTotalElements(0L);
        data.setTotalPages(0L);
        List<MicrofrontendPageItem> list = new ArrayList<>();
        data.setStream(list);

        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/search").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(request)))
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
                .extract().as(MicrofrontendPageResultDTO.class);

        Assertions.assertEquals(1, response.getSize());
        Assertions.assertEquals(data.getTotalElements(), response.getTotalElements());
        Assertions.assertEquals(data.getTotalPages(), response.getTotalPages());
        Assertions.assertEquals(data.getNumber(), response.getNumber());
        Assertions.assertNotNull(response.getStream());
        Assertions.assertEquals(0, response.getStream().size());
    }

    /**
     * Scenario: Search request with matching criteria results into response with a single microfrontend (mfe) in list matching
     * the
     * criteria, according to the uniqueness (constraints).
     * Given
     * When I search with criteria for which at least a mfe exists matching the search criteria
     * Then I get a 'OK' response code back
     * AND corresponding mfe is returned within the list
     */
    @Test
    void searchMicrofrontends_shouldReturnMicrofrontendListWithSingleElement_whenSearchCriteriaDoesMatch() {

        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2023-11-30T13:53:03.688710200+01:00");

        int pageSizeRequest = 10;

        MicrofrontendSearchCriteria request = new MicrofrontendSearchCriteria();
        request.setProductName("productA");
        request.setAppId("7a0ee705-8fd0-47b0");
        request.setAppName("AppA");
        request.setPageNumber(0);
        request.setPageSize(pageSizeRequest);

        List<MicrofrontendPageItem> stream = new ArrayList<>();

        MicrofrontendPageItem mfe = createMicrofrontendPageItem("7a0ee705-8fd0-47b0-8205-b2a5f6540b9e", offsetDateTime,
                "csommer",
                offsetDateTime, "csommer", 1, false, "App-ID", "1.0.0",
                "AppName", "some description", "", "https://localhost/mfe/core/ah-mgmt/",
                "https://localhost/mfe/core/ah-mgmt/remoteEntry.js", "ProductName",
                "developers@1000kit.org", "some notes", "/AnnouncementManagementModule");

        MicrofrontendPageResult data = new MicrofrontendPageResult();
        data.setNumber(0);
        data.setSize(pageSizeRequest);
        stream.add(mfe);
        data.setTotalElements((long) stream.size());
        data.setTotalPages(1L);
        data.setStream(stream);

        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/search").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(request)))
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
                .extract().as(MicrofrontendPageResultDTO.class);

        Assertions.assertNotNull(response);

        List<MicrofrontendAbstractDTO> mfeAbstractsPageResults = response.getStream();
        Optional<MicrofrontendAbstractDTO> responseProductAbstractItem = mfeAbstractsPageResults.stream()
                .filter(e -> e.getAppName().equals(mfe.getAppName()))
                .findFirst();

        Assertions.assertEquals(1, mfeAbstractsPageResults.size());
        Assertions.assertTrue(responseProductAbstractItem.isPresent());
        Assertions.assertEquals(mfe.getId(), responseProductAbstractItem.get().getId());
        Assertions.assertEquals(mfe.getAppId(), responseProductAbstractItem.get().getAppId());
        Assertions.assertEquals(mfe.getAppName(), responseProductAbstractItem.get().getAppName());
        Assertions.assertEquals(mfe.getDescription(), responseProductAbstractItem.get().getDescription());
        Assertions.assertEquals(mfe.getRemoteBaseUrl(), responseProductAbstractItem.get().getRemoteBaseUrl());
        Assertions.assertEquals(mfe.getProductName(), responseProductAbstractItem.get().getProductName());
    }

    /**
     * Scenario: Wildcard search to return all or subset of available microfrontends (mfe).
     * Given mfe(s) exist(s)
     * When I search with no criteria for the same (wildcard)
     * Then I get a 'OK' response code back
     * AND a product list is returned within the defined range of product-page-size-numbers
     */
    @Test
    void searchMicrofrontends_shouldReturnMicrofrontendList_whenSearchCriteriaIsNotGivenButMFEsAreAvailable() {

        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2023-11-30T13:53:03.688710200+01:00");

        int pageSizeRequest = 10;

        MicrofrontendSearchCriteria request = new MicrofrontendSearchCriteria();
        request.setProductName(null);
        request.setAppId(null);
        request.setAppName(null);
        request.setPageNumber(0);
        request.setPageSize(pageSizeRequest);

        List<MicrofrontendPageItem> stream = new ArrayList<>();

        MicrofrontendPageItem mfe = createMicrofrontendPageItem("7a0ee705-8fd0-47b0-8205-b2a5f6540b9e", offsetDateTime,
                "csommer",
                offsetDateTime, "csommer", 1, false, "App-ID", "1.0.0",
                "AppName", "some description", "", "https://localhost/mfe/core/ah-mgmt/",
                "https://localhost/mfe/core/ah-mgmt/remoteEntry.js", "ProductName",
                "developers@1000kit.org", "some notes", "/AnnouncementManagementModule");
        MicrofrontendPageItem mfe2 = createMicrofrontendPageItem("888ee705-8fd0-47b0-8205-b2a5f6540888", offsetDateTime, "cso",
                offsetDateTime, "cso", 1, false, "App-IDv2", "2.0.0",
                "AppNamev2", "some description2", "", "https://localhost/mfe/core/ah-mgmtv2/",
                "https://localhost/mfe/core/ah-mgmtv2/remoteEntry.js", "ProductName",
                "developers@1000kit.org", "some notes", "/AnnouncementManagementModulev2");

        MicrofrontendPageResult data = new MicrofrontendPageResult();
        data.setNumber(0);
        data.setSize(pageSizeRequest);
        stream.add(mfe);
        stream.add(mfe2);
        data.setTotalElements((long) stream.size());
        data.setTotalPages(1L);
        data.setStream(stream);

        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/search").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(request)))
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
                .extract().as(MicrofrontendPageResultDTO.class);

        Assertions.assertNotNull(response);

        List<MicrofrontendAbstractDTO> mfeAbstractsPageResults = response.getStream();
        Optional<MicrofrontendAbstractDTO> responseProductAbstractItem1 = mfeAbstractsPageResults.stream()
                .filter(e -> e.getAppName().equals(mfe.getAppName()))
                .findFirst();
        Optional<MicrofrontendAbstractDTO> responseProductAbstractItem2 = mfeAbstractsPageResults.stream()
                .filter(e -> e.getAppName().equals(mfe2.getAppName()))
                .findFirst();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(data.getStream().size(), mfeAbstractsPageResults.size());
        Assertions.assertTrue(responseProductAbstractItem1.isPresent());
        Assertions.assertTrue(responseProductAbstractItem2.isPresent());

        // first mfe
        Assertions.assertEquals(mfe.getId(), responseProductAbstractItem1.get().getId());
        Assertions.assertEquals(mfe.getAppId(), responseProductAbstractItem1.get().getAppId());
        Assertions.assertEquals(mfe.getAppName(), responseProductAbstractItem1.get().getAppName());
        Assertions.assertEquals(mfe.getDescription(), responseProductAbstractItem1.get().getDescription());
        Assertions.assertEquals(mfe.getRemoteBaseUrl(), responseProductAbstractItem1.get().getRemoteBaseUrl());
        Assertions.assertEquals(mfe.getProductName(), responseProductAbstractItem1.get().getProductName());

        // second mfe
        Assertions.assertEquals(mfe2.getId(), responseProductAbstractItem2.get().getId());
        Assertions.assertEquals(mfe2.getAppId(), responseProductAbstractItem2.get().getAppId());
        Assertions.assertEquals(mfe2.getAppName(), responseProductAbstractItem2.get().getAppName());
        Assertions.assertEquals(mfe2.getDescription(), responseProductAbstractItem2.get().getDescription());
        Assertions.assertEquals(mfe2.getRemoteBaseUrl(), responseProductAbstractItem2.get().getRemoteBaseUrl());
        Assertions.assertEquals(mfe2.getProductName(), responseProductAbstractItem2.get().getProductName());

        // general structure
        Assertions.assertEquals(pageSizeRequest, response.getSize());
        Assertions.assertEquals(data.getTotalElements(), response.getTotalElements());
        Assertions.assertEquals(data.getTotalPages(), response.getTotalPages());
        Assertions.assertEquals(data.getNumber(), response.getNumber());
    }

    /**
     * Scenario: Delete microfrontend (mfe) by existing mfe id.
     * Given
     * When I try to delete a mfe by existing mfe id
     * Then I get a 'No Content' response code back
     */
    @Test
    void deleteMicrofrontend_shouldDeleteMicrofrontend() {

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
     * Scenario: Update existing microfrontend (mfe) successfully.
     * Given
     * When I try to update a mfe by existing mfe id
     * Then I get a 'No Content' response code back
     */
    @Test
    void updateMicrofrontend_shouldUpdateMicrofrontend() {

        String id = "7a0ee705-8fd0-47b0-8205-b2a5f6540b9e";

        Set<String> classificationSet = new HashSet<>();
        classificationSet.add("some");
        classificationSet.add("another");
        List<UpdateUIEndpoint> uiEndpointSetForRequest = new ArrayList<>();
        UpdateUIEndpoint uiEndpointItemForRequest = new UpdateUIEndpoint();
        uiEndpointItemForRequest.setName("Search");
        uiEndpointItemForRequest.setPath("/search");
        uiEndpointSetForRequest.add(uiEndpointItemForRequest);
        UpdateMicrofrontendRequest request = updateMicrofrontendRequestSVC("App-ID", "1.0.0",
                "AppName", "some description", "", "https://localhost/mfe/core/ah-mgmt/",
                "https://localhost/mfe/core/ah-mgmt/remoteEntry.js", "ProductName", classificationSet,
                "developers@1000kit.org", "sun", "some notes", "/AnnouncementManagementModule", uiEndpointSetForRequest);

        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/" + id).withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(request)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode()));

        List<UpdateUIEndpointDTO> uiEndpointDTOSetForRequest = new ArrayList<>();
        UpdateUIEndpointDTO uiEndpointDTOItemForRequest = new UpdateUIEndpointDTO();
        uiEndpointDTOItemForRequest.setName("Search");
        uiEndpointDTOItemForRequest.setPath("/search");
        uiEndpointDTOSetForRequest.add(uiEndpointDTOItemForRequest);
        UpdateMicrofrontendRequestDTO requestDTO = updateMicrofrontendRequestBFF("App-ID", "1.0.0",
                "AppName", "some description", "", "https://localhost/mfe/core/ah-mgmt/",
                "https://localhost/mfe/core/ah-mgmt/remoteEntry.js", "ProductName", classificationSet,
                "developers@1000kit.org", "sun", "some notes", "/AnnouncementManagementModule", uiEndpointDTOSetForRequest);

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
     * Scenario: Return 404-status code when trying to update microfrontend (mfe) by a non-existing mfe id.
     * Given mfe-ID is not existing
     * When I try to update a mfe by a non-existing mfe id "nonExisting"
     * Then I get a 'Not Found' response code back
     */
    @Test
    void updateMicrofrontend_shouldReturnNotFound_whenMicrofrontendIdDoesNotExist() {

        String id = "notExisting";

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode("404");

        UpdateMicrofrontendRequest request = updateMicrofrontendRequestSVC("App-ID", "1.0.0",
                "AppName", "some description", "", "https://localhost/mfe/core/ah-mgmt/",
                "https://localhost/mfe/core/ah-mgmt/remoteEntry.js", "ProductName", null,
                "developers@1000kit.org", "sun", "some notes", "/AnnouncementManagementModule", null);

        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/" + id).withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(request)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode())
                        .withBody(JsonBody.json(problemDetailResponse)));

        UpdateMicrofrontendRequestDTO requestDTO = updateMicrofrontendRequestBFF("App-ID", "1.0.0",
                "AppName", "some description", "", "https://localhost/mfe/core/ah-mgmt/",
                "https://localhost/mfe/core/ah-mgmt/remoteEntry.js", "ProductName", null,
                "developers@1000kit.org", "sun", "some notes", "/AnnouncementManagementModule", null);

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
     * Scenario: Return 400-status code with exception when trying to update microfrontend (mfe) by an already used app name.
     * Given mfe-ID is not existing
     * When I try to update a mfe by used value for app id
     * Then I get a 'Bad Request' response code back
     * AND a ProblemDetailResponseObject is returned
     */
    @Test
    void updateMicrofrontend_shouldReturnNotFound_whenRunningIntoValidationConstraints() {

        String id = "7a0ee705-8fd0-47b0-8205-b2a5f6540b9e";

        ProblemDetailResponse data = new ProblemDetailResponse();
        data.setErrorCode("MERGE_ENTITY_FAILED");
        data.setDetail(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'ps_microfrontend_app_id'  Detail: "
                        +
                        "Key (product_name, app_id)=(Announcement-Management, ABCee705-8fd0-47b0-8205-b2a5f654888) already exists.]");
        List<ProblemDetailParam> list = new ArrayList<>();
        ProblemDetailParam param1 = new ProblemDetailParam();
        ProblemDetailParam param2 = new ProblemDetailParam();
        param1.setKey("constraint");
        param1.setValue(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'ps_microfrontend_app_id'  Detail: "
                        +
                        "Key (product_name, app_id)=(Announcement-Management, ABCee705-8fd0-47b0-8205-b2a5f654888) already exists.]");
        param2.setKey("constraintName");
        param2.setValue("ps_microfrontend_app_id");
        list.add(param1);
        list.add(param2);
        data.setParams(list);
        data.setInvalidParams(null);

        UpdateMicrofrontendRequest request = updateMicrofrontendRequestSVC("ABCee705-8fd0-47b0-8205-b2a5f654888", "1.0.0",
                "AppName", "some description", "", "https://localhost/mfe/core/ah-mgmt/",
                "https://localhost/mfe/core/ah-mgmt/remoteEntry.js", "Announcement-Management", null,
                "developers@1000kit.org", "sun", "some notes", "/AnnouncementManagementModule", null);

        // create mock rest endpoint
        mockServerClient.when(request()
                .withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/" + id)
                .withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(request)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withBody(JsonBody.json(data))
                        .withContentType(MediaType.APPLICATION_JSON));

        UpdateMicrofrontendRequestDTO requestDTO = updateMicrofrontendRequestBFF("ABCee705-8fd0-47b0-8205-b2a5f654888", "1.0.0",
                "AppName", "some description", "", "https://localhost/mfe/core/ah-mgmt/",
                "https://localhost/mfe/core/ah-mgmt/remoteEntry.js", "Announcement-Management", null,
                "developers@1000kit.org", "sun", "some notes", "/AnnouncementManagementModule", null);

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
     * Helper method to create a microfrontend (mfe) object
     *
     * @param id unique id of the mfe
     * @param creationDateTime datetime of creation
     * @param creationUser user name
     * @param modificationDateTime datetime of modification
     * @param modificationUser user name
     * @param operator whether system- or manually created (boolean)
     * @param appId unique id of application
     * @param appVersion version number
     * @param appName unique name of application
     * @param description general mfe description
     * @param technology representing used technology (angular, react, ...)
     * @param remoteBaseUrl uri for remote base path
     * @param remoteEntry webpack hook
     * @param productName name of associated product
     * @param classifications tags for mfe
     * @param contact contact details (like mail address) for e.g. application support
     * @param iconName identifier of PrimeNG icon lib, z.b. trash, times
     * @param note additional notes
     * @param exposedModule module information
     * @param endpoints endpoints which can be used to address app pages & services
     * @return
     */
    private Microfrontend createMicrofrontend(String id, OffsetDateTime creationDateTime, String creationUser,
            OffsetDateTime modificationDateTime,
            String modificationUser,
            Integer modificationCount,
            Boolean operator,
            String appId,
            String appVersion,
            String appName,
            String description,
            String technology,
            String remoteBaseUrl,
            String remoteEntry,
            String productName,
            Set<String> classifications,
            String contact,
            String iconName,
            String note,
            String exposedModule,
            List<UIEndpoint> endpoints) {

        Microfrontend mfe = new Microfrontend();
        mfe.setId(id);
        mfe.setCreationDate(creationDateTime);
        mfe.setCreationUser(creationUser);
        mfe.setModificationDate(modificationDateTime);
        mfe.setModificationUser(modificationUser);
        mfe.setModificationCount(modificationCount);
        mfe.setOperator(operator);
        mfe.setAppId(appId);
        mfe.setAppVersion(appVersion);
        mfe.setAppName(appName);
        mfe.setDescription(description);
        mfe.setTechnology(technology);
        mfe.setRemoteBaseUrl(remoteBaseUrl);
        mfe.setRemoteEntry(remoteEntry);
        mfe.setProductName(productName);
        //mfe.setClassifications(classifications);
        mfe.setContact(contact);
        mfe.setIconName(iconName);
        mfe.setNote(note);
        mfe.setExposedModule(exposedModule);
        mfe.setEndpoints(endpoints);

        return mfe;
    }

    /**
     * Helper method to create a microfrontend (mfe) object
     *
     * @param id unique id of the mfe
     * @param creationDateTime datetime of creation
     * @param creationUser user name
     * @param modificationDateTime datetime of modification
     * @param modificationUser user name
     * @param operator whether system- or manually created (boolean)
     * @param appId unique id of application
     * @param appVersion version number
     * @param appName unique name of application
     * @param description general mfe description
     * @param technology representing used technology (angular, react, ...)
     * @param remoteBaseUrl uri for remote base path
     * @param remoteEntry webpack hook
     * @param productName name of associated product
     * @param contact contact details (like mail address) for e.g. application support
     * @param note additional notes
     * @param exposedModule module information
     * @return
     */
    private MicrofrontendPageItem createMicrofrontendPageItem(String id, OffsetDateTime creationDateTime, String creationUser,
            OffsetDateTime modificationDateTime,
            String modificationUser,
            Integer modificationCount,
            Boolean operator,
            String appId,
            String appVersion,
            String appName,
            String description,
            String technology,
            String remoteBaseUrl,
            String remoteEntry,
            String productName,
            String contact,
            String note,
            String exposedModule) {

        MicrofrontendPageItem mfe = new MicrofrontendPageItem();
        mfe.setId(id);
        mfe.setCreationDate(creationDateTime);
        mfe.setCreationUser(creationUser);
        mfe.setModificationDate(modificationDateTime);
        mfe.setModificationUser(modificationUser);
        mfe.setModificationCount(modificationCount);
        mfe.setOperator(operator);
        mfe.setAppId(appId);
        mfe.setAppVersion(appVersion);
        mfe.setAppName(appName);
        mfe.setDescription(description);
        mfe.setTechnology(technology);
        mfe.setRemoteBaseUrl(remoteBaseUrl);
        mfe.setRemoteEntry(remoteEntry);
        mfe.setProductName(productName);
        mfe.setContact(contact);
        mfe.setNote(note);
        mfe.setExposedModule(exposedModule);

        return mfe;
    }

    /**
     * Helper method to create a microfrontend (mfe) request object for the backend service (as mock).
     *
     * @param appId unique id of application
     * @param appVersion version number
     * @param appName unique name of application
     * @param description general mfe description
     * @param technology representing used technology (angular, react, ...)
     * @param remoteBaseUrl uri for remote base path
     * @param remoteEntry webpack hook
     * @param productName name of associated product
     * @param classifications tags for mfe
     * @param contact contact details (like mail address) for e.g. application support
     * @param iconName identifier of PrimeNG icon lib, z.b. trash, times
     * @param note additional notes
     * @param exposedModule module information
     * @param endpoints endpoints which can be used to address app pages & services
     * @return
     */
    private CreateMicrofrontendRequest createMicrofrontendRequestSVC(
            String appId,
            String appVersion,
            String appName,
            String description,
            String technology,
            String remoteBaseUrl,
            String remoteEntry,
            String productName,
            Set<String> classifications,
            String contact,
            String iconName,
            String note,
            String exposedModule,
            List<CreateUIEndpoint> endpoints) {

        CreateMicrofrontendRequest mfe = new CreateMicrofrontendRequest();
        mfe.setAppId(appId);
        mfe.setAppVersion(appVersion);
        mfe.setAppName(appName);
        mfe.setDescription(description);
        mfe.setTechnology(technology);
        mfe.setRemoteBaseUrl(remoteBaseUrl);
        mfe.setRemoteEntry(remoteEntry);
        mfe.setProductName(productName);
        //mfe.setClassifications(classifications);
        mfe.setContact(contact);
        mfe.setIconName(iconName);
        mfe.setNote(note);
        mfe.setExposedModule(exposedModule);
        mfe.setEndpoints(endpoints);

        return mfe;
    }

    /**
     * Helper method to create a microfrontend (mfe) request object for the bff service (inbound call).
     *
     * @param appId unique id of application
     * @param appVersion version number
     * @param appName unique name of application
     * @param description general mfe description
     * @param technology representing used technology (angular, react, ...)
     * @param remoteBaseUrl uri for remote base path
     * @param remoteEntry webpack hook
     * @param productName name of associated product
     * @param classifications tags for mfe
     * @param contact contact details (like mail address) for e.g. application support
     * @param iconName identifier of PrimeNG icon lib, z.b. trash, times
     * @param note additional notes
     * @param exposedModule module information
     * @param endpoints endpoints which can be used to address app pages & services
     * @return
     */
    private CreateMicrofrontendRequestDTO createMicrofrontendRequestBFF(
            String appId,
            String appVersion,
            String appName,
            String description,
            String technology,
            String remoteBaseUrl,
            String remoteEntry,
            String productName,
            Set<String> classifications,
            String contact,
            String iconName,
            String note,
            String exposedModule,
            List<CreateUIEndpointDTO> endpoints) {

        CreateMicrofrontendRequestDTO mfe = new CreateMicrofrontendRequestDTO();
        mfe.setAppId(appId);
        mfe.setAppVersion(appVersion);
        mfe.setAppName(appName);
        mfe.setDescription(description);
        mfe.setTechnology(technology);
        mfe.setRemoteBaseUrl(remoteBaseUrl);
        mfe.setRemoteEntry(remoteEntry);
        mfe.setProductName(productName);
        mfe.setClassifications(classifications);
        mfe.setContact(contact);
        mfe.setIconName(iconName);
        mfe.setNote(note);
        mfe.setExposedModule(exposedModule);
        mfe.setEndpoints(endpoints);

        return mfe;
    }

    /**
     * Helper method to update a microfrontend (mfe) request object for the backend service (as mock).
     *
     * @param appId unique id of application
     * @param appVersion version number
     * @param appName unique name of application
     * @param description general mfe description
     * @param technology representing used technology (angular, react, ...)
     * @param remoteBaseUrl uri for remote base path
     * @param remoteEntry webpack hook
     * @param productName name of associated product
     * @param classifications tags for mfe
     * @param contact contact details (like mail address) for e.g. application support
     * @param iconName identifier of PrimeNG icon lib, z.b. trash, times
     * @param note additional notes
     * @param exposedModule module information
     * @param endpoints endpoints which can be used to address app pages & services
     * @return
     */
    private UpdateMicrofrontendRequest updateMicrofrontendRequestSVC(
            String appId,
            String appVersion,
            String appName,
            String description,
            String technology,
            String remoteBaseUrl,
            String remoteEntry,
            String productName,
            Set<String> classifications,
            String contact,
            String iconName,
            String note,
            String exposedModule,
            List<UpdateUIEndpoint> endpoints) {

        UpdateMicrofrontendRequest mfe = new UpdateMicrofrontendRequest();
        mfe.setAppId(appId);
        mfe.setAppVersion(appVersion);
        mfe.setAppName(appName);
        mfe.setDescription(description);
        mfe.setTechnology(technology);
        mfe.setRemoteBaseUrl(remoteBaseUrl);
        mfe.setRemoteEntry(remoteEntry);
        mfe.setProductName(productName);
        //mfe.setClassifications(classifications);
        mfe.setContact(contact);
        mfe.setIconName(iconName);
        mfe.setNote(note);
        mfe.setExposedModule(exposedModule);
        mfe.setEndpoints(endpoints);

        return mfe;
    }

    /**
     * Helper method to update a microfrontend (mfe) request object for bff (inbound).
     *
     * @param appId unique id of application
     * @param appVersion version number
     * @param appName unique name of application
     * @param description general mfe description
     * @param technology representing used technology (angular, react, ...)
     * @param remoteBaseUrl uri for remote base path
     * @param remoteEntry webpack hook
     * @param productName name of associated product
     * @param classifications tags for mfe
     * @param contact contact details (like mail address) for e.g. application support
     * @param iconName identifier of PrimeNG icon lib, z.b. trash, times
     * @param note additional notes
     * @param exposedModule module information
     * @param endpoints endpoints which can be used to address app pages & services
     * @return
     */
    private UpdateMicrofrontendRequestDTO updateMicrofrontendRequestBFF(
            String appId,
            String appVersion,
            String appName,
            String description,
            String technology,
            String remoteBaseUrl,
            String remoteEntry,
            String productName,
            Set<String> classifications,
            String contact,
            String iconName,
            String note,
            String exposedModule,
            List<UpdateUIEndpointDTO> endpoints) {

        UpdateMicrofrontendRequestDTO mfe = new UpdateMicrofrontendRequestDTO();
        mfe.setAppId(appId);
        mfe.setAppVersion(appVersion);
        mfe.setAppName(appName);
        mfe.setDescription(description);
        mfe.setTechnology(technology);
        mfe.setRemoteBaseUrl(remoteBaseUrl);
        mfe.setRemoteEntry(remoteEntry);
        mfe.setProductName(productName);
        mfe.setClassifications(classifications);
        mfe.setContact(contact);
        mfe.setIconName(iconName);
        mfe.setNote(note);
        mfe.setExposedModule(exposedModule);
        mfe.setEndpoints(endpoints);

        return mfe;
    }

}
