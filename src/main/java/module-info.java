module edu.neumont.pwest.finalfinal {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;
    requires java.desktop;

    // Gson reflects over private fields (TeamSlot), which needs the package opened to it.
    // Without com.google.gson here, saving the team throws JsonIOException at runtime.
    opens edu.neumont.Pwest.finalfinal to javafx.fxml, com.google.gson;
    exports edu.neumont.Pwest.finalfinal;
}