package edu.neumont.Pwest.finalfinal;

import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The species suggestion popup used by both the Tracker and the Team Builder search boxes.
 * Attach it to a TextField and it shows matching Pokemon names as you type.
 */
public final class SpeciesAutocomplete {

    private static final int MAX_SUGGESTIONS = 10;

    private final ContextMenu suggestions = new ContextMenu();

    /** Stops the popup appearing when the field is filled by code instead of by typing. */
    private boolean suppressed = false;

    private SpeciesAutocomplete() {
    }

    public static SpeciesAutocomplete attach(TextField field, List<String> allNames) {
        SpeciesAutocomplete autocomplete = new SpeciesAutocomplete();
        autocomplete.wire(field, allNames == null ? List.of() : allNames);
        return autocomplete;
    }

    private void wire(TextField field, List<String> allNames) {
        suggestions.setHideOnEscape(true);

        field.textProperty().addListener((obs, oldText, newText) -> {
            if (suppressed || newText == null || newText.isBlank()) {
                suggestions.hide();
                return;
            }

            String search = newText.toLowerCase();
            List<MenuItem> matches = allNames.stream()
                    .filter(name -> name.startsWith(search))
                    .limit(MAX_SUGGESTIONS)
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
            if (!isFocused) {
                suggestions.hide();
            }
        });
    }

    /** Runs an action that writes to the field without the popup opening in response. */
    public void withoutSuggestions(Runnable action) {
        suppressed = true;
        try {
            action.run();
        } finally {
            suppressed = false;
        }
    }
}
