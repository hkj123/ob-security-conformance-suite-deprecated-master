package io.fintechlabs.testframework.condition.client;

import java.text.ParseException;

import com.google.gson.JsonObject;
import com.nimbusds.jose.PlainHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;

import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PostEnvironment;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

public class SerializeRequestObjectWithNullAlgorithm extends AbstractCondition {

	/**
	 * @param testId
	 * @param log
	 * @param optional
	 */
	public SerializeRequestObjectWithNullAlgorithm(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = "request_object_claims")
	@PostEnvironment(strings = "request_object")
	public Environment evaluate(Environment env) {

		JsonObject requestObjectClaims = env.getObject("request_object_claims");

		if (requestObjectClaims == null) {
			throw error("Couldn't find request object claims");
		}

		// FIXME: this processing should be handled in a separate condition
		if (!requestObjectClaims.has("iss")) {
			String clientId = env.getString("client", "client_id");
			if (clientId != null) {
				requestObjectClaims.addProperty("iss", clientId);
			} else {
				// Only a "should" requirement
				log("Request object contains no issuer and client ID not found");
			}
		}

		// FIXME: this processing should be handled in a separate condition
		if (!requestObjectClaims.has("aud")) {
			String serverIssuerUrl = env.getString("server", "issuer");
			if (serverIssuerUrl != null) {
				requestObjectClaims.addProperty("aud", serverIssuerUrl);
			} else {
				// Only a "should" requirement
				log("Request object contains no audience and server issuer URL not found");
			}
		}

		try {
			JWTClaimsSet claimSet = JWTClaimsSet.parse(requestObjectClaims.toString());

			PlainHeader header = new PlainHeader();

			PlainJWT requestObject = new PlainJWT(header, claimSet);

			env.putString("request_object", requestObject.serialize());

			logSuccess("Serialized the request object", args("request_object", requestObject.serialize()));

			return env;
		} catch (ParseException e) {
			throw error(e);
		}

	}

}
