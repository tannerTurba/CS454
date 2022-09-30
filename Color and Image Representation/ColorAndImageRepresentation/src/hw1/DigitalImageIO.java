package hw1;
import java.io.*;
import java.util.Scanner;

public class DigitalImageIO {
    public enum ImageType { INDEXED, PACKED, LINEAR_ARRAY, MULTIDIM_ARRAY };
    
    public static DigitalImage read( File file, ImageType type ) throws IOException, IllegalFileFormatException {
        //Open file and check the file format. 
        Scanner scanner = new Scanner(file);
        String fileFormat = scanner.nextLine();
        if(!scanner.next().equals("P3")) {
            scanner.close();
            throw new IllegalFileFormatException(fileFormat, "P3");
        }
        
        //Get the width and height of the image and pass to the constructor.
        int width = Integer.parseInt(scanner.nextLine());
        int height = Integer.parseInt(scanner.nextLine());
        DigitalImage image;
        switch(type) {
            case INDEXED: 
                image = new IndexedDigitalImage(width, height);
                break;
            case PACKED: 
                image = new PackedPixelImage(width, height, 3);
                break;
            case LINEAR_ARRAY: 
                image = new LinearArrayDigitalImage(width, height, 3);
                break;
            case MULTIDIM_ARRAY: 
                image = new ArrayDigitalImage(width, height, 3);
                break;
            default:
                scanner.close();
                throw new IllegalFileFormatException();
        }

        //Read in the RGB components, pack them in an array, and add to the image.
        for(int row = 0; row < height; row++) {
            for(int column = 0; column < width; column++) {
                int red = Integer.parseInt(scanner.next());
                int green = Integer.parseInt(scanner.next());
                int blue = Integer.parseInt(scanner.next());
                int [] pixel = {red, green, blue};
                image.setPixel(row, column, pixel);
            }
            //Consume the whitespace and newline character. 
            scanner.nextLine();
        }

        scanner.close();
        return image;
    }
    
    public static void write( File file, DigitalImage image ) throws IOException {
        //Open the file writer and write format, width, and height to the file.
        FileWriter fWriter = new FileWriter(file);
        fWriter.write("P3\n");
        fWriter.write(image.getWidth() + '\n');
        fWriter.write(image.getHeight() + '\n');

        //Loop through the DigitalImage and write each RGB component to the file. 
        for(int row = 0; row < image.getHeight(); row++) {
            for(int column = 0; column < image.getWidth(); column ++) {
                for(int i :image.getPixel(row, column)) {
                    fWriter.write(i + " ");
                }
            }
            fWriter.write('\n');
        }
        fWriter.close();
    }
}
