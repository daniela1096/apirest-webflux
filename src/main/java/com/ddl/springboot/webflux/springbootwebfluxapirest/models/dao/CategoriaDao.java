package com.ddl.springboot.webflux.springbootwebfluxapirest.models.dao;

import com.ddl.springboot.webflux.springbootwebfluxapirest.models.documents.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String> {

    public Mono<Categoria> findByNombre(String nombre);

}
