package opn.threading.pleasesync;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for factorizing numbers represented as BigInteger.
 * No attention paid to efficiency (except the use of an unlimited-size cache)
 *
 * @author ups
 * based on http://stackoverflow.com/questions/12251962/prime-factorization-of-large-numbers
 */
class Factorizer {

    /**
     * Cache for storing previously computed results
     */
    private static Map<BigInteger, List<BigInteger>> cache = new HashMap<>();

    /**
     * Compute the factorization of the given number
     */
    @SuppressWarnings("WeakerAccess")
    public static synchronized List<BigInteger> primeFactors(BigInteger number) {
        if (cache.containsKey(number)) return cache.get(number);
        BigInteger n = number;
        List<BigInteger> factors = new ArrayList<>();
        for (BigInteger i = BigInteger.valueOf(2); i.compareTo(n.divide(i)) <= 0; i = i.add(BigInteger.ONE)) {
            while ((n.mod(i)).compareTo(BigInteger.ZERO) == 0) {
                factors.add(i);
                n = n.divide(i);
            }
        }
        if (n.compareTo(BigInteger.ONE) > 0) {
            factors.add(n);
        }
        cache.put(number, factors);
        return factors;
    }
}
