package no.nav.tokentest;

import com.google.gson.Gson;
import io.jsonwebtoken.Jwts;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public class TokenHandlerTest {

    private TokenHandler tokenHandler = new TokenHandler();

    @Before
    public void setUp() {
        tokenHandler = new TokenHandler();
    }

    @Test
    public void testSigning() {
        var signedToken = tokenHandler.getSignedToken("{\"testname\":\"testvalue\"}");
    }

    @Test
    public void testSigningAndValidating() {
        var signedToken = tokenHandler.getSignedToken("testsignedid", "{\"testname\":\"testvalue2\"}", "testissuer2");

        var decodedToken = tokenHandler.validateAndParseToken(signedToken);
        Assert.assertEquals("testsignedid", decodedToken.getId());
        Assert.assertEquals("testissuer2", decodedToken.getIssuer());
        Assert.assertEquals("{\"testname\":\"testvalue2\"}", decodedToken.getSubject());
    }

    @Test
    public void testKeyFromJWKS() throws NoSuchAlgorithmException, InvalidKeySpecException {

        var gson = new Gson();
        var key = gson.fromJson(tokenHandler.getJWKS("testkid"), Jwks.class)
                .getKeys().stream()
                .filter((k)-> k.getKid().equals("testkid"))
                .findAny().orElseThrow(()->new RuntimeException("Finner ikke sertifikat i JWKS."));
        KeyFactory keyFactory = KeyFactory.getInstance(key.getKty());

        var decoder = Base64.getUrlDecoder();
        var spec = new RSAPublicKeySpec(new BigInteger(1, decoder.decode(key.getN())), new BigInteger(1, decoder.decode(key.getE())));
        var publicKeyFromJWKS = keyFactory.generatePublic(spec);
        var expctedPublicKey = tokenHandler.getPublicKey();
        Assert.assertEquals(expctedPublicKey, publicKeyFromJWKS);
    }

    @Test
    public void testValidatingFromJWKS() throws InvalidKeySpecException, NoSuchAlgorithmException {

        var signedToken = tokenHandler.getSignedToken("{\"testname\":\"testvalue3\"}");

        var gson = new Gson();
        var key = gson.fromJson(tokenHandler.getJWKS("testkid"), Jwks.class)
                .getKeys().stream()
                .filter((k)-> k.getKid().equals("testkid"))
                .findAny().orElseThrow(()->new RuntimeException("Finner ikke sertifikat i JWKS."));
        KeyFactory keyFactory = KeyFactory.getInstance(key.getKty());

        var decoder = Base64.getUrlDecoder();
        var spec = new RSAPublicKeySpec(new BigInteger(1, decoder.decode(key.getN())), new BigInteger(1, decoder.decode(key.getE())));
        var publicKeyFromJWKS = keyFactory.generatePublic(spec);

        var claims = Jwts.parser()
                .setSigningKey(publicKeyFromJWKS)
                .parseClaimsJws(signedToken).getBody();

        Assert.assertEquals("{\"testname\":\"testvalue3\"}", claims.getSubject());
    }
}