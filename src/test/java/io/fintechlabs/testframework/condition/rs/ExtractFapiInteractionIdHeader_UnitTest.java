package io.fintechlabs.testframework.condition.rs;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import static org.junit.Assert.assertEquals;

import io.fintechlabs.testframework.condition.Condition.ConditionResult;
import io.fintechlabs.testframework.condition.rs.ExtractFapiInteractionIdHeader;
import io.fintechlabs.testframework.condition.ConditionError;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

@RunWith(MockitoJUnitRunner.class)
public class ExtractFapiInteractionIdHeader_UnitTest {

	@Spy
	private Environment env = new Environment();

	@Mock
	private TestInstanceEventLog eventLog;

	private ExtractFapiInteractionIdHeader cond;

	private String id = "c770aef3-6784-41f7-8e0e-ff5f97bddb3a"; // example from FAPI spec
	private String altId = "this-is-an-arbitrary-string";

	private JsonObject goodRequest;
	private JsonObject altRequest;
	private JsonObject missingHeader;
	private JsonObject noHeaders;

	@Before
	public void setUp() throws Exception {

		cond = new ExtractFapiInteractionIdHeader("UNIT-TEST", eventLog, ConditionResult.INFO);

		goodRequest = new JsonParser().parse("{\"headers\":{"
			+ "\"x-fapi-interaction-id\": \"" + id + "\""
			+ "}}").getAsJsonObject();
		altRequest = new JsonParser().parse("{\"headers\":{"
			+ "\"x-fapi-interaction-id\": \"" + altId + "\""
			+ "}}").getAsJsonObject();
		missingHeader = new JsonParser().parse("{\"headers\":{}}").getAsJsonObject();
		noHeaders = new JsonParser().parse("{}").getAsJsonObject();

	}

	@Test
	public void test_good() {

		env.putObject("incoming_request", goodRequest);
		cond.evaluate(env);

		verify(env, atLeastOnce()).getString("incoming_request", "headers.x-fapi-interaction-id");
		assertEquals(id, env.getString("fapi_interaction_id"));
	}

	@Test
	public void test_alt() {

		env.putObject("incoming_request", altRequest);
		cond.evaluate(env);

		verify(env, atLeastOnce()).getString("incoming_request", "headers.x-fapi-interaction-id");
		assertEquals(altId, env.getString("fapi_interaction_id"));
	}

	@Test(expected = ConditionError.class)
	public void test_missing() {
		env.putObject("incoming_request", missingHeader);
		cond.evaluate(env);
	}

	@Test(expected = ConditionError.class)
	public void test_noHeader() {
		env.putObject("incoming_request", noHeaders);
		cond.evaluate(env);
	}

}
