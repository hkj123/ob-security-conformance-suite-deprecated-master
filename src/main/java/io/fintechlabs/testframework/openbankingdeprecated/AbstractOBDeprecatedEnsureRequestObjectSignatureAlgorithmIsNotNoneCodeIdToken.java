package io.fintechlabs.testframework.openbankingdeprecated;

import io.fintechlabs.testframework.condition.client.SetAuthorizationEndpointRequestResponseTypeToCodeIdtoken;

public abstract class AbstractOBDeprecatedEnsureRequestObjectSignatureAlgorithmIsNotNoneCodeIdToken extends AbstractOBDeprecatedEnsureRequestObjectSignatureAlgorithmIsNotNone {

	@Override
	protected ResponseMode getResponseMode() {
		return ResponseMode.FRAGMENT;
	}

	@Override
	protected void createAuthorizationRequest() {

		super.createAuthorizationRequest();

		callAndStopOnFailure(SetAuthorizationEndpointRequestResponseTypeToCodeIdtoken.class);
	}

	@Override
	protected void performTokenEndpointIdTokenExtraction() {
		performTokenEndpointIdTokenExtractionCodeIdToken();
	}
}
