package bmpinterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import usbprinter.PCLPrinter;
import usbprinter.PSPrinter;

/**
 * Created by diego on 09-09-15.
 */
public class BmpTranslator implements Translator{

    private int start ;
    private int width;
    protected int height; //protected
    private int bitsPixel;
    private int size;
    private byte[] bmpData;


    public BmpTranslator(byte[] fileData){
        bmpData = fileData;
        start = BytesAdapter.toInt(fileData, 10, 4);//bmpData[10];
        width =  BytesAdapter.toInt(fileData, 18, 4);//bmpData[18];
        height =  BytesAdapter.toInt(fileData, 22, 4);//bmpData[22];
        bitsPixel =  BytesAdapter.toInt(fileData, 28, 2);
        size = 120 + height*((width+1+7)*3);
    }

    public void addPCLImage(PCLPrinter printer){

        int mod = (4 - (width*3)%4) %4;
        int realWeight = width*3 + mod;

        for (int i = start + realWeight*height-1 ; i > start; i -= realWeight){
            printer.addESC();
            printer.addText("*b" + realWeight + "W");
            for (int j = realWeight-1; j>= mod   ; j-=3) {
                for (int k = 2; k>=0; k--){
                    printer.add(bmpData[i - j + k]);
                }
                //add(bmpData[i-j]); //~ if the bitmap is "one bit per pixel"
            }
            if (mod>0) {
                for (int j = mod - 1; j >= 0; j--) { //FIX THIS
                    printer.add(bmpData[i-j]);
                }
            }
        }
    }

    @Override
    public void addPSImage(PSPrinter printer) {
        return;
    }

    public int getPCLSize(){
        return size;
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }


}
