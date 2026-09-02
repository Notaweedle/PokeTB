package edu.neumont.Pwest.finalfinal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import javafx.fxml.Initializable;

/**
 * Controller for TeamBuilder.fxml.
 * Rename the package above to match your project structure, and update
 * fx:controller in the FXML to match if you move this class.
 */

public class TeamBuilderController implements Initializable {

    private static final int MAX_TOTAL_EV = 510;
    private static final int MAX_STAT_EV = 255;
    private static final int MAX_IV = 31;

    // ===== Top bar =====
    @FXML private BorderPane teamBuilderRoot;
    @FXML private Button trackerButton;

    // ===== Slot picker =====
    @FXML private ToggleButton monSlot1Button;
    @FXML private ToggleButton monSlot2Button;
    @FXML private ToggleButton monSlot3Button;
    @FXML private ToggleButton monSlot4Button;
    @FXML private ToggleButton monSlot5Button;
    @FXML private ToggleButton monSlot6Button;
    private final ToggleGroup slotGroup = new ToggleGroup();

    // ===== Species / shiny / moves =====
    @FXML private TextField speciesSearchField;
    @FXML private CheckBox shinyCheckBox;
    @FXML private ComboBox<String> move1ComboBox;
    @FXML private ComboBox<String> move2ComboBox;
    @FXML private ComboBox<String> move3ComboBox;
    @FXML private ComboBox<String> move4ComboBox;


    @FXML private Label hpBaseLabel;
    @FXML private Label atkBaseLabel;
    @FXML private Label defBaseLabel;
    @FXML private Label spAtkBaseLabel;
    @FXML private Label spDefBaseLabel;
    @FXML private Label spdBaseLabel;

    // ===== Stats grid =====
    @FXML private GridPane statsGrid;
    @FXML private Spinner<Integer> hpIvSpinner;
    @FXML private Spinner<Integer> atkIvSpinner;
    @FXML private Spinner<Integer> defIvSpinner;
    @FXML private Spinner<Integer> spAtkIvSpinner;
    @FXML private Spinner<Integer> spDefIvSpinner;
    @FXML private Spinner<Integer> spdIvSpinner;

    @FXML private Spinner<Integer> hpEvSpinner;
    @FXML private Spinner<Integer> atkEvSpinner;
    @FXML private Spinner<Integer> defEvSpinner;
    @FXML private Spinner<Integer> spAtkEvSpinner;
    @FXML private Spinner<Integer> spDefEvSpinner;
    @FXML private Spinner<Integer> spdEvSpinner;

    @FXML private Label evTotalLabel;

    // ===== Nature / ability =====
    @FXML private ComboBox<String> natureComboBox;
    @FXML private ComboBox<String> abilityComboBox;

    @FXML private Button saveTeamButton;

    // ===== Prof.Bug panel (same as Tracker) =====
    @FXML private ListView<String> chatHistoryList;
    @FXML private TextField chatInputField;
    @FXML private Button chatSendButton;

    APIController apiController = new APIController();
    private Pokemon currentMon;

    private TeamSlot[] team = new TeamSlot[6];
    private int currentSlotIndex = 0;
    private List<ToggleButton> slotButtons;

    private final Map<String, Pokemon> monCache = new HashMap<>();

    private SpeciesAutocomplete autocomplete;

    static final String SAVE_DIR = String.valueOf(Paths.get(System.getProperty("user.home"), "Documents", "PokeTracker"));
    private static final String TEAM_FILE = SAVE_DIR + "/team.json";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupSlotToggleGroup();
        setupIvSpinners();
        setupEvSpinners();
        setupNatureAndAbilityChoices();
        setupNameDisplay();

        saveTeamButton.setOnAction(e -> onSaveTeam());
        chatSendButton.setOnAction(e -> onSendChat());
        trackerButton.setOnAction(e -> onSwitchToTracker());
        speciesSearchField.setOnAction(e -> onSpeciesSearch());

        List<String> allNames;
        try {
            allNames = apiController.allPokemonNames();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            allNames = List.of();
        }
        autocomplete = SpeciesAutocomplete.attach(speciesSearchField, allNames);

