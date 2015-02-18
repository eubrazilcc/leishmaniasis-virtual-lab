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
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;

import eu.eubrazilcc.lvl.core.Sequence;
import eu.eubrazilcc.lvl.core.entrez.EntrezHelper.Format;
import eu.eubrazilcc.lvl.storage.mongodb.cache.CachedVersionable;
import eu.eubrazilcc.lvl.storage.mongodb.cache.SequencePersistingCache;

/**
 * Utility class to help with creation of NCBI objects (DNA sequences, PubMed citations) in disk (filesystem).
 * @author Erik Torres <ertorser@upv.es>
 */
public final class NcbiObjectWriter {

	private final static Logger LOGGER = getLogger(NcbiObjectWriter.class);
	private static final SequencePersistingCache PERSISTING_CACHE = new SequencePersistingCache();

	/**
	 * Opens the source file that corresponds to the specified sequence.
	 * @param sequence - the sequence for which the file source file will be opened
	 * @param format - the format to use to open the source file
	 * @return the source file that corresponds to the specified sequence.
	 */
	public static File openGenBankFile(final Sequence sequence, final Format format) {
		checkArgument(sequence != null && isNotBlank(sequence.getId()) && isNotBlank(sequence.getVersion()), "Uninitialized or invalid sequence");
		checkArgument(format != null, "Uninitialized format");
		File file = null;
		try {
			switch (format) {
			case GB_SEQ_XML:
				CachedVersionable cachedFile = PERSISTING_CACHE.getIfPresent(sequence.getId());
				if (cachedFile != null) {
					if (!cachedFile.getVersion().equals(sequence.getVersion())) {
						cachedFile = PERSISTING_CACHE.update(sequence.getId(), sequence);
					}
				} else {
					cachedFile = PERSISTING_CACHE.put(sequence.getId(), sequence);
				}
				file = new File(cachedFile.getCachedFilename());			
				break;
			default:
				throw new IllegalArgumentException("Unsupported format: " + format.toString());
			}
		} catch (IOException e) {
			LOGGER.error("Failed to get file for sequence", e);
		}
		return file;		
	}

}