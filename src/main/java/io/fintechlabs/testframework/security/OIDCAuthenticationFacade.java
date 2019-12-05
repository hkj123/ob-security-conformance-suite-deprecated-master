package io.fintechlabs.testframework.security;

import java.util.Map;

import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.mitre.openid.connect.model.UserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

@Component
public class OIDCAuthenticationFacade implements AuthenticationFacade {

	// used for the OAuth layer's issuer
	@Value("${oauth.introspection_url}")
	private String introspectionUrl;

	// this gets set by the test runners and used later on
	private ThreadLocal<Authentication> localAuthentication = new ThreadLocal<>();

	@Override
	public void setLocalAuthentication(Authentication a) {
		localAuthentication.set(a);
	}

	@Override
	public Authentication getContextAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	/**
	 * If the security context has an Authentication object, return it.
	 *
	 * If not, return anything saved in the thread-local localAuthentication since
	 * we might be running in a background task.
	 *
	 * @return
	 */
	private Authentication getAuthentication() {
		Authentication a = getContextAuthentication();
		if (a != null) {
			return a;
		} else {
			return localAuthentication.get();
		}
	}

	private OIDCAuthenticationToken getOIDC() {
		Authentication a = getAuthentication();
		if (a instanceof OIDCAuthenticationToken) {
			return (OIDCAuthenticationToken) a;
		}
		return null;
	}

	private OAuth2Authentication getOAuth() {
		Authentication a = getAuthentication();
		if (a instanceof OAuth2Authentication) {
			return (OAuth2Authentication) a;
		}
		return null;
	}

	/**
	 * Check to see if the current logged in user has the ROLE_ADMIN authority defined in
	 * GoogleHostedDomainAdminAuthoritiesMapper
	 *
	 * TODO: Probably should move the ROLES to a different static class.
	 *
	 * @return
	 */
	@Override
	public boolean isAdmin() {
		Authentication a = getAuthentication();
		if (a != null) {
			return a.getAuthorities().contains(GoogleHostedDomainAdminAuthoritiesMapper.ROLE_ADMIN);
		}
		return false;
	}

	@Override
	public ImmutableMap<String, String> getPrincipal() {
		OIDCAuthenticationToken token = getOIDC();
		OAuth2Authentication auth = getOAuth();
		if (token != null) {
			@SuppressWarnings("unchecked")
			ImmutableMap<String, String> prinicipal = (ImmutableMap<String, String>) token.getPrincipal();

			return prinicipal;
		} else if (auth != null) {
			// TODO: we might be able to build this off of other properties instead
			return ImmutableMap.of("sub", auth.getOAuth2Request().getClientId(), "iss", introspectionUrl);
		}
		return null;
	}

	@Override
	public String getDisplayName() {
		OIDCAuthenticationToken token = getOIDC();
		OAuth2Authentication auth = getOAuth();
		if (token != null) {
			Map<String, String> principal = getPrincipal();
			if (principal != null) {
				String displayName = principal.toString();
				UserInfo userInfo = getUserInfo();
				if (userInfo != null) {
					if (!Strings.isNullOrEmpty(userInfo.getEmail())) {
						displayName = userInfo.getEmail();
					} else if (!Strings.isNullOrEmpty(userInfo.getPreferredUsername())) {
						displayName = userInfo.getPreferredUsername();
					} else if (!Strings.isNullOrEmpty(userInfo.getName())) {
						displayName = userInfo.getName();
					}
					return displayName;
				}
				return displayName;
			}
		} else if (auth != null) {
			return auth.getName();
		}
		return "";
	}

	@Override
	public UserInfo getUserInfo() {
		OIDCAuthenticationToken token = getOIDC();

		if (token != null) {
			return token.getUserInfo();
		} else {
			return null;
		}

	}
}
