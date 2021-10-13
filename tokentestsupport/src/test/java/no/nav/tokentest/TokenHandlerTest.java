package no.nav.tokentest;

import com.google.gson.Gson;
import io.jsonwebtoken.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TokenHandlerTest {

    static Stream<TokenHandler> tokenHandlerProvider() {
        return Stream.of(new TokenHandler(), new TokenHandler(true), new TokenHandler(false));
    }

    @ParameterizedTest
    @MethodSource("tokenHandlerProvider")
    public void testSigning(TokenHandler tokenHandler) {
        tokenHandler.getSignedToken("{\"testname\":\"testvalue\"}");
    }

    @ParameterizedTest
    @MethodSource("tokenHandlerProvider")
    public void testSigningAndValidating(TokenHandler tokenHandler) {
        var signedToken = tokenHandler.getSignedToken("{\"testname\":\"testvalue2\"}");

        var decodedToken = tokenHandler.validateAndParseToken(signedToken);

        assertEquals("testvalue2", decodedToken.get("testname"));
    }

    @ParameterizedTest
    @MethodSource("tokenHandlerProvider")
    public void testValidatingWithExposedPublicKey(TokenHandler tokenHandler) {
        var signedToken = tokenHandler.getSignedToken("{\"testname\":\"testvalue2\"}");

        var decodedToken = Jwts.parser()
                .setSigningKey(tokenHandler.getPublicKey())
                .parseClaimsJws(signedToken).getBody();

        assertEquals("testvalue2", decodedToken.get("testname"));
    }

    @ParameterizedTest
    @MethodSource("tokenHandlerProvider")
    public void testValidatingTokenSignedWithExposedPrivateKey(TokenHandler tokenHandler) {

        var signedToken = Jwts.builder().setPayload("{\"testname\":\"testvalue2\"}")
                .signWith(SignatureAlgorithm.RS256, tokenHandler.getPrivateKey()).compact();

        var decodedToken = tokenHandler.validateAndParseToken(signedToken);

        assertEquals("testvalue2", decodedToken.get("testname"));
    }

    @ParameterizedTest
    @MethodSource("tokenHandlerProvider")
    public void testValidatingInvalidToken(TokenHandler tokenHandler) {
        var signedToken = tokenHandler.getSignedToken(TokenClaims.builder().withDefaultClaims().build());

        Assertions.assertThrows(SignatureException.class, () -> new TokenHandler().validateAndParseToken(signedToken));
    }

    @ParameterizedTest
    @MethodSource("tokenHandlerProvider")
    public void testSigningAndValidatingWithClaimsList(TokenHandler tokenHandler) {
        var claimsMap = createClaimsMap();
        var signedToken = tokenHandler.getSignedToken(TokenClaims.builder().withClaims(claimsMap).build());

        var decodedToken = tokenHandler.validateAndParseToken(signedToken);
        validateClaimsMap(claimsMap, decodedToken);
    }

    @ParameterizedTest
    @MethodSource("tokenHandlerProvider")
    public void testSigningAndValidatingWithClaimsListAndHeader(TokenHandler tokenHandler) {
        var claimsMap = createClaimsMap();
        var signedToken = tokenHandler.getSignedToken(TokenHeaders.builder().withKid("testkid").build(),
                TokenClaims.builder().withClaims(claimsMap).build());

        var decodedToken = tokenHandler.validateAndParseTokenToJwts(signedToken);
        validateClaimsMap(claimsMap, decodedToken.getBody());

        assertEquals(2, decodedToken.getHeader().size());
        assertEquals("testkid", decodedToken.getHeader().getKeyId());
        assertEquals("RS256", decodedToken.getHeader().getAlgorithm());
    }

    //@Test(expected = ExpiredJwtException.class)
    @ParameterizedTest
    @MethodSource("tokenHandlerProvider")
    public void testValidatingWithOutdatedExpiration(TokenHandler tokenHandler) {
        var signedToken = tokenHandler.getSignedToken(TokenClaims.builder().withDefaultClaims()
                .withClaim("exp", Instant.now().minus(1, ChronoUnit.HOURS).getEpochSecond()).build());

        assertThrows(ExpiredJwtException.class, () -> tokenHandler.validateAndParseToken(signedToken));
    }

    //@Test(expected = PrematureJwtException.class)
    @ParameterizedTest
    @MethodSource("tokenHandlerProvider")
    public void testValidatingWithPrematureNBF(TokenHandler tokenHandler) {
        var signedToken = tokenHandler.getSignedToken(TokenClaims.builder().withDefaultClaims()
                .withClaim("nbf", Instant.now().plus(1, ChronoUnit.HOURS).getEpochSecond()).build());

        assertThrows(PrematureJwtException.class, () -> tokenHandler.validateAndParseToken(signedToken));
    }

    private Map<String, Object> createClaimsMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("iss", "testiss");
        map.put("sub", "{\"testname\":\"testvalue4\"}");
        map.put("aud", "testaudi");
        map.put("exp", Instant.now().plus(1, ChronoUnit.HOURS).getEpochSecond());
        map.put("nbf", Instant.now().minus(1, ChronoUnit.HOURS).getEpochSecond());
        map.put("iat", Instant.now().minus(30, ChronoUnit.MINUTES).getEpochSecond());
        map.put("jti", "testid");
        return map;
    }

    private void validateClaimsMap(Map<String, Object> claimsMap, Claims decodedToken) {
        assertEquals(claimsMap.get("iss"), decodedToken.getIssuer());
        assertEquals(claimsMap.get("sub"), decodedToken.getSubject());
        assertEquals(claimsMap.get("aud"), decodedToken.getAudience());
        assertEquals(Date.from(Instant.ofEpochSecond((Long) claimsMap.get("exp"))), decodedToken.getExpiration());
        assertEquals(Date.from(Instant.ofEpochSecond((Long) claimsMap.get("nbf"))), decodedToken.getNotBefore());
        assertEquals(Date.from(Instant.ofEpochSecond((Long) claimsMap.get("iat"))), decodedToken.getIssuedAt());
        assertEquals(claimsMap.get("jti"), decodedToken.getId());
    }

    @ParameterizedTest
    @MethodSource("tokenHandlerProvider")
    public void testKeyFromJWKS(TokenHandler tokenHandler) throws NoSuchAlgorithmException, InvalidKeySpecException {

        var gson = new Gson();
        var key = gson.fromJson(tokenHandler.getJWKS("testkid"), Jwks.class)
                .getKeys().stream()
                .filter((k) -> k.getKid().equals("testkid"))
                .findAny().orElseThrow(() -> new RuntimeException("Can't find certificate in JWKS."));
        KeyFactory keyFactory = KeyFactory.getInstance(key.getKty());

        var decoder = Base64.getUrlDecoder();
        var spec = new RSAPublicKeySpec(new BigInteger(1, decoder.decode(key.getN())), new BigInteger(1, decoder.decode(key.getE())));
        var publicKeyFromJWKS = keyFactory.generatePublic(spec);
        var expctedPublicKey = tokenHandler.getPublicKey();

        assertEquals(expctedPublicKey, publicKeyFromJWKS);
    }

    @ParameterizedTest
    @MethodSource("tokenHandlerProvider")
    public void testValidatingFromJWKS(TokenHandler tokenHandler) throws InvalidKeySpecException, NoSuchAlgorithmException {

        var claimsMap = createClaimsMap();
        var signedToken = tokenHandler.getSignedToken(TokenClaims.builder().withClaims(claimsMap).build());

        var gson = new Gson();
        var key = gson.fromJson(tokenHandler.getJWKS("testkid"), Jwks.class)
                .getKeys().stream()
                .filter((k) -> k.getKid().equals("testkid"))
                .findAny().orElseThrow(() -> new RuntimeException("Can't find certificate in JWKS."));
        KeyFactory keyFactory = KeyFactory.getInstance(key.getKty());

        var decoder = Base64.getUrlDecoder();
        var spec = new RSAPublicKeySpec(new BigInteger(1, decoder.decode(key.getN())), new BigInteger(1, decoder.decode(key.getE())));
        var publicKeyFromJWKS = keyFactory.generatePublic(spec);

        var claims = Jwts.parser()
                .setSigningKey(publicKeyFromJWKS)
                .parseClaimsJws(signedToken).getBody();

        validateClaimsMap(claimsMap, claims);
    }

    public static class IkkeParameterisert {
        @Test
        public void testSigningAndValidatingStaticKeyPair() {
            var signedToken = new TokenHandler(true).getSignedToken("{\"testname\":\"testvalue2\"}");

            var decodedToken = new TokenHandler(true).validateAndParseToken(signedToken);

            assertEquals("testvalue2", decodedToken.get("testname"));
        }
    }
}