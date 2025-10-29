package dev.charles.SimpleService.security.introspector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimAccessor;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;
import org.springframework.security.oauth2.server.resource.introspection.*;
import org.springframework.util.Assert;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.io.Serial;
import java.net.URI;
import java.time.Instant;
import java.util.*;

public class GoogleOpaqueTokenIntrospector implements OpaqueTokenIntrospector {
    private static final String AUTHORITY_PREFIX = "SCOPE_";
    private static final ParameterizedTypeReference<Map<String, Object>> STRING_OBJECT_MAP = new ParameterizedTypeReference<>() {
    };
    private final Log logger = LogFactory.getLog(getClass());
    private final RestOperations restOperations;
    private Converter<String, RequestEntity<?>> requestEntityConverter;
    private Converter<OAuth2TokenIntrospectionClaimAccessor, ? extends OAuth2AuthenticatedPrincipal> authenticationConverter = this::defaultAuthenticationConverter;

    /**
     * Creates a {@code OpaqueTokenAuthenticationProvider} with the provided parameters
     * The given {@link RestOperations} should perform its own client authentication
     * against the introspection endpoint.
     * @param introspectionUri The introspection endpoint uri
     * @param restOperations The client for performing the introspection request
     */
    public GoogleOpaqueTokenIntrospector(String introspectionUri, RestOperations restOperations) {
        Assert.notNull(introspectionUri, "introspectionUri cannot be null");
        Assert.notNull(restOperations, "restOperations cannot be null");
        this.requestEntityConverter = this.defaultRequestEntityConverter(introspectionUri);
        this.restOperations = restOperations;
    }

    private Converter<String, RequestEntity<?>> defaultRequestEntityConverter(String introspectionUri) {
        return (token) -> {
            URI parameterizedUri = URI.create(introspectionUri + "?access_token=" + token);
            return new RequestEntity<>( HttpMethod.GET, parameterizedUri);
        };
    }

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        RequestEntity<?> requestEntity = this.requestEntityConverter.convert(token);
        if (requestEntity == null) {
            throw new OAuth2IntrospectionException("requestEntityConverter returned a null entity");
        }
        ResponseEntity<Map<String, Object>> responseEntity = makeRequest(requestEntity);
        Map<String, Object> claims = adaptToNimbusResponse(responseEntity);
        OAuth2TokenIntrospectionClaimAccessor accessor = convertClaimsSet(claims);
        return this.authenticationConverter.convert(accessor);
    }

    private ResponseEntity<Map<String, Object>> makeRequest(RequestEntity<?> requestEntity) {
        try {
            return this.restOperations.exchange(requestEntity, STRING_OBJECT_MAP);
        }
        catch (Exception ex) {
            throw new OAuth2IntrospectionException(ex.getMessage(), ex);
        }
    }

    private Map<String, Object> adaptToNimbusResponse(ResponseEntity<Map<String, Object>> responseEntity) {
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new OAuth2IntrospectionException(
                    "Introspection endpoint responded with " + responseEntity.getStatusCode());
        }
        Map<String, Object> claims = responseEntity.getBody();
        // relying solely on the authorization server to validate this token (not checking
        // 'exp', for example)
        if (claims == null) {
            return Collections.emptyMap();
        }
        claims.computeIfAbsent("active", k -> {
            long expirationTimeEpoch = Long.parseLong((String) claims.get("exp"));
            Instant expirationInstant = Instant.ofEpochSecond(expirationTimeEpoch);
            Instant now = Instant.now();
            return !now.isAfter(expirationInstant);
        });
        boolean active = (boolean) claims.compute("active", (k, v) -> {
            if (v instanceof String) {
                return Boolean.parseBoolean((String) v);
            }
            if (v instanceof Boolean) {
                return v;
            }
            return false;
        });

        if (!active) {
            this.logger.trace("Did not validate token since it is inactive");
            throw new BadOpaqueTokenException("Provided token isn't active");
        }
        return claims;
    }

    private ArrayListFromStringClaimAccessor convertClaimsSet(Map<String, Object> claims) {
        Map<String, Object> converted = new LinkedHashMap<>(claims);
        converted.computeIfPresent(OAuth2TokenIntrospectionClaimNames.AUD, (k, v) -> {
            if (v instanceof String) {
                return Collections.singletonList(v);
            }
            return v;
        });
        converted.computeIfPresent(OAuth2TokenIntrospectionClaimNames.CLIENT_ID, (k, v) -> v.toString());
        converted.computeIfPresent(OAuth2TokenIntrospectionClaimNames.EXP,
                (k, v) -> Instant.ofEpochSecond(Long.parseLong((String) v)));
        converted.computeIfPresent(OAuth2TokenIntrospectionClaimNames.IAT,
                (k, v) -> Instant.ofEpochSecond(Long.parseLong((String) v)));

        converted.computeIfPresent(OAuth2TokenIntrospectionClaimNames.ISS, (k, v) -> v.toString());
        converted.computeIfPresent(OAuth2TokenIntrospectionClaimNames.NBF,
                (k, v) -> Instant.ofEpochSecond(((Number) v).longValue()));
        converted.computeIfPresent(OAuth2TokenIntrospectionClaimNames.SCOPE,
                (k, v) -> (v instanceof String s) ? new ArrayListFromString(s.split(" ")) : v);
        return () -> converted;
    }

    /**
     * If {@link GoogleOpaqueTokenIntrospector#authenticationConverter} is not explicitly
     * set, this default converter will be used. transforms an
     * {@link OAuth2TokenIntrospectionClaimAccessor} into an
     * {@link OAuth2AuthenticatedPrincipal} by extracting claims, mapping scopes to
     * authorities, and creating a principal.
     * @return {@link Converter Converter&lt;OAuth2TokenIntrospectionClaimAccessor,
     * OAuth2AuthenticatedPrincipal&gt;}
     * @since 6.3
     */
    private OAuth2IntrospectionAuthenticatedPrincipal defaultAuthenticationConverter(
            OAuth2TokenIntrospectionClaimAccessor accessor) {
        Collection<GrantedAuthority> authorities = authorities(accessor.getScopes());
        return new OAuth2IntrospectionAuthenticatedPrincipal(accessor.getClaims(), authorities);
    }

    private Collection<GrantedAuthority> authorities(List<String> scopes) {
        if (!(scopes instanceof ArrayListFromString)) {
            return Collections.emptyList();
        }
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        for (String scope : scopes) {
            authorities.add(new SimpleGrantedAuthority(AUTHORITY_PREFIX + scope));
        }
        return authorities;
    }

    // gh-7563
    private static final class ArrayListFromString extends ArrayList<String> {

        @Serial
        private static final long serialVersionUID = -1804103555781637109L;

        ArrayListFromString(String... elements) {
            super(Arrays.asList(elements));
        }

    }

    // gh-15165
    private interface ArrayListFromStringClaimAccessor extends OAuth2TokenIntrospectionClaimAccessor {

        @Override
        default List<String> getScopes() {
            Object value = getClaims().get(OAuth2TokenIntrospectionClaimNames.SCOPE);
            if (value instanceof ArrayListFromString list) {
                return list;
            }
            return OAuth2TokenIntrospectionClaimAccessor.super.getScopes();
        }

    }

}
