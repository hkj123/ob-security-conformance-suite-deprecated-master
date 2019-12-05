package io.fintechlabs.testframework.condition.client;

import com.google.gson.JsonObject;

import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

/**
 * @author ddrysdale
 *
 */


public class ValidateErrorResponseFromAuthorizationEndpoint extends AbstractCondition {

	public ValidateErrorResponseFromAuthorizationEndpoint(String testId, TestInstanceEventLog log,
														  ConditionResult conditionResultOnFailure,
														  String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	@Override
	@PreEnvironment(required = "authorization_endpoint_response")
	public Environment evaluate(Environment env) {

		JsonObject callbackParams = env.getObject("authorization_endpoint_response");
		int requiredParameterCount = 0;
		int optionalParameterCount = 0;

		// https://openid.net/specs/openid-connect-core-1_0.html#AuthError
		if (callbackParams.has("error")) {
			requiredParameterCount++;
			if (callbackParams.has("state")) {

				String stateCallback = callbackParams.get("state").getAsString();
				String stateSystem = env.getString("state");

				if (!stateCallback.equals(stateSystem)) {
					throw error("Error callback state value does not match system value",
							args("Expected", stateSystem, "Actual", stateCallback));
				} else {
					requiredParameterCount++;

					// Now, count the optional parameters
					if (callbackParams.has("error_description")) {
						optionalParameterCount++;
					}
					if (callbackParams.has("error_uri")) {
						optionalParameterCount++;
					}

					// Check the number of keys we've found, and can accept, against the total.
					if (callbackParams.keySet().size() - requiredParameterCount - optionalParameterCount == 0) {
						logSuccess("error response includes only expected parameters", callbackParams);
					} else {
						throw error("error response includes unexpected parameters", callbackParams);
					}
				}
			} else {
				throw error("No state parameter found", callbackParams);
			}
		} else {
			throw error("No error parameter found", callbackParams);
		}
		return env;
	}

}
