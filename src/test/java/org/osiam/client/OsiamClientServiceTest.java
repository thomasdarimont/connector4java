package org.osiam.client;

import org.junit.Before;
import org.junit.Test;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.OauthClient;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

/**
 * Created by tom on 31.07.15.
 */
public class OsiamClientServiceTest {

    private static final String ENDPOINT = "http://localhost:8080/osiam-auth-server";

    private OsiamClientService clientService;
    private AccessToken accessToken;

    @Before
    public void setUp() throws Exception {

        clientService = new OsiamClientService.Builder(ENDPOINT).build();
        accessToken = new AccessToken.Builder("2cf7924f-b725-43b8-8d2d-09bc384cf9e0")
                .setExpiresAt(new Date(1438328170956L))
                .build();
    }

    @Test
    public void testName() throws Exception {

        OauthClient client = new OauthClient.Builder(null).setId("testClientool").build();
        client.setClientSecret("testSecret");
        client.setAccessTokenValiditySeconds(9999);
        client.setRefreshTokenValiditySeconds(9999);
        client.setRedirectUri("http://localhost:8080/?clientCreated");
        client.setScope(new HashSet(Arrays.asList("POST", "PUT", "GET", "DELETE", "PATCH")));
        client.setImplicit(false);
        client.setGrants(new HashSet<String>(Arrays.asList("authorization_code", "client_credentials", "refreshtoken")));

        OauthClient createdClient = clientService.createClient(client, accessToken);

        System.out.println(createdClient);
    }
}
