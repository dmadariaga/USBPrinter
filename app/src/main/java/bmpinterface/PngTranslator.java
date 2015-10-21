package bmpinterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.Inflater;

import usbprinter.PCLPrinter;

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
        if (colorType==3)    savePalette();
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

        decompresser.setInput(compressedData,0,totalLength);
        try {
            int resultLength = decompresser.inflate(data);
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


    public void createPixels(){
        int pixCursor = 0;
        int unfilterWidth = 0;
        switch (colorType){
            case 3: unfilterWidth = pixelsWidth;
                break;
            case 2: unfilterWidth = pixelsWidth*3;
                break;
            case 6: unfilterWidth = pixelsWidth*4;
                break;
        }
        Unfilter unfilter = new Unfilter(unfilterWidth,3, data);//Depende del bitdepth
        decompressedData = new BytesAdapter(data);
        pixels = new byte[decompressedData.length()*6];
        for (int i=0; i<decompressedData.length(); i++){
            if (i%(unfilterWidth+1)==0) {   //DEPENDE DEL BITDEPTH
                continue;
            }
            //if (bitDepth==16){
             //   pixels[pixCursor]= decompressedData.getByte(i);
              //  pixCursor++;
            //}

            for (int j=0; j<(8/bitDepth);j++){
                pixels[pixCursor++] = decompressedData.getByteFromBits(bitDepth, i, j);
            }
        }
    }

    public void addImage(PCLPrinter printer){   //subir instrucciones genericas un nivel (agregar getWidth y getHeight)
        printer.addESC();
        printer.addText("*r0f" + pixelsWidth + "s" + pixelsHeight + "T");
        printer.addESC();
        printer.addText("*t" + pixelsWidth * (9.6/2) + "h" + pixelsHeight * (9.6/2) + "V");
        printer.addESC();
        printer.addText("*r3A");
        printer.addESC();
        printer.addText("*b0M"); //Mode: unecode
        if (colorType==3)    addWithPalette(printer);
        else    addWithoutPalette(printer);
    }

    public void addWithPalette(PCLPrinter printer){ //tuplas RGB (RGBA?)
        int pixCursor = 0;
        int mod = (pixelsWidth)%4;
        for (int i=0; i<pixelsHeight ; i++){
            printer.addESC();
            printer.addText("*b" + (dataWidth + mod)+ "W");
            for (int j = 0; j<pixelsWidth; j++ ) {
                if ((pixels[pixCursor]&0xFF) >= palette.length) printer.add(palette[0]);
                else printer.add(palette[pixels[pixCursor]&0xFF]); //OUT OF RANGE
                pixCursor++;
            }
            if (mod>0) {
                for (int j = mod - 1; j >= 0; j--) {
                    printer.add(0);
                }
            }

        }
    }

    public void addWithoutPalette(PCLPrinter printer){
        int pixCursor = 0;
        int mod = (pixelsWidth*3)%4;
        for (int i=0; i<pixelsHeight ; i++){
            printer.addESC();
            printer.addText("*b" + (dataWidth +mod) + "W");
            for (int j = 0; j<pixelsWidth; j++ ) {
                printer.add(pixels[pixCursor++]);   //R
                printer.add(pixels[pixCursor++]);   //G
                printer.add(pixels[pixCursor++]);   //B
            }

        }
    }

    public int getSize(){
        return 120 + pixelsHeight*((pixelsWidth+1+7)*3);
    }
}
