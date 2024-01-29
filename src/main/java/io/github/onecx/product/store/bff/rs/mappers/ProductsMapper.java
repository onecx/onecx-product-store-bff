package io.github.onecx.product.store.bff.rs.mappers;

import java.util.*;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.product.store.bff.clients.model.*;
import gen.io.github.onecx.product.store.bff.rs.internal.model.*;

@Mapper(uses = { OffsetDateTimeMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE) //hotfix
public interface ProductsMapper {

    @Mapping(target = "classifications")
    CreateProductRequest mapCreateProduct(CreateProductRequestDTO createProduct);

    @Mapping(target = "classifications")
    UpdateProductRequest mapUpdateProduct(UpdateProductRequestDTO updateProduct);

    ProductSearchCriteria mapProductSearchCriteria(ProductSearchCriteriaDTO productSearchCriteria);

    @Mapping(target = "classifications")
    ProductDTO mapProduct(Product product);

    @Mapping(target = "classifications")
    ProductAbstractDTO mapProductAbstract(ProductAbstract productAbstract);

    @Mapping(target = "removeStreamItem", ignore = true)
    ProductPageResultDTO mapProductSearchPageResponse(ProductPageResult searchResults);

    default Set<String> map(String classifications) {
        String[] values = classifications.split(",");
        Set<String> hashSet = new HashSet<>(Arrays.asList(values));
        return hashSet;
    }

    default String map(Set<String> classifications) {
        String str = classifications.stream().map(Object::toString).collect(Collectors.joining(","));
        return str;
    }

}
