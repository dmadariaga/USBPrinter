package bmpinterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import usbprinter.PCLPrinter;

/**
 * Created by diego on 11-09-15.
 */
public class BmpTest1 {
    public static void main(String[] argv){
        String path = "app/images/voto.bmp";
        File f = new File(path);
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(f);
        }
        catch(IOException e){
            e.printStackTrace();
        }

        PCLPrinter printer = new PCLPrinter(fis);

        File file = new File("app/images/outbmp.txt");
        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(file);
            fos.write(printer.printBmp());
        }
        catch (IOException e){
            e.printStackTrace();
        }
        finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

    }
}
