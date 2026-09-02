package edu.neumont.Pwest.finalfinal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import edu.neumont.Pwest.finalfinal.TeamBuilderController.TeamSlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Round-trip tests for the two things that get written to disk.
 *
 * These write into a JUnit @TempDir, never the real ~/Documents/PokeTracker folder, so
 * running the suite can't damage a saved team.
 *
 * Note: surefire runs tests on the classpath, where module-info.java is ignored. That
 * means these confirm the *serialization* is correct but cannot catch a missing
 * "opens ... to com.google.gson" -- that only shows up when the app runs on the module path.
 */
class PersistenceTest {

    private static TeamSlot slotFor(String species) {
        TeamSlot slot = new TeamSlot();
        slot.setSpeciesName(species);
        slot.setShiny(true);
        slot.setMove1("thunder-shock");
        slot.setMove2("quick-attack");
        slot.setMove3(null);
        slot.setMove4(null);
        slot.setAbility("static");
        slot.setNature("Timid");
        slot.setHpIv(31);
        slot.setAtkIv(0);
        slot.setDefIv(31);
        slot.setSpAtkIv(31);
        slot.setSpDefIv(31);
        slot.setSpdIv(31);
        slot.setHpEv(4);
        slot.setAtkEv(0);
        slot.setDefEv(0);
        slot.setSpAtkEv(252);
        slot.setSpDefEv(0);
        slot.setSpdEv(252);
        return slot;
    }

    @Nested
    @DisplayName("Team save file")
    class TeamRoundTrip {

        @Test
        void everyFieldSurvivesAWriteAndRead(@TempDir Path folder) throws IOException {
            TeamSlot[] original = new TeamSlot[6];
            original[0] = slotFor("pikachu");

            TeamSlot[] loaded = writeThenRead(original, folder.resolve("team.json"));

            TeamSlot slot = loaded[0];
            assertNotNull(slot);
            assertEquals("pikachu", slot.getSpeciesName());
            assertTrue(slot.isShiny());
            assertEquals("thunder-shock", slot.getMove1());
            assertEquals("quick-attack", slot.getMove2());
            assertNull(slot.getMove3());
            assertEquals("static", slot.getAbility());
            assertEquals("Timid", slot.getNature());
            assertEquals(31, slot.getHpIv());
            assertEquals(0, slot.getAtkIv());
            assertEquals(252, slot.getSpAtkEv());
            assertEquals(252, slot.getSpdEv());
            assertEquals(4, slot.getHpEv());
        }

        @Test
        void emptySlotsStayEmptyAndTheTeamStaysSixLong(@TempDir Path folder) throws IOException {
            TeamSlot[] original = new TeamSlot[6];
            original[0] = slotFor("pikachu");
            original[3] = slotFor("charizard");

            TeamSlot[] loaded = writeThenRead(original, folder.resolve("team.json"));

            assertEquals(6, loaded.length);
            assertEquals("pikachu", loaded[0].getSpeciesName());
            assertEquals("charizard", loaded[3].getSpeciesName());
            assertNull(loaded[1]);
            assertNull(loaded[2]);
            assertNull(loaded[4]);
            assertNull(loaded[5]);
        }

        @Test
        void anEntirelyEmptyTeamRoundTrips(@TempDir Path folder) throws IOException {
            TeamSlot[] loaded = writeThenRead(new TeamSlot[6], folder.resolve("team.json"));

            assertEquals(6, loaded.length);
            assertArrayEquals(new TeamSlot[6], loaded);
        }

        @Test
        void theFileItWritesIsActuallyReadableJson(@TempDir Path folder) throws IOException {
            Path file = folder.resolve("team.json");
            TeamSlot[] team = new TeamSlot[6];
            team[0] = slotFor("pikachu");

            write(team, file);

            String text = Files.readString(file);
            assertTrue(Files.size(file) > 0, "a save that writes nothing is the bug we already hit once");
            assertTrue(text.contains("pikachu"));
            assertTrue(text.trim().startsWith("["), "the team is stored as a JSON array");
        }
    }

    @Nested
    @DisplayName("Corrupt or missing save files")
    class BadInput {

        @Test
        void anEmptyFileReadsAsNullSoTheAppCanFallBack(@TempDir Path folder) throws IOException {
            Path file = folder.resolve("team.json");
            Files.writeString(file, "");

            try (Reader reader = Files.newBufferedReader(file)) {
                assertNull(new Gson().fromJson(reader, TeamSlot[].class),
                        "loadTeam() relies on this being null rather than throwing");
            }
        }

        @Test
        void malformedJsonThrowsSomethingLoadTeamCatches(@TempDir Path folder) throws IOException {
            Path file = folder.resolve("team.json");
            Files.writeString(file, "{ this is not json ");

            try (Reader reader = Files.newBufferedReader(file)) {
                assertThrows(JsonSyntaxException.class,
                        () -> new Gson().fromJson(reader, TeamSlot[].class));
            }
        }

        @Test
        void aShortTeamArrayIsPaddedBackToSix() {
            // Mirrors the guard in loadTeam(): an older save might hold fewer than six slots
            TeamSlot[] loaded = new Gson().fromJson("[null,null]", TeamSlot[].class);
            assertEquals(2, loaded.length, "Gson gives back exactly what was in the file");

            TeamSlot[] sized = new TeamSlot[6];
            System.arraycopy(loaded, 0, sized, 0, Math.min(loaded.length, 6));
            assertEquals(6, sized.length);
        }
    }

    @Nested
    @DisplayName("Tracker save file")
    class TrackerRoundTrip {

        @Test
        void trackedEntriesSurviveAWriteAndRead(@TempDir Path folder) throws IOException {
            Path file = folder.resolve("tracker.json");
            TrackedEntry entry = new TrackedEntry("leafeon", 470, List.of("x", "y", "sword"));
            entry.setCount(7);
            entry.setGame("sword");

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (Writer writer = Files.newBufferedWriter(file)) {
                gson.toJson(List.of(entry), writer);
            }

            TrackedEntry[] loaded;
            try (Reader reader = Files.newBufferedReader(file)) {
                loaded = new Gson().fromJson(reader, TrackedEntry[].class);
            }

            assertEquals(1, loaded.length);
            assertEquals("leafeon", loaded[0].getSpeciesName());
            assertEquals(470, loaded[0].getDexNumber());
            assertEquals(7, loaded[0].getCount());
            assertEquals("sword", loaded[0].getGame());
            assertEquals(List.of("x", "y", "sword"), loaded[0].getAvailableGames());
        }

        @Test
        void aCountOfZeroIsPreservedRatherThanDropped(@TempDir Path folder) throws IOException {
            Path file = folder.resolve("tracker.json");
            TrackedEntry entry = new TrackedEntry("eevee", 133, List.of("red"));

            try (Writer writer = Files.newBufferedWriter(file)) {
                new Gson().toJson(List.of(entry), writer);
            }
            try (Reader reader = Files.newBufferedReader(file)) {
                TrackedEntry[] loaded = new Gson().fromJson(reader, TrackedEntry[].class);
                assertEquals(0, loaded[0].getCount());
            }
        }
    }

    private static void write(TeamSlot[] team, Path file) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = Files.newBufferedWriter(file)) {
            gson.toJson(team, writer);
        }
    }

    private static TeamSlot[] writeThenRead(TeamSlot[] team, Path file) throws IOException {
        write(team, file);
        try (Reader reader = Files.newBufferedReader(file)) {
            return new Gson().fromJson(reader, TeamSlot[].class);
        }
    }
}
