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

public class ShiftOp implements PluggableImageOp, BufferedImageOp {
    private double hueTarget, satScale, shiftStrength;

    public ShiftOp(double hueTarget, double satScale, double shiftStrength) {
        super();
        this.hueTarget = hueTarget;
        this.satScale = satScale;
        this.shiftStrength = shiftStrength;
    }
    
    public ShiftOp() {
    	super();
    }

    public BufferedImageOp getDefault(BufferedImage src) {
        return new ShiftOp(0, 1.5, 1);
    }

    public String getAuthorName() {
        return "Tanner Turba";
    }

    public double getHueTarget() {
        return hueTarget;
    }

    public double getSatScale() {
        return satScale;
    }

    public double getShiftStrength() {
        return shiftStrength;
    }

    public void setHueTarget(double hueTarget) {
        this.hueTarget = hueTarget;
    }

    public void setSatScale(double satScale) {
    	this.satScale = satScale;
    }

    public void setShiftStrength(double shiftStrength) {
    	this.shiftStrength = shiftStrength;
    }
    
    /**
     * Shifts the hue, based on the difference to the target hue and the shift strength.
     * @param hue
     * @return
     */
    private double hShift(float hue) {
        double diff = hueTarget - hue;
        if(diff < 0 ) {
        	diff *= -1;
        }
        double dH = diff % 1.0;
        return hue + Math.pow(dH, shiftStrength);
    }

    /**
     * Adjusts the hue and saturation of the image.
     */
    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        //Create a dest image if one doesn't exist.
        if(dest == null) {
            dest = createCompatibleDestImage(src, src.getColorModel());
        }
        
        //For each point in the image, 
        for(Location pt : new RasterScanner(src, false)) {
            //Get hsb band values.
        	Color rgb = new Color(src.getRGB(pt.col, pt.row));
        	float[] hsb = Color.RGBtoHSB(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), null);

            //Adjust hue and saturation.
        	hsb[0] = (float) hShift(hsb[0]);
        	hsb[1] = (float) (hsb[1] * satScale);

            //Set pixel in the image.
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