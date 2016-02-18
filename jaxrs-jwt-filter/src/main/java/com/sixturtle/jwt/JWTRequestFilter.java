package com.sixturtle.jwt;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Priority;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;

/**
 * A JAX-RS Request Filter to intercept authorization header and verify the validity of JWT
 * token using a JWT library and shared signing secret from IDP.
 * <p>
 * {@code
 *  <system-properties>
         <property name="api.security.keystore.file" value="secure-keystore.jks"/>
         <property name="api.security.keystore.password" value="changeit"/>
         <property name="api.security.key.alias" value="jwt"/>
 *  /system-properties>
 * }
 * </p>
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
@JWTSecured
public class JWTRequestFilter implements ContainerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JWTRequestFilter.class);

    public static final String PROP_SECURITY_KEYSTORE  = "api.security.keystore.file";
    public static final String PROP_SECURITY_PASSWORD  = "api.security.keystore.password";
    public static final String PROP_SECURITY_KEY_ALIAS = "api.security.key.alias";

    private static final String DEFAULT_KEYSTORE_KEY_ALIAS  = "jwt";
    private static final String DEFAULT_KEYSTORE_PASSWORD   = "changeit";
    private static final String DEFAULT_KEYSTORE            = "secure-keystore.jks";

    private Pattern     tokenPattern  = Pattern.compile("^Bearer$", Pattern.CASE_INSENSITIVE);
    private JWSVerifier jwsVerifier;

    /**
     * Instantiates a new JWT verifier with signing secret.
     *
     * @throws Exception in case of error setting up JWS Verifier
     */
    public JWTRequestFilter() throws Exception {
        String keystore = System.getProperty(PROP_SECURITY_KEYSTORE,  DEFAULT_KEYSTORE);
        String password = System.getProperty(PROP_SECURITY_PASSWORD,  DEFAULT_KEYSTORE_PASSWORD);
        String alias    = System.getProperty(PROP_SECURITY_KEY_ALIAS, DEFAULT_KEYSTORE_KEY_ALIAS);

        PublicKey publicKey = loadPublicKey(keystore, password, alias);
        if (publicKey != null) {
            jwsVerifier = new RSASSAVerifier((RSAPublicKey) publicKey);
        } else {
            throw new RuntimeException("Configuration error: unable to load JWT signing public key from keystore: " + keystore);
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.ws.rs.container.ContainerRequestFilter#filter(javax.ws.rs.container.ContainerRequestContext)
     */
    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null) {
            String token = parseBearerToken(authorizationHeader);
            if (token != null) {
                JWTClaimsSet claims     = validateToken(token);
                JWTPrincipal principal  = buildPrincipal(claims);
                if (principal != null) {
                    // Build and inject JavaEE SecurityContext for @RoleAllowed, isUserInRole(), getUserPrincipal() to work
                    JWTSecurityContext ctx = new JWTSecurityContext(
                                                    principal,
                                                    requestContext.getSecurityContext().isSecure());
                    requestContext.setSecurityContext(ctx);
                } else {
                    throw new NotAuthorizedException(
                            "Unauthorized: Unable to extract claims from JWT",
                            Response.status(Status.UNAUTHORIZED));
                }
            } else {
                throw new NotAuthorizedException(
                            "Unauthorized: Unable to parse Bearer token",
                            Response.status(Status.UNAUTHORIZED));
            }
        } else {
            throw new NotAuthorizedException(
                        "Unauthorized: No Authorization header was found",
                        Response.status(Status.UNAUTHORIZED));
        }
    }

    /**
     * Creates a new instance of {@link JWTPrincipal} from JSON Web Token (JWT)
     * claims.
     *
     * <pre>
     * JWT: {
     *  ...
     *    "email": "john.doe@idp.com",
          "name": "John Doe",
          "family_name": "Doe",
          "given_name": "John"
       }
     * </pre>
     *
     * @param claims
     *            The JWT claims set
     * @return A new instance of {@link JWTPrincipal} from JSON Web Token (JWT)
     *         claims
     */
    private JWTPrincipal buildPrincipal(final JWTClaimsSet claims) {
        JWTPrincipal principal = null;

        try {
            if (claims != null) {
                String subject   = claims.getSubject();
                String email     = (String) claims.getClaim("email");
                String firstName = (String) claims.getClaim("given_name");
                String lastName  = (String) claims.getClaim("family_name");

                // TODO: Extract custom attributes, e.g. roles, organization affiliation etc. and put into principal.

                principal = new JWTPrincipal(subject, email, firstName, lastName);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return principal;
    }

    /**
     * Validate the JSON Web Token for signature, expiration and not before
     * time.
     *
     * @param token
     *            The JSON Web Token
     * @return {@link JWTClaimsSet} in case of success, null otherwise
     */
    private JWTClaimsSet validateToken(final String token) {
        JWTClaimsSet claims = null;

        try {
            JWT jwt = JWTParser.parse(token);
            if (jwt instanceof SignedJWT) {
                SignedJWT signedJWT = (SignedJWT) jwt;
                if (signedJWT.verify(jwsVerifier)) {
                    claims = signedJWT.getJWTClaimsSet();
                    log.trace("JWT claims: {}", claims.getClaims());

                    Date expirationTime = claims.getExpirationTime();
                    Date now = new Date();
                    Date notBeforeTime = claims.getNotBeforeTime();
                    if (notBeforeTime.compareTo(now) > 0) {
                        throw new NotAuthorizedException(
                                    "Unauthorized: too early, token not valid yet",
                                    Response.status(Status.UNAUTHORIZED));
                    }
                    if (expirationTime.compareTo(now) <= 0) {
                        throw new NotAuthorizedException(
                                    "Unauthorized: too late, token expired",
                                    Response.status(Status.UNAUTHORIZED));
                    }
                } else {
                    throw new NotAuthorizedException(
                                "Unauthorized: Unable to verify Bearer token",
                                Response.status(Status.UNAUTHORIZED));
                }
            } else {
                throw new NotAuthorizedException(
                            "Unauthorized: Unexpected JWT type",
                            Response.status(Status.UNAUTHORIZED));
            }
        } catch (ParseException | JOSEException e) {
            throw new NotAuthorizedException(
                        e.getMessage(),
                        Response.status(Status.UNAUTHORIZED),
                        e);
        }
        return claims;
    }

    /**
     * Extract Bearer token value from string "Bearer [value]".
     *
     * @param bearerToken
     *            The Bearer token string of the form "Bearer [value]"
     * @return The value part of the token if scheme (prefix) matches with
     *         Bearer, null otherwise
     */
    private String parseBearerToken(final String bearerToken) {
        String tokenValue = null;
        if (bearerToken  != null) {
            String[] parts = bearerToken.split(" ");
            if (parts.length == 2) {
                String scheme       = parts[0];
                String credentials  = parts[1];
                if (tokenPattern.matcher(scheme).matches()) {
                    tokenValue = credentials;
                }
            }
        }
        return tokenValue;
    }

    /**
     * Gets public key from a JKS keystore.
     *
     * @param keystoreFile
     *            The keystore file pathname
     * @param password
     *            The keystore password
     * @param alias
     *            The key alias name
     * @return {@link RSAPublicKey} for the alias if found, null otherwise
     */
    private PublicKey loadPublicKey(String keystoreFile, String password, String alias) {
        PublicKey publicKey = null;
        log.debug("Loading public key: {} from keystore: {}", alias, keystoreFile);
        try {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());

            File file = new File(keystoreFile);
            InputStream is = null;
            if (file.exists()) {
                is = new BufferedInputStream(new FileInputStream(file));
            } else {
                is = getClass().getResourceAsStream(keystoreFile);
            }

            if (is != null) {
              keystore.load(is, password.toCharArray());
              Certificate cert = keystore.getCertificate(alias);
              if (cert != null) {
                  publicKey = cert.getPublicKey();
              } else {
                  log.error("Invalid key alias provided, key not found");
              }
            } else {
                log.error("Unable to load keystore file: {}", keystoreFile);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return publicKey;
    }

    /**
     * Implements {@link Principal} to represent a username, role etc. for JAAS Subject.
     * An instance of {@link Principal} is obtained from call to SecurityContext.getUserPrincipal().
     * The default returned type is above principal interface which provides just principal.getName().
     *
     * In order to get more information about the logged-in user, type cast to JWTPrincipal.
     * {@code String email = (JWTPrincipal) principal.getEmail();}
     */
    public class JWTPrincipal implements Principal {
        private String name;
        private String email;
        private String firstName;
        private String lastName;
        private String[] organizations;
        private String[] roles;

        /**
         * Init {@link JWTPrincipal}.
         *
         * @param name
         *            Unique name of the user
         * @param email
         *            Email of the user
         * @param firstName
         *            First name of the user
         * @param lastName
         *            Last name of the user
         * @param username
         *            The username
         */
        public JWTPrincipal(
                final String name,
                final String email,
                final String firstName,
                final String lastName) {
            this.name           = name;
            this.email          = email;
            this.firstName      = firstName;
            this.lastName       = lastName;
        }

        /* (non-Javadoc)
         * @see java.security.Principal#getName()
         */
        @Override
        public String getName() {
            return name;
        }
        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the email
         */
        public String getEmail() {
            return email;
        }

        /**
         * @param email the email to set
         */
        public void setEmail(String email) {
            this.email = email;
        }

        /**
         * @return the firstName
         */
        public String getFirstName() {
            return firstName;
        }

        /**
         * @param firstName the firstName to set
         */
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        /**
         * @return the lastName
         */
        public String getLastName() {
            return lastName;
        }

        /**
         * @param lastName the lastName to set
         */
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        /**
         * @return the organizations
         */
        public String[] getOrganizations() {
            return organizations;
        }

        /**
         * @param organizations the organizations to set
         */
        public void setOrganizations(String[] organizations) {
            this.organizations = organizations;
        }

        /**
         * @return the roles
         */
        public String[] getRoles() {
            return roles;
        }

        /**
         * @param roles the roles to set
         */
        public void setRoles(String[] roles) {
            this.roles = roles;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("JWTPrincipal {")
                   .append("name:").append(name).append(",")
                   .append("email:").append(email).append(",")
                   .append("firstName:").append(firstName).append(",")
                   .append("lastName:").append(lastName).append(",")
                   .append("organizations:").append(Arrays.toString(organizations)).append(",")
                   .append("roles:").append(Arrays.toString(roles))
                   .append("}");
            return builder.toString();
        }
    }

    /**
     * Implements {@link SecurityContext} to create a custom context from JWT token.
     */
    public static class JWTSecurityContext implements SecurityContext {
        private JWTPrincipal principal;
        private boolean      isSecure;
        private Set<String>  roles = new HashSet<>();

        /**
         * Init context.
         *
         * @param principal
         *            The {@link JWTPrincipal}
         * @param isSecure
         *            true if secure, false otherwise
         */
        public JWTSecurityContext(final JWTPrincipal principal, final boolean isSecure) {
            this.principal  = principal;
            this.isSecure   = isSecure;
            String[] names  = principal.getRoles();
            for (int iIndex = 0; names != null && iIndex < names.length; ++iIndex) {
                roles.add(names[iIndex]);
            }
            names = principal.getOrganizations();
            for (int iIndex = 0; names != null && iIndex < names.length; ++iIndex) {
                roles.add(names[iIndex]);
            }
            log.trace("JWTSecurityContext() - principal: {}, roles: {}, isSecure: {}", principal, roles, isSecure);
        }

        /*
         * (non-Javadoc)
         * @see javax.ws.rs.core.SecurityContext#getAuthenticationScheme()
         */
        @Override
        public String getAuthenticationScheme() {
            return "JWT"; // informational
        }

        /*
         * (non-Javadoc)
         * @see javax.ws.rs.core.SecurityContext#getUserPrincipal()
         */
        @Override
        public Principal getUserPrincipal() {
            return principal;
        }

        /*
         * (non-Javadoc)
         * @see javax.ws.rs.core.SecurityContext#isSecure()
         */
        @Override
        public boolean isSecure() {
            return isSecure;
        }

        /*
         * (non-Javadoc)
         * @see javax.ws.rs.core.SecurityContext#isUserInRole(java.lang.String)
         */
        @Override
        public boolean isUserInRole(final String role) {
            return roles.contains(role);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("JWTSecurityContext {")
                   .append("principal:").append(principal).append(",")
                   .append("roles:").append(roles).append(",")
                   .append("isSecure:").append(isSecure)
                   .append("}");
            return builder.toString();
        }
    }
}