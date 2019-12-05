package io.fintechlabs.testframework.condition.common;

import com.google.common.base.Strings;

import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

/**
 * @author jricher
 *
 */
public class EnsureIncomingTls12 extends AbstractCondition {

	private static final String TLS_12 = "TLSv1.2";

	/**
	 * @param testId
	 * @param log
	 * @param conditionResultOnFailure
	 * @param requirements
	 */
	public EnsureIncomingTls12(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = "client_request")
	public Environment evaluate(Environment env) {

		String protocol = env.getString("client_request", "headers.x-ssl-protocol");

		if (Strings.isNullOrEmpty(protocol)) {
			throw error("TLS Protocol not found");
		}

		if (protocol.equals(TLS_12)) {
			logSuccess("Found TLS 1.2 connection");
			return env;
		} else {
			throw error("Found disallowed TLS connection", args("expected", TLS_12, "actual", protocol));
		}

	}

}
