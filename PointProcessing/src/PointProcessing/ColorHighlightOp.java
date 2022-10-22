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

public class ColorHighlightOp implements PluggableImageOp, BufferedImageOp {
    private Color targetColor;

    public ColorHighlightOp(Color targetColor) {
        super();
        this.targetColor = targetColor;
    }
    
    public ColorHighlightOp() {
    	super();
    }

    public BufferedImageOp getDefault(BufferedImage src) {
        return new ColorHighlightOp(new Color(220, 50, 50));
    }

    public String getAuthorName() {
        return "Tanner Turba";
    }

    public Color getTargetColor() {
    	return targetColor;
    }
    
    public void setTargetColor(Color targetColor) {
    	this.targetColor = targetColor;
    }
    
    /**
     * Calculate and return the L2 distance from the current color to the target color.
     * @param color
     * @return
     */
    private double getL2Distance(Color color) {
        return Math.sqrt(Math.pow((color.getRed() - targetColor.getRed()), 2) + 
						Math.pow((color.getGreen() - targetColor.getGreen()), 2) + 
						Math.pow((color.getBlue() - targetColor.getBlue()), 2)) / 255;
    }

    /**
     * Highlights the desired target color in the image.
     */
    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        //Create a destination image if none exists.
        if(dest == null) {
            dest = createCompatibleDestImage(src, src.getColorModel());
        }
        
        //For each point in the image, 
        for(Location pt : new RasterScanner(src, false)) {
            //Get all band information.
        	Color rgb = new Color(src.getRGB(pt.col, pt.row));
        	float[] hsb = Color.RGBtoHSB(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), null);

            //Calculate the distance between colors and adjust S value.
        	double distance = getL2Distance(rgb);
        	hsb[1] = (float)(Math.min(1, (hsb[1] * 1.1 * Math.pow(Math.E, (-3 * distance)))));

            //Create new color and set in the dest image.
        	int newColor = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
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