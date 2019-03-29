package no.nav.tokentest;

import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class TokenHandler {
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public TokenHandler() {
        this(false);
    }
    public TokenHandler(boolean staticKeyPair) {
        try {
            if (staticKeyPair) {
                InputStream is = TokenHandler.class.getResourceAsStream("/teststatkeystore.jks");
                var keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                keystore.load(is, "test1234".toCharArray());
                this.privateKey = (PrivateKey) keystore.getKey("teststatkey", "test1234".toCharArray());
                this.publicKey = keystore.getCertificate("teststatkey").getPublicKey();
            } else {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                var keyPair = keyPairGenerator.generateKeyPair();
                this.privateKey = keyPair.getPrivate();
                this.publicKey = keyPair.getPublic();
            }
        } catch (IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated(forRemoval = true)
    public String getSignedToken(String payload) {
        return getSignedToken("testid", payload, "testissuer");
    }

    @Deprecated(forRemoval = true)
    public String getSignedToken(String id, String payload, String issuer) {
        return Jwts.builder().setId(id)
                .setIssuedAt(new Date())
                .setSubject(payload)
                .setIssuer(issuer)
                .setExpiration(Date.from(Instant.now().plus(10, ChronoUnit.MINUTES)))
                .signWith(SignatureAlgorithm.RS256, privateKey).compact();
    }

    /**
     * Standard-claims:
     * exp, nbf og iat er long representert av sekunder fra epoke
     *
     * @param claimsList
     * @return
     */
    public String getSignedToken(Map<String, Object> claimsList) {
        var claims = defaultClaims();
        claims.putAll(claimsList);
        return Jwts.builder().setClaims(claims)
                .signWith(SignatureAlgorithm.RS256, privateKey).compact();
    }

    private Map<String, Object> defaultClaims() {
        var defaultClaims = new HashMap<String, Object>();
        defaultClaims.put("iss", "tokentestsupport-TokenHandler");
        defaultClaims.put("exp", Instant.now().plus(5, ChronoUnit.MINUTES).getEpochSecond());
        defaultClaims.put("nbf", Instant.now().minus(1, ChronoUnit.MINUTES).getEpochSecond());
        defaultClaims.put("iat", Instant.now().getEpochSecond());
        defaultClaims.put("jti", "testid");
        return defaultClaims;
    }

    public Claims validateAndParseToken(String jwt) {
        return Jwts.parser()
                .setSigningKey(publicKey)
                .parseClaimsJws(jwt).getBody();
    }

    public String getJWKS(String kid) {
        var publicKey = (RSAPublicKey) this.publicKey;
        var key = new Jwks.Keys();
        key.setKty(publicKey.getAlgorithm()); // getAlgorithm() returns kty not algorithm
        key.setKid(kid);
        key.setN(Base64.getUrlEncoder().encodeToString(publicKey.getModulus().toByteArray()));
        key.setE(Base64.getUrlEncoder().encodeToString(publicKey.getPublicExponent().toByteArray()));
        key.setAlg("RS256");
        key.setUse("sig");
        var keys = new Jwks();
        keys.setKeys(new ArrayList<Jwks.Keys>(Arrays.asList(key)));

        var gson = new Gson();
        return gson.toJson(keys);
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
}
