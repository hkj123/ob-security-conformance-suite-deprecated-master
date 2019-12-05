package io.fintechlabs.testframework.condition.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

/**
 * @author srmoore
 */
public class SetDynamicRegistrationRequestGrantTypeToImplicit extends AbstractCondition {

	public SetDynamicRegistrationRequestGrantTypeToImplicit(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	@Override
	@PreEnvironment(required = {"dynamic_registration_request"})
	public Environment evaluate(Environment env) {

		if (!env.containsObject("dynamic_registration_request")){
			throw error("No dynamic registration request object found");
		}
		JsonObject dynamicRegistrationRequest = env.getObject("dynamic_registration_request");
		JsonArray grantTypes = new JsonArray();
		grantTypes.add("implicit");
		dynamicRegistrationRequest.add("grant_types",grantTypes);
		env.putObject("dynamic_registration_request", dynamicRegistrationRequest);

		return env;
	}
}
