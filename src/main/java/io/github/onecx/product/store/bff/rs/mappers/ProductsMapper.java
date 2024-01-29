package io.github.onecx.product.store.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.product.store.bff.clients.model.*;
import gen.io.github.onecx.product.store.bff.rs.internal.model.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ProductsMapper {

    @Mapping(target = "classifications", qualifiedByName = "transformClassificationsFromListToString")
    CreateProductRequest mapCreateProduct(CreateProductRequestDTO createProduct);

    @Mapping(target = "classifications", qualifiedByName = "transformClassificationsFromListToString")
    UpdateProductRequest mapUpdateProduct(UpdateProductRequestDTO updateProduct);

    ProductSearchCriteria mapProductSearchCriteria(ProductSearchCriteriaDTO productSearchCriteria);


    @Mapping(target = "classifications", qualifiedByName = "transformClassificationsFromStringToList")
    ProductDTO mapProduct(Product product);


    @Mapping(target = "classifications", qualifiedByName = "transformClassificationsFromStringToList")
    ProductAbstractDTO mapProductAbstract(ProductAbstract productAbstract);

    @Mapping(target = "removeStreamItem", ignore = true)
    ProductPageResultDTO mapProductSearchPageResponse(ProductPageResult searchResults);

    @Named("transformClassificationsFromStringToList")
    default List<String> transformClassificationsFromStringToList(String classificationString) {
        try {
            return Stream.of(classificationString.split(",", -1))
                    .map(String::trim)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    @Named("transformClassificationsFromListToString")
    default String transformClassificationsFromListToString(Set<String> classifications) {
        try {
            return Optional.ofNullable(classifications).orElse(Collections.emptySet()).stream().map(String::trim).collect(Collectors.joining(","));
        } catch (Exception ex) {
            return "";
        }
    }

}
