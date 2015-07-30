package org.osiam.client;

import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.OauthClient;
import org.osiam.resources.scim.User;

/**
 * Created by tom on 31.07.15.
 */
public class OsiamClientService extends AbstractOsiamService<OauthClient> {

    protected OsiamClientService(Builder builder) {
        super(builder);
    }

    /**
     * See {@link OsiamConnector#createUser(User, AccessToken)}
     */
    public OauthClient createClient(OauthClient client, AccessToken accessToken) {
        return createResource(client, accessToken);
    }

    @Override
    protected String getPath() {
        return "Client";
    }

    /**
     * See {@link OsiamConnector.Builder}
     */
    public static class Builder extends AbstractOsiamService.Builder<OauthClient> {

        /**
         * Set up the Builder for the construction of an {@link OsiamClientService} instance for the OSIAM service at the
         * given endpoint
         *
         * @param endpoint The URL at which the OSIAM server lives.
         */
        public Builder(String endpoint) {
            super(endpoint, OauthClient.class, "Client");
        }

        /**
         * See {@link OsiamConnector.Builder#build()}
         */
        public OsiamClientService build() {
            return new OsiamClientService(this);
        }
    }
}
