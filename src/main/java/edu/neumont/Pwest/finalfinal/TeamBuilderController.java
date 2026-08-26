package edu.neumont.Pwest.finalfinal;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupSlotToggleGroup();
        setupIvSpinners();
        setupEvSpinners();
        setupNatureAndAbilityChoices();

        saveTeamButton.setOnAction(e -> onSaveTeam());
        chatSendButton.setOnAction(e -> onSendChat());
        trackerButton.setOnAction(e -> onSwitchToTracker());

        // TODO: load persisted team data and select slot 1 by default
    }

    private void setupSlotToggleGroup() {
        for (ToggleButton btn : List.of(monSlot1Button, monSlot2Button, monSlot3Button,
                monSlot4Button, monSlot5Button, monSlot6Button)) {
            btn.setToggleGroup(slotGroup);
        }
        slotGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                return;
            }
            // TODO: save the currently displayed mon's fields into the old slot's
            // model object before loading the new slot's data into the form.
            onSlotSelected((ToggleButton) newToggle);
        });
    }

    private void onSlotSelected(ToggleButton selected) {
        // TODO: look up the Pokémon assigned to this slot (if any) and populate
        // speciesSearchField, shinyCheckBox, move combo boxes, spinners, and
        // nature/ability combo boxes from it. Clear the form if the slot is empty.
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
            // Lambda listener: recompute the running total every time any EV spinner changes.
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

    private void setupNatureAndAbilityChoices() {
        // TODO: replace with the full nature list (25 natures) and pull abilities
        // dynamically from the selected species via your Pokémon API layer.
        natureComboBox.getItems().addAll("Adamant", "Modest", "Jolly", "Timid", "Bold");
    }

    private void onSaveTeam() {
        // TODO: read every field above into a model object for the currently
        // selected slot, then persist the full team via your encode/decode layer.
    }

    private void onSendChat() {
        String message = chatInputField.getText();
        if (message == null || message.isBlank()) {
            return;
        }
        chatHistoryList.getItems().add("You: " + message);
        chatInputField.clear();

        // TODO: send `message` (plus current team-building context, e.g. selected
        // species) to Prof.Bug's backend and append the response with:
        // chatHistoryList.getItems().add("Prof.Bug: " + response);
    }

    private void onSwitchToTracker() {
        // TODO: swap the scene/root back to your Tracker view.
    }
}