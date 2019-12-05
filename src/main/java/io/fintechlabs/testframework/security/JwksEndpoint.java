package io.fintechlabs.testframework.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Publish the public keys this server uses for signing.
 *
 * @author jricher
 *
 */
@Controller
public class JwksEndpoint {

	@Autowired
	private KeyManager keyManager;

	@GetMapping(value = "/jwks", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Object getJwkSet() {
		JsonObject jwks = new JsonParser().parse(keyManager.getPublicKeys().toString()).getAsJsonObject(); // put it into a GSON object

		return new ResponseEntity<Object>(jwks, HttpStatus.OK);
	}

}
