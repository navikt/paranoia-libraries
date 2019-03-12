package no.nav.idvalidator;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class OrganisasjonsnummerValidator implements Predicate<String> {

    @Override
    public boolean test(String orgnr) {
        if(orgnr == null ||!orgnr.matches("\\d{9}") ) return false;
        List<Integer> vekttall = List.of(3,2,7,6,5,4,3,2);
        int sum = IntStream.range(0,orgnr.length()-1)
                .map(i -> (Integer.parseInt(orgnr.substring(i, i + 1)) * vekttall.get(i)))
                .sum();
        int rest = sum % 11;
        if(rest== 0 && Integer.parseInt(orgnr.substring(orgnr.length()-1)) == rest) return true;
        return Integer.parseInt(orgnr.substring(orgnr.length()-1))== 11 - rest;
    }
}
