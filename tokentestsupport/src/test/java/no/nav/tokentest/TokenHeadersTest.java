package no.nav.tokentest;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class TokenHeadersTest {
    
    @Test
    public void testTomHeaders() {
        TokenHeaders th = TokenHeaders.builder().build();
        assertEquals(0, th.getHeadersMap().size());
    }

    @Test
    public void testWithHeader() {
        TokenHeaders th = TokenHeaders.builder().withHeader("testkey", "testValue").build();
        assertEquals(1, th.getHeadersMap().size());
        assertEquals("testValue", th.getHeadersMap().get("testkey"));
    }

    @Test
    public void testWithHeaders() {
        var claimsMap = new HashMap<String, Object>();
        claimsMap.put("key1", "value1");
        claimsMap.put("key2", "value2");
        TokenHeaders th = TokenHeaders.builder().withHeaders(claimsMap).build();
        assertEquals(2, th.getHeadersMap().size());
        assertEquals("value1", th.getHeadersMap().get("key1"));
        assertEquals("value2", th.getHeadersMap().get("key2"));
    }

    @Test
    public void testWithKid() {
        TokenHeaders th = TokenHeaders.builder().withKid("testkid").build();
        assertEquals(1, th.getHeadersMap().size());
        assertEquals("testkid", th.getHeadersMap().get("kid"));
    }
}
