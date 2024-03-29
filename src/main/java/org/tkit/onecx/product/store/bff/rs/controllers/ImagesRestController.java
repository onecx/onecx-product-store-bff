package org.tkit.onecx.product.store.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.product.store.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.product.store.bff.rs.mappers.ImagesMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.product.store.bff.rs.internal.ImagesInternalApiService;
import gen.org.tkit.onecx.product.store.bff.rs.internal.model.ImageInfoDTO;
import gen.org.tkit.onecx.product.store.bff.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.product.store.bff.rs.internal.model.RefTypeDTO;
import gen.org.tkit.onecx.product.store.client.api.ImagesInternalApi;
import gen.org.tkit.onecx.product.store.client.model.ImageInfo;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class ImagesRestController implements ImagesInternalApiService {

    @Inject
    @RestClient
    ImagesInternalApi imageApi;

    @Inject
    ImagesMapper imageMapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response getImage(String refId, RefTypeDTO refType) {
        Response.ResponseBuilder responseBuilder;
        try (Response response = imageApi.getImage(refId, imageMapper.map(refType))) {
            var contentType = response.getHeaderString(HttpHeaders.CONTENT_TYPE);
            var contentLength = response.getHeaderString(HttpHeaders.CONTENT_LENGTH);
            var body = response.readEntity(byte[].class);
            if (contentType != null && body.length != 0) {
                responseBuilder = Response.status(response.getStatus())
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .header(HttpHeaders.CONTENT_LENGTH, contentLength)
                        .entity(body);
            } else {
                responseBuilder = Response.status(Response.Status.BAD_REQUEST);
            }

            return responseBuilder.build();
        }
    }

    @Override
    public Response updateImage(String refId, RefTypeDTO refType, byte[] body, Integer contentLength) {

        try (Response response = imageApi.updateImage(refId, imageMapper.map(refType), body,
                contentLength)) {

            ImageInfoDTO imageInfoDTO = imageMapper.map(response.readEntity(ImageInfo.class));
            return Response.status(response.getStatus()).entity(imageInfoDTO).build();
        }
    }

    @Override
    public Response uploadImage(Integer contentLength, String refId, RefTypeDTO refType, byte[] body) {

        try (Response response = imageApi.uploadImage(contentLength, refId, imageMapper.map(refType),
                body)) {
            ImageInfoDTO imageInfoDTO = imageMapper.map(response.readEntity(ImageInfo.class));
            return Response.status(response.getStatus()).entity(imageInfoDTO).build();
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
