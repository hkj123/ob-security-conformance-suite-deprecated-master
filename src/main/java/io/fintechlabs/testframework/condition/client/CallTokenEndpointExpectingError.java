package io.fintechlabs.testframework.condition.client;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

public class CallTokenEndpointExpectingError extends AbstractCondition {

	private static final Logger logger = LoggerFactory.getLogger(CallTokenEndpointExpectingError.class);

	/**
	 * @param testId
	 * @param log
	 */
	public CallTokenEndpointExpectingError(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = { "server", "token_endpoint_request_form_parameters" })
	public Environment evaluate(Environment env) {

		if (env.getString("server", "token_endpoint") == null) {
			throw error("Couldn't find token endpoint");
		}

		if (!env.containsObject("token_endpoint_request_form_parameters")) {
			throw error("Couldn't find request form");
		}

		// build up the form
		JsonObject formJson = env.getObject("token_endpoint_request_form_parameters");
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		for (String key : formJson.keySet()) {
			form.add(key, formJson.get(key).getAsString());
		}

		try {
			RestTemplate restTemplate = createRestTemplate(env);

			// extract the headers for use (below)
			HttpHeaders headers = headersFromJson(env.getObject("token_endpoint_request_headers"));

			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
			headers.setAcceptCharset(Collections.singletonList(Charset.forName("UTF-8")));

			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

			String jsonString = null;

			try {
				jsonString = restTemplate.postForObject(env.getString("server", "token_endpoint"), request, String.class);
			} catch (RestClientResponseException e) {

				logSuccess("Token endpoint returned error", args("code", e.getRawStatusCode(), "status", e.getStatusText(), "body", e.getResponseBodyAsString()));

				return env;
			} catch (ResourceAccessException e) {

				logSuccess("Token endpoint refused connection", args("message", e.getMessage()));

				return env;
			}

			if (Strings.isNullOrEmpty(jsonString)) {
				throw error("Empty response from the token endpoint");
			} else {
				log("Token endpoint response",
					args("token_endpoint_response", jsonString));

				try {
					JsonElement jsonRoot = new JsonParser().parse(jsonString);
					if (jsonRoot == null || !jsonRoot.isJsonObject()) {
						throw error("Token Endpoint did not return a JSON object");
					}

					JsonObject response = jsonRoot.getAsJsonObject();

					if (response.has("error") && !Strings.isNullOrEmpty(response.get("error").getAsString())) {
						logSuccess("Found error in token endpoint error response", response);
						return env;
					} else {
						throw error("No error from token endpoint", response);
					}

				} catch (JsonParseException e) {
					throw error(e);
				}
			}
		} catch (NoSuchAlgorithmException | KeyManagementException | CertificateException | InvalidKeySpecException | KeyStoreException | IOException | UnrecoverableKeyException e) {
			logger.warn("Error creating HTTP Client", e);
			throw error("Error creating HTTP Client", e);
		}

	}

}
