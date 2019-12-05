package io.fintechlabs.testframework.condition.rs;

import java.net.InetAddress;

import com.google.common.base.Strings;
import com.google.common.net.InetAddresses;

import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PostEnvironment;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

/**
 * @author jricher
 *
 */
public class ExtractFapiIpAddressHeader extends AbstractCondition {

	/**
	 * @param testId
	 * @param log
	 * @param conditionResultOnFailure
	 * @param requirements
	 */
	public ExtractFapiIpAddressHeader(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = "incoming_request")
	@PostEnvironment(strings = "fapi_customer_ip_address")
	public Environment evaluate(Environment env) {

		String header = env.getString("incoming_request", "headers.x-fapi-customer-ip-address");
		if (Strings.isNullOrEmpty(header)) {
			throw error("Couldn't find FAPI ip address header");
		} else {

			// try to parse it to make sure it's in the right format
			try {

				InetAddress addr = InetAddresses.forString(header);

				env.putString("fapi_customer_ip_address", header);
				logSuccess("Found a FAPI ip address header", args("fapi_customer_ip_address", header, "addr", addr.getHostAddress()));

				return env;

			} catch (IllegalArgumentException e) {
				throw error("Could not parse FAPI ip address header", args("fapi_customer_ip_address", header));
			}

		}

	}

}
