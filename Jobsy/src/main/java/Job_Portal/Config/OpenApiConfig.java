package Job_Portal.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI jobsyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Jobsy API")
                        .description("API documentation for the Jobsy government job portal.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Jobsy")));
    }
}
