package bmpinterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by diego on 30-09-15.
 */
public class PngTest1 {
    public static void main(String[] argv) {
        String path = "app/images/boat.png";
        File f = new File(path);
        byte[] fileData = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
        }
        catch(IOException e){
            e.printStackTrace();
        }
        try {
            int len = fis.available();
            fileData = new byte[len];
            fis.read(fileData);
        }
        catch (IOException e){
            e.printStackTrace();
        }

        PngTranslator p = new PngTranslator(fileData);

        File file = new File("app/images/compressout.txt");
        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(file);
            fos.write(p.decompressedData.getData());
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
