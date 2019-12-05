package io.fintechlabs.testframework.condition.as;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static org.junit.Assert.assertEquals;

import io.fintechlabs.testframework.condition.Condition.ConditionResult;
import io.fintechlabs.testframework.condition.ConditionError;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

@RunWith(MockitoJUnitRunner.class)
public class ExtractNonceFromAuthorizationRequest_UnitTest {

	@Spy
	private Environment env = new Environment();

	@Mock
	private TestInstanceEventLog eventLog;

	private ExtractNonceFromAuthorizationRequest cond;

	private String nonce = "123456";

	private JsonObject hasNonce;
	private JsonObject noNonce;
	private JsonObject onlyNonce;
	private JsonObject noParams;

	@Before
	public void setUp() throws Exception {

		cond = new ExtractNonceFromAuthorizationRequest("UNIT-TEST", eventLog, ConditionResult.INFO);

		hasNonce = new JsonParser().parse("{\"params\": " +
			"{\"nonce\": \"" + nonce + "\", \"state\": \"843192\"}" +
			"}").getAsJsonObject();
		noNonce = new JsonParser().parse("{\"params\": " +
			"{\"state\": \"843192\"}" +
			"}").getAsJsonObject();
		onlyNonce = new JsonParser().parse("{\"params\": " +
			"{\"nonce\": \"" + nonce + "\"}" +
			"}").getAsJsonObject();
		noParams = new JsonParser().parse("{}").getAsJsonObject();

	}

	@Test
	public void test_good() {

		env.putObject("authorization_endpoint_request", hasNonce);
		cond.evaluate(env);

		verify(env, atLeastOnce()).getObject("authorization_endpoint_request");
		verify(env, times(1)).putString("nonce", nonce);

		assertEquals(env.getString("nonce"), nonce);
	}

	@Test
	public void test_only() {

		env.putObject("authorization_endpoint_request", onlyNonce);
		cond.evaluate(env);

		verify(env, atLeastOnce()).getObject("authorization_endpoint_request");
		verify(env, times(1)).putString("nonce", nonce);

		assertEquals(env.getString("nonce"), nonce);

	}

	@Test(expected = ConditionError.class)
	public void test_bad() {

		env.putObject("authorization_endpoint_request", noNonce);
		cond.evaluate(env);

	}
	@Test(expected = ConditionError.class)
	public void test_missing() {

		env.putObject("authorization_endpoint_request", noParams);
		cond.evaluate(env);

	}
}
