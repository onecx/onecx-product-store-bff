package io.github.onecx.product.store.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.time.OffsetDateTime;
import java.util.*;

import io.github.onecx.product.store.bff.rs.controllers.MicrofrontendsRestController;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.product.store.bff.clients.model.*;
import gen.io.github.onecx.product.store.bff.rs.internal.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import net.minidev.json.JSONObject;

@QuarkusTest
@TestHTTPEndpoint(MicrofrontendsRestController.class)
class MicrofrontendsRestControllerTest extends AbstractTest {

    private static final String PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH = "/internal/microfrontends";

    @InjectMockServerClient
    MockServerClient mockServerClient;

    @AfterEach
    void setUp() {

        mockServerClient.reset();
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
                "AppName", "some description", "", "https://localhost/mfe/core/ah-mgmt/" ,
                "https://localhost/mfe/core/ah-mgmt/remoteEntry.js" , "ProductName", classificationSet,
                "developers@1000kit.org", "sun", "some notes", "/AnnouncementManagementModule", uiEndpointSet);

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/" + data.getId())
                        .withMethod(HttpMethod.GET))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        var response = given()
                .when()
                .contentType(APPLICATION_JSON)
                .get(data.getId())
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(MicrofrontendDTO.class);

        Assertions.assertNotNull(response);
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
        Assertions.assertEquals(data.getClassifications().size(), response.getClassifications().size());
        Object[] arrayItem = data.getClassifications().toArray();
        Assertions.assertTrue(response.getClassifications().contains(arrayItem[0]));
        Assertions.assertTrue(response.getClassifications().contains(arrayItem[1]));

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
    void getProduct_shouldReturnNotFound_whenProductIdDoesNotExist() {

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode(String.valueOf(Response.Status.NOT_FOUND.getStatusCode()));

        String id = "notExisting";
        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/" + id)
                        .withMethod(HttpMethod.GET))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode())
                        .withBody(JsonBody.json(problemDetailResponse)));

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .get(id)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

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
                "AppName", "some description", "", "https://localhost/mfe/core/ah-mgmt/" ,
                "https://localhost/mfe/core/ah-mgmt/remoteEntry.js" , "ProductName", classificationSet,
                "developers@1000kit.org", "sun", "some notes", "/AnnouncementManagementModule", uiEndpointSet);

        List<CreateUIEndpoint> uiEndpointSetForRequest = new ArrayList<>();
        CreateUIEndpoint uiEndpointItemForRequest = new CreateUIEndpoint();
        uiEndpointItemForRequest.setName("Search");
        uiEndpointItemForRequest.setPath("/search");
        uiEndpointSetForRequest.add(uiEndpointItemForRequest);
        CreateMicrofrontendRequest request = createMicrofrontendRequestSVC("App-ID", "1.0.0",
                "AppName", "some description", "", "https://localhost/mfe/core/ah-mgmt/" ,
                "https://localhost/mfe/core/ah-mgmt/remoteEntry.js" , "ProductName", classificationSet,
                "developers@1000kit.org", "sun", "some notes", "/AnnouncementManagementModule", uiEndpointSetForRequest);


        // create mock rest endpoint
        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH).withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        List<CreateUIEndpointDTO> uiEndpointSetForRequestBFF = new ArrayList<>();
        CreateUIEndpointDTO uiEndpointItemForRequestBFF = new CreateUIEndpointDTO();
        uiEndpointItemForRequestBFF.setName("Search");
        uiEndpointItemForRequestBFF.setPath("/search");
        uiEndpointSetForRequestBFF.add(uiEndpointItemForRequestBFF);
        CreateMicrofrontendRequestDTO requestDTO = createMicrofrontendRequestBFF("App-ID", "1.0.0",
                "AppName", "some description", "", "https://localhost/mfe/core/ah-mgmt/" ,
                "https://localhost/mfe/core/ah-mgmt/remoteEntry.js" , "ProductName", classificationSet,
                "developers@1000kit.org", "sun", "some notes", "/AnnouncementManagementModule", uiEndpointSetForRequestBFF);


        var response = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(MicrofrontendDTO.class);

        Assertions.assertNotNull(response);
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
        Assertions.assertEquals(data.getClassifications().size(), response.getClassifications().size());
        Object[] arrayItem = data.getClassifications().toArray();
        Assertions.assertTrue(response.getClassifications().contains(arrayItem[0]));
        Assertions.assertTrue(response.getClassifications().contains(arrayItem[1]));

        Assertions.assertEquals(data.getContact(), response.getContact());
        Assertions.assertEquals(data.getIconName(), response.getIconName());
        Assertions.assertEquals(data.getNote(), response.getNote());
        Assertions.assertEquals(data.getExposedModule(), response.getExposedModule());
        Assertions.assertEquals(data.getEndpoints().size(), response.getEndpoints().size());
        Assertions.assertEquals(data.getEndpoints().get(0).getName(), response.getEndpoints().get(0).getName());
        Assertions.assertEquals(data.getEndpoints().get(0).getPath(), response.getEndpoints().get(0).getPath());
    }

    /**
     * Scenario: Throw 400 Bad Request exception when provided product name and product id are already used for another microfrontend.
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
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'ps_microfrontend_app_id'  Detail: Key (product_name, app_id)=(Announcement-Management, ABCee705-8fd0-47b0-8205-b2a5f6540b9e) already exists.]");
        List<ProblemDetailParam> list = new ArrayList<>();
        ProblemDetailParam param1 = new ProblemDetailParam();
        ProblemDetailParam param2 = new ProblemDetailParam();
        param1.setKey("constraint");
        param1.setValue(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'ps_microfrontend_app_id'  Detail: Key (product_name, app_id)=(Announcement-Management, ABCee705-8fd0-47b0-8205-b2a5f6540b9e) already exists.]");
        param2.setKey("constraintName");
        param2.setValue("ps_microfrontend_app_id");
        list.add(param1);
        list.add(param2);
        data.setParams(list);
        data.setInvalidParams(null);

        CreateMicrofrontendRequest request = createMicrofrontendRequestSVC("App-ID", "1.0.0",
                "AppName", "some description", "", "https://localhost/mfe/core/ah-mgmt/" ,
                "https://localhost/mfe/core/ah-mgmt/remoteEntry.js" , "ProductName", null,
                "developers@1000kit.org", "sun", "some notes", "/AnnouncementManagementModule", null);

        // create mock rest endpoint
        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH).withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        CreateMicrofrontendRequestDTO requestDTO = createMicrofrontendRequestBFF("App-ID", "1.0.0",
                "AppName", "some description", "", "https://localhost/mfe/core/ah-mgmt/" ,
                "https://localhost/mfe/core/ah-mgmt/remoteEntry.js" , "ProductName", null,
                "developers@1000kit.org", "sun", "some notes", "/AnnouncementManagementModule", null);

        var response = given()
                .when()
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

    //         /**
    //          * Scenario: Throw 400 Bad Request exception when provided product name is already used for another product.
    //          * Given
    //          * When I try to create a product with used (in backend) product name
    //          * Then I get a 'Bad Request' response code back
    //          * AND problem details are within the response body
    //          */
    //         @Test
    //         void createProduct_shouldReturnBadRequest_whenNameIsNotUnique() {
    //
    //             Set<String> classificationSet = new HashSet<>();
    //             classificationSet.add("Themes");
    //             classificationSet.add("Menu");
    //             ProblemDetailResponse data = new ProblemDetailResponse();
    //             data.setErrorCode("PERSIST_ENTITY_FAILED");
    //             data.setDetail(
    //                     "could not execute statement [ERROR: duplicate key value violates unique constraint 'ui_ps_product_name'  Detail: Key "
    //                             +
    //                             "(name)=(test-appl2) already exists.]");
    //             List<ProblemDetailParam> list = new ArrayList<>();
    //             ProblemDetailParam param1 = new ProblemDetailParam();
    //             ProblemDetailParam param2 = new ProblemDetailParam();
    //             param1.setKey("constraint");
    //             param1.setValue(
    //                     "could not execute statement [ERROR: duplicate key value violates unique constraint 'ui_ps_product_name'  Detail: Key "
    //                             +
    //                             "(name)=(test-appl2) already exists.]");
    //             param2.setKey("constraintName");
    //             param2.setValue("ui_ps_product_name");
    //             list.add(param1);
    //             list.add(param2);
    //             data.setParams(list);
    //             data.setInvalidParams(null);
    //
    //             CreateProductRequest request = new CreateProductRequest();
    //             request.setBasePath("/app3");
    //             request.setDescription("Here is some description 2");
    //             request.setName("test-appl2");
    //             request.setImageUrl("https://prod.ucwe.capgemini.com/wp-content/uploads/2023/11/world-cloud-report-banner1_2023.jpg");
    //             request.setDisplayName("Product ABC");
    //             request.setIconName("Sun");
    //             request.setVersion("0");
    //             request.setClassifications(classificationSet);
    //
    //             // create mock rest endpoint
    //             mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH).withMethod(HttpMethod.POST)
    //                     .withBody(JsonBody.json(request)))
    //                     .withPriority(100)
    //                     .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
    //                             .withContentType(MediaType.APPLICATION_JSON)
    //                             .withBody(JsonBody.json(data)));
    //
    //             CreateProductRequestDTO requestDTO = new CreateProductRequestDTO();
    //             requestDTO.basePath("/app3");
    //             requestDTO.setDescription("Here is some description 2");
    //             requestDTO.setName("test-appl2");
    //             requestDTO
    //                     .setImageUrl("https://prod.ucwe.capgemini.com/wp-content/uploads/2023/11/world-cloud-report-banner1_2023.jpg");
    //             requestDTO.setDisplayName("Product ABC");
    //             requestDTO.setIconName("Sun");
    //             requestDTO.setVersion("0");
    //             requestDTO.setClassifications(classificationSet);
    //
    //             var response = given()
    //                     .when()
    //                     .contentType(APPLICATION_JSON)
    //                     .body(requestDTO)
    //                     .post()
    //                     .then()
    //                     .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
    //                     .contentType(APPLICATION_JSON)
    //                     .extract().as(ProblemDetailResponseDTO.class);
    //
    //             Assertions.assertNotNull(response);
    //             List<ProblemDetailParamDTO> problemDetailsParams = response.getParams();
    //             Optional<ProblemDetailParamDTO> paramConstraint = problemDetailsParams.stream()
    //                     .filter(e -> e.getKey().equals("constraint")
    //                     .findFirst());
    //             Optional<ProblemDetailParamDTO> paramConstraintName = problemDetailsParams.stream()
    //                     .filter(e -> e.getKey().equals("constraintName"))
    //                     .findFirst();
    //
    //             Assertions.assertTrue(paramConstraint.isPresent());
    //             Assertions.assertTrue(paramConstraintName.isPresent());
    //
    //             Assertions.assertEquals(data.getErrorCode(), response.getErrorCode());
    //             Assertions.assertEquals(data.getDetail(), response.getDetail());
    //             Assertions.assertEquals(param1.getValue(), paramConstraint.get().getValue());
    //             Assertions.assertEquals(param2.getValue(), paramConstraintName.get().getValue());
    //             Assertions.assertNull(response.getInvalidParams());
    //
    //         }

    /**
     * Scenario: Search request for non-matching criteria results into successful response with empty mfe list.
     * Given
     * When I search with criteria for which no mfe(s) exist(s) matching the search criteria
     * Then I get a 'OK' response code back
     * AND empty product list is returned within
     */
    @Test
    void searchMicrofrontends_shouldReturnEmptyList_whenSearchCriteriaDoesNotMatch() {

        MicrofrontendSearchCriteria request = new MicrofrontendSearchCriteria();
        request.setProductName("somethingNotMatching");
        request.setPageNumber(0);
        request.setPageSize(10);

        ProductPageResult data = new ProductPageResult();
        data.setNumber(0);
        data.setSize(1);
        data.setTotalElements(0L);
        data.setTotalPages(0L);
        List<ProductAbstract> list = new ArrayList<>();
        data.setStream(list);

        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/search").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(request)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        ProductSearchCriteriaDTO requestDTO = new ProductSearchCriteriaDTO();
        requestDTO.setName("somethingNotMatching");
        requestDTO.setPageNumber(0);
        requestDTO.setPageSize(10);

        var response = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post("/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProductPageResultDTO.class);

        Assertions.assertEquals(1, response.getSize());
        Assertions.assertEquals(data.getTotalElements(), response.getTotalElements());
        Assertions.assertEquals(data.getTotalPages(), response.getTotalPages());
        Assertions.assertEquals(data.getNumber(), response.getNumber());
        Assertions.assertNull(response.getStream());
    }

    /**
     * Scenario: Search request with matching criteria results into response with a single microfrontend (mfe) in list matching the
     * criteria, according to the uniqueness (constraints).
     * Given
     * When I search with criteria for which at least a mfe exists matching the search criteria
     * Then I get a 'OK' response code back
     * AND corresponding mfe is returned within the list
     */
    @Test
    void searchMicrofrontends_shouldReturnMicrofrontendListWithSingleElement_whenSearchCriteriaDoesMatch() {


    }

    // TODO product name multiple elements



    /**
     * Scenario: Delete product by existing product id.
     * Given
     * When I try to delete a product by existing product id
     * Then I get a 'No Content' response code back
     */
    @Test
    void deleteMicrofrontend_shouldDeleteMicrofrontend() {

        String id = "82789c64-9473-5555-b30a-8749d784b287";

        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/" + id).withMethod(HttpMethod.DELETE))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode()));

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", id)
                .delete("/{id}")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

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
        mfe.setClassifications(classifications);
        mfe.setContact(contact);
        mfe.setIconName(iconName);
        mfe.setNote(note);
        mfe.setExposedModule(exposedModule);
        mfe.setEndpoints(endpoints);


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
        mfe.setClassifications(classifications);
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

}
