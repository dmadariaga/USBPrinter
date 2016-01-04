package usbprinter;

import java.io.InputStream;

/**
 * Created by diego on 11-11-15.
 */
public class PSPrinter extends USBPrinter{
    private int pageSize;

    public PSPrinter(InputStream is) {
        super(is);
    }

    @Override
    public byte[] getPrintData(){
        messageData = new byte[translator.getPCLSize()*2]; //GETPSSIZE
        addUEL("POSTSCRIPT");
        addText("%!PS-Adobe-3.0\r");
        addPosition();
        translator.addPSImage(this);
        addText("showpage\r");
        addText("\004");
        return messageData;
    }

    /**
     * Add to the printer data stream the bytes corresponding to the PCL command for
     * setting the position where the image will be placed on the page.
     */
    @Override
    public void addPosition(){
        addText(xPosition + " " + yPosition + " translate\r");
    }
}
