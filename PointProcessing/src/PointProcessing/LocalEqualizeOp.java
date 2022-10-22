package PointProcessing;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import pixeljelly.features.Histogram;
import pixeljelly.ops.PluggableImageOp;
import pixeljelly.scanners.Location;
import pixeljelly.scanners.RasterScanner;

public class LocalEqualizeOp implements PluggableImageOp, BufferedImageOp {
    private int width = 0; 
    private int height = 0;
    private boolean brightnessBandOnly = false;

    public LocalEqualizeOp(int w, int h, boolean brightnessBandOnly) {
        super();
        width = w;
        height = h;
        this.brightnessBandOnly = brightnessBandOnly;
    }
    
    public LocalEqualizeOp() {
    	super();
    }

    public BufferedImageOp getDefault(BufferedImage src) {
        return new LocalEqualizeOp(5, 5, true);
    }

    public String getAuthorName() {
        return "Tanner Turba";
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean getBrightnessBandOnly() {
        return brightnessBandOnly;
    }

    public void setWidth(int w) {
        width = w;
    }

    public void setHeight(int h) {
        height = h;
    }

    public void setBrightnessBandOnly(boolean b) {
        brightnessBandOnly = b;
    }

    /**
     *  Performs local histogram equalization on an image.
     */
    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        //Creates a dest image if none exists.
        if(dest == null) {
            dest = createCompatibleDestImage(src, src.getColorModel());
        }
        
        //If looking at the brightness band.
        if(brightnessBandOnly) {
            //Get the brightness band from the image.
        	BufferedImageOp op = new BandExtractOp('V');
        	brightnessBandOnly = false;
        	BufferedImage tup = filter(op.filter(src, null), null);
        	
            //For each point in the image, 
        	for(Location pt : new RasterScanner(tup, false)) {
                //Get hsb bands of point.
        		Color rgb = new Color(src.getRGB(pt.col, pt.row));
        		float[] hsb = Color.RGBtoHSB(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), null);

                //Set the rgb color based on the brightness band.
        		int newColor = Color.HSBtoRGB(hsb[0], hsb[1], tup.getRaster().getSample(pt.col, pt.row, 0)/255);
        		dest.setRGB(pt.col, pt.row, newColor);
        	}
        }
        
        //Use rectangle objects to restrict bounds of equalization
        Rectangle iRect = new Rectangle(0, 0, src.getWidth() - 1, src.getHeight() - 1);
        for(Location pt : new RasterScanner(src, false)) {
        	Rectangle regionRect = new Rectangle(pt.col - width/2, pt.row - height/2, width, height);
        	regionRect = regionRect.intersection(iRect);
        	BufferedImage subImage = src.getSubimage(regionRect.x, regionRect.y, regionRect.width, regionRect.height);
        	
        	//Pass subImage into histogram to get cdf, then lookupTable
        	Histogram histogram = new Histogram(subImage, pt.band);
        	double[] cdf = histogram.getCDF();
        	float[] lookupTable = new float[cdf.length];
        	for(int i = 0; i < cdf.length; i++) {
        		lookupTable[i] = (float) (cdf[i] * cdf.length);
        	}
        	
        	//set the sample, using the original sample as the index into the lookup table.
        	dest.getRaster().setSample(pt.col, pt.row, pt.band, lookupTable[src.getRaster().getSample(pt.col, pt.row, pt.band)]);
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