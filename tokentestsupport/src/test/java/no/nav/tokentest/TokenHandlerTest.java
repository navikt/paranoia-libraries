package no.nav.tokentest;

import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.PrematureJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(value = Parameterized.class)
public class TokenHandlerTest {
    private TokenHandler tokenHandler;

    public TokenHandlerTest(TokenHandler tokenHandler) {
        this.tokenHandler = tokenHandler;
    }

    @Parameterized.Parameters
    public static Collection<TokenHandler> setUp() {
        return Arrays.asList(new TokenHandler(), new TokenHandler(true), new TokenHandler(false));
    }

    @Test
    public void testSigning() {
        var signedToken = tokenHandler.getSignedToken("{\"testname\":\"testvalue\"}");
    }

    @Test
    public void testSigningAndValidating() {
        var signedToken = tokenHandler.getSignedToken("{\"testname\":\"testvalue2\"}");

        var decodedToken = tokenHandler.validateAndParseToken(signedToken);
        assertEquals("testvalue2", decodedToken.get("testname"));
    }

    @Test
    public void testValidatingWithExposedPublicKey() {
        var signedToken = tokenHandler.getSignedToken("{\"testname\":\"testvalue2\"}");

        var decodedToken = Jwts.parser()
                .setSigningKey(tokenHandler.getPublicKey())
                .parseClaimsJws(signedToken).getBody();
        assertEquals("testvalue2", decodedToken.get("testname"));
    }

    @Test
    public void testValidatingTokenSignedWithExposedPrivateKey() {

        var signedToken = Jwts.builder().setPayload("{\"testname\":\"testvalue2\"}")
                .signWith(SignatureAlgorithm.RS256, tokenHandler.getPrivateKey()).compact();

        var decodedToken = tokenHandler.validateAndParseToken(signedToken);
        assertEquals("testvalue2", decodedToken.get("testname"));
    }

    @Test(expected = SignatureException.class)
    public void testValidatingInvalidToken() {
        var signedToken = tokenHandler.getSignedToken(TokenClaims.builder().withDefaultClaims().build());

        var decodedToken = new TokenHandler().validateAndParseToken(signedToken);
    }

    @Test
    public void testSigningAndValidatingWithClaimsList() {
        var claimsMap = createClaimsMap();
        var signedToken = tokenHandler.getSignedToken(TokenClaims.builder().withClaims(claimsMap).build());

        var decodedToken = tokenHandler.validateAndParseToken(signedToken);
        validateClaimsMap(claimsMap, decodedToken);
    }

    @Test
    public void testSigningAndValidatingWithClaimsListAndHeader() {
        var claimsMap = createClaimsMap();
        var signedToken = tokenHandler.getSignedToken(TokenHeaders.builder().withKid("testkid").build(),
                TokenClaims.builder().withClaims(claimsMap).build());

        var decodedToken = tokenHandler.validateAndParseTokenToJwts(signedToken);
        validateClaimsMap(claimsMap, decodedToken.getBody());
        assertEquals(2, decodedToken.getHeader().size());
        assertEquals("testkid", decodedToken.getHeader().getKeyId());
        assertEquals("RS256", decodedToken.getHeader().getAlgorithm());
    }

    @Test(expected = ExpiredJwtException.class)
    public void testValidatingWithOutdatedExpiration() {
        var signedToken = tokenHandler.getSignedToken(TokenClaims.builder().withDefaultClaims()
                .withClaim("exp", Instant.now().minus(1, ChronoUnit.HOURS).getEpochSecond()).build());

        var decodedToken = tokenHandler.validateAndParseToken(signedToken);
    }

    @Test(expected = PrematureJwtException.class)
    public void testValidatingWithPrematureNBF() {
        var signedToken = tokenHandler.getSignedToken(TokenClaims.builder().withDefaultClaims()
                .withClaim("nbf", Instant.now().plus(1, ChronoUnit.HOURS).getEpochSecond()).build());

        var decodedToken = tokenHandler.validateAndParseToken(signedToken);
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

    private void validateClaimsMap(Map<String, Object> claimsMap, Claims decodedToken){
        assertEquals(claimsMap.get("iss"), decodedToken.getIssuer());
        assertEquals(claimsMap.get("sub"), decodedToken.getSubject());
        assertEquals(claimsMap.get("aud"), decodedToken.getAudience());
        assertEquals(Date.from(Instant.ofEpochSecond((Long) claimsMap.get("exp"))), decodedToken.getExpiration());
        assertEquals(Date.from(Instant.ofEpochSecond((Long) claimsMap.get("nbf"))), decodedToken.getNotBefore());
        assertEquals(Date.from(Instant.ofEpochSecond((Long) claimsMap.get("iat"))), decodedToken.getIssuedAt());
        assertEquals(claimsMap.get("jti"), decodedToken.getId());
    }

    @Test
    public void testKeyFromJWKS() throws NoSuchAlgorithmException, InvalidKeySpecException {

        var gson = new Gson();
        var key = gson.fromJson(tokenHandler.getJWKS("testkid"), Jwks.class)
                .getKeys().stream()
                .filter((k)-> k.getKid().equals("testkid"))
                .findAny().orElseThrow(()->new RuntimeException("Can't find certificate in JWKS."));
        KeyFactory keyFactory = KeyFactory.getInstance(key.getKty());

        var decoder = Base64.getUrlDecoder();
        var spec = new RSAPublicKeySpec(new BigInteger(1, decoder.decode(key.getN())), new BigInteger(1, decoder.decode(key.getE())));
        var publicKeyFromJWKS = keyFactory.generatePublic(spec);
        var expctedPublicKey = tokenHandler.getPublicKey();
        assertEquals(expctedPublicKey, publicKeyFromJWKS);
    }

    @Test
    public void testValidatingFromJWKS() throws InvalidKeySpecException, NoSuchAlgorithmException {

        var claimsMap = createClaimsMap();
        var signedToken = tokenHandler.getSignedToken(TokenClaims.builder().withClaims(claimsMap).build());

        var gson = new Gson();
        var key = gson.fromJson(tokenHandler.getJWKS("testkid"), Jwks.class)
                .getKeys().stream()
                .filter((k)-> k.getKid().equals("testkid"))
                .findAny().orElseThrow(()->new RuntimeException("Can't find certificate in JWKS."));
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