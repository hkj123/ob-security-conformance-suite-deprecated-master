package io.fintechlabs.testframework.condition.client;

import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

public class CheckTokenEndpointHttpStatus200 extends AbstractCondition {

	public CheckTokenEndpointHttpStatus200(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	@Override
//	@PreEnvironment(strings = "token_endpoint_response_http_status") - FIXME can't do this for an integer
	public Environment evaluate(Environment env) {
		int httpStatus = env.getInteger("token_endpoint_response_http_status");

		if (httpStatus != 200) {
			throw error("Invalid http status "+httpStatus);
		}

		logSuccess("Token endpoint http status code was 200");

		return env;
	}

}
