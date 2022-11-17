package hw3;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import pixeljelly.ops.ConvolutionOp;
import pixeljelly.ops.NullOp;
import pixeljelly.ops.PluggableImageOp;
import pixeljelly.scanners.Location;
import pixeljelly.scanners.RasterScanner;
import pixeljelly.utilities.NonSeperableKernel;

public class OrientedEdgeOp extends NullOp implements PluggableImageOp {
	private double strength, orientation, epsilon;

	public OrientedEdgeOp() {
		setStrength(0.5);
		setOrientation(0.75);
		setEpsilon(0.15);
	}
	
	public OrientedEdgeOp(double strength, double orientation, double epsilon) {
		setStrength(strength);
		setOrientation(orientation);
		setEpsilon(epsilon);
	}
	
	public double getStrength() {
		return strength;
	}
	
	public double getOrientation() {
		return orientation;
	}
	
	public double getEpsilon() {
		return epsilon;
	}
	
	public void setStrength(double strength) {
		this.strength = strength;
	}
	
	public void setOrientation(double orientation) {
		this.orientation = orientation;
	}
	
	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}
	
	@Override
	public String getAuthorName() {
		return "Tanner & Graelynn";
	}

	@Override
	public BufferedImageOp getDefault(BufferedImage arg0) {
		return new OrientedEdgeOp();
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		double orientationDegrees = orientation * 360;
		double epsilonDegrees = epsilon * 360;
		
		dest = super.filter(src, dest);
		NonSeperableKernel rowV = new NonSeperableKernel(3, 1, new float[] {-0.5f, 0, 0.5f});
		NonSeperableKernel colV = new NonSeperableKernel(1, 3, new float[] {-0.5f, 0, 0.5f});
		
		ConvolutionOp horizontalOp = new ConvolutionOp(rowV, 128);
		ConvolutionOp verticalOp = new ConvolutionOp(colV, 128);
		
		BufferedImage vGradient = verticalOp.filter(src, null);
		BufferedImage hGradient = horizontalOp.filter(src, null);
		
		for(Location pt : new RasterScanner(vGradient, false)) {
			double verticalMag = (vGradient.getRaster().getSample(pt.col, pt.row, 0) - 128) / 128.0;
			double horizontalMag = (hGradient.getRaster().getSample(pt.col, pt.row, 0) - 128) / 128.0;
			
			if(horizontalMag != 0) {
				double magnitude = (Math.sqrt(Math.pow(verticalMag, 2) + Math.pow(horizontalMag, 2)) / Math.sqrt(2));
				double angle = Math.toDegrees(Math.atan2(verticalMag, horizontalMag));
				if(angle < 0) {
					angle += 360;
				}
				
				if(magnitude >= strength && (orientationDegrees - epsilonDegrees) <= angle && angle <= (orientationDegrees + epsilonDegrees)) {
					dest.setRGB(pt.col, pt.row, Color.RED.getRGB());
				}
			}
		}
		return dest;
	}
}
