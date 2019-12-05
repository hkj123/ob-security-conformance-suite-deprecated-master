package io.fintechlabs.testframework.condition.common;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gson.JsonParser;

import io.fintechlabs.testframework.condition.Condition.ConditionResult;
import io.fintechlabs.testframework.condition.ConditionError;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

@RunWith(MockitoJUnitRunner.class)
public class CheckForKeyIdInServerJWKs_UnitTest {

	@Spy
	private Environment env = new Environment();

	@Mock
	private TestInstanceEventLog eventLog;

	private CheckForKeyIdInServerJWKs cond;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		cond = new CheckForKeyIdInServerJWKs("UNIT-TEST", eventLog, ConditionResult.INFO);
	}

	/**
	 * Test method for {@link CheckForKeyIdInServerJWKs#evaluate(io.fintechlabs.testframework.testmodule.Environment)}.
	 */
	@Test
	public void testEvaluate_noError() {

		env.putObject("server_jwks", new JsonParser().parse("{" +
			"  \"keys\": [" +
			"    {" +
			"      \"kty\": \"RSA\"," +
			"      \"d\": \"ePRRljk3VGde-JOxwH5LMAY4dERXTLCs7x1NcbuVXF7dyggrbQpDMhCrdH2UJQyvsb4c7H6w9LvosC7UnDgcTzrdd-rccz02XtiTCjP4mXqDUv1qZkYoy-HUq0UH7-WdRJNnx9sZTmWox-yucUbtfdmVRVuI355vNfG3ldsl6kC3DRNKH8nLay0eiS1x62p5S00Fz7TY_hzRqgexsjKu_UNQYDpQcOBVR9qi_mp3Yp8MdsJ2DoBrFJnDTTrG9uNlos1n1kBw-MVQQIPyHKIcElS5b1D-uc53MyxmWUAhiSnf0gpYgk3bYHccAmi_vsRqajLTt0w6hbNozhFht94mq3aLjGQJjt78zSu42hrUqTR-FLHwSoIGilS9SbdYypyYku0ocinwVb51IraDIwJY0_b42aHxE5an9HNaephGvPYPq0jA0xXehLSHqtS9D49OlxcnAvRkyn3TnPbxrdPAaZ6sJEHC8jpdYgKZ9LM7QRAFxwIk3Bxrf4rcEoC_mk5f5in4JYlYmx7VfXSzmSTXLfAWbqpLYRgtaCTFZ-HBLU1VJiAr2nkzw6cewwpxUCrowADO_w0GYRIU3BWamY2i--9wbI2nRUlxaROSMYJx5-A4sWDNPKJWQpX1SUyBpgTO82tHicZ2wzVEqMA4-np_leFBXFvAaMSuIxJ-aQlkLyE\","
			+
			"      \"e\": \"AQAB\"," +
			"      \"use\": \"sig\"," +
			"      \"kid\": \"fapi-test-suite\"," +
			"      \"alg\": \"RS256\"," +
			"      \"n\": \"qVGMeRQ8KG9ZVQFXa2YxCiapfesToXILzIvmHqNOJ8EbVwZKcwZ1iHpAzaZhnrSHJBU7Fgz9PEeTAqY-2FrRLE2xz3pW_LCg3y4fb1Sfi5GBSl1es0e4UD4Ie0g5SfQeZIpHfGM9zZx8rmsiDLRUyBsB8kHyUG6Ul5pHpAUHhrtghCpzL5lvnVvCM3-apeSyOdquS09tjwGUPUWifrKr0X1xEoNm7rsChZXQbB-MwqJG-ZGw51udn5-3mMunqp-B37jXdo4tYYOg0kKrpZSqQdRPANmTbge3LIN7uYprFVmGDMvXwiLMF9Unkeudox6nuIy3MWSeFRWpcrSQ_7s5p4mSupZxWHWrN_qSM-H9mTosvPg32m7XjQvlEJhuXqn13VYd8ZhmYSqV7s_x8bveZJLBnNPAkjPDktz8eUsdX3S8X6MNoYh4Ch7rsE_FO21B3FcMwtFql11z3_3PCAQOB3lzggy1hs-4Zb1As84R1Fmy0WKHTPAKPlJtLNFKz_bVf7xRbrt8BlPbkF4HduQixi-twkWRkUz8_-zCzWhz6s9y5ltQgs0_rl9aYSAt5z8UarP1PNfQIYGBmhwqGHi2LmrYFRjBEHRIsDXJcUGWgkfKMsiECs-mT-6AIw2L5DZIMIo8aroEnVpS24Ln2fwpdrRjO-b65VW-5cFOM6gERCc\""
			+
			"    }" +
			"  ]" +
			"}").getAsJsonObject());

		cond.evaluate(env);
	}

	/**
	 * Test method for {@link CheckForKeyIdInServerJWKs#evaluate(io.fintechlabs.testframework.testmodule.Environment)}.
	 */
	@Test(expected = ConditionError.class)
	public void testEvaluate_missingKeyId() {

		env.putObject("server_jwks", new JsonParser().parse("{" +
			"  \"keys\": [" +
			"    {" +
			"      \"kty\": \"RSA\"," +
			"      \"d\": \"ePRRljk3VGde-JOxwH5LMAY4dERXTLCs7x1NcbuVXF7dyggrbQpDMhCrdH2UJQyvsb4c7H6w9LvosC7UnDgcTzrdd-rccz02XtiTCjP4mXqDUv1qZkYoy-HUq0UH7-WdRJNnx9sZTmWox-yucUbtfdmVRVuI355vNfG3ldsl6kC3DRNKH8nLay0eiS1x62p5S00Fz7TY_hzRqgexsjKu_UNQYDpQcOBVR9qi_mp3Yp8MdsJ2DoBrFJnDTTrG9uNlos1n1kBw-MVQQIPyHKIcElS5b1D-uc53MyxmWUAhiSnf0gpYgk3bYHccAmi_vsRqajLTt0w6hbNozhFht94mq3aLjGQJjt78zSu42hrUqTR-FLHwSoIGilS9SbdYypyYku0ocinwVb51IraDIwJY0_b42aHxE5an9HNaephGvPYPq0jA0xXehLSHqtS9D49OlxcnAvRkyn3TnPbxrdPAaZ6sJEHC8jpdYgKZ9LM7QRAFxwIk3Bxrf4rcEoC_mk5f5in4JYlYmx7VfXSzmSTXLfAWbqpLYRgtaCTFZ-HBLU1VJiAr2nkzw6cewwpxUCrowADO_w0GYRIU3BWamY2i--9wbI2nRUlxaROSMYJx5-A4sWDNPKJWQpX1SUyBpgTO82tHicZ2wzVEqMA4-np_leFBXFvAaMSuIxJ-aQlkLyE\","
			+
			"      \"e\": \"AQAB\"," +
			"      \"use\": \"sig\"," +
			"      \"alg\": \"RS256\"," +
			"      \"n\": \"qVGMeRQ8KG9ZVQFXa2YxCiapfesToXILzIvmHqNOJ8EbVwZKcwZ1iHpAzaZhnrSHJBU7Fgz9PEeTAqY-2FrRLE2xz3pW_LCg3y4fb1Sfi5GBSl1es0e4UD4Ie0g5SfQeZIpHfGM9zZx8rmsiDLRUyBsB8kHyUG6Ul5pHpAUHhrtghCpzL5lvnVvCM3-apeSyOdquS09tjwGUPUWifrKr0X1xEoNm7rsChZXQbB-MwqJG-ZGw51udn5-3mMunqp-B37jXdo4tYYOg0kKrpZSqQdRPANmTbge3LIN7uYprFVmGDMvXwiLMF9Unkeudox6nuIy3MWSeFRWpcrSQ_7s5p4mSupZxWHWrN_qSM-H9mTosvPg32m7XjQvlEJhuXqn13VYd8ZhmYSqV7s_x8bveZJLBnNPAkjPDktz8eUsdX3S8X6MNoYh4Ch7rsE_FO21B3FcMwtFql11z3_3PCAQOB3lzggy1hs-4Zb1As84R1Fmy0WKHTPAKPlJtLNFKz_bVf7xRbrt8BlPbkF4HduQixi-twkWRkUz8_-zCzWhz6s9y5ltQgs0_rl9aYSAt5z8UarP1PNfQIYGBmhwqGHi2LmrYFRjBEHRIsDXJcUGWgkfKMsiECs-mT-6AIw2L5DZIMIo8aroEnVpS24Ln2fwpdrRjO-b65VW-5cFOM6gERCc\""
			+
			"    }" +
			"  ]" +
			"}").getAsJsonObject());

		cond.evaluate(env);
	}

}
