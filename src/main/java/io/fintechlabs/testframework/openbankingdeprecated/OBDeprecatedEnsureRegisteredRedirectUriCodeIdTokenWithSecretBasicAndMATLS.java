package io.fintechlabs.testframework.openbankingdeprecated;

import com.google.gson.JsonObject;

import io.fintechlabs.testframework.condition.Condition.ConditionResult;
import io.fintechlabs.testframework.condition.client.AddBasicAuthClientSecretAuthenticationParameters;
import io.fintechlabs.testframework.condition.client.AddClientIdToTokenEndpointRequest;
import io.fintechlabs.testframework.condition.client.CreateTokenEndpointRequestForAuthorizationCodeGrant;
import io.fintechlabs.testframework.condition.client.CreateTokenEndpointRequestForClientCredentialsGrant;
import io.fintechlabs.testframework.condition.client.SetAccountScopeOnTokenEndpointRequest;
import io.fintechlabs.testframework.condition.common.EnsureMinimumClientSecretEntropy;
import io.fintechlabs.testframework.testmodule.PublishTestModule;

@PublishTestModule(
	testName = "ob-deprecated-eol-sept-2019-ensure-registered-redirect-uri-code-id-token-with-secret-basic-and-matls",
	displayName = "OB-deprecated-EOL-sept-2019: ensure registered redirect URI (code id_token with client_secret_basic authentication and MATLS)",
	summary = "This test uses an unregistered redirect uri. The authorization server should display an error saying the redirect uri is invalid, a screenshot of which should be uploaded.",
	profile = "OB-deprecated-EOL-Sept-2019",
	configurationFields = {
		"server.discoveryUrl",
		"client.client_id",
		"client.scope",
		"client.jwks",
		"client.client_secret",
		"mtls.key",
		"mtls.cert",
		"mtls.ca",
		"resource.resourceUrl",
		"resource.resourceUrlAccountRequests",
		"resource.resourceUrlAccountsResource",
		"resource.institution_id"
	}
)
public class OBDeprecatedEnsureRegisteredRedirectUriCodeIdTokenWithSecretBasicAndMATLS extends AbstractOBDeprecatedEnsureRegisteredRedirectUriCodeIdToken {

	@Override
	protected ResponseMode getResponseMode() {
		return ResponseMode.FRAGMENT;
	}

	@Override
	protected void onConfigure(JsonObject config, String baseUrl) {

		super.onConfigure(config, baseUrl);

		logClientSecretWarning();
		callAndContinueOnFailure(EnsureMinimumClientSecretEntropy.class, ConditionResult.FAILURE, "RFC6819-5.1.4.2-2", "RFC6749-10.10");
	}

	@Override
	protected void createClientCredentialsRequest() {

		callAndStopOnFailure(CreateTokenEndpointRequestForClientCredentialsGrant.class);
		callAndStopOnFailure(SetAccountScopeOnTokenEndpointRequest.class);

		callAndStopOnFailure(AddBasicAuthClientSecretAuthenticationParameters.class);

		callAndStopOnFailure(AddClientIdToTokenEndpointRequest.class);
	}

	@Override
	protected void createAuthorizationCodeRequest() {

		callAndStopOnFailure(CreateTokenEndpointRequestForAuthorizationCodeGrant.class);

		callAndStopOnFailure(AddBasicAuthClientSecretAuthenticationParameters.class);

		callAndStopOnFailure(AddClientIdToTokenEndpointRequest.class);
	}

}
