/*
 * Copyright (C) 2013 tarent AG
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.osiam.client.oauth;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.osiam.resources.scim.Resource;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public class OauthClient extends Resource {

    @JsonProperty
    private int accessTokenValiditySeconds;

    @JsonProperty
    private int refreshTokenValiditySeconds;

    @JsonProperty
    private String redirectUri;

    @JsonProperty("client_secret")
    private String clientSecret = generateSecret();

    @JsonProperty
    private Set<String> scope;

    @JsonProperty
    private Set<String> grants = generateGrants();

    @JsonProperty
    private boolean implicit;

    @JsonProperty
    private long validityInSeconds;

    public OauthClient(){}

    public OauthClient(Resource.Builder builder) {
        super(builder);
    }

    public int getAccessTokenValiditySeconds() {
        return accessTokenValiditySeconds;
    }

    public int getRefreshTokenValiditySeconds() {
        return refreshTokenValiditySeconds;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public Set<String> getScope() {
        return scope;
    }

    public Set<String> getGrants() {
        return grants;
    }

    private Set<String> generateGrants() {
        Set<String> result = new HashSet<>();
        Collections.addAll(result, "authorization_code", "refresh-token");
        return result;
    }

    private String generateSecret() {
        return UUID.randomUUID().toString();
    }

    public void setGrants(Set<String> grants) {
        this.grants = grants;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setScope(Set<String> scope) {
        this.scope = scope;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setAccessTokenValiditySeconds(int accessTokenValiditySeconds) {
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
    }

    public void setRefreshTokenValiditySeconds(int refreshTokenValiditySeconds) {
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
    }

    public boolean isImplicit() {
        return implicit;
    }

    public void setImplicit(boolean implicit) {
        this.implicit = implicit;
    }

    public long getValidityInSeconds() {
        return validityInSeconds;
    }

    public void setValidityInSeconds(long validity) {
        this.validityInSeconds = validity;
    }

    public static class Builder extends Resource.Builder {

        private int accessTokenValiditySeconds;

        private int refreshTokenValiditySeconds;

        private String redirectUri;

        private String clientSecret;

        private Set<String> scope;

        private Set<String> grants;

        private boolean implicit;

        private long validityInSeconds;

        public Builder(Resource resource) {
            super(resource);
        }

        @Override
        public OauthClient build() {

            OauthClient client = new OauthClient(this);
            client.setAccessTokenValiditySeconds(accessTokenValiditySeconds);
            client.setRefreshTokenValiditySeconds(refreshTokenValiditySeconds);
            client.setRedirectUri(redirectUri);
            client.setClientSecret(clientSecret);
            client.setScope(scope);
            client.setGrants(grants);
            client.setImplicit(implicit);
            client.setValidityInSeconds(validityInSeconds);

            return client;
        }

        public int getAccessTokenValiditySeconds() {
            return accessTokenValiditySeconds;
        }

        public Builder setAccessTokenValiditySeconds(int accessTokenValiditySeconds) {
            this.accessTokenValiditySeconds = accessTokenValiditySeconds;
            return this;
        }

        public int getRefreshTokenValiditySeconds() {
            return refreshTokenValiditySeconds;
        }

        public Builder setRefreshTokenValiditySeconds(int refreshTokenValiditySeconds) {
            this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
            return this;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public Builder setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public Builder setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public Set<String> getScope() {
            return scope;
        }

        public Builder setScope(Set<String> scope) {
            this.scope = scope;
            return this;
        }

        public Set<String> getGrants() {
            return grants;
        }

        public Builder setGrants(Set<String> grants) {
            this.grants = grants;
            return this;
        }

        public boolean isImplicit() {
            return implicit;
        }

        public Builder setImplicit(boolean implicit) {
            this.implicit = implicit;
            return this;
        }

        public long getValidityInSeconds() {
            return validityInSeconds;
        }

        public Builder setValidityInSeconds(long validityInSeconds) {
            this.validityInSeconds = validityInSeconds;
            return this;
        }
    }
}
