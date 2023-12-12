package io.github.onecx.product.store.bff.rs.mappers;

import gen.io.github.onecx.product.store.bff.clients.model.Product;
import gen.io.github.onecx.product.store.bff.clients.model.ProductAbstract;
import gen.io.github.onecx.product.store.bff.clients.model.ProductPageResult;
import gen.io.github.onecx.product.store.bff.rs.internal.model.ProductAbstractDTO;
import gen.io.github.onecx.product.store.bff.rs.internal.model.ProductDTO;
import gen.io.github.onecx.product.store.bff.rs.internal.model.ProductPageResultDTO;
import org.mapstruct.Mapper;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import java.util.ArrayList;

@Mapper(uses = {OffsetDateTimeMapper.class})
public class ResponseMapper {

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
        productDTO.setClassifications(product.getClassifications());
        productDTO.setDisplayName(product.getDisplayName());
        productDTO.setIconName(product.getIconName());
        productDTO.setVersion(product.getVersion());
        productDTO.setModificationCount(product.getModificationCount());

        return productDTO;
    }

    public ProductAbstractDTO mapProductAbstract(ProductAbstract productAbstract) {

        ProductAbstractDTO productAbstractDTO = new ProductAbstractDTO();
        productAbstractDTO.setId(productAbstract.getId());
        productAbstractDTO.setName(productAbstract.getName());
        productAbstractDTO.setDescription(productAbstract.getDescription());
        productAbstractDTO.setImageUrl(productAbstract.getImageUrl());
        productAbstractDTO.setDisplayName(productAbstract.getDisplayName());

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
