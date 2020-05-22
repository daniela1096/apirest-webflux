package com.ddl.springboot.webflux.springbootwebfluxapirest.models.dao;

import com.ddl.springboot.webflux.springbootwebfluxapirest.models.documents.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String> {

}
