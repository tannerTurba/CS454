import java.awt.image.BufferedImage;
import java.awt.Color;
import hw1.*;


public class ImageConverter {
    
    public static BufferedImage toBufferedImage(DigitalImage src) {
        int width = src.getWidth();
        int height = src.getHeight();
        BufferedImage bImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for(int row = 0; row < src.getHeight(); row++) {
            for(int column = 0; column < src.getWidth(); column ++) {
                int [] bands = src.getPixel(row, column);
                Color c = new Color(bands[0], bands[1], bands[2]);
                bImage.setRGB(column, row, c.getRGB());
            }
        }
        return bImage;
    }
    
    public static DigitalImage toDigitalImage(BufferedImage src) {
        int width = src.getWidth();
        int height = src.getHeight();
        PackedPixelImage pImage = new PackedPixelImage(width, height, 3);
        int[] bands = new int[3];

        for(int row = 0; row < src.getHeight(); row++) {
            for(int column = 0; column < src.getWidth(); column ++) {
                src.getData().getPixel(row, column, bands);
                pImage.setPixel(column, row, bands);
            }
        }
        return pImage;
    }
}
