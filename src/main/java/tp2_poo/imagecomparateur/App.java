package tp2_poo.imagecomparateur;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("primary.fxml"));
        Parent root = fxmlLoader.load();
        
        Scene scene = new Scene(root);
        
        stage.setTitle("Comparateur d'Images - Analyse Avancée");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.show();
        
        System.out.println("✅ Application lancée avec succès !");
    }

    public static void main(String[] args) {
        launch();
    }
}