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

package eu.eubrazilcc.lvl.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.listAllPhlebotomines;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.saveNucleotides;
import static eu.eubrazilcc.lvl.core.entrez.GenBankSequenceAnalizer.listSequences;
import static java.nio.file.Files.createTempDirectory;
import static org.apache.commons.io.FileUtils.deleteQuietly;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

import eu.eubrazilcc.lvl.core.SequenceDataSource;
import eu.eubrazilcc.lvl.storage.dao.SequenceDAO;

/**
 * Utilities to perform common task with sequences. For example, to search sequences in the NCBI
 * nucleotide database and create the sequences missing in the application's database.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class SequenceHelper {

	private final static Logger LOGGER = LoggerFactory.getLogger(SequenceHelper.class);

	/**
	 * Imports sequences from external databases into the application's database.
	 * @param database - source database, for example, GenBank
	 */
	public static final void importSequences(final SequenceDataSource database) {
		checkArgument(database != null, "Uninitialized database");
		switch (database) {
		case GENBANK:
			importSequencesFromGenBank();
			break;
		default:
			throw new IllegalArgumentException("Unsupported database: " + database);
		}
	}

	private static final void importSequencesFromGenBank() {
		final Set<String> ids = listAllPhlebotomines();
		checkState(ids != null, "Failed to find sequence ids");
		File tmpDir = null;
		try {			
			// filter sequences already stored in the application's database
			final Set<String> missingIds = from(ids).transform(new Function<String, String>() {
				@Override
				public String apply(final String id) {
					return SequenceDAO.INSTANCE.find(id) == null ? id : null;					
				}				
			}).filter(notNull()).toSet();
			// fetch missing sequences
			tmpDir = createTempDirectory("tmp_sequences").toFile();
			saveNucleotides(missingIds, tmpDir);
			final Collection<File> files = listSequences(tmpDir);
			for (final File file : files) {
				
				
				// TODO
			}
		} catch (Exception e) {
			throw new IllegalStateException("Failed to create sequences from GenBank", e);
		} finally {
			if (tmpDir != null) {
				deleteQuietly(tmpDir);
			}
		}
	}

}