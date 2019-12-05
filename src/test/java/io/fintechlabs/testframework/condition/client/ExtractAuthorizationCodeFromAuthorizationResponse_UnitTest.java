package io.fintechlabs.testframework.condition.client;

import static org.assertj.core.api.Assertions.assertThat;

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

import io.fintechlabs.testframework.condition.Condition.ConditionResult;
import io.fintechlabs.testframework.condition.ConditionError;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

@RunWith(MockitoJUnitRunner.class)
public class ExtractAuthorizationCodeFromAuthorizationResponse_UnitTest {

	@Spy
	private Environment env = new Environment();

	@Mock
	private TestInstanceEventLog eventLog;

	private JsonObject callbackParams;

	private JsonObject callbackParamsWithoutCode;

	private ExtractAuthorizationCodeFromAuthorizationResponse cond;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		cond = new ExtractAuthorizationCodeFromAuthorizationResponse("UNIT-TEST", eventLog, ConditionResult.INFO);

		callbackParams = new JsonParser().parse("{"
			+ "\"code\":\"SplxlOBeZQQYbYS6WxSbIA\","
			+ "\"state\":\"xyz\""
			+ "}").getAsJsonObject();

		callbackParamsWithoutCode = new JsonParser().parse("{"
			+ "\"state\":\"xyz\""
			+ "}").getAsJsonObject();
	}

	/**
	 * Test method for {@link io.fintechlabs.testframework.condition.client.ExtractAuthorizationCodeFromAuthorizationResponse#evaluate(io.fintechlabs.testframework.testmodule.Environment)}.
	 */
	@Test
	public void testEvaluate_valuePresent() {

		env.putObject("callback_params", callbackParams);

		cond.evaluate(env);

		verify(env, atLeastOnce()).getString("callback_params", "code");

		assertThat(env.getString("code")).isEqualTo("SplxlOBeZQQYbYS6WxSbIA");
	}

	/**
	 * Test method for {@link io.fintechlabs.testframework.condition.client.ExtractAuthorizationCodeFromAuthorizationResponse#evaluate(io.fintechlabs.testframework.testmodule.Environment)}.
	 */
	@Test(expected = ConditionError.class)
	public void testEvaluate_valueMissing() {

		env.putObject("callback_params", callbackParamsWithoutCode);

		cond.evaluate(env);
	}

	/**
	 * Test method for {@link io.fintechlabs.testframework.condition.client.ExtractAuthorizationCodeFromAuthorizationResponse#evaluate(io.fintechlabs.testframework.testmodule.Environment)}.
	 */
	@Test(expected = ConditionError.class)
	public void testparamsMissing() {

		cond.evaluate(env);
	}
}
