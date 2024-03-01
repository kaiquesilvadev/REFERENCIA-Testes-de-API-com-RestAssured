package com.devsuperior.dscommerce.controlles;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)//A anotação cria e inicializa o nosso ambiente de testes.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)//A anotação permite modificar o ciclo de vida da Classe de testes.
public class ProductControllerRA {

	
	@LocalServerPort
	private int port;
	
	private Long idExistente , idInexistente;
	private String buscaPorNome ;
	
	@BeforeAll
	void setUp() throws Exception {
		buscaPorNome = "Macbook";
		idExistente = 2l;
		idInexistente = 2000L;
		RestAssured.port = port;
		RestAssured.basePath = "/products";
	}
	
	@Test
	public void findByIdDeveRetornaCodigo200EProductDTOQuandoIdExstente() {
		
		RestAssured.given()
		    .accept(ContentType.JSON)
		.when()
			.get("/{id}" , idExistente)
		.then()
			.statusCode(HttpStatus.OK.value())
			.body("id" , is(idExistente.intValue()))
			.body("name", equalTo("Smart TV"))
			.body("price", is(2190.0F))
			.body("categories.id", hasItems(2 , 3))
			.body("categories.name", hasItems("Eletrônicos" , "Computadores"));
	}
	
	@Test
	public void findByIdDeveRetornaCodigo404QuandoIdInexistente() {
		
		RestAssured.given()
		    .accept(ContentType.JSON)
		.when()
			.get("/{id}" , idInexistente)
		.then()
			.statusCode(HttpStatus.NOT_FOUND.value());
	}
	
	@Test
	public void findAllPaginadaExibeListagemPaginadaQuandoCampoNomeNaoPreenchido() {
		
		RestAssured.given()
		    .accept(ContentType.JSON)
		.when()
			.get()
		.then()
			.statusCode(HttpStatus.OK.value())
			.body("content.name", hasItems("Macbook Pro" , "PC Gamer"));
	}
	
	@Test
	public void findAllPaginadaFiltraProdutosPorNomeEExibeListagemPaginadaQuandoCampoNomePreenchidos() {
		
		RestAssured.given()
	    	.accept(ContentType.JSON)
	    	.queryParam("name", buscaPorNome)
	    .when()
	    	.get()
	    .then()
	    	.statusCode(HttpStatus.OK.value())
	    	.body("content.id[0]", is(3))
	    	.body("content.name[0]", equalTo("Macbook Pro"));
			
	}
	
	@Test
	public void findAllPaginadaFiltraProdutoComPrecoMaiorQueDoisMil() {
		
		RestAssured.given()
		.queryParam("?size=25")
	    	.accept(ContentType.JSON)
	    .when()
	    	.get()
	    .then()
	    	.statusCode(HttpStatus.OK.value())
	    	.body("content.findAll { it.price > 2000.0}.name", hasItems("PC Gamer Hera"));
	
	}
}
