package be4fe.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition(
		info = @io.swagger.v3.oas.annotations.info.Info(
				title = "PORTALE PROCONSUL-GROUP API",
				version = "v1",
				description = ""
		)
)
public class OpenApiConfig {

	@Bean
	public OpenAPI customOpenAPI() {

		return new OpenAPI()
				.info(new Info())
				.servers(List.of(
						new Server().url("http://localhost:8080")
				));
	}
}
