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

/**
 * @author jricher
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ExtractMTLSCertificatesFromConfiguration_UnitTest {

	@Spy
	private Environment env = new Environment();

	@Mock
	private TestInstanceEventLog eventLog;

	private ExtractMTLSCertificatesFromConfiguration cond;

	private String cert = "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";

	private String certPEM = "-----BEGIN CERTIFICATE-----\\n" +
		"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\\n" +
		"-----END CERTIFICATE-----\\n";

	private String key = "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";

	private String keyPEM = "-----BEGIN PRIVATE KEY-----\\n" +
		"MIIJIgIBAAKCAf8xS7xubMKaFHLlXV09VdYMJdOVq7gg3WTkXfi21IoHGhAuvDEl\\n" +
		"Nz6vvhF11H/MK4nQRI6XmF0ERqIUnULjH8zFdt2LWLx9IDa47eD5FHintXK7k6w+\\n" +
		"0GXqc83wvpgao86ZtCax1ZCYOjbNUwd8R3B+SaqCRJFGWY6t/d6dpJv9W6AzOt7+\\n" +
		"+mujQfI3/HqQlT82PyOcY1eDWQFGLVw/pZahFLKFMMmFkRBtTJCI7xL2EkMN3NiB\\n" +
		"QmLeL2boFPzbv1nKuNM3CgAAiq7a7FJSJY2WrQQKJPFmojUCen0w7IXBusvI4THV\\n" +
		"qD7IFdQOI4VVHhenQWDMG3AelhehES9o6JukZ7v27aoBEU9WD4OoDkTqEQesufxF\\n" +
		"wSdGp0Th6Tq+nu461B8n1hNzb1OLtfl3LDVl2tPLDDeOe56t9K/a3Omxjlt+D5rl\\n" +
		"5wIbATw1rH/Q/JdTWle/7Nh00bQ0QdnETO5gwZnH6WWKnW0/OpC1TL14kjdwjGyT\\n" +
		"YnnsiMFR4Z353saQUc2hGWskLRlFpapFNuJS0NGM0HGosu2SovPsRmVjfDE8KlX1\\n" +
		"kUT0HTrfti0ppEdEiS3ebtocYz6KdH+/BCGKha6PgWadkJ1H6QrZEw07LwfSmIPW\\n" +
		"W/cU+qO+hON1j6GNPvecLxNeAsmTeGkDn6YsIxtjvd1CrCCDKrgxNfmzAgMBAAEC\\n" +
		"ggH/L1tRr0d+f6TrZ9sUiqreUZc5N4za+3+UwCpil8mAvCfWqf8Su2ziJNTUUz0M\\n" +
		"dKjS4SgrUAkIOuZcKK+XJThUcNKrLIXXteDvkSK9QKvg6URP71GDZGixr9UGX6PJ\\n" +
		"3bXF8TT3A1pmaUdrhD6ib0r2D+xXCIQ0h7/baNz9MraDQJb4RJ7mwU7zfsgImK5N\\n" +
		"VH58VnG7lS5+UOl9ZtyGxYIfPanzgi6HOnBMtqOaKmJ59bk/f57MwwhykH47PvOC\\n" +
		"otyltzFtf2905xBTwG7M+qum5LxbqB8rZWyovjPL9ucR7DW2NcnZJSdXAMKvj9Gh\\n" +
		"8k5RbNVK12n4gPmxw4MN55umdGucG1RBc1zwk9ArbFI3wxAQaq1zatCXce0CrzAh\\n" +
		"PbwEHCE4T/9WjFIhwXlMlsvzxSl6AiYYRDTXW9KGgNIJkCTcyXtO668cUJDJ/Ifu\\n" +
		"tIVV5deGEvZH/9uLvwxMzIc9bNBPR1kMma3XzFwuUqtQ+ZBO6/cO3SXE2+2onoz6\\n" +
		"fHDM/76vUGZ/z+0DuVFzGYPGML9Mmh1dvztDP0xfHNB6JeR31JQOZtVZ1tBU0Opq\\n" +
		"ejcsBClEXEo5J3RzaVHXKMb4+LUqYXEDgvelejB6wT0PGAwTRqCYrviZ4q2dRZrW\\n" +
		"IpeB2xs2bxQMF+xAn5QNGg7UZKOasYsPVkBJg+Y3C/rUaQKCAQAH3Vl0ba6GLbWw\\n" +
		"Al1tKhJ3Jj5JHv33yYZfAxQBTzmLmzVX8s33lo+xhSGrflbCDkh678j0qEzTjq2j\\n" +
		"y2ZVp2DRnYgwG4nJLEKbHsfOFLWDhldMWHQP4i2ZORKHTd3hXDOT6E+DLx/JAnwi\\n" +
		"mvHhB9QRv2uSJPqMVqPKdXUrSkLYMGyGSh0uUl8/Mi55TnaE0d7HbF0YBo2vtH/e\\n" +
		"mjgFAhORvGtEN06HgcqWBDy1nV1eNjhncvG+f1XmnAjCTsjbpkW27VRthcD/wAne\\n" +
		"p0qcCWzcFYVi8opXWiJ+OTBsVHzAUbYOz1xkpUllqRphuo8ng4aTQUN4fCs1xYp1\\n" +
		"YAfUyL63AoIBAAZEnaj+GkdM5auP3+23sV5B/slir2NLTfYvIN4/f7OSlSHRjQl9\\n" +
		"uGYq86trFYwK6fkqNYeE3PcPKfiXixPJO0SJBnrB/Z+NpHLnsZcyDv58Mnll+UK3\\n" +
		"0OEzgh+/gtN38ftF4UlGu76YMPH+hGqI6DoegHcWcMUvNjq44niQ86BWBng9bEuj\\n" +
		"PDC4htW8nS/Ia4BXvA8o4lsfN0aVVuIsdaxMJ19ppYBiLCFddssm0m9gxvC+1+JQ\\n" +
		"omTybPCe4iUxXCKEsBi5CSCMcA+R+NUDrBoThdlMoMl0/PTU9cMKTGkXupmjXbYb\\n" +
		"ZpXvqWFiyHPE2qhxfZjk2IG0xdi0kcAXoOUCggEAB9dUAfX9BEB8niCtf1JsGKlj\\n" +
		"fknNEoi1VPNftbKEgEFd3PLzEb/mQiqnGDGdVFsjPpblt7A48NCXJPB9djasHDHA\\n" +
		"/53lMVLUkY4NzdTt6FU/opmqFc/+gH7bj1U+PBtOPVBoPjX0rdexZlsvf5nrgUpl\\n" +
		"eM8vkk4rfYbALEoc/SjCet1X3MA5wGtK1J07I0+PmyraYkLebuk6d/kwkyWv1ySR\\n" +
		"mfC+dfIcxpcw5C4iUfUjJVj/11tjjMnTHc+pCP3tEeXrwEqT0ylnbbtDcvEevQsj\\n" +
		"8zQ4Y8E1FL32HnvZ6XFOX9O0nZAB7r670+ZKJi5HNXfjSjQabMEoO8Bj7m27XwKC\\n" +
		"AQABNsbmVRiX2J/e44W2T86Nd/C15mQbshkOZkBSay/7gpdZrnFHdk8Bkq4Q9DN/\\n" +
		"JRn9xRwK/EOjog8583ffRCkzc+qKWgoaHd/M1W0C4JIg3cMU2hg1wM4237gDGB9p\\n" +
		"f6ChTv58J7PzDRzlsarJy2xW3VN6PSFoP2WcZ/SM714QHrkwDo1r9NiSgxqySM6U\\n" +
		"05dmixeERCHbDiexhvkF4yCDV1iE1TxVqi3r5GM+o208Xx0IwZ2kWoOpU36f99XK\\n" +
		"6E107ggBMc0/vZNyoI32C7kIb+GLnZjCi+L2NEzJErSL4imk2gwrWhE7VuiSUQSL\\n" +
		"z4Od/iUaOLBqJq3u88IK10i1AoIBAAUXP9vGwZoEO4U69CN4J2cx6hvJx6bNapxV\\n" +
		"kyiryzRKFPeJJbNCckfV3vwTM+iw1mVfkWo1mIhGpI9fF4crWXwy2JNDZW7oZqvm\\n" +
		"JVPxNPULSyENeSDinYB/v5bUBlj8oaHoBooeVhd83eKsoOgPBUayw/n1ZFdmem7z\\n" +
		"YtIRyC0QFYgcbhW+OKc378lHfL3TEXynujkxx8tbiPuq9DyslPe5GOxjuT6qkbzJ\\n" +
		"0QYlFtpimaPU1AInrs0YKwSbne1ciBpmK7H/MWGZHJ9oKB6sPhWniq+yEVbYZAfV\\n" +
		"9DVB0Qhc4lBvLz5YXIlcMpnqZKTbydpgiMrgWouslxT3T0/6l3E=\\n" +
		"-----END PRIVATE KEY-----\\n";

	// OB issuer chain
	private String caPEM = "-----BEGIN CERTIFICATE-----\\n" +
		"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\\n" +
		"xbFBVdqAkgA7vg==\\n" +
		"-----END CERTIFICATE-----\\n" +
		"-----BEGIN CERTIFICATE-----\\n" +
		"MIIFaTCCA1GgAwIBAgIEWWdU0zANBgkqhkiG9w0BAQsFADBjMQswCQYDVQQGEwJH\\n" +
		"QjEdMBsGA1UEChMUT3BlbiBCYW5raW5nIExpbWl0ZWQxETAPBgNVBAsTCFRlc3Qg\\n" +
		"UEtJMSIwIAYDVQQDExlPcGVuIEJhbmtpbmcgVGVzdCBSb290IENBMB4XDTE3MDcx\\n" +
		"NjIwNTYzOVoXDTI3MDcxNjIxMjYzOVowZjELMAkGA1UEBhMCR0IxHTAbBgNVBAoT\\n" +
		"FE9wZW4gQmFua2luZyBMaW1pdGVkMREwDwYDVQQLEwhUZXN0IFBLSTElMCMGA1UE\\n" +
		"AxMcT3BlbiBCYW5raW5nIFRlc3QgSXNzdWluZyBDQTCCASIwDQYJKoZIhvcNAQEB\\n" +
		"BQADggEPADCCAQoCggEBAKRqnkNQSeADxIV58SNC/3B9A3GtggOoNIe8tbggVJ9S\\n" +
		"pGt4YR7tmn+ij/u6tKVauc4Za2d8RDw9aoh66r0emEMlcSxFM7PlAsQeQXLe2/Q9\\n" +
		"FBlRZojAcu4pynz8pDjEbcK94dcU61chctVf04idsBtQlRJqhsWdOzHoBueVh9JQ\\n" +
		"uDom5W0+4+uSC3YquuikQxFROYmOAz3qZRQXG6nXsw2ardMURjOlfHBMu/l2zJGF\\n" +
		"0G/hTFWvFGMB7g7JUtGfZfLngQ1Z/MolW6o3ct8NyxAJ675wsOLeP0LiFFLs6TG4\\n" +
		"oXUxSmlukDpL7mgcO/bXWNoGyMJiXeJMvMP0pVhlpZUCAwEAAaOCASAwggEcMA4G\\n" +
		"A1UdDwEB/wQEAwIBBjASBgNVHRMBAf8ECDAGAQH/AgEAMIG1BgNVHR8Ega0wgaow\\n" +
		"LKAqoCiGJmh0dHA6Ly9vYi50cnVzdGlzLmNvbS9vYnRlc3Ryb290Y2EuY3JsMHqg\\n" +
		"eKB2pHQwcjELMAkGA1UEBhMCR0IxHTAbBgNVBAoTFE9wZW4gQmFua2luZyBMaW1p\\n" +
		"dGVkMREwDwYDVQQLEwhUZXN0IFBLSTEiMCAGA1UEAxMZT3BlbiBCYW5raW5nIFRl\\n" +
		"c3QgUm9vdCBDQTENMAsGA1UEAxMEQ1JMMTAfBgNVHSMEGDAWgBTSYkPzbUv5C69Y\\n" +
		"LC0rqI01j9rYWTAdBgNVHQ4EFgQUDwHAL+hobPcjv45lbokNxqaFd7cwDQYJKoZI\\n" +
		"hvcNAQELBQADggIBADzs0nSe0yqgoxzp4/VC6/HkHUj02MjUB5cwqOmjr222XBZD\\n" +
		"Ga9AEzggszalhOKGPr+k6jYkrFIv/hc4qBSFaQyti+nihJWzHJulPs9RJynnArg3\\n" +
		"GtXJPkBtAH9bI94EajSTCWqWRNU1N5IY/fMRR+SzYPBW0L7qXG7yl+BgoMP7b0IS\\n" +
		"TjeBnh/2uYtWMYcVGUggid1h/0Fhu8oDcauMNyi+GwX5ba4EFthWXNLGF754Sh9p\\n" +
		"B3W6PbQ606atPrQ1Tt/MPC4PUs9uGA7s/XudqwsRIJ7HE9tLMxXwivRnNqwRc6F+\\n" +
		"a0bsjZhSCYlxHBGEjpSljP6s4eh5L33P6VQnDtIweuPZNKUPMnwPj3lRmm91/9Mf\\n" +
		"757PMJU4y/HpL80PLUGy/d43I96X6U8VeeLAiSsRDFlW9T9eTcIEG6exg6sMEeZr\\n" +
		"++L+Habc6AvvllkKK/4ZSIpDLSobm7YIj8Brvg5fBnpbBNx9PN4+LmrVBQ7R8vrN\\n" +
		"JFkpbvXfN+2bGv++2loAishhORug10yM1gSPtBhMUsx7on4/MqLSy0FdNpvLjJp5\\n" +
		"t7JUHCzID9uPdzVNDZlThN0YskTx9XX3TwqQoCeCXx4vscN0yciNbdJkNmilMJHn\\n" +
		"7+e2V9glTJo3xtFrQB+seQhlNMCop8VdC8tg1IDkk7GPXLhyjKejNgREN46o\\n" +
		"-----END CERTIFICATE-----\\n";

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		cond = new ExtractMTLSCertificatesFromConfiguration("UNIT-TEST", eventLog, ConditionResult.INFO);

	}

	/**
	 * Test method for {@link io.fintechlabs.testframework.condition.client.ExtractAuthorizationCodeFromAuthorizationResponse#evaluate(io.fintechlabs.testframework.testmodule.Environment)}.
	 */
	@Test
	public void testEvaluate_valuePresent() {

		JsonObject config = new JsonParser().parse("{\"mtls\":{"
			+ "\"cert\":\"" + cert + "\","
			+ "\"key\":\"" + key + "\""
			+ "}}").getAsJsonObject();

		env.putObject("config", config);

		cond.evaluate(env);

		verify(env, atLeastOnce()).getString("config", "mtls.cert");
		verify(env, atLeastOnce()).getString("config", "mtls.key");
		verify(env, atLeastOnce()).getString("config", "mtls.ca");

		assertThat(env.getString("mutual_tls_authentication", "cert")).isEqualTo(cert);
		assertThat(env.getString("mutual_tls_authentication", "key")).isEqualTo(key);
	}

	@Test
	public void testEvaluate_valuePresentPEM() {

		JsonObject config = new JsonParser().parse("{\"mtls\":{"
			+ "\"cert\":\"" + certPEM + "\","
			+ "\"key\":\"" + keyPEM + "\","
			+ "\"ca\":\"" + caPEM + "\""
			+ "}}").getAsJsonObject();

		env.putObject("config", config);

		cond.evaluate(env);

		verify(env, atLeastOnce()).getString("config", "mtls.cert");
		verify(env, atLeastOnce()).getString("config", "mtls.key");
		verify(env, atLeastOnce()).getString("config", "mtls.ca");

		assertThat(env.getString("mutual_tls_authentication", "cert")).isEqualTo(cert);
		assertThat(env.getString("mutual_tls_authentication", "key")).isEqualTo(key);
	}

	@Test(expected = ConditionError.class)
	public void testEvaluate_noKey() {

		JsonObject config = new JsonParser().parse("{\"mtls\":{"
			+ "\"cert\":\"" + cert + "\""
			+ "}}").getAsJsonObject();

		env.putObject("config", config);

		cond.evaluate(env);
	}

	@Test(expected = ConditionError.class)
	public void testEvaluate_badKey() {

		JsonObject config = new JsonParser().parse("{\"mtls\":{"
			+ "\"cert\":\"" + cert + "\","
			+ "\"key\":\"bad key value\""
			+ "}}").getAsJsonObject();

		env.putObject("config", config);

		cond.evaluate(env);
	}

	@Test(expected = ConditionError.class)
	public void testEvaluate_noCert() {

		JsonObject config = new JsonParser().parse("{\"mtls\":{"
			+ "\"key\":\"" + key + "\""
			+ "}}").getAsJsonObject();

		env.putObject("config", config);

		cond.evaluate(env);
	}

	@Test(expected = ConditionError.class)
	public void testEvaluate_badCert() {

		JsonObject config = new JsonParser().parse("{\"mtls\":{"
			+ "\"cert\":\"bad cert value\","
			+ "\"key\":\"" + key + "\""
			+ "}}").getAsJsonObject();

		env.putObject("config", config);

		cond.evaluate(env);
	}

	/**
	 * Test method for {@link io.fintechlabs.testframework.condition.client.ExtractAuthorizationCodeFromAuthorizationResponse#evaluate(io.fintechlabs.testframework.testmodule.Environment)}.
	 */
	@Test(expected = ConditionError.class)
	public void testEvaluate_valueMissing() {

		cond.evaluate(env);
	}

}
