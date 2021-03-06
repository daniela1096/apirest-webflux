package com.ddl.springboot.webflux.springbootwebfluxapirest.app.handler;

import com.ddl.springboot.webflux.springbootwebfluxapirest.models.documents.Categoria;
import com.ddl.springboot.webflux.springbootwebfluxapirest.models.documents.Producto;
import com.ddl.springboot.webflux.springbootwebfluxapirest.models.services.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;

import static org.springframework.web.reactive.function.BodyInserters.*;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.beans.BeanProperty;
import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

@Component
public class ProductoHandler {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private Validator validator;

    public Mono<ServerResponse> listar(ServerRequest serverRequest) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productoService.findAll(), Producto.class);
    }

    public Mono<ServerResponse> ver(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        return productoService.findById(id)
                .flatMap(p -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue((p)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> crear(ServerRequest serverRequest) {
        Mono<Producto> producto = serverRequest.bodyToMono(Producto.class);
        return producto.flatMap(p -> {
            Errors errors = new BeanPropertyBindingResult(p, Producto.class.getName());
            validator.validate(p, errors);
            if (errors.hasErrors()) {
                return Flux.fromIterable(errors.getFieldErrors())
                        .map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
                        .collectList()
                        .flatMap(list -> ServerResponse.badRequest().bodyValue(list));
            } else {
                if (p.getCreateAt() == null) {
                    p.setCreateAt(new Date());
                }
                return productoService.save(p).flatMap(pdb -> ServerResponse.created(URI.create("/api/v2/productos/".concat(pdb.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(pdb));
            }

        });
    }


    public Mono<ServerResponse> editar(ServerRequest serverRequest) {
        Mono<Producto> producto = serverRequest.bodyToMono(Producto.class);
        String id = serverRequest.pathVariable("id");

        Mono<Producto> productoDb = productoService.findById(id);

        return productoDb.zipWith(producto, (db, request) -> {
            db.setNombre(request.getNombre());
            db.setPrecio(request.getPrecio());
            db.setCategoria(request.getCategoria());
            return db;
        }).flatMap(p -> ServerResponse.created(URI.create("/api/v2/productos/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(productoService.save(p), Producto.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> eliminar(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        Mono<Producto> productoDb = productoService.findById(id);

        return productoDb.flatMap(p -> productoService.delete(p)
                .then(ServerResponse.noContent().build()))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    @Value("${config.uploads.path}")
    private String path;

    public Mono<ServerResponse> upload(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        return serverRequest.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(file -> productoService.findById(id)
                        .flatMap(p -> {
                            p.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
                                    .replace(" ", "")
                                    .replace(":", "")
                                    .replace("\\", ""));
                            return file.transferTo(new File(path + p.getFoto()))
                                    .then(productoService.save(p));
                        })).flatMap(p -> ServerResponse.created(URI.create("/api/v2/productos/".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(p)).switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> crearConFoto(ServerRequest serverRequest) {
        Mono<Producto> producto = serverRequest.multipartData().map(multipart -> {
            FormFieldPart nombre = (FormFieldPart) multipart.toSingleValueMap().get("nombre");
            FormFieldPart precio = (FormFieldPart) multipart.toSingleValueMap().get("precio");
            FormFieldPart categoriaId = (FormFieldPart) multipart.toSingleValueMap().get("categoria.id");
            FormFieldPart categoriaNombre = (FormFieldPart) multipart.toSingleValueMap().get("categoria.nombre");

            Categoria categoria = new Categoria(categoriaNombre.value());
            categoria.setId(categoriaId.value());
            return new Producto(nombre.value(), Double.parseDouble(precio.value()), categoria);
        });

        return serverRequest.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(file -> producto
                        .flatMap(p -> {
                            p.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
                                    .replace(" ", "")
                                    .replace(":", "")
                                    .replace("\\", ""));

                            p.setCreateAt(new Date());

                            return file.transferTo(new File(path + p.getFoto()))
                                    .then(productoService.save(p));
                        })).flatMap(p -> ServerResponse.created(URI.create("/api/v2/productos/".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(p));
    }


}
