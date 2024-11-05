package org.tkit.onecx.product.store.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.product.store.bff.rs.mappers.*;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.product.store.bff.rs.internal.ProductsApiService;
import gen.org.tkit.onecx.product.store.bff.rs.internal.model.*;
import gen.org.tkit.onecx.product.store.client.api.MicrofrontendsInternalApi;
import gen.org.tkit.onecx.product.store.client.api.MicroservicesInternalApi;
import gen.org.tkit.onecx.product.store.client.api.ProductsInternalApi;
import gen.org.tkit.onecx.product.store.client.api.SlotsInternalApi;
import gen.org.tkit.onecx.product.store.client.model.*;

@LogService
@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class ProductsRestController implements ProductsApiService {

    @RestClient
    @Inject
    ProductsInternalApi client;

    @RestClient
    @Inject
    MicroservicesInternalApi msClient;

    @RestClient
    @Inject
    MicrofrontendsInternalApi mfeClient;

    @RestClient
    @Inject
    SlotsInternalApi slotClient;

    @Inject
    ProductsMapper mapper;

    @Inject
    MicrofrontendsMapper mfeMapper;

    @Inject
    MicroservicesMapper msMapper;

    @Inject
    SlotsMapper slotMapper;

    private final ProblemDetailMapper problemDetailMapper;

    private final ExceptionMapper exceptionMapper;

    @Inject
    public ProductsRestController(ProductsMapper mapper,
            ProblemDetailMapper problemDetailMapper, ExceptionMapper exceptionMapper) {

        this.mapper = mapper;
        this.problemDetailMapper = problemDetailMapper;
        this.exceptionMapper = exceptionMapper;
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
            return Response.status(response.getStatus()).build();
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
    public Response getProductByName(String name) {
        try (Response response = client.getProductByName(name)) {
            Product resultProduct = response.readEntity(Product.class);
            ProductDTO resultProductDTO = mapper.mapProduct(resultProduct);
            return Response.status(response.getStatus()).entity(resultProductDTO).build();
        }
    }

    @Override
    public Response getProductDetailsByCriteria(ProductSearchCriteriaDTO productSearchCriteriaDTO) {
        ProductDetailsDTO detailsDTO = new ProductDetailsDTO();
        detailsDTO.setName(productSearchCriteriaDTO.getName());
        try (Response mfeResponse = mfeClient.searchMicrofrontends(mapper.map(productSearchCriteriaDTO))) {
            detailsDTO.setMicrofrontends(mfeMapper.map(mfeResponse.readEntity(MicrofrontendPageResult.class)));
        }

        try (Response msResponse = msClient.searchMicroservice(mapper.mapMsCriteria(productSearchCriteriaDTO))) {
            detailsDTO.setMicroservices(msMapper.map(msResponse.readEntity(MicroservicePageResult.class)));
        }

        try (Response slotResponse = slotClient.searchSlots(mapper.mapSlotCriteria(productSearchCriteriaDTO))) {
            detailsDTO.setSlots(slotMapper.mapToList(slotResponse.readEntity(SlotPageResult.class)));
        }

        return Response.status(Response.Status.OK).entity(detailsDTO).build();
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
            return Response.status(response.getStatus()).build();
        } catch (WebApplicationException ex) {
            return Response.status(ex.getResponse().getStatus())
                    .entity(problemDetailMapper.map(ex.getResponse().readEntity(ProblemDetailResponse.class))).build();
        }
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public Response restException(ClientWebApplicationException ex) {
        return exceptionMapper.clientException(ex);
    }
}
