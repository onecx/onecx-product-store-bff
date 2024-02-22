package org.tkit.onecx.product.store.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.product.store.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.product.store.bff.rs.mappers.MicroservicesMapper;
import org.tkit.onecx.product.store.bff.rs.mappers.ProblemDetailMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.product.store.bff.rs.internal.MicroservicesApiService;
import gen.org.tkit.onecx.product.store.bff.rs.internal.model.*;
import gen.org.tkit.onecx.product.store.client.api.MicroservicesInternalApi;
import gen.org.tkit.onecx.product.store.client.model.Microservice;
import gen.org.tkit.onecx.product.store.client.model.MicroservicePageResult;
import gen.org.tkit.onecx.product.store.client.model.ProblemDetailResponse;

@LogService
@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class MicroservicesRestController implements MicroservicesApiService {

    @RestClient
    @Inject
    MicroservicesInternalApi client;

    @Inject
    MicroservicesMapper mapper;

    @Inject
    ProblemDetailMapper problemDetailMapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response createMicroservice(CreateMicroserviceRequestDTO createMicroserviceRequestDTO) {

        try (Response response = client.createMicroservice(mapper.mapCreateMs(createMicroserviceRequestDTO))) {
            Microservice createdMs = response.readEntity(Microservice.class);
            MicroserviceDTO createdMsDTO = mapper.map(createdMs);
            return Response.status(response.getStatus()).entity(createdMsDTO).build();
        } catch (WebApplicationException ex) {
            return Response.status(ex.getResponse().getStatus())
                    .entity(problemDetailMapper.map(ex.getResponse().readEntity(ProblemDetailResponse.class))).build();
        }
    }

    @Override
    public Response deleteMicroservice(String id) {
        try (Response response = client.deleteMicroservice(id)) {
            return Response.fromResponse(response).build();
        }
    }

    @Override
    public Response getMicroservice(String id) {
        try (Response response = client.getMicroservice(id)) {
            Microservice resultMs = response.readEntity(Microservice.class);
            MicroserviceDTO resultProductDTO = mapper.map(resultMs);
            return Response.status(response.getStatus()).entity(resultProductDTO).build();
        }
    }

    @Override
    public Response getMicroserviceByAppId(String appId) {
        try (Response response = client.getMicroserviceByAppId(appId)) {
            MicroserviceDTO result = mapper.map(response.readEntity(Microservice.class));
            return Response.status(response.getStatus()).entity(result).build();
        }
    }

    @Override
    public Response searchMicroservice(MfeAndMsSearchCriteriaDTO microserviceSearchCriteriaDTO) {
        try (Response response = client.searchMicroservice(mapper.mapMsSearchCriteria(microserviceSearchCriteriaDTO))) {
            MicroservicePageResult searchPageResults = response.readEntity(MicroservicePageResult.class);
            MicroservicePageResultDTO searchPageResultDTO = mapper.mapMsSearchPageResponse(searchPageResults);
            return Response.status(response.getStatus()).entity(searchPageResultDTO).build();
        }
    }

    @Override
    public Response updateMicroservice(String id, UpdateMicroserviceRequestDTO updateMicroserviceRequestDTO) {
        try (Response response = client.updateMicroservice(id, mapper.mapUpdateMs(updateMicroserviceRequestDTO))) {
            return Response.fromResponse(response).build();
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
    public Response restException(WebApplicationException ex) {
        return Response.status(ex.getResponse().getStatus()).build();
    }
}
