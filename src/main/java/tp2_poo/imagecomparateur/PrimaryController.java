package tp2_poo.imagecomparateur;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;

public class PrimaryController {

    // ==================== COMPOSANTS FXML ====================
    
    @FXML private ImageView imageView1;
    @FXML private ImageView imageView2;
    
    @FXML private Label placeholder1;
    @FXML private Label placeholder2;
    
    @FXML private Label infoImage1;
    @FXML private Label infoImage2;
    
    @FXML private Button btnCharger1;
    @FXML private Button btnCharger2;
    @FXML private Button btnEffacer1;
    @FXML private Button btnEffacer2;
    @FXML private Button btnComparer;
    
    @FXML private CheckBox checkRotation;
    @FXML private Label statusLabel;
    
    // ==================== VARIABLES ====================
    
    private File fichierImage1;
    private File fichierImage2;
    
    // ==================== INITIALISATION ====================
    
    @FXML
    public void initialize() {
        System.out.println("✅ Contrôleur initialisé");
        mettreAJourEtatBoutons();
    }
    
    // ==================== CHARGEMENT DES IMAGES ====================
    
    @FXML
    private void chargerImage1() {
        chargerImage(1);
    }
    
    @FXML
    private void chargerImage2() {
        chargerImage(2);
    }
    
