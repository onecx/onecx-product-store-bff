package io.github.onecx.product.store.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.product.store.bff.clients.model.CreateProductRequest;
import gen.io.github.onecx.product.store.bff.clients.model.Product;
import gen.io.github.onecx.product.store.bff.clients.model.ProductSearchCriteria;
import gen.io.github.onecx.product.store.bff.clients.model.UpdateProductRequest;
import gen.io.github.onecx.product.store.bff.rs.internal.model.CreateProductRequestDTO;
import gen.io.github.onecx.product.store.bff.rs.internal.model.ProductDTO;
import gen.io.github.onecx.product.store.bff.rs.internal.model.ProductSearchCriteriaDTO;
import gen.io.github.onecx.product.store.bff.rs.internal.model.UpdateProductRequestDTO;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ProductsMapper {
    CreateProductRequest mapCreateProduct(CreateProductRequestDTO createProduct);

    UpdateProductRequest mapUpdateProduct(UpdateProductRequestDTO updateProduct);

    ProductSearchCriteria mapProductSearchCriteria(ProductSearchCriteriaDTO productSearchCriteria);

    ProductDTO mapProduct(Product product);

}
