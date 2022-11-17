package hw3;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;

import Jama.Matrix;
import pixeljelly.scanners.Location;
import pixeljelly.scanners.RasterScanner;

public class ImageDatabase {
	public static void main(String[] args) throws NumberFormatException, IOException {
		new ImageDatabase(args);
	}
	
	public ImageDatabase(String[] args) throws NumberFormatException, IOException {
		if (args[0].equals("create")) {
			// make sure it is rgb or hsb
			if (!(args[6].toLowerCase().equals("rgb") || args[6].toLowerCase().equals("hsb"))) {
				System.out.println("Only the RGB and HSB color models are supported.");
			}
			// check if X + Y + Z <= 10
			if (Integer.parseInt(args[1]) + Integer.parseInt(args[2]) + Integer.parseInt(args[3]) > 10) {
				System.out.println("sum of Xn, Yn, and Zn values must be less than or equal to 10");
			}
			// check if the values are between 1 and 8
			if (!(Integer.parseInt(args[1]) >= 1 && Integer.parseInt(args[1]) <= 8)
					|| !(Integer.parseInt(args[2]) >= 1 && Integer.parseInt(args[2]) <= 8)
					|| !(Integer.parseInt(args[3]) >= 1 && Integer.parseInt(args[3]) <= 8)) {
				System.out.println("Xn, Yn, and Zn values must be between [1, 8]");
			}
			create(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), args[4], args[5]);
		} else if (args[0].equals("query")) {
			query(args[1], args[2], args[3], (Integer.parseInt(args[4])));
		} else {
			System.out.println("First argument must be create or query. Other arguments are not supported.");
		}
	}

	public void create(int Xn, int Yn, int Zn, String url, String db, String colorModel) throws IOException, FileNotFoundException {
		// bits allocated to each band 
		int nr = (int) Math.pow(2, Xn);
		int ng = (int) Math.pow(2, Yn);
		int nb = (int) Math.pow(2, Zn);
		
		// open file for reading, open file for writing. Write resolutions to the file. 
		BufferedReader scan = new BufferedReader(new FileReader(new File(url)));
		FileWriter temp = new FileWriter(new File(db));
		temp.write(nr + " " + ng + " " + nb + '\n');
		
		// loop through all the urls in the file.
		String urlLine;
		while ((urlLine = scan.readLine()) != null) {
			// create histogram with proper length.
			double[] histogram = new double[(nr * ng * nb)];
			
			// write out the URL from the file, padded with newlines.
			temp.write('\n');
			temp.write(urlLine);
			temp.write('\n');
			String[] split = urlLine.split(" ");
			urlLine = split[2];
			
			//Remove newline char if necessary.
			if (urlLine.charAt(urlLine.length() - 1) == '\n') {
				urlLine = urlLine.substring(0, urlLine.length() - 1);
			}
			
			// read the image at the provided URL
			BufferedImage oracle;
			try {
				oracle = ImageIO.read(new URL(urlLine));
			} 
			catch (Exception e) {
				continue; 
			}
			
			// loop through each pixel in the image to generate the histogram
			WritableRaster src = oracle.getRaster();
			for (Location pt : new RasterScanner(oracle, false)) {
				int red = 0;
				int green = 0;
				int blue = 0;
				
				// if color image get each band, else use first band for all three.
				if(src.getNumBands() == 3) {
					red = src.getSample(pt.col, pt.row, 0);
					green = src.getSample(pt.col, pt.row, 1);
					blue = src.getSample(pt.col, pt.row, 2);
				}
				else if(src.getNumBands() == 1) {
					red = src.getSample(pt.col, pt.row, 0);
					green = src.getSample(pt.col, pt.row, 0);
					blue = src.getSample(pt.col, pt.row, 0);
				}
				
				// find the index of the histogram to increment and do so
				int index = getIndex(red, green, blue, nr, ng, nb);
				histogram[index]++;
			}
			// normalize the histogram
			int numPixels = src.getHeight() * src.getWidth();
			for (int i = 0; i < histogram.length; i++) {
				histogram[i] = histogram[i] / numPixels;
			}
			// store histogram in the DB file
			for (int i = 0; i < histogram.length; i++) {
				temp.write(Double.toString(histogram[i]));
				temp.write(" ");
			}
			
		}
		// close the scanner
		scan.close();
		temp.close();
	}

	public void query(String q, String db, String response, int k) throws MalformedURLException, IOException {
		// get the resolutions from the db file
		File dbFile = new File(db);
		Scanner scan = new Scanner(dbFile);
		int nr = scan.nextInt();
		int ng = scan.nextInt();
		int nb = scan.nextInt();
		// create the histogram and an array for the center of the colors at the needed
		// index
		double[] histogram = new double[(nr * ng * nb)];
		Color[] center = new Color[histogram.length];
		// read the q image
		BufferedImage oracle = ImageIO.read(new URL(q));
		// get the histogram for q
		WritableRaster src = oracle.getRaster();
		for (Location pt : new RasterScanner(oracle, false)) {
			int red = 0;
			int green = 0;
			int blue = 0;
			
			// if color image get each band, else use first band for all three.
			if(src.getNumBands() == 3) {
				red = src.getSample(pt.col, pt.row, 0);
				green = src.getSample(pt.col, pt.row, 1);
				blue = src.getSample(pt.col, pt.row, 2);
			}
			else if(src.getNumBands() == 1) {
				red = src.getSample(pt.col, pt.row, 0);
				green = src.getSample(pt.col, pt.row, 0);
				blue = src.getSample(pt.col, pt.row, 0);
			}
			
			// find the index of the histogram to increment and do so
			int index = getIndex(red, green, blue, nr, ng, nb);
			histogram[index]++;
			// find the center of the color for the center array
			// center[index] = getCenter(red, green, blue, nr, ng, nb);
		}
		// normalize the histogram
		int numPixels = src.getHeight() * src.getWidth();
		for (int i = 0; i < histogram.length; i++) {
			histogram[i] = histogram[i] / numPixels;
		}
		// find the centers
		for (int r = 0; r < nr; r++) {
			for (int g = 0; g < ng; g++) {
				for (int b = 0; b < nb; b++) {
					int i = r * (ng * nb) + g * nb + b;
					int redC = r * (256 / nr) + 128 / nr;
					int greenC = g * (256 / ng) + 128 / ng;
					int blueC = b * (265 / nb) + 128 / nb;
					center[i] = new Color(redC, greenC, blueC);
				}
			}
		}
		// create an ordered list to store similar histograms
		ArrayList<URLNode> similarPictures = new ArrayList<URLNode>();
		// loop through the db file and find similarities
		while (scan.hasNextLine()) {
			// create a histogram to store for the histogram in the file
			double[] compHistogram = new double[(nr * ng * nb)];
			// get the data from the db file
			for (int i = 0; i < (nr * ng * nb); i++) {
				compHistogram[i] = scan.nextDouble();
			}
			// compute A
			double[][] Atemp = createA(histogram, compHistogram, center);
			Matrix A = new Matrix(Atemp);
			// compute H1 - H2
			double[][] hDiffTemp = subtract(histogram, compHistogram);
			Matrix hDiff = new Matrix(hDiffTemp);
			// get the transpose of H1 - H2
			Matrix transpose = hDiff.transpose();
			// do the matrix multiplication and get the distance

//			System.out.println(hDiff.getRowDimension() + " " + hDiff.getColumnDimension() + " " + A.getRowDimension()
//					+ " " + A.getColumnDimension());
			Matrix temp = hDiff.times(A);
//			System.out.println(temp.getRowDimension() + " " + temp.getColumnDimension() + " "
//					+ transpose.getRowDimension() + " " + transpose.getColumnDimension());
			Matrix result = temp.times(transpose);
			// read the url and store into the sorted list of URLs
			double histogramDistance = result.get(0, 0);
			String url = scan.nextLine();
			// System.out.println(url);
			url = scan.nextLine();
			String[] split = url.split(" ");
			URLNode toAdd = new URLNode(split, histogramDistance);
			addNode(similarPictures, toAdd);
		}
		// print the list of ordered urls to the file
		printList(response, similarPictures, q);
		// close the scanner
		scan.close();
	}

	public void printList(String filename, ArrayList<URLNode> list, String q) throws IOException {
		// change this to an html file later
		File html = new File(filename);
		FileWriter temp = new FileWriter(html);
		// print out the header stuff
		temp.write("<!DOCTYPE html>" + '\n');
		temp.write("<html><head><title>Pictures</title>" + '\n');
		temp.write("<link href=\"style.css\" rel=\"stylesheet\"></head>" + '\n');
		temp.write("<body>" + '\n' + '\n');
		// write out the query image info
		temp.write("<img class=\"query\" src=\"" + q + "\">" + '\n' + '\n');
		for (int i = 0; i < list.size(); i++) {
			temp.write("<div class=\"img\">" + '\n');
			temp.write("<a href=\"" + list.get(i).url[0] + "\";" + '\n');
			temp.write("class=\"flickr\"></a>" + '\n');
			temp.write("<a" + '\n');
			temp.write("href=\"" + list.get(i).url[1] + "\">;" + '\n');
			temp.write("<img" + '\n');
			temp.write("src=\"" + list.get(i).url[2] + "\">;" + '\n');
			temp.write("</div>" + '\n');
			temp.write("distance: " + list.get(i).distance + '\n');
		}
		// write out the footer
		temp.write("</body>" + '\n' + "</html>");
		// close the file writer
		temp.close();
	}

	public void addNode(ArrayList<URLNode> list, URLNode toAdd) {
		if (list.isEmpty()) {
			list.add(toAdd);
			return;
		} else {
			for (int i = 0; i < list.size(); i++) {
				if (toAdd.distance < list.get(i).distance) {
					list.add(i, toAdd);
					return;
				}
			}
			list.add(toAdd);
		}
	}

	public double[][] subtract(double[] h, double[] cH) {
		double[][] toReturn = new double[1][h.length];
		for (int i = 0; i < h.length; i++) {
			toReturn[0][i] = h[i] - cH[i];
		}
		return toReturn;
	}

	public Color getCenter(int r, int g, int b, int nr, int ng, int nb) {
		int rFloor = (int) (r * nr / 256);
		int gFloor = (int) (g * ng / 256);
		int bFloor = (int) (b * nb / 256);
		int rC = rFloor * (256 / nr) + 128 / nr;
		int gC = gFloor * (256 / ng) + 128 / ng;
		int bC = bFloor * (256 / nb) + 128 / nb;
		return new Color(rC, gC, bC);
	}

	public double[][] createA(double[] histogram, double[] compHistogram, Color[] center) {
		double[][] A = new double[histogram.length][histogram.length];
		double max = 0;
		for (int i = 0; i < histogram.length; i++) {
			for (int j = 0; j < histogram.length; j++) {
				// compute ci and cj
				Color ci = center[i];
				Color cj = center[j];
				// get the distance between the colors
				double distance = getDistance(ci, cj);
				// keep track of the max distance
				if (distance > max) {
					max = distance;
				}
			}
		}
		// now compute A[i][j] with the maximum value that was found
		for (int i = 0; i < histogram.length; i++) {
			for (int j = 0; j < histogram.length; j++) {
				// compute ci and cj
				Color ci = center[i];
				Color cj = center[j];
				// get the distance between the colors
				double distance = getDistance(ci, cj);
				// compute A[i][j]. Dividing by 765 because max distance would be 255 + 255 +
				// 255. MAX VAL IN HISTOGRAM
				A[i][j] = 1 - (distance / max);
			}
		}
		return A;
	}

	public double getDistance(Color ci, Color cj) {
		int red = Math.abs(ci.getRed() - cj.getRed());
		int green = Math.abs(ci.getGreen() - cj.getGreen());
		int blue = Math.abs(ci.getBlue() - cj.getBlue());
		return red + green + blue;
	}

	public int getIndex(int r, int g, int b, int nr, int ng, int nb) {
		int rFloor = (int) (r * nr / 256.);
		int gFloor = (int) (g * ng / 256.);
		int bFloor = (int) (b * nb / 256.);
		return rFloor * (ng * nb) + gFloor * (nb) + bFloor;
	}
}
