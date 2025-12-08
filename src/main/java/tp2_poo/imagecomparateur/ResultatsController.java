package tp2_poo.imagecomparateur;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class ResultatsController {

    @FXML private Label labelScoreFinal;
    @FXML private Label labelInterpretation;
    @FXML private Label labelRotation;
    @FXML private Label labelSousTitre;
    
    @FXML private ProgressBar progressSSIM;
    @FXML private ProgressBar progressBords;
    @FXML private ProgressBar progressHistogramme;
    
    @FXML private Label labelSSIM;
    @FXML private Label labelBords;
    @FXML private Label labelHistogramme;
    
    private ComparateurImagesCore.ResultatComparaison resultat;
    private boolean avecRotation;
    
    /**
     * Initialise la fenêtre avec les résultats de comparaison
     */
    public void initialiserResultats(ComparateurImagesCore.ResultatComparaison resultat, boolean avecRotation) {
        this.resultat = resultat;
        this.avecRotation = avecRotation;
        
        System.out.println("Initialisation de la fenêtre de résultats");
        
        // Mettre à jour le score final
        labelScoreFinal.setText(String.format("%.2f%%", resultat.scoreFinal));
        
        // Mettre à jour les barres de progression
        progressSSIM.setProgress(resultat.ssim / 100.0);
        progressBords.setProgress(resultat.bords / 100.0);
        progressHistogramme.setProgress(resultat.histogramme / 100.0);
        
        // Mettre à jour les labels de pourcentage
        labelSSIM.setText(String.format("%.2f%%", resultat.ssim));
        labelBords.setText(String.format("%.2f%%", resultat.bords));
        labelHistogramme.setText(String.format("%.2f%%", resultat.histogramme));
        
        // Appliquer les couleurs
        String couleurFinal = getCouleurScore(resultat.scoreFinal);
        labelScoreFinal.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: " + couleurFinal + ";");
        
        progressSSIM.setStyle("-fx-accent: " + getCouleurScore(resultat.ssim) + ";");
        progressBords.setStyle("-fx-accent: " + getCouleurScore(resultat.bords) + ";");
        progressHistogramme.setStyle("-fx-accent: " + getCouleurScore(resultat.histogramme) + ";");
        
        // Interprétation
        String interpretation;
        String emoji;
        if (resultat.scoreFinal >= 90) {
            interpretation = "✨ Images très similaires";
            emoji = "😊";
        } else if (resultat.scoreFinal >= 70) {
            interpretation = "👍 Images similaires";
            emoji = "🙂";
        } else if (resultat.scoreFinal >= 50) {
            interpretation = "⚠️ Images moyennement similaires";
            emoji = "😐";
        } else {
            interpretation = "❌ Images différentes";
            emoji = "😕";
        }
        
        labelInterpretation.setText(emoji + " " + interpretation);
        labelInterpretation.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + couleurFinal + ";");
        
        // Info rotation
        if (avecRotation) {
            if (resultat.rotation == 0) {
                labelRotation.setText("🔄 Meilleure correspondance sans rotation");
            } else {
                labelRotation.setText(String.format("🔄 Meilleure correspondance avec rotation de %d°", resultat.rotation));
            }
            labelRotation.setVisible(true);
        } else {
            labelRotation.setVisible(false);
        }
        
        System.out.println("✅ Fenêtre de résultats initialisée");
    }
    
    /**
     * Copie le rapport dans le presse-papiers
     */
    @FXML
    private void copierRapport() {
        StringBuilder rapport = new StringBuilder();
        rapport.append("═══════════════════════════════════════\n");
        rapport.append("    RAPPORT DE COMPARAISON D'IMAGES\n");
        rapport.append("═══════════════════════════════════════\n\n");
        
        if (avecRotation && resultat.rotation != 0) {
            rapport.append(String.format("🔄 Rotation appliquée : %d°\n\n", resultat.rotation));
        }
        
        rapport.append(String.format("SSIM (Structure)      : %.2f%%\n", resultat.ssim));
        rapport.append(String.format("Bords (Contours)      : %.2f%%\n", resultat.bords));
        rapport.append(String.format("Histogramme           : %.2f%%\n\n", resultat.histogramme));
        
        rapport.append(String.format(">>> SCORE FINAL       : %.2f%%\n", resultat.scoreFinal));
        rapport.append("    (70% SSIM + 10% Bords + 20% Histogramme)\n\n");
        
        String interpretation;
        if (resultat.scoreFinal >= 90) {
            interpretation = "Images très similaires";
        } else if (resultat.scoreFinal >= 70) {
            interpretation = "Images similaires";
        } else if (resultat.scoreFinal >= 50) {
            interpretation = "Images moyennement similaires";
        } else {
            interpretation = "Images différentes";
        }
        
        rapport.append("Interprétation : ").append(interpretation).append("\n");
        rapport.append("═══════════════════════════════════════\n");
        
        // Copier dans le presse-papiers
        ClipboardContent content = new ClipboardContent();
        content.putString(rapport.toString());
        Clipboard.getSystemClipboard().setContent(content);
        
        // Afficher une confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rapport copié");
        alert.setHeaderText(null);
        alert.setContentText("Le rapport a été copié dans le presse-papiers ! 📋");
        alert.showAndWait();
        
        System.out.println("📋 Rapport copié dans le presse-papiers");
    }
    
    /**
     * Ferme la fenêtre de résultats
     */
    @FXML
    private void fermerFenetre() {
        Stage stage = (Stage) labelScoreFinal.getScene().getWindow();
        stage.close();
        System.out.println("❌ Fenêtre de résultats fermée");
    }
    
    /**
     * Retourne une couleur en fonction du score
     */
    private String getCouleurScore(double score) {
        if (score >= 90) return "#27ae60"; // Vert
        if (score >= 70) return "#f39c12"; // Orange
        if (score >= 50) return "#e67e22"; // Orange foncé
        return "#e74c3c"; // Rouge
    }
}