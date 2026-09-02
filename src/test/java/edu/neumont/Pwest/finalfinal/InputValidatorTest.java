package edu.neumont.Pwest.finalfinal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the regex validation layer. Every method under test is static and free of
 * JavaFX and networking, so these run in milliseconds with no toolkit started.
 */
class InputValidatorTest {

    @Nested
    @DisplayName("Search box - accepted species names")
    class SpeciesHappyPath {

        @ParameterizedTest(name = "\"{0}\" resolves to {1}")
        @CsvSource({
                "pikachu,                pikachu",
                "Pikachu,                pikachu",
                "'  CHARIZARD  ',        charizard",
                "ho-oh,                  ho-oh",
                "porygon2,               porygon2",
                "porygon-z,              porygon-z",
                "deoxys-normal,          deoxys-normal",
                "jangmo-o,               jangmo-o",
                "nidoran-f,              nidoran-f",
                "urshifu-single-strike,  urshifu-single-strike"
        })
        void acceptsRealSpeciesNames(String typed, String expectedSlug) {
            InputValidator.SearchQuery result = InputValidator.parseSearch(typed);

            assertTrue(result.isValid(), "should have been accepted");
            assertEquals(InputValidator.QueryKind.SPECIES, result.getKind());
            assertEquals(expectedSlug, result.getApiQuery());
            assertNull(result.getProblem(), "a valid query has no problem message");
        }

        @ParameterizedTest(name = "\"{0}\" cleans up to {1}")
        @CsvSource({
                "'Mr. Mime',      mr-mime",
                "Farfetch''d,     farfetchd",
                "'Deoxys  Normal', deoxys-normal",
                "type_null,       type-null",
                "'Type: Null',    type-null"
        })
        void normalizesPunctuationAndSpacing(String typed, String expectedSlug) {
            InputValidator.SearchQuery result = InputValidator.parseSearch(typed);

            assertTrue(result.isValid(), "should have been accepted");
            assertEquals(expectedSlug, result.getApiQuery());
        }
    }

    @Nested
    @DisplayName("Search box - accepted Dex numbers")
    class DexHappyPath {

        @ParameterizedTest(name = "\"{0}\" is Dex #{1}")
        @CsvSource({
                "25,    25",
                "#25,   25",
                "0025,  25",
                "1,     1",
                "1025,  1025"
        })
        void acceptsDexNumbers(String typed, int expectedDex) {
            InputValidator.SearchQuery result = InputValidator.parseSearch(typed);

            assertTrue(result.isValid(), "should have been accepted");
            assertEquals(InputValidator.QueryKind.DEX, result.getKind());
            assertEquals(expectedDex, result.getDexNumber());
            assertEquals(String.valueOf(expectedDex), result.getApiQuery(),
                    "leading zeros and # should be stripped before hitting the API");
        }

        @Test
        void speciesQueriesReportNoDexNumber() {
            assertEquals(-1, InputValidator.parseSearch("pikachu").getDexNumber());
        }
    }

    @Nested
    @DisplayName("Search box - rejected input")
    class RejectedInput {

        @ParameterizedTest(name = "rejects \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {
                "   ",
                "-",
                "---",
                "-pikachu",
                "pikachu-",
                "pika--chu",
                "z",
                "!!!",
                "<script>alert(1)</script>",
                "../../etc/passwd",
                "pikachu; DROP TABLE mons"
        })
        void rejectsMalformedInput(String typed) {
            InputValidator.SearchQuery result = InputValidator.parseSearch(typed);

            assertFalse(result.isValid(), "should have been rejected");
            assertEquals(InputValidator.QueryKind.INVALID, result.getKind());
        }

        @ParameterizedTest(name = "rejects out-of-range Dex \"{0}\"")
        @ValueSource(strings = {"0", "1026", "9999"})
        void rejectsDexNumbersOutsideTheDex(String typed) {
            assertFalse(InputValidator.parseSearch(typed).isValid());
        }

        @Test
        void boundaryDexNumbersAreHandledCorrectly() {
            assertTrue(InputValidator.parseSearch("1025").isValid(), "1025 is the last Pokemon");
            assertFalse(InputValidator.parseSearch("1026").isValid(), "1026 does not exist");
        }

