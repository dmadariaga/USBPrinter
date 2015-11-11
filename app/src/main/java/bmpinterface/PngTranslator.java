package bmpinterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.Inflater;

import usbprinter.PCLPrinter;
import usbprinter.PSPrinter;

/**
 * Created by diego on 30-09-15.
 */
public class PngTranslator implements Translator {
    private BytesAdapter pngData;
    public BytesAdapter decompressedData;
    private int pixelsWidth;
    private int pixelsHeight;
    private int dataWidth;
    private int bitDepth;   //Para 16 bits dos opciones: transformar a 8 bits (regla de 3) o ver si puedo mandarlo así a la impresora
    private int colorType;
    private int compressionMethod;
    private int filterMethod;
    private int interlaceMethod;
    private byte data[];
    private byte[][] palette;
    private int cursor;
    public byte[] pixels;
    public int[] background = {0xFF, 0xFF, 0xFF};


    public PngTranslator(byte[] fileData){
        //checkSignature
        cursor = 0;
        pngData = new BytesAdapter(fileData);
        int ihdr = pngData.find(8, "IHDR".getBytes());
        pixelsWidth = pngData.bigEndianToInt(ihdr+4,4);
        pixelsHeight = pngData.bigEndianToInt(ihdr+8,4);
        dataWidth = pixelsWidth*3; //+ pixelsWidth%4;  //ancho debe ser multiplo de 4
        bitDepth = pngData.bigEndianToInt(ihdr+12,1);   //Bit depth is a single-byte integer giving the number of bits per sample or per palette index (not per pixel).
                                                        //Valid values are 1, 2, 4, 8, and 16, although not all values are allowed for all color types.

        colorType = pngData.bigEndianToInt(ihdr+13,1);  //Color type is a single-byte integer that describes the interpretation of the image data. Color type codes represent
                                                        //sums of the following values: 1 (palette used), 2 (color used), and 4 (alpha channel used). Valid values are0,2,3,4,and6

        compressionMethod = pngData.bigEndianToInt(ihdr+14,1);
        filterMethod = pngData.bigEndianToInt(ihdr+15,1);
        interlaceMethod = pngData.bigEndianToInt(ihdr+16,1);
        savePalette();
        saveBackground();
        readData();
    }

    public void readData(){
        int totalLength = 0;
        data= new byte[(pixelsWidth*4+1)*pixelsHeight]; // calcular bien
        byte compressedData[] = new byte[data.length]; //cuanto comprime zip??
        Inflater decompresser = new Inflater();
        while((cursor = pngData.find(cursor, "IDAT".getBytes())) != -1) {
            int length = pngData.bigEndianToInt(cursor - 4, 4);
            cursor += 4;
            for (int i=0; i<length; i++){
                compressedData[totalLength + i] = pngData.getByte(cursor);
                cursor++;
            }
            totalLength += length;
        }

        decompresser.setInput(compressedData, 0, totalLength);
        try {
            decompresser.inflate(data);
            decompresser.end();
            createPixels();
        } catch (java.util.zip.DataFormatException e) {
            e.printStackTrace();
        }


    }

    public void savePalette(){
        int plte = pngData.find(cursor, "PLTE".getBytes());
        if (plte>=0){
            int length = pngData.bigEndianToInt(plte-4, 4);
            //if length%3 != 0 paleta mal definida
            palette = new byte[length/3][3];
            for (int i=0; i<length/3; i++){
                palette[i] = (pngData.getData(plte + 3*i+4, 3));
            }
        }
    }

    public void saveBackground(){
        int bkgd = pngData.find(cursor, "bKGD".getBytes());
        if (bkgd>=0){
            background = new int[3];
            for (int i=0; i<3; i++){
                background[i] = pngData.getByte(bkgd + 4 + i);
            }
        }
    }


    public void createPixels(){
        int pixCursor = 0;
        int realPixelsWidth = 0;
        int lengthPixel = 0;
        switch (colorType){
            case 0: realPixelsWidth = pixelsWidth;
                    if (bitDepth == 16) lengthPixel = 2;
                    else    lengthPixel = 1;    // bitDepth in {1,2,4,8}
                break;
            case 2: realPixelsWidth = pixelsWidth*3;
                    if (bitDepth == 16) lengthPixel = 6;
                    else    lengthPixel = 3;    // bitDepth = 8
                break;
            case 3: realPixelsWidth = pixelsWidth;
                    lengthPixel = 1;
                break;
            case 6: realPixelsWidth = pixelsWidth*4;
                    if (bitDepth == 16) lengthPixel = 8;
                    else lengthPixel = 4;
                break;
        }

        int unfilterWidth = (int)((double)realPixelsWidth/ (8.0/bitDepth) +0.5);

        Unfilter unfilter = new Unfilter(unfilterWidth,lengthPixel, data);
        decompressedData = new BytesAdapter(data);
        pixels = new byte[decompressedData.length()*8];
        for (int i=1; i<decompressedData.length(); i++){
            if (i%(unfilterWidth+1)==0) {
                pixCursor -= ((unfilterWidth*8 - bitDepth*realPixelsWidth)/bitDepth);
                continue;
            }
            if (bitDepth==16){
                pixels[pixCursor++]= (byte)( (decompressedData.getUnsignedByte(i)*256.0 + decompressedData.getUnsignedByte(i+1))*0xFF/0xFFFF) ;
                i++;
            }

            for (int j=0; j<(8/bitDepth);j++){
                pixels[pixCursor++] = decompressedData.getByteFromBits(bitDepth, i, j);
            }
        }
    }

