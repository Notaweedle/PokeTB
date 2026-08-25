module edu.neumont.rmajka.finalfinal {
    requires javafx.controls;
    requires javafx.fxml;


    opens edu.neumont.rmajka.finalfinal to javafx.fxml;
    exports edu.neumont.rmajka.finalfinal;
}