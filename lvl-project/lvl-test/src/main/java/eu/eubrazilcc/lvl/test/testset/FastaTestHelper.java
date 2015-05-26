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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

/**
 * Creates FASTA files that can be used in tests.
 * @author Erik Torres <ertorser@upv.es>
 */
public class FastaTestHelper {

	private static final String NA_CODE = "ACGTURYKMSWBDHVNX-";
	private static final String AA_CODE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ*-";

	public static File createTestFasta(final File dir, final String filename, final int count, final String code) throws IOException {
		final File fastaFile = new File(dir, filename);
		dir.mkdirs();
		fastaFile.createNewFile();
		assertThat("FASTA file exists in the local filesystem and is writable", fastaFile.canWrite(), equalTo(true));
		final Random rand = new Random();
		try (final PrintWriter pw = new PrintWriter(fastaFile)) {
			for (int i = 0; i < count; i++) {
				pw.println((i > 0 ? "\n" : "") + ">sequence " + i);
				int length = 20 + rand.nextInt(240), col = 0;
				while (length > 0) {
					char symbol = 0;
					if ("NA".equals(code)) {
						symbol = NA_CODE.charAt(rand.nextInt(NA_CODE.length()));											
					} else if ("AA".equals(code)) {
						symbol = AA_CODE.charAt(rand.nextInt(AA_CODE.length()));
					} else {
						throw new IllegalStateException("Unsupported code: " + code);
					}
					pw.print(symbol);
					col++;
					if (col > 79) {
						col = 0;
						pw.println();
					}
					length--;					
				}
			}
		}
		assertThat("Image file has content", fastaFile.length() > 0, equalTo(true));
		return fastaFile;
	}

}