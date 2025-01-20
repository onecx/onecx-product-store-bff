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
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.product.store.bff.rs.controllers.ProductsRestController;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.product.store.bff.rs.internal.model.*;
import gen.org.tkit.onecx.product.store.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import net.minidev.json.JSONObject;

@QuarkusTest
@TestHTTPEndpoint(ProductsRestController.class)
class ProductsRestControllerTest extends AbstractTest {

    private static final String PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH = "/internal/products";

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    @InjectMockServerClient
    MockServerClient mockServerClient;

    /**
     * Scenario: Receives product by product-id successfully.
     * Given
     * When I query GET endpoint with an existing id
     * Then I get a 'OK' response code back
     * AND associated product is returned
     */
    @Test
    void getProduct_shouldReturnProduct() {

        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2023-11-30T13:53:03.688710200+01:00");
        OffsetDateTimeMapper mapper = new OffsetDateTimeMapper();

        Product data = createProduct("7a0ee705-8fd0-47b0-8205-b2a5f6540b9e", "0", offsetDateTime, null, offsetDateTime,
                null, "test-appl2", "Here is some description 2", false,
                "https://rndmImageUrl.rnmd/rndmImage.jpg",
                "/app3", 0, "Product ABC", "Sun", "Themes, Menu");

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
                .extract().as(ProductDTO.class);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(data.getId(), response.getId());
        Assertions.assertEquals(data.getName(), response.getName());
        Assertions.assertEquals(data.getImageUrl(), response.getImageUrl());
        Assertions.assertEquals(data.getBasePath(), response.getBasePath());
        Assertions.assertEquals(data.getDescription(), response.getDescription());
        Assertions.assertEquals(data.getVersion(), response.getVersion());
        Assertions.assertEquals(mapper.map(data.getCreationDate()), mapper.map(response.getCreationDate()));
        Assertions.assertEquals(data.getCreationUser(), response.getCreationUser());
        Assertions.assertEquals(mapper.map(data.getModificationDate()), mapper.map(response.getModificationDate()));
        Assertions.assertEquals(data.getModificationUser(), response.getModificationUser());
        Assertions.assertEquals(data.getModificationCount(), response.getModificationCount());
        Assertions.assertEquals(data.getDisplayName(), response.getDisplayName());
        Assertions.assertEquals(data.getIconName(), response.getIconName());
        Assertions.assertEquals(Arrays.stream(data.getClassifications().split(",")).toArray().length,
                response.getClassifications().size());
    }

