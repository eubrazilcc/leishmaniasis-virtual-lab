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

package eu.eubrazilcc.lvl.core.util;

import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.AllTests.TEST_RESOURCES_PATH;
import static java.io.File.separator;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.io.FilenameUtils.getBaseName;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;

/**
 * Test utilities.
 * @author Erik Torres <ertorser@upv.es>
 */
public class TestUtils {

	public static final String ANCHOR_FILENAME = "m2anchor";

	public static final String GENBANK_FOLDER = "genbank";
	public static final String GB_SETS_FOLDER = GENBANK_FOLDER + separator + "sets";
	public static final String GB_SEQUENCES_FOLDER = GENBANK_FOLDER + separator + "sequences";

	public static final String PUBMED_FOLDER = "pubmed";
	public static final String PM_SETS_FOLDER = PUBMED_FOLDER + separator + "sets";
	public static final String PM_ARTICLES_FOLDER = PUBMED_FOLDER + separator + "articles";

	public static final String TAXONOMY_FOLDER = "taxonomy";
	public static final String TAXONOMY_SETS_FOLDER = TAXONOMY_FOLDER + separator + "sets";

	public static final String TAPIR_FOLDER = "tapir";
	public static final String TAPIR_REQUESTS_FOLDER = TAPIR_FOLDER + separator + "requests";
	public static final String TAPIR_SEARCH_TEMPLATES_FOLDER = TAPIR_FOLDER + separator + "search_tpls";
	public static final String TAPIR_RESPONSES_FOLDER = TAPIR_FOLDER + separator + "responses";

	public static final String PHYLOGENETIC_FOLDER = "phylogenetic";
	public static final String PHYLOGENETIC_TREE_FOLDER = PHYLOGENETIC_FOLDER + separator + "tree";
	public static final String PHYLOGENETIC_ALIGNMENT_FOLDER = PHYLOGENETIC_FOLDER + separator + "alignment";

	public static final String GEOJSON_FOLDER = "geojson";

	public static final String RESOURCES_FOLDER;
	static {
		final URL anchorURL = TestUtils.class.getClassLoader().getResource(ANCHOR_FILENAME);
		File anchorFile = null;
		try {
			anchorFile = new File(anchorURL.toURI());
		} catch (Exception e) {
			anchorFile = new File(System.getProperty("user.dir"));
		}
		RESOURCES_FOLDER = anchorFile.getParent();
	}

	public static Collection<File> getGenBankFlatFiles() {
		final File dir = new File(concat(TEST_RESOURCES_PATH, GB_SEQUENCES_FOLDER));		
		return listFiles(dir, new String[] { "gb" }, false);
	}

	public static Collection<File> getGBSeqXMLFiles() {
		final File dir = new File(concat(TEST_RESOURCES_PATH, GB_SEQUENCES_FOLDER));
		return listFiles(dir, new String[] { "xml" }, false);
	}

	public static Collection<File> getGBSeqXMLSetFiles() {
		final File dir = new File(concat(TEST_RESOURCES_PATH, GB_SETS_FOLDER));
		return listFiles(dir, new String[] { "xml" }, false);
	}

	public static Collection<File> getPubMedXMLFiles() {
		final File dir = new File(concat(TEST_RESOURCES_PATH, PM_ARTICLES_FOLDER));
		return listFiles(dir, new String[] { "xml" }, false);
	}	

	public static Collection<File> getPubMedXMLSetFiles() {
		final File dir = new File(concat(TEST_RESOURCES_PATH, PM_SETS_FOLDER));
		return listFiles(dir, new String[] { "xml" }, false);
	}

	public static Collection<File> getTaxonomyXMLSetFiles() {
		final File dir = new File(concat(TEST_RESOURCES_PATH, TAXONOMY_SETS_FOLDER));
		return listFiles(dir, new String[] { "xml" }, false);
	}

	public static Collection<File> getTapirRequests() {
		final File dir = new File(concat(TEST_RESOURCES_PATH, TAPIR_REQUESTS_FOLDER));
		return listFiles(dir, new String[] { "xml" }, false);
	}

	public static Collection<File> getTapirSearchTemplates() {
		final File dir = new File(concat(TEST_RESOURCES_PATH, TAPIR_SEARCH_TEMPLATES_FOLDER));
		return listFiles(dir, new String[] { "xml" }, false);
	}

	public static Collection<File> getTapirResponses() {
		final File dir = new File(concat(TEST_RESOURCES_PATH, TAPIR_RESPONSES_FOLDER));
		return listFiles(dir, new String[] { "xml" }, false);
	}	

	public static Collection<File> getGeoJsonFiles() {
		final File dir = new File(concat(TEST_RESOURCES_PATH, GEOJSON_FOLDER));
		return listFiles(dir, new String[] { "json" }, false);
	}

	public static Collection<Phylogenetic> getPhylogeneticFiles() {
		final File treeDir = new File(concat(TEST_RESOURCES_PATH, PHYLOGENETIC_TREE_FOLDER));
		final File alignDir = new File(concat(TEST_RESOURCES_PATH, PHYLOGENETIC_ALIGNMENT_FOLDER));
		final Collection<File> trees = listFiles(treeDir, new String[] { "nh" }, false);
		final Collection<File> alignments = listFiles(alignDir, new String[] { "fasta" }, false);
		final List<Phylogenetic> files = newArrayList();
		for (final File tree : trees) {
			files.add(new Phylogenetic(tree, findFile(getBaseName(tree.getName()) + ".fasta", alignments)));
		}
		return files;
	}

	private static File findFile(final String name, final Collection<File> files) {
		for (final File file : files) {			
			if (file.getName().equals(name)) {
				return file;
			}
		}
		return null;
	}

	public static Collection<File> getTextFiles() {
		final List<File> files = newArrayList();
		files.addAll(getGenBankFlatFiles());
		files.addAll(getGBSeqXMLFiles());
		files.addAll(getGBSeqXMLSetFiles());
		files.addAll(getPubMedXMLSetFiles());
		files.addAll(getGeoJsonFiles());
		return files;
	}

	public static class Phylogenetic {
		private final File tree;
		private final File alignment;
		public Phylogenetic(final File tree, final File alignment) {
			this.tree = tree;
			this.alignment = alignment;
		}
		public File getTree() {
			return tree;
		}
		public File getAlignment() {
			return alignment;
		}		
	}

}