import java.util.Arrays;
import java.util.Locale;

/**
 * Educational multi-criteria neighborhood ranking with fixed weights and Softmax.
 * The inputs are synthetic; this does not train a model or calculate real routes.
 */
public class Main {
    // Population, transport infrastructure, cost, environmental impact, social benefit.
    private static final double[] WEIGHTS = {0.30, 0.20, -0.10, 0.20, 0.20};

    /** Computes a weighted score from five synthetic 0..100 criteria. */
    public static double weightedScore(double[] criteria) {
        if (criteria == null || criteria.length != WEIGHTS.length) {
            throw new IllegalArgumentException("Exactly five criteria are required.");
        }

        double score = 0.0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            if (!Double.isFinite(criteria[i]) || criteria[i] < 0 || criteria[i] > 100) {
                throw new IllegalArgumentException("Each criterion must be finite and within [0, 100].");
            }
            score += criteria[i] * WEIGHTS[i];
        }
        return score;
    }

    /**
     * Converts finite scores to non-negative shares that sum to 1.
     * Subtracting the maximum scaled score avoids overflow in exp().
     */
    public static double[] softmax(double[] scores, double temperature) {
        if (scores == null || scores.length == 0) {
            throw new IllegalArgumentException("At least one score is required.");
        }
        if (!Double.isFinite(temperature) || temperature <= 0) {
            throw new IllegalArgumentException("Temperature must be positive and finite.");
        }

        double[] scaled = new double[scores.length];
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < scores.length; i++) {
            scaled[i] = scores[i] / temperature;
            if (!Double.isFinite(scaled[i])) {
                throw new IllegalArgumentException("Scores must yield finite scaled values.");
            }
            max = Math.max(max, scaled[i]);
        }

        double sum = 0.0;
        double[] shares = new double[scores.length];
        for (int i = 0; i < shares.length; i++) {
            shares[i] = Math.exp(scaled[i] - max);
            sum += shares[i];
        }
        for (int i = 0; i < shares.length; i++) {
            shares[i] /= sum;
        }
        return shares;
    }

    private static double readTemperature(String[] args) {
        if (args.length == 0) {
            return 1.0;
        }
        if (args.length != 2 || !"--temperature".equals(args[0])) {
            throw new IllegalArgumentException("Usage: java -cp out Main [--temperature <positive-number>]");
        }
        try {
            double value = Double.parseDouble(args[1]);
            if (!Double.isFinite(value) || value <= 0) {
                throw new IllegalArgumentException("Temperature must be positive and finite.");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Temperature must be a valid number.", exception);
        }
    }

    public static void main(String[] args) {
        double temperature = readTemperature(args);

        String[] neighborhoods = {
                "İstasyon Mahallesi", "Karakaş Mahallesi", "Pınar Mahallesi"
        };
        // Synthetic examples; not observations or official Kırklareli transport data.
        double[][] criteria = {
                {80, 75, 60, 70, 85},
                {70, 65, 55, 80, 75},
                {85, 80, 50, 75, 70}
        };

        double[] scores = new double[neighborhoods.length];
        for (int i = 0; i < neighborhoods.length; i++) {
            scores[i] = weightedScore(criteria[i]);
        }
        double[] shares = softmax(scores, temperature);

        Integer[] order = {0, 1, 2};
        Arrays.sort(order, (a, b) -> Double.compare(shares[b], shares[a]));

        System.out.println("Kırklareli Neighborhood Ranking (synthetic data)");
        System.out.printf(Locale.US, "Softmax temperature: %.1f%n", temperature);
        System.out.println("Rank | Neighborhood       | Weighted score | Softmax share");
        for (int rank = 0; rank < order.length; rank++) {
            int i = order[rank];
            System.out.printf(Locale.US, "%4d | %-18s | %14.2f | %12.2f%%%n",
                    rank + 1, neighborhoods[i], scores[i], shares[i] * 100);
        }
        System.out.println("Note: Softmax shares are not validated probabilities or actual route quality.");
    }
}