    /**
     * Scenario: Receives product by product-id successfully.
     * Given
     * When I query GET endpoint with an existing id
     * Then I get a 'OK' response code back
     * AND associated product is returned
     */
    @Test
    void getProductByName_test() {

        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2023-11-30T13:53:03.688710200+01:00");
        OffsetDateTimeMapper mapper = new OffsetDateTimeMapper();

        Product data = createProduct("7a0ee705-8fd0-47b0-8205-b2a5f6540b9e", "0", offsetDateTime, null, offsetDateTime,
                null, "test-appl2", "Here is some description 2", false,
                "https://my-page.site.url",
                "/app3", 0, "Product ABC", "Sun", "Themes, Menu");
        // create mock rest endpoint
        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/name/" + data.getName())
                        .withMethod(HttpMethod.GET))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .get("/name/" + data.getName())
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProductDTO.class);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(data.getId(), response.getId());
        Assertions.assertEquals(data.getName(), response.getName());
        Assertions.assertEquals(data.getImageUrl(), response.getImageUrl());
        Assertions.assertEquals(data.getBasePath(), response.getBasePath());
        Assertions.assertEquals(data.getDescription(), response.getDescription());
        Assertions.assertEquals(data.getVersion(), response.getVersion());
        Assertions.assertEquals(mapper.map(data.getCreationDate()), mapper.map(response.getCreationDate()));
        Assertions.assertEquals(data.getCreationUser(), response.getCreationUser());
        Assertions.assertEquals(mapper.map(data.getModificationDate()), mapper.map(response.getModificationDate()));
        Assertions.assertEquals(data.getModificationUser(), response.getModificationUser());
        Assertions.assertEquals(data.getModificationCount(), response.getModificationCount());
        Assertions.assertEquals(data.getDisplayName(), response.getDisplayName());
        Assertions.assertEquals(data.getIconName(), response.getIconName());
        Assertions.assertEquals(Arrays.stream(data.getClassifications().split(",")).toArray().length,
                response.getClassifications().size());

        // getByName for not existing name -> should return not found
        // create mock rest endpoint
        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/name/not-existing")
                        .withMethod(HttpMethod.GET))
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode()));

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .get("/name/not-existing")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        Product data2 = createProduct("7a0ee705-8fd0-47b0-8205-b2a5f6540b9e", "0", offsetDateTime, null, offsetDateTime,
                null, "test-appl50", "Here is some description 2", false,
                "https://rndmImageUrl.rnmd/rndmImage.jpg",
                "/app3", 0, "Product ABC", "Sun", "Themes, Menu");

        // create mock rest endpoint
        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/name/" + data2.getName())
                        .withMethod(HttpMethod.GET))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data2)));

        var response2 = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .get("/name/" + data2.getName())
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProductDTO.class);
        Assertions.assertNotNull(response2);
        Assertions.assertEquals(data2.getId(), response2.getId());
    }

    /**
     * Scenario: Receive 404 Not Found when product with id is not existing in backend service.
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
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode())
                        .withBody(JsonBody.json(problemDetailResponse)));

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .get(id)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    /**
     * Scenario: Create new product in case product name and base path is unique (not yet existing).
     * Given
     * When I try to create a product with a non-existing (in backend) product name and basepath
     * Then I get a 'OK' response code back
     * AND created product is returned
     */
    @Test
    void createProduct_shouldAddNewProduct_whenNameAndBasePathAreUnique() {

        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2023-11-30T13:53:03.688710200+01:00");
        OffsetDateTimeMapper mapper = new OffsetDateTimeMapper();
        Set<String> classificationSet = new HashSet<>();
        classificationSet.add("Themes");
        classificationSet.add("Menu");
        Product data = this.createProduct("ABCee705-8fd0-47b0-8205-b2a5f6540b9e", "0", offsetDateTime, null, offsetDateTime,
                null, "test-appl35", "Here is some description", false,
                "https://rndmImageUrl.rnmd/rndmImage.jpg",
                "/app35", 0, "Product ABC", "Sun", "Themes,Menu");

        CreateProductRequest request = new CreateProductRequest();
        request.setBasePath("/app35");
        request.setDescription("Here is some more description");
        request.setName("test-appl35");
        request.setImageUrl("https://rndmImageUrl.rnmd/rndmImage.jpg");
        request.setDisplayName("Product ABC");
        request.setIconName("Sun");
        request.setVersion("0");

        // create mock rest endpoint
        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH).withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        CreateProductRequestDTO requestDTO = new CreateProductRequestDTO();
        requestDTO.basePath("/app35");
        requestDTO.setDescription("Here is some more description");
        requestDTO.setName("test-appl35");
        requestDTO
                .setImageUrl("https://rndmImageUrl.rnmd/rndmImage.jpg");
        requestDTO.setDisplayName("Product ABC");
        requestDTO.setIconName("Sun");
        requestDTO.setVersion("0");
        requestDTO.setClassifications(classificationSet);

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
                .extract().as(ProductDTO.class);

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

        Assertions.assertNotNull(response);
        Assertions.assertEquals(data.getId(), response.getId());
        Assertions.assertEquals(data.getName(), response.getName());
        Assertions.assertEquals(data.getImageUrl(), response.getImageUrl());
        Assertions.assertEquals(data.getBasePath(), response.getBasePath());
        Assertions.assertEquals(data.getDescription(), response.getDescription());
        Assertions.assertEquals(data.getVersion(), response.getVersion());
        Assertions.assertEquals(mapper.map(data.getCreationDate()), mapper.map(response.getCreationDate()));
        Assertions.assertEquals(data.getCreationUser(), response.getCreationUser());
        Assertions.assertEquals(mapper.map(data.getModificationDate()), mapper.map(response.getModificationDate()));
        Assertions.assertEquals(data.getModificationUser(), response.getModificationUser());
        Assertions.assertEquals(data.getModificationCount(), response.getModificationCount());
        Assertions.assertEquals(data.getDisplayName(), response.getDisplayName());
        Assertions.assertEquals(data.getIconName(), response.getIconName());
        Assertions.assertEquals(data.getClassifications().split(",").length, response.getClassifications().size());
    }

    /**
     * Scenario: Throw 400 Bad Request exception when provided base path is already used for another product.
     * Given
     * When I try to create a product with used (in backend) base path
     * Then I get a 'Bad Request' response code back
     * AND problem details are within the response body
     */
    @Test
    void createProduct_shouldReturnBadRequest_whenBasePathIsNotUnique() {

        Set<String> classificationSet = new HashSet<>();
        classificationSet.add("Themes");
        classificationSet.add("Menu");
        ProblemDetailResponse data = new ProblemDetailResponse();
        data.setErrorCode("PERSIST_ENTITY_FAILED");
        data.setDetail(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'ui_ps_product_base_path'  Detail: "
                        +
                        "Key (base_path)=(/app6) already exists.");
        List<ProblemDetailParam> list = new ArrayList<>();
        ProblemDetailParam param1 = new ProblemDetailParam();
        ProblemDetailParam param2 = new ProblemDetailParam();
        param1.setKey("constraint");
        param1.setValue(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'ui_ps_product_base_path'  Detail: "
                        +
                        "Key (base_path)=(/app6) already exists.");
        param2.setKey("constraintName");
        param2.setValue("ui_ps_product_base_path");
        list.add(param1);
        list.add(param2);
        data.setParams(list);
        data.setInvalidParams(null);

        CreateProductRequest request = new CreateProductRequest();
        request.setBasePath("/app6");
        request.setDescription("Here is some description 2");
        request.setName("test-appl2");
        request.setImageUrl("https://rndmImageUrl.rnmd/rndmImage.jpg");
        request.setDisplayName("Product ABC");
        request.setIconName("Sun");
        request.setVersion("0");

        // create mock rest endpoint
        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH).withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        CreateProductRequestDTO requestDTO = new CreateProductRequestDTO();
        requestDTO.basePath("/app6");
        requestDTO.setDescription("Here is some description 2");
        requestDTO.setName("test-appl2");
        requestDTO
                .setImageUrl("https://rndmImageUrl.rnmd/rndmImage.jpg");
        requestDTO.setDisplayName("Product ABC");
        requestDTO.setIconName("Sun");
        requestDTO.setVersion("0");
        requestDTO.setClassifications(classificationSet);

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
     * When I try to create product without setting all mandatory fields
     * Then I get a 'Bad Request' response code back
     * AND problem details are within the response body
     */
    @Test
    void createProduct_shouldReturnBadRequest_whenRunningIntoValidationConstraints() {

        ProblemDetailResponse data = new ProblemDetailResponse();
        data.setErrorCode("CONSTRAINT_VIOLATIONS");
        data.setDetail(
                "createProduct.createProductRequestDTO.name: must not be null");
        List<ProblemDetailInvalidParam> list = new ArrayList<>();
        ProblemDetailInvalidParam param1 = new ProblemDetailInvalidParam();
        param1.setName("createProduct.createProductRequestDTO.name");
        param1.setMessage("must not be null");
        list.add(param1);
        data.setParams(null);
        data.setInvalidParams(list);

        CreateProductRequest request = new CreateProductRequest();
        request.setBasePath("/app6");
        request.setDescription("Here is some description 2");
        request.setName(null);
        request.setImageUrl("https://rndmImageUrl.rnmd/rndmImage.jpg");
        request.setDisplayName("Product ABC");
        request.setIconName("Sun");
        request.setVersion("0");
        request.setClassifications(null);

        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH).withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        CreateProductRequestDTO requestDTO = new CreateProductRequestDTO();
        requestDTO.basePath("/app6");
        requestDTO.setDescription("Here is some description 2");
        requestDTO.setName(null);
        requestDTO
                .setImageUrl("https://rndmImageUrl.rnmd/rndmImage.jpg");
        requestDTO.setDisplayName("Product ABC");
        requestDTO.setIconName("Sun");
        requestDTO.setVersion("0");
        requestDTO.setClassifications(null);

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
     * Scenario: Search request for non-matching criteria results into successful response with empty product list.
     * Given
     * When I search with criteria for which no product(s) exist(s) matching the search criteria
     * Then I get a 'OK' response code back
     * AND empty product list is returned within
     */
    @Test
    void searchProducts_shouldReturnEmptyList_whenSearchCriteriaDoesNotMatch() {

        ProductSearchCriteria request = new ProductSearchCriteria();
        request.setName("somethingNotMatching");
        request.setPageNumber(null);
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
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        ProductSearchCriteriaDTO requestDTO = new ProductSearchCriteriaDTO();
        requestDTO.setName("somethingNotMatching");
        requestDTO.setPageNumber(null);
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
                .extract().as(ProductPageResultDTO.class);

        Assertions.assertEquals(1, response.getSize());
        Assertions.assertEquals(data.getTotalElements(), response.getTotalElements());
        Assertions.assertEquals(data.getTotalPages(), response.getTotalPages());
        Assertions.assertEquals(data.getNumber(), response.getNumber());
        Assertions.assertNotNull(response.getStream());
        Assertions.assertEquals(0, response.getStream().size());
    }

    /**
     * Scenario: Search request with matching criteria results into response with a single product in list matching the
     * criteria.
     * Given
     * When I search with criteria for which a product exists matching the search criteria
     * Then I get a 'OK' response code back
     * AND corresponding product is returned within the list
     */
    @Test
    void searchProducts_shouldReturnProductList_whenSearchCriteriaDoesMatch() {

        OffsetDateTime dateTime = OffsetDateTime.parse("2023-11-30T13:53:03.688710200+01:00");

        int pageSizeRequest = 10;

        ProductSearchCriteria request = new ProductSearchCriteria();
        request.setName("test-appl2");
        request.setPageNumber(0);
        request.setPageSize(pageSizeRequest);

        List<ProductAbstract> stream = new ArrayList<>();

        ProductAbstract product = this.createProductAbstract("7a0ee705-8fd0-47b0-8205-b2a5f6540b9e", "0", dateTime,
                null, dateTime,
                null, "test-appl2", "Here is some description 2", false,
                "https://rndmImageUrl.rnmd/rndmImage.jpg",
                "/app3", 0, "MyName", "Times");

        ProductPageResult data = new ProductPageResult();
        data.setNumber(0);
        data.setSize(pageSizeRequest);
        stream.add(product);
        data.setTotalElements((long) stream.size());
        data.setTotalPages(1L);
        data.setStream(stream);

        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/search").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(request)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        ProductSearchCriteriaDTO requestDTO = new ProductSearchCriteriaDTO();
        requestDTO.setName("test-appl2");
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
                .extract().as(ProductPageResultDTO.class);

        Assertions.assertNotNull(response);

        List<ProductAbstractDTO> productPageProductAbstractResults = response.getStream();
        Optional<ProductAbstractDTO> responseProductAbstractItem = productPageProductAbstractResults.stream()
                .filter(e -> e.getName().equals(product.getName()))
                .findFirst();

        Assertions.assertEquals(1, productPageProductAbstractResults.size());
        Assertions.assertTrue(responseProductAbstractItem.isPresent());
        Assertions.assertEquals(product.getName(), responseProductAbstractItem.get().getName());
        Assertions.assertEquals(product.getImageUrl(), responseProductAbstractItem.get().getImageUrl());
        Assertions.assertEquals(product.getDescription(), responseProductAbstractItem.get().getDescription());
        Assertions.assertEquals(product.getId(), responseProductAbstractItem.get().getId());
    }

    @Test
    void searchProducts_shouldReturnInternalServerError_whenDownStreamServiceRunsIntoRuntimeIssues() {

        ProductSearchCriteria request = new ProductSearchCriteria();
        request.setName("");
        request.setPageNumber(0);
        request.setPageSize(1);

        JSONObject responseBody = new JSONObject();
        responseBody.put("details",
                "Error id 1e82bc38-185a-40c7-bf51-7524fcbe2e37-2, org.tkit.quarkus.jpa.exceptions.DAOException: ErrorKeys," +
                        "key:ERROR_FIND_PRODUCTS_BY_CRITERIA,parameters:[],namedParameters:{}");
        responseBody.put("stack", "ERROR_FIND_PRODUCTS_BY_CRITERIA\\n\\ta");

        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/search").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(request)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(responseBody)));

        ProductSearchCriteriaDTO requestDTO = new ProductSearchCriteriaDTO();
        requestDTO.setName("");
        requestDTO.setPageNumber(0);
        requestDTO.setPageSize(1);

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post("/search")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * Scenario: Wildcard search to return all or subset of available products.
     * Given product(s) exist(s)
     * When I search with no criteria for the same (wildcard)
     * Then I get a 'OK' response code back
     * AND a product list is returned within the defined range of product-page-size-numbers
     */
    @Test
    void searchProducts_shouldReturnProductList_whenSearchCriteriaIsNotGivenButProductsAreAvailable() {

        OffsetDateTime dateTime = OffsetDateTime.parse("2023-11-30T13:53:03.688710200+01:00");

        int pageSizeRequest = 10;

        ProductSearchCriteria request = new ProductSearchCriteria();
        request.setName(null);
        request.setPageNumber(0);
        request.setPageSize(pageSizeRequest);

        List<ProductAbstract> stream = new ArrayList<>();

        ProductAbstract productAbstractOne = this.createProductAbstract("7a0ee705-8fd0-47b0-8205-b2a5f6540b9e", "0", dateTime,
                null,
                dateTime,
                null, "test-appl2", "Here is some description 2", false,
                "https://rndmImageUrl.rnmd/rndmImage.jpg",
                "/app3", 0, "Product 1", "Icon");

        ProductAbstract productAbstractTwo = this.createProductAbstract("e72a1699-9e60-4531-9422-8c325bed7e6a", "0", dateTime,
                "CSommer",
                dateTime,
                "CSommer", "test-appl5", "Here is some description 5", true,
                "https://rndmImageUrl.rnmd/rndmImage.jpg",
                "/app5", 1, "Product 2", "Sunny");

        ProductPageResult data = new ProductPageResult();
        data.setNumber(0);
        data.setSize(pageSizeRequest);
        stream.add(productAbstractOne);
        stream.add(productAbstractTwo);
        data.setTotalElements((long) stream.size());
        data.setTotalPages(1L);
        data.setStream(stream);

        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/search").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(request)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        ProductSearchCriteriaDTO requestDTO = new ProductSearchCriteriaDTO();
        requestDTO.setName(null);
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
                .extract().as(ProductPageResultDTO.class);

        Assertions.assertNotNull(response);

        List<ProductAbstractDTO> productAbstractResults = response.getStream();
        Optional<ProductAbstractDTO> responseProductAbstract1 = productAbstractResults.stream()
                .filter(e -> e.getName().equals(productAbstractOne.getName()))
                .findFirst();
        Optional<ProductAbstractDTO> responseProductAbstract2 = productAbstractResults.stream()
                .filter(e -> e.getName().equals(productAbstractTwo.getName()))
                .findFirst();

        Assertions.assertTrue(responseProductAbstract1.isPresent());
        Assertions.assertTrue(responseProductAbstract2.isPresent());

        // first product
        Assertions.assertEquals(productAbstractOne.getName(), responseProductAbstract1.get().getName());
        Assertions.assertEquals(productAbstractOne.getImageUrl(), responseProductAbstract1.get().getImageUrl());
        Assertions.assertEquals(productAbstractOne.getDescription(), responseProductAbstract1.get().getDescription());
        Assertions.assertEquals(productAbstractOne.getId(), responseProductAbstract1.get().getId());

        // second product
        Assertions.assertEquals(productAbstractTwo.getName(), responseProductAbstract2.get().getName());
        Assertions.assertEquals(productAbstractTwo.getImageUrl(), responseProductAbstract2.get().getImageUrl());
        Assertions.assertEquals(productAbstractTwo.getDescription(), responseProductAbstract2.get().getDescription());
        Assertions.assertEquals(productAbstractTwo.getId(), responseProductAbstract2.get().getId());

        // general structure
        Assertions.assertEquals(pageSizeRequest, response.getSize());
        Assertions.assertEquals(data.getTotalElements(), response.getTotalElements());
        Assertions.assertEquals(data.getTotalPages(), response.getTotalPages());
        Assertions.assertEquals(data.getNumber(), response.getNumber());
    }

    /**
     * Scenario: Delete product by existing product id.
     * Given
     * When I try to delete a product by existing product id
     * Then I get a 'No Content' response code back
     */
    @Test
    void deleteProduct_shouldDeleteProduct() {

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
     * Scenario: Delete product by existing product id.
     * Given
     * When I try to delete a product by existing product id
     * Then I get a 'No Content' response code back
     */
    @Test
    void deleteProduct_shouldReturnInternalServerError_whenDownStreamServiceRunsIntoRuntimeIssues() {

        String id = "82789c64-9473-457e-b30a-8749d784b287";
        JSONObject responseBody = new JSONObject();
        responseBody.put("details", "Error id 1e82bc38-185a-40c7-bf51-7524fcbe2e37-2, org.tkit.quarkus.jpa.exceptions");
        responseBody.put("stack", "SOME RUNTIME ERROR\\n\\ta");

        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/" + id).withMethod(HttpMethod.DELETE))
                .respond(httpRequest -> response().withStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(responseBody)));

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", id)
                .delete("/{id}")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    /**
     * Scenario: Update existing product successfully.
     * Given
     * When I try to update a product by existing product id
     * Then I get a 'No Content' response code back
     */
    @Test
    void updateProduct_shouldUpdateProduct() {

        String id = "7a0ee705-8fd0-47b0-8205-b2a5f6540b9e";

        UpdateProductRequest request = new UpdateProductRequest();
        request.setBasePath("/app3");
        request.setDescription("Some changes");
        request.setName("test-appl2");
        request.setImageUrl("https://rndmImageUrl.rnmd/rndmImage.jpg");
        request.setVersion("1.0.0");
        request.setClassifications(null);
        request.setIconName("Sunny");

        Product response = new Product();
        response.setBasePath("/app3");
        response.setDescription("Some changes");
        response.setName("test-appl2");
        response.setImageUrl("https://rndmImageUrl.rnmd/rndmImage.jpg");
        response.setVersion("1.0.0");
        response.setClassifications(null);
        response.setIconName("Sunny");

        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/" + id).withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(request)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withBody(JsonBody.json(response)));

        UpdateProductRequestDTO updateProductRequestDTO = new UpdateProductRequestDTO();
        updateProductRequestDTO.setBasePath("/app3");
        updateProductRequestDTO.setDescription("Some changes");
        updateProductRequestDTO.setName("test-appl2");
        updateProductRequestDTO
                .setImageUrl("https://rndmImageUrl.rnmd/rndmImage.jpg");
        updateProductRequestDTO.setVersion("1.0.0");
        updateProductRequestDTO.setClassifications(null);
        updateProductRequestDTO.setIconName("Sunny");

        ProductDTO dto = new ProductDTO();
        dto.setBasePath("/app3");
        dto.setDescription("Some changes");
        dto.setName("test-appl2");
        dto.setImageUrl("https://rndmImageUrl.rnmd/rndmImage.jpg");
        dto.setVersion("1.0.0");
        dto.setClassifications(null);
        dto.setIconName("Sunny");

        var responseDTO = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", id)
                .body(updateProductRequestDTO)
                .put("/{id}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode()).extract().as(ProductDTO.class);

        Assertions.assertEquals(request.getBasePath(), responseDTO.getBasePath());
    }

    /**
     * Scenario: Return 404-status code when trying to update product by a non-existing product id.
     * Given Product-ID is not existing
     * When I try to update a product by a non-existing product id "nonExisting"
     * Then I get a 'Not Found' response code back
     */
    @Test
    void updateProduct_shouldReturnNotFound_whenProductIdDoesNotExist() {

        String id = "notExisting";

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode("404");

        UpdateProductRequest request = new UpdateProductRequest();
        request.setBasePath("/app3");
        request.setDescription("Some changes");
        request.setName("test-appl2");
        request.setImageUrl("https://rndmImageUrl.rnmd/rndmImage.jpg");
        request.setClassifications(null);
        request.setIconName("Trash");
        request.setVersion("1.0.0");

        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/" + id).withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(request)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode())
                        .withBody(JsonBody.json(problemDetailResponse)));

        UpdateProductRequestDTO updateProductRequestDTO = new UpdateProductRequestDTO();
        updateProductRequestDTO.setBasePath("/app3");
        updateProductRequestDTO.setDescription("Some changes");
        updateProductRequestDTO.setName("test-appl2");
        updateProductRequestDTO
                .setImageUrl("https://rndmImageUrl.rnmd/rndmImage.jpg");
        updateProductRequestDTO.setClassifications(null);
        updateProductRequestDTO.setIconName("Trash");
        updateProductRequestDTO.setVersion("1.0.0");

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(updateProductRequestDTO)
                .put("/{id}", id)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);
    }

    /**
     * Scenario: Return 400-status code with exception when trying to update product by an already used product name.
     * Given Product-ID does existing
     * When I try to update a product by used value for product name
     * Then I get a 'Bad Request' response code back
     * AND a ProblemDetailResponseObject is returned
     */
    @Test
    void updateProduct_shouldReturnNotFound_whenRunningIntoValidationConstraints() {

        String id = "7a0ee705-8fd0-47b0-8205-b2a5f6540b9e";

        ProblemDetailResponse data = new ProblemDetailResponse();
        data.setErrorCode("PERSIST_ENTITY_FAILED");
        data.setDetail(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'ui_ps_product_base_path'  Detail: "
                        +
                        "Key (base_path)=(/app15) already exists.]");
        List<ProblemDetailParam> list = new ArrayList<>();
        ProblemDetailParam param1 = new ProblemDetailParam();
        ProblemDetailParam param2 = new ProblemDetailParam();
        param1.setKey("constraint");
        param1.setValue(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'ui_ps_product_base_path'  Detail: "
                        +
                        "Key (base_path)=(/app15) already exists.]");
        param2.setKey("constraintName");
        param2.setValue("ui_ps_product_base_path");
        list.add(param1);
        list.add(param2);
        data.setParams(list);
        data.setInvalidParams(null);

        UpdateProductRequest request = new UpdateProductRequest();
        request.setBasePath("/app15");
        request.setDescription("Here is some description 2");
        request.setName("test-appl10");
        request.setImageUrl("https://rndmImageUrl.rnmd/rndmImage.jpg");
        request.setVersion("1.0.0");
        request.setIconName("Trash");
        request.setClassifications(null);

        // create mock rest endpoint
        mockServerClient.when(request()
                .withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/" + id)
                .withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(request)))
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withBody(JsonBody.json(data))
                        .withContentType(MediaType.APPLICATION_JSON));

        UpdateProductRequestDTO requestDTO = new UpdateProductRequestDTO();
        requestDTO.basePath("/app15");
        requestDTO.setDescription("Here is some description 2");
        requestDTO.setName("test-appl10");
        requestDTO
                .setImageUrl("https://rndmImageUrl.rnmd/rndmImage.jpg");
        requestDTO.version("1.0.0");
        requestDTO.setIconName("Trash");
        requestDTO.setClassifications(null);

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

    @Test
    void getProductDetailsByNameTest() {
        MicrofrontendSearchCriteria mfeCriteria = new MicrofrontendSearchCriteria();
        mfeCriteria.productName("p1");
        MicrofrontendPageResult mfeResult = new MicrofrontendPageResult();
        mfeResult.setStream(List.of(new MicrofrontendPageItem().productName("p1").appName("mfe1"),
                new MicrofrontendPageItem().productName("p1").appName("mfe2"),
                new MicrofrontendPageItem().productName("p1").appName("mfe3")));
        mockServerClient
                .when(request().withPath("/internal/microfrontends/search")
                        .withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(mfeCriteria)))
                .withId("mock1")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(mfeResult)));

        SlotSearchCriteria slotCriteria = new SlotSearchCriteria();
        slotCriteria.productName("p1");
        SlotPageResult slotPageResult = new SlotPageResult();
        slotPageResult.setStream(List.of(new SlotPageItem().productName("p1")));
        mockServerClient
                .when(request().withPath("/internal/slots/search")
                        .withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(slotCriteria)))
                .withId("mock2")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(slotPageResult)));

        MicroserviceSearchCriteria msCriteria = new MicroserviceSearchCriteria();
        msCriteria.productName("p1");
        MicroservicePageResult msResult = new MicroservicePageResult();
        msResult.setStream(List.of(new MicroservicePageItem().productName("p1")));

        mockServerClient
                .when(request().withPath("/internal/microservices/search")
                        .withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(msCriteria)))
                .withId("mock3")
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(msResult)));

        var response = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(new ProductSearchCriteria().name("p1"))
                .post("/details")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProductDetailsDTO.class);

        Assertions.assertEquals(3, response.getMicrofrontends().size());
        Assertions.assertEquals(1, response.getMicroservices().size());
        Assertions.assertEquals(1, response.getSlots().size());
        mockServerClient.clear("mock1");
        mockServerClient.clear("mock2");
        mockServerClient.clear("mock3");

    }

    /**
     * Helper method to create products
     *
     * @param id unique id of the product
     * @param version version number
     * @param creationDateTime datetime of creation
     * @param creationUser user name
     * @param modificationDateTime datetime of modification
     * @param modificationUser user name
     * @param productName unique name of product
     * @param productDescription general product description
     * @param operator whether system- or manually created (boolean)
     * @param productImageUrl url for product image
     * @param productBasePath uri for base path
     * @param modificationCount counted modification of the entity
     * @param displayName dedicated name for displaying to user
     * @param iconName identifier of PrimeNG icon lib, z.b. trash, times
     * @param classifications tags for product
     * @return
     */
    private Product createProduct(String id, String version, OffsetDateTime creationDateTime, String creationUser,
            OffsetDateTime modificationDateTime, String modificationUser, String productName,
            String productDescription, boolean operator, String productImageUrl, String productBasePath,
            int modificationCount, String displayName, String iconName, String classifications) {

        Product product = new Product();
        product.setId(id);
        product.setVersion(version);
        product.setCreationDate(creationDateTime);
        product.setCreationUser(creationUser);
        product.setModificationDate(modificationDateTime);
        product.setModificationUser(modificationUser);
        product.setName(productName);
        product.setDescription(productDescription);
        product.setOperator(operator);
        product.setImageUrl(productImageUrl);
        product.setBasePath(productBasePath);
        product.setModificationCount(modificationCount);
        product.setDisplayName(displayName);
        product.setIconName(iconName);
        product.setClassifications(classifications);
        return product;
    }

    /**
     * Helper method to create productAbstracts (a subset of product entity)
     *
     * @param id unique id of the product
     * @param version version number
     * @param creationDateTime datetime of creation
     * @param creationUser user name
     * @param modificationDateTime datetime of modification
     * @param modificationUser user name
     * @param productName unique name of product
     * @param productDescription general product description
     * @param operator whether system- or manually created (boolean)
     * @param productImageUrl url for product image
     * @param productBasePath uri for base path
     * @param modificationCount counted modification of the entity
     * @param displayName dedicated name for displaying to user
     * @param iconName identifier of PrimeNG icon lib, z.b. trash, times
     * @return
     */
    private ProductAbstract createProductAbstract(String id, String version, OffsetDateTime creationDateTime,
            String creationUser,
            OffsetDateTime modificationDateTime, String modificationUser, String productName,
            String productDescription, boolean operator, String productImageUrl,
            String productBasePath,
            int modificationCount, String displayName, String iconName) {

        ProductAbstract productAbstract = new ProductAbstract();
        productAbstract.setId(id);
        productAbstract.setVersion(version);
        productAbstract.setCreationDate(creationDateTime);
        productAbstract.setCreationUser(creationUser);
        productAbstract.setModificationDate(modificationDateTime);
        productAbstract.setModificationUser(modificationUser);
        productAbstract.setName(productName);
        productAbstract.setDescription(productDescription);
        productAbstract.setOperator(operator);
        productAbstract.setImageUrl(productImageUrl);
        productAbstract.setBasePath(productBasePath);
        productAbstract.setModificationCount(modificationCount);
        productAbstract.setDisplayName(displayName);
        productAbstract.setIconName(iconName);
        return productAbstract;
    }

}
