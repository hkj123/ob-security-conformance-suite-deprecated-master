package io.fintechlabs.testframework.condition.client;

import com.google.gson.JsonElement;

import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PostEnvironment;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

public class GetStaticClient2Configuration extends AbstractCondition {

	/**
	 * @param testId
	 * @param log
	 */
	public GetStaticClient2Configuration(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.testmodule.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = "config")
	@PostEnvironment(required = "client2")
	public Environment evaluate(Environment env) {
		if (!env.containsObject("config")) {
			throw error("Couldn't find a configuration");
		}

		// make sure we've got a client object
		JsonElement client = env.getElementFromObject("config", "client2");
		if (client == null || !client.isJsonObject()) {
			throw error("Definition for client2 not present in supplied configuration");
		} else {
			// we've got a client object, put it in the environment
			env.putObject("client2", client.getAsJsonObject());

			logSuccess("Found a static second client object", client.getAsJsonObject());
			return env;
		}
	}

}
