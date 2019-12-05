package io.fintechlabs.testframework.condition.rs;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;

import io.fintechlabs.testframework.condition.PostEnvironment;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

/**
 * @author jricher
 *
 */
public class CreateOpenBankingAccountRequestResponse extends AbstractOpenBankingApiResponse {

	/**
	 * @param testId
	 * @param log
	 * @param conditionResultOnFailure
	 * @param requirements
	 */
	public CreateOpenBankingAccountRequestResponse(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(strings = {"account_request_id", "fapi_interaction_id"})
	@PostEnvironment(required = {"account_request_response", "account_request_response_headers"})
	public Environment evaluate(Environment env) {

		String accountRequestId = env.getString("account_request_id");

		JsonObject data = new JsonObject();
		data.addProperty("Status", "AwaitingAuthorisation");
		data.addProperty("AccountRequestId", accountRequestId);

		JsonObject response = createResponse(data);

		String fapiInteractionId = env.getString("fapi_interaction_id");
		if (Strings.isNullOrEmpty(fapiInteractionId)) {
			throw error("Couldn't find FAPI Interaction ID");
		}

		JsonObject headers = new JsonObject();
		headers.addProperty("x-fapi-interaction-id", fapiInteractionId);

		logSuccess("Created account request response", args("account_request_response", response, "account_request_response_headers", headers));

		env.putObject("account_request_response", response);
		env.putObject("account_request_response_headers", headers);

		return env;

	}

}
