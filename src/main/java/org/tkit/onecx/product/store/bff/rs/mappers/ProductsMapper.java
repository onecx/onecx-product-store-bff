package org.tkit.onecx.product.store.bff.rs.mappers;

import java.util.*;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.product.store.bff.rs.internal.model.*;
import gen.org.tkit.onecx.product.store.client.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ProductsMapper {

    CreateProductRequest mapCreateProduct(CreateProductRequestDTO createProduct);

    UpdateProductRequest mapUpdateProduct(UpdateProductRequestDTO updateProduct);

    ProductSearchCriteria mapProductSearchCriteria(ProductSearchCriteriaDTO productSearchCriteria);

    @Mapping(target = "removeClassificationsItem", ignore = true)
    ProductDTO mapProduct(Product product);

    @Mapping(target = "removeApplicationsItem", ignore = true)
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

    @Mapping(target = "type", ignore = true)
    @Mapping(target = "productName", source = "name")
    @Mapping(target = "appName", ignore = true)
    @Mapping(target = "appId", ignore = true)
    MicrofrontendSearchCriteria map(ProductDetailsCriteriaDTO productSearchCriteriaDTO);

    @Mapping(target = "productName", source = "name")
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "appId", ignore = true)
    MicroserviceSearchCriteria mapMsCriteria(ProductDetailsCriteriaDTO productSearchCriteriaDTO);

    @Mapping(target = "productName", source = "name")
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "appId", ignore = true)
    SlotSearchCriteria mapSlotCriteria(ProductDetailsCriteriaDTO productSearchCriteriaDTO);

    @Mapping(target = "removeProvidersItem", ignore = true)
    @Mapping(target = "removeClassificationsItem", ignore = true)
    ProductCriteriaDTO mapCriteriaLists(ProductCriteria productCriteria);

    ProductLoadSearchCriteria mapLoadCriteria(ProductLoadSearchCriteriaDTO productLoadSearchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    ProductsLoadResultDTO mapLoadResult(ProductsLoadResult productsLoadResult);

    MfeAbstractDTO map(MicrofrontendAbstract mfeAbstract);

    @Mapping(target = "removeSlotsItem", ignore = true)
    @Mapping(target = "removeMicroservicesItem", ignore = true)
    @Mapping(target = "removeMicrofrontendsItem", ignore = true)
    LoadProductDTO map(LoadProduct product);
}
