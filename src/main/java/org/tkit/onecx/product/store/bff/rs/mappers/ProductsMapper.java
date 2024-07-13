package org.tkit.onecx.product.store.bff.rs.mappers;

import java.util.*;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.product.store.bff.rs.internal.model.*;
import gen.org.tkit.onecx.product.store.client.model.*;
import gen.org.tkit.onecx.workspace.client.model.WorkspaceAbstract;
import gen.org.tkit.onecx.workspace.client.model.WorkspacePageResult;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ProductsMapper {

    default Set<String> workspaceNames(WorkspacePageResult result) {
        if (result == null || result.getStream() == null) {
            return Set.of();
        }
        return result.getStream().stream().map(WorkspaceAbstract::getDisplayName).collect(Collectors.toSet());
    }

    CreateProductRequest mapCreateProduct(CreateProductRequestDTO createProduct);

    UpdateProductRequest mapUpdateProduct(UpdateProductRequestDTO updateProduct);

    ProductSearchCriteria mapProductSearchCriteria(ProductSearchCriteriaDTO productSearchCriteria);

    @Mapping(target = "removeClassificationsItem", ignore = true)
    ProductDTO mapProduct(Product product);

    @Mapping(target = "removeClassificationsItem", ignore = true)
    ProductAbstractDTO mapProductAbstract(ProductAbstract productAbstract);

    @Mapping(target = "removeStreamItem", ignore = true)
    ProductPageResultDTO mapProductSearchPageResponse(ProductPageResult searchResults);

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

    @Mapping(target = "removeClassificationsItem", ignore = true)
    @Mapping(target = "removeWorkspacesItem", ignore = true)
    @Mapping(target = "workspaces", source = "workspaceNames")
    ProductAndWorkspacesDTO mapProductWithWorkspaceNames(Product resultProduct, Set<String> workspaceNames);

    @Mapping(target = "productName", source = "name")
    @Mapping(target = "appName", ignore = true)
    @Mapping(target = "appId", ignore = true)
    MicrofrontendSearchCriteria map(ProductSearchCriteriaDTO productSearchCriteriaDTO);

    @Mapping(target = "productName", source = "name")
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "appId", ignore = true)
    MicroserviceSearchCriteria mapMsCriteria(ProductSearchCriteriaDTO productSearchCriteriaDTO);

    @Mapping(target = "productName", source = "name")
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "appId", ignore = true)
    SlotSearchCriteria mapSlotCriteria(ProductSearchCriteriaDTO productSearchCriteriaDTO);
}
