package io.fintechlabs.testframework.condition.client;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

import java.util.List;

public class EnsureInvalidRequestOrInvalidRequestObjectError extends AbstractCondition {

	private static final List<String> PERMITTED_ERRORS = ImmutableList.of("invalid_request", "invalid_request_object");

	public EnsureInvalidRequestOrInvalidRequestObjectError(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	@Override
	@PreEnvironment(required = "authorization_endpoint_response")
	public Environment evaluate(Environment env) {

		String error = env.getString("authorization_endpoint_response", "error");
		if (Strings.isNullOrEmpty(error)) {
			throw error("Permitted 'error' field not found");
		} else if (!PERMITTED_ERRORS.contains(error)) {
			throw error("'error' field has unexpected value", args("permitted", PERMITTED_ERRORS, "actual", error));
		}
		logSuccess("Authorization endpoint returned 'error'", args("permitted", PERMITTED_ERRORS, "error", error));
		return env;
	}
}
