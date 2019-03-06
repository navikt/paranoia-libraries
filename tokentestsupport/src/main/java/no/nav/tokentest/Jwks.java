package no.nav.tokentest;

import java.util.List;

class Jwks {
    private List<Keys> keys;

    public List<Keys> getKeys() {
        return keys;
    }

    public void setKeys(List<Keys> keys) {
        this.keys = keys;
    }

    static class Keys {
        private String kty;
        private String e;
        private String use;
        private String kid;
        private String alg;
        private String n;

        public String getKty() {
            return kty;
        }

        public void setKty(String kty) {
            this.kty = kty;
        }

        public String getE() {
            return e;
        }

        public void setE(String e) {
            this.e = e;
        }

        public String getUse() {
            return use;
        }

        public void setUse(String use) {
            this.use = use;
        }

        public String getKid() {
            return kid;
        }

        public void setKid(String kid) {
            this.kid = kid;
        }

        public String getAlg() {
            return alg;
        }

        public void setAlg(String alg) {
            this.alg = alg;
        }

        public String getN() {
            return n;
        }

        public void setN(String n) {
            this.n = n;
        }
    }
}
