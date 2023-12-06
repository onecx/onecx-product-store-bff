package io.github.onecx.product.store.bff.rs.mappers;

import java.util.ArrayList;

import jakarta.inject.Inject;

import org.mapstruct.Mapper;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.io.github.onecx.product.store.bff.clients.model.Product;
import gen.io.github.onecx.product.store.bff.clients.model.ProductPageResult;
import gen.io.github.onecx.product.store.bff.rs.internal.model.ProductAbstractDTO;
import gen.io.github.onecx.product.store.bff.rs.internal.model.ProductDTO;
import gen.io.github.onecx.product.store.bff.rs.internal.model.ProductPageResultDTO;

@Mapper(uses = { OffsetDateTimeMapper.class })
public class ResponseMapper {

    @Inject
    ProductsMapper mapper;

    public ProductDTO mapCreateProductResponse(Product product) {

        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setVersion(product.getVersion());
        productDTO.setCreationDate(product.getCreationDate());
        productDTO.setCreationUser(product.getCreationUser());
        productDTO.setModificationDate(product.getModificationDate());
        productDTO.setModificationUser(product.getModificationUser());
        productDTO.setName(product.getName());
        productDTO.setDescription(product.getDescription());
        productDTO.setOperator(product.getOperator());
        productDTO.setImageUrl(product.getImageUrl());
        productDTO.setBasePath(product.getBasePath());

        return productDTO;
    }

    public ProductAbstractDTO mapProductAbstract(Product product) {

        ProductAbstractDTO productAbstractDTO = new ProductAbstractDTO();
        productAbstractDTO.setId(product.getId());
        productAbstractDTO.setName(product.getName());
        productAbstractDTO.setDescription(product.getDescription());
        productAbstractDTO.setImageUrl(product.getImageUrl());

        return productAbstractDTO;
    }

    public ProductPageResultDTO mapProductSearchPageResponse(ProductPageResult searchResults) {

        ProductPageResultDTO searchResultsDTO = new ProductPageResultDTO();
        searchResultsDTO.setNumber(searchResults.getNumber());
        searchResultsDTO.setStream(searchResults.getStream() != null
                ? searchResults.getStream().stream().map(this::mapProductAbstract).toList()
                : new ArrayList<>());
        searchResultsDTO.setSize(searchResults.getSize());
        searchResultsDTO.setTotalElements(searchResults.getTotalElements());
        searchResultsDTO.setTotalPages(searchResults.getTotalPages());
        return searchResultsDTO;
    }

}
