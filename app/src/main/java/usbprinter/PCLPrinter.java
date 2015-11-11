package usbprinter;

import java.io.IOException;
import java.io.InputStream;

import bmpinterface.BmpTranslator;
import bmpinterface.PngTranslator;
import bmpinterface.Translator;

public class PCLPrinter implements UsbPrinter {
    private Translator translator;
    private byte[] fileData;
    private byte[] messageData;
    private int cursor;

    public PCLPrinter(InputStream is) {
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

    public byte[] printFile() {

        messageData = new byte[translator.getPCLSize()];
        addUEL("PCL");
        resetPrinter();
        addUnitOfMeasure(600);  //Unit-of-Measure (600 PCL units per inch)
        selectFormat("2");
        addESC();
        addText("*p0P");    //Push (save) palette from the pallete stack
        setPosition(0,0);  //Postion X,Y in PCL units (units of measure)
        int[] dataImage = {2, 3, 0, 8, 8, 8};
        configureImageData(6, dataImage);
        addDotsPerInch(75);
        addImageFile();
        addESC();
        addText("*rC"); //End raster graphics
        addESC();
        addText("*p1P");    //Pop (restore) palette from the pallete stack
        resetPrinter();
        return messageData;

    }

    public void addImageFile(){
        addESC();
        addText("*r0f" + translator.getWidth() + "s" + translator.getHeight() + "T");
        addESC();
        addText("*t" + translator.getWidth() * (9.6 / 2) + "h" + translator.getHeight()*(9.6/2) + "V");
        addESC();
        addText("*r3A");
        addESC();
        addText("*b0M"); //Mode: unecode
        translator.addPCLImage(this);
    }
    public void add(byte b) {
        messageData[cursor] = b;
        cursor++;
    }


    public void add(byte[] b){
        for (int i=0; i< b.length; i++){
            add(b[i]);
        }
    }

    public void add(double d) { add((byte) (d+0.5)); }

    public void add(int i) {
        add((byte) i);
    }

    public void addText(String text){
        byte[] byteText = text.getBytes();
        for (int i=0; i<byteText.length; i++){
            add(byteText[i]);
        }
    }

    public void addESC(){
        add(27);  // <ESC> ascii
    }


    public void setPosition(int x, int y){ //byte size!
        int[] pos = {x, y};
        String[] axis = {"X","Y"};

        for (int i=0; i<2; i++){
            addESC();
            addText("*p" + pos[i] + axis[i]);
        }
    }

    public void selectFormat(String f){
        addESC();
        add(38);  // & ascii
        add(108);  // l ascii
        addText(f);   // format
        add(65);  // A ascii
    }

    public void addUnitOfMeasure(int u){
        addESC();
        addText("&u" + u + "D");
    }

    public void configureImageData(int l, int[]data){
        addESC();
        addText("*v"+l+"W");
        for (int i = 0; i<l; i++){
            add(data[i]);
        }
    }

    public void addDotsPerInch(int i){
        addESC();
        addText("*t" + i + "R");
    }

    public void addUEL(String language){ //Universal Exit Language, control to PJL
        addESC();
        addText("%-12345X");
        addText("@PJL Enter Language = " + language);
        add(0x0D);
        add(0x0A);
    }
    public void resetPrinter(){
        addESC();
        add(69);  // E ascii
    }

    public Translator getTranslator(){
        return translator;
    }

}
