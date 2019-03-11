package no.nav;

import no.nav.idvalidator.OrganisasjonsnummerValidator;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OrganisasjonsnummerValidatorTest {
    OrganisasjonsnummerValidator validator = new OrganisasjonsnummerValidator();

    @Test
    public void testGyldigeOrganisasjonsnummer() {
        assertTrue(validator.test("889640782"));
        assertTrue(validator.test("135795310"));
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
