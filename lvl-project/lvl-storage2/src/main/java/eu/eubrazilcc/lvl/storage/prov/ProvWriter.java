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

package eu.eubrazilcc.lvl.storage.prov;

import org.openprovenance.prov.interop.InteropFramework;
import org.openprovenance.prov.model.Document;

/**
 * Exports provenance to several file formats.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class ProvWriter {

	/**
	 * Exports the specified document to a file. The file format is discovered from the file extension. <strong>Note </strong> that 
	 * Graphviz is needed locally to export to PROVN and SVG formats.
	 * @param document - provenance document to be exported
	 * @param file - output file
	 * @see <a href="http://www.graphviz.org/">Graphviz</a>
	 */
	public static void provToFile(final Document document, final String file) {
		final InteropFramework intF = new InteropFramework();
		intF.writeDocument(file, document);     
		// intF.writeDocument(System.out, ProvFormat.JSON, document);
		// intF.writeDocument(System.out, ProvFormat.PROVN, document);
	}
	
}