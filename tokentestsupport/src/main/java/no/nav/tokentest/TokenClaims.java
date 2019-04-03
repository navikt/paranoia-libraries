package no.nav.tokentest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class TokenClaims {
    private Map<String, Object> claimsMap;
    private TokenClaims(Map<String, Object> claimsMap){
        this.claimsMap = claimsMap;
    }

    Map<String, Object> getClaimsMap() {
        return claimsMap;
    }

    public static TokenClaimsBuilder builder() {
        return new TokenClaimsBuilder();
    }

    public static class TokenClaimsBuilder {
        private HashMap<String, Object> claimsMap = new HashMap<>();
        private TokenClaimsBuilder(){}

        public TokenClaimsBuilder withClaims(Map<String, Object> claimsMap) {
            this.claimsMap.putAll(claimsMap);
            return this;
        }

        public TokenClaimsBuilder withDefaultClaims() {
            claimsMap.putAll(defaultClaims());
            return this;
        }

        public TokenClaimsBuilder withClaim(String key, Object value) {
            claimsMap.put(key, value);
            return this;
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

        public TokenClaims build() {
            return new TokenClaims(claimsMap);
        }
    }
}
