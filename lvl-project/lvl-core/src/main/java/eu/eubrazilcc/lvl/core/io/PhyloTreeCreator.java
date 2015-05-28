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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.exec.CommandExecutor.execCommand;
import static java.util.Arrays.asList;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.Logger;

/**
 * Exports phylogenetic trees to different image formats.
 * @author Erik Torres <ertorser@upv.es>
 */
public class PhyloTreeCreator {

	private final static Logger LOGGER = getLogger(PhyloTreeCreator.class);

	public static List<String> newickExportAll(final File tree, final @Nullable File alignment, final File outputDir) throws IOException, InterruptedException {
		final List<String> list = newArrayList();
		for (final String format : new String[]{ "png", "svg" }) {
			list.add(exportNewick(tree, alignment, outputDir, format));
			if (alignment != null) {
				list.add(exportNewick(tree, null, outputDir, format));
			}
		}
		return list;
	}

	public static @Nullable String newickToPng(final File tree, final @Nullable File alignment, final File outputDir) throws IOException, InterruptedException {
		return exportNewick(tree, alignment, outputDir, "png");
	}

	public static @Nullable String newickToSvg(final File tree, final @Nullable File alignment, final File outputDir) throws IOException, InterruptedException {
		return exportNewick(tree, alignment, outputDir, "svg");
	}

	private static @Nullable String exportNewick(final File tree, final @Nullable File alignment, final File outputDir, final String format) throws IOException, InterruptedException {
		checkArgument(tree != null && tree.canRead(), "Uninitialized or invalid tree file");
		checkArgument(alignment == null || alignment.canRead(), "Invalid alignment file");
		checkArgument(outputDir != null && ((outputDir.isDirectory() && outputDir.canWrite()) || outputDir.mkdirs()), "Uninitialized or invalid output directory");		
		final String format2 = trimToEmpty(format).toLowerCase();
		checkArgument(isNotBlank(format2) && asList("png", "svg").contains(format2), "Uninitialized or invalid format");
		String outputPath = null;
		final String phyloTreeToolPath = CONFIG_MANAGER.getPhyloTreeToolPath();
		if (new File(phyloTreeToolPath).canExecute()) {
			final File outputFile = new File(outputDir, getBaseName(tree.getName()) + (alignment != null ? "_" + getBaseName(alignment.getName()) : "") + "." + format2);
			outputPath = outputFile.getCanonicalPath();
			phyloTreeTool(tree.getCanonicalPath(), alignment != null ? alignment.getCanonicalPath() : null, outputPath, phyloTreeToolPath);			
		} else {
			LOGGER.info("Ignoring request since phylogenetic tree tool is not available locally");
		}
		return outputPath;
	}

	private static void phyloTreeTool(final String tree, final @Nullable String alignment, final String output, final String phyloTreeToolPath) throws IOException, InterruptedException {
		execCommand(phyloTreeToolPath + " \"" + tree + "\" " + (alignment != null ? " -a \"" + alignment + "\" " : "") + " -o \"" + output + "\"", 60);
	}

}