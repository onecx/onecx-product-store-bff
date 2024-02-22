package org.tkit.onecx.product.store.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.product.store.bff.rs.internal.model.*;
import gen.org.tkit.onecx.product.store.client.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface MicroservicesMapper {

    @Mapping(target = "appVersion", source = "version")
    @Mapping(target = "appName", source = "name")
    MicroserviceDTO map(Microservice ms);

    @Mapping(target = "version", source = "appVersion")
    @Mapping(target = "name", source = "appName")
    CreateMicroserviceRequest mapCreateMs(CreateMicroserviceRequestDTO createMs);

    @Mapping(target = "version", source = "appVersion")
    @Mapping(target = "name", source = "appName")
    UpdateMicroserviceRequest mapUpdateMs(UpdateMicroserviceRequestDTO updateMs);

    @Mapping(target = "removeStreamItem", ignore = true)
    MicroservicePageResultDTO mapMsSearchPageResponse(MicroservicePageResult searchResults);

    @Mapping(target = "appVersion", source = "version")
    @Mapping(target = "appName", source = "name")
    MicroserviceDTO map(MicroservicePageItem pageItem);

    @Mapping(target = "name", source = "appName")
    MicroserviceSearchCriteria mapMsSearchCriteria(MfeAndMsSearchCriteriaDTO msSearchCriteria);

}
