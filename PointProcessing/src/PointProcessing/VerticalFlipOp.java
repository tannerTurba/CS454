package PointProcessing;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;

import pixeljelly.ops.PluggableImageOp;

public class VerticalFlipOp implements PluggableImageOp, BufferedImageOp {
    public VerticalFlipOp() {
        super();
    }

    public BufferedImageOp getDefault(BufferedImage src) {
        return new HorizontalFlipOp();
    }

    public String getAuthorName() {
        return "Tanner Turba";
    }

    /**
     * Reflects the image over a vertical line.
     */
    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        if(dest == null) {
            dest = createCompatibleDestImage(src, src.getColorModel());
        }
        
        for(int row = 0; row < src.getHeight(); row++) {
            for(int column = 0; column < src.getWidth(); column++) {
                int srcPixel = src.getRGB(column, row);
                dest.setRGB(src.getWidth() - 1 - column, row, srcPixel);
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