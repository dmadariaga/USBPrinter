para imprimir una captura de pantalla dentro de la aplicación (hacia una impresora PCL conectada)

        myView = this.findViewById(R.id.main);
        myView.setDrawingCacheEnabled(true);
        Bitmap b = myView.getDrawingCache();

        String FILENAME = "tempScreen";
        try {
            OutputStream os = openFileOutput(FILENAME, Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.PNG, 90, os);
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        HashMap<String, UsbDevice> usbDevices = manager.getDeviceList();
        String[] array = usbDevices.keySet().toArray(new String[usbDevices.keySet().size()]);

        Arrays.sort(array);

	Iterator<UsbDevice> deviceIterator = usbDevices.values().iterator();
        while(deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();

            InputStream is = null;
            try {
                is = openFileInput(FILENAME);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            PCLPrinter printer = new PCLPrinter(is);
            byte[] bytes = printer.print();
            int TIMEOUT = 10000;
            boolean forceClaim = true;

            UsbInterface intf = device.getInterface(0);
            UsbEndpoint endpoint = intf.getEndpoint(0);
            UsbDeviceConnection connection = manager.openDevice(device);
            connection.claimInterface(intf, forceClaim);

            int bytesToSend = bytes.length;
            int offset = 0;
            while (bytesToSend >= 15000) {
                connection.bulkTransfer(endpoint, bytes, offset, 15000, TIMEOUT);
                offset += 15000;
                bytesToSend -= 15000;
            }
	    connection.bulkTransfer(endpoint, bytes, offset, bytesToSend, TIMEOUT);

            deleteFile(FILENAME);
        }

