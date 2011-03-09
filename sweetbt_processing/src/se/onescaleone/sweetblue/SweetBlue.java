/**
 * you can put a one sentence description of your library here.
 *
 * ##copyright##
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 * 
 * @author		##author##
 * @modified	##date##
 * @version		##version##
 */

package se.onescaleone.sweetblue;

import java.util.HashMap;

import processing.core.PApplet;
import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * This is a template class and can be used to start a new processing library or
 * tool. Make sure you rename this class as well as the name of the example
 * package 'template' to your own lobrary or tool naming convention.
 * 
 * @example Hello
 * 
 *          (the tag @example followed by the name of an example included in
 *          folder 'examples' will automatically include the example in the
 *          javadoc.)
 * 
 */

public class SweetBlue {

	/*
	 * This is NOT NEEDED, just use the handler!!! // Create the listener list
	 * protected EventListenerList listenerList = new EventListenerList();
	 * 
	 * // This methods allows classes to register for MyEvents public void
	 * addMyEventListener(ArduinoEventListener listener) {
	 * listenerList.add(ArduinoEventListener.class, listener); }
	 * 
	 * // This methods allows classes to unregister for MyEvents public void
	 * removeMyEventListener(ArduinoEventListener listener) {
	 * listenerList.remove(ArduinoEventListener.class, listener); }
	 */

	// myParent is a reference to the parent sketch
	PApplet myParent;

	public final static String VERSION = "##version##";

	/* Contains threads to control the communication */
	private BluetoothChatService mChatService = null;
	private static boolean currentlySendingData = false;

	/* Debug variables */
	public static boolean DEBUG = false;
	public static String DEBUGTAG = "##name## ##version## Debug message: ";

	private int state = -1;
	public static final int STATE_CONNECTED = 18;
	public static final int STATE_DISCONNECTED = 28;

	/* Link to the applications main handler */
	private Handler mainHandler;
	private Handler recieveHandler;

	/* Bluetooth constants, handler messages */
	public static final int MESSAGE_STATE_CHANGE = 19;
	public static final int MESSAGE_READ = 29;
	public static final int MESSAGE_WRITE = 39;
	public static final int MESSAGE_DEVICE_NAME = 49;
	public static final int MESSAGE_TOAST = 59;
	public static final int MESSATE_TEST_VIBRATOR = 69;
	// public static final int MESSAGE_ECHO = 79;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String DATA_STRING = "data_string";
	public static final String DATA_VALUE = "data_value";
	public static final String TOAST = "toast";

	/* Arduino Constants */
	public static final int HIGH = 1;
	public static final int LOW = 0;
	public static final int INPUT = 6;
	public static final int OUTPUT = 7;

	/* Map containing all pins and their read-values */
	private HashMap<Integer, Integer> values;

	/**
	 * a Constructor, usually called in the setup() method in your sketch to
	 * initialize and start the library.
	 * 
	 * @example Hello
	 * @param theParent
	 */
	public SweetBlue(PApplet theParent) {
		myParent = theParent;
		welcome();

		/* Init hashmap */
		values = new HashMap<Integer, Integer>();
	}

	/**
	 * Tries to connect to the supplied MAC address.
	 * 
	 * @param mac
	 */
	public void connect(final String mac) {
		/* Make sure the chatservice isn't connected */
		if (mChatService != null) {
			mChatService.stop();
		}

		if (mac != null) {

			mainHandler = new Handler(Looper.getMainLooper());
			mainHandler.post(new Runnable() {

				@Override
				public void run() {

					// init the handler (recieve messages from bt thread)
					if (recieveHandler == null)
						recieveHandler = new Handler() {

							@Override
							public void handleMessage(Message msg) {

								switch (msg.what) {
								case MESSAGE_STATE_CHANGE:

									switch (msg.arg1) {
									case BluetoothChatService.STATE_CONNECTED:
										state = STATE_CONNECTED;
										break;
									case BluetoothChatService.STATE_CONNECTING:
										state = STATE_DISCONNECTED;
										break;
									case BluetoothChatService.STATE_LISTEN:
										state = STATE_DISCONNECTED;
										break;
									case BluetoothChatService.STATE_NONE:
										state = STATE_DISCONNECTED;
										break;
									}
									break;
								case MESSAGE_DEVICE_NAME:
									// Print the connected device name to PDE
									myParent.println(msg.getData().getString(
											DEVICE_NAME)
											+ " connected.");
									break;
								case MESSAGE_READ:
									// Read from the output stream... byte[]
									int[] data = msg.getData().getIntArray(
											DATA_VALUE);

									/* Add the value to the hashmap */
									if (data != null)
										values.put(data[0], data[1]);

									if (SweetBlue.DEBUG) {
										/* Print the read data array */
										StringBuffer sb = new StringBuffer();
										for (int i = 0; i < data.length; i++)
											sb.append(data[i]).append(",");
										Log.i("System.out", SweetBlue.DEBUGTAG
												+ sb.toString());
									}
									break;
								/*
								 * This shouldn't be needed, we're using the
								 * "System.out" instead case MESSAGE_ECHO: //
								 * Read from the output stream... string String
								 * echo = msg.getData().getString( DATA_STRING);
								 * myParent.println("==== START ECHO ====");
								 * myParent.println(echo);
								 * myParent.println("==== END ECHO ====");
								 * break;
								 */
								}
							}

						};
					// init the chatservice
					if (mChatService == null && recieveHandler != null)
						mChatService = new BluetoothChatService(recieveHandler);

					// Connect the chatservice
					mChatService.connect(BluetoothAdapter.getDefaultAdapter()
							.getRemoteDevice(mac));

					// Add the listener
				}
			});

		}
	}