    public void addPSImage(PSPrinter printer){
        addGrayScale(printer);
    }

    public void addPCLImage(PCLPrinter printer){

        switch (colorType){ // again
            case 0: addGrayScale(printer);
                break;
            case 2: addRGB(printer);
                break;
            case 3 : addWithPalette(printer);
                break;
            case 6: addRGBA(printer);
                break;
        }

    }

    public void addWithPalette(PCLPrinter printer){
        int pixCursor = 0;
        int mod = (4 - (dataWidth)%4) %4;
        for (int i=0; i<pixelsHeight ; i++){
            printer.addESC();
            printer.addText("*b" + (dataWidth + mod)+ "W");
            for (int j = 0; j<pixelsWidth; j++ ) {
                if ((pixels[pixCursor]&0xFF) >= palette.length){  //OUT OF RANGE
                    printer.add(background[0]);
                    printer.add(background[1]);
                    printer.add(background[2]);
                }
                else printer.add(palette[pixels[pixCursor]&0xFF]);
                pixCursor++;
            }
            fillSpace(mod,printer);
        }
    }

    public void addRGB(PCLPrinter printer){
        int pixCursor = 0;
        int mod = (4 - (dataWidth)%4) %4;
        for (int i=0; i<pixelsHeight ; i++){
            printer.addESC();
            printer.addText("*b" + (dataWidth +mod) + "W");
            for (int j = 0; j<pixelsWidth; j++ ) {
                printer.add(pixels[pixCursor++]);   //R
                printer.add(pixels[pixCursor++]);   //G
                printer.add(pixels[pixCursor++]);   //B
            }
            fillSpace(mod,printer);
        }

    }

    public void addRGBA(PCLPrinter printer){
        int pixCursor = 0;
        int mod = (4 - (dataWidth)%4) %4;
        for (int i=0; i<pixelsHeight ; i++){
            printer.addESC();
            printer.addText("*b" + (dataWidth +mod) + "W");
            for (int j = 0; j<pixelsWidth; j++ ) {
                double r = (pixels[pixCursor++] & 0xFF)/255.0;
                double g = (pixels[pixCursor++] & 0xFF)/255.0;
                double b = (pixels[pixCursor++] & 0xFF)/255.0;
                double a = (pixels[pixCursor++] & 0xFF)/255.0;

                printer.add( (a*r + (1-a)*(background[0]/255.0))*255 );   //R
                printer.add( (a*g + (1-a)*(background[1]/255.0))*255 );   //G
                printer.add( (a*b + (1-a)*(background[2]/255.0))*255 );   //B
            }
            fillSpace(mod,printer);
        }

    }
    public void addGrayScale(PSPrinter printer){
        int width = (int)((double)pixelsWidth/ (8.0/bitDepth) +0.5);
        printer.addText("100 200 translate");
        printer.addText(pixelsWidth +" "+ pixelsHeight + " scale\n");
        printer.addText(pixelsWidth +" "+ pixelsHeight +" "+ bitDepth);
        printer.addText(" ["+ pixelsWidth +" 0 0 -"+pixelsHeight+" 0 "+pixelsHeight+"]\n");
        printer.addText("{<\n");
        for (int i=0; i<pixelsHeight; i++){
            for (int j=0; j<width; j++) {
                printer.addText(("0"+Integer.toHexString(0xFF & decompressedData.getByte( (width+1)*i+1+j ))).substring(1));
            }
            printer.addText("\n");
        }
        printer.addText(">}\n");
        printer.addText("image\n");
        printer.addText("showpage\n");
    }

    public void addGrayScale(PCLPrinter printer){
        int factor = 0;
        switch (bitDepth){
            case 16: factor = 1;    // 0xFF/0xFF since the images was transformed to a 8bit sample
                break;
            case 8: factor = 1;     // 0xFF/0xFF
                break;
            case 4: factor = 17;    // 0xFF/0x0F
                break;
            case 2: factor = 185;   // 0xFF/0x03
                break;
            case 1: factor = 255;   // 0xFF/0x01
                break;
        }

        int pixCursor = 0;
        int mod = (4 - (dataWidth)%4) %4;
        for (int i=0; i<pixelsHeight ; i++) {
            printer.addESC();
            printer.addText("*b" + (dataWidth + mod) + "W");
            for (int j = 0; j < pixelsWidth; j++) {
                printer.add(pixels[factor*pixCursor]);
                printer.add(pixels[factor*pixCursor]);
                printer.add(pixels[factor*pixCursor++]);
            }
            fillSpace(mod,printer);
        }
    }

    public void fillSpace(int mod, PCLPrinter printer){
        for (int i = 0; i <= mod; i++){
            printer.add(0);
        }
    }

    public int getPCLSize(){
        return 120 + pixelsHeight*((pixelsWidth+1+7)*3);
    }

    public int getWidth(){
        return pixelsWidth;
    }
    public int getHeight(){
        return pixelsHeight;
    }
}
