package io.github.onecx.product.store.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.product.store.bff.clients.model.*;
import gen.io.github.onecx.product.store.bff.rs.internal.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface MicrofrontendsMapper {

    MicrofrontendAbstractDTO mapMfeAbstract(Microfrontend mfe);

    @Mapping(target = "removeEndpointsItem", ignore = true)
    @Mapping(target = "removeClassificationsItem", ignore = true)
    MicrofrontendDTO mapMfe(Microfrontend mfe);

    CreateMicrofrontendRequest mapCreateMfe(CreateMicrofrontendRequestDTO createMfe);

    UpdateMicrofrontendRequest mapUpdateMfe(UpdateMicrofrontendRequestDTO updateMfe);

    @Mapping(target = "removeStreamItem", ignore = true)
    public MicrofrontendPageResultDTO mapMfeSearchPageResponse(MicrofrontendPageResult searchResults);

    MicrofrontendSearchCriteria mapMfeSearchCriteria(MicrofrontendSearchCriteriaDTO mfeSearchCriteria);

}
