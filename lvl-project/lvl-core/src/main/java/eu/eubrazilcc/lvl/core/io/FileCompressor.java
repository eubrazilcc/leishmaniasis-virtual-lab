/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.eubrazilcc.lvl.core.io;

import static com.google.common.collect.Lists.newArrayList;
import static java.io.File.separator;
import static org.apache.commons.compress.archivers.tar.TarArchiveOutputStream.LONGFILE_GNU;
import static org.apache.commons.io.FileUtils.forceMkdir;
import static org.apache.commons.io.FilenameUtils.getFullPath;
import static org.apache.commons.io.IOUtils.copy;
import static org.apache.commons.io.IOUtils.write;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

/**
 * File archive and compression utility.
 * @author Apache Whirr<br>
 * Changes (major) by Erik Torres <ertorser@upv.es>:
 * <ul>
 * <li>Target directory is created, avoiding FileNotFoundException to be raised on compress/uncompress.</li>
 * <li>Add additional routines to compress/uncompress and archive files.
 * </ul>
 * @see <a href="http://whirr.apache.org/">Apache Whirr</a>
 */
public final class FileCompressor {

	public static final String GZIP_EXT = "gz";
	private static final int BUFFER_SIZE = 1024;

	/**
	 * Creates a tarball from the source directory and writes it into the target directory.
	 * @param srcDir - directory whose files will be added to the tarball
	 * @param targetName - directory where tarball will be written to
	 * @throws IOException when an exception occurs on creating the tarball
	 */
	public static void tarGzipDir(final String srcDir, final String targetName) throws IOException {


		FileOutputStream fileOutputStream = null;
		BufferedOutputStream bufferedOutputStream = null;
		GzipCompressorOutputStream gzipOutputStream = null;
		TarArchiveOutputStream tarArchiveOutputStream = null;

		try {
			forceMkdir(new File(getFullPath(targetName)));
			fileOutputStream = new FileOutputStream(new File(targetName));
			bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
			gzipOutputStream = new GzipCompressorOutputStream(bufferedOutputStream);
			tarArchiveOutputStream = new TarArchiveOutputStream(gzipOutputStream);

			addFilesInDirectory(tarArchiveOutputStream, srcDir);
		} finally {
			if (tarArchiveOutputStream != null) {
				tarArchiveOutputStream.finish();
			}
			if (tarArchiveOutputStream != null) {
				tarArchiveOutputStream.close();
			}
			if (gzipOutputStream != null) {
				gzipOutputStream.close();
			}
			if (bufferedOutputStream != null) {
				bufferedOutputStream.close();
			}
			if (fileOutputStream != null) {
				fileOutputStream.close();
			}
		}
	}

	private static void addFilesInDirectory(final TarArchiveOutputStream tarOutputStream, final String path) throws IOException {
		final File file = new File(path);
		final File[] children = file.listFiles();
		if (children != null) {
			for (final File child : children) {
				addFile(tarOutputStream, child.getAbsolutePath(), separator);
			}
		}
	}

	private static void addFile(final TarArchiveOutputStream tarOutputStream, final String path, final String base) throws IOException {
		final File file = new File(path);
		final String entryName = base + file.getName();
		final TarArchiveEntry tarEntry = new TarArchiveEntry(file, entryName);

		tarOutputStream.setLongFileMode(LONGFILE_GNU);
		tarOutputStream.putArchiveEntry(tarEntry);

		if (file.isFile()) {
			copy(new FileInputStream(file), tarOutputStream);
			tarOutputStream.closeArchiveEntry();
		} else {
			tarOutputStream.closeArchiveEntry();
			final File[] children = file.listFiles();
			if (children != null) {
				for (final File child : children) {
					addFile(tarOutputStream, child.getAbsolutePath(), entryName + separator);
				}
			}
		}
	}

