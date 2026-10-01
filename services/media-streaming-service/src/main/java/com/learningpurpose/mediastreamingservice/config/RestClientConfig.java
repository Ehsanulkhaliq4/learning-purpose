package com.learningpurpose.mediastreamingservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient mediasoupRestClient(@Value("${mediasoup.service.url}") String mediasoupUrl){
        return RestClient.builder().baseUrl(mediasoupUrl).build();
    }

}
