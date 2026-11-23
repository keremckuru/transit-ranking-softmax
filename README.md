# Transit Neighborhood Ranking with Softmax

A small, dependency-free Java demonstration of **multi-criteria scoring and Softmax normalization**. It compares three example neighborhoods in Kırklareli, Türkiye using five synthetic indicators, ranks their weighted scores, and displays normalized score shares.

> **Important:** This project uses **synthetic, manually assigned data and fixed weights**. It is **not a trained machine-learning model**, a real-world transport recommendation engine, or a route-finding algorithm. Softmax output is not a calibrated probability of a neighborhood being the best choice.

## What the program evaluates

| Criterion | Weight | Interpretation |
| --- | ---: | --- |
| Population density | `+0.30` | Higher example score is preferred |
| Transport infrastructure | `+0.20` | Higher is preferred |
| Cost | `-0.10` | Higher cost score reduces utility |
| Environmental impact | `+0.20` | Higher *benefit* score is preferred |
| Social benefit | `+0.20` | Higher is preferred |

Every input criterion is an illustrative score from `0` to `100`; values are not official measurements. **Positive** weights increase the total score and the **negative** cost weight decreases it. The weights are illustrative, not learned or validated.

## Method

First, compute a weighted sum for each neighborhood:

~~~text
score_i = 0.30 * population
        + 0.20 * infrastructure
        - 0.10 * cost
        + 0.20 * environmental_benefit
        + 0.20 * social_benefit
~~~

Then apply Softmax to all neighborhood scores:

~~~text
softmax(score_i / T) = exp(score_i / T) / sum_j(exp(score_j / T))
~~~

`T` is a positive **temperature** parameter (`T = 1` by default). Larger values produce a more even distribution; smaller values concentrate more weight on the highest-ranked option. The implementation subtracts the largest scaled score before calculating exponentials to improve numerical stability.

Softmax changes the *displayed shares*, not the ordering of distinct weighted scores.

## Example dataset

| Neighborhood | Population | Infrastructure | Cost | Environment | Social benefit | Weighted score |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| İstasyon Mahallesi | 80 | 75 | 60 | 70 | 85 | 64.0 |
| Karakaş Mahallesi | 70 | 65 | 55 | 80 | 75 | 59.5 |
| Pınar Mahallesi | 85 | 80 | 50 | 75 | 70 | 65.5 |

These are **illustrative values in the source code**, not a survey or public dataset.

## Run

Requires **JDK 17+**. No Maven or third-party dependencies.

From the repository root:

~~~bash
javac -encoding UTF-8 -d out src/Main.java
java -cp out Main
~~~

Example output (`T = 1`):

~~~text
Kırklareli Neighborhood Ranking (synthetic data)
Softmax temperature: 1.0
Rank | Neighborhood       | Weighted score | Softmax share
   1 | Pınar Mahallesi    |          65.50 |        81.59%
   2 | İstasyon Mahallesi |          64.00 |        18.21%
   3 | Karakaş Mahallesi  |          59.50 |         0.20%
Note: Softmax shares are not validated probabilities or actual route quality.
~~~

Try a different temperature:

~~~bash
java -cp out Main --temperature 5
~~~

This adjusts how concentrated the output shares are, **not** the underlying scores or ranking.

## Tests

~~~bash
javac -encoding UTF-8 -d out src/Main.java tests/MainTest.java
java -cp out MainTest
~~~

The test suite checks weighted scoring, score ordering, Softmax normalization, numerical stability for large inputs, temperature sensitivity, equal-score behavior, and invalid inputs. GitHub Actions runs the same checks on pushes and pull requests.

## Project structure

~~~text
.
├── src/Main.java                  # Weighted scoring, stable Softmax, CLI
├── tests/MainTest.java            # Dependency-free regression tests
├── .github/workflows/java.yml     # Compile and test in CI
└── README.md
~~~

## Limitations and next steps

- **This ranks neighborhoods, not routes.** There are no road networks, routes, public transit schedules, or travel times in the model.
- Scores and weights are hand-picked for demonstration. Without sourcing, validating, and weighting real evidence, the results must not guide public policy.
- The five criteria are simplified; environmental impact is treated as a *positive environmental benefit* scale.
- A future version could load a documented CSV, explore weight sensitivity, compare multiple decision methods, and evaluate robustness under plausible input uncertainty.

**Background:** The project started as a Java exercise exploring Softmax and has been refined to distinguish mathematical normalization from machine learning and real-world transit planning.
