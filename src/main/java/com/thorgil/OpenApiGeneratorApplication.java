package com.thorgil;

import com.fasterxml.jackson.databind.Module;
import com.thorgil.openapi.mwnz.xml.client.api.CompaniesXmlApi;

import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

@SpringBootApplication(
    nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class
)
@ComponentScan(
    basePackages = {"com.thorgil.openapi.mwnz.companies.api"},
    nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class
)
public class OpenApiGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenApiGeneratorApplication.class, args);
    }

    @Bean(name = "com.thorgil.openapi.mwnz.xml.client.api.CompaniesXmlApi")
    public CompaniesXmlApi companiesXmlApi(@Value("${xml_api_base_url}")  String xmlApiBaseUrl) {
        return new CompaniesXmlApi(xmlApiBaseUrl);
    }


    // The following bean was generated
    @Bean(name = "org.openapitools.OpenApiGeneratorApplication.jsonNullableModule")
    public Module jsonNullableModule() {
        return new JsonNullableModule();
    }

}