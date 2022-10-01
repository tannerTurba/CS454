import java.awt.*;


//pixel is  byte[] use width and height to adjust index. Byte contains index into color palette. Color palette colors are int[]s
public class IndexedDigitalImage implements DigitalImage {

    private int width, height;
    private int PALETTESIZE = 256;
    private byte[] pixels;
    private Color[] palette = new Color[PALETTESIZE];

    public IndexedDigitalImage(int width, int height) {
        this.width = width;
        this.height = height;
        pixels = new byte[width * height];
    }

    public IndexedDigitalImage(int width, int height, Color[] palette) {
        this.width = width;
        this.height = height;
        pixels = new byte[width * height];
        System.arraycopy(palette, 0, this.palette, 0, palette.length);
    }

    private void setPaletteColor(int paletteIndex, Color color) {
        palette[paletteIndex] = color;
    }

    private Color getPaletteColor(int paletteIndex) {
        return palette[paletteIndex];
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getBands() {
        return 0;
    }

    @Override
    public int[] getPixel(int x, int y) {
        //Get the palette index and then color
        int paletteIndex = Byte.toUnsignedInt(pixels[rasterIndex(x, y)]);
        Color pixelColor = getPaletteColor(paletteIndex);

        //Return the three components put in an array. 
        int[] pixel = {pixelColor.getRed(), pixelColor.getGreen(), pixelColor.getBlue()};
        return pixel;
    }

    @Override
    public void setPixel(int x, int y, int[] pixel) {
        //Copy pixel and create a color object from each channel.
        int[] pixelCopy = new int[3];
        System.arraycopy(pixel, 0, pixelCopy, 0, pixel.length);
        Color color = new Color(pixelCopy[0], pixelCopy[1], pixelCopy[2]);
        
        //Add color to the palette and then set the btye in pixels to the new palette index.
        int paletteIndex = addColorToPalette(color);
        pixels[rasterIndex(x, y)] = (byte)paletteIndex;
    }

    @Override
    public int getSample(int x, int y, int band) {
        //Get the pallete index and color. 
        int paletteIndex = Byte.toUnsignedInt(pixels[rasterIndex(x, y)]);
        Color pixelColor = palette[paletteIndex];

        //Get the specified band.
        switch( band ) {
            case 0: return pixelColor.getRed();
            case 1: return pixelColor.getGreen();
            case 2: return pixelColor.getBlue();
            default: return -1;
        }
    }

    @Override
    public void setSample(int x, int y, int band, int sample) {
        Color oldColor = palette[ pixels[ rasterIndex( x, y ) ] ];
        Color newColor = new Color(255, 255, 255);
        
        //Create the new color depeinding on the band that is passed as a parameter.
        switch( band ) {
            case 0: newColor = new Color( sample, oldColor.getGreen(), oldColor.getBlue() ); break;
            case 1: newColor = new Color( oldColor.getRed(), sample, oldColor.getBlue() ); break;
            case 2: newColor = new Color( oldColor.getRed(), oldColor.getGreen(), sample); break;
        }

        //Add that color to the palette
        int paletteIndex = addColorToPalette(newColor);

        //Set the byte at the (x,y) coordinate to the palette index of the new color.
        pixels[rasterIndex(x, y)] = (byte)paletteIndex;
    }

    private int rasterIndex( int x, int y ) {
        return y * width + x;
    }

    private int addColorToPalette(Color color) {
        //Find an open index in the palette for the new color.
        int paletteIndex = findOpenPaletteIndex();
        if(paletteIndex != -1) {
            setPaletteColor(paletteIndex, color);
        }
        //Otherwise search the palette for the color closest to new color.
        else {
            paletteIndex = findSimilarColor(color);
            setPaletteColor(paletteIndex, color);
        }
        return paletteIndex;
    }

    private int findSimilarColor(Color color) {
        double bestL1 = 10.0;
        int index = -1;

        //Loop through the palette and find the index of the most similar color using the L1 formula. 
        for (int i = 0; i < PALETTESIZE; i++) {
            if(palette[i] != null) {
                double l1 = Math.sqrt(Math.pow((color.getRed() - palette[i].getRed()), 2) + Math.pow((color.getGreen() - palette[i].getGreen()), 2) + Math.pow((color.getBlue() - palette[i].getBlue()), 2)) / 256;
                if(l1 < bestL1) {
                    bestL1 = l1;
                    index = i;
                }
            }
        }
        return index;
    }

    private int findOpenPaletteIndex() {
        //Loop through the palette and return the index of the first open space.
        for(int i = 0; i < PALETTESIZE; i++) {
            if(palette[i] == null) {
                return i;
            }
        }
        return -1;
    }
}
