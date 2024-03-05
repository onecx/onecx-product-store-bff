package org.tkit.onecx.product.store.bff.rs.mappers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.product.store.bff.rs.internal.model.*;
import gen.org.tkit.onecx.product.store.client.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MicrofrontendsMapper {

    @Mapping(target = "removeEndpointsItem", ignore = true)
    @Mapping(target = "removeClassificationsItem", ignore = true)
    MicrofrontendDTO mapMfe(Microfrontend mfe);

    CreateMicrofrontendRequest mapCreateMfe(CreateMicrofrontendRequestDTO createMfe);

    UpdateMicrofrontendRequest mapUpdateMfe(UpdateMicrofrontendRequestDTO updateMfe);

    @Mapping(target = "removeStreamItem", ignore = true)
    MicrofrontendPageResultDTO mapMfeSearchPageResponse(MicrofrontendPageResult searchResults);

    MicrofrontendSearchCriteria mapMfeSearchCriteria(MfeAndMsSearchCriteriaDTO mfeSearchCriteria);

    default Set<String> map(String classifications) {
        if (classifications != null && !classifications.isBlank()) {
            String[] values = classifications.split(",");
            return new LinkedHashSet<>(Arrays.asList(values));
        } else
            return new HashSet<>();
    }

    default String map(Set<String> classifications) {
        if (classifications != null && !classifications.isEmpty()) {
            return classifications.stream().map(Object::toString).collect(Collectors.joining(","));
        } else
            return "";
    }
}
