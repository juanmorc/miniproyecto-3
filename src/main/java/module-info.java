module org.example.miniproyecto3 {
    requires javafx.controls;
    requires javafx.fxml;


    opens application to javafx.fxml;
    exports application;
    exports;
    opens to
}