package pl.touk.sputnik.processor.detekt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.touk.sputnik.configuration.Configuration;
import pl.touk.sputnik.configuration.ConfigurationBuilder;
import pl.touk.sputnik.review.Review;
import pl.touk.sputnik.review.ReviewFile;
import pl.touk.sputnik.review.ReviewFormatterFactory;
import pl.touk.sputnik.review.ReviewResult;
import pl.touk.sputnik.review.Severity;
import pl.touk.sputnik.review.Violation;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DetektProcessorTest {
    private static final String CONFIGURATION_WITH_KTLINT_ENABLED_AND_WITH_DETEKT_CONFIG_FILE = "detekt/configuration/configurationWithEnabledDetektAndDetektConfigFile.properties";
    private static final String CONFIGURATION_WITH_KTLINT_ENABLED_AND_WITHOUT_DETEKT_CONFIG_FILE = "detekt/configuration/configurationWithEnabledDetektAndWithoutDetektConfigFile.properties";

    private static final String VIOLATIONS_1 = "src/test/resources/detekt/testFiles/Violations1.kt";
    private static final String VIOLATIONS_2 = "src/test/resources/detekt/testFiles/sub/Violations2.kt";
    private static final String VIOLATIONS_3 = "src/test/resources/detekt/testFiles/Violations3.kt";
    private static final String VIOLATIONS_4 = "src/test/resources/detekt/testFiles/Violations4.kt";
    private static final String REVIEW_GROOVY_FILE = "src/test/resources/codeNarc/testFiles/FileWithOneViolationLevel2.groovy";

    private DetektProcessor sut;
    private Configuration config;

    @BeforeEach
    void setUp() {
        config = ConfigurationBuilder.initFromResource(CONFIGURATION_WITH_KTLINT_ENABLED_AND_WITH_DETEKT_CONFIG_FILE);
        sut = new DetektProcessor(config);
    }

    @Test
    void shouldReturnViolationsOnlyForOneRequestedFile() {
        Review review = getReview(VIOLATIONS_1);

        ReviewResult result = sut.process(review);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldReturnViolationsOnlyForRequestedFiles() {
        Review review = getReview(VIOLATIONS_2, VIOLATIONS_3);

        ReviewResult result = sut.process(review);

        assertThat(result).isNotNull();

    }

    @Test
    void shouldReturnGlobalScopeViolation() {
        Review review = getReview(VIOLATIONS_4);

        ReviewResult result = sut.process(review);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldReturnNoViolationsForNotKotlinFiles() {
        Review review = getReview(REVIEW_GROOVY_FILE);

        ReviewResult result = sut.process(review);

        assertThat(result).isNotNull();
        assertThat(result.getViolations()).isEmpty();
    }

    @Test
    void shouldReturnNoViolationsForEmptyReview() {
        Review review = getReview();

        ReviewResult result = sut.process(review);

        assertThat(result).isNotNull();
        assertThat(result.getViolations()).isEmpty();
    }

    @Test
    void shouldProcessReviewsOnDefaultConfig() {
        Configuration configWithoutDetektConfigFile =
                ConfigurationBuilder.initFromResource(CONFIGURATION_WITH_KTLINT_ENABLED_AND_WITHOUT_DETEKT_CONFIG_FILE);
        Review review = getReview(VIOLATIONS_1, VIOLATIONS_2, VIOLATIONS_3, REVIEW_GROOVY_FILE);

        DetektProcessor detektProcessor = new DetektProcessor(configWithoutDetektConfigFile);
        ReviewResult result = detektProcessor.process(review);

        assertThat(result).isNotNull();
    }

    private Review getReview(String... filePaths) {
        List<ReviewFile> files = new ArrayList<>();
        for (String filePath : filePaths) {
            files.add(new ReviewFile(filePath));
        }
        return new Review(files, ReviewFormatterFactory.get(config));
    }
}
