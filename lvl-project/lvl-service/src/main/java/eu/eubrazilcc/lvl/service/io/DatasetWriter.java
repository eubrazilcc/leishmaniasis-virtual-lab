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

package eu.eubrazilcc.lvl.service.io;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static eu.eubrazilcc.lvl.core.DataSource.Notation.NOTATION_LONG;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.entrez.GbSeqXmlHelper.getSequence;
import static eu.eubrazilcc.lvl.core.io.FastaReader.readFasta;
import static eu.eubrazilcc.lvl.core.io.FastaWriter.writeFasta;
import static eu.eubrazilcc.lvl.core.io.FileCompressor.gzip;
import static eu.eubrazilcc.lvl.core.util.MimeUtils.isTextFile;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.ID_FRAGMENT_SEPARATOR;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.GBSEQ_XMLB;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.GBSEQ_XML_FACTORY;
import static eu.eubrazilcc.lvl.storage.dao.SequenceDAOHelper.fromString;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.move;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.REQUEST_ENTITY_TOO_LARGE;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryNotEmptyException;
import java.util.Set;

import javax.ws.rs.WebApplicationException;

import org.slf4j.Logger;

import eu.eubrazilcc.lvl.core.Dataset;
import eu.eubrazilcc.lvl.core.Metadata;
import eu.eubrazilcc.lvl.core.Sequence;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSeq;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSet;
import eu.eubrazilcc.lvl.storage.SequenceKey;
import eu.eubrazilcc.lvl.storage.dao.SequenceDAO;

