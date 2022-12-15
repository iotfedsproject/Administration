package eu.h2020.symbiote.administration.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {


    @Bean
    public Docket api() {

        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("eu.h2020.symbiote.administration"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());

    }


    ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Administration")
                .description("Administration component of SymbIoTe")
                .version("1.0.0")
                .build();
    }
}
