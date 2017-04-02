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

/**
 *
 * @author Aleksandr Malikov <schn27@gmail.com>
 */
public class Crc {
	public Crc(byte width, int poly, int init, boolean reverseInput, boolean reverseOutput, int finalXor) {
		this.width = width;
		mask = (int)((1L << width) - 1);
		this.poly = poly;
		this.init = init;
		this.reverseInput = reverseInput;
		this.reverseOutput = reverseOutput;
		this.finalXor = finalXor;
		this.crc = init;
	}
	
	public void accumulate(byte c) {
		c = reverseInput ? reverseByte(c) : c;
		crc ^= c << (width - 8);

		for (int i = 0; i < 8; ++i) {
			crc = ((crc & (1 << (width - 1))) != 0) ? (crc << 1) ^ poly : (crc << 1);
		}
	}

	public long get() {
		return ((reverseOutput ? reverse(crc) : crc) ^ finalXor) & mask;
	}

	public void reset() {
		crc = init;
	}

	private byte reverseByte(byte x) {
		x = (byte)(((x >> 1) & 0x55) | ((x & 0x55) << 1));
		x = (byte)(((x >> 2) & 0x33) | ((x & 0x33) << 2));
		return 
			(byte)(((x >> 4) & 0x0F) | (x << 4));
	}

	private int reverse(int x) {
		x = ((x >> 1) & 0x55555555) | ((x & 0x55555555) << 1);
		x = ((x >> 2) & 0x33333333) | ((x & 0x33333333) << 2);
		x = ((x >> 4) & 0x0F0F0F0F) | ((x & 0x0F0F0F0F) << 4);
		
		if (width <= 8) {
			return x;
		}
		
		x = ((x >> 8) & 0x00FF00FF) | ((x & 0x00FF00FF) << 8);

		if (width <= 16) {
			return x;
		}		
		
		return ((x >> 16) & 0x0000FFFF) | (x << 16);
	}
	
	private final byte width;
	private final int mask;
	private final int poly;
	private final int init;
	private final boolean reverseInput;
	private final boolean reverseOutput;
	private final int finalXor;
	private int crc;
}