	/**
	 * Uncompress the content of a tarball file to the specified directory.
	 * @param srcFile - the tarball to be uncompressed
	 * @param outDir - directory where the files (and directories) extracted from the tarball will be written
	 * @return the list of files (and directories) extracted from the tarball
	 * @throws IOException when an exception occurs on uncompressing the tarball
	 */
	public static List<String> unGzipUnTar(final String srcFile, final String outDir) throws IOException {
		final List<String> uncompressedFiles = newArrayList();
		FileInputStream fileInputStream = null;
		BufferedInputStream bufferedInputStream = null;
		GzipCompressorInputStream gzipCompressorInputStream = null;
		TarArchiveInputStream tarArchiveInputStream = null;
		BufferedInputStream bufferedInputStream2 = null;

		try {
			forceMkdir(new File(outDir));
			fileInputStream = new FileInputStream(srcFile);
			bufferedInputStream = new BufferedInputStream(fileInputStream);
			gzipCompressorInputStream = new GzipCompressorInputStream(fileInputStream);
			tarArchiveInputStream = new TarArchiveInputStream(gzipCompressorInputStream);

			TarArchiveEntry entry = null;
			while ((entry = tarArchiveInputStream.getNextTarEntry()) != null) {
				final File outputFile = new File(outDir, entry.getName());
				if (entry.isDirectory()) {					
					// attempting to write output directory
					if (!outputFile.exists()) {
						// attempting to create output directory
						if (!outputFile.mkdirs()) {
							throw new IOException("Cannot create directory: " + outputFile.getCanonicalPath());
						}
					}
				} else {
					// attempting to create output file
					final byte[] content = new byte[(int) entry.getSize()];
					tarArchiveInputStream.read(content, 0, content.length);
					final FileOutputStream outputFileStream = new FileOutputStream(outputFile);
					write(content, outputFileStream);
					outputFileStream.close();
					uncompressedFiles.add(outputFile.getCanonicalPath());					
				}
			}
		} finally {
			if (bufferedInputStream2 != null) {
				bufferedInputStream2.close();
			}
			if (tarArchiveInputStream != null) {
				tarArchiveInputStream.close();
			}
			if (gzipCompressorInputStream != null) {
				gzipCompressorInputStream.close();
			}
			if (bufferedInputStream != null) {
				bufferedInputStream.close();
			}
			if (fileInputStream != null) {
				fileInputStream.close();
			}
		}
		return uncompressedFiles;
	}

	/**
	 * Compress a file using GZIP.
	 * @param srcFile - file to be compressed
	 * @return the name of the file produced which is generated adding the extension {@link FileCompressor#GZIP_EXT} to the original filename.
	 * @throws IOException when an exception occurs on uncompressing the file
	 */
	public static String gzip(final String srcFile) throws IOException {
		final String outFile = srcFile + "." + GZIP_EXT;
		try (final FileOutputStream fos = new FileOutputStream(outFile);
				final BufferedOutputStream bos = new BufferedOutputStream(fos);
				final GzipCompressorOutputStream gos = new GzipCompressorOutputStream(bos);
				final FileInputStream fis = new FileInputStream(srcFile)) {
			final byte[] buffer = new byte[BUFFER_SIZE];
			int n = 0;
			while ((n = fis.read(buffer)) > 0) {
				gos.write(buffer, 0, n);
			}			
		}
		return outFile;
	}

	/**
	 * Uncompress a GZIP compressed file.
	 * @param srcFile - file to be uncompressed
	 * @param outFile - output file
	 * @throws IOException when an exception occurs on uncompressing the file
	 */
	public static void gunzip(final String srcFile, final String outFile) throws IOException {
		try (final FileInputStream fis = new FileInputStream(srcFile);
				final BufferedInputStream bis = new BufferedInputStream(fis);
				final FileOutputStream fos = new FileOutputStream(outFile);
				final GzipCompressorInputStream gis = new GzipCompressorInputStream(fis)) {
			final byte[] buffer = new byte[BUFFER_SIZE];
			int n = 0;
			while (-1 != (n = gis.read(buffer))) {
				fos.write(buffer, 0, n);
			}
		}		
	}

}