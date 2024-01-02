package io.github.onecx.product.store.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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

@LogService
@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class ProductsRestController implements ProductsApiService {

    @RestClient
    @Inject
    ProductsInternalApi client;

    private final ProductsMapper mapper;

    private final ProblemDetailMapper problemDetailMapper;

    @Inject
    public ProductsRestController(ProductsMapper mapper,
            ProblemDetailMapper problemDetailMapper) {

        this.mapper = mapper;
        this.problemDetailMapper = problemDetailMapper;
    }

    @Override
    public Response createProduct(CreateProductRequestDTO createProductRequestDTO) {

        try (Response response = client.createProduct(mapper.mapCreateProduct(createProductRequestDTO))) {
            Product createdProduct = response.readEntity(Product.class);
            ProductDTO createdProductDTO = mapper.mapProduct(createdProduct);
            return Response.status(response.getStatus()).entity(createdProductDTO).build();
        } catch (WebApplicationException ex) {
            return Response.status(ex.getResponse().getStatus())
                    .entity(problemDetailMapper.map(ex.getResponse().readEntity(ProblemDetailResponse.class))).build();
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
        } catch (WebApplicationException ex) {
            return Response.status(ex.getResponse().getStatus())
                    .entity(problemDetailMapper.map(ex.getResponse().readEntity(ProblemDetailResponse.class))).build();
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
        } catch (WebApplicationException ex) {
            return Response.status(ex.getResponse().getStatus())
                    .entity(problemDetailMapper.map(ex.getResponse().readEntity(ProblemDetailResponse.class))).build();
        }
    }
}
