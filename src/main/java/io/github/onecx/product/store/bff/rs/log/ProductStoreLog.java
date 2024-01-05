package io.github.onecx.product.store.bff.rs.log;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.tkit.quarkus.log.cdi.LogParam;

import gen.io.github.onecx.product.store.bff.rs.internal.model.*;

@ApplicationScoped
public class ProductStoreLog implements LogParam {

    @Override
    public List<LogParam.Item> getClasses() {

        return List.of(
                this.item(10, CreateMicrofrontendRequestDTO.class,
                        x -> "CreateMicrofrontendRequestDTO[ appId: " +
                                ((CreateMicrofrontendRequestDTO) x).getAppId()
                                + ", AppName: " + ((CreateMicrofrontendRequestDTO) x).getAppName()
                                + ", remoteBaseUrl: " + ((CreateMicrofrontendRequestDTO) x).getRemoteBaseUrl()
                                + ", remoteEntry: " + ((CreateMicrofrontendRequestDTO) x).getRemoteEntry()
                                + ", productName: " + ((CreateMicrofrontendRequestDTO) x).getProductName()
                                + " ]"),
                this.item(10, UpdateMicrofrontendRequestDTO.class,
                        x -> "UpdateMicrofrontendRequestDTO[ appId: " +
                                ((UpdateMicrofrontendRequestDTO) x).getAppId()
                                + ", AppName: " + ((UpdateMicrofrontendRequestDTO) x).getAppName()
                                + ", remoteBaseUrl: " + ((UpdateMicrofrontendRequestDTO) x).getRemoteBaseUrl()
                                + ", remoteEntry: " + ((UpdateMicrofrontendRequestDTO) x).getRemoteEntry()
                                + ", productName: " + ((UpdateMicrofrontendRequestDTO) x).getProductName()
                                + " ]"),
                this.item(10, MicrofrontendSearchCriteriaDTO.class,
                        x -> "MicrofrontendSearchCriteriaDTO[ appId: " +
                                ((MicrofrontendSearchCriteriaDTO) x).getAppId()
                                + ", AppName: " + ((MicrofrontendSearchCriteriaDTO) x).getAppName()
                                + ", productName: " + ((MicrofrontendSearchCriteriaDTO) x).getProductName()
                                + " ]"),
                this.item(10, CreateProductRequestDTO.class,
                        x -> "CreateProductRequestDTO[ name: " +
                                ((CreateProductRequestDTO) x).getName()
                                + ", basePath: " + ((CreateProductRequestDTO) x).getBasePath() + " ]"),
                this.item(10, UpdateProductRequestDTO.class,
                        x -> "UpdateProductRequestDTO[ name: " + ((UpdateProductRequestDTO) x).getName()
                                + ", basePath: " + ((UpdateProductRequestDTO) x).getBasePath() + " ]"),
                this.item(10, ProductSearchCriteriaDTO.class,
                        x -> "ProductSearchCriteriaDTO[ name: " + ((ProductSearchCriteriaDTO) x).getName()
                                + " ]"));
    }

}
