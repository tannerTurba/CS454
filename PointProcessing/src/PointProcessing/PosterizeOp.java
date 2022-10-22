package PointProcessing;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import pixeljelly.ops.PluggableImageOp;
import pixeljelly.scanners.Location;
import pixeljelly.scanners.RasterScanner;

public class PosterizeOp implements PluggableImageOp, BufferedImageOp {
    private Color[] VALIDCOLORS = {Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.BLACK, Color.WHITE};
    
    public PosterizeOp() {
    	super();
    }

    public BufferedImageOp getDefault(BufferedImage src) {
        return new PosterizeOp();
    }

    public String getAuthorName() {
        return "Tanner Turba";
    }
    
    /**
     * Finds the most similar color in the palette to the color that is passed.
     * @param color the color that will be replaced.
     * @return the closest of the valid colors. 
     */
    private int findSimilarColor(Color color) {
        double bestL2 = 10.0;
        int index = -1;

        //Loop through the palette and find the index of the most similar color using the L2 formula. 
        for (int i = 0; i < VALIDCOLORS.length; i++) {
            double l2 = Math.sqrt(Math.pow((color.getRed() - VALIDCOLORS[i].getRed()), 2) + 
            						Math.pow((color.getGreen() - VALIDCOLORS[i].getGreen()), 2) + 
            						Math.pow((color.getBlue() - VALIDCOLORS[i].getBlue()), 2)) / 256;
            if(l2 < bestL2) {
                bestL2 = l2;
                index = i;
            }
        }
        return VALIDCOLORS[index].getRGB();
    }

    /**
     * Replace each pixel with one of the given valid colors.
     */
    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        //Create a destination image if one doesn't exits.
        if(dest == null) {
            dest = createCompatibleDestImage(src, src.getColorModel());
        }
        
        //For each point in the image, 
        for(Location pt : new RasterScanner(src, true)) {
            //Replace each of the colors with a valid color.
        	Color rgb = new Color(src.getRGB(pt.col, pt.row));
        	int newColor = findSimilarColor(rgb);
        	dest.setRGB(pt.col, pt.row, newColor);
        }
        return dest;
    }

    @Override
    public Rectangle2D getBounds2D(BufferedImage src) {
        return new Rectangle(src.getWidth(), src.getHeight());
    }

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
        return new BufferedImage(destCM, 
                                destCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), 
                                destCM.isAlphaPremultiplied(), 
                                null);
    }

    @Override
    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RenderingHints getRenderingHints() {
        // TODO Auto-generated method stub
        return null;
    }
}
