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

package eu.eubrazilcc.lvl.service.io;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.FluentIterable.from;
import static eu.eubrazilcc.lvl.core.DataSource.Notation.NOTATION_LONG;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.Format.GB_SEQ_XML;
import static eu.eubrazilcc.lvl.core.entrez.GbSeqXmlHelper.getSequence;
import static eu.eubrazilcc.lvl.core.entrez.GbSeqXmlHelper.openGenBankFile;
import static eu.eubrazilcc.lvl.core.entrez.GbSeqXmlHelper.toFasta;
import static eu.eubrazilcc.lvl.core.io.FileCompressor.gzip;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.ID_FRAGMENT_SEPARATOR;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.GBSEQ_XMLB;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.GBSEQ_XML_FACTORY;
import static eu.eubrazilcc.lvl.storage.dao.SequenceDAOHelper.fromString;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.io.IOUtils.copy;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.util.List;

import org.slf4j.Logger;

import com.google.common.base.Function;

import eu.eubrazilcc.lvl.core.PublicLink;
import eu.eubrazilcc.lvl.core.Sequence;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSeq;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSet;
import eu.eubrazilcc.lvl.storage.SequenceKey;
import eu.eubrazilcc.lvl.storage.dao.SequenceDAO;

/**
 * Utility class to help with public link creation.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class PublicLinkWriter {

	private final static Logger LOGGER = getLogger(PublicLinkWriter.class);

	public static final String EXPORT_DEFAULT = "export";
	public static final String EXPORT_FASTA = "export_fasta";

	public static final String GZIP = "gzip";
	public static final String NONE = "none";

	public static String writePublicLink(final PublicLink publicLink, final File outputDir) {		
		checkArgument(publicLink != null, "Uninitialized public link");
		checkArgument(publicLink.getTarget() != null, "Uninitialized target");
		checkArgument(publicLink.getTarget().getIds() != null && !publicLink.getTarget().getIds().isEmpty(), "Uninitialized or invalid id");
		checkArgument(isNotBlank(publicLink.getTarget().getType()), "Uninitialized or invalid type");
		checkArgument(isNotBlank(publicLink.getTarget().getCollection()), "Uninitialized or invalid collection");
		final String type = publicLink.getTarget().getType().trim().toLowerCase();
		String path = null;
		if ("sequence".equals(type)) {
			path = sequence2publicLink(publicLink, outputDir);
		} else {
			throw new IllegalArgumentException("Unsupported type: " + publicLink.getTarget().getType());
		}
		return path;
	}

	private static String sequence2publicLink(final PublicLink publicLink, final File outputDir) {
		final String filter = isNotBlank(publicLink.getTarget().getFilter()) ? publicLink.getTarget().getFilter().trim().toLowerCase() : EXPORT_DEFAULT;
		String path = null;
		if (EXPORT_DEFAULT.equals(filter) || EXPORT_FASTA.equals(filter)) {
			final String compression = getCompression(publicLink.getTarget().getCompression());
			final List<File> files = from(publicLink.getTarget().getIds()).transform(new Function<String, File>() {
				@Override
				public File apply(final String id) {
					final SequenceDAO<? extends Sequence> dao = fromString(publicLink.getTarget().getCollection());
					final Sequence sequence = dao.find(SequenceKey.builder().parse(id, ID_FRAGMENT_SEPARATOR, NOTATION_LONG));
					checkState(sequence != null, "Sequence not found: " + id);
					return openGenBankFile(sequence, GB_SEQ_XML);					
				}
			}).toList();
			try {
				if (EXPORT_FASTA.equals(filter)) {
					if (files.size() == 1) {
						path = linkFastaSequence(files.get(0), outputDir, compression);						
					} else {
						path = linkFastaSequences(files, outputDir, compression);
					}
				} else {
					if (files.size() == 1) {
						path = linkGbXmlSequence(files.get(0), outputDir, compression);
					} else {
						path = linkGbXmlSequences(files, outputDir, compression);
					}
				}
			} catch (IOException e) {
				throw new IllegalStateException("Failed to copy sequence", e);
			}	
		} else {
			throw new IllegalArgumentException("Unsupported filter: " + publicLink.getTarget().getFilter());
		}
		return path;
	}

	private static String linkFastaSequence(final File inFile, final File outDir, final String compression) throws IOException {		
		final File outFile = new File(outDir, getBaseName(inFile.getCanonicalPath()) + ".fasta" + (compression.equals(GZIP) ? ".gz" : ""));
		LOGGER.trace("Writing FASTA sequence from '" + inFile.getCanonicalPath() + "' to '" + outFile.getCanonicalPath());
		toFasta(inFile, outFile.getCanonicalPath(), compression.equals(GZIP));
		return outFile.getCanonicalPath();
	}

	private static String linkFastaSequences(final List<File> files, final File outDir, final String compression) throws IOException {
		final File outFile = new File(outDir, "sequences.fasta" + (compression.equals(GZIP) ? ".gz" : ""));
		LOGGER.trace("Writing a bulk of FASTA sequences to '" + outFile.getCanonicalPath());
		toFasta(files, outFile.getCanonicalPath(), compression.equals(GZIP));
		return outFile.getCanonicalPath();
	}

	private static String linkGbXmlSequence(final File inFile, final File outDir, final String compression) throws IOException {		
		final File outFile = new File(outDir, getName(inFile.getCanonicalPath()));
		String outFilename = outFile.getCanonicalPath();		
		LOGGER.trace("Writing NCBI sequence from '" + inFile.getCanonicalPath() + "' to '" + outFile.getCanonicalPath());
		outFile.getParentFile().mkdirs();
		try (final FileInputStream fin = new FileInputStream(inFile); final FileOutputStream fos = new FileOutputStream(outFile)) {			
			copy(fin, fos);
		}
		if (compression.equals(GZIP)) {
			outFilename = gzip(outFile.getCanonicalPath());
			outFile.delete();
		}
		return outFilename;
	}

	private static String linkGbXmlSequences(final List<File> files, final File outDir, final String compression) throws IOException {
		final File outFile = new File(outDir, "sequences.xml");
		String outFilename = outFile.getCanonicalPath();
		LOGGER.trace("Writing a bulk of NCBI sequences to '" + outFile.getCanonicalPath());
		final GBSet set = GBSEQ_XML_FACTORY.createGBSet();
		for (final File file : files) {
			final GBSeq sequence = getSequence(file);
			set.getGBSeq().add(sequence);
		}
		outFile.getParentFile().mkdirs();
		GBSEQ_XMLB.typeToFile(set, outFile);
		if (compression.equals(GZIP)) {
			outFilename = gzip(outFile.getCanonicalPath());
			outFile.delete();
		}
		return outFilename;
	}

	private static String getCompression(final String compression) {
		String compression2 = isNotBlank(compression) ? compression.trim().toLowerCase() : NONE;
		if (!NONE.equals(compression2)) {
			if (GZIP.equals(compression2)) {
				// gzip compressed output
			} else {
				throw new IllegalArgumentException("Unsupported compression: " + compression);
			}
		}
		return compression2;
	}

	public static void unsetPublicLink(final PublicLink publicLink, final File baseDir) {
		unsetPublicLink(new File(baseDir, publicLink.getPath()));		
	}

	public static void unsetPublicLink(final File file) {
		if (isRegularFile(file.toPath(), NOFOLLOW_LINKS)) {
			deleteQuietly(file);
		}
		final File parent = file.getParentFile();
		if (isDirectory(parent.toPath(), NOFOLLOW_LINKS)) {
			try {
				delete(parent.toPath());
			} catch (DirectoryNotEmptyException e) {
				LOGGER.warn("The directory is not empty, so was not removed: " + parent.getAbsolutePath());
			} catch (IOException e) {
				LOGGER.error("Failed to remove directory: " + parent.getAbsolutePath(), e);
			}
		}
		LOGGER.trace("Public link was removed: " + file.getAbsolutePath());
	}

}