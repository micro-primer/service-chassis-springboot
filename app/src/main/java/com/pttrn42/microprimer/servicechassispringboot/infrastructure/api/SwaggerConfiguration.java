package com.pttrn42.microprimer.servicechassispringboot.infrastructure.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import static java.util.Collections.emptyList;

@Configuration
class SwaggerConfiguration {

    @Bean
    Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .paths(PathSelectors.any())
                .build()
                .useDefaultResponseMessages(false)
                .apiInfo(new ApiInfo(
                        "service-chassis-springboot",
                        "This service provides an API for ...",
                        "",
                        "",
                        new Contact("", "", ""),
                        "",
                        "",
                        emptyList()
                ));
    }

    @Bean
    WebMvcConfigurer swaggerUiConfiguration(@Value("${springfox.documentation.swagger-ui.base-url}")  String swaggerUiUrl) {
        return new WebMvcConfigurer() {
            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                registry.addRedirectViewController(swaggerUiUrl + "/swagger-ui", swaggerUiUrl + "/swagger-ui/")
                        .setKeepQueryParams(true)
                        .setStatusCode(HttpStatus.PERMANENT_REDIRECT);
                registry.addViewController(swaggerUiUrl + "/swagger-ui/")
                        .setViewName("forward:" + swaggerUiUrl + "/swagger-ui/index.html");
            }
        };
    }
}
