package no.nav.tokentest;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class TokenClaimsTest {

    @Test
    public void testTomClaims() {
        TokenClaims tc = TokenClaims.builder().build();
        assertEquals(0, tc.getClaimsMap().size());
    }

    @Test
    public void testWithClaim() {
        TokenClaims tc = TokenClaims.builder().withClaim("testkey", "testValue").build();
        assertEquals(1, tc.getClaimsMap().size());
        assertEquals("testValue", tc.getClaimsMap().get("testkey"));
    }

    @Test
    public void testWithClaims() {
        var claimsMap = new HashMap<String, Object>();
        claimsMap.put("key1", "value1");
        claimsMap.put("key2", "value2");
        TokenClaims tc = TokenClaims.builder().withClaims(claimsMap).build();
        assertEquals(2, tc.getClaimsMap().size());
        assertEquals("value1", tc.getClaimsMap().get("key1"));
        assertEquals("value2", tc.getClaimsMap().get("key2"));
    }

    @Test
    public void testWithDefaultClaims() {
        TokenClaims tc = TokenClaims.builder().withDefaultClaims().build();
        assertEquals(5, tc.getClaimsMap().size());
        assertEquals("tokentestsupport-TokenHandler", tc.getClaimsMap().get("iss"));
        assertEquals("testid", tc.getClaimsMap().get("jti"));
    }

    @Test
    public void testWithDefaultClaimsOverkjorbareWithClaim() {
        TokenClaims tc = TokenClaims.builder().withDefaultClaims().withClaim("iss", "iss2").build();
        assertEquals(5, tc.getClaimsMap().size());
        assertEquals("iss2", tc.getClaimsMap().get("iss"));
        assertEquals("testid", tc.getClaimsMap().get("jti"));
    }

    @Test
    public void testWithDefaultClaimsOverkjorbareWithClaims() {
        var claimsMap = new HashMap<String, Object>();
        claimsMap.put("iss", "iss3");
        TokenClaims tc = TokenClaims.builder().withDefaultClaims().withClaims(claimsMap).build();
        assertEquals(5, tc.getClaimsMap().size());
        assertEquals("iss3", tc.getClaimsMap().get("iss"));
        assertEquals("testid", tc.getClaimsMap().get("jti"));
    }
}