package io.fintechlabs.testframework.condition.client;

import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

import java.util.Objects;

// author: ddrysdale

public class CheckJwksUriIsHostedOnOpenBankingDirectory extends ValidateJsonUri {

	private static final String environmentVariable = "jwks_uri";
	private static final String requiredHostName = "keystore.openbanking.org.uk";

	public CheckJwksUriIsHostedOnOpenBankingDirectory(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	@Override
	@PreEnvironment(required = "server")
	public Environment evaluate(Environment env) {
		String hostName = requiredHostName;
		if(Objects.nonNull(env.getString("config", "server.hostName"))){
			hostName = env.getString("config", "server.hostName");
		}
		return validateWithHost(env, environmentVariable, hostName);
	}
}
