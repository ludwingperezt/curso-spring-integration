package dev.ludwing.prueba;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

class TestCreateUser {
	
	private final String CONTEXT_PATH = "/mobile-app-ws";

	@BeforeEach
	void setUp() throws Exception {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 8080;
		
	}

	/**
	 * Este es un test de integración para el endpoint de creación de usuarios.
	 */
	@Test
	void testCreateUser() {
		
		// En este bloque se definen los datos a ser enviados en la petición a la API
		List<Map<String, Object>> userAddresses = new ArrayList<>();
		
		Map<String, Object> shippingAddress = new HashMap<>();
		Map<String, Object> billingAddress = new HashMap<>();
		
		shippingAddress.put("city", "New York");
		shippingAddress.put("country", "US");
		shippingAddress.put("streetName", "Street XYZ");
		shippingAddress.put("postalCode", "1234");
		shippingAddress.put("type", "shipping");
		
		billingAddress.put("city", "New York");
		billingAddress.put("country", "US");
		billingAddress.put("streetName", "Street ABC");
		billingAddress.put("postalCode", "1234");
		billingAddress.put("type", "shipping");
		
		userAddresses.add(shippingAddress);
		userAddresses.add(billingAddress);
		
		Map<String, Object> userDetails = new HashMap<>();
		userDetails.put("firstName", "Ludwing");
		userDetails.put("lastName", "Perez");
		userDetails.put("email", "prueba@test.com");
		userDetails.put("password", "12345");
		userDetails.put("addresses", userAddresses);
		
		// Aquí se envía la petición y se procesa la respuesta
		Response response = given()
			.contentType("application/json")
			.accept("application/json")
			.body(userDetails)
			.when()
			.post(CONTEXT_PATH + "/users")
			.then()
			.statusCode(200)
			.contentType("application/json")
			.extract()
			.response()
			;
		
		// Verificar que el ID generado por la aplicación no es null y su longitud es de 30 caracteres
		String userId = response.jsonPath().getString("userId");
		assertNotNull(userId);
		assertTrue(userId.length() == 30);
		
		// Verificar los datos de las direcciones enviadas
		// Para ello se convierte la respuesta a un string el cual luego se 
		// convierte a un JSONObject del cual se van a extraer la lista de
		// direcciones.
		String bodyString = response.body().asString();
		try {
			JSONObject responseJson = new JSONObject(bodyString);
			
			JSONArray addresses = responseJson.getJSONArray("addresses");
			
			assertNotNull(addresses);
			assertTrue(addresses.length() == 2);  // Se compara con 1 porque se agregó solamente una dirección.
			
			String addressId = addresses.getJSONObject(0).getString("addressId");
			
			assertNotNull(addressId);
			assertTrue(addressId.length() == 30);
			
		} catch (JSONException e) {
			fail(e.getMessage());
		}
	}

}
