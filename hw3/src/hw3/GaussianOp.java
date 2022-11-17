package hw3;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import pixeljelly.ops.ConvolutionOp;
import pixeljelly.ops.NullOp;
import pixeljelly.ops.PluggableImageOp;
import pixeljelly.utilities.SeperableKernel;

public class GaussianOp extends NullOp implements PluggableImageOp{
	private double alpha, sigma;
	
	public GaussianOp() {
		alpha = 2;
		sigma = 3;
	}
	
	public GaussianOp(double alpha, double sigma) {
		setAlpha(alpha);
		setSigma(sigma);
	}
	
	public double getStrength() {
		return Math.ceil(alpha * sigma);
	}
	
	public double getAlpha() {
		return alpha;
	}
	
	public double getSigma() { 
		return sigma;
	}
	
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public void setSigma(double sigma) {
		this.sigma = sigma;
	}
	
	@Override
	public String getAuthorName() {
		return "Tanner & Graelynn";
	}

	@Override
	public BufferedImageOp getDefault(BufferedImage arg0) {
		return new GaussianOp(2.0, 3.0);
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		float[] coefficients = getGaussianCoefficients(alpha, sigma);
		SeperableKernel sKernel = new SeperableKernel(coefficients, coefficients);
		ConvolutionOp c = new ConvolutionOp(sKernel, true);
		return c.filter(src, dest);
	}
	
	private float[] getGaussianCoefficients(double alpha, double sigma) {
		int w = (int)Math.ceil(alpha * sigma);
		float[] result = new float[w * 2 + 1];
		for(int n = 0; n <= w; n++) {
			double coefficient = Math.exp(-(n * n) / (2 * sigma * sigma)) / (Math.sqrt(2 * Math.PI) * sigma);
			result[w + n] = (float)coefficient;
			result[w - n] = (float)coefficient;
		}
		return result;
	}
}