        @Test
        void rejectionExplainsWhy() {
            String problem = InputValidator.parseSearch("!!!").getProblem();

            assertNull(InputValidator.parseSearch("pikachu").getProblem());
            assertTrue(problem != null && !problem.isBlank(),
                    "a rejected query must tell the user what was wrong");
        }
    }

    @Nested
    @DisplayName("IV range 0-31")
    class IvRange {

        @ParameterizedTest
        @ValueSource(strings = {"0", "1", "9", "10", "29", "30", "31"})
        void acceptsValuesInRange(String text) {
            assertTrue(InputValidator.isValidIv(text));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"32", "40", "99", "100", "-1", "3.5", "abc", "031", " 5"})
        void rejectsEverythingElse(String text) {
            assertFalse(InputValidator.isValidIv(text));
        }

        @Test
        void boundariesAreExact() {
            assertTrue(InputValidator.isValidIv("31"), "31 is the max IV");
            assertFalse(InputValidator.isValidIv("32"), "32 is one past the max");
        }
    }

    @Nested
    @DisplayName("EV range 0-255")
    class EvRange {

        @ParameterizedTest
        @ValueSource(strings = {"0", "9", "99", "100", "199", "200", "249", "250", "255"})
        void acceptsValuesInRange(String text) {
            assertTrue(InputValidator.isValidEv(text));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"256", "260", "300", "999", "-4", "abc", "25a"})
        void rejectsEverythingElse(String text) {
            assertFalse(InputValidator.isValidEv(text));
        }

        @Test
        void boundariesAreExact() {
            assertTrue(InputValidator.isValidEv("255"), "255 is the max EV");
            assertFalse(InputValidator.isValidEv("256"), "256 is one past the max");
        }
    }

    @Nested
    @DisplayName("Mid-typing filters used by the spinner text fields")
    class TypingFilters {

        @ParameterizedTest
        @ValueSource(strings = {"", "0", "05", "3", "31", "0031"})
        void ivFilterAllowsPartialEntry(String text) {
            assertTrue(InputValidator.isIvInProgress(text));
        }

        @ParameterizedTest
        @ValueSource(strings = {"32", "99", "a", "-"})
        void ivFilterStillBlocksIllegalValues(String text) {
            assertFalse(InputValidator.isIvInProgress(text));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "0", "025", "255", "0255"})
        void evFilterAllowsPartialEntry(String text) {
            assertTrue(InputValidator.isEvInProgress(text));
        }

        @ParameterizedTest
        @ValueSource(strings = {"256", "999", "a"})
        void evFilterStillBlocksIllegalValues(String text) {
            assertFalse(InputValidator.isEvInProgress(text));
        }

        @Test
        void emptyIsAllowedWhileTypingButNotAsAFinalValue() {
            assertTrue(InputValidator.isIvInProgress(""), "backspacing to empty must be allowed");
            assertFalse(InputValidator.isValidIv(""), "but empty is not a committed value");
        }
    }

    @Nested
    @DisplayName("Display formatting")
    class Prettify {

        @ParameterizedTest(name = "{0} -> {1}")
        @CsvSource({
                "ultra-sun,        Ultra Sun",
                "special-attack,   Special Attack",
                "pikachu,          Pikachu",
                "porygon2,         Porygon2",
                "thunder-shock,    Thunder Shock",
                "urshifu-single-strike, Urshifu Single Strike"
        })
        void turnsApiSlugsIntoReadableNames(String slug, String expected) {
            assertEquals(expected, InputValidator.prettify(slug));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void handlesMissingNames(String slug) {
            assertEquals("", InputValidator.prettify(slug));
        }
    }

    @Nested
    @DisplayName("normalize()")
    class Normalize {

        @Test
        void nullBecomesEmptyRatherThanThrowing() {
            assertEquals("", InputValidator.normalize(null));
        }

        @Test
        void collapsesRunsOfWhitespace() {
            assertEquals("deoxys-normal", InputValidator.normalize("  Deoxys   Normal  "));
        }

        @Test
        void leavesHyphenStructureAloneSoThePatternCanJudgeIt() {
            // normalize only fixes formatting; validity is the regex's job
            assertEquals("-pikachu", InputValidator.normalize("-Pikachu"));
            assertFalse(InputValidator.parseSearch("-Pikachu").isValid());
        }
    }

    @Nested
    @DisplayName("looseSearch() move filtering")
    class LooseSearch {

        @Test
        void matchesSubstringsCaseInsensitively() {
            Pattern pattern = InputValidator.looseSearch("THUNDER");
            assertTrue(pattern.matcher("thunder-punch").matches());
            assertFalse(pattern.matcher("quick-attack").matches());
        }

        @Test
        void treatsRegexMetacharactersAsLiteralText() {
            // Without Pattern.quote this line throws PatternSyntaxException instead of failing to match
            Pattern pattern = InputValidator.looseSearch("(");
            assertFalse(pattern.matcher("thunder-punch").matches());
            assertTrue(pattern.matcher("weird(move").matches());
        }

        @Test
        void handlesNullInput() {
            assertTrue(InputValidator.looseSearch(null).matcher("anything").matches());
        }
    }
}
