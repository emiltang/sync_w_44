package opn.threading.pleasesync;

import javax.swing.*;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * The controller of the application, containing the logic
 * NOTE: for the purposes of the exercise, this is the only class that is intended to be modified
 *
 * @author ups
 */
class MainController {

    /**
     * Number of subsequent factorization results computed with one click
     */
    private static final int N_RESULTS = 20;
    /**
     * Singleton pattern: creation
     */
    private static MainController instance = new MainController();
    /**
     * Set gathering all prime factors seen so far, used for displaying statistics
     */
    private final Set<BigInteger> allFactors = new HashSet<>();

    /**
     * Singleton pattern: access
     */
    @SuppressWarnings("WeakerAccess")
    public static MainController get() {
        return instance;
    }

    /**
     * Perform a factorization, request the GUI to display
     */
    @SuppressWarnings("WeakerAccess")
    public void factorize(String number_text, final MainGUI gui) {
        new Thread(() -> {
            try {
                final BigInteger number = new BigInteger(number_text);
                // Count the current number of factors
                int current_n_factors = allFactors.size();
                // Time the operations: begin
                Date startTime = new Date();
                // Compute and display the N_RESULTS factorizations

                IntStream.range(0, N_RESULTS).parallel().forEach(i -> {
                    BigInteger n = number.add(BigInteger.valueOf(i));
                    List<BigInteger> result = Factorizer.primeFactors(n);
                    synchronized (allFactors) {
                        allFactors.addAll(result);
                    }
                    gui.displayFactorization(n, result);
                });

                /*
                for (int i = 0; i < N_RESULTS; i++) {
                    List<BigInteger> result = Factorizer.primeFactors(number);
                    allFactors.addAll(result);
                    gui.displayFactorization(number, result);
                    number = number.add(BigInteger.ONE);
                }*/

                // Time the operations: end
                Date endTime = new Date();
                // Display statistics
                long millis = endTime.getTime() - startTime.getTime();
                gui.displayStatistics(millis, allFactors.size() - current_n_factors);
            } catch (NumberFormatException exn) {
                JOptionPane.showMessageDialog(null, "Illegal number: " + exn);
            }
        }).start();
    }

}
