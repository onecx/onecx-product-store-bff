package io.github.onecx.product.store.bff.rs;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import java.text.ParseException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.tkit.quarkus.log.cdi.LogService;

import gen.io.github.onecx.product.store.bff.clients.api.ProductsInternalApi;
import gen.io.github.onecx.product.store.bff.clients.model.*;
import gen.io.github.onecx.product.store.bff.rs.internal.ProductsApiService;
import gen.io.github.onecx.product.store.bff.rs.internal.model.*;
import io.github.onecx.product.store.bff.rs.mappers.ProblemDetailMapper;
import io.github.onecx.product.store.bff.rs.mappers.ProductsMapper;
import io.github.onecx.product.store.bff.rs.mappers.ResponseMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LogService
@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@Path("/products")
public class ProductsRestController implements ProductsApiService {

    @RestClient
    @Inject
    ProductsInternalApi client;

    private final ProductsMapper mapper;

    private final ResponseMapper responseMapper;

    private final ProblemDetailMapper problemDetailMapper;

    private static final String UNEXPECTED_ERROR_LOG_MESSAGE = "Unexpected exception occurred: ";
    private static final String PARSE_EXCEPTION_LOG_MESSAGE = "Exception occurred while processing the request: ";

    @Inject
    public ProductsRestController(ProductsMapper mapper, ResponseMapper responseMapper,
            ProblemDetailMapper problemDetailMapper) {

        this.mapper = mapper;
        this.responseMapper = responseMapper;
        this.problemDetailMapper = problemDetailMapper;
    }

    @Override
    public Response createProduct(CreateProductRequestDTO createProductRequestDTO) {

        CreateProductRequest createProduct;
        try {
            createProduct = mapper.mapCreateProduct(createProductRequestDTO);
        } catch (ParseException ex) {
            // Handle specific expected exceptions
            log.error(PARSE_EXCEPTION_LOG_MESSAGE + ex.getMessage());
            return Response.status(INTERNAL_SERVER_ERROR.getStatusCode())
                    .entity(createErrorResponse(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                    .build();
        }
        try (Response response = client.createProduct(createProduct)) {
            Product createdProduct = response.readEntity(Product.class);
            ProductDTO createdProductDTO = responseMapper.mapCreateProductResponse(createdProduct);
            return Response.status(response.getStatus()).entity(createdProductDTO).build();
        } catch (WebApplicationException wae) {
            // Handle specific WebApplicationException
            return handleWebApplicationException(wae);
        } catch (Exception e) {
            // Catch-all for unexpected exceptions
            log.error(UNEXPECTED_ERROR_LOG_MESSAGE + e.getMessage());
            return Response.status(INTERNAL_SERVER_ERROR.getStatusCode())
                    .entity(createErrorResponse(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                    .build();
        }
    }

    @Override
    public Response deleteProduct(String id) {

        try (Response response = client.deleteProduct(id)) {
            return Response.fromResponse(response).build();
        } catch (WebApplicationException wae) {
            return handleWebApplicationException(wae);
        } catch (Exception e) {
            // Catch-all for unexpected exceptions
            log.error(UNEXPECTED_ERROR_LOG_MESSAGE + e.getMessage());
            return Response.status(INTERNAL_SERVER_ERROR.getStatusCode())
                    .entity(createErrorResponse(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                    .build();
        }
    }

    @Override
    public Response getProduct(String id) {

        try (Response response = client.getProduct(id)) {
            Product resultProduct = response.readEntity(Product.class);
            ProductDTO resultProductDTO = mapper.mapProduct(resultProduct);
            return Response.status(response.getStatus()).entity(resultProductDTO).build();
        } catch (WebApplicationException wae) {
            return handleWebApplicationException(wae);
        } catch (ParseException ex) {
            // Handle specific expected exceptions
            log.error(PARSE_EXCEPTION_LOG_MESSAGE + ex.getMessage());
            return Response.status(INTERNAL_SERVER_ERROR.getStatusCode())
                    .entity(createErrorResponse(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                    .build();
        } catch (Exception e) {
            // Catch-all for unexpected exceptions
            log.error(UNEXPECTED_ERROR_LOG_MESSAGE + e.getMessage());
            return Response.status(INTERNAL_SERVER_ERROR.getStatusCode())
                    .entity(createErrorResponse(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                    .build();
        }

    }

    @Override
    public Response searchProducts(ProductSearchCriteriaDTO productSearchCriteriaDTO) {

        ProductSearchCriteria searchCriteria;

        try {
            searchCriteria = mapper.mapProductSearchCriteria(productSearchCriteriaDTO);
        } catch (ParseException ex) {
            // Handle specific expected exceptions
            log.error(PARSE_EXCEPTION_LOG_MESSAGE + ex.getMessage());
            return Response.status(INTERNAL_SERVER_ERROR.getStatusCode())
                    .entity(createErrorResponse(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                    .build();
        }
        try (Response response = client.searchProducts(searchCriteria)) {
            ProductPageResult searchPageResults = response.readEntity(ProductPageResult.class);
            ProductPageResultDTO searchPageResultDTO = responseMapper.mapProductSearchPageResponse(searchPageResults);
            return Response.status(response.getStatus()).entity(searchPageResultDTO).build();
        } catch (WebApplicationException wae) {
            return handleWebApplicationException(wae);
        } catch (Exception e) {
            // Catch-all for unexpected exceptions
            log.error(UNEXPECTED_ERROR_LOG_MESSAGE + e.getMessage());
            return Response.status(INTERNAL_SERVER_ERROR.getStatusCode())
                    .entity(createErrorResponse(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                    .build();
        }
    }

    @Override
    public Response updateProduct(String id, UpdateProductRequestDTO updateProductRequestDTO) {

        UpdateProductRequest updateProduct;
        try {
            updateProduct = mapper.mapUpdateProduct(updateProductRequestDTO);
        } catch (ParseException ex) {
            // Handle specific expected exceptions
            log.error(PARSE_EXCEPTION_LOG_MESSAGE + ex.getMessage());
            return Response.status(INTERNAL_SERVER_ERROR.getStatusCode())
                    .entity(createErrorResponse(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                    .build();
        }
        try (Response response = client.updateProduct(id, updateProduct)) {
            return Response.fromResponse(response).build();
        } catch (WebApplicationException wae) {
            return handleWebApplicationException(wae);
        } catch (Exception e) {
            // Catch-all for unexpected exceptions
            log.error(UNEXPECTED_ERROR_LOG_MESSAGE + e.getMessage());
            return Response.status(INTERNAL_SERVER_ERROR.getStatusCode())
                    .entity(createErrorResponse(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                    .build();
        }
    }

    /**
     * @param wae
     * @return
     */
    private Response handleWebApplicationException(WebApplicationException wae) {

        try {
            return Response.status(wae.getResponse().getStatus())
                    .entity(problemDetailMapper.map(wae.getResponse().readEntity(ProblemDetailResponse.class)))
                    .build();
        } catch (Exception ex) {
            log.error("Exception occurred while handling WebApplicationException: " + ex.getMessage());
            return Response.status(wae.getResponse().getStatus()).build();
        }
    }

    private ProblemDetailResponseDTO createErrorResponse(String errorMessage) {

        ProblemDetailResponseDTO errorResponse = new ProblemDetailResponseDTO();
        errorResponse.setErrorCode(String.valueOf(INTERNAL_SERVER_ERROR.getStatusCode()));
        errorResponse.setDetail(errorMessage);
        return errorResponse;
    }
}
