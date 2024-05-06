package org.tkit.onecx.product.store.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.product.store.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.product.store.bff.rs.mappers.SlotsMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.product.store.bff.rs.internal.SlotsApiService;
import gen.org.tkit.onecx.product.store.bff.rs.internal.model.CreateSlotRequestDTO;
import gen.org.tkit.onecx.product.store.bff.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.product.store.bff.rs.internal.model.SlotSearchCriteriaDTO;
import gen.org.tkit.onecx.product.store.bff.rs.internal.model.UpdateSlotRequestDTO;
import gen.org.tkit.onecx.product.store.client.api.SlotsInternalApi;
import gen.org.tkit.onecx.product.store.client.model.Slot;
import gen.org.tkit.onecx.product.store.client.model.SlotPageResult;

@LogService
@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class SlotsRestController implements SlotsApiService {

    @Inject
    @RestClient
    SlotsInternalApi slotsClient;

    @Inject
    SlotsMapper slotsMapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response createSlot(CreateSlotRequestDTO createSlotRequestDTO) {
        try (Response response = slotsClient.createSlot(slotsMapper.map(createSlotRequestDTO))) {
            return Response.status(response.getStatus()).entity(slotsMapper.map(response.readEntity(Slot.class))).build();
        }
    }

    @Override
    public Response deleteSlot(String id) {
        try (Response response = slotsClient.deleteSlot(id)) {
            return Response.status(response.getStatus()).build();
        }
    }

    @Override
    public Response getSlot(String id) {
        try (Response response = slotsClient.getSlot(id)) {
            return Response.status(response.getStatus()).entity(slotsMapper.map(response.readEntity(Slot.class))).build();
        }
    }

    @Override
    public Response searchSlots(SlotSearchCriteriaDTO slotSearchCriteriaDTO) {
        try (Response response = slotsClient.searchSlots(slotsMapper.map(slotSearchCriteriaDTO))) {
            return Response.status(response.getStatus()).entity(slotsMapper.map(response.readEntity(SlotPageResult.class)))
                    .build();
        }
    }

    @Override
    public Response updateSlot(String id, UpdateSlotRequestDTO updateSlotRequestDTO) {
        try (Response response = slotsClient.updateSlot(id, slotsMapper.map(updateSlotRequestDTO))) {
            return Response.status(response.getStatus()).build();
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
