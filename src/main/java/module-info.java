module edu.neumont.pwest.finalfinal {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;
    requires java.desktop;

    opens edu.neumont.Pwest.finalfinal to javafx.fxml;
    exports edu.neumont.Pwest.finalfinal;
}