package org.pololu.maestro;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;
import com.felhr.*;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.felhr.utils.HexData;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * Primary class for controlling servos via USB.
 *
 * Android requires us to get user permission to access a USB device. The best
 * approach is to do that in the Activity and then pass the device into the
 * overloaded constructor.
 *
 * @See MaestroSSCActivity.java
 *
 * @author Patrick H. Ryan
 *
 */
public class MaestroUSBDevice implements Serializable {
    private static final String TAG = "MaestroUSBDevice";
    private UsbDevice device;
    private UsbDeviceConnection controlConnection;
    private UsbSerialDevice serial;

    // Pololu Protocol Commands
    public static final int CMD_SET_POSITION = 0x85;
    public static final int CMD_SET_SPEED = 0x87;
    public static final int CMD_SET_ACCELERATION = 0x89;
    public static final int CMD_SET_PWM = 0x8A;
    public static final int CMD_GET_POSITION = 0x90;
    public static final int CMD_GET_MOVING_STATE = 0x93;
    public static final int CMD_GET_ERRORS = 0xA1;
    public static final int CMD_GO_HOME = 0xA2;

    public MaestroUSBDevice() {
    }

    public void explicitSend(byte[] a) {
        //serial.write(a);
        if(a != null && serial != null) {
            Log.d("MAESTRO: SEND: ", HexData.hexToString(a));
            //serial.syncWrite(a, 5000);
            //serial.syncRead(buffer, 5000);
            serial.write(a);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Log.d("MAESTRO: ", String.valueOf(i));
        }

    }

    public void sendCommand(int command, int channel, int value) {
        Log.d(TAG, "sendCommand(" + command + ", " + channel + ", " + value + ")");

        if (device == null) {
            Log.e(TAG, "sendCommand(): device is NULL!");
            return;
        }

        controlConnection.controlTransfer(0x40, command, value, channel, null, 0, 5000);
    }

    public int readInt() {
        Log.d(TAG, "readInt()");

        if (device == null) {
            Log.e(TAG, "readInt(): device is NULL!");
            return 0;
        }

        // TODO Add code to read from device

        return 0;
    }
    UsbEndpoint endpoint;
    public void setDevice(UsbManager usbManager, UsbDevice usbDevice) {
//		Log.d(TAG, "setDevice " + usbDevice);

        device = usbDevice;

        if (device != null) {
            usbDeviceDump(usbDevice);

            UsbInterface controlInterface = device.getInterface(1);

            endpoint = controlInterface.getEndpoint(0);

            UsbDeviceConnection connection = usbManager.openDevice(device);

            if (connection != null && connection.claimInterface(controlInterface, true)) {
                controlConnection = connection;
                serial = UsbSerialDevice.createUsbSerialDevice(device, controlConnection);
                boolean t = serial.open();
                Log.d("Set DEVICE : ", "" + t);
                serial.setBaudRate(9600);
                serial.setParity(UsbSerialInterface.PARITY_NONE);
                serial.setStopBits(UsbSerialInterface.STOP_BITS_1);
                serial.setDataBits(UsbSerialInterface.DATA_BITS_8);
            } else {
                Log.e(TAG, "open connection FAIL");
                controlConnection = null;
            }
        }
    }

    public void usbDeviceDump(UsbDevice usbDevice) {
//		Log.d(TAG, "usbDeviceDump " + usbDevice);

        if (usbDevice == null) {
            Log.e(TAG, "usbDevice is NULL!");
            return;
        }

        int numInterfaces = usbDevice.getInterfaceCount();

        // Log.d(TAG, numInterfaces + " interfaces");
        Log.d(TAG, "Device found: VendorId=" + usbDevice.getVendorId() + " ProductId=" + usbDevice.getProductId());

        for (int i = 0; i < numInterfaces; i++) {
            UsbInterface usbInterface = usbDevice.getInterface(i);
//			Log.d(TAG, "\tInterface(" + i + "): class=" + usbInterface.getInterfaceClass() + " protocol=" + usbInterface.getInterfaceProtocol());

            int numEndpoints = usbInterface.getEndpointCount();
//			Log.d(TAG, numEndpoints + " endpoints");

            for (int j = 0; j < numEndpoints; j++) {
                UsbEndpoint usbEndpoint = usbInterface.getEndpoint(j);
//				Log.d(TAG, "\t\tEndpoint(" + j + ")");
            }
        }
    }
}