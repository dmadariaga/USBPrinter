package bmpinterface;
import junit.framework.TestCase;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import usbprinter.PCLPrinter;


public class BmpTest{
    File f;
    InputStream fis = null;
    byte[] data;
    PCLPrinter printer;
    byte[] a = {(byte)0xA0, (byte)0x14, (byte)0x00, (byte)0x00 };
    BytesAdapter adapter;

    @Before
    public void setUp(){
        String path = "src/test/java/bmpinterface/tiger.bmp";
        f = new File(path);
        fis = null;
        adapter = new BytesAdapter(a);
        try{
            fis = new FileInputStream(f);
            printer = new PCLPrinter(fis);
            //data = printer.printBmp();

        }
        catch(IOException e){
            fail();
        }
    }


    @Test//big endian
    public void bigEndianIntTest(){
        assertTrue(160 == BytesAdapter.toInt(a,0,1));
        assertTrue(5280 == BytesAdapter.toInt(a,0,2));
        assertTrue(20 == BytesAdapter.toInt(a,1,1));
        assertTrue(0 == BytesAdapter.toInt(a,3,1));
        assertTrue(5280 == BytesAdapter.toInt(a,0,4));
    }

    @Test
    public void bitsToBytesTest(){
        assertTrue(10 == adapter.getByteFromBits(4, 0, 0));
        assertTrue(0 == adapter.getByteFromBits(4, 0, 1));
        assertTrue(0x14 == adapter.getByteFromBits(8, 1, 0));
        assertTrue(4 == adapter.getByteFromBits(4, 1, 1));
    }

    @Test
    public void readMeasuresTest(){
       //assertTrue(printer.getTranslator().getSize()==236280);
    }

    @After
    public void tearDown(){
        try {
            if (fis != null) {
                fis.close();
            }
        }
        catch (IOException e){
            fail();
        }
    }

}

