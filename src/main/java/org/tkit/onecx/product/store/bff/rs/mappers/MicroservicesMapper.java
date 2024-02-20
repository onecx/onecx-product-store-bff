package org.tkit.onecx.product.store.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.product.store.bff.rs.internal.model.*;
import gen.org.tkit.onecx.product.store.client.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface MicroservicesMapper {

    MicroserviceDTO map(Microservice ms);

    CreateMicroserviceRequest mapCreateMs(CreateMicroserviceRequestDTO createMs);

    UpdateMicroserviceRequest mapUpdateMs(UpdateMicroserviceRequestDTO updateMs);

    @Mapping(target = "removeStreamItem", ignore = true)
    MicroservicePageResultDTO mapMsSearchPageResponse(MicroservicePageResult searchResults);

    MicroserviceSearchCriteria mapMsSearchCriteria(MicroserviceSearchCriteriaDTO msSearchCriteria);

}
