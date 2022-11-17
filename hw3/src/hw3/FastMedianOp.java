package hw3;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import pixeljelly.ops.NullOp;
import pixeljelly.ops.PluggableImageOp;
import pixeljelly.scanners.Location;
import pixeljelly.scanners.RasterScanner;
import pixeljelly.utilities.ImagePadder;
import pixeljelly.utilities.ReflectivePadder;

public class FastMedianOp extends NullOp implements PluggableImageOp{
	private int m, n;
	private ImagePadder padder = ReflectivePadder.getInstance();
	
	public FastMedianOp() {
		m = 9;
		n = 9;
	}
	
	public FastMedianOp(int m, int n) {
		this.m = m;
		this.n = n;
	}
	
	public int getM() {
		return m;
	}
	
	public int getN() {
		return n;
	}
	
	@Override
	public String getAuthorName() {
		return "Tanner & Graelynn";
	}

	@Override
	public BufferedImageOp getDefault(BufferedImage arg0) {
		return new FastMedianOp(9, 9);
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		if(dest == null) {
            dest = createCompatibleDestImage(src, src.getColorModel());
        }
		
		int[] histCounts = new int[256];
		int median = -1;
		int sum = 0;
		
		for(int band = 0; band < src.getRaster().getNumBands(); band ++) {
			for(Location pt : new RasterScanner(src, false)) {
				Rectangle regionRect = new Rectangle(pt.col - m/2, pt.row - n/2, m, n);
		    	
		    	//Reset histogram array if looking at a new band or null.
		    	if(pt.col == 0) {
		    		histCounts = new int[256];
		    		for(int x = regionRect.x; x < regionRect.x + regionRect.width; x++) {
		    			for(int y = regionRect.y; y < regionRect.y + regionRect.height; y++) {
		    				histCounts[padder.getSample(src, x, y, band)]++;
		    			}
		    		}
		    		median = 0;
		    		sum = histCounts[median];
		    	}
		    	else {
		    		//Remove left-most column from the histogram and decrement sum if necessary.
	    			for(int y = regionRect.y; y < regionRect.y + regionRect.height; y++) {
	    				int sampleValue = padder.getSample(src, regionRect.x - 1, y, band);
		    			histCounts[sampleValue]--;
		    			if(sampleValue <= median) {
		    				sum--;
		    			}
	    			}
		    		
		    		//Add right-most column to the histogram.
		    		for(int y = regionRect.y; y < regionRect.y + regionRect.height; y++) {
	    				int sampleValue = padder.getSample(src, regionRect.x + regionRect.width - 1, y, band);
		    			histCounts[sampleValue]++;
		    			if(sampleValue <= median) {
		    				sum++;
		    			}
	    			}
		    	}	 
		    	int target = m * n / 2 + 1;
		    	
	    		//Search top-down.
		    	if(sum < target) {
			    	//Top-down until median has been found.
		    		while(sum < target) {
		    			median++;
		    			sum += histCounts[median];
		    		}
		    	}
		    	//Search bottom-up.
		    	else if(sum > target) {
		    		//Set index to previous median and search bottom-up until median is found.
		    		while(sum > target) {
		    			sum -= histCounts[median];
		    			median--;
		    		}
		    		if(sum != target) {
		    			median++;
		    			sum+= histCounts[median];
		    		}
		    	}
		    	
	    		dest.getRaster().setSample(pt.col, pt.row, band, median);
			}
		}
		return dest;
	}

}

