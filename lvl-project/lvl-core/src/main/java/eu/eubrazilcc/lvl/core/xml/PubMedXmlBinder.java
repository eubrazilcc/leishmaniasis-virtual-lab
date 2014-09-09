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

package eu.eubrazilcc.lvl.core.xml;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBElement;

import eu.eubrazilcc.lvl.core.Reference;
import eu.eubrazilcc.lvl.core.xml.ncbi.pubmed.ObjectFactory;
import eu.eubrazilcc.lvl.core.xml.ncbi.pubmed.PubmedArticle;
import eu.eubrazilcc.lvl.core.xml.ncbi.pubmed.PubmedArticleSet;

/**
 * NCBI PubMed article XML binding helper.
 * @author Erik Torres <ertorser@upv.es>
 */
public class PubMedXmlBinder extends XmlBinder {

	private static final Class<?>[] SUPPORTED_CLASSES = {
		PubmedArticleSet.class,
		PubmedArticle.class
	};

	public static final ObjectFactory PUBMED_XML_FACTORY = new ObjectFactory();	

	public static final PubMedXmlBinder PUBMED_XML = new PubMedXmlBinder();

	private PubMedXmlBinder() {
		super(SUPPORTED_CLASSES);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <T> JAXBElement<T> createType(final T obj) {
		Object element = null;
		Class<? extends Object> clazz = obj.getClass();
		if (clazz.equals(PubmedArticleSet.class)) {
			element = PUBMED_XML_FACTORY.createPubmedArticleSet();
		} else if (clazz.equals(PubmedArticle.class)) {
			element = PUBMED_XML_FACTORY.createPubmedArticle();
		} else {
			throw new IllegalArgumentException("Unsupported type: " + clazz.getCanonicalName());
		}
		return (JAXBElement<T>) element;
	}
	
	/**
	 * Gets the PubMed identifier (PMID) from a PubMed article.
	 * @param article - PubMed article
	 * @return if found, the PubMed identifier (PMID), otherwise {@code null}.
	 */
	public static final @Nullable String getPubMedId(final PubmedArticle article) {
		checkArgument(article != null && article.getMedlineCitation() != null && article.getMedlineCitation().getPMID() != null, 
				"Uninitialized or invalid article");
		return isNotBlank(article.getMedlineCitation().getPMID().getvalue()) ? article.getMedlineCitation().getPMID().getvalue().trim() : null;		
	}

	/**
	 * Parses a publication reference from a PubMed article.
	 * @param article - PubMed article
	 * @return a {@link Reference} built from the input PubMed article.
	 */
	public static final Reference parseArticle(final PubmedArticle article) {
		checkArgument(article != null, "Uninitialized or invalid article");
		return Reference.builder()
				.title(article.getMedlineCitation().getArticle().getArticleTitle())
				.pubmedId(article.getMedlineCitation().getPMID().getvalue())
				.build();
	}

}