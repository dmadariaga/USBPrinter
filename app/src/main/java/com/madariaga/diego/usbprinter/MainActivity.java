package com.madariaga.diego.usbprinter;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import bmpinterface.PngTranslator;
import usbprinter.PCLPrinter;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsbReceiver =  new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                TextView tv = (TextView) findViewById(R.id.hello);
                if (ACTION_USB_PERMISSION.equals(action)) {
                    synchronized (this) {
                        UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                        if(device != null){
                            tv.setText("NONULL");
                        }
                        else {
                            Log.d(TAG, "permission denied for device " + device);
                            tv.setText("NULL");
                        }
                    }
                }
                else{
                    tv.setText("NO");
                }


            }
        };

        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, new IntentFilter(ACTION_USB_PERMISSION));
        registerReceiver(mUsbReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
        Intent mIntent = new Intent();
        mIntent.setAction(ACTION_USB_PERMISSION);
        mUsbReceiver.onReceive(this,mIntent);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private UsbManager manager;

    private static final String TAG = "USB";
    private static final String ACTION_USB_PERMISSION =
            "com.madariaga.diego.usbprinter.USB_PERMISSION";
    private static BroadcastReceiver mUsbReceiver = null;
    private View myView;


    public void onClicklScreen(View view){
        myView = this.findViewById(R.id.main);
        myView.setDrawingCacheEnabled(true);
        Bitmap b = myView.getDrawingCache();
        File sd = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);


        File f = new File(sd, "capture.png");
        TextView tv = (TextView) findViewById(R.id.hello);
        try {
            if (sd.canWrite()) {
                tv.setText("Can write");
                f.createNewFile();
                OutputStream os = new FileOutputStream(f);
                b.compress(Bitmap.CompressFormat.PNG, 90, os);
                os.close();
            }
            else{
                tv.setText("Can't write");
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        myView.setDrawingCacheEnabled(false);
    }
    public void onClickCheck(View view){
        // Intent mIntent = new Intent();
        //mIntent.setAction(ACTION_USB_PERMISSION);
        //mUsbReceiver.onReceive(this,mIntent);

        HashMap<String, UsbDevice> usbDevices = manager.getDeviceList();
        String[] array = usbDevices.keySet().toArray(new String[usbDevices.keySet().size()]);

        Arrays.sort(array);

        //ArrayAdapter<String> adaptor = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item, array);
        //mListUsbAndroid.setAdapter(adaptor);
        TextView tv = (TextView) findViewById(R.id.hello);
        tv.setText("Device List (" + usbDevices.size() + "):");

        Iterator<UsbDevice> deviceIterator = usbDevices.values().iterator();
        while(deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            tv.setText(tv.getText() +" "+ device.getVendorId()+" "+device.getProductId()+" "+device.getDeviceId());

            Resources resources = this.getResources();
            InputStream is = resources.openRawResource(R.raw.lena);

            PCLPrinter printer = new PCLPrinter(is);
            byte[] bytes = printer.print();
            int TIMEOUT = 10000;
            boolean forceClaim = true;

            UsbInterface intf = device.getInterface(0);
            UsbEndpoint endpoint = intf.getEndpoint(0);
            UsbDeviceConnection connection = manager.openDevice(device);
            connection.claimInterface(intf, forceClaim);
            //connection.bulkTransfer(endpoint, bytes, bytes.length, TIMEOUT);
            int bytesToSend = bytes.length;
            int offset = 0;
            while (bytesToSend>=15000){
                connection.bulkTransfer(endpoint, bytes, offset,15000, TIMEOUT);
                offset += 15000;
                bytesToSend -= 15000;
            }

            connection.bulkTransfer(endpoint, bytes, offset,bytesToSend, TIMEOUT);

            tv.setText(tv.getText()+" printing...");
        }
    }
}
