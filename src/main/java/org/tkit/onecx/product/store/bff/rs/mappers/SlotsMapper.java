package org.tkit.onecx.product.store.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.product.store.bff.rs.internal.model.*;
import gen.org.tkit.onecx.product.store.client.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface SlotsMapper {
    CreateSlotRequest map(CreateSlotRequestDTO createSlotRequestDTO);

    SlotDTO map(Slot slot);

    SlotSearchCriteria map(SlotSearchCriteriaDTO slotSearchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    SlotPageResultDTO map(SlotPageResult slotPageResult);

    UpdateSlotRequest map(UpdateSlotRequestDTO updateSlotRequestDTO);
}
