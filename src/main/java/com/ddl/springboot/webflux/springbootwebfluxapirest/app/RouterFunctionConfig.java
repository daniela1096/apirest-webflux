package com.ddl.springboot.webflux.springbootwebfluxapirest.app;

import com.ddl.springboot.webflux.springbootwebfluxapirest.app.handler.ProductoHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;


@Configuration
public class RouterFunctionConfig {

    @Value("${config.base.endpoint}")
    private String url;

    @Bean
    public RouterFunction<ServerResponse> routes(ProductoHandler handler){
        return route(GET(url), handler::listar)
                .andRoute(GET(url.concat("/{id}")), handler::ver)
                .andRoute(POST(url), handler::crear)
                .andRoute(PUT(url.concat("/{id}")), handler::editar)
                .andRoute(DELETE(url.concat("/{id}")), handler::eliminar)
                .andRoute(POST("/api/v2/productos/upload/{id}"), handler::upload)
                .andRoute(POST("/api/v2/productos/crearconfoto"), handler::crearConFoto);
    }
}
