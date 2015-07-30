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

package org.osiam.client;

import static com.google.common.base.Preconditions.*;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

import org.osiam.client.exception.*;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.query.Query;
import org.osiam.client.query.QueryBuilder;
import org.osiam.resources.helper.UserDeserializer;
import org.osiam.resources.scim.Resource;
import org.osiam.resources.scim.SCIMSearchResult;
import org.osiam.resources.scim.User;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Strings;

/**
 * AbstractOsiamService provides all basic methods necessary to manipulate the Entities registered in the given OSIAM
 * installation. For the construction of an instance please use the included {@link AbstractOsiamService.Builder}
 */
abstract class AbstractOsiamService<T extends Resource> {

    protected static final String CONNECTION_SETUP_ERROR_STRING = "Cannot connect to server";

    protected static final String AUTHORIZATION = "Authorization";
    protected static final String BEARER = "Bearer ";

    private final Class<T> type;
    private final String typeName;
    private final ObjectMapper mapper;

    protected final WebTarget targetEndpoint;

    protected AbstractOsiamService(Builder<T> builder) {
        type = builder.type;
        typeName = builder.typeName;

        mapper = new ObjectMapper();
        SimpleModule userDeserializerModule = new SimpleModule("userDeserializerModule", Version.unknownVersion())
                .addDeserializer(User.class, new UserDeserializer(User.class));
        mapper.registerModule(userDeserializerModule);

        targetEndpoint = OsiamConnector.getClient().target(builder.endpoint);
    }

    protected T getResource(String id, AccessToken accessToken) {
        checkArgument(!Strings.isNullOrEmpty(id), "The given id must not be null nor empty.");
        checkAccessTokenIsNotNull(accessToken);

        StatusType status;
        String content;
        try {
            Response response = targetEndpoint.path(getPath()).path(id).request(MediaType.APPLICATION_JSON)
                    .header(AUTHORIZATION, BEARER + accessToken.getToken())
                    .get();

            status = response.getStatusInfo();
            content = response.readEntity(String.class);
        } catch (ProcessingException e) {
            throw new ConnectionInitializationException(CONNECTION_SETUP_ERROR_STRING, e);
        }

        checkAndHandleResponse(content, status, accessToken);

        return mapToResource(content);
    }

    protected List<T> getAllResources(AccessToken accessToken) {
        Query query = new QueryBuilder().count(Integer.MAX_VALUE).build();
        return searchResources(query, accessToken).getResources();
    }

    protected SCIMSearchResult<T> searchResources(Query query, AccessToken accessToken) {
        checkNotNull(query, "The given query must not be null.");
        checkAccessTokenIsNotNull(accessToken);

        StatusType status;
        String content;
        try {
            Response response = targetEndpoint.path(getPath())
                    .queryParam("attributes", query.getAttributes())
                    .queryParam("filter", query.getFilter())
                    .queryParam("sortBy", query.getSortBy())
                    .queryParam("sortOrder", query.getSortOrder())
                    .queryParam("startIndex",
                            query.getStartIndex() != QueryBuilder.DEFAULT_START_INDEX ? query.getStartIndex() : null)
                    .queryParam("count",
                            query.getCount() != QueryBuilder.DEFAULT_COUNT ? query.getCount() : null)
                    .request(MediaType.APPLICATION_JSON)
                    .header(AUTHORIZATION, BEARER + accessToken.getToken())
                    .get();

            status = response.getStatusInfo();
            content = response.readEntity(String.class);
        } catch (ProcessingException e) {
            throw new ConnectionInitializationException(CONNECTION_SETUP_ERROR_STRING, e);
        }

        checkAndHandleResponse(content, status, accessToken);

        try {
            JavaType queryResultType = TypeFactory.defaultInstance()
                    .constructParametrizedType(SCIMSearchResult.class, SCIMSearchResult.class, type);

            return mapper.readValue(content, queryResultType);
        } catch (IOException e) {
            throw new OsiamClientException(String.format("Unable to deserialize search result: %s", content), e);
        }
    }

    protected void deleteResource(String id, AccessToken accessToken) {
        checkArgument(!Strings.isNullOrEmpty(id), "The given id must not be null nor empty.");
        checkAccessTokenIsNotNull(accessToken);

        StatusType status;
        String content;
        try {
            Response response = targetEndpoint.path(getPath()).path(id).request(MediaType.APPLICATION_JSON)
                    .header(AUTHORIZATION, BEARER + accessToken.getToken())
                    .delete();

            status = response.getStatusInfo();
            content = response.readEntity(String.class);
        } catch (ProcessingException e) {
            throw new ConnectionInitializationException(CONNECTION_SETUP_ERROR_STRING, e);
        }

        checkAndHandleResponse(content, status, accessToken);
    }

    protected T createResource(T resource, AccessToken accessToken) {
        checkNotNull(resource, "The given %s must not be null nor empty.", typeName);
        checkAccessTokenIsNotNull(accessToken);

        String resourceAsString;
        try {
            resourceAsString = mapper.writeValueAsString(resource);
        } catch (JsonProcessingException e) {
            throw new ConnectionInitializationException(CONNECTION_SETUP_ERROR_STRING, e);
        }

        StatusType status;
        String content;
        try {
            Response response = targetEndpoint.path(getPath()).request(MediaType.APPLICATION_JSON)
                    .header(AUTHORIZATION, BEARER + accessToken.getToken())
                    .post(Entity.entity(resourceAsString, MediaType.APPLICATION_JSON));

            status = response.getStatusInfo();
            content = response.readEntity(String.class);
        } catch (ProcessingException e) {
            throw new ConnectionInitializationException(CONNECTION_SETUP_ERROR_STRING, e);
        }

        checkAndHandleResponse(content, status, accessToken);

        return mapToResource(content);
    }

