package io.fintechlabs.testframework.condition.client;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;

import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PostEnvironment;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

public class CreateAuthorizationEndpointRequestFromClientInformation extends AbstractCondition {

	public CreateAuthorizationEndpointRequestFromClientInformation(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	@Override
	@PreEnvironment(required = "client", strings = "redirect_uri" )
	@PostEnvironment(required = "authorization_endpoint_request")
	public Environment evaluate(Environment env) {

		String clientId = env.getString("client", "client_id");

		if (Strings.isNullOrEmpty(clientId)) {
			throw error("Couldn't find client ID");
		}

		String redirectUri = env.getString("redirect_uri");

		if (Strings.isNullOrEmpty(redirectUri)) {
			throw error("Couldn't find redirect URI");
		}

		JsonObject authorizationEndpointRequest = new JsonObject();

		authorizationEndpointRequest.addProperty("client_id", clientId);
		authorizationEndpointRequest.addProperty("redirect_uri", redirectUri);

		String scope = env.getString("client", "scope");
		if (!Strings.isNullOrEmpty(scope)) {
			authorizationEndpointRequest.addProperty("scope", scope);
		} else {
			log("Leaving off 'scope' parameter from authorization request");
		}

		env.putObject("authorization_endpoint_request", authorizationEndpointRequest);

		logSuccess("Created authorization endpoint request", authorizationEndpointRequest);

		return env;

	}

}
