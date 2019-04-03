package no.nav.tokentest;

import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
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

    public String getSignedToken(String payload) {
        return Jwts.builder().setPayload(payload)
                .signWith(SignatureAlgorithm.RS256, privateKey).compact();
    }


    /**
     * Standard-claims:
     * exp, nbf og iat er long representert av sekunder fra epoke
     *
     * @param claims
     * @return
     */
    public String getSignedToken(TokenClaims claims) {
        return getSignedToken(TokenHeaders.builder().build(), claims);
    }

    public String getSignedToken(TokenHeaders headers, TokenClaims claims) {
        return Jwts.builder().setHeader(headers.getHeadersMap()).setClaims(claims.getClaimsMap())
                .signWith(SignatureAlgorithm.RS256, privateKey).compact();
    }

    public Claims validateAndParseToken(String jwt) {
        return validateAndParseTokenToJwts(jwt).getBody();
    }

    public Jws<Claims> validateAndParseTokenToJwts(String jwt) {
        return Jwts.parser()
                .setSigningKey(publicKey)
                .parseClaimsJws(jwt);
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
        keys.setKeys(new ArrayList<>(Arrays.asList(key)));

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