    protected String getPath() {
        return typeName + "s";
    }

    protected T updateResource(String id, T resource, AccessToken accessToken) {
        return modifyResource(id, resource, "PATCH", accessToken);
    }

    protected T replaceResource(String id, T resource, AccessToken accessToken) {
        return modifyResource(id, resource, "PUT", accessToken);
    }

    private T modifyResource(String id, T resource, String method, AccessToken accessToken) {
        checkArgument(!Strings.isNullOrEmpty(id), "The given id must not be null nor empty.");
        checkNotNull(resource, "The given %s must not be null nor empty.", typeName);
        checkAccessTokenIsNotNull(accessToken);

        String resourceAsString;
        try {
            resourceAsString = mapper.writeValueAsString(resource);
        } catch (JsonProcessingException e) {
            throw new ConnectionInitializationException(CONNECTION_SETUP_ERROR_STRING, e);
        }

        StatusType status;
        String content;
        try {
            Response response = targetEndpoint.path(getPath()).path(id).request(MediaType.APPLICATION_JSON)
                    .header(AUTHORIZATION, BEARER + accessToken.getToken())
                    .method(method, Entity.entity(resourceAsString, MediaType.APPLICATION_JSON));

            status = response.getStatusInfo();
            content = response.readEntity(String.class);
        } catch (ProcessingException e) {
            throw new ConnectionInitializationException(CONNECTION_SETUP_ERROR_STRING, e);
        }

        checkAndHandleResponse(content, status, accessToken);

        return mapToResource(content);
    }

    protected T mapToResource(String content) {
        return mapToType(content, type);
    }

    protected <U> U mapToType(String content, Class<U> type) {
        try {
            return mapper.readValue(content, type);
        } catch (IOException e) {
            throw new OsiamClientException(String.format("Unable to parse %s: %s", typeName, content), e);
        }
    }

    protected void checkAndHandleResponse(String content, StatusType status, AccessToken accessToken) {
        if (status.getFamily() == Family.SUCCESSFUL) {
            return;
        }

        if (status.getStatusCode() == Status.UNAUTHORIZED.getStatusCode()) {
            String errorMessage = extractErrorMessageUnauthorized(content, status);
            throw new UnauthorizedException(errorMessage);
        } else if (status.getStatusCode() == Status.BAD_REQUEST.getStatusCode()) {
            String errorMessage = extractErrorMessage(content, status);
            throw new ConflictException(errorMessage);
        } else if (status.getStatusCode() == Status.NOT_FOUND.getStatusCode()) {
            String errorMessage = extractErrorMessage(content, status);
            throw new NoResultException(errorMessage);
        } else if (status.getStatusCode() == Status.FORBIDDEN.getStatusCode()) {
            String errorMessage = extractErrorMessageForbidden(accessToken);
            throw new ForbiddenException(errorMessage);
        } else if (status.getStatusCode() == Status.CONFLICT.getStatusCode()) {
            String errorMessage = extractErrorMessage(content, status);
            throw new ConflictException(errorMessage);
        } else {
            String errorMessage = extractErrorMessageDefault(content, status);
            throw new OsiamRequestException(status.getStatusCode(), errorMessage);
        }

    }

    protected String extractErrorMessageForbidden(AccessToken accessToken) {
        return "Insufficient scopes: " + accessToken.getScopes();
    }

    protected String extractErrorMessageUnauthorized(String content, StatusType status) {
        return extractErrorMessage(content, status);
    }

    protected String extractErrorMessageDefault(String content, StatusType status) {
        return extractErrorMessage(content, status);
    }

    protected String extractErrorMessage(String content, StatusType status) {

        String message = getScimErrorMessage(content);
        if (message == null) {
            message = getOAuthErrorMessage(content);
        }
        if (message == null) {
            message = String.format("Could not deserialize the error response for the HTTP status '%s'.",
                    status.getReasonPhrase());
            if (content != null) {
                message += String.format(" Original response: %s", content);
            }
        }
        return message;
    }

    private String getScimErrorMessage(String content) {
        try {
            ScimErrorMessage error = new ObjectMapper().readValue(content, ScimErrorMessage.class);
            return error.getDescription();
        } catch (ProcessingException | IOException e) {
            return null;
        }
    }

    private String getOAuthErrorMessage(String content) {
        try {
            OAuthErrorMessage error = new ObjectMapper().readValue(content, OAuthErrorMessage.class);
            return error.getDescription();
        } catch (ProcessingException | IOException e) {
            return null;
        }
    }

    protected static void checkAccessTokenIsNotNull(AccessToken accessToken) {
        checkNotNull(accessToken, "The given accessToken must not be null.");
    }

    protected static class Builder<T> {
        private String endpoint;
        private Class<T> type;
        private String typeName;

        @SuppressWarnings("unchecked")
        protected Builder(String endpoint) {
            this.endpoint = endpoint;
            this.type = (Class<T>)
                    ((ParameterizedType) getClass().getGenericSuperclass())
                            .getActualTypeArguments()[0];
            typeName = type.getSimpleName();
        }

        protected Builder(String endpoint, Class<T> type, String typeName){
            this.endpoint = endpoint;
            this.type = type;
            this.typeName = typeName;
        }
    }
}
