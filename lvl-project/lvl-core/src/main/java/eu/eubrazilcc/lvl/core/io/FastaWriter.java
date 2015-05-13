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

package eu.eubrazilcc.lvl.core.io;

import static eu.eubrazilcc.lvl.core.DataSource.GENBANK_SHORT;
import static org.apache.commons.lang.StringUtils.trimToEmpty;

import java.io.IOException;
import java.io.OutputStream;

import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSeq;

/**
 * Writes a sequence to a file or an output stream. Headers are generated using the NCBI format.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://www.ncbi.nlm.nih.gov/books/NBK21097/#A631">The NCBI Handbook, Chapter 16: Appendix 1 - FASTA identifiers</a>
 */
public class FastaWriter {

	public static final char HEADER_FIELD_SEPARATOR = '|';
	public static final char FASTA_BEGIN = '>';
	public static final int LINE_LENGTH = 70;

	private static final byte[] LINE_SEPARATOR = System.getProperty("line.separator").getBytes();	

	public static void writeFasta(final OutputStream os, final GBSeq gbSeq) throws IOException {
		final String header = fastaHeader(gbSeq);
		os.write(FASTA_BEGIN);
		os.write(header.getBytes());
		os.write(LINE_SEPARATOR);

		int compoundCount = 0;
		final String sequence = trimToEmpty(gbSeq.getGBSeqSequence()).toUpperCase();
		for (int i = 0; i < sequence.length(); i++) {
			os.write(sequence.charAt(i));
			compoundCount++;
			if (compoundCount == LINE_LENGTH) {
				os.write(LINE_SEPARATOR);
				compoundCount = 0;
			}
		}

		if ((sequence.length() % LINE_LENGTH) != 0) {
			os.write(LINE_SEPARATOR);
		}
	}

	public static String fastaHeader(final GBSeq gbSeq) {		
		return GENBANK_SHORT + HEADER_FIELD_SEPARATOR + trimToEmpty(gbSeq.getGBSeqAccessionVersion()) 
				+ HEADER_FIELD_SEPARATOR + trimToEmpty(gbSeq.getGBSeqLocus());
	}

}