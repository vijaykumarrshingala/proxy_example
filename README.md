# proxy_example

openssl s_client -connect your.proxy.host:443 -showcerts </dev/null | openssl x509 -outform PEM > proxy.crt






keytool -import -alias proxy-cert -file proxy.crt -keystore truststore.jks -storepass changeit




import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "CustomAuthHeader";

        return new OpenAPI()
            .info(new Info()
                .title("Market Data API")
                .version("1.0")
                .description("API for fetching market data using custom token prefix."))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components().addSecuritySchemes(securitySchemeName,
                new SecurityScheme()
                    .name("Authorization")
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)
                    .description("Use prefix: customeprefix <your-token>")));
    }
}


