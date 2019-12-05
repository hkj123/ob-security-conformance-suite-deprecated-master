package io.fintechlabs.testframework.condition.as;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.fintechlabs.testframework.condition.Condition.ConditionResult;
import io.fintechlabs.testframework.condition.ConditionError;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class FAPIValidateRequestObjectSigningAlg_UnitTest {

	@Spy
	private Environment env = new Environment();

	@Mock
	private TestInstanceEventLog eventLog;

	private FAPIValidateRequestObjectSigningAlg cond;

	private JsonObject header;


	@Before
	public void setUp() throws Exception {

		cond = new FAPIValidateRequestObjectSigningAlg("UNIT-TEST", eventLog, ConditionResult.INFO);

		header = new JsonParser().parse("{"
			+ "\"iss\":\"test-client-id-346334adgdsfgdfg3425\""
			+ "}").getAsJsonObject();
		header.addProperty("alg", "PS256");

	}

	private void addRequestObject(Environment env, JsonObject header) {
		JsonObject requestObject = new JsonObject();
		requestObject.getAsJsonObject().add("header", header);
		env.putObject("authorization_request_object", requestObject);
	}


	@Test
	public void testEvaluate_noError() {

		addRequestObject(env, header);

		cond.evaluate(env);

		verify(env, atLeastOnce()).getString("authorization_request_object", "header.alg");
	}


	@Test(expected = ConditionError.class)
	public void testEvaluate_invalidAlg() {

		header.remove("exp");
		header.addProperty("alg", "RS256");

		addRequestObject(env, header);

		cond.evaluate(env);

	}

}
