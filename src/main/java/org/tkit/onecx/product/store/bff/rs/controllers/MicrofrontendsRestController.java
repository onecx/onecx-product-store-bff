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
import org.tkit.onecx.product.store.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.product.store.bff.rs.mappers.MicrofrontendsMapper;
import org.tkit.onecx.product.store.bff.rs.mappers.ProblemDetailMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.product.store.bff.rs.internal.MicrofrontendsApiService;
import gen.org.tkit.onecx.product.store.bff.rs.internal.model.*;
import gen.org.tkit.onecx.product.store.client.api.MicrofrontendsInternalApi;
import gen.org.tkit.onecx.product.store.client.model.Microfrontend;
import gen.org.tkit.onecx.product.store.client.model.MicrofrontendPageResult;
import gen.org.tkit.onecx.product.store.client.model.ProblemDetailResponse;

@LogService
@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class MicrofrontendsRestController implements MicrofrontendsApiService {

    @RestClient
    @Inject
    MicrofrontendsInternalApi client;

    private final MicrofrontendsMapper mapper;

    private final ProblemDetailMapper problemDetailMapper;

    private final ExceptionMapper exceptionMapper;

    @Inject
    public MicrofrontendsRestController(MicrofrontendsMapper mapper,
            ProblemDetailMapper problemDetailMapper, ExceptionMapper exceptionMapper) {

        this.mapper = mapper;
        this.problemDetailMapper = problemDetailMapper;
        this.exceptionMapper = exceptionMapper;
    }

    @Override
    public Response createMicrofrontend(CreateMicrofrontendRequestDTO createMicrofrontendRequestDTO) {

        try (Response response = client.createMicrofrontend(mapper.mapCreateMfe(createMicrofrontendRequestDTO))) {
            Microfrontend createdMfe = response.readEntity(Microfrontend.class);
            MicrofrontendDTO createdMfeDTO = mapper.mapMfe(createdMfe);
            return Response.status(response.getStatus()).entity(createdMfeDTO).build();
        } catch (WebApplicationException ex) {
            return Response.status(ex.getResponse().getStatus())
                    .entity(problemDetailMapper.map(ex.getResponse().readEntity(ProblemDetailResponse.class))).build();
        }
    }

    @Override
    public Response deleteMicrofrontend(String id) {

        try (Response response = client.deleteMicrofrontend(id)) {
            return Response.fromResponse(response).build();
        }
    }

    @Override
    public Response getMicrofrontend(String id) {

        try (Response response = client.getMicrofrontend(id)) {
            Microfrontend resultMfe = response.readEntity(Microfrontend.class);
            MicrofrontendDTO resultProductDTO = mapper.mapMfe(resultMfe);
            return Response.status(response.getStatus()).entity(resultProductDTO).build();
        }
    }

    @Override
    public Response searchMicrofrontends(MfeAndMsSearchCriteriaDTO microfrontendSearchCriteriaDTO) {

        try (Response response = client.searchMicrofrontends(mapper.mapMfeSearchCriteria(microfrontendSearchCriteriaDTO))) {
            MicrofrontendPageResult searchPageResults = response.readEntity(MicrofrontendPageResult.class);
            MicrofrontendPageResultDTO searchPageResultDTO = mapper.mapMfeSearchPageResponse(searchPageResults);
            return Response.status(response.getStatus()).entity(searchPageResultDTO).build();
        }
    }

    @Override
    public Response updateMicrofrontend(String id, UpdateMicrofrontendRequestDTO updateMicrofrontendRequestDTO) {

        try (Response response = client.updateMicrofrontend(id, mapper.mapUpdateMfe(updateMicrofrontendRequestDTO))) {
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
    public Response restException(ClientWebApplicationException ex) {
        return exceptionMapper.clientException(ex);
    }
}
