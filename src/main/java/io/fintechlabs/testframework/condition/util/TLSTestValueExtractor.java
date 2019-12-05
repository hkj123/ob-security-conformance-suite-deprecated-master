package io.fintechlabs.testframework.condition.util;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.JsonObject;

/**
 * Extracts the host and port of a given URL string (including the default port if none is
 * specified) into a JSON object, for use with TLS testing.
 *
 * @author jricher
 *
 */
public class TLSTestValueExtractor {

	public static JsonObject extractTlsFromUrl(String urlString) throws MalformedURLException {
		URL url = new URL(urlString);
		JsonObject tls = new JsonObject();
		tls.addProperty("testHost", url.getHost());
		tls.addProperty("testPort", url.getPort() > 0 ? url.getPort() : url.getDefaultPort());

		return tls;
	}

}
