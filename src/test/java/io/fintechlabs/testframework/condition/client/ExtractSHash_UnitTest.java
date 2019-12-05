package io.fintechlabs.testframework.condition.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gson.JsonObject;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import io.fintechlabs.testframework.condition.Condition.ConditionResult;
import io.fintechlabs.testframework.condition.ConditionError;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

/**
 * @author jricher
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ExtractSHash_UnitTest {

	@Spy
	private Environment env = new Environment();

	@Mock
	private TestInstanceEventLog eventLog;

	private ExtractSHash cond;

	/*
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		cond = new ExtractSHash("UNIT-TEST", eventLog, ConditionResult.INFO);

	}

	private void addIdToken(Environment env, String alg, String stateHash) {

		JsonObject header = new JsonObject();
		header.addProperty("alg", alg);

		JsonObject claims = new JsonObject();
		claims.addProperty("s_hash", stateHash);

		JsonObject idToken = new JsonObject();
		idToken.add("header", header);
		idToken.add("claims", claims);

		env.putObject("id_token", idToken);

	}

	/**
	 * Test method for {@link io.fintechlabs.testframework.condition.client.ValidateSHash#evaluate(io.fintechlabs.testframework.testmodule.Environment)}.
	 */
	@Test
	public void testEvaluate_noError() {

		env.putString("state", "12345");
		addIdToken(env, "HS256", "WZRHGrsBESr8wYFZ9sx0tA");

		cond.evaluate(env);

		verify(env, atLeastOnce()).getString("id_token", "claims.s_hash");
		verify(env, atLeastOnce()).getString("id_token", "header.alg");

		assertThat(env.getString("s_hash", "s_hash")).isEqualTo("WZRHGrsBESr8wYFZ9sx0tA");
		assertThat(env.getString("s_hash", "alg")).isEqualTo("HS256");

	}

	/**
	 * Test method for {@link io.fintechlabs.testframework.condition.client.ValidateSHash#evaluate(io.fintechlabs.testframework.testmodule.Environment)}.
	 */
	@Test(expected = ConditionError.class)
	public void testEvaluate_missingIdToken() {

		env.putString("state", "12345");

		cond.evaluate(env);
	}

	/**
	 * Test method for {@link io.fintechlabs.testframework.condition.client.ValidateSHash#evaluate(io.fintechlabs.testframework.testmodule.Environment)}.
	 */
	@Test(expected = ConditionError.class)
	public void testEvaluate_missingHash() {

		env.putString("state", "12345");
		addIdToken(env, "HS256", null);

		cond.evaluate(env);
	}

	/**
	 * Test method for {@link io.fintechlabs.testframework.condition.client.ValidateSHash#evaluate(io.fintechlabs.testframework.testmodule.Environment)}.
	 */
	@Test(expected = ConditionError.class)
	public void testEvaluate_missingAlg() {

		env.putString("state", "12345");
		addIdToken(env, null, "WZRHGrsBESr8wYFZ9sx0tA");

		cond.evaluate(env);
	}

}
