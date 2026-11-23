/** Dependency-free regression tests. Run with java -cp out MainTest. */
public class MainTest {
    private static int passed = 0;

    private static void assertClose(double expected, double actual, double tolerance, String message) {
        if (!Double.isFinite(actual) || Math.abs(expected - actual) > tolerance) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
        passed++;
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
        passed++;
    }

    private static void assertInvalid(Runnable operation, String message) {
        try {
            operation.run();
        } catch (IllegalArgumentException expected) {
            passed++;
            return;
        }
        throw new AssertionError(message + ": expected IllegalArgumentException");
    }

    public static void main(String[] args) {
        double first = Main.weightedScore(new double[]{80, 75, 60, 70, 85});
        double second = Main.weightedScore(new double[]{70, 65, 55, 80, 75});
        double third = Main.weightedScore(new double[]{85, 80, 50, 75, 70});
        assertClose(64.0, first, 1e-9, "İstasyon score");
        assertClose(59.5, second, 1e-9, "Karakaş score");
        assertClose(65.5, third, 1e-9, "Pınar score");

        double[] shares = Main.softmax(new double[]{first, second, third}, 1.0);
        assertClose(1.0, shares[0] + shares[1] + shares[2], 1e-12, "Shares sum to 1");
        assertTrue(shares[2] > shares[0] && shares[0] > shares[1],
                "Ranking follows weighted score ordering");
        assertClose(0.8159209597316858, shares[2], 1e-9, "Sample Pınar share");

        double[] large = Main.softmax(new double[]{1000, 1001}, 1.0);
        assertClose(1.0, large[0] + large[1], 1e-12, "Stable for large values");
        assertClose(0.7310585786300049, large[1], 1e-12, "Correct shifted Softmax");

        double[] warm = Main.softmax(new double[]{first, second, third}, 10.0);
        assertTrue(warm[2] < shares[2], "Higher temperature reduces concentration");
        assertClose(0.5, Main.softmax(new double[]{10, 10}, 1.0)[0], 1e-12,
                "Equal scores yield equal shares");

        assertInvalid(() -> Main.weightedScore(new double[]{1, 2}), "Missing criteria rejected");
        assertInvalid(() -> Main.weightedScore(new double[]{1, 2, 3, 4, -1}),
                "Negative criterion rejected");
        assertInvalid(() -> Main.softmax(new double[]{1, 2}, 0),
                "Zero temperature rejected");
        assertInvalid(() -> Main.softmax(new double[]{1, Double.NaN}, 1),
                "Non-finite score rejected");
        assertInvalid(() -> Main.softmax(new double[0], 1), "Empty scores rejected");

        System.out.println("All " + passed + " assertions passed.");
    }
}
