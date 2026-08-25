package edu.neumont.rmajka.finalfinal;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

/**
 * Controller for Tracker.fxml.
 * Rename the package above to match your project structure, and update
 * fx:controller in the FXML to match if you move this class.
 */

public class TrackerController implements Initializable {

    // ===== Top bar =====
    @FXML private BorderPane trackerRoot;
    @FXML private Button teamBuilderButton;

    // ===== Center: search + counter list =====
    @FXML private TextField speciesSearchField;
    @FXML private Button searchButton;
    @FXML private Button addCounterButton;
    @FXML private ScrollPane counterScrollPane;
    @FXML private VBox counterListContainer;

    // ===== Right: Prof.Bug panel =====
    @FXML private ListView<String> chatHistoryList;
    @FXML private TextField chatInputField;
    @FXML private Button chatSendButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO: load persisted counter data and populate counterListContainer
        // TODO: load persisted chat history (if you keep any) into chatHistoryList

        searchButton.setOnAction(e -> onSearch());
        addCounterButton.setOnAction(e -> onAddCounter());
        chatSendButton.setOnAction(e -> onSendChat());
        teamBuilderButton.setOnAction(e -> onSwitchToTeamBuilder());
    }

    private void onSearch() {
        String query = speciesSearchField.getText();
        // TODO: call your Pokémon API layer with `query`, then either
        // scroll to / highlight an existing counter row, or feed the
        // result into onAddCounter().
    }

    private void onAddCounter() {
        // TODO: build a new counter row (see the comment in Tracker.fxml
        // for the suggested HBox structure) and append it to
        // counterListContainer.getChildren(). Persist the updated list.
    }

    private void onSendChat() {
        String message = chatInputField.getText();
        if (message == null || message.isBlank()) {
            return;
        }
        chatHistoryList.getItems().add("You: " + message);
        chatInputField.clear();

        // TODO: send `message` to Prof.Bug's backend (API call or rule-based
        // system) and append the response with:
        // chatHistoryList.getItems().add("Prof.Bug: " + response);
    }

    private void onSwitchToTeamBuilder() {
        // TODO: swap the scene/root to your Team Builder view.
    }
}