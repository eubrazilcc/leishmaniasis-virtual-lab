/*
 * Copyright 2014 EUBrazilCC (EU‐Brazil Cloud Connect)
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

package eu.eubrazilcc.lvl.core.entrez;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.of;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.Format.GB_SEQ_XML;
import static eu.eubrazilcc.lvl.core.io.FastaWriter.writeFasta;
import static eu.eubrazilcc.lvl.core.io.FileCompressor.gzip;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.GBSEQ_XMLB;
import static java.nio.file.Files.move;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

import eu.eubrazilcc.lvl.core.Sequence;
import eu.eubrazilcc.lvl.core.entrez.EntrezHelper.Format;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSeq;

/**
 * Utility class to deal with sequences stored in GenBank Sequence XML format.
 * @author Erik Torres <ertorser@upv.es>
 */
public class GbSeqXmlHelper {

	/** TODO
	 * Opens the source file that corresponds to the specified sequence.
	 * @param sequence - the sequence for which the file source file will be opened
	 * @param format - the format to use to open the source file
	 * @return the source file that corresponds to the specified sequence.
	 *
	public static File openGenBankFile(final Sequence sequence, final Format format) {
		checkArgument(sequence != null, "Uninitialized sequence");
		checkArgument(format != null, "Uninitialized format");
		File file = null;
		switch (format) {
		case GB_SEQ_XML:
			file = new File(CONFIG_MANAGER.getGenBankDir(GB_SEQ_XML), Integer.toString(sequence.getGi()) + ".xml");
			break;
		default:
			throw new IllegalArgumentException("Unsupported format: " + format.toString());
		}
		return file;
	} */

	/**
	 * Converts the specified file, which is expected to contain a valid sequence in GenBank Sequence XML format, to FASTA and
	 * writes the produced FASTA to the filesystem. Optionally, the output file can be compressed with GZIP.
	 * @param file - a file containing a valid sequence in GenBank Sequence XML format
	 * @param filename - the file where the FASTA output will be written
	 * @param compressed - setting this parameter to {@code true} will cause the FASTA output file to be compressed with GZIP
	 * @throws IOException when an exception occurs on converting the file to FASTA
	 */
	public static void toFasta(final File file, final String filename, final boolean compressed) throws IOException {
		checkArgument(file != null, "Uninitialized file");
		toFasta(of(file), filename, compressed);
	}

	/**
	 * Converts a collection of files, which are expected to contain valid sequences in GenBank Sequence XML format, to FASTA and
	 * writes the produced FASTA to the filesystem. Optionally, the output file can be compressed with GZIP.
	 * @param files - a collection of files, each containing a valid sequence in GenBank Sequence XML format
	 * @param filename - the file where the FASTA output will be written
	 * @param compressed - setting this parameter to {@code true} will cause the FASTA output file to be compressed with GZIP
	 * @throws IOException when an exception occurs on converting the file to FASTA
	 */
	public static void toFasta(final Collection<File> files, final String filename, final boolean compressed) throws IOException {
		checkArgument(files != null && !files.isEmpty(), "Uninitialized files collection");		
		final File outfile = new File(filename);
		final File outdir = outfile.getParentFile();		
		checkState(outdir != null && (outdir.isDirectory() || outdir.mkdirs()) && outdir.canWrite(), 
				"Cannot write on parent directory: " + outdir.getAbsolutePath());
		checkState(outfile.canWrite() || outfile.createNewFile(), "Cannot write on output file: " + outfile.getAbsolutePath());
		// write FASTA sequences to file
		try (final FileOutputStream fos = new FileOutputStream(outfile); final BufferedOutputStream bos = new BufferedOutputStream(fos)) {			
			for (final File file : files) {
				final GBSeq sequence = getSequence(file);
				writeFasta(bos, sequence);
			}			
		}
		// file compression
		if (compressed) {
			final String gzFilename = gzip(filename);
			move(get(gzFilename), get(filename), REPLACE_EXISTING);
		}
	}

	/**
	 * Gets a sequence from a file.
	 * @param file - a file containing a valid sequence in GenBank Sequence XML format
	 * @return the sequence parsed from the specified file.
	 * @throws IOException when an exception occurs on parsing the file
	 */
	public static GBSeq getSequence(final File file) throws IOException {
		checkState(file.canRead(), "Sequence file cannot be opened: " + file.getAbsolutePath());
		final GBSeq gbSeq = GBSEQ_XMLB.typeFromFile(file);
		checkState(gbSeq != null, "Invalid sequence file");
		return gbSeq;
	}

}