package tp2_poo.imagecomparateur;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.*;



public class ComparateurImagesCore {
    
    // ==================== CLASSE RÉSULTAT ====================
    
    public static class ResultatComparaison {
        public double ssim;
        public double bords;
        public double histogramme;
        public double scoreFinal;
        public int rotation;
        
        public ResultatComparaison(double ssim, double bords, double histogramme, 
                                   double scoreFinal, int rotation) {
            this.ssim = ssim;
            this.bords = bords;
            this.histogramme = histogramme;
            this.scoreFinal = scoreFinal;
            this.rotation = rotation;
        }
        
        @Override
        public String toString() {
            return String.format("SSIM: %.2f%%, Bords: %.2f%%, Histogramme: %.2f%%, " +
                               "Score Final: %.2f%%, Rotation: %d°",
                               ssim, bords, histogramme, scoreFinal, rotation);
        }
    }
    
    // ==================== LECTURE D'IMAGES ====================
    
    /**
     * Lit une image et la convertit en matrice de niveaux de gris
     * Supporte: BMP, JPG, JPEG, PNG, GIF
     */
    public static int[][] lireImageGris(String nomFichier) throws IOException {
        File fichier = new File(nomFichier);
        
        if (!fichier.exists()) {
            throw new IOException("Fichier introuvable : " + nomFichier);
        }

        String extension = nomFichier.substring(nomFichier.lastIndexOf('.') + 1).toLowerCase();
        
        if (extension.equals("bmp")) {
            return lireBMPGris(nomFichier);
        } else if (extension.equals("jpg") || extension.equals("jpeg") || 
                   extension.equals("png") || extension.equals("gif")) {
            return lireImageAvecImageIO(nomFichier);
        } else {
            throw new IOException("Format non supporté : " + extension + 
                                  ". Formats acceptés : BMP, PNG, JPG, JPEG, GIF");
        }
    }

