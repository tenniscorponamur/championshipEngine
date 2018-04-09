package be.company.fca;

import org.flywaydb.core.Flyway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.swagger.web.*;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * First class of Tennis Corpo Engine
 * Created by fca on 22-03-18.
 */

///@EnableOAuth2Sso
@SpringBootApplication
@EnableSwagger2
public class App {

    public static void main(String[] args) throws Exception{
//        Flyway flyway = new Flyway();
//        flyway.setDataSource("jdbc:postgresql://localhost:5432/tennisCorpo", "fca", "fca");
//        flyway.baseline();

        SpringApplication.run(App.class, args);
    }

    // localhost:9100/swagger-ui.html

    @Bean
    UiConfiguration uiConfig() {
        return UiConfigurationBuilder.builder()
                .deepLinking(true)
                .displayOperationId(false)
                .defaultModelsExpandDepth(1)
                .defaultModelExpandDepth(1)
                .defaultModelRendering(ModelRendering.EXAMPLE)
                .displayRequestDuration(false)
                .docExpansion(DocExpansion.NONE)
                .filter(true)
                .maxDisplayedTags(null)
                .operationsSorter(OperationsSorter.ALPHA)
                .showExtensions(false)
                .tagsSorter(TagsSorter.ALPHA)
                .validatorUrl(null)
                .build();
    }

}
