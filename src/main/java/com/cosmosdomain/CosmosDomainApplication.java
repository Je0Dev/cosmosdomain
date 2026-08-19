package com.cosmosdomain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration.class,
    org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration.class
})
@EnableCaching
public class CosmosDomainApplication {

    public static void main(String[] args) {
        SpringApplication.run(CosmosDomainApplication.class, args);
    }
}