/*
 * Copyright 2014-2015 EUBrazilCC (EU‐Brazil Cloud Connect)
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by 
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *   http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 * 
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 */

package eu.eubrazilcc.lvl.test.testset;

import static java.awt.Color.blue;
import static java.awt.Color.green;
import static java.awt.Color.red;
import static java.awt.Color.white;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

/**
 * Creates and loads images that can be used in tests.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ImageTestHelper {

	public static int MAX_PIXELS = 256;

	/**
	 * Creates a PNG image file in the specified directory with the specified file name. After creating the image, this
	 * method checks that the created file is writable and that has content (file length is not <tt>0</tt>).
	 * @param dir - target directory where the file will be created
	 * @param filename - the file name of the created file
	 * @return A file representing the creating image in the file system.
	 * @throws IOException Thrown when an error occurs during the creation of the file.
	 */
	public static File createTestPng(final File dir, final String filename) throws IOException {		
		return createTestImage(dir, filename, "PNG");
	}

	/**
	 * Creates a JPEG image file in the specified directory with the specified file name. After creating the image, this
	 * method checks that the created file is writable and that has content (file length is not <tt>0</tt>).
	 * @param dir - target directory where the file will be created
	 * @param filename - the file name of the created file
	 * @return A file representing the creating image in the file system.
	 * @throws IOException Thrown when an error occurs during the creation of the file.
	 */
	public static File createTestJpeg(final File dir, final String filename) throws IOException {		
		return createTestImage(dir, filename, "JPEG");
	}

	private static File createTestImage(final File dir, final String filename, final String format) throws IOException {
		final BufferedImage img = new BufferedImage(MAX_PIXELS, MAX_PIXELS, TYPE_INT_RGB);
		// set image background color
		final Graphics2D graphics = img.createGraphics();
		graphics.setPaint(white);
		graphics.fillRect (0, 0, img.getWidth(), img.getHeight());
		// draw three random pixels
		final Random rand = new Random();		
		img.setRGB(rand.nextInt(MAX_PIXELS), rand.nextInt(MAX_PIXELS), red.getRGB());
		img.setRGB(rand.nextInt(MAX_PIXELS), rand.nextInt(MAX_PIXELS), green.getRGB());
		img.setRGB(rand.nextInt(MAX_PIXELS), rand.nextInt(MAX_PIXELS), blue.getRGB());
		// save and check the image
		final File imgFile = new File(dir, filename);
		dir.mkdirs();
		imgFile.createNewFile();
		assertThat("Image file exists in the local filesystem and is writable", imgFile.canWrite(), equalTo(true));
		ImageIO.write(img, format, imgFile);
		assertThat("Image file has content", imgFile.length() > 0, equalTo(true));
		return imgFile;
	}

	public static void verifyImage(final File imgFile) throws IOException {
		final BufferedImage img = ImageIO.read(imgFile);
		assertThat("Image is not null", img, notNullValue());
		assertThat("Image is height is not 0", img.getHeight() > 0, equalTo(true));
		assertThat("Image is width is not 0", img.getWidth() > 0, equalTo(true));
		assertThat("Image content is not null", img.getData(), notNullValue());		
	}
}