/**
 * Utility class to help with dataset creation.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class DatasetWriter {

	private final static Logger LOGGER = getLogger(DatasetWriter.class);

	public static final String EXPORT_DEFAULT = "export";
	public static final String EXPORT_FASTA = "export_fasta";

	public static final String GZIP = "gzip";
	public static final String NONE = "none";

	/**
	 * Writes database objects specified in the {@link Dataset#getMetadata() metadata} of the dataset to a temporary file. The created file
	 * will be assigned to the {@link Dataset#getOutfile() outfile} field of the input dataset. Unless the metadata is valid and the outfile
	 * is <tt>null</tt>, this method will fail.
	 * @param dataset - contains the specification of the dataset to be written to disk
	 */
	public static void writeDataset(final Dataset dataset) {
		checkArgument(dataset != null, "Uninitialized dataset");
		checkArgument(dataset.getOutfile() == null, "The dataset already contains an output file");
		checkArgument(dataset.getMetadata() != null, "Uninitialized or invalid metadata");
		final Metadata metadata = dataset.getMetadata();
		checkArgument(metadata.getTarget() != null, "Uninitialized target");
		checkArgument(metadata.getTarget().getIds() != null && !metadata.getTarget().getIds().isEmpty(), "Uninitialized or invalid id");
		checkArgument(isNotBlank(metadata.getTarget().getType()), "Uninitialized or invalid type");
		checkArgument(isNotBlank(metadata.getTarget().getCollection()), "Uninitialized or invalid collection");
		final String type = metadata.getTarget().getType().trim().toLowerCase();
		File outfile = null;
		if ("sequence".equals(type)) {			
			outfile = sequence2dataset(dataset);
		} else {
			throw new IllegalArgumentException("Unsupported type: " + metadata.getTarget().getType());
		}
		dataset.setOutfile(outfile);		
	}

	public static void writeDataset(final Dataset dataset, final InputStream is, final String filename) {
		checkArgument(dataset != null, "Uninitialized dataset");
		checkArgument(dataset.getOutfile() == null, "The dataset already contains an output file");
		checkArgument(dataset.getMetadata() != null, "Uninitialized or invalid metadata");
		checkArgument(is != null, "Uninitialized input stream");
		checkArgument(isNotBlank(filename), "Uninitialized or invalid filename");
		File tmpFile = null;
		OutputStream os = null;
		try {
			final File baseOutputDir = new File(CONFIG_MANAGER.getLocalCacheDir(), DatasetWriter.class.getSimpleName());
			baseOutputDir.mkdirs();				
			final File outputDir = createTempDirectory(baseOutputDir.toPath(), "dataset-").toFile();
			tmpFile = new File(outputDir, getBaseName(filename));
			LOGGER.trace("Writing custom dataset to '" + tmpFile.getCanonicalPath() + "'");
			os = new FileOutputStream(tmpFile);
			long total = 0l, max = CONFIG_MANAGER.getMaxUserUploadedFileSize() * 1024l;
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = is.read(bytes)) != -1) {
				os.write(bytes, 0, read);
				total += read;
				if (total > max) {
					throw new WebApplicationException("Attachment exceeds the allowable limit", REQUEST_ENTITY_TOO_LARGE);
				}
			}
			os.flush();
			os.close();
			dataset.setOutfile(tmpFile);			
			final String type = discoverFileFormat(tmpFile);
			if ("FASTA".equals(type)) {
				dataset.getMetadata().getTarget().setType("sequence");
				dataset.getMetadata().getTags().add("fasta");
			} else if ("GBSeq".equals(type)) {
				dataset.getMetadata().getTarget().setType("sequence");
				dataset.getMetadata().getTags().add("ncbi_xml");
			} else {
				throw new IllegalArgumentException("Unknown file format");
			}
		} catch (IOException e) {
			throw new WebApplicationException("Failed to save attachment", INTERNAL_SERVER_ERROR);
		} finally {
			try {
				is.close();
			} catch (Exception ignore) { }
			try {
				os.close();
			} catch (Exception ignore) { }		
		}
	}

	private static String discoverFileFormat(final File file) {
		if (isTextFile(file)) {
			if (isFastaFile(file)) {
				return "FASTA";
			} else if (isGbXmlSeqFile(file)) {
				return "GBSeq";
			}
		}
		throw new IllegalStateException("Unsupported file format");
	}

	private static boolean isFastaFile(final File file) {
		boolean checked = false;
		try {
			final String[] sequences = readFasta(file);
			checked = sequences != null && sequences.length > 0;
		} catch (Exception ignore) { }
		return checked;
	}

	private static boolean isGbXmlSeqFile(final File file) {
		boolean checked = false;
		try {
			final GBSeq sequence = getSequence(file);
			checked = sequence != null && isNotBlank(sequence.getGBSeqPrimaryAccession());
		} catch (Exception ignore) { }
		return checked;
	}

	private static File sequence2dataset(final Dataset dataset) {		
		final Metadata metadata = dataset.getMetadata();
		final String filter = isNotBlank(metadata.getTarget().getFilter()) ? metadata.getTarget().getFilter().trim().toLowerCase() : EXPORT_DEFAULT;
		checkState(EXPORT_DEFAULT.equals(filter) || EXPORT_FASTA.equals(filter), "Unsupported filter: " + filter);
		final String extension = EXPORT_DEFAULT.equals(filter) ? "xml" : "fasta";
		final File outfile = createOutputFile(metadata.getTarget().getIds(), extension);
		final String compression = getCompression(metadata.getTarget().getCompression());
		try (final FileWriter fw = new FileWriter(outfile, true)) {
			final int count = metadata.getTarget().getIds().size();
			if (EXPORT_FASTA.equals(filter)) {
				LOGGER.trace(new StringBuffer("Writing ").append(count).append(" FASTA sequence(s) to '")
						.append(outfile.getAbsolutePath()).append("'").toString());
				metadata.getTarget().getIds().stream().forEachOrdered(id -> {
					final Sequence sequence = fetchSequence(metadata.getTarget().getCollection(), id);
					try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
						writeFasta(os, sequence.getSequence());
						fw.append(new String(os.toByteArray(), UTF_8.name()));
					} catch (IOException e2) {
						throw new IllegalStateException("Failed to write FASTA sequence [id=" + id + "]", e2);
					}
				});
				dataset.getMetadata().getTags().add("fasta");
			} else {
				LOGGER.trace(new StringBuffer("Writing ").append(count).append(" NCBI sequence(s) to '")
						.append(outfile.getAbsolutePath()).append("'").toString());
				if (count == 1) {
					final Sequence sequence = fetchSequence(metadata.getTarget().getCollection(), 
							metadata.getTarget().getIds().iterator().next());
					GBSEQ_XMLB.typeToFile(sequence.getSequence(), outfile);
				} else {
					final GBSet set = GBSEQ_XML_FACTORY.createGBSet();
					metadata.getTarget().getIds().stream().forEachOrdered(id -> {
						final Sequence sequence = fetchSequence(metadata.getTarget().getCollection(), id);
						set.getGBSeq().add(sequence.getSequence());
					});
					GBSEQ_XMLB.typeToFile(set, outfile);
				}
				dataset.getMetadata().getTags().add("ncbi_xml");
			}
		} catch (IOException e) {
			throw new IllegalStateException("Failed to write sequence(s) to file", e);
		}
		// file compression
		if (GZIP.equals(compression)) {
			try {
				final String outfilename = outfile.getCanonicalPath();
				final String gzFilename = gzip(outfilename);
				move(get(gzFilename), get(outfilename), REPLACE_EXISTING);
			} catch (IOException e) {
				throw new IllegalStateException("Failed to apply file compression", e);
			}
		}
		return outfile;
	}

	private static File createOutputFile(final Set<String> ids, final String ext) {
		try {
			final File baseOutputDir = new File(CONFIG_MANAGER.getLocalCacheDir(), DatasetWriter.class.getSimpleName());
			baseOutputDir.mkdirs();
			final File outDir = createTempDirectory(baseOutputDir.toPath(), "dataset-").toFile();
			return new File(outDir, new StringBuffer(ids.size() == 1 ? ids.iterator().next() : "sequences")
					.append(".").append(ext).toString());
		} catch (IOException e) {
			throw new IllegalStateException("Failed to create output file", e);
		}
	}

	private static final Sequence fetchSequence(final String collection, final String id) {
		final SequenceDAO<? extends Sequence> dao = fromString(collection);
		final Sequence sequence = dao.find(SequenceKey.builder().parse(id, ID_FRAGMENT_SEPARATOR, NOTATION_LONG));
		checkState(sequence != null, "Sequence not found: " + id);
		return sequence;
	}

	public static void unsetDataset(final Dataset dataset) {
		File outfile = null;
		checkArgument(dataset != null && (outfile = dataset.getOutfile()) != null, "Uninitialized of invalid dataset");		
		if (isRegularFile(outfile.toPath(), NOFOLLOW_LINKS)) {
			deleteQuietly(outfile);
		}
		final File parent = outfile.getParentFile();
		if (isDirectory(parent.toPath(), NOFOLLOW_LINKS)) {
			try {
				delete(parent.toPath());
			} catch (DirectoryNotEmptyException e) {
				LOGGER.warn("The directory is not empty, so was not removed: " + parent.getAbsolutePath());
			} catch (IOException e) {
				LOGGER.error("Failed to remove directory: " + parent.getAbsolutePath(), e);
			}
		}
		LOGGER.trace("Public link was removed: " + outfile.getAbsolutePath());		
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

}