package io.github.onecx.product.store.bff.rs.controllers;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.io.github.onecx.product.store.bff.clients.api.ProductsInternalApi;
import gen.io.github.onecx.product.store.bff.clients.model.*;
import gen.io.github.onecx.product.store.bff.rs.internal.ProductsApiService;
import gen.io.github.onecx.product.store.bff.rs.internal.model.*;
import io.github.onecx.product.store.bff.rs.mappers.ExceptionMapper;
import io.github.onecx.product.store.bff.rs.mappers.ProblemDetailMapper;
import io.github.onecx.product.store.bff.rs.mappers.ProductsMapper;
import io.github.onecx.product.store.bff.rs.mappers.ResponseMapper;

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
    private final ExceptionMapper exceptionMapper;

    private static final String UNEXPECTED_ERROR_LOG_MESSAGE = "Unexpected exception occurred: ";
    private static final String PARSE_EXCEPTION_LOG_MESSAGE = "Exception occurred while processing the request: ";

    @Inject
    public ProductsRestController(ProductsMapper mapper, ResponseMapper responseMapper,
            ProblemDetailMapper problemDetailMapper, ExceptionMapper exceptionMapper) {

        this.mapper = mapper;
        this.responseMapper = responseMapper;
        this.problemDetailMapper = problemDetailMapper;
        this.exceptionMapper = exceptionMapper;
    }

    @Override
    public Response createProduct(CreateProductRequestDTO createProductRequestDTO) {

        try (Response response = client.createProduct(mapper.mapCreateProduct(createProductRequestDTO))) {
            Product createdProduct = response.readEntity(Product.class);
            ProductDTO createdProductDTO = mapper.mapProduct(createdProduct);
            return Response.status(response.getStatus()).entity(createdProductDTO).build();
        }
    }

    @Override
    public Response deleteProduct(String id) {

        try (Response response = client.deleteProduct(id)) {
            return Response.fromResponse(response).build();
        }
    }

    @Override
    public Response getProduct(String id) {

        try (Response response = client.getProduct(id)) {
            Product resultProduct = response.readEntity(Product.class);
            ProductDTO resultProductDTO = mapper.mapProduct(resultProduct);
            return Response.status(response.getStatus()).entity(resultProductDTO).build();
        }

    }

    @Override
    public Response searchProducts(ProductSearchCriteriaDTO productSearchCriteriaDTO) {

        try (Response response = client.searchProducts(mapper.mapProductSearchCriteria(productSearchCriteriaDTO))) {
            ProductPageResult searchPageResults = response.readEntity(ProductPageResult.class);
            ProductPageResultDTO searchPageResultDTO = mapper.mapProductSearchPageResponse(searchPageResults);
            return Response.status(response.getStatus()).entity(searchPageResultDTO).build();
        }
    }

    @Override
    public Response updateProduct(String id, UpdateProductRequestDTO updateProductRequestDTO) {

        try (Response response = client.updateProduct(id, mapper.mapUpdateProduct(updateProductRequestDTO))) {
            return Response.fromResponse(response).build();
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

            return Response.status(wae.getResponse().getStatus()).build();
        }
    }

    private ProblemDetailResponseDTO createErrorResponse(String errorMessage) {

        ProblemDetailResponseDTO errorResponse = new ProblemDetailResponseDTO();
        errorResponse.setErrorCode(String.valueOf(INTERNAL_SERVER_ERROR.getStatusCode()));
        errorResponse.setDetail(errorMessage);
        return errorResponse;
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {

        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public Response restException(WebApplicationException ex) {

        return Response.status(ex.getResponse().getStatus()).build();
    }
}