	public boolean isConnected() {
		if (mChatService != null
				&& mChatService.getState() == BluetoothChatService.STATE_CONNECTED)
			return true;
		else
			return false;
	}

	private void welcome() {
		System.out.println("##name## ##version## by ##author##");
	}

	/**
	 * return the version of the library.
	 * 
	 * @return String
	 */
	public static String version() {
		return VERSION;
	}

	/**
	 * DEPRECATED
	 * 
	 * Tries to write data to the bluetooth port.
	 * 
	 * byte[]
	 * 
	 * @param data
	 */
	public byte[] write(byte value) {
		if (!this.isCurrentlySendingData()) {

			byte[] data = new byte[1];
			data[0] = value;

			mChatService.write(attachHeaderBytes(data));

			return data;
		}

		return null;
	}

	/**
	 * DEPRECATED
	 * 
	 * Tries to write data to the bluetooth port.
	 * 
	 * byte[]
	 * 
	 * @param data
	 */
	public byte[] write(byte[] value) {
		if (!this.isCurrentlySendingData()) {
			mChatService.write(attachHeaderBytes(value));

			return value;
		}

		return null;
	}

	/**
	 * DEPRECATED
	 * 
	 * Tries to write data to the bluetooth port.
	 * 
	 * int
	 * 
	 * @param data
	 */
	@Deprecated
	public byte[] write(int value) {
		if (!this.isCurrentlySendingData()) {
			byte[] data = new byte[4];

			for (int i = 0; i < 4; i++) {
				int offset = (data.length - 1 - i) * 8;
				data[i] = (byte) ((value >>> offset) & 0xFF);
			}

			mChatService.write(attachHeaderBytes(data));

			return data;
		}
		return null;
	}

	/**
	 * 
	 * @return
	 */
	public static boolean isCurrentlySendingData() {
		return SweetBlue.currentlySendingData;
	}

	/**
	 * Function that allows BluetoothWorkers to "steal" the attention for the
	 * chatservice. Returns true to the worker if the variable was succesfully
	 * set. Returns false if the chatservice is busy.
	 * 
	 * @param currentlySendingData
	 * @return
	 */
	public static boolean setCurrentlySendingData(boolean currentlySendingData) {
		if (SweetBlue.currentlySendingData != currentlySendingData) {
			SweetBlue.currentlySendingData = currentlySendingData;
			return true;
		} else {
			return false;
		}
	}

	public int getState() {
		return state;
	}

	/**
	 * Changes the mode of a pin on the ArduinoBT.
	 * 
	 * example: pinMode( 1, SweetBlue.OUTPUT );
	 * 
	 * Note: Pins 0, 1, and 7 are a big NO-NO! These pins are connected to the
	 * bluetooth communication and reset and shouldn't be used!
	 * 
	 * @param pin
	 *            number on the ArduinoBT
	 * @param mode
	 *            OUTPUT or INPUT
	 * @return null (if fail) or the sent byte[]
	 */
	public byte[] pinMode(final int pin, int mode) {
		switch (mode) {
		case INPUT:
			mChatService.write(assemblePackage((byte) pin, (byte) 0x01,
					(byte) 0x00));
			break;
		case OUTPUT:
			mChatService.write(assemblePackage((byte) pin, (byte) 0x01,
					(byte) 0x01));
		}
		return null;
	}

	/**
	 * Writes HIGH or LOW to the specified pin on the ArduinoBT.
	 * 
	 * example: ditialWrite( 2, SweetBlue.HIGH );
	 * 
	 * @param pin
	 *            number on the ArduinoBT
	 * @param value
	 *            HIGH or LOW
	 */
	public void digitalWrite(final int pin, int value) {
		mChatService.write(assemblePackage((byte) pin, (byte) 0x03,
				(byte) value));
	}

