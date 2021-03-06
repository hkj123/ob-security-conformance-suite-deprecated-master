/**
 * @author ddrysdale
 *
 */

package io.fintechlabs.testframework.condition.client;

import io.fintechlabs.testframework.condition.PostEnvironment;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

public class AddAcrScaClaimToAuthorizationEndpointRequest extends AbstractAddClaimToAuthorizationEndpointRequest {

	public AddAcrScaClaimToAuthorizationEndpointRequest(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	@Override
	@PreEnvironment(required = "authorization_endpoint_request")
	@PostEnvironment(required = "authorization_endpoint_request")
	public Environment evaluate(Environment env) {
		return addClaim(env, "acr", "urn:openbanking:psd2:sca", true);
	}

}
