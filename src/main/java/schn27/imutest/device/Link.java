/* 
 * The MIT License
 *
 * Copyright 2016 Aleksandr Malikov <schn27@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package schn27.imutest.device;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import schn27.serial.Com;
import schn27.serial.NotAvailableException;
import schn27.serial.Serial;

/**
 *
 * @author amalikov
 */
public class Link implements Runnable {
	public Link(String portName, 
			Consumer<Map<String, Object>> valuesConsumer,
			Consumer<String> consoleConsumer) {
		this.portName = portName;
		this.valuesConsumer = valuesConsumer;
		this.consoleConsumer = consoleConsumer;
		
		values = new ConcurrentHashMap<>();
		
		headers = new byte[][]{
			{(byte)0xfe, (byte)0x81, (byte)0xff, (byte)0x55}, 
			{(byte)0xfe, (byte)0x81, (byte)0x00, (byte)0xaa}, 
			{(byte)0xfe, (byte)0x81, (byte)0x00, (byte)0xab}
		};
		
		buffer = new byte[256];
		outStream = new ByteArrayOutputStream();
	}
	
	@Override
	public void run() {
		running = true;
		thread = Thread.currentThread();
		
		try {
			serial = new Com(portName, 921600);
			serial.open();
			
			while (!Thread.currentThread().isInterrupted() && running) {
				try {
					processData();
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}
		} catch (IOException | NotAvailableException ex) {
			log.log(Level.SEVERE, "exception", ex);
		} finally {
			try {
				serial.close();
			} catch (IOException ex) {
				log.log(Level.SEVERE, "exception", ex);
			}
			
			serial = null;
		}
		
		running = false;
	}
	
	public void stop() {
		running = false;
		thread.interrupt();
		while (thread.isAlive()) {
			try {
				thread.join();
			} catch (InterruptedException ex) {
			}
		}
	}
	
	public void sendCommand(String text) {
		text += "\r\n";
		serial.write(text.getBytes(), 0, text.length());
	}
	
	private void processData() throws InterruptedException, NotAvailableException {
		if (serial.read(buffer, bufferPos, 1, 50) != 1) {
			bufferPos = 0;
			outStream.reset();
		} else {
			++bufferPos;
					
			if (getHeaderIndex() < 0) {
				processConsole();
			} else if (bufferPos == 4) {
				processMessage();
			}
		}
	}
	
	private int getHeaderIndex() {
		for (int i = 0; i < headers.length; ++i) {
			byte header[] = headers[i];
			
			boolean equals = true;
			
			for (int j = 0; j < bufferPos; ++j) {
				equals &= header[j] == buffer[j];
			}
			
			if (equals) {
				return i;
			}
		}
		
		return -1;
	}
	
	private void processConsole() {
		int b = buffer[bufferPos - 1] & 0xff;
		bufferPos = 0;
		
		if (b < 128) {
			outStream.write(b);

			if (b == '\n') {
				String str = outStream.toString();
				outStream.reset();
				if (str.length() >= 2) {
					consoleConsumer.accept(str.substring(0, str.length() - 2));
				}
			}
		} else {
			outStream.reset();
		}
	}

	private void processMessage() throws InterruptedException, NotAvailableException {
		switch (getHeaderIndex()) {
		case 0:
			processMessage0();
			break;
		case 1:
			processMessage1();
			break;
		case 2:
			processMessage2();
			break;
		}
		
		bufferPos = 0;
	}
	
	private void processMessage0() throws InterruptedException, NotAvailableException {
		if (serial.read(buffer, bufferPos, 32, 50) != 32) {
			return;
		}
		
		ByteBuffer bb = ByteBuffer.wrap(buffer, 0, 256).order(ByteOrder.BIG_ENDIAN);
		bb.getInt();	// header
		
		final float G = 9.81f;
		
		values.put("Gyro X", bb.getFloat());
		values.put("Gyro Y", bb.getFloat());
		values.put("Gyro Z", bb.getFloat());
		values.put("Accel X", bb.getFloat() * G);
		values.put("Accel Y", bb.getFloat() * G);
		values.put("Accel Z", bb.getFloat() * G);
		values.put("Status", bb.get());
		values.put("Sequence", bb.get());
		values.put("Temperature", bb.getShort() * 0.01f);
		valuesConsumer.accept(values);
		
		bb.getInt();	// CRC
	}

	private void processMessage1() throws InterruptedException, NotAvailableException {
		if (serial.read(buffer, bufferPos, 7, 50) != 7) {
			return;
		}
		
		ByteBuffer bb = ByteBuffer.wrap(buffer, 0, 256).order(ByteOrder.BIG_ENDIAN);
	}	

	private void processMessage2() throws InterruptedException, NotAvailableException {
		if (serial.read(buffer, bufferPos, 9, 50) != 9) {
			return;
		}
		
		ByteBuffer bb = ByteBuffer.wrap(buffer, 0, 256).order(ByteOrder.BIG_ENDIAN);
	}	
	
	private final String portName;
	private final Consumer<Map<String, Object>> valuesConsumer;
	private final Consumer<String> consoleConsumer;
	private final Map<String, Object> values;
	private Thread thread;
	private Serial serial;
	private volatile boolean running;
	
	private final byte[][] headers;

	private final byte[] buffer;
	private int bufferPos;
		
	private final ByteArrayOutputStream outStream;

	private static final Logger log = Logger.getLogger(Link.class.getName());
}