import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class SeamCarvingGreedy {
    public static void main(String[] args) {
        try {
            File file = new File("cartoon.jpg");
            BufferedImage img = ImageIO.read(file);
            if (img == null) {
                System.out.println("Error: Image not found");
                return;
            }


            int width = img.getWidth();
            int height = img.getHeight(); 

            System.out.print("Enter how much to resize (e.g., 0.7 or 0.5): ");
            Scanner scanner = new Scanner(System.in);
            double s = scanner.nextDouble();
            int targetWidth = (int) (width * s);

            System.out.println("Original width = " + width);
            System.out.println("Original height = " + height);

            while (width > targetWidth) {
                double[][] energy = computeEnergyMatrix(img);
                int [] seam = findSeamGreedy(energy);
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


    
    
 public static int[] findSeamGreedy(double[][] energy) {
        int rows = energy.length;
        int cols = energy[0].length;
        int[] seam = new int[rows];

        // Start from the top row with the lowest energy pixel
        int minIndex = 0;
        for (int j = 1; j < cols; j++) {
            if (energy[0][j] < energy[0][minIndex]) {// comparing first row pixels to fine the minimum pixel
                minIndex = j;
            }
        }
        seam[0] = minIndex;// the start of the seam we are going to remove

        // get the lowest energy neighbor 
        for (int i = 1; i < rows; i++) {
            int prev = seam[i - 1];// prev = the previous row placement
            double minEnergy = energy[i][prev];// the pixel directly under
            int newR = prev; // new = the current row placement

            if (prev > 0 && energy[i][prev - 1] < minEnergy) {
                newR = prev - 1;
                minEnergy = energy[i][prev - 1];//the pixel on the left diagonal
            }
            if (prev < cols - 1 && energy[i][prev + 1] < minEnergy) {
                newR = prev + 1;
                minEnergy = energy[i][prev + 1];//the pixel on the right diagonal

            }
            seam[i] = newR; 
        }
        return seam;
    }
    
    public static BufferedImage removeSeam(BufferedImage img, int[] seam) {
    int width = img.getWidth();
    int height = img.getHeight();
    
    // Create a new image with one column less (since we are removing a seam)
    BufferedImage newImage = new BufferedImage(width - 1, height, BufferedImage.TYPE_INT_RGB);

    // Iterate over each row to rebuild the image without the removed seam
    for (int row = 0; row < height; row++) {
        int newCol = 0; // Index for the new column in the modified image

        for (int col = 0; col < width; col++) {
            // Skip the pixel that belongs to the seam (the least energy path)
            if (col == seam[row]) continue;
            
            // Copy the remaining pixels to the new image
            newImage.setRGB(newCol++, row, img.getRGB(col, row));
        }
    }
    return newImage; // Return the resized image after removing the seam
}
    
   

}
