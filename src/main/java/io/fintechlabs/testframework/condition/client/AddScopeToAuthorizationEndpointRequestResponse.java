package io.fintechlabs.testframework.condition.client;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PostEnvironment;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

public class AddScopeToAuthorizationEndpointRequestResponse extends AbstractCondition {

	public AddScopeToAuthorizationEndpointRequestResponse(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	@Override
	@PreEnvironment(required = { "authorization_endpoint_request", "client" } )
	@PostEnvironment(required = "authorization_endpoint_request")
	public Environment evaluate(Environment env) {
		JsonObject authorizationEndpointRequest = env.getObject("authorization_endpoint_request");

		String scope = env.getString("client", "scope");
		if (Strings.isNullOrEmpty(scope)) {
			throw error("scope missing/empty in client object");
		}

		authorizationEndpointRequest.addProperty("scope", scope);

		env.putObject("authorization_endpoint_request", authorizationEndpointRequest);

		logSuccess("Added scope of '"+scope+"' to authorization endpoint request", authorizationEndpointRequest);

		return env;
	}

}
