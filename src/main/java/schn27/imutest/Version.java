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
package schn27.imutest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author amalikov
 */
public class Version {
	public static String get() {
		Manifest mf = getManifest();
		return mf != null ? mf.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION) : null;
	}
	
	private static Manifest getManifest() {
		Enumeration<URL> resources;
		try {
			resources = Version.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
		} catch (IOException ex) {
			Logger.getLogger(Version.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}

		while (resources.hasMoreElements()) {
			URL url = resources.nextElement();
			
			if (url.toString().contains("canecutest")) {
				try (InputStream stream = url.openStream()) {
					return new Manifest(stream);
				} catch (IOException ex) {
					Logger.getLogger(Version.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		
		return null;
	}
}