	/**
	 * Reads value from pin.
	 * 
	 * @param pin
	 * @param variable
	 */
	public void digitalRead(int pin, int[] variable) {
		mChatService
				.write(assemblePackage((byte) pin, (byte) 0x02, (byte) 0x00));

		/**/
		// Read the last position/value of the selected pin in the map
		// return values.get(pin);

		variable[0] = values.get(pin);
	}

	/**
	 * Writes value to the specified pin on the ArduinoBT.
	 * 
	 * example: analogWrite( 2, 127 );
	 * 
	 * @param pin
	 *            number on the ArduinoBT
	 * @param value
	 *            0 - 255
	 */
	public void analogWrite(final int pin, int value) {
		if (value >= 0 && value <= 255)
			mChatService.write(assemblePackage((byte) pin, (byte) 0x03,
					(byte) value));
		else
			Log.i("System.out", SweetBlue.DEBUGTAG
					+ "Bad value on analogWrite!");
	}

	/**
	 * Reads value from pin.
	 * 
	 * IMPORTANT! To "fix" the issue of pass-by-value on primitives, we need the
	 * variable to be non-primitive. An array will do fine for solving this
	 * initially.
	 * 
	 * @param pin
	 *            The pin number to read
	 * @param variable
	 *            variable to which the reading should be written.
	 */
	public void analogRead(int pin, int[] variable) {
		mChatService
				.write(assemblePackage((byte) pin, (byte) 0x02, (byte) 0x00));

		variable[0] = values.get(pin);
	}

	/**
	 * Assembles the Arduino command package and prepares it for serial
	 * 
	 * @param pin
	 * @param cmd
	 * @param val
	 */
	private byte[] assemblePackage(byte pin, byte cmd, byte val) {
		/* Header */
		// [FP][FP][cmd][len][arduinocmd][pin][val][chksum]

		/* Create the package */
		byte[] buffer = new byte[8];

		/* Footprint */
		buffer[0] = (byte) 0xff;
		buffer[1] = (byte) 0xff;

		/* Main command - 0x02 right now */
		buffer[2] = (byte) 0x02;

		/* Length, it's always the same size... for now */
		buffer[3] = (byte) 0x03;

		/* Arduino command - pinmode, digitalwrite, analogread... etc */
		buffer[4] = cmd;

		/* The pin on which to act */
		buffer[5] = pin;

		/* The value */
		buffer[6] = val;

		/* The checksum - cmd ^ len ^ arduinocmd ^ pin ^ val */
		buffer[7] = (byte) ((((buffer[2] ^ buffer[3]) ^ cmd) ^ pin) ^ val);

		return buffer;
	}

	/**
	 * 
	 * Used to organize data before transmitting to bluetooth device.
	 * 
	 * HEADER(4 bytes) - DATA(x bytes) - CHECKSUM(1 byte)
	 * 
	 * Header: [FOOTPRINT][FOOTPRINT][COMMAND][LENGTH]: (0xff)(0xff)(0x??)(0x??)
	 * 
	 * Data: [DATA]...: (0 - 127)
	 * 
	 * Checksum: [CHECKSUM]: (COMMAND XOR LENGTH)
	 * 
	 * @param in
	 * @return
	 * @deprecated This function is used for the Sweet tool, the library uses
	 *             the newer "firmata" like function to communicate with
	 *             ArduinoBT.
	 */
	@Deprecated
	private byte[] attachHeaderBytes(byte[] in) {
		/* Create the header bytes */
		byte[] header = new byte[4];
		header[0] = (byte) 0xff;
		header[1] = (byte) 0xff;
		header[2] = (byte) 0x02;
		header[3] = (byte) in.length;

		/* Create the checksum byte */
		byte checksum = 0;
		checksum = (byte) (header[2] ^ header[3]);
		for (int i = 0; i < in.length; i++)
			checksum ^= in[i];

		/* Data fix, making sure we won't have two 0xff in a row */
		// Removed because it caused issues...
		// for (int i = 1; i < in.length; i += 2)
		// in[i] |= 0x80;

		/* Final assembly... */
		byte[] outdata = new byte[header.length + in.length + 1];
		int i = 0;
		/* ...header */
		for (int index = 0; index < header.length; index++, i++)
			outdata[i] = header[index];
		/* ...data */
		for (int index = 0; index < in.length; index++, i++)
			outdata[i] = in[index];
		/* ...chksum */
		outdata[outdata.length - 1] = checksum;

		return outdata;
	}
}