    /**
     * Lecture avec ImageIO (PNG, JPG, JPEG, GIF)
     */
    private static int[][] lireImageAvecImageIO(String nomFichier) throws IOException {
        java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(new File(nomFichier));
        
        
        
        if (img == null) {
            throw new IOException("Impossible de lire l'image : " + nomFichier);
        }

        int largeur = img.getWidth();
        int hauteur = img.getHeight();
        int[][] gris = new int[hauteur][largeur];

        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                int rgb = img.getRGB(x, y);
                
                // Extraction des composantes RGB
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                // Conversion en niveaux de gris (pondération perceptuelle)
                int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);
                gris[y][x] = gray;
            }
        }
        
        return gris;
    }

    /**
     * Lecture BMP native (optimisée pour performance)
     */
    private static int[][] lireBMPGris(String nomFichier) throws IOException {
        try (FileInputStream fis = new FileInputStream(nomFichier)) {
            byte[] header = new byte[54];
            fis.read(header, 0, 54);

            if (header[0] != 'B' || header[1] != 'M')
                throw new IOException("Fichier BMP invalide : " + nomFichier);

            int largeur = ((header[21] & 0xff) << 24) | ((header[20] & 0xff) << 16)
                        | ((header[19] & 0xff) << 8) | (header[18] & 0xff);
            int hauteur = ((header[25] & 0xff) << 24) | ((header[24] & 0xff) << 16)
                        | ((header[23] & 0xff) << 8) | (header[22] & 0xff);
            int bitsParPixel = ((header[29] & 0xff) << 8) | (header[28] & 0xff);
            
            if (bitsParPixel != 24) 
                throw new IOException("BMP non 24 bits : " + nomFichier);

            int tailleLigne = ((largeur * 3 + 3) / 4) * 4;
            int[][] gris = new int[hauteur][largeur];
            byte[] ligne = new byte[tailleLigne];

            for (int y = 0; y < hauteur; y++) {
                fis.read(ligne);
                for (int x = 0; x < largeur; x++) {
                    int i = x * 3;
                    int b = ligne[i] & 0xFF;
                    int g = ligne[i + 1] & 0xFF;
                    int r = ligne[i + 2] & 0xFF;
                    int gray = (int)(0.299*r + 0.587*g + 0.114*b);
                    gris[hauteur - 1 - y][x] = gray;
                }
            }
            return gris;
        }
    }
    
    // ==================== TRANSFORMATION D'IMAGES ====================
    
    /**
     * Redimensionne une image avec interpolation bilinéaire
     */
    public static int[][] redimensionner(int[][] img, int nouvelleLargeur, int nouvelleHauteur) {
        int hauteurOriginale = img.length;
        int largeurOriginale = img[0].length;
        
        int[][] imgRedim = new int[nouvelleHauteur][nouvelleLargeur];
        
        double ratioX = (double) largeurOriginale / nouvelleLargeur;
        double ratioY = (double) hauteurOriginale / nouvelleHauteur;
        
        for (int y = 0; y < nouvelleHauteur; y++) {
            for (int x = 0; x < nouvelleLargeur; x++) {
                double origX = x * ratioX;
                double origY = y * ratioY;
                
                int x1 = (int) origX;
                int y1 = (int) origY;
                int x2 = Math.min(x1 + 1, largeurOriginale - 1);
                int y2 = Math.min(y1 + 1, hauteurOriginale - 1);
                
                double dx = origX - x1;
                double dy = origY - y1;
                
                // Interpolation bilinéaire
                double val = img[y1][x1] * (1 - dx) * (1 - dy) +
                            img[y1][x2] * dx * (1 - dy) +
                            img[y2][x1] * (1 - dx) * dy +
                            img[y2][x2] * dx * dy;
                
                imgRedim[y][x] = (int) Math.round(val);
            }
        }
        
        return imgRedim;
    }
    
    /**
     * Pivote une matrice de 90° dans le sens horaire
     */
    public static int[][] pivoter90Matrice(int[][] img) {
        int hauteur = img.length;
        int largeur = img[0].length;
        int[][] rotated = new int[largeur][hauteur];
        
        for (int y = 0; y < hauteur; y++) {
            for (int x = 0; x < largeur; x++) {
                rotated[x][hauteur - 1 - y] = img[y][x];
            }
        }
        
        return rotated;
    }
    
    /**
     * Pivote une matrice de 180°
     */
    public static int[][] pivoter180Matrice(int[][] img) {
        return pivoter90Matrice(pivoter90Matrice(img));
    }
    
    /**
     * Pivote une matrice de 270° dans le sens horaire
     */
    public static int[][] pivoter270Matrice(int[][] img) {
        return pivoter90Matrice(pivoter90Matrice(pivoter90Matrice(img)));
    }
    
    /**
     * Normalise une image (valeurs entre 0 et 1)
     */
    public static double[][] normaliser(int[][] img) {
        int h = img.length, w = img[0].length;
        double[][] norm = new double[h][w];
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                norm[y][x] = img[y][x] / 255.0;
        return norm;
    }
    
    // ==================== HISTOGRAMMES ====================
    
    /**
     * Crée un histogramme à partir d'une matrice de niveaux de gris
     */
    public static int[] creerHistogramme(int[][] img) {
        int[] histogramme = new int[256];
        
        for (int y = 0; y < img.length; y++) {
            for (int x = 0; x < img[0].length; x++) {
                int niveau = Math.min(255, Math.max(0, img[y][x]));
                histogramme[niveau]++;
            }
        }
        
        return histogramme;
    }
    
    /**
     * Compare deux histogrammes (méthode de Bhattacharyya)
     * @return Score de similarité entre 0 et 100
     */
    public static double comparerHistogrammes(int[] hist1, int[] hist2) {
        // Normaliser les histogrammes
        int total1 = 0, total2 = 0;
        for (int i = 0; i < 256; i++) {
            total1 += hist1[i];
            total2 += hist2[i];
        }
        
        if (total1 == 0 || total2 == 0) {
            return 0.0;
        }
        
        double[] norm1 = new double[256];
        double[] norm2 = new double[256];
        
        for (int i = 0; i < 256; i++) {
            norm1[i] = (double) hist1[i] / total1;
            norm2[i] = (double) hist2[i] / total2;
        }
        
        // Calcul de la corrélation (méthode de Bhattacharyya)
        double somme = 0.0;
        for (int i = 0; i < 256; i++) {
            somme += Math.sqrt(norm1[i] * norm2[i]);
        }
        
        // Conversion en score de similarité (0-100%)
        double score = somme * 100.0;
        return Math.max(0, Math.min(100, score));
    }
    
    // ==================== ALGORITHMES DE COMPARAISON ====================
    
    /**
     * Calcule le SSIM (Structural Similarity Index) simplifié
     * @return Valeur entre 0 et 1
     */
    public static double ssim(double[][] img1, double[][] img2) {
        int h = Math.min(img1.length, img2.length);
        int w = Math.min(img1[0].length, img2[0].length);

        double moy1 = 0, moy2 = 0;
        int total = h * w;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                moy1 += img1[y][x];
                moy2 += img2[y][x];
            }
        moy1 /= total;
        moy2 /= total;

        double var1 = 0, var2 = 0, cov = 0;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                double a = img1[y][x] - moy1;
                double b = img2[y][x] - moy2;
                var1 += a * a;
                var2 += b * b;
                cov += a * b;
            }

        var1 /= total - 1;
        var2 /= total - 1;
        cov /= total - 1;

        double C1 = 0.01 * 0.01, C2 = 0.03 * 0.03;

        double ssim = ((2*moy1*moy2 + C1) * (2*cov + C2)) /
                      ((moy1*moy1 + moy2*moy2 + C1) * (var1 + var2 + C2));
        return Math.max(0, Math.min(1, ssim));
    }

    /**
     * Applique le filtre de Sobel pour détecter les bords
     */
    public static double[][] sobel(double[][] img) {
        int h = img.length, w = img[0].length;
        double[][] grad = new double[h][w];
        int[][] gx = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] gy = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};
        
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                double sx = 0, sy = 0;
                for (int i = -1; i <= 1; i++)
                    for (int j = -1; j <= 1; j++) {
                        sx += gx[i + 1][j + 1] * img[y + i][x + j];
                        sy += gy[i + 1][j + 1] * img[y + i][x + j];
                    }
                grad[y][x] = Math.sqrt(sx*sx + sy*sy);
            }
        }
        return grad;
    }

    /**
     * Compare deux images de bords (détection Sobel)
     * @return Score de similarité entre 0 et 100
     */
    public static double comparerBords(double[][] edges1, double[][] edges2) {
        int h = Math.min(edges1.length, edges2.length);
        int w = Math.min(edges1[0].length, edges2[0].length);
        
        // Normalisation des gradients
        double max1 = 0, max2 = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                max1 = Math.max(max1, edges1[y][x]);
                max2 = Math.max(max2, edges2[y][x]);
            }
        }
        
        // Éviter la division par zéro
        if (max1 < 0.001) max1 = 1.0;
        if (max2 < 0.001) max2 = 1.0;
        
        // Normaliser les bords
        double[][] norm1 = new double[h][w];
        double[][] norm2 = new double[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                norm1[y][x] = edges1[y][x] / max1;
                norm2[y][x] = edges2[y][x] / max2;
            }
        }
        
        // Calcul de la différence (RMSE)
        double diff = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double d = Math.abs(norm1[y][x] - norm2[y][x]);
                diff += d * d;
            }
        }
        diff = Math.sqrt(diff / (h * w));
        
        // Conversion en score de similarité
        double score = 100 * Math.exp(-3.0 * diff);
        return Math.max(0, Math.min(100, score));
    }
    
    // ==================== COMPARAISON PRINCIPALE ====================
    
    /**
     * Compare deux images avec une rotation spécifique
     * @param gray1 Première image en niveaux de gris
     * @param gray2Original Deuxième image en niveaux de gris
     * @param rotation Angle de rotation (0, 90, 180, 270)
     * @return Résultat de comparaison
     */
    public static ResultatComparaison comparerAvecRotation(int[][] gray1, int[][] gray2Original, 
                                                           int rotation) {
        int[][] gray2 = gray2Original;
        
        // Appliquer la rotation si nécessaire
        if (rotation == 90) {
            gray2 = pivoter90Matrice(gray2Original);
        } else if (rotation == 180) {
            gray2 = pivoter180Matrice(gray2Original);
        } else if (rotation == 270) {
            gray2 = pivoter270Matrice(gray2Original);
        }
        
        // Créer les histogrammes AVANT redimensionnement
        int[] hist1 = creerHistogramme(gray1);
        int[] hist2 = creerHistogramme(gray2);
        double histogrammeScore = comparerHistogrammes(hist1, hist2);
        
        // Redimensionner si nécessaire
        int h1 = gray1.length, w1 = gray1[0].length;
        int h2 = gray2.length, w2 = gray2[0].length;
        
        if (h1 != h2 || w1 != w2) {
            int minH = Math.min(h1, h2);
            int minW = Math.min(w1, w2);
            
            if (h1 != minH || w1 != minW) {
                gray1 = redimensionner(gray1, minW, minH);
            }
            if (h2 != minH || w2 != minW) {
                gray2 = redimensionner(gray2, minW, minH);
            }
        }
        
        // Normalisation
        double[][] norm1 = normaliser(gray1);
        double[][] norm2 = normaliser(gray2);
        
        // Calcul SSIM
        double ssimValue = ssim(norm1, norm2) * 100.0;
        
        // Calcul bords
        double[][] edges1 = sobel(norm1);
        double[][] edges2 = sobel(norm2);
        double edgesScore = comparerBords(edges1, edges2);
        
        // Score final (70% SSIM + 10% Bords + 20% Histogramme)
        double scoreFinal = (ssimValue * 0.6) + (edgesScore * 0.1) + (histogrammeScore * 0.3);
        
        return new ResultatComparaison(ssimValue, edgesScore, histogrammeScore, 
                                       scoreFinal, rotation);
    }
    
    /**
     * Compare deux images avec toutes les rotations possibles
     * @param gray1 Première image en niveaux de gris
     * @param gray2 Deuxième image en niveaux de gris
     * @return Meilleur résultat de comparaison
     */
    public static ResultatComparaison comparerAvecToutesRotations(int[][] gray1, int[][] gray2) {
        ResultatComparaison meilleur = null;
        int[] rotations = {0, 90, 180, 270};
        
        for (int rotation : rotations) {
            // Copie de gray1 pour chaque rotation
            int[][] gray1Copy = new int[gray1.length][gray1[0].length];
            for (int y = 0; y < gray1.length; y++) {
                System.arraycopy(gray1[y], 0, gray1Copy[y], 0, gray1[0].length);
            }
            
            ResultatComparaison resultat = comparerAvecRotation(gray1Copy, gray2, rotation);
            
            if (meilleur == null || resultat.scoreFinal > meilleur.scoreFinal) {
                meilleur = resultat;
            }
        }
        
        return meilleur;
    }
    
    /**
     * Compare deux images (méthode simplifiée)
     * @param cheminImage1 Chemin de la première image
     * @param cheminImage2 Chemin de la deuxième image
     * @param testerRotations Si true, teste toutes les rotations
     * @return Résultat de comparaison
     */
    public static ResultatComparaison comparerImages(String cheminImage1, String cheminImage2, 
                                                     boolean testerRotations) throws IOException {
        int[][] gray1 = lireImageGris(cheminImage1);
        int[][] gray2 = lireImageGris(cheminImage2);
        
        if (testerRotations) {
            return comparerAvecToutesRotations(gray1, gray2);
        } else {
            return comparerAvecRotation(gray1, gray2, 0);
        }
    }
    
    // ==================== UTILITAIRES ====================
    
    /**
     * Formate le résultat de comparaison en texte lisible
     */
    public static String formaterResultat(ResultatComparaison resultat, 
                                         int largeur1, int hauteur1,
                                         int largeur2, int hauteur2,
                                         boolean avecRotation) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("      RÉSULTATS DE COMPARAISON\n");
        sb.append("========================================\n\n");
        
        // Info rotation si applicable
        if (avecRotation) {
            if (resultat.rotation == 0) {
                sb.append("🔄 Meilleure correspondance : Aucune rotation\n");
            } else {
                sb.append(String.format("🔄 Meilleure correspondance : Rotation %d°\n", 
                                       resultat.rotation));
            }
            sb.append("\n");
        }
        
        // Info redimensionnement si applicable
        if (hauteur1 != hauteur2 || largeur1 != largeur2) {
            sb.append(String.format(" Images redimensionnées : %dx%d → %dx%d\n\n", 
                largeur1, hauteur1, 
                Math.min(largeur1, largeur2), Math.min(hauteur1, hauteur2)));
        }
        
        sb.append(String.format("SSIM simplifié         : %.2f %%\n", resultat.ssim));
        sb.append(String.format("Similarité des bords   : %.2f %%\n", resultat.bords));
        sb.append(String.format("Similarité histogramme : %.2f %%\n", resultat.histogramme));
        sb.append(String.format("\n>>> SCORE FINAL        : %.2f %%\n", resultat.scoreFinal));
        sb.append("    (60% SSIM + 20% Bords + 20% Histogramme)\n");
        sb.append("\n========================================\n");
        
        if (resultat.scoreFinal >= 90) {
            sb.append("Interprétation : Images très similaires\n");
        } else if (resultat.scoreFinal >= 70) {
            sb.append("Interprétation : Images similaires\n");
        } else if (resultat.scoreFinal >= 50) {
            sb.append("Interprétation : Images moyennement similaires\n");
        } else {
            sb.append("Interprétation : Images différentes\n");
        }
        
        return sb.toString();
    }
    
  
    
   
}