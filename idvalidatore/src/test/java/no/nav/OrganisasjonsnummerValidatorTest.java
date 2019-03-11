package no.nav;

import no.nav.idvalidator.OrganisasjonsnummerValidator;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OrganisasjonsnummerValidatorTest {
    OrganisasjonsnummerValidator validator = new OrganisasjonsnummerValidator();

    @Test
    public void testGyldigeOrganisasjonsnummer() {
        assertTrue(validator.test("135795310"));
        assertTrue(validator.test("999162681"));
        assertTrue(validator.test("889640782"));
        assertTrue(validator.test("998004993"));
        assertTrue(validator.test("974791854"));
        assertTrue(validator.test("921117795"));
        assertTrue(validator.test("917755736"));
        assertTrue(validator.test("995690217"));
        assertTrue(validator.test("991013628"));
        assertTrue(validator.test("993110469"));
    }

    @Test
    public void testUgyldigeOrganisasjonsnummer() {
        assertFalse(validator.test("889640780"));
        assertFalse(validator.test("135795311"));
        assertFalse(validator.test("135795312"));
        assertFalse(validator.test("135795313"));
        assertFalse(validator.test("135795314"));
        assertFalse(validator.test("135795315"));
        assertFalse(validator.test("135795316"));
        assertFalse(validator.test("135795317"));
        assertFalse(validator.test("135795318"));
        assertFalse(validator.test("135795319"));
    }

    @Test
    public void testUgyldigOrganisasjonsnummerMedMinusTegnForTiVerdi(){
        assertFalse(validator.test("88664422-"));
    }

    @Test
    public void testUgyldigOrganisasjonsnummerMedNullForTiVerdi(){
        assertFalse(validator.test("886644220"));
    }

    @Test
    public void testUgyldigOrganisasjonsnummerMedAForTiVerdi(){
        assertFalse(validator.test("88664422A"));
    }

    @Test
    public void testUgyldigOrganisasjonsnummerMedTiForTiVerdi(){
        assertFalse(validator.test("8866442210"));
    }

    @Test
    public void testUgyldigOrganisasjonsnummerMedTiSiffer(){
        assertFalse(validator.test("8896407820"));
    }

    @Test
    public void testUgyldigOrganisasjonsnummerMedAatteSiffer(){
        assertFalse(validator.test("88964078"));
    }

    @Test
    public void testUgyldigOrganisasjonsnummerMedBokstav(){
        assertFalse(validator.test("88964A782"));
    }

    @Test
    public void testUgyldigOrganisasjonsnummerMedSpesialtegn(){
        assertFalse(validator.test("88964{782"));
    }

    @Test
    public void testUgyldigOrganisasjonsnummerMedTomVerdi(){
        assertFalse(validator.test(""));
    }

    @Test
    public void testUgyldigOrganisasjonsnummerMedNullVerdi(){
        assertFalse(validator.test(null));
    }

    @Test
    public void testUgyldigOrganisasjonsnummerMedBokstaverForTall(){
        assertFalse(validator.test("bdfhjfdba"));
        assertFalse(validator.test("BDFHJFDBA"));
    }
}
