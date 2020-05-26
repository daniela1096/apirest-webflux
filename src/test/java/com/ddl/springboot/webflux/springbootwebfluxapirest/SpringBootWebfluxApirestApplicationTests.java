package com.ddl.springboot.webflux.springbootwebfluxapirest;

import com.ddl.springboot.webflux.springbootwebfluxapirest.models.documents.Categoria;
import com.ddl.springboot.webflux.springbootwebfluxapirest.models.documents.Producto;
import com.ddl.springboot.webflux.springbootwebfluxapirest.models.services.ProductoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;
import java.util.List;

//@AutoConfigureWebTestClient
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringBootWebfluxApirestApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	ProductoService service;

	@Value("${config.base.endpoint}")
	private String url;

	@Test
	void listarTest() {

		client.get()
				.uri(url)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBodyList(Producto.class)
				.consumeWith(response -> {
					List<Producto> productos = response.getResponseBody();
					productos.forEach(p -> System.out.println(p.getNombre()));
					Assertions.assertTrue(productos.size() > 0);
					//assertThat(productos.size(), is(equalTo(9)));
				});
				//.hasSize(9);
	}


	@Test
	void verTest() {

		Producto producto = service.findByNombre("Sony Notebook").block();

		client.get()
				.uri(url.concat("/{id}"), Collections.singletonMap("id", producto.getId()))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(Producto.class)
				.consumeWith(response -> {
							Producto prod = response.getResponseBody();
							assertThat(prod.getNombre(), is(equalTo("Sony Notebook")));
							assertThat(prod.getId(), is(notNullValue()));
						});

//				.expectBody()
//				.jsonPath("$.id").isNotEmpty()
//				.jsonPath("$.nombre").isEqualTo("Apple iPod");
	}


	@Test
	void crearTest(){
		Categoria categoria = service.findByNombreCategoria("Muebles").block();
		Producto producto = new Producto("prueba test crear", 2933.43, categoria);
		client.post().uri(url)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(producto), Producto.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.nombre").isEqualTo("prueba test crear")
				.jsonPath("$.categoria.nombre").isEqualTo("Muebles");

	}


	@Test
	void crearForma2Test(){
		Categoria categoria = service.findByNombreCategoria("Muebles").block();
		Producto producto = new Producto("hola", 2933.43, categoria);
		client.post().uri(url)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(producto), Producto.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(Producto.class)
				.consumeWith(response -> {
					Producto p = response.getResponseBody();
					assertThat(p.getId(), is(notNullValue()));
					assertThat(p.getNombre(), is(equalTo("hola")));
					//assertThat(p.getCategoria(), is(equalTo("Muebles")));
					//Assertions.assertEquals("Muebles", p.getCategoria());
				});
	}

	@Test
	void editarTest(){
		Producto producto = service.findByNombre("Apple iPod").block();
		Categoria categoria = service.findByNombreCategoria("Muebles").block();
		Producto productoEditado = new Producto("holaEditado", 2933.43, categoria);

		client.put()
				.uri(url.concat("/{id}"), Collections.singletonMap("id", producto.getId()))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(productoEditado), Producto.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.nombre").isEqualTo("holaEditado")
				.jsonPath("$.categoria.nombre").isEqualTo("Muebles");
	}

	@Test
	void eliminarTest(){
		Producto producto = service.findByNombre("Sony Camara HD Digital").block();
		client.delete()
				.uri(url.concat("/{id}"), Collections.singletonMap("id", producto.getId()))
				.exchange()
				.expectStatus().isNoContent()
				.expectBody()
				.isEmpty();

		client.get()
				.uri(url.concat("/{id}"), Collections.singletonMap("id", producto.getId()))
				.exchange()
				.expectStatus().isNotFound()
				.expectBody()
				.isEmpty();
	}

	}