        team = loadTeam();
        readSlotToForm(currentSlotIndex);
    }

    private void setupSlotToggleGroup() {
        slotButtons = List.of(monSlot1Button, monSlot2Button, monSlot3Button,
                monSlot4Button, monSlot5Button, monSlot6Button);

        for (ToggleButton btn : slotButtons) {
            btn.setToggleGroup(slotGroup);
        }


        monSlot1Button.setSelected(true);

        slotGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                slotGroup.selectToggle(oldToggle);
                return;
            }

            int newIndex = slotButtons.indexOf(newToggle);
            if (newIndex < 0 || newIndex == currentSlotIndex) {
                return;
            }

            writeFormToSlot(currentSlotIndex);
            currentSlotIndex = newIndex;
            readSlotToForm(currentSlotIndex);
        });
    }


    private void setupIvSpinners() {
        for (Spinner<Integer> spinner : List.of(hpIvSpinner, atkIvSpinner, defIvSpinner,
                spAtkIvSpinner, spDefIvSpinner, spdIvSpinner)) {
            spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_IV, 0));
            guardSpinnerText(spinner, InputValidator::isIvInProgress);
        }
    }

    private void setupEvSpinners() {
        List<Spinner<Integer>> evSpinners = List.of(hpEvSpinner, atkEvSpinner, defEvSpinner,
                spAtkEvSpinner, spDefEvSpinner, spdEvSpinner);

        for (Spinner<Integer> spinner : evSpinners) {
            spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_STAT_EV, 0));
            guardSpinnerText(spinner, InputValidator::isEvInProgress);
            spinner.valueProperty().addListener((obs, oldVal, newVal) -> updateEvTotal(evSpinners));
        }
    }


    private void guardSpinnerText(Spinner<Integer> spinner, Predicate<String> allows) {
        TextField editor = spinner.getEditor();

        editor.setTextFormatter(new TextFormatter<>(change ->
                allows.test(change.getControlNewText()) ? change : null));

        editor.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                commitSpinnerText(spinner);
            }
        });
    }

    private void commitSpinnerText(Spinner<Integer> spinner) {
        String text = spinner.getEditor().getText();
        if (text == null || text.isBlank()) {
            spinner.getEditor().setText(String.valueOf(spinner.getValue()));
            return;
        }
        spinner.getValueFactory().setValue(Integer.parseInt(text));
    }

    private void updateEvTotal(List<Spinner<Integer>> evSpinners) {
        int total = evSpinners.stream()
                .mapToInt(Spinner::getValue)
                .sum();

        evTotalLabel.setText(total + " / " + MAX_TOTAL_EV);

        boolean overCap = total > MAX_TOTAL_EV;
        evTotalLabel.getStyleClass().remove("ev-total-over");
        if (overCap) {
            evTotalLabel.getStyleClass().add("ev-total-over");
        }
        evTotalLabel.setStyle(overCap ? "-fx-text-fill: #c0392b; -fx-font-weight: bold;" : "");
    }

    private boolean evTotalIsLegal() {
        int total = List.of(hpEvSpinner, atkEvSpinner, defEvSpinner,
                        spAtkEvSpinner, spDefEvSpinner, spdEvSpinner).stream()
                .mapToInt(Spinner::getValue)
                .sum();

        if (total <= MAX_TOTAL_EV) {
            return true;
        }

        Alert over = new Alert(Alert.AlertType.WARNING);
        over.setHeaderText("Too many EVs");
        over.setContentText("This Pokemon has " + total + " EVs, but the limit is "
                + MAX_TOTAL_EV + ".\nRemove " + (total - MAX_TOTAL_EV) + " before saving.");
        over.showAndWait();
        return false;
    }
    @FXML
    private void onSpeciesSearch() {
        InputValidator.SearchQuery query = InputValidator.parseSearch(speciesSearchField.getText());

        if (!query.isValid()) {

            Alert bad = new Alert(Alert.AlertType.WARNING);
            bad.setHeaderText("That isn't a Pokemon I can look up");
            bad.setContentText(query.getProblem());
            bad.showAndWait();
            return;
        }

        Pokemon found = lookupSpecies(query.getApiQuery());
        if (found == null) {
            return;
        }

        currentMon = found;

        autocomplete.withoutSuggestions(() -> speciesSearchField.setText(currentMon.getName()));

        populateFromPokemon(currentMon);
    }

    private static String normalizeSpecies(String raw) {
        return InputValidator.normalize(raw);
    }

    private Pokemon lookupSpecies(String normalizedName) {
        if (normalizedName == null || normalizedName.isBlank()) {
            return null;
        }
        Pokemon cached = monCache.get(normalizedName);
        if (cached != null) {
            return cached;
        }

        Pokemon fetched = apiController.fetchPokemon(normalizedName);
        if (fetched != null) {
            monCache.put(normalizedName, fetched);
        }
        return fetched;
    }

    private void populateFromPokemon(Pokemon mon) {
        // base stats
        hpBaseLabel.setText(String.valueOf(mon.getHp()));
        atkBaseLabel.setText(String.valueOf(mon.getAttack()));
        defBaseLabel.setText(String.valueOf(mon.getDefense()));
        spAtkBaseLabel.setText(String.valueOf(mon.getSpAttack()));
        spDefBaseLabel.setText(String.valueOf(mon.getSpDefense()));
        spdBaseLabel.setText(String.valueOf(mon.getSpeed()));

        // abilities
        abilityComboBox.getItems().setAll(mon.getAbilities());
        abilityComboBox.setValue(null);

        // moves — same list in all four boxes
        List<String> moves = mon.getLearnableMoves();
        for (ComboBox<String> box : List.of(move1ComboBox, move2ComboBox, move3ComboBox, move4ComboBox)) {
            box.getItems().setAll(moves);
            box.setValue(null);
        }
    }

    private void setupNameDisplay() {
        StringConverter<String> slugDisplay = new StringConverter<String>() {
            @Override
            public String toString(String slug) {
                return InputValidator.prettify(slug);
            }

            @Override
            public String fromString(String typed) {
                return InputValidator.normalize(typed);
            }
        };

        for (ComboBox<String> box : List.of(move1ComboBox, move2ComboBox, move3ComboBox, move4ComboBox)) {
            box.setConverter(slugDisplay);
        }
        abilityComboBox.setConverter(slugDisplay);
    }

    private void setupNatureAndAbilityChoices() {
        natureComboBox.getItems().addAll(
                "Hardy","Lonely","Adamant","Naughty","Brave",
                "Bold",	"Docile","Impish","Lax","Relaxed",
                "Modest","Mild","Bashful","Rash","Quiet",
                "Calm","Gentle","Careful","Quirky","Sassy",
                "Timid","Hasty","Jolly","Naive","Serious");
    }

    @FXML
    private void onSaveTeam() {
        if (!evTotalIsLegal()) {
            return;
        }

        writeFormToSlot(currentSlotIndex);

        if (!saveTeam()) {
            return;
        }

        TeamSlot saved = team[currentSlotIndex];
        String what = saved == null
                ? "Slot " + (currentSlotIndex + 1) + " cleared."
                : InputValidator.prettify(saved.getSpeciesName())
                  + " saved to slot " + (currentSlotIndex + 1) + ".";

        Alert ok = new Alert(Alert.AlertType.INFORMATION);
        ok.setHeaderText("Team saved");
        ok.setContentText(what + "\n" + countFilledSlots() + " of 6 slots filled.");
        ok.showAndWait();
    }

    private int countFilledSlots() {
        int filled = 0;
        for (TeamSlot slot : team) {
            if (slot != null) {
                filled++;
            }
        }
        return filled;
    }
    private void onSendChat() {
        String message = chatInputField.getText();
        if (message == null || message.isBlank()) {
            return;
        }
        chatHistoryList.getItems().add("You: " + message);
        chatInputField.clear();

        // prob not used
    }
    private void onSwitchToTracker() {

        writeFormToSlot(currentSlotIndex);
        saveTeam();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PokemonTracker.fxml"));
            Parent teamBuilderRoot = loader.load();
            trackerButton.getScene().setRoot(teamBuilderRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void writeFormToSlot(int index) {
        String normalized = normalizeSpecies(speciesSearchField.getText());

        // No species means an empty slot -- don't persist a blank shell.
        if (normalized.isEmpty()) {
            team[index] = null;
            return;
        }

        TeamSlot slot = team[index];
        if (slot == null) {
            slot = new TeamSlot();
            team[index] = slot;
        }


        boolean matchesCurrent = currentMon != null && currentMon.getName().equals(normalized);
        slot.setSpeciesName(matchesCurrent ? currentMon.getName() : normalized);
        slot.setShiny(shinyCheckBox.isSelected());
        //Moves
        slot.setMove1(move1ComboBox.getValue());
        slot.setMove2(move2ComboBox.getValue());
        slot.setMove3(move3ComboBox.getValue());
        slot.setMove4(move4ComboBox.getValue());
        //ability / nature
        slot.setAbility(abilityComboBox.getValue());
        slot.setNature(natureComboBox.getValue());
        //IV
        slot.setHpIv(hpIvSpinner.getValue());
        slot.setAtkIv(atkIvSpinner.getValue());
        slot.setDefIv(defIvSpinner.getValue());
        slot.setSpAtkIv(spAtkIvSpinner.getValue());
        slot.setSpDefIv(spDefIvSpinner.getValue());
        slot.setSpdIv(spdIvSpinner.getValue());
        //EV
        slot.setHpEv(hpEvSpinner.getValue());
        slot.setAtkEv(atkEvSpinner.getValue());
        slot.setDefEv(defEvSpinner.getValue());
        slot.setSpAtkEv(spAtkEvSpinner.getValue());
        slot.setSpDefEv(spDefEvSpinner.getValue());
        slot.setSpdEv(spdEvSpinner.getValue());


    }
    private void readSlotToForm(int index) {
        TeamSlot slot = team[index];
        if (slot == null) {
            clearForm();
            return;
        }

        autocomplete.withoutSuggestions(() -> {
            currentMon = lookupSpecies(slot.getSpeciesName());
            if (currentMon != null) {
                populateFromPokemon(currentMon);
            } else {
                clearBaseStatLabels();
            }

            fillFormFrom(slot);
        });
    }

    private void fillFormFrom(TeamSlot slot) {
        speciesSearchField.setText(slot.getSpeciesName());
        shinyCheckBox.setSelected(slot.isShiny());
        //moves
        move1ComboBox.setValue(slot.getMove1());
        move2ComboBox.setValue(slot.getMove2());
        move3ComboBox.setValue(slot.getMove3());
        move4ComboBox.setValue(slot.getMove4());
        //ability and stuff
        abilityComboBox.setValue(slot.getAbility());
        natureComboBox.setValue(slot.getNature());
        //iv's
        hpIvSpinner.getValueFactory().setValue(slot.getHpIv());
        atkIvSpinner.getValueFactory().setValue(slot.getAtkIv());
        defIvSpinner.getValueFactory().setValue(slot.getDefIv());
        spAtkIvSpinner.getValueFactory().setValue(slot.getSpAtkIv());
        spDefIvSpinner.getValueFactory().setValue(slot.getSpDefIv());
        spdIvSpinner.getValueFactory().setValue(slot.getSpdIv());
        //ev
        hpEvSpinner.getValueFactory().setValue(slot.getHpEv());
        atkEvSpinner.getValueFactory().setValue(slot.getAtkEv());
        defEvSpinner.getValueFactory().setValue(slot.getDefEv());
        spAtkEvSpinner.getValueFactory().setValue(slot.getSpAtkEv());
        spDefEvSpinner.getValueFactory().setValue(slot.getSpDefEv());
        spdEvSpinner.getValueFactory().setValue(slot.getSpdEv());

    }
    @FXML
    private void clearForm() {
        speciesSearchField.clear();
        shinyCheckBox.setSelected(false);

        for (ComboBox<String> box : List.of(move1ComboBox, move2ComboBox, move3ComboBox, move4ComboBox)) {
            box.getItems().clear();
            box.setValue(null);
            box.getEditor().clear();
        }

        natureComboBox.setValue(null);
        abilityComboBox.getItems().clear();
        abilityComboBox.setValue(null);

        for (Spinner<Integer> spinner : List.of(hpIvSpinner, atkIvSpinner, defIvSpinner,
                spAtkIvSpinner, spDefIvSpinner, spdIvSpinner)) {
            spinner.getValueFactory().setValue(0);
        }
        for (Spinner<Integer> spinner : List.of(hpEvSpinner, atkEvSpinner, defEvSpinner,
                spAtkEvSpinner, spDefEvSpinner, spdEvSpinner)) {
            spinner.getValueFactory().setValue(0);
        }

        clearBaseStatLabels();

        currentMon = null;
    }

    private void clearBaseStatLabels() {
        hpBaseLabel.setText("-");
        atkBaseLabel.setText("-");
        defBaseLabel.setText("-");
        spAtkBaseLabel.setText("-");
        spDefBaseLabel.setText("-");
        spdBaseLabel.setText("-");
    }


    private boolean saveTeam() {
        new File(SAVE_DIR).mkdirs();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();


        try (Writer writer = new FileWriter(TEAM_FILE)) {
            gson.toJson(team, writer);
            return true;
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setHeaderText("Could not save your team.");
            error.setContentText("Writing to \"" + TEAM_FILE + "\" failed:\n" + e.getMessage());
            error.showAndWait();
            return false;
        }
    }

    private TeamSlot[] loadTeam() {
        if (!Files.exists(Path.of(TEAM_FILE))) {
            return new TeamSlot[6];
        }
        try (Reader reader = Files.newBufferedReader(Path.of(TEAM_FILE))) {
            TeamSlot[] loaded = new Gson().fromJson(reader, TeamSlot[].class);
            if (loaded == null) {
                return new TeamSlot[6];
            }
            TeamSlot[] sized = new TeamSlot[6];
            System.arraycopy(loaded, 0, sized, 0, Math.min(loaded.length, 6));
            return sized;
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return new TeamSlot[6];
        }
    }
    public static class TeamSlot {
        private String speciesName;
        private boolean shiny;
        private String move1, move2, move3, move4;
        private int hpIv, atkIv, defIv, spAtkIv, spDefIv, spdIv;
        private int hpEv, atkEv, defEv, spAtkEv, spDefEv, spdEv;
        private String nature;
        private String ability;

        public String getSpeciesName() {
            return speciesName;
        }
        public void setSpeciesName(String speciesName) {
            this.speciesName = speciesName;
        }
        public boolean isShiny() {
            return shiny;
        }
        public void setShiny(boolean shiny) {
            this.shiny = shiny;
        }
        public String getMove1() {
            return move1;
        }

        public void setMove1(String move1) {
            this.move1 = move1;
        }

        public String getMove2() {
            return move2;
        }

        public void setMove2(String move2) {
            this.move2 = move2;
        }

        public String getMove3() {
            return move3;
        }

        public void setMove3(String move3) {
            this.move3 = move3;
        }

        public String getMove4() {
            return move4;
        }

        public void setMove4(String move4) {
            this.move4 = move4;
        }

        public int getHpIv() {
            return hpIv;
        }

        public void setHpIv(int hpIv) {
            this.hpIv = hpIv;
        }

        public int getAtkIv() {
            return atkIv;
        }

        public void setAtkIv(int atkIv) {
            this.atkIv = atkIv;
        }

        public int getDefIv() {
            return defIv;
        }

        public void setDefIv(int defIv) {
            this.defIv = defIv;
        }

        public int getSpDefIv() {
            return spDefIv;
        }

        public void setSpDefIv(int spDefIv) {
            this.spDefIv = spDefIv;
        }

        public int getSpAtkIv() {
            return spAtkIv;
        }

        public void setSpAtkIv(int spAtkIv) {
            this.spAtkIv = spAtkIv;
        }

        public int getSpdIv() {
            return spdIv;
        }

        public void setSpdIv(int spdIv) {
            this.spdIv = spdIv;
        }

        public int getHpEv() {
            return hpEv;
        }

        public void setHpEv(int hpEv) {
            this.hpEv = hpEv;
        }

        public int getAtkEv() {
            return atkEv;
        }

        public void setAtkEv(int atkEv) {
            this.atkEv = atkEv;
        }

        public int getDefEv() {
            return defEv;
        }

        public void setDefEv(int defEv) {
            this.defEv = defEv;
        }

        public int getSpAtkEv() {
            return spAtkEv;
        }

        public void setSpAtkEv(int spAtkEv) {
            this.spAtkEv = spAtkEv;
        }

        public int getSpDefEv() {
            return spDefEv;
        }

        public void setSpDefEv(int spDefEv) {
            this.spDefEv = spDefEv;
        }

        public int getSpdEv() {
            return spdEv;
        }

        public void setSpdEv(int spdEv) {
            this.spdEv = spdEv;
        }

        public String getNature() {
            return nature;
        }

        public void setNature(String nature) {
            this.nature = nature;
        }

        public String getAbility() {
            return ability;
        }

        public void setAbility(String ability) {
            this.ability = ability;
        }
    }



}