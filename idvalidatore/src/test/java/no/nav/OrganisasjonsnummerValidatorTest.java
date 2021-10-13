package no.nav;

import no.nav.idvalidator.OrganisasjonsnummerValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrganisasjonsnummerValidatorTest {
    OrganisasjonsnummerValidator validator = new OrganisasjonsnummerValidator();

    @Test
    public void testGyldigeOrganisasjonsnummer() {
        for (String orgNr : gyldigeOrgNummer())
            assertTrue(validator.test(orgNr), orgNr + " er et gyldig organisasjonsnummer, og skulle gitt true som svar");
    }

    private List<String> gyldigeOrgNummer() {
        return List.of("123456785",
                "135795313",
                "999263550",
                "999162681",
                "889640782",
                "998004993",
                "974791854",
                "921117795",
                "917755736",
                "995690217",
                "991013628",
                "993110469");
    }

    @Test
    public void testUgyldigeOrganisasjonsnummer() {
        for (String orgNr : ugyldigeOrgNummer())
            assertFalse(validator.test(orgNr), orgNr + " er et ugyldig organisasjonsnummer, og skulle gitt false som svar");
    }

    private List<String> ugyldigeOrgNummer() {
        return List.of("889640780",
                "135795310",
                "135795311",
                "135795312",
                "135795314",
                "135795315",
                "135795316",
                "135795317",
                "135795318",
                "135795319");
    }

    @Test
    public void testUgyldigOrganisasjonsnummerMedMinusTegnForTiVerdi() {
        assertFalse(validator.test("88664422-"));
    }

    @Test
    public void testUgyldigOrganisasjonsnummerMedNullForTiVerdi() {
        assertFalse(validator.test("886644220"));
    }

    @Test
    public void testUgyldigOrganisasjonsnummerMedAForTiVerdi() {
        assertFalse(validator.test("88664422A"));
    }

    @Test
    public void testUgyldigOrganisasjonsnummerMedTiForTiVerdi() {
        assertFalse(validator.test("8866442210"));
    }

    @Test
    public void testUgyldigOrganisasjonsnummerMedTiSiffer() {
        assertFalse(validator.test("8896407820"));
    }

    @Test
    public void testUgyldigOrganisasjonsnummerMedAatteSiffer() {
        assertFalse(validator.test("88964078"));
    }

    @Test
    public void testUgyldigOrganisasjonsnummerMedBokstav() {
        assertFalse(validator.test("88964A782"));
        assertFalse(validator.test("88964A786"));
    }

    @Test
    public void testUgyldigOrganisasjonsnummerMedSpesialtegn() {
        assertFalse(validator.test("88964{782"));
        assertFalse(validator.test("88964{786"));
    }

    @Test
    public void testUgyldigOrganisasjonsnummerMedTomVerdi() {
        assertFalse(validator.test(""));
    }

    @Test
    public void testUgyldigOrganisasjonsnummerMedNullVerdi() {
        assertFalse(validator.test(null));
    }

    @Test
    public void testUgyldigOrganisasjonsnummerMedBokstaverForTall() {
        assertFalse(validator.test("bdfhjfdba"));
        assertFalse(validator.test("BDFHJFDBA"));
        assertFalse(validator.test("bdfhjfdb2"));
        assertFalse(validator.test("BDFHJFDB2"));
    }
}
