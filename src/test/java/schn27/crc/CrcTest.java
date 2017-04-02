/*
 * The MIT License
 *
 * Copyright 2017 Aleksandr Malikov <schn27@gmail.com>.
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
package schn27.crc;

import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author Aleksandr Malikov <schn27@gmail.com>
 */
public class CrcTest extends TestCase {
	@Test
	public void testCrc8() {
		Crc crc = new Crc((byte)8, 0x07, 0xff, false, false, 0x00);
		assertEquals((byte)0xfb, (byte)calcCrcFor(crc, "123456789"));
	}

	@Test
	public void testCrc16() {
		Crc crc = new Crc((byte)16, 0x8005, 0x0000, true, true, 0x0000);
		assertEquals((short)0xbb3d, (short)calcCrcFor(crc, "123456789"));
	}
	
	@Test
	public void testCrc32() {
		Crc crc = new Crc((byte)32, 0x4c11db7, 0xffffffff, true, true, 0xffffffff);
		assertEquals((int)0xcbf43926, (int)calcCrcFor(crc, "123456789"));
	}
	
	private long calcCrcFor(Crc crc, String str) {
		for (char c : str.toCharArray()) {
			crc.accumulate((byte)c);
		}
		
		return crc.get();
	}
}
