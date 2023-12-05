package io.github.onecx.product.store.bff.rs;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.quarkus.log.cdi.LogService;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.product.store.bff.clients.model.*;
import gen.io.github.onecx.product.store.bff.rs.internal.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@LogService
@TestHTTPEndpoint(ProductsRestController.class)
class ProductsRestControllerTest extends AbstractTest {

    // TODO Date-Dinger lösen

    private static final String PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH = "/internal/products";

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
        //OffsetDateTime dateTime = OffsetDateTime.parse("2023-11-30T13:53:03.688710200+01:00");
        LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.systemDefault());
        OffsetDateTimeMapper mapper = new OffsetDateTimeMapper();
        //LocalDateTime localDateTime = LocalDateTime.of(2023, 11, 30, 15, 30);
        OffsetDateTime dateTime = mapper.map(localDateTime);
        Product data = createProduct("7a0ee705-8fd0-47b0-8205-b2a5f6540b9e", 0, dateTime, null, dateTime,
                null, "test-appl2", "Here is some description 2", false,
                "https://prod.ucwe.capgemini.com/wp-content/uploads/2023/11/world-cloud-report-banner1_2023.jpg",
                "/app3");

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
                .pathParam("id", data.getId())
                .get("/{id}")
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
        //Assertions.assertEquals(mapper.map(data.getCreationDate()), response.getCreationDate());
        Assertions.assertEquals(data.getCreationUser(), response.getCreationUser());
        //Assertions.assertEquals(mapper.map(data.getModificationDate()), response.getModificationDate());
        Assertions.assertEquals(data.getModificationUser(), response.getModificationUser());

    }

    /**
     * Scenario: Receive 404 Not Found when product is not existing.
     * Given
     * When I query GET endpoint with a non-existing id
     * Then I get a 'Not Found' response code back
     */
    @Test
    void getProduct_shouldReturnNotFound_whenProductIdDoesNotExist() {
        String id = "notExisting";
        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/" + id)
                        .withMethod(HttpMethod.GET))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode()));

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .get("/{id}", id)
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

        OffsetDateTime dateTime = OffsetDateTime.parse("2023-11-30T13:53:03.688710200+01:00");
        Product data = this.createProduct("7a0ee705-8fd0-47b0-8205-b2a5f6540b9e", 0, dateTime, null, dateTime,
                null, "test-appl2", "Here is some description 2", false,
                "https://prod.ucwe.capgemini.com/wp-content/uploads/2023/11/world-cloud-report-banner1_2023.jpg",
                "/app3");

        CreateProductRequest request = new CreateProductRequest();
        request.setBasePath("/app3");
        request.setDescription("Here is some description 2");
        request.setName("test-appl2");
        request.setImageUrl("https://prod.ucwe.capgemini.com/wp-content/uploads/2023/11/world-cloud-report-banner1_2023.jpg");

        // create mock rest endpoint
        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH).withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        CreateProductRequestDTO requestDTO = new CreateProductRequestDTO();
        requestDTO.basePath("/app3");
        requestDTO.setDescription("Here is some description 2");
        requestDTO.setName("test-appl2");
        requestDTO
                .setImageUrl("https://prod.ucwe.capgemini.com/wp-content/uploads/2023/11/world-cloud-report-banner1_2023.jpg");

        var response = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post()
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProductDTO.class);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(data.getId(), response.getId());
        Assertions.assertEquals(data.getName(), response.getName());
        Assertions.assertEquals(data.getImageUrl(), response.getImageUrl());
        Assertions.assertEquals(data.getBasePath(), response.getBasePath());
        Assertions.assertEquals(data.getDescription(), response.getDescription());
        Assertions.assertEquals(data.getVersion(), response.getVersion());
        //Assertions.assertEquals(data.getCreationDate(), response.getCreationDate());
        Assertions.assertEquals(data.getCreationUser(), response.getCreationUser());
        //Assertions.assertEquals(data.getModificationDate(), response.getModificationDate());
        Assertions.assertEquals(data.getModificationUser(), response.getModificationUser());
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
        ProblemDetailResponse data = new ProblemDetailResponse();
        data.setErrorCode("PERSIST_ENTITY_FAILED");
        data.setDetail(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'ui_ps_product_base_path'  Detail: Key (base_path)=(/app6) already exists.");
        List<ProblemDetailParam> list = new ArrayList<>();
        ProblemDetailParam param1 = new ProblemDetailParam();
        ProblemDetailParam param2 = new ProblemDetailParam();
        param1.setKey("constraint");
        param1.setValue(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'ui_ps_product_base_path'  Detail: Key (base_path)=(/app6) already exists.");
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
        request.setImageUrl("https://prod.ucwe.capgemini.com/wp-content/uploads/2023/11/world-cloud-report-banner1_2023.jpg");

        // create mock rest endpoint
        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH).withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        CreateProductRequestDTO requestDTO = new CreateProductRequestDTO();
        requestDTO.basePath("/app6");
        requestDTO.setDescription("Here is some description 2");
        requestDTO.setName("test-appl2");
        requestDTO
                .setImageUrl("https://prod.ucwe.capgemini.com/wp-content/uploads/2023/11/world-cloud-report-banner1_2023.jpg");

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

    /**
     * Scenario: Throw 400 Bad Request exception when provided product name is already used for another product.
     * Given
     * When I try to create a product with used (in backend) product name
     * Then I get a 'Bad Request' response code back
     * AND problem details are within the response body
     */
    @Test
    void createProduct_shouldReturnBadRequest_whenNameIsNotUnique() {
        ProblemDetailResponse data = new ProblemDetailResponse();
        data.setErrorCode("PERSIST_ENTITY_FAILED");
        data.setDetail(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'ui_ps_product_name'  Detail: Key (name)=(test-appl2) already exists.]");
        List<ProblemDetailParam> list = new ArrayList<>();
        ProblemDetailParam param1 = new ProblemDetailParam();
        ProblemDetailParam param2 = new ProblemDetailParam();
        param1.setKey("constraint");
        param1.setValue(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'ui_ps_product_name'  Detail: Key (name)=(test-appl2) already exists.]");
        param2.setKey("constraintName");
        param2.setValue("ui_ps_product_name");
        list.add(param1);
        list.add(param2);
        data.setParams(list);
        data.setInvalidParams(null);

        CreateProductRequest request = new CreateProductRequest();
        request.setBasePath("/app3");
        request.setDescription("Here is some description 2");
        request.setName("test-appl2");
        request.setImageUrl("https://prod.ucwe.capgemini.com/wp-content/uploads/2023/11/world-cloud-report-banner1_2023.jpg");

        // create mock rest endpoint
        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH).withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(request)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        CreateProductRequestDTO requestDTO = new CreateProductRequestDTO();
        requestDTO.basePath("/app3");
        requestDTO.setDescription("Here is some description 2");
        requestDTO.setName("test-appl2");
        requestDTO
                .setImageUrl("https://prod.ucwe.capgemini.com/wp-content/uploads/2023/11/world-cloud-report-banner1_2023.jpg");

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

    @Test
    @Disabled // not yet implemented
    void createProduct_shouldReturnInternalServerError_whenRunningIntoRuntimeIssues() {

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
        List<Product> list = new ArrayList<>();
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
        requestDTO.setPageNumber(null);
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

        List<Product> stream = new ArrayList<>();

        Product product = this.createProduct("7a0ee705-8fd0-47b0-8205-b2a5f6540b9e", 0, dateTime, null, dateTime,
                null, "test-appl2", "Here is some description 2", false,
                "https://prod.ucwe.capgemini.com/wp-content/uploads/2023/11/world-cloud-report-banner1_2023.jpg",
                "/app3");

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
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        ProductSearchCriteriaDTO requestDTO = new ProductSearchCriteriaDTO();
        requestDTO.setName("test-appl2");
        requestDTO.setPageNumber(0);
        requestDTO.setPageSize(pageSizeRequest);

        var response = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post("/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProductPageResultDTO.class);

        Assertions.assertNotNull(response);

        List<ProductDTO> productsResults = response.getStream();
        Optional<ProductDTO> responseProduct1 = productsResults.stream().filter(e -> e.getName().equals(product.getName()))
                .findFirst();

        Assertions.assertEquals(1, productsResults.size());
        Assertions.assertTrue(responseProduct1.isPresent());
        Assertions.assertEquals(product.getName(), responseProduct1.get().getName());
        Assertions.assertEquals(product.getImageUrl(), responseProduct1.get().getImageUrl());
        Assertions.assertEquals(product.getBasePath(), responseProduct1.get().getBasePath());
        Assertions.assertEquals(product.getDescription(), responseProduct1.get().getDescription());
        Assertions.assertEquals(product.getId(), responseProduct1.get().getId());
        Assertions.assertEquals(product.getVersion(), responseProduct1.get().getVersion());
        //Assertions.assertEquals(productOne.getCreationDate(), responseProduct1.get().getCreationDate());
        Assertions.assertEquals(product.getCreationUser(), responseProduct1.get().getCreationUser());
        //Assertions.assertEquals(productOne.getModificationDate(), responseProduct1.get().getModificationDate());
        Assertions.assertEquals(product.getModificationUser(), responseProduct1.get().getModificationUser());
    }

    @Test
    @Disabled // not yet implemented
    void searchProducts_shouldReturnInternalServerError_whenRunningIntoRuntimeIssues() {

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

        List<Product> stream = new ArrayList<>();

        Product productOne = this.createProduct("7a0ee705-8fd0-47b0-8205-b2a5f6540b9e", 0, dateTime, null, dateTime,
                null, "test-appl2", "Here is some description 2", false,
                "https://prod.ucwe.capgemini.com/wp-content/uploads/2023/11/world-cloud-report-banner1_2023.jpg",
                "/app3");

        Product productTwo = this.createProduct("e72a1699-9e60-4531-9422-8c325bed7e6a", 0, dateTime, "CSommer", dateTime,
                "CSommer", "test-appl5", "Here is some description 5", true,
                "https://prod.ucwe.capgemini.com/wp-content/uploads/2023/11/world-cloud-report-banner1_2023.jpg",
                "/app5");

        ProductPageResult data = new ProductPageResult();
        data.setNumber(0);
        data.setSize(pageSizeRequest);
        stream.add(productOne);
        stream.add(productTwo);
        data.setTotalElements((long) stream.size());
        data.setTotalPages(1L);
        data.setStream(stream);

        mockServerClient
                .when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/search").withMethod(HttpMethod.POST)
                        .withBody(JsonBody.json(request)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        ProductSearchCriteriaDTO requestDTO = new ProductSearchCriteriaDTO();
        requestDTO.setName(null);
        requestDTO.setPageNumber(0);
        requestDTO.setPageSize(pageSizeRequest);

        var response = given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(requestDTO)
                .post("/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProductPageResultDTO.class);

        Assertions.assertNotNull(response);

        List<ProductDTO> productsResults = response.getStream();
        Optional<ProductDTO> responseProduct1 = productsResults.stream().filter(e -> e.getName().equals(productOne.getName()))
                .findFirst();
        Optional<ProductDTO> responseProduct2 = productsResults.stream().filter(e -> e.getName().equals(productTwo.getName()))
                .findFirst();

        Assertions.assertTrue(responseProduct1.isPresent());
        Assertions.assertTrue(responseProduct2.isPresent());

        // first product
        Assertions.assertEquals(productOne.getName(), responseProduct1.get().getName());
        Assertions.assertEquals(productOne.getImageUrl(), responseProduct1.get().getImageUrl());
        Assertions.assertEquals(productOne.getBasePath(), responseProduct1.get().getBasePath());
        Assertions.assertEquals(productOne.getDescription(), responseProduct1.get().getDescription());
        Assertions.assertEquals(productOne.getId(), responseProduct1.get().getId());
        Assertions.assertEquals(productOne.getVersion(), responseProduct1.get().getVersion());
        //Assertions.assertEquals(productOne.getCreationDate(), responseProduct1.get().getCreationDate());
        Assertions.assertEquals(productOne.getCreationUser(), responseProduct1.get().getCreationUser());
        //Assertions.assertEquals(productOne.getModificationDate(), responseProduct1.get().getModificationDate());
        Assertions.assertEquals(productOne.getModificationUser(), responseProduct1.get().getModificationUser());

        // second product
        Assertions.assertEquals(productTwo.getName(), responseProduct2.get().getName());
        Assertions.assertEquals(productTwo.getImageUrl(), responseProduct2.get().getImageUrl());
        Assertions.assertEquals(productTwo.getBasePath(), responseProduct2.get().getBasePath());
        Assertions.assertEquals(productTwo.getDescription(), responseProduct2.get().getDescription());
        Assertions.assertEquals(productTwo.getId(), responseProduct2.get().getId());
        Assertions.assertEquals(productTwo.getVersion(), responseProduct2.get().getVersion());
        //Assertions.assertEquals(productTwo.getCreationDate(), responseProduct2.get().getCreationDate());
        Assertions.assertEquals(productTwo.getCreationUser(), responseProduct2.get().getCreationUser());
        //Assertions.assertEquals(productTwo.getModificationDate(), responseProduct2.get().getModificationDate());
        Assertions.assertEquals(productTwo.getModificationUser(), responseProduct2.get().getModificationUser());

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
        String id = "82789c64-9473-457e-b30a-8749d784b287";

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
        request.setImageUrl("https://prod.ucwe.capgemini.com/wp-content/uploads/2023/11/world-cloud-report-banner1_2023.jpg");

        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/" + id).withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(request)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NO_CONTENT.getStatusCode()));

        UpdateProductRequestDTO updateProductRequestDTO = new UpdateProductRequestDTO();
        updateProductRequestDTO.setBasePath("/app3");
        updateProductRequestDTO.setDescription("Some changes");
        updateProductRequestDTO.setName("test-appl2");
        updateProductRequestDTO
                .setImageUrl("https://prod.ucwe.capgemini.com/wp-content/uploads/2023/11/world-cloud-report-banner1_2023.jpg");

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .pathParam("id", id)
                .body(updateProductRequestDTO)
                .put("/{id}")
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

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

        UpdateProductRequest request = new UpdateProductRequest();
        request.setBasePath("/app3");
        request.setDescription("Some changes");
        request.setName("test-appl2");
        request.setImageUrl("https://prod.ucwe.capgemini.com/wp-content/uploads/2023/11/world-cloud-report-banner1_2023.jpg");

        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/" + id).withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(request)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode()));

        UpdateProductRequestDTO updateProductRequestDTO = new UpdateProductRequestDTO();
        updateProductRequestDTO.setBasePath("/app3");
        updateProductRequestDTO.setDescription("Some changes");
        updateProductRequestDTO.setName("test-appl2");
        updateProductRequestDTO
                .setImageUrl("https://prod.ucwe.capgemini.com/wp-content/uploads/2023/11/world-cloud-report-banner1_2023.jpg");

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(updateProductRequestDTO)
                .put("/{id}", id)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    /**
     * Scenario: Return 400-status code with exception when trying to update product by an already used product name.
     * Given Product-ID is not existing
     * When I try to update a product by a non-existing product id "nonExisting"
     * Then I get a 'Bad Request' response code back
     * AND a ProblemDetailResponseObject is returned
     */
    @Test
    void updateProduct_shouldReturnNotFound_whenRunningIntoValidationConstraints() {
        String id = "7a0ee705-8fd0-47b0-8205-b2a5f6540b9e";

        ProblemDetailResponse data = new ProblemDetailResponse();
        data.setErrorCode("PERSIST_ENTITY_FAILED");
        data.setDetail(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'ui_ps_product_base_path'  Detail: Key (base_path)=(/app15) already exists.]");
        List<ProblemDetailParam> list = new ArrayList<>();
        ProblemDetailParam param1 = new ProblemDetailParam();
        ProblemDetailParam param2 = new ProblemDetailParam();
        param1.setKey("constraint");
        param1.setValue(
                "could not execute statement [ERROR: duplicate key value violates unique constraint 'ui_ps_product_base_path'  Detail: Key (base_path)=(/app15) already exists.]");
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
        request.setImageUrl("https://prod.ucwe.capgemini.com/wp-content/uploads/2023/11/world-cloud-report-banner1_2023.jpg");

        // create mock rest endpoint
        mockServerClient.when(request().withPath(PRODUCT_STORE_SVC_INTERNAL_API_BASE_PATH + "/" + id).withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(request)))
                .withPriority(100)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        UpdateProductRequestDTO requestDTO = new UpdateProductRequestDTO();
        requestDTO.basePath("/app15");
        requestDTO.setDescription("Here is some description 2");
        requestDTO.setName("test-appl10");
        requestDTO
                .setImageUrl("https://prod.ucwe.capgemini.com/wp-content/uploads/2023/11/world-cloud-report-banner1_2023.jpg");

        var response = given()
                .when()
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
     * @return instantiate Product-Object with attributes for given values
     */
    private Product createProduct(String id, int version, OffsetDateTime creationDateTime, String creationUser,
            OffsetDateTime modificationDateTime, String modificationUser, String productName,
            String productDescription, boolean operator, String productImageUrl, String productBasePath) {

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
        return product;
    }
}