    private void chargerImage(int numeroImage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        
        // Filtres de fichiers
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Toutes les Images", 
                "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif"),
            new FileChooser.ExtensionFilter("PNG", "*.png"),
            new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("BMP", "*.bmp"),
            new FileChooser.ExtensionFilter("GIF", "*.gif")
        );
        
        // Ouvrir le dialogue
        Stage stage = (Stage) btnCharger1.getScene().getWindow();
        File fichier = fileChooser.showOpenDialog(stage);
        
        if (fichier != null) {
            try {
                Image image = new Image(fichier.toURI().toString());
                
                if (numeroImage == 1) {
                    fichierImage1 = fichier;
                    imageView1.setImage(image);
                    placeholder1.setVisible(false);
                    btnEffacer1.setDisable(false);
                    infoImage1.setText(String.format("Dimensions : %.0f x %.0f px", 
                        image.getWidth(), image.getHeight()));
                    updateStatus("Image 1 chargée : " + fichier.getName());
                } else {
                    fichierImage2 = fichier;
                    imageView2.setImage(image);
                    placeholder2.setVisible(false);
                    btnEffacer2.setDisable(false);
                    infoImage2.setText(String.format("Dimensions : %.0f x %.0f px", 
                        image.getWidth(), image.getHeight()));
                    updateStatus("Image 2 chargée : " + fichier.getName());
                }
                
                mettreAJourEtatBoutons();
                
            } catch (Exception e) {
                afficherErreur("Erreur de chargement", 
                    "Impossible de charger l'image : " + e.getMessage());
            }
        }
    }
    
    // ==================== EFFACEMENT DES IMAGES ====================
    
    @FXML
    private void effacerImage1() {
        fichierImage1 = null;
        imageView1.setImage(null);
        placeholder1.setVisible(true);
        btnEffacer1.setDisable(true);
        infoImage1.setText("Dimensions : -");
        updateStatus("Image 1 effacée");
        mettreAJourEtatBoutons();
    }
    
    @FXML
    private void effacerImage2() {
        fichierImage2 = null;
        imageView2.setImage(null);
        placeholder2.setVisible(true);
        btnEffacer2.setDisable(true);
        infoImage2.setText("Dimensions : -");
        updateStatus("Image 2 effacée");
        mettreAJourEtatBoutons();
    }
    
    // ==================== COMPARAISON ====================
    
    @FXML
    private void comparerImages() {
        System.out.println("DeBUT comparerImages()");
        
        if (fichierImage1 == null || fichierImage2 == null) {
            System.out.println("Images manquantes !");
            afficherErreur("Erreur", "Veuillez charger deux images avant de comparer.");
            return;
        }
        
        System.out.println("✅ Fichier 1: " + fichierImage1.getAbsolutePath());
        System.out.println("✅ Fichier 2: " + fichierImage2.getAbsolutePath());
        
        // Désactiver les boutons pendant la comparaison
        btnComparer.setDisable(true);
        btnCharger1.setDisable(true);
        btnCharger2.setDisable(true);
        updateStatus("Comparaison en cours...");
        
        System.out.println(" Lancement de la tâche...");
        
        // Créer une tâche en arrière-plan
        Task<ComparateurImagesCore.ResultatComparaison> task = new Task<>() {
            @Override
            protected ComparateurImagesCore.ResultatComparaison call() throws Exception {
                System.out.println("Dans le thread de traitement...");
                boolean testerRotations = checkRotation.isSelected();
                System.out.println("Tester rotations: " + testerRotations);
                
                ComparateurImagesCore.ResultatComparaison resultat = ComparateurImagesCore.comparerImages(
                    fichierImage1.getAbsolutePath(),
                    fichierImage2.getAbsolutePath(),
                    testerRotations
                );
                
                System.out.println("Résultat obtenu: " + resultat);
                return resultat;
            }
        };
        
        // Gérer la réussite
        task.setOnSucceeded(event -> {
            System.out.println("Task succeeded!");
            ComparateurImagesCore.ResultatComparaison resultat = task.getValue();
            System.out.println("Affichage des résultats...");
            afficherResultats(resultat);
            btnComparer.setDisable(false);
            btnCharger1.setDisable(false);
            btnCharger2.setDisable(false);
            updateStatus("Comparaison terminée");
        });
        
        // Gérer l'échec
        task.setOnFailed(event -> {
            System.out.println("Task failed!");
            Throwable exception = task.getException();
            exception.printStackTrace();
            afficherErreur("Erreur de comparaison", 
                "Une erreur s'est produite : " + exception.getMessage());
            btnComparer.setDisable(false);
            btnCharger1.setDisable(false);
            btnCharger2.setDisable(false);
            updateStatus("Erreur lors de la comparaison");
        });
        
        // Lancer la tâche dans un thread séparé
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        
        System.out.println("Thread lance !");
    }
    
    // ==================== AFFICHAGE DES RÉSULTATS (POPUP) ====================
    
    private void afficherResultats(ComparateurImagesCore.ResultatComparaison resultat) {
        System.out.println("?Ouverture de la fenetre de resultats...");
        
        try {
            // Charger le FXML de la popup
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tp2_poo/imagecomparateur/resultats-view.fxml"));
            Parent root = loader.load();
            
            // Récupérer le contrôleur et initialiser les résultats
            ResultatsController controller = loader.getController();
            controller.initialiserResultats(resultat, checkRotation.isSelected());
            
            // Créer une nouvelle fenêtre (Stage)
            Stage popupStage = new Stage();
            popupStage.setTitle("📊 Résultats de la Comparaison");
            popupStage.setScene(new Scene(root));
            popupStage.setResizable(false);
            
            // Définir comme fenêtre modale (bloque la fenêtre principale)
            popupStage.initModality(Modality.APPLICATION_MODAL);
            
            // Centrer par rapport à la fenêtre principale
            Stage mainStage = (Stage) btnComparer.getScene().getWindow();
            popupStage.initOwner(mainStage);
            
            // Afficher la fenêtre
            popupStage.showAndWait();
            
            System.out.println("Fenetre de resultats fermee");
            
        } catch (Exception e) {
            System.err.println(" Erreur lors de l'ouverture de la fenetre de resultats :");
            e.printStackTrace();
            
            // Fallback : afficher une alerte simple
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Resultats");
            alert.setHeaderText("Score Final : " + String.format("%.2f%%", resultat.scoreFinal));
            alert.setContentText(String.format(
                "SSIM: %.2f%%\nBords: %.2f%%\nHistogramme: %.2f%%\nRotation: %d°",
                resultat.ssim, resultat.bords, resultat.histogramme, resultat.rotation
            ));
            alert.showAndWait();
        }
    }
    
    // ==================== UTILITAIRES ====================
    
    private void mettreAJourEtatBoutons() {
        boolean deuxImagesChargees = (fichierImage1 != null && fichierImage2 != null);
        btnComparer.setDisable(!deuxImagesChargees);
    }
    
    private void updateStatus(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }
    
    private void afficherErreur(String titre, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(titre);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}