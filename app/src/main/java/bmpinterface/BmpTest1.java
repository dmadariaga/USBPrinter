package bmpinterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import usbprinter.PCLPrinter;
import usbprinter.PSPrinter;

/**
 * Created by diego on 11-09-15.
 */
public class BmpTest1 {
    public static void main(String[] argv){
        String path = "app/images/boat.png";
        File f = new File(path);
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(f);
        }
        catch(IOException e){
            e.printStackTrace();
        }

        PSPrinter printer = new PSPrinter(fis);

        File file = new File("app/images/out.txt");
        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(file);
            fos.write(printer.print());
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
