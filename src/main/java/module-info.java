module tp2_poo.imagecomparateur {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop; 

    opens tp2_poo.imagecomparateur to javafx.fxml;
    exports tp2_poo.imagecomparateur;
}
