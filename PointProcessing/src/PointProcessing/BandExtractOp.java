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

public class BandExtractOp implements PluggableImageOp, BufferedImageOp {
    private char band;

    public BandExtractOp(char band) {
        super();
        this.band = band;
    }
    
    public BandExtractOp() {
    	super();
    }

    public BufferedImageOp getDefault(BufferedImage src) {
        return new BandExtractOp('H');
    }

    public String getAuthorName() {
        return "Tanner Turba";
    }

    public char getBand() {
    	return band;
    }

	/**
	 * Sets the value of the band, checking if it is a valid input.
	 * @param band the new band value.
	 * @throws IllegalArgumentException if the new band value is invalid.
	 */
    public void setBand(char band) throws IllegalArgumentException {
    	if(band == 'R' || band == 'G' || band == 'B' || band == 'Y' || band == 'I' || band == 'Q' || band == 'H' || band == 'S' || band == 'V') {
    		this.band = band;
    	}
    	else {
    		throw new IllegalArgumentException();
    	}
    }
    
	/**
	 * Converts rgb to yiq
	 * @param rgb the rgb color.
	 * @return an array of yiq containing each element
	 */
    private float[] RGBtoYIQ(Color rgb) {
    	float r = rgb.getRed();
    	float g = rgb.getGreen();
    	float b = rgb.getBlue();
    	
    	float[] yiq = new float[3];
    	yiq[0] = (0.299900f * r) + (0.587000f * g) + (0.114000f * b);
        yiq[1] = (0.595716f * r) - (0.274453f * g) - (0.321264f * b);
        yiq[2] = (0.211456f * r) - (0.522591f * g) + (0.311350f * b);
        return yiq;
    }

	/**
	 * Performs in-place operations that returns a single band extracted from
	 * the source file, represented as a grayscale image.
	 */
    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		//Create image for destination if one is not available.
        if(dest == null) {
            dest = createCompatibleDestImage(src, src.getColorModel());
        }
        
		//For each point in the image,
        for(Location pt : new RasterScanner(src, false)) {
			//Get all representations of the current pixel color.
        	Color rgb = new Color(src.getRGB(pt.col, pt.row));
        	float[] yiq = RGBtoYIQ(rgb);
        	float[] hsv = Color.RGBtoHSB(rgb.getRed(), rgb.getBlue(), rgb.getGreen(), null);
        	
			//Set each band to the desired band to be extracted. 
        	switch(band) {
        	case 'R':	dest.getRaster().setSample(pt.col, pt.row, 0, rgb.getRed());
        				dest.getRaster().setSample(pt.col, pt.row, 1, rgb.getRed());
        				dest.getRaster().setSample(pt.col, pt.row, 2, rgb.getRed());
        				break;
        	case 'G': 	dest.getRaster().setSample(pt.col, pt.row, 0, rgb.getGreen());
			        	dest.getRaster().setSample(pt.col, pt.row, 1, rgb.getGreen());
			        	dest.getRaster().setSample(pt.col, pt.row, 2, rgb.getGreen());
						break;
        	case 'B': 	dest.getRaster().setSample(pt.col, pt.row, 0, rgb.getBlue());
			        	dest.getRaster().setSample(pt.col, pt.row, 1, rgb.getBlue());
			        	dest.getRaster().setSample(pt.col, pt.row, 2, rgb.getBlue());
						break;
        	case 'Y': 	dest.getRaster().setSample(pt.col, pt.row, 0, yiq[0]);
			        	dest.getRaster().setSample(pt.col, pt.row, 1, yiq[0]);
			        	dest.getRaster().setSample(pt.col, pt.row, 2, yiq[0]);
						break;
        	case 'I': 	dest.getRaster().setSample(pt.col, pt.row, 0, yiq[1]);
			        	dest.getRaster().setSample(pt.col, pt.row, 1, yiq[1]);
			        	dest.getRaster().setSample(pt.col, pt.row, 2, yiq[1]);
						break;
        	case 'Q': 	dest.getRaster().setSample(pt.col, pt.row, 0, yiq[2]);
			        	dest.getRaster().setSample(pt.col, pt.row, 1, yiq[2]);
			        	dest.getRaster().setSample(pt.col, pt.row, 2, yiq[2]);
						break;
        	case 'H': 	dest.getRaster().setSample(pt.col, pt.row, 0, hsv[0] * 255);
			        	dest.getRaster().setSample(pt.col, pt.row, 1, hsv[0] * 255);
			        	dest.getRaster().setSample(pt.col, pt.row, 2, hsv[0] * 255);
						break;
        	case 'S': 	dest.getRaster().setSample(pt.col, pt.row, 0, hsv[1] * 255);
			        	dest.getRaster().setSample(pt.col, pt.row, 1, hsv[1] * 255);
			        	dest.getRaster().setSample(pt.col, pt.row, 2, hsv[1] * 255);
        				break;
        	case 'V': 	dest.getRaster().setSample(pt.col, pt.row, 0, hsv[2] * 255);
			        	dest.getRaster().setSample(pt.col, pt.row, 1, hsv[2] * 255);
			        	dest.getRaster().setSample(pt.col, pt.row, 2, hsv[2] * 255);
						break;
        	}
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