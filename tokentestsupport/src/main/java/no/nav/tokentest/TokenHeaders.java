package no.nav.tokentest;

import java.util.HashMap;
import java.util.Map;

public class TokenHeaders {
    private Map<String, Object> headersMap;
    private TokenHeaders(Map<String, Object> headersMap){
        this.headersMap = headersMap;
    }

    Map<String, Object> getHeadersMap() {
        return headersMap;
    }

    public static TokenHeadersBuilder builder() {
        return new TokenHeadersBuilder();
    }

    public static class TokenHeadersBuilder {
        private Map<String, Object> claimsMap = new HashMap<>();
        private TokenHeadersBuilder(){}

        public TokenHeadersBuilder withHeaders(Map<String, Object> claimsMap) {
            this.claimsMap.putAll(claimsMap);
            return this;
        }

        public TokenHeadersBuilder withKid(String kid) {
            claimsMap.put("kid", kid);
            return this;
        }

        public TokenHeadersBuilder withHeader(String key, Object value) {
            claimsMap.put(key, value);
            return this;
        }

        public TokenHeaders build() {
            return new TokenHeaders(claimsMap);
        }
    }
}
