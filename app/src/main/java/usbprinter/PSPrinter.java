package usbprinter;

import java.io.IOException;
import java.io.InputStream;

import bmpinterface.PngTranslator;
import bmpinterface.Translator;

/**
 * Created by diego on 11-11-15.
 */
public class PSPrinter implements UsbPrinter{
    private Translator translator;
    private byte[] fileData;
    private byte[] messageData;
    private int cursor;

    public PSPrinter(InputStream is) {
        try {
            int len = is.available();
            fileData = new byte[len];
            is.read(fileData);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        cursor = 0;
    }

    public byte[] print(){
        translator = new PngTranslator(fileData);
        return printFile();
    }

    public byte[] printFile(){
        messageData = new byte[translator.getPCLSize()]; //GETPSSIZE
        addText("%!");
        translator.addPSImage(this);
        return messageData;
    }


    public void add(byte b) {
        messageData[cursor] = b;
        cursor++;
    }

    public void addText(String text){
        byte[] byteText = text.getBytes();
        for (int i=0; i<byteText.length; i++){
            add(byteText[i]);
        }
    }
}
