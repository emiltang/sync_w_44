package opn.threading.pleasesync;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;
import java.util.List;

/**
 * Main GUI, run for app
 * WindowMaker generated + manually written displayFactorization and dumpFactorizationInConsole methods at bottom
 *
 * @author ups
 */

public class MainGUI {

    private JFrame frame;
    private JTextArea textArea;

    /**
     * Create the application.
     */
    private MainGUI() {
        initialize();
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                MainGUI window = new MainGUI();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 900, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        JLabel lblPrimeNumberFactorization = new JLabel("Prime number factorization");
        lblPrimeNumberFactorization.setBounds(31, 27, 178, 16);
        frame.getContentPane().add(lblPrimeNumberFactorization);

        JLabel lblNumber = new JLabel("Number:");
        lblNumber.setBounds(31, 70, 61, 16);
        frame.getContentPane().add(lblNumber);

        final JTextField textField = new JTextField();
        textField.setText("1");
        textField.setBounds(104, 64, 134, 28);
        frame.getContentPane().add(textField);
        textField.setColumns(10);

        JLabel lblFactors = new JLabel("Factors:");
        lblFactors.setBounds(31, 118, 61, 16);
        frame.getContentPane().add(lblFactors);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(104, 107, 718, 450);
        frame.getContentPane().add(scrollPane);

        textArea = new JTextArea();
        textArea.setText("");
        scrollPane.setViewportView(textArea);

        JButton btnFactorize = new JButton("Factorize!");
        btnFactorize.addActionListener(e -> MainController.get().factorize(textField.getText(), MainGUI.this));
        btnFactorize.setBounds(308, 65, 117, 29);
        frame.getContentPane().add(btnFactorize);

    }

    /**
     * Utility method for displaying the result of a factorization in the GUI
     * NOTE: for the purposes of the exercise, this method is not to be modified!
     *
     * @param number the number that was being factorized
     * @param result the factorization result
     */
    @SuppressWarnings("WeakerAccess")
    public void displayFactorization(BigInteger number, List<BigInteger> result) {
        this.dumpFactorizationInConsole(number, result);
        EventQueue.invokeLater(() -> {
            textArea.append("Factorization of " + number + ": ");
            result.forEach(x -> textArea.append(x + " "));
            textArea.append("\n");
        });
    }

    /**
     * Utility method for displaying the result of a factorization on the console
     * NOTE: for the purposes of the exercise, this method is not to be modified!
     *
     * @param number the number that was being factorized
     * @param result the factorization result
     */
    private void dumpFactorizationInConsole(BigInteger number, List<BigInteger> result) {
        System.out.println("Factorization of " + number + ": ");
        result.stream().map(x -> x + " ").forEach(System.out::println);
        System.out.println("\n");
    }

    /**
     * Utility method for displaying statistics about factorization in the GUI
     * NOTE: for the purposes of the exercise, this method is not to be modified!
     *
     * @param millis        the number of milliseconds consumed by the operation
     * @param n_new_factors the number of new factors discovered in the operation
     */
    @SuppressWarnings("WeakerAccess")
    public void displayStatistics(long millis, int n_new_factors) {
        EventQueue.invokeLater(() ->
                textArea.append("Time taken: " + millis / 1000.0 + "s, new factors found: " + n_new_factors + "\n")
        );
    }

}
