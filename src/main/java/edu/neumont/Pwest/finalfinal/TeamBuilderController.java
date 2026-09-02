package edu.neumont.Pwest.finalfinal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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

    static final String SAVE_DIR = String.valueOf(Paths.get(System.getProperty("user.home"), "Documents", "PokeTracker"));
    private static final String TEAM_FILE = SAVE_DIR + "/team.json";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupSlotToggleGroup();
        setupIvSpinners();
        setupEvSpinners();
        setupNatureAndAbilityChoices();

        saveTeamButton.setOnAction(e -> onSaveTeam());
        chatSendButton.setOnAction(e -> onSendChat());
        trackerButton.setOnAction(e -> onSwitchToTracker());
        speciesSearchField.setOnAction(e -> onSpeciesSearch());

        List<String> allNames = null;
        try {
            allNames = apiController.allPokemonNames();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        setupAutocomplete(speciesSearchField, allNames);

        // TODO: load persisted team data
    }



    private void setupAutocomplete(TextField field, List<String> allNames) {
        ContextMenu suggestions = new ContextMenu();
        suggestions.setHideOnEscape(true);

        field.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isBlank()) {
                suggestions.hide();
                return;
            }

            String search = newText.toLowerCase();
            List<MenuItem> matches = allNames.stream()
                    .filter(name -> name.startsWith(search))
                    .limit(10)
                    .map(name -> {
                        MenuItem item = new MenuItem(name);
                        item.setOnAction(e -> {
                            field.setText(name);
                            field.positionCaret(name.length());
                            suggestions.hide();
                        });
                        return item;
                    })
                    .collect(Collectors.toList());

            if (matches.isEmpty()) {
                suggestions.hide();
            } else {
                suggestions.getItems().setAll(matches);
                if (!suggestions.isShowing()) {
                    suggestions.show(field, Side.BOTTOM, 0, 0);
                }
            }
        });

        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) suggestions.hide();
        });
    }

    private void setupSlotToggleGroup() {
        for (ToggleButton btn : List.of(monSlot1Button, monSlot2Button, monSlot3Button,
                monSlot4Button, monSlot5Button, monSlot6Button)) {
            btn.setToggleGroup(slotGroup);
        }
        monSlot1Button.setSelected(true);
        slotGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                return;
            }
            // TODO: save the currently displayed mon's fields into the old slot's
            // model object before loading the new slot's data into the form.

        });
    }


    private void setupIvSpinners() {
        for (Spinner<Integer> spinner : List.of(hpIvSpinner, atkIvSpinner, defIvSpinner,
                spAtkIvSpinner, spDefIvSpinner, spdIvSpinner)) {
            spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_IV, 0));
        }
    }

    private void setupEvSpinners() {
        List<Spinner<Integer>> evSpinners = List.of(hpEvSpinner, atkEvSpinner, defEvSpinner,
                spAtkEvSpinner, spDefEvSpinner, spdEvSpinner);

        for (Spinner<Integer> spinner : evSpinners) {
            spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_STAT_EV, 0));
            spinner.valueProperty().addListener((obs, oldVal, newVal) -> updateEvTotal(evSpinners));
        }
    }

    private void updateEvTotal(List<Spinner<Integer>> evSpinners) {
        int total = evSpinners.stream()
                .mapToInt(Spinner::getValue)
                .sum();

        evTotalLabel.setText(total + " / " + MAX_TOTAL_EV);

        // TODO: decide how you want to enforce the 510 cap -- e.g. flip evTotalLabel
        // to an error styleClass, or roll back the most recent spinner change,
        // when total > MAX_TOTAL_EV.
    }
    @FXML
    private void onSpeciesSearch() {
        String query = speciesSearchField.getText().trim().toLowerCase().replace(" ", "-");
        if (query.isEmpty()) {
            return;
        }
        currentMon = apiController.fetchPokemon(query);
        populateFromPokemon(currentMon);
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
        writeFormToSlot(currentSlotIndex);
        saveTeam();
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PokemonTracker.fxml"));
            Parent teamBuilderRoot = loader.load();
            trackerButton.getScene().setRoot(teamBuilderRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void writeFormToSlot(int index) {
        TeamSlot slot = team[index];
        if (slot == null) {
            slot = new TeamSlot();
            team[index] = slot;
        }

        slot.setSpeciesName(speciesSearchField.getText());
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

        hpBaseLabel.setText("-");
        atkBaseLabel.setText("-");
        defBaseLabel.setText("-");
        spAtkBaseLabel.setText("-");
        spDefBaseLabel.setText("-");
        spdBaseLabel.setText("-");

        currentMon = null;
    }

    private void saveEntries(List<TrackedEntry> entries) {
        new File(SAVE_DIR).mkdirs();
        System.out.println(SAVE_DIR);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = new FileWriter(TEAM_FILE)) {
            gson.toJson(entries, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<TrackedEntry> loadEntries() {
        if (!Files.exists(Path.of(TEAM_FILE))) {
            return new ArrayList<>();
        }
        Gson gson = new Gson();
        try (Reader reader = Files.newBufferedReader(Path.of(TEAM_FILE))) {
            Type listType = new TypeToken<List<TrackedEntry>>(){}.getType();
            List<TrackedEntry> loaded = gson.fromJson(reader, listType);
            return loaded == null ? new ArrayList<>() : loaded;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void saveTeam() {
        new File(SAVE_DIR).mkdirs();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = new FileWriter(TEAM_FILE)) {
            gson.toJson(team, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TeamSlot[] loadTeam() {
        if (!Files.exists(Path.of(TEAM_FILE))) {
            return new TeamSlot[6];
        }
        try (Reader reader = Files.newBufferedReader(Path.of(TEAM_FILE))) {
            TeamSlot[] loaded = new Gson().fromJson(reader, TeamSlot[].class);
            return loaded == null ? new TeamSlot[6] : loaded;
        } catch (IOException | JsonSyntaxException e) {
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