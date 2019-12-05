package io.fintechlabs.testframework.condition.client;

import com.google.common.base.Strings;

import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PostEnvironment;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

/**
 * Creates a callback URL based on the base_url environment value
 *
 *
 * @author jricher
 *
 */
public class CreateRedirectUri extends AbstractCondition {

	/**
	 * @param testId
	 * @param log
	 */
	public CreateRedirectUri(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.testmodule.Condition#assertTrue(io.fintechlabs.testframework.testmodule.Environment, io.fintechlabs.testframework.logging.EventLog)
	 */
	@Override
	@PreEnvironment(strings = "base_url")
	@PostEnvironment(strings = "redirect_uri")
	public Environment evaluate(Environment in) {
		String baseUrl = in.getString("base_url");

		if (Strings.isNullOrEmpty(baseUrl)) {
			throw error("Base URL was null or empty");
		}

		String suffix = in.getString("redirect_uri_suffix");

		if (!Strings.isNullOrEmpty(suffix)) {
			log("Appending suffix to redirect URI", args("suffix", suffix));
		} else {
			suffix = "";
		}

		// calculate the redirect URI based on our given base URL
		String redirectUri = baseUrl + "/callback" + suffix;
		in.putString("redirect_uri", redirectUri);

		logSuccess("Created redirect URI",
			args("redirect_uri", redirectUri));

		return in;
	}

}
