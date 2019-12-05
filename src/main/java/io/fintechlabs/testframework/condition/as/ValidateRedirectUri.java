package io.fintechlabs.testframework.condition.as;

import com.google.common.base.Strings;

import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

/**
 * @author jricher
 *
 */
public class ValidateRedirectUri extends AbstractCondition {

	/**
	 * @param testId
	 * @param log
	 * @param optional
	 */
	public ValidateRedirectUri(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = { "client", "token_endpoint_request" })
	public Environment evaluate(Environment env) {

		String expected = env.getString("client", "redirect_uri");
		String actual = env.getString("token_endpoint_request", "params.redirect_uri");

		if (Strings.isNullOrEmpty(expected)) {
			throw error("Couldn't find redirect uri to compare");
		}

		if (expected.equals(actual)) {
			logSuccess("Found redirect uri", args("redirect_uri", actual));
			return env;
		} else {
			throw error("Didn't find matching redirect uri", args("expected", expected, "actual", actual));
		}

	}

}
