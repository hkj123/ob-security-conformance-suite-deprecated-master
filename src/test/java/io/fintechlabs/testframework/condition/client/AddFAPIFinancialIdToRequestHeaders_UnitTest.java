package io.fintechlabs.testframework.condition.client;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import io.fintechlabs.testframework.condition.Condition.ConditionResult;
import io.fintechlabs.testframework.condition.ConditionError;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

@RunWith(MockitoJUnitRunner.class)
public class AddFAPIFinancialIdToRequestHeaders_UnitTest {

	@Spy
	private Environment env = new Environment();

	@Mock
	private TestInstanceEventLog eventLog;

	private AddFAPIFinancialIdToResourceEndpointRequest cond;

	private JsonObject client;

	private String financialId;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		cond = new AddFAPIFinancialIdToResourceEndpointRequest("UNIT-TEST", eventLog, ConditionResult.INFO);


		// create a random financial ID so we know it's getting copied
		financialId = RandomStringUtils.randomAlphanumeric(30);

		client = new JsonObject();
		client.addProperty("fapi_financial_id", financialId);


	}

	/**
	 */
	@Test
	public void testEvaluate() {

		env.putObject("client", client);

		cond.evaluate(env);

		JsonObject req = env.getObject("resource_endpoint_request_headers");

		assertNotNull(req);
		assertTrue(req.has("x-fapi-financial-id"));
		assertEquals(financialId, req.get("x-fapi-financial-id").getAsString());

	}

	@Test
	public void testEvaluate_existingHeaders() {

		env.putObject("client", client);
		env.putObject("resource_endpoint_request_headers",	new JsonObject());

		cond.evaluate(env);

		JsonObject req = env.getObject("resource_endpoint_request_headers");

		assertNotNull(req);
		assertTrue(req.has("x-fapi-financial-id"));
		assertEquals(financialId, req.get("x-fapi-financial-id").getAsString());

	}

	@Test
	public void testEvaluate_existingHeadersOverwrite() {

		env.putObject("client", client);
		env.putObject("resource_endpoint_request_headers",	new JsonParser().parse("{\"x-fapi-financial-id\":\"foo-bar\"}").getAsJsonObject());

		cond.evaluate(env);

		JsonObject req = env.getObject("resource_endpoint_request_headers");

		assertNotNull(req);
		assertTrue(req.has("x-fapi-financial-id"));
		assertEquals(financialId, req.get("x-fapi-financial-id").getAsString());

	}


	@Test(expected = ConditionError.class)
	public void testEvaluate_noFinancialId() {

		env.putObject("client", new JsonObject());

		cond.evaluate(env);

	}
}
