package io.github.onecx.product.store.bff.rs.mappers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.product.store.bff.clients.model.*;
import gen.io.github.onecx.product.store.bff.rs.internal.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MicrofrontendsMapper {

    @Mapping(target = "removeEndpointsItem", ignore = true)
    @Mapping(target = "removeClassificationsItem", ignore = true)
    MicrofrontendDTO mapMfe(Microfrontend mfe);

    CreateMicrofrontendRequest mapCreateMfe(CreateMicrofrontendRequestDTO createMfe);

    UpdateMicrofrontendRequest mapUpdateMfe(UpdateMicrofrontendRequestDTO updateMfe);

    @Mapping(target = "removeStreamItem", ignore = true)
    MicrofrontendPageResultDTO mapMfeSearchPageResponse(MicrofrontendPageResult searchResults);

    MicrofrontendSearchCriteria mapMfeSearchCriteria(MicrofrontendSearchCriteriaDTO mfeSearchCriteria);

    default Set<String> map(String classifications) {
        if (classifications != null && !classifications.isBlank()) {
            String[] values = classifications.split(",");
            Set<String> hashSet = new HashSet<>(Arrays.asList(values));
            return hashSet;
        } else
            return new HashSet<String>();
    }

    default String map(Set<String> classifications) {
        if (classifications != null && !classifications.isEmpty()) {
            String str = classifications.stream().map(Object::toString).collect(Collectors.joining(","));
            return str;
        } else
            return "";
    }
}
