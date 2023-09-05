package ua.com.obox.authserver.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Andrey Nemeritskyy",
                        email = "andrey.nemeritskyy@gmail.com",
                        url = "https://localhost:8080"
                ),
                description = "OpenApi documentation for Spring Security",
                title = "OpenApi specification - Obox",
                version = "1.0"
        ),
        servers = {
                @Server(
                        description = "Obox Dev",
                        url = "https://api.obox.pp.ua"
                ),
                @Server(
                        description = "Obox Prod",
                        url = "https://api.obox.com.ua"
                ),
                @Server(
                        description = "Local Server",
                        url = "http://localhost:8080"
                ),
        },
        security = {
                @SecurityRequirement(
                        name = "bearerAuth"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT auth description",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
