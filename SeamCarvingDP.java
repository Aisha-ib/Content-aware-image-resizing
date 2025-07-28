import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;

public class SeamCarvingDP{
    public static void main(String[] args) {
        try {
            File file = new File("sample2.jpg");
            BufferedImage img = ImageIO.read(file);
            if (img == null) {
                System.out.println("Error: Image not found");
                return;
            }

            int width = img.getWidth();
            int height = img.getHeight();
            System.out.println("Original width = " + width);
            System.out.println("Original height = " + height);

            int targetWidth = (int) (width * 0.5);

            while (width > targetWidth) {
                double[][] energy = computeEnergyMatrix(img);
                int[] seam = computeSeam(energy); 
                img = removeSeam(img, seam);  
                width--; 
            }

            System.out.println("Final width = " + width);
            System.out.println("Final height = " + height);

            File outputFile = new File("output.jpg");

            if (ImageIO.write(img, "jpg", outputFile)) {
                System.out.println("Image saved successfully!");
            } else {
                System.out.println("Error: Could not save image.");
            }

        } catch (IOException e) {
            System.out.println("Error loading or saving image: " + e.getMessage());
        }
    }

    public static double[][] computeEnergyMatrix(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        double[][] energy = new double[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                energy[i][j] = computeEnergy(img, j, i);
            }
        }
        return energy;
    }

    public static double computeEnergy(BufferedImage img, int i, int j) {

        
        // finding pixel neighbours and their brightness 
        double a = getBrightness(img ,i - 1, j - 1);
        double b = getBrightness(img,i, j - 1);
        double c = getBrightness(img,i + 1, j - 1);
        double d = getBrightness(img,i - 1, j);
        double f = getBrightness(img,i + 1, j);
        double g = getBrightness(img,i - 1, j + 1);
        double h = getBrightness(img,i, j + 1);
        double I = getBrightness(img,i + 1, j + 1);

        double xenergy = a + 2 * d + g - c - 2 * f - I;
        double yenergy = a + 2 * b + c - g - 2 * h - I;

        return Math.sqrt((xenergy * xenergy) + (yenergy * yenergy));
    }

    public static double getBrightness(BufferedImage img, int x, int y) {

        int width = img.getWidth();
        int height = img.getHeight();

        if (x <= 0 || x >= width-1 || y <= 0 || y >= height-1) {  // for edge pixels
            return 0;
        }
        Color color = new Color(img.getRGB(x, y));
        return color.getRed() + color.getGreen() + color.getBlue();
    }

    public static int[] computeSeam(double[][] energy) {
        int height = energy.length;
        int width = energy[0].length;
        int[] bestSeam = new int[height];
        double minEnergy = Double.MAX_VALUE;

        for (int startX = 0; startX < width; startX++) {
            int[] currentSeam = new int[height];
            currentSeam[0] = startX;
            double totalEnergy = energy[0][startX];

            for (int y = 1; y < height; y++) {
                int prevX = currentSeam[y - 1];
                int bestX = prevX;
                double min = energy[y][prevX];

                if (prevX > 0 && energy[y][prevX - 1] < min) {
                    min = energy[y][prevX - 1];
                    bestX = prevX - 1;
                }
                if (prevX < width - 1 && energy[y][prevX + 1] < min) {
                    min = energy[y][prevX + 1];
                    bestX = prevX + 1;
                }

                currentSeam[y] = bestX;
                totalEnergy += min;
            }

            if (totalEnergy < minEnergy) {
                minEnergy = totalEnergy;
                bestSeam = currentSeam.clone();
            }
        }
        return bestSeam;
    } 
       
    public static BufferedImage removeSeam(BufferedImage img, int[] seam) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage newImage = new BufferedImage(width - 1, height, BufferedImage.TYPE_INT_RGB);

        for (int row = 0; row < height; row++) {
            int newCol = 0;
            for (int col = 0; col < width; col++) {
                if (col == seam[row]) continue;
                newImage.setRGB(newCol, row, img.getRGB(col, row));
                newCol++;
            }
        }

        return newImage;
    }
}
