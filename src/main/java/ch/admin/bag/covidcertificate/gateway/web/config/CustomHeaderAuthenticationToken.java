package ch.admin.bag.covidcertificate.gateway.web.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

public class CustomHeaderAuthenticationToken implements Authentication {

	private final String id;

	private final String clientCert;

	public CustomHeaderAuthenticationToken(String id, String clientCert) {
		this.id = id;
		this.clientCert = clientCert;
	}

	public String getId() {
		return id;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.emptyList();
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public Object getDetails() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return null;
	}

	@Override
	public boolean isAuthenticated() {
		return true;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public String getName() {
		return getId();
	}

	public String getClientCert() {
		return clientCert;
	}
}
