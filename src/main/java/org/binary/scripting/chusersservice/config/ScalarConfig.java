package org.binary.scripting.chusersservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class ScalarConfig {

    @Value("${springdoc.api-docs.path:/v3/api-docs}")
    private String apiDocsPath;

    @Bean
    public RouterFunction<ServerResponse> scalarRouterFunction() {
        return RouterFunctions.route()
                .GET("/scalar", request -> ServerResponse.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .bodyValue(generateScalarHtml()))
                .GET("/docs", request -> ServerResponse.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .bodyValue(generateScalarHtml()))
                .build();
    }

    private String generateScalarHtml() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Contractors Haven Users API</title>
                    <meta charset="utf-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1" />
                    <link rel="icon" type="image/svg+xml" href="https://scalar.com/favicon.svg" />
                </head>
                <body>
                    <script
                        id="api-reference"
                        data-url="%s"
                        data-configuration='{
                            "theme": "purple",
                            "layout": "modern",
                            "showSidebar": true,
                            "hideModels": false,
                            "hideDownloadButton": false,
                            "hideDarkModeToggle": false,
                            "darkMode": true
                        }'>
                    </script>
                    <script src="https://cdn.jsdelivr.net/npm/@scalar/api-reference"></script>
                </body>
                </html>
                """.formatted(apiDocsPath);
    }
}