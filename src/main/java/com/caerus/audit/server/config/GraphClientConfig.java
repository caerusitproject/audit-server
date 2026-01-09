package com.caerus.audit.server.config;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class GraphClientConfig {

    @Bean
    public TokenCredential tokenCredential(
            @Value("${microsoft.graph.tenant-id}") String tenantId,
            @Value("${microsoft.graph.client-id}") String clientId,
            @Value("${microsoft.graph.client-secret}") String clientSecret) {

        return new ClientSecretCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }

    @Bean
    public GraphServiceClient<?> graphServiceClient(TokenCredential tokenCredential) {

        return GraphServiceClient
                .builder()
                .authenticationProvider(
                        new TokenCredentialAuthProvider(
                                List.of("https://graph.microsoft.com/.default"),
                                tokenCredential
                        )
                )
                .buildClient();
    }
}
