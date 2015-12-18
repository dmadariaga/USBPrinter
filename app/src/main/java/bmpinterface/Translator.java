package bmpinterface;

import usbprinter.PCLPrinter;
import usbprinter.PSPrinter;
import usbprinter.USBPrinter;

/**
 * Created by diego on 25-09-15.
 */
public interface Translator {
    public void addPCLImage(PCLPrinter printer);
    public void addPSImage(PSPrinter printer);
    public int getPCLSize();
    public int getWidth();
    public int getHeight();
}
