package com.devsuperior.dscommerce.controlles;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;

import com.devsuperior.dscommerce.util.TokenUtil;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)//A anotação cria e inicializa o nosso ambiente de testes.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)//A anotação permite modificar o ciclo de vida da Classe de testes.
public class OrderControllerRA {
	
	private String clientUsername, clientPassword, adminUsername, adminPassword;
	private String adminToken, clientToken, invalidToken;
	
	private Long idExistente , idInexistente , dependentProductId;

	@BeforeEach
	void setUp() throws Exception {
		
		clientUsername = "maria@gmail.com";
		clientPassword = "123456";
		adminUsername = "alex@gmail.com";
		adminPassword = "123456";
		
		clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
		adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
		invalidToken = adminToken + "xpto";
		
	
		idExistente = 2l;
		
		RestAssured.baseURI = "http://localhost:8080";
		RestAssured.basePath = "/orders";
	}
	
	@Test
	public void findByIdDeveRetornaCodigo200EOrdeDTOQuandoIdExstenteELogadoComoAdmin() {
		
		RestAssured.given()
			.header("Authorization", "Bearer " + adminToken)
		    .accept(ContentType.JSON)
		.when()
			.get("/{id}" , idExistente)
		.then()
			.statusCode(HttpStatus.OK.value());
	}
	
	@Test
	public void findByIdDeveRetornaCodigo200EOrdeDTOQuandoIdExstenteELogadoComoClientEPedidoPertencerAoMesmo() {
		idExistente = 1l;
		
		RestAssured.given()
			.header("Authorization", "Bearer " + clientToken)
		    .accept(ContentType.JSON)
		.when()
			.get("/{id}" , idExistente)
		.then()
			.statusCode(HttpStatus.OK.value());
	}

}
