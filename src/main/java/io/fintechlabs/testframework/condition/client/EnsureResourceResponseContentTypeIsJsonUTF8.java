package io.fintechlabs.testframework.condition.client;

import java.nio.charset.Charset;

import org.springframework.http.InvalidMediaTypeException;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.net.MediaType;

import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

public class EnsureResourceResponseContentTypeIsJsonUTF8 extends AbstractCondition {

	public EnsureResourceResponseContentTypeIsJsonUTF8(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	@Override
	@PreEnvironment(required = "resource_endpoint_response_headers")
	public Environment evaluate(Environment env) {

		String contentTypeStr = env.getString("resource_endpoint_response_headers", "content-type");

		if (!Strings.isNullOrEmpty(contentTypeStr)) {
			try {
				MediaType parsedType = MediaType.parse(contentTypeStr);

				Optional<Charset> charset = parsedType.charset();
				if (charset.isPresent()) {
					String charsetName = charset.get().name();
					if (charsetName.equals("UTF-8")) {
						logSuccess("Response charset is UTF-8", args("content_type", contentTypeStr));
					} else {
						throw error("Response charset is not UTF-8",
							args("content_type", contentTypeStr,
								"charset", charset.get().name()));
					}
				} else {
					throw error("Response charset not declared", (args("content_type", contentTypeStr)));
				}

				if (parsedType.is(MediaType.JSON_UTF_8)) {
					logSuccess("Response content type is JSON", args("content_type", contentTypeStr));
					return env;
				}
			} catch (InvalidMediaTypeException e) {
				throw error("Unable to parse content type", args("content_type", contentTypeStr));
			}
		}

		throw error("Resource endpoint did not declare a content type");
	}

}
