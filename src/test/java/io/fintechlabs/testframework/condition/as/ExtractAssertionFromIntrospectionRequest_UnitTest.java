package io.fintechlabs.testframework.condition.as;

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

import io.fintechlabs.testframework.condition.Condition.ConditionResult;
import io.fintechlabs.testframework.condition.ConditionError;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

@RunWith(MockitoJUnitRunner.class)
public class ExtractAssertionFromIntrospectionRequest_UnitTest {

	@Spy
	private Environment env = new Environment();

	@Mock
	private TestInstanceEventLog eventLog;

	private JsonObject request;

	private JsonObject header;

	private JsonObject payload;

	private ExtractAssertionFromIntrospectionRequest cond;

	private String assertionType;

	private String assertion;


	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		cond = new ExtractAssertionFromIntrospectionRequest("UNIT-TEST", eventLog, ConditionResult.INFO);

		assertionType = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

		assertion = "eyJraWQiOiJyZXNvdXJjZS1rZXkiLCJhbGciOiJSUzI1NiIsInR"
			+ "5cCI6IkpXVCJ9.eyJzdWIiOiJwcm90ZWN0ZWQtcmVzb3VyY2UtMSIsImlzcyI6InByb3Rl"
			+ "Y3RlZC1yZXNvdXJjZS0xIiwiYXVkIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6ODQ0My90ZXN0L2"
			+ "EvcnMtdGVzdC8iLCJleHAiOjE1MjY2ODI1MTYsImlhdCI6MTUyNjY4MjIxNiwianRpIjoi"
			+ "eWhiMWtyM002d1hSVzF3c2VSTWFFaVNJS0tsejNrczIifQ.ETixtUTR0A8mW7TcSl759fH"
			+ "hAogor9vQjduzx3sQ8mc8_Yr2xkw0Z0XJSKbRgHOPIttJNaKUTZD-iFG-1TWGqWuIiqeFR"
			+ "2JBymUMEwh9uSvCKWqqk0WtlAZYLJW8iDbmEqpUbTtmOLr_Iz_6PLd-WPKYsqb8NMMp9ay"
			+ "UY9iFRwaJBYK77Pf9QiRY2NotAlKw0oNYvIb10LscQue_DKbMLZdhU73eggSWBev_FbAm2"
			+ "Yt9Q2Uvk_tG1AsZA8IZdfZQCLZ4jCPHWWytcKM0w1Ygv625bQkwJKv-zZ5zaOi9855gdt5"
			+ "t1LuwDQfmswGdQ4U_8LjJUXaEIQ-ZFXvMlcI5HQ";

		header = new JsonParser().parse("{\n" +
			"  \"kid\": \"resource-key\",\n" +
			"  \"alg\": \"RS256\",\n" +
			"  \"typ\": \"JWT\"\n" +
			"}").getAsJsonObject();

		payload = new JsonParser().parse("{\n" +
			"  \"sub\": \"protected-resource-1\",\n" +
			"  \"iss\": \"protected-resource-1\",\n" +
			"  \"aud\": \"https://localhost:8443/test/a/rs-test/\",\n" +
			"  \"exp\": 1526682516,\n" +
			"  \"iat\": 1526682216,\n" +
			"  \"jti\": \"yhb1kr3M6wXRW1wseRMaEiSIKKlz3ks2\"\n" +
			"}").getAsJsonObject();

	}

	/**
	 * Test method for {@link io.fintechlabs.testframework.condition.as.ExtractClientCertificateFromRequestHeaders#evaluate(io.fintechlabs.testframework.testmodule.Environment)}.
	 */
	@Test
	public void testEvaluate() {

		request = new JsonParser().parse("{\n" +
			"	\"params\": {\n" +
			"		\"client_assertion\": \"" + assertion + "\",\n" +
			"		\"client_assertion_type\": \"" + assertionType +"\"\n" +
			"	}\n" +
			"}").getAsJsonObject();

		env.putObject("introspection_request", request);

		cond.evaluate(env);

		JsonObject res = env.getObject("resource_assertion");

		assertNotNull(res);
		assertEquals(assertion, res.get("assertion").getAsString());
		assertEquals(assertionType, res.get("assertion_type").getAsString());
		assertEquals(header, res.get("assertion_header").getAsJsonObject());
		assertEquals(payload, res.get("assertion_payload").getAsJsonObject());
	}

	/**
	 * Test method for {@link io.fintechlabs.testframework.condition.as.ExtractClientCertificateFromRequestHeaders#evaluate(io.fintechlabs.testframework.testmodule.Environment)}.
	 */
	@Test(expected = ConditionError.class)
	public void testEvaluate_valueMissing() {

		request = new JsonParser().parse("{\n" +
			"	\"params\": {\n" +
			"	}\n" +
			"}").getAsJsonObject();

		cond.evaluate(env);

	}

	@Test(expected = ConditionError.class)
	public void testEvaluate_paramMissing() {

		request = new JsonParser().parse("{\n" +
			"}").getAsJsonObject();

		cond.evaluate(env);
	}

	@Test(expected = ConditionError.class)
	public void testEvaluate_assertionMissing() {

		request = new JsonParser().parse("{\n" +
			"	\"params\": {\n" +
			"		\"client_assertion_type\": \"" + assertionType +"\"\n" +
			"	}\n" +
			"}").getAsJsonObject();

		cond.evaluate(env);

	}
	@Test(expected = ConditionError.class)
	public void testEvaluate_assertionEmpty() {

		request = new JsonParser().parse("{\n" +
			"	\"params\": {\n" +
			"		\"client_assertion\": \"\",\n" +
			"		\"client_assertion_type\": \"" + assertionType +"\"\n" +
			"	}\n" +
			"}").getAsJsonObject();

		cond.evaluate(env);
	}

	@Test(expected = ConditionError.class)
	public void testEvaluate_typeMissing() {

		request = new JsonParser().parse("{\n" +
			"	\"params\": {\n" +
			"		\"client_assertion\": \"" + assertion + "\"\n" +
			"	}\n" +
			"}").getAsJsonObject();

		cond.evaluate(env);

	}

	@Test(expected = ConditionError.class)
	public void testEvaluate_typeEmpty() {

		request = new JsonParser().parse("{\n" +
			"	\"params\": {\n" +
			"		\"client_assertion\": \"" + assertion + "\",\n" +
			"		\"client_assertion_type\": \"\"\n" +
			"	}\n" +
			"}").getAsJsonObject();

		cond.evaluate(env);

	}

	@Test(expected = ConditionError.class)
	public void testEvaluate_assertionMalformed() {

		request = new JsonParser().parse("{\n" +
			"	\"params\": {\n" +
			"		\"client_assertion\": \"abcdef\",\n" +
			"		\"client_assertion_type\": \"" + assertionType +"\"\n" +
			"	}\n" +
			"}").getAsJsonObject();

		cond.evaluate(env);

	}

	@Test(expected = ConditionError.class)
	public void testEvaluate_wrongType() {

		request = new JsonParser().parse("{\n" +
			"	\"params\": {\n" +
			"		\"client_assertion\": \"" + assertion + "\",\n" +
			"		\"client_assertion_type\": \"batman\"\n" +
			"	}\n" +
			"}").getAsJsonObject();

		cond.evaluate(env);
	}
}
