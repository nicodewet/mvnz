package com.thorgil;

import com.fasterxml.jackson.databind.Module;
import com.thorgil.openapi.mwnz.xml.client.api.CompaniesXmlApi;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Choosing to NOT annotate the CompaniesApi companiesIdGet method with @CrossOrigin which would have the same effect as
                // our global configuration that we specify here:
                // 1. All origins are allowed (a request to the specific pathPattern can originate from any origin).
                // 2. For the path /v1/companies/* with the * meaning match zero or more characters
                //    (see org.springframework.util.AntPathMatcher for authoritative Ant-style path pattern documentation)
                // 3. The time that the preflight response is cached (maxAge) is 30 minutes.
                registry.addMapping("/v1/companies/*").allowedOrigins("*").maxAge(3600);
            }
        };
    }

    // The following bean was generated
    @Bean(name = "org.openapitools.OpenApiGeneratorApplication.jsonNullableModule")
    public Module jsonNullableModule() {
        return new JsonNullableModule();
    }

}