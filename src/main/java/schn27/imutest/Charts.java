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
package schn27.imutest;

import com.sun.tools.visualvm.charts.ChartFactory;
import com.sun.tools.visualvm.charts.SimpleXYChartDescriptor;
import com.sun.tools.visualvm.charts.SimpleXYChartSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

/**
 *
 * @author Aleksandr Malikov <schn27@gmail.com>
 */
public class Charts {
	public Charts(javax.swing.JPanel container, Duration historyLength, Duration minPeriod, String[] names, double factor) {
		this.container = container;
		this.historyLength = historyLength;
		this.minPeriod = minPeriod;
		this.names = names;
		this.factor = factor;
		scale = 1 / factor;
		values = new long[names.length];
		init();
	}
	
	public void clear() {
		if (chart != null) {
			container.remove(chart.getChart());
		}
		
		init();
	}
	
	public void add(Map<String, Object> values) {
		Instant now = Instant.now();
		if (Duration.between(lastTimestamp, now).compareTo(minPeriod) < 0) {
			return;		// skip
		}
		
		for (int i = 0; i < names.length; ++i) {
			Number v = (Number)values.get(names[i]);
			if (v != null) {
				this.values[i] = (long)(v.doubleValue() * scale);
			}
		}
		
		chart.addValues(now.toEpochMilli(), this.values);
		lastTimestamp = now;
	}
	
	private void init() {
		lastTimestamp = Instant.MIN;
		
		Arrays.fill(values, 0);
		
		SimpleXYChartDescriptor descriptor =
			SimpleXYChartDescriptor.decimal(0, 0, 0, factor, false, (int)(historyLength.toMillis() / minPeriod.toMillis()));	

		for (String name : names) {
			descriptor.addLineItems(name);
		}

		chart = ChartFactory.createSimpleXYChart(descriptor);
		container.add(chart.getChart());
	}
	
	private final javax.swing.JPanel container;
	private final Duration historyLength;
	private final Duration minPeriod;
	private final String[] names;
	private final double factor;
	private final double scale;
	private final long[] values;
	private SimpleXYChartSupport chart;
	private Instant lastTimestamp;
}
