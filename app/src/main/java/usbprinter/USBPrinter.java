package usbprinter;

import java.io.IOException;
import java.io.InputStream;

import bmpinterface.PngTranslator;
import bmpinterface.Translator;

/**
 * Created by diego on 09-09-15.
 */
public abstract class USBPrinter {
    protected Translator translator;
    protected byte[] fileData;
    protected byte[] messageData;
    protected int cursor;
    protected int xPosition;
    protected int yPosition;
    protected double xScale;
    protected double yScale;

    public USBPrinter(InputStream is) {
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

    /**
     * Return a byte array that has the bytes corresponding to printer commands to print
     * the image file that is saved in the "fileData" byte array.
     * @return      Byte data stream with the commands to print the image file.
     */
    public byte[] print(){
        translator = new PngTranslator(fileData);
        return getPrintData();
    }

    /**
     *
     * @return
     */
    public abstract byte[] getPrintData();

    /**
     * Add to the printer data stream the bytes corresponding to the commands for
     * setting the position given where the image will be placed on the page.
     */
    public abstract void addPosition();

    /**
     * Add a specific byte to the printer byte data stream.
     * @param b     Byte to be added.
     */
    public void add(byte b) {
        messageData[cursor] = b;
        cursor++;
    }

    /**
     * Add a set of specifics bytes to the printer byte data stream, by sending
     * them one by one.
     * @param b     Byte array to be added.
     */
    public void add(byte[] b){
        for (int i=0; i< b.length; i++){
            add(b[i]);
        }
    }

    /**
     * Add the byte representation of a decimal number to the printer byte data stream.
     * @param d     Decimal number to be added as its byte representation.
     */
    public void add(double d) { add((byte) (d+0.5)); }

    /**
     * Add the byte representation of an integer number to the printer byte data stream.
     * @param i     Integer number to be added as its byte representation.
     */
    public void add(int i) {
        add((byte) i);
    }

    /**
     * Add a sequence of characters (one by one) that represents a String text
     * to the byte data stream.
     * @param text      The string to be added
     */
    public void addText(String text){
        byte[] byteText = text.getBytes();
        for (int i=0; i<byteText.length; i++){
            add(byteText[i]);
        }
    }

    /**
     * Add the <ESC> ascii character to the byte data stream.
     */
    public void addESC(){
        add(27);  // <ESC> ascii
    }

    /**
     * Add to the byte data stream the characters corresponding to the
     * Universal Exit Language (UEL) and selects a specific language to send the
     * instruction set to the printer.
     * @param language      The Language that will be used to send the instruction set to
     *                      the printer.
     */
    public void addUEL(String language){ //Universal Exit Language, control to PJL
        addESC();
        addText("%-12345X");
        //addText("@PJL Enter Language = " + language);
        //add((byte)0x0D);
        //add((byte)0x0A);
    }

    /**
     * Set the position where the image will be placed on the page.
     * The meaning of this values (orientation in page) may vary with different
     * types of Page Descriptor Languages (like PostScript or PCL)
     * @param x     x coordinate to place the image on the page
     * @param y     y coordinate to place the image on the page
     */
    public void setPosition(int x, int y){
        xPosition = x;
        yPosition = y;
    }

    /**
     * Set the scaling to apply on the image. This scaling is specified with values for
     * width and height scale.
     * @param x     Value to apply width scaling
     * @param y     Value to apply height scaling
     */
    public void setScale(int x, int y){
        xScale = x;
        yScale = y;
    }
}
