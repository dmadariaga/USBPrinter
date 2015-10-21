package bmpinterface;

import usbprinter.PCLPrinter;

/**
 * Created by diego on 25-09-15.
 */
public interface Translator {
    public void addImage(PCLPrinter printer);
    public int getSize();
}
