//package eu.h2020.symbiote.administration.config;
//
//
//import io.swagger.annotations.Info;
//import io.swagger.annotations.License;
//import io.swagger.v3.oas.models.ExternalDocumentation;
//import io.swagger.v3.oas.models.OpenAPI;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
////@EnableSwagger2
//public class SwaggerConfig {
//
//
////    @Bean
////    public Docket api() {
////
////        return new Docket(DocumentationType.SWAGGER_2)
////                .select()
////                .apis(RequestHandlerSelectors.basePackage("eu.h2020.symbiote.administration"))
////                .paths(PathSelectors.any())
////                .build()
////                .apiInfo(apiInfo());
////
////    }
////
////
////    ApiInfo apiInfo() {
////        return new ApiInfoBuilder()
////                .title("Administration")
////                .description("Administration component of SymbIoTe")
////                .version("1.0.0")
////                .build();
////    }
//
////    @Bean
////    public GroupedOpenApi publicApi() {
////        return GroupedOpenApi.builder()
////                .group("springshop-public")
////                .pathsToMatch("/public/**")
////                .build();
////    }
////    @Bean
////    public GroupedOpenApi adminApi() {
////        return GroupedOpenApi.builder()
////                .group("springshop-admin")
////                .pathsToMatch("/admin/**")
////                .addMethodFilter(method -> method.isAnnotationPresent(Admin.class))
////                .build();
////    }
//
//    @Bean
//    public OpenAPI springShopOpenAPI() {
//        return new OpenAPI()
//                .info(new Info().title("SpringShop API")
//                        .description("Spring shop sample application")
//                        .version("v0.0.1")
//                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
//                .externalDocs(new ExternalDocumentation()
//                        .description("SpringShop Wiki Documentation")
//                        .url("https://springshop.wiki.github.org/docs"));
//    }
//}
