package usbprinter;

import java.io.IOException;
import java.io.InputStream;

import bmpinterface.BmpTranslator;
import bmpinterface.PngTranslator;
import bmpinterface.Translator;

public class PCLPrinter extends USBPrinter {
    private int unitOfMeasure;
    private int dotsPerInch;
    private int pageSize;
    private int dataImage[];

    public PCLPrinter(InputStream is) {
        super(is);
        setDefaultValues();
    }

    /**
     * Set the default values for the PCL instructions used.
     * This values are defined in the "PCL 5 Printer LanguageTechnical Reference Manual".
     * (http://h20566.www2.hp.com/hpsc/doc/public/display?docId=emr_na-bpl13210)
     */
    public void setDefaultValues(){
        unitOfMeasure = 300;
        dotsPerInch = 75;
        pageSize = 2;
        xPosition = yPosition = 0;
        xScale = yScale = 0.5;
        int defaultData[] = {2, 3, 0, 8, 8, 8};
        setDataImage(defaultData);
    }

    /**
     * Select the page size to be used. It must be one of the 16 different types
     * offered by the PCL language. View "PCL 5 Printer Language Technical Quick Reference Guide" page 6.
     * (http://h20565.www2.hp.com/hpsc/doc/public/display?docId=emr_na-bpl13205)
     * @param p     Number that represents the page size selected.
     */
    public void setPageSize(int p){
        pageSize = p;       //2 - Letter (8.5" x 11")
                            //3 - Legal (8.5" x 14")
                            //6 - Ledger (11" x 17")
                            //25 - A5 paper (148mm x 210mm)
                            //26 - A4 paper (210mm x 297mm)
                            //27 - A3 (297mm x 420mm)
                            //45 - JIS B5 paper (182mm x 257mm)
                            //46 - JIS B4 paper (250mm x 354mm)
                            //71 - Hagaki postcard (100mm x 148mm)
                            //72 - Oufuku-Hagaki postcard (200mm x 148mm)
                            //80 - Monarch Envelope (3 7/8" x 7 1/2")
                            //81 - Commercial Envelope 10 (4 1/8" x 9 1/2")
                            //90 - International DL (110mm x 220mm)
                            //91 - International C5 (162mm x 229mm)
                            //100 - International B5 (176mm x 250mm)
                            //101 - Custom (size varies with printer)
    }

    /**
     * Select the unit of measure to be used by the printer. It must be one of the values defined by the
     * PCL language. View "PCL 5 Printer Language Technical Quick Reference Guide" page 5.
     * (http://h20565.www2.hp.com/hpsc/doc/public/display?docId=emr_na-bpl13205)
     * @param u     Unit of measure established for the PCL unit.
     */
    public void setUnitOfMeasure(int u){
        unitOfMeasure = u;  //Range = 96, 100, 120, 144, 150, 160, 180, 200, 225, 240, 288, 300, 360,
                            // 400, 450, 480, 600, 720, 800, 900, 1200, 1440, 1800, 2400, 3600, 7200
    }

    /**
     *Select the value of raster graphics resolution tu be used by te raster data operations. It must be one of the
     * values defined by the PCL language. View "PCL 5 Printer Language Technical Quick Reference Guide" page 20.
     * (http://h20565.www2.hp.com/hpsc/doc/public/display?docId=emr_na-bpl13205)
     * @param d     Raster graphics resolution designated for raster data operations.
     */
    public void setRasterGraphicsResolution(int d){
        dotsPerInch = d;    //Range = 75, 100, 150, 200, 300, 600
    }

    /**
     * Set the values to execute the Configure Image Data command (CID) with the information for palette creation and
     * raster data transmission. View "PCL 5 Printer Language Technical Quick Reference Guide" page 22.
     * (http://h20565.www2.hp.com/hpsc/doc/public/display?docId=emr_na-bpl13205)
     *
     * @param data      Array with the values needed to the CID command.
     */
    public void setDataImage(int[] data){
        dataImage = data;
    }

    @Override
    public byte[] getPrintData() {
        messageData = new byte[translator.getPCLSize()];
        //addUEL("PCL");
        resetPrinter();
        addUnitOfMeasure();  //Unit-of-Measure (600 PCL units per inch)
        addPageSize();
        addESC();
        addText("*p0P");    //Push (save) palette from the pallete stack
        addPosition();      //Postion X,Y in PCL units (units of measure)
        addConfigureImageData();
        addDotsPerInch();
        addImageFile();
        addESC();
        addText("*rC");     //End raster graphics
        addESC();
        addText("*p1P");    //Pop (restore) palette from the pallete stack
        resetPrinter();
        return messageData;
    }

    /**
     * Add to the printer data stream the PCL commands to send the pixels of the selected image.
     */
    public void addImageFile(){
        addESC();
        addText("*r0f" + translator.getWidth() + "s" + translator.getHeight() + "T");
        addESC();
        addText("*t" + translator.getWidth() * (9.6 * xScale) + "h" + translator.getHeight() * (9.6 * yScale) + "V");
        addESC();
        addText("*r3A");
        addESC();
        addText("*b0M"); //Mode: unecode
        translator.addPCLImage(this);
    }

    /**
     * Add to the printer data stream the bytes corresponding to the PCL command for
     * setting the position where the image will be placed on the page.
     */
    @Override
    public void addPosition(){ //byte size!
        int[] pos = {xPosition, yPosition};
        String[] axis = {"X","Y"};

        for (int i=0; i<2; i++){
            addESC();
            addText("*p" + pos[i] + axis[i]);
        }
    }

    /**
     * Add to the printer data stream the bytes corresponding to the PCL command for
     * setting the page size to use.
     */
    public void addPageSize(){
        addESC();
        add(38);    // & ascii
        add(108);   // l ascii
        addText(Integer.toString(pageSize)); // format
        add(65);    // A ascii
    }


    /**
     * Add to the printer data stream the bytes corresponding to the PCL command for
     * setting the value to the unit of measure used.
     */
    public void addUnitOfMeasure(){
        addESC();
        addText("&u" + unitOfMeasure + "D");
    }

    /**
     * Add to the printer data stream the bytes corresponding to the Configure Image Data
     * command (CID) with the values designated.
     */
    public void addConfigureImageData(){
        addESC();
        addText("*v"+6+"W");
        for (int i = 0; i<6; i++){
            add(dataImage[i]);
        }
    }

    /**
     * Add to the printer data stream the bytes corresponding to the configuration
     * of the dots per inch's value.
     */
    public void addDotsPerInch(){
        addESC();
        addText("*t" + dotsPerInch + "R");
    }

    /**
     * Add to the printer data stream the bytes corresponding to the RESET command. ( <ESC>E )
     */
    public void resetPrinter(){
        addESC();
        add(69);  // E ascii
    }

    /**
     * Get the current image translator used to print.
     * @return      current image translator.
     */
    public Translator getTranslator(){
        return translator;
    }

}
