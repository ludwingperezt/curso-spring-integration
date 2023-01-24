package dev.ludwing.prueba;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * Para que este test corra sin problemas hay que correr los tests de TestCreateUser
 * de forma independiente, luego en la base de datos poner el campo email_verification_status
 * en true para el usuario recién creado y luego de eso ya se puede correr esta suite de
 * tests correctamente.
 * 
 * @author ludwingp
 *
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UsersWebServiceEndpointTest {
	
	private final String CONTEXT_PATH = "/mobile-app-ws";
	
	private static String authHeader;
	
	private static String userId;
	
	private final String EMAIL = "prueba@test.com";
	
	private final String PASSWORD = "12345";
	
	private static List<Map<String, String>> addresses;

	@BeforeEach
	void setUp() throws Exception {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 8080;
	}

	/**
	 * Este test comprueba el proceso de login.
	 * 
	 * Para que el token retornado por el servicio esté disponible para tests posteriores
	 * que requieren ese token de autenticación, este test debe correr primero y almacenar
	 * el token y el ID retornados en las cabeceras de la respuesta. Para ello es necesario
	 * usar la anotación @TestMethodOrder(MethodOrderer.OrderAnnotation.class) para indicar
	 * que la ejecución de los tests tiene un orden, y también la anotación @Order(1)
	 * que indica el orden en que se ejecutan los tests.
	 */
	@Test
	@Order(1)
	void testUserLogin() {
		
		Map<String, String> loginDetails = new HashMap<>();
		loginDetails.put("email", EMAIL);
		loginDetails.put("password", PASSWORD);
		
		Response response = given()
				.contentType("application/json")
				.accept("application/json")
				.body(loginDetails)
				.when()
				.post(CONTEXT_PATH + "/users/login")
				.then()
				.statusCode(200)
				.extract()
				.response()
				;
		
		authHeader = response.header("Authorization");
		userId = response.header("UserID");
		
		assertNotNull(authHeader);
		assertNotNull(userId);			
		
	}
	
	/**
	 * Test para comprobar que el endpoint de detalles de usuario funciona.
	 * Para este endpoint es necesario obtener un token de autenticación.
	 */
	@Test
	@Order(2)
	void testGetUserDetails() {
		Response response = given()
				.pathParam("userId", userId)  // Esto es para agregar un parámetro vía URL
				.header("Authorization", authHeader)
				.accept("application/json")
				.when()
				.get(CONTEXT_PATH + "/users/{userId}") // Esto es para tomar un pathParam y enviarlo en una parte de la URI
				.then()
				.statusCode(200)
				.contentType("application/json")
				.extract()
				.response()
				;
		
		String userPublicId = response.jsonPath().getString("userId");
		String userEmail = response.jsonPath().getString("email");
		String firstName = response.jsonPath().getString("firstName");
		String lastName = response.jsonPath().getString("lastName");
		
		addresses = response.jsonPath().getList("addresses");
		String addressId = addresses.get(0).get("addressId");
		
		assertNotNull(userPublicId);
		assertNotNull(userEmail);
		assertNotNull(firstName);
		assertNotNull(lastName);
		assertNotNull(addressId);
		assertEquals(EMAIL, userEmail);  // Verificar que el email retornado sea el mismo que el enviado en el login.
		
		assertTrue(addresses.size() == 2);  // El usuario testeado debe tener dos direcciones para que esta condición se cumpla
		assertTrue(addressId.length() == 30);  // Validar la longitud del ID de la primera dirección retornada por el servicio.	
	}
	
	@Test
	@Order(3)
	void testUpdateUserDetails() {
		Map<String, Object> userDetails = new HashMap<>();
		
		userDetails.put("firstName", "Juan H.");
		userDetails.put("lastName", "P. T.");
		
		Response response = given()
				.pathParam("userId", userId)  // Esto es para agregar un parámetro vía URL
				.header("Authorization", authHeader)
				.contentType("application/json")
				.accept("application/json")
				.body(userDetails)
				.when()
				.put(CONTEXT_PATH + "/users/{userId}") // Esto es para tomar un pathParam y enviarlo en una parte de la URI
				.then()
				.statusCode(200)
				.contentType("application/json")
				.extract()
				.response()
				;
		
		String firstName = response.jsonPath().getString("firstName");
		String lastName = response.jsonPath().getString("lastName");
		
		// Verificar que los datos solicitados fueron actualizados
		assertEquals(firstName, "Juan H.");
		assertEquals(lastName, "P. T.");
		
		List<Map<String, String>> storedAddresses = response.jsonPath().getList("addresses");
		
		assertNotNull(storedAddresses);
		// Verificar que las direcciones no han cambiado comparando el tamaño de la lista de direcciones originales
		// con las que retorna el endpoint de actualización.  Debido a que las direcciones originales se inicializan
		// al momento de hacer el test del endpoint GET de detalles de usuario y como los tests se ejecutan en un orden
		// determinado, por eso es posible hacer las comparaciones que se hacen aquí.
		assertTrue(addresses.size() == storedAddresses.size());
		assertEquals(addresses.get(0).get("streetName"), storedAddresses.get(0).get("streetName"));
	}
	
	@Test
	@Order(4)
	void testDeleteUserDetails() {
		Response response = given()
				.pathParam("userId", userId)  // Esto es para agregar un parámetro vía URL
				.header("Authorization", authHeader)
				.accept("application/json")
				.when()
				.delete(CONTEXT_PATH + "/users/{userId}") // Esto es para tomar un pathParam y enviarlo en una parte de la URI
				.then()
				.statusCode(200)
				.contentType("application/json")
				.extract()
				.response()
				;
		
		String operationResult = response.jsonPath().getString("operationResult");
		
		assertEquals(operationResult, "SUCCESS");
	}

}
