import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class SeamCarvingBF {

    public static void main(String[] args) {
        try {
            File file = new File("cartoonn.jpg");
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
                List<Integer> seam = computeSeam(energy);
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


    static List<Integer> lowSeam;
    static double minCost;
    
       /**
     * Brute-force seam computation:
     * For each pixel in the top row, recursively try all possible downward paths,
     * tracking the cumulative energy. The path (seam) with the smallest total energy
     * is chosen.
     */

    public static List<Integer> computeSeam(double[][] E) {  // E The energy matrix 
        int width = E[0].length; 
        int height = E.length;
    
        minCost = Double.POSITIVE_INFINITY;    // Initialize to a very high value.
        lowSeam  = new ArrayList<>(); // list of column indices for each row store the path with lowest energy 
    
        for (int col = 0; col < width; col++) {  
            findSeam(0, col, 0, new ArrayList<>(), E);
        }
    
        return lowSeam ;
    }
    
    private static void findSeam(int row, int col, double cost, List<Integer> seam, double[][] E) {
        int height = E.length; 
        int width = E[0].length;
    
        if (col < 0 || col >= width) return;
    
        seam.add(col);
        cost += E[row][col];
    
        if (row == height - 1) { // when reach the last row, compare with mincost and update lowSeam  
            if (cost < minCost) {
                minCost = cost;
                lowSeam  = new ArrayList<>(seam); 
            }
        } else {
            for (int nextCol = col - 1; nextCol <= col + 1; nextCol++) { // recursively explore (left, center, right)
                findSeam(row + 1, nextCol, cost, seam, E);
            }
        }
        seam.remove(seam.size() - 1);  // remove last coiumn after exploring all paths 
    }
    
    public static BufferedImage removeSeam(BufferedImage img, List<Integer> seam) {
    int width = img.getWidth();
    int height = img.getHeight();
    
    // Create a new image with one column less (since we are removing a seam)
    BufferedImage newImage = new BufferedImage(width - 1, height, BufferedImage.TYPE_INT_RGB);

    // Iterate over each row to rebuild the image without the removed seam
    for (int row = 0; row < height; row++) {
        int newCol = 0; // Index for the new column in the modified image

        for (int col = 0; col < width; col++) {
            // Skip the pixel that belongs to the seam (the least energy path)
            if (col == seam.get(row)) continue;
            
            // Copy the remaining pixels to the new image
            newImage.setRGB(newCol++, row, img.getRGB(col, row));
        }
    }
    return newImage; // Return the resized image after removing the seam
}
}