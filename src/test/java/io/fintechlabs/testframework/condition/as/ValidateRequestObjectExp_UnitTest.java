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
public class ValidateRequestObjectExp_UnitTest {

	@Spy
	private Environment env = new Environment();

	@Mock
	private TestInstanceEventLog eventLog;

	private ValidateRequestObjectExp cond;

	private JsonObject claims;

	private long nowSeconds;


	@Before
	public void setUp() throws Exception {

		cond = new ValidateRequestObjectExp("UNIT-TEST", eventLog, ConditionResult.INFO);

		Date now = new Date();
		nowSeconds = now.getTime() / 1000;
		long issuedAt = nowSeconds - 10; // pretend this came from a distant server


		claims = new JsonParser().parse("{"
			+ "\"iss\":\"test-client-id-346334adgdsfgdfg3425\""
			+ "}").getAsJsonObject();
		claims.addProperty("exp", issuedAt + 300);

	}

	private void addRequestObjectClaims(Environment env, JsonObject claims) {
		JsonObject requestObjectClaims = new JsonObject();
		requestObjectClaims.getAsJsonObject().add("claims", claims);
		env.putObject("authorization_request_object", requestObjectClaims);
	}


	@Test
	public void testEvaluate_noError() {

		addRequestObjectClaims(env, claims);

		cond.evaluate(env);

		verify(env, atLeastOnce()).getLong("authorization_request_object", "claims.exp");
	}

	@Test(expected = ConditionError.class)
	public void testEvaluate_missingExp() {

		claims.remove("exp");

		addRequestObjectClaims(env, claims);

		cond.evaluate(env);

	}

	@Test(expected = ConditionError.class)
	public void testEvaluate_invalidExp() {

		claims.remove("exp");
		claims.addProperty("exp", nowSeconds - (5 * 60)); // 5 minutes in the past is not ok

		addRequestObjectClaims(env, claims);

		cond.evaluate(env);

	}

	@Test
	public void testEvaluate_allowableExpSkew() {

		claims.remove("exp");
		claims.addProperty("exp", nowSeconds - (4 * 60)); // 4 minutes out should be fine still

		addRequestObjectClaims(env, claims);

		cond.evaluate(env);

	}

	@Test
	public void testEvaluate_validExpFuture() {

		claims.remove("exp");
		claims.addProperty("exp", nowSeconds + (60 * 60 * 23)); // exp claim time range has not expired (23 hours)

		addRequestObjectClaims(env, claims);

		cond.evaluate(env);

	}

	@Test(expected = ConditionError.class)
	public void testEvaluate_invalidExpFuture() {

		claims.remove("exp");
		claims.addProperty("exp", nowSeconds + (60 * 60 * 25)); // exp claim time range has expired (25 hours)

		addRequestObjectClaims(env, claims);

		cond.evaluate(env);

	}

}
