package no.nav.tokentest;

import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

public class TokenHandler {
    private KeyPair keyPair;


    public TokenHandler() {
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyPair = keyPairGenerator.generateKeyPair();
    }

    public String getSignedToken(String payload) {
        return getSignedToken("testid", payload, "testissuer");
    }

    public String getSignedToken(String id, String payload, String issuer) {
        return Jwts.builder().setId(id)
                .setIssuedAt(new Date())
                .setSubject(payload)
                .setIssuer(issuer)
                .setExpiration(Date.from(Instant.now().plus(10, ChronoUnit.MINUTES)))
                .signWith(SignatureAlgorithm.RS256, keyPair.getPrivate()).compact();
    }

    public Claims validateAndParseToken(String jwt) {
        return Jwts.parser()
                .setSigningKey(keyPair.getPublic())
                .parseClaimsJws(jwt).getBody();
    }

    public String getJWKS(String kid) {
        var publicKey = (RSAPublicKey) keyPair.getPublic();
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
        return keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }
}
