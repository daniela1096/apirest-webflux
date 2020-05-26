package com.ddl.springboot.webflux.springbootwebfluxapirest.models.documents;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotEmpty;

@Setter
@Getter
@NoArgsConstructor
@Document(collection = "categorias")
public class Categoria {

	@Id
	@NotEmpty
	private String id;
	private String nombre;

	public Categoria(String nombre) {
		this.nombre = nombre;
	}

}
