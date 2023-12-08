package io.github.onecx.product.store.bff.rs.mappers;

import gen.io.github.onecx.product.store.bff.clients.model.CreateProductRequest;
import gen.io.github.onecx.product.store.bff.clients.model.Product;
import gen.io.github.onecx.product.store.bff.clients.model.ProductSearchCriteria;
import gen.io.github.onecx.product.store.bff.clients.model.UpdateProductRequest;
import gen.io.github.onecx.product.store.bff.rs.internal.model.CreateProductRequestDTO;
import gen.io.github.onecx.product.store.bff.rs.internal.model.ProductDTO;
import gen.io.github.onecx.product.store.bff.rs.internal.model.ProductSearchCriteriaDTO;
import gen.io.github.onecx.product.store.bff.rs.internal.model.UpdateProductRequestDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import java.text.ParseException;

@Mapper(uses = {OffsetDateTimeMapper.class})
public interface ProductsMapper {

    CreateProductRequest mapCreateProduct(CreateProductRequestDTO createProduct) throws ParseException;

    UpdateProductRequest mapUpdateProduct(UpdateProductRequestDTO updateProduct) throws ParseException;

    ProductSearchCriteria mapProductSearchCriteria(ProductSearchCriteriaDTO productSearchCriteria) throws ParseException;

    @Mapping(target = "removeClassificationsItem", ignore = true)
    ProductDTO mapProduct(Product product) throws ParseException;

}
