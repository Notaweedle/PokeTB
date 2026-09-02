package edu.neumont.Pwest.finalfinal;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the JSON parsing half of APIController against a saved response, so nothing
 * here touches the network and the results don't change when PokeAPI does.
 */
class APIControllerTest {

    /** Trimmed-down copy of a real /pokemon/25 response. */
    private static final String PIKACHU_JSON =
            "{"
          + "\"name\":\"pikachu\","
          + "\"id\":25,"
          + "\"sprites\":{"
          +   "\"front_default\":\"https://img/25.png\","
          +   "\"front_shiny\":\"https://img/25-shiny.png\"},"
          + "\"stats\":["
          +   "{\"base_stat\":35,\"stat\":{\"name\":\"hp\"}},"
          +   "{\"base_stat\":55,\"stat\":{\"name\":\"attack\"}},"
          +   "{\"base_stat\":40,\"stat\":{\"name\":\"defense\"}},"
          +   "{\"base_stat\":50,\"stat\":{\"name\":\"special-attack\"}},"
          +   "{\"base_stat\":50,\"stat\":{\"name\":\"special-defense\"}},"
          +   "{\"base_stat\":90,\"stat\":{\"name\":\"speed\"}}"
          + "],"
          + "\"abilities\":["
          +   "{\"ability\":{\"name\":\"static\"}},"
          +   "{\"ability\":{\"name\":\"lightning-rod\"}}"
          + "],"
          + "\"moves\":["
          +   "{\"move\":{\"name\":\"thunder-shock\"}},"
          +   "{\"move\":{\"name\":\"quick-attack\"}}"
          + "],"
          + "\"game_indices\":["
          +   "{\"version\":{\"name\":\"red\"}},"
          +   "{\"version\":{\"name\":\"blue\"}}"
          + "]"
          + "}";

    /**
     * Some alternate forms come back with an empty game_indices array -- this is the
     * shape that used to be worth worrying about, so it gets its own fixture.
     */
    private static final String NO_GAMES_JSON = PIKACHU_JSON.replace(
            "\"game_indices\":[{\"version\":{\"name\":\"red\"}},{\"version\":{\"name\":\"blue\"}}]",
            "\"game_indices\":[]");

    private APIController api;

    @BeforeEach
    void setUp() {
        api = new APIController();
    }

    @Nested
    @DisplayName("buildPokemonFrom()")
    class BuildPokemon {

        @Test
        void readsEveryFieldOffASavedResponse() {
            Pokemon mon = api.buildPokemonFrom(PIKACHU_JSON);

            assertEquals("pikachu", mon.getName());
            assertEquals(25, mon.getNationalDexNumber());
            assertEquals("https://img/25.png", mon.getSpriteUrl());
            assertEquals("https://img/25-shiny.png", mon.getShinySpriteUrl());
        }

        @Test
        void mapsEachBaseStatToTheRightField() {
            Pokemon mon = api.buildPokemonFrom(PIKACHU_JSON);

            assertEquals(35, mon.getHp());
            assertEquals(55, mon.getAttack());
            assertEquals(40, mon.getDefense());
            assertEquals(50, mon.getSpAttack());
            assertEquals(50, mon.getSpDefense());
            assertEquals(90, mon.getSpeed());
        }

        @Test
        void collectsAbilitiesMovesAndGames() {
            Pokemon mon = api.buildPokemonFrom(PIKACHU_JSON);

            assertEquals(List.of("static", "lightning-rod"), mon.getAbilities());
            assertEquals(List.of("thunder-shock", "quick-attack"), mon.getLearnableMoves());
            assertEquals(List.of("red", "blue"), mon.getGamesIncludedIn());
        }

        @Test
        void survivesAFormWithNoGameIndices() {
            Pokemon mon = api.buildPokemonFrom(NO_GAMES_JSON);

            assertTrue(mon.getGamesIncludedIn().isEmpty(),
                    "an empty game_indices array should give an empty list, not blow up");
            assertEquals("pikachu", mon.getName(), "the rest of the response still parses");
        }

        @Test
        void rejectsAResponseThatIsMissingItsStats() {
            String noStats = PIKACHU_JSON.replaceAll("\"stats\":\\[.*?\\],", "\"stats\":[],");

            assertThrows(IllegalStateException.class, () -> api.buildPokemonFrom(noStats));
        }
    }

    @Nested
    @DisplayName("findStat()")
    class FindStat {

        private JsonArray stats() {
            return JsonParser.parseString(PIKACHU_JSON)
                    .getAsJsonObject()
                    .get("stats")
                    .getAsJsonArray();
        }

        @ParameterizedTest(name = "{0} = {1}")
        @CsvSource({
                "hp,               35",
                "attack,           55",
                "defense,          40",
                "special-attack,   50",
                "special-defense,  50",
                "speed,            90"
        })
        void findsEachStatByName(String statName, int expected) {
            assertEquals(expected, api.findStat(stats(), statName));
        }

        @Test
        void throwsWhenTheStatIsNotPresent() {
            IllegalStateException thrown = assertThrows(IllegalStateException.class,
                    () -> api.findStat(stats(), "friendship"));

            assertTrue(thrown.getMessage().contains("friendship"),
                    "the error should name the stat it couldn't find");
        }

        @Test
        void throwsOnAnEmptyStatsArray() {
            assertThrows(IllegalStateException.class,
                    () -> api.findStat(new JsonArray(), "hp"));
        }

        @Test
        void isCaseSensitiveLikeTheApiItself() {
            assertThrows(IllegalStateException.class, () -> api.findStat(stats(), "HP"));
        }
    }

    @Nested
    @DisplayName("extractNames()")
    class ExtractNames {

        private JsonArray arrayFrom(String field) {
            JsonObject root = JsonParser.parseString(PIKACHU_JSON).getAsJsonObject();
            return root.get(field).getAsJsonArray();
        }

        @Test
        void pullsTheNameOutOfEachNestedObject() {
            assertEquals(List.of("static", "lightning-rod"),
                    api.extractNames(arrayFrom("abilities"), "ability"));
        }

        @Test
        void keepsTheOriginalOrder() {
            assertEquals(List.of("thunder-shock", "quick-attack"),
                    api.extractNames(arrayFrom("moves"), "move"));
        }

        @Test
        void returnsAnEmptyListForAnEmptyArray() {
            assertTrue(api.extractNames(new JsonArray(), "ability").isEmpty());
        }

        @Test
        void throwsWhenTheInnerKeyIsWrong() {
            assertThrows(RuntimeException.class,
                    () -> api.extractNames(arrayFrom("abilities"), "move"));
        }
    }
}
