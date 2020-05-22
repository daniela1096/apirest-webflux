package com.ddl.springboot.webflux.springbootwebfluxapirest.models.dao;

import com.ddl.springboot.webflux.springbootwebfluxapirest.models.documents.Producto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductoDao extends ReactiveMongoRepository<Producto, String> {

}
