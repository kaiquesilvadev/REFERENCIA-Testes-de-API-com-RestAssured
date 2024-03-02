package com.devsuperior.dscommerce.controlles;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import com.devsuperior.dscommerce.dto.ProductDTO;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.tests.ProductFactory;
import com.devsuperior.dscommerce.util.TokenUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)//A anotação cria e inicializa o nosso ambiente de testes.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)//A anotação permite modificar o ciclo de vida da Classe de testes.
public class ProductControllerRA {

	
	@LocalServerPort
	private int port;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private String clientUsername, clientPassword, adminUsername, adminPassword;
	private String adminToken, clientToken, invalidToken;
	
	private Product product;
	private ProductDTO productDTO;
	private Long idExistente , idInexistente;
	private String buscaPorNome ;
	
	@BeforeAll
	void setUp() throws Exception {
		
		clientUsername = "maria@gmail.com";
		clientPassword = "123456";
		adminUsername = "alex@gmail.com";
		adminPassword = "123456";
		
		clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
		adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
		invalidToken = adminToken + "xpto";
		
		product = ProductFactory.createProduct();
		
		buscaPorNome = "Macbook";
		idExistente = 2l;
		idInexistente = 2000L;
		RestAssured.baseURI = "http://localhost:8080";
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
	
	@Test
	public void insertDeProdutoInsereProdutoComDadosValidosQuandoLogadoComoAdmin() throws JsonProcessingException {
		String newProduct = objectMapper.writeValueAsString(new ProductDTO(product)); 
		
		
		RestAssured.given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.body(newProduct)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post()
		.then()
			.statusCode(201);
	}
	
	public void insertDeProdutoRetorna422QuandoLogadoComoAdminENomeInvalido() throws JsonProcessingException {
		
		product.setName("");
		String newProduct = objectMapper.writeValueAsString(new ProductDTO(product)); 
		
		
		RestAssured.given()
			.header("Content-type", "application/json")
			.header("Authorization", "Bearer " + adminToken)
			.body(newProduct)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.post()
		.then()
			.statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
	}
}
