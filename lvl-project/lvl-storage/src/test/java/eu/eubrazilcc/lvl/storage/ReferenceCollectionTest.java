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

package eu.eubrazilcc.lvl.storage;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static eu.eubrazilcc.lvl.core.xml.PubMedXmlBinder.PUBMED_XML_FACTORY;
import static eu.eubrazilcc.lvl.storage.dao.ReferenceDAO.ORIGINAL_ARTICLE_KEY;
import static eu.eubrazilcc.lvl.storage.dao.ReferenceDAO.REFERENCE_DAO;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.apache.commons.lang.mutable.MutableLong;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.Reference;
import eu.eubrazilcc.lvl.core.xml.ncbi.pubmed.PubmedArticle;
import eu.eubrazilcc.lvl.storage.dao.WriteResult;

/**
 * Tests reference collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ReferenceCollectionTest {

	@Test
	public void test() {
		System.out.println("ReferenceCollectionTest.test()");
		try {
			// create article citation
			final PubmedArticle article = PUBMED_XML_FACTORY.createPubmedArticle()
					.withMedlineCitation(PUBMED_XML_FACTORY.createMedlineCitation()
							.withArticle(PUBMED_XML_FACTORY.createArticle().withArticleTitle("The best paper in the world")))
							.withMedlineCitation(PUBMED_XML_FACTORY.createMedlineCitation().withPMID(PUBMED_XML_FACTORY.createPMID().withvalue("ABCD1234")));

			// insert
			final Reference reference = Reference.builder()
					.title("The best paper in the world")
					.pubmedId("ABCD1234")
					.publicationYear(1984)
					.seqids(newHashSet("gb:ABC12345678"))
					.article(article)
					.build();			
			WriteResult<Reference> writeResult = REFERENCE_DAO.insert(reference);
			assertThat("insert write result is not null", writeResult, notNullValue());
			assertThat("insert write result Id is not null", writeResult.getId(), notNullValue());
			assertThat("insert write result Id is not empty", isNotBlank(writeResult.getId()), equalTo(true));
			final String dbId = writeResult.getId();

			// insert ignoring duplicates
			writeResult = REFERENCE_DAO.insert(reference, true);
			assertThat("insert write result is not null after duplicate insertion", writeResult, notNullValue());
			assertThat("insert write result Id is not null after duplicate insertion", writeResult.getId(), notNullValue());
			assertThat("insert write result Id is not empty after duplicate insertion", isNotBlank(writeResult.getId()), equalTo(true));
			assertThat("insert write result Id coincides with expected after duplicate insertion", writeResult.getId(), equalTo(dbId));

			try {
				REFERENCE_DAO.insert(reference, false);
				fail("Expected Exception due to duplicate insertion");
			} catch (RuntimeException expected) {
				System.out.println("Expected exception caught: " + expected.getClass());
			}

			// find
			Reference reference2 = REFERENCE_DAO.find(reference.getPubmedId());
			assertThat("reference is not null", reference2, notNullValue());
			assertThat("reference coincides with original", reference2, equalTo(reference));
			assertThat("reference contains original sequence", reference2.getArticle(), notNullValue());
			System.out.println(reference2.toString());

			// list with projection
			List<Reference> references = REFERENCE_DAO.list(0, Integer.MAX_VALUE, null, null, ImmutableMap.of(ORIGINAL_ARTICLE_KEY, false), null);
			assertThat("projected references is not null", references, notNullValue());
			assertThat("number of projected references coincides with expected", references.size(), equalTo(1));
			assertThat("article was filtered from database response", references.get(0).getArticle(), nullValue());

			// update
			reference.setTitle("The second best paper in the world");
			REFERENCE_DAO.update(reference);

			// find after update
			reference2 = REFERENCE_DAO.find(reference.getPubmedId());
			assertThat("reference is not null", reference2, notNullValue());
			assertThat("reference coincides with original", reference2, equalTo(reference));
			System.out.println(reference2.toString());

			// remove
			REFERENCE_DAO.delete(reference.getPubmedId());
			final long numRecords = REFERENCE_DAO.count();
			assertThat("number of references stored in the database coincides with expected", numRecords, equalTo(0l));

			// pagination
			final List<String> ids = newArrayList();
			for (int i = 0; i < 11; i++) {
				final PubmedArticle article3 = PUBMED_XML_FACTORY.createPubmedArticle()
						.withMedlineCitation(PUBMED_XML_FACTORY.createMedlineCitation()
								.withArticle(PUBMED_XML_FACTORY.createArticle().withArticleTitle("Paper number " + i)))
								.withMedlineCitation(PUBMED_XML_FACTORY.createMedlineCitation().withPMID(PUBMED_XML_FACTORY.createPMID().withvalue(Integer.toString(i))));
				final Reference reference3 = Reference.builder()
						.title("Paper number " + i)
						.pubmedId(Integer.toString(i))
						.article(article3)
						.build();
				ids.add(reference3.getPubmedId());
				REFERENCE_DAO.insert(reference3);
			}
			final int size = 3;
			int start = 0;
			references = null;
			final MutableLong count = new MutableLong(0l);
			do {
				references = REFERENCE_DAO.list(start, size, null, null, null, count);
				if (references.size() != 0) {
					System.out.println("Paging: first item " + start + ", showing " + references.size() + " of " + count.getValue() + " items");
				}
				start += references.size();
			} while (!references.isEmpty());
			for (final String id2 : ids) {			
				REFERENCE_DAO.delete(id2);
			}
			REFERENCE_DAO.stats(System.out);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("ReferenceCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("ReferenceCollectionTest.test() has finished");
		}
	}

}