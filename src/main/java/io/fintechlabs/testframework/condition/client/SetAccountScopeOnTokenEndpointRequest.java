package io.fintechlabs.testframework.condition.client;

import com.google.gson.JsonObject;

import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PostEnvironment;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

/**
 * @author jricher
 *
 */
public class SetAccountScopeOnTokenEndpointRequest extends AbstractCondition {

	/**
	 * @param testId
	 * @param log
	 * @param conditionResultOnFailure
	 * @param requirements
	 */
	public SetAccountScopeOnTokenEndpointRequest(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = "token_endpoint_request_form_parameters")
	@PostEnvironment(required = "token_endpoint_request_form_parameters")
	public Environment evaluate(Environment env) {

		JsonObject tokenEndpointRequest = env.getObject("token_endpoint_request_form_parameters");

		// overwrite anyhing that's already there
		tokenEndpointRequest.addProperty("scope", "accounts");

		logSuccess("Set scope parameter to accounts for OB testing", tokenEndpointRequest);

		env.putObject("token_endpoint_request_form_parameters", tokenEndpointRequest);

		return env;

	}

}
