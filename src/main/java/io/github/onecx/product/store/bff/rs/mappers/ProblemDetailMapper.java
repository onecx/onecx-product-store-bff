package io.github.onecx.product.store.bff.rs.mappers;

import gen.io.github.onecx.product.store.bff.clients.model.ProblemDetailResponse;
import gen.io.github.onecx.product.store.bff.rs.internal.model.ProblemDetailResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

@Mapper(uses = {OffsetDateTimeMapper.class})
public interface ProblemDetailMapper {

    @Mapping(target = "removeParamsItem", ignore = true)
    @Mapping(target = "removeInvalidParamsItem", ignore = true)
    ProblemDetailResponseDTO map(ProblemDetailResponse problemDetailResponse);
}
