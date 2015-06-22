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
import static eu.eubrazilcc.lvl.core.xml.PubMedXmlBinder.PUBMED_XMLB;
import static eu.eubrazilcc.lvl.core.xml.PubMedXmlBinder.PUBMED_XML_FACTORY;
import static eu.eubrazilcc.lvl.core.xml.XmlHelper.prettyPrint;
import static eu.eubrazilcc.lvl.storage.Citation.PUBMED_KEY;
import static eu.eubrazilcc.lvl.storage.Filter.FilterType.FILTER_COMPARE;
import static eu.eubrazilcc.lvl.storage.Filter.FilterType.FILTER_REGEX;
import static eu.eubrazilcc.lvl.storage.Filter.FilterType.FILTER_TEXT;
import static eu.eubrazilcc.lvl.storage.Filters.LogicalType.LOGICAL_AND;
import static eu.eubrazilcc.lvl.storage.Filters.LogicalType.LOGICAL_OR;
import static eu.eubrazilcc.lvl.storage.LvlObject.LVL_GUID_FIELD;
import static eu.eubrazilcc.lvl.storage.LvlObject.LvlObjectStatus.DRAFT;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoJsonMapper.objectToJson;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoJsonOptions.JSON_PRETTY_PRINTER;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;

import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;
import eu.eubrazilcc.lvl.core.xml.ncbi.pubmed.PubmedArticle;
import eu.eubrazilcc.lvl.storage.mongodb.MongoCollectionStats;

/**
 * Tests {@link Citation} collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class CitationCollectionTest {

	public static final long TIMEOUT = 5l;

	@Test
	public void test() {
		System.out.println("CitationCollectionTest.test()");
		try {
			// create geographic objects
			final Point madPoint = Point.builder().coordinates(LngLatAlt.builder().coordinates(-3.7037901999999576d, 40.4167754d).build()).build();
			final Point bcnPoint = Point.builder().coordinates(LngLatAlt.builder().coordinates( 2.1734034999999494d, 41.3850639d).build()).build();
			final Point vlcPoint = Point.builder().coordinates(LngLatAlt.builder().coordinates(-0.3762881000000107d, 39.4699075d).build()).build();
			final Polygon polygon = Polygon.builder().exteriorRing(
					LngLatAlt.builder().coordinates(-4.0d, 30.0d).build(),
					LngLatAlt.builder().coordinates( 3.0d, 30.0d).build(),
					LngLatAlt.builder().coordinates( 3.0d, 42.0d).build(),
					LngLatAlt.builder().coordinates(-4.0d, 42.0d).build(),
					LngLatAlt.builder().coordinates(-4.0d, 30.0d).build()).build();

			// create original PubMed articles
			final PubmedArticle article1 = PUBMED_XML_FACTORY.createPubmedArticle()
					.withMedlineCitation(PUBMED_XML_FACTORY.createMedlineCitation()
							.withPMID(PUBMED_XML_FACTORY.createPMID().withvalue("EFGH5678"))
							.withDateCreated(PUBMED_XML_FACTORY.createDateCreated().withYear(PUBMED_XML_FACTORY.createYear().withvalue("2015")))
							.withArticle(PUBMED_XML_FACTORY.createArticle().withPubModel("Electronic-Print").withArticleTitle("The best paper in the world").withJournal(PUBMED_XML_FACTORY.createJournal().withTitle("Journal of Awesomeness")).withAbstract(PUBMED_XML_FACTORY.createAbstract().withAbstractText("This paper presents a text.")).withAuthorList(PUBMED_XML_FACTORY.createAuthorList().withAuthor(PUBMED_XML_FACTORY.createAuthor().withLastNameOrForeNameOrInitialsOrSuffixOrNameIDOrCollectiveName("John Doe"))).withPublicationTypeList(PUBMED_XML_FACTORY.createPublicationTypeList().withPublicationType(PUBMED_XML_FACTORY.createPublicationType().withvalue("Journal Article"))).withLanguage(PUBMED_XML_FACTORY.createLanguage().withvalue("eng")).withArticleDate(PUBMED_XML_FACTORY.createArticleDate().withYear(PUBMED_XML_FACTORY.createYear().withvalue("2015")))));
			/* Uncomment for additional output */
			System.out.println(" >> Original PubMed article:\n" + prettyPrint(PUBMED_XMLB.typeToXml(article1)));			

			final PubmedArticle article2 = PUBMED_XML_FACTORY.createPubmedArticle()
					.withMedlineCitation(PUBMED_XML_FACTORY.createMedlineCitation()
							.withPMID(PUBMED_XML_FACTORY.createPMID().withvalue("ABCD1234"))
							.withDateCreated(PUBMED_XML_FACTORY.createDateCreated().withYear(PUBMED_XML_FACTORY.createYear().withvalue("2014")))
							.withArticle(PUBMED_XML_FACTORY.createArticle().withPubModel("Electronic-Print").withArticleTitle("Rocket science rocks").withJournal(PUBMED_XML_FACTORY.createJournal().withTitle("Journal of Awesomeness")).withAbstract(PUBMED_XML_FACTORY.createAbstract().withAbstractText("It rocks! There is no much to say.")).withAuthorList(PUBMED_XML_FACTORY.createAuthorList().withAuthor(PUBMED_XML_FACTORY.createAuthor().withLastNameOrForeNameOrInitialsOrSuffixOrNameIDOrCollectiveName("Jane Doe"))).withPublicationTypeList(PUBMED_XML_FACTORY.createPublicationTypeList().withPublicationType(PUBMED_XML_FACTORY.createPublicationType().withvalue("Journal Article"))).withLanguage(PUBMED_XML_FACTORY.createLanguage().withvalue("eng")).withArticleDate(PUBMED_XML_FACTORY.createArticleDate().withYear(PUBMED_XML_FACTORY.createYear().withvalue("2014")))));

			// insert new citation in the database
			final Citation citation1 = Citation.builder()
					.lvlId("CITATION_1")
					.lvl(LvlCitation.builder().cited(newArrayList("SEQ1", "SEQ2")).build())
					.pubmed(article1)
					.location(madPoint)
					.status(DRAFT)
					.build(); // TODO : include provenance
			citation1.save().get(TIMEOUT, SECONDS);
			assertThat("inserted object Id is not null", citation1.getDbId(), notNullValue());
			String dbId = citation1.getDbId().toString();
			assertThat("inserted Id is not null", dbId, notNullValue());
			assertThat("inserted Id is not empty", isNotBlank(dbId), equalTo(true));
			/* Uncomment for additional output */
			System.out.println(" >> Inserted citation:\n" + citation1.toJson(JSON_PRETTY_PRINTER));

			// find citation by global id
			Citation citation3 = Citation.builder().lvlId("CITATION_1").build();
			citation3.fetch().get(TIMEOUT, SECONDS);
			assertThat("inserted last modified field is not null", citation3.getLastModified(), notNullValue());
			citation1.setLastModified(citation3.getLastModified());
			assertThat("fetched citation coincides with expected", citation3, equalTo(citation1));
			/* Uncomment for additional output */
			System.out.println(" >> Fetched citation:\n" + citation3.toJson(JSON_PRETTY_PRINTER));

			// insert a second citation in the database
			final Citation citation2 = Citation.builder()
					.lvlId("CITATION_2")
					.pubmed(article2)
					.location(bcnPoint)
					.build();
			citation2.save().get(TIMEOUT, SECONDS);
			assertThat("inserted object Id is not null", citation2.getDbId(), notNullValue());
			dbId = citation2.getDbId().toString();
			assertThat("inserted Id is not null", dbId, notNullValue());
			assertThat("inserted Id is not empty", isNotBlank(dbId), equalTo(true));
			/* Uncomment for additional output */
			System.out.println(" >> Inserted citation:\n" + citation2.toJson(JSON_PRETTY_PRINTER));

			// search by proximity
			final Citations citations = new Citations();
			FeatureCollection features = citations.getNear(vlcPoint, 0.0d, 1000000.0d).get(TIMEOUT, SECONDS);
			assertThat("feature collection is not null", features, notNullValue());
			assertThat("feature collection list is not null", features.getFeatures(), notNullValue());
			assertThat("feature collection list is not empty", features.getFeatures().isEmpty(), equalTo(false));
			assertThat("number of features coincides with expected", features.getFeatures().size(), equalTo(2));			
			/* Uncomment for additional output */
			System.out.println(" >> Feature collection (geoNear):\n" + objectToJson(features, JSON_PRETTY_PRINTER));

			features = citations.getNear(vlcPoint, 303000.0d, 305000.0d).get(TIMEOUT, SECONDS);
			assertThat("feature collection is not null", features, notNullValue());
			assertThat("feature collection list is not null", features.getFeatures(), notNullValue());
			assertThat("feature collection list is not empty", features.getFeatures().isEmpty(), equalTo(false));
			assertThat("number of features coincides with expected", features.getFeatures().size(), equalTo(1));			
			/* Uncomment for additional output */
			System.out.println(" >> Feature collection (geoNear):\n" + objectToJson(features, JSON_PRETTY_PRINTER));

			// find elements within a polygon
			features = citations.getWithin(polygon).get(TIMEOUT, SECONDS);
			assertThat("feature collection is not null", features, notNullValue());
			assertThat("feature collection list is not null", features.getFeatures(), notNullValue());
			assertThat("feature collection list is not empty", features.getFeatures().isEmpty(), equalTo(false));
			assertThat("number of features coincides with expected", features.getFeatures().size(), equalTo(2));			
			/* Uncomment for additional output */
			System.out.println(" >> Feature collection (getWithin):\n" + objectToJson(features, JSON_PRETTY_PRINTER));

			// type ahead
			final List<String> values = citations.typeahead(LVL_GUID_FIELD, "ion_", 10).get(TIMEOUT, SECONDS);
			assertThat("typeahead values are not null", values, notNullValue());
			assertThat("typeahead values are not empty", values.isEmpty(), equalTo(false));
			assertThat("number of typeahead values coincides with expected", values.size(), equalTo(2));			
			/* Uncomment for additional output */
			System.out.println(" >> Typeahead:\n" + objectToJson(values, JSON_PRETTY_PRINTER));

			// list with all properties			
			int count = citations.fetch(0, Integer.MAX_VALUE, null, null, null).get(TIMEOUT, SECONDS);
			assertThat("number of fetched elements coincides with expected", count, equalTo(2));
			assertThat("number of fetched elements coincides with expected", citations.size(), equalTo(count));
			/* Uncomment for additional output */
			System.out.println(" >> Fetched citations:\n" + citations.toJson(JSON_PRETTY_PRINTER));

			// list with filters (equal)
			Filters filters = Filters.builder()
					.filters(newArrayList(Filter.builder().type(FILTER_COMPARE).fieldName(PUBMED_KEY).value("=EFGH5678").build()))
					.build();
			count = citations.fetch(0, Integer.MAX_VALUE, filters, null, null).get(TIMEOUT, SECONDS);
			assertThat("number of fetched elements (with filter) coincides with expected", count, equalTo(1));
			assertThat("number of fetched elements (with filter) coincides with expected", citations.size(), equalTo(count));
			assertThat("found citation coincides with expected", citations.get(0).getLvlId(), equalTo(citation1.getLvlId()));

			// list with filters (regular expression)
			filters = Filters.builder()
					.filters(newArrayList(Filter.builder().type(FILTER_REGEX).fieldName(LVL_GUID_FIELD).value("(?i)([a-z])*_2").build()))
					.build();
			count = citations.fetch(0, Integer.MAX_VALUE, filters, null, null).get(TIMEOUT, SECONDS);
			assertThat("number of fetched elements (with filter) coincides with expected", count, equalTo(1));
			assertThat("number of fetched elements (with filter) coincides with expected", citations.size(), equalTo(count));
			assertThat("found citation coincides with expected", citations.get(0).getLvlId(), equalTo(citation2.getLvlId()));

			// list with filters (text)
			filters = Filters.builder()
					.filters(newArrayList(Filter.builder().type(FILTER_TEXT).fieldName("pubmed.medlineCitation.article.articleTitle").value("rock").build()))
					.build();
			count = citations.fetch(0, Integer.MAX_VALUE, filters, null, null).get(TIMEOUT, SECONDS);
			assertThat("number of fetched elements (with filter) coincides with expected", count, equalTo(1));
			assertThat("number of fetched elements (with filter) coincides with expected", citations.size(), equalTo(count));
			assertThat("found citation coincides with expected", citations.get(0).getLvlId(), equalTo(citation2.getLvlId()));

			// list with filters (compare)
			filters = Filters.builder()
					.filters(newArrayList(Filter.builder().type(FILTER_COMPARE).fieldName("pubmed.medlineCitation.dateCreated.year.value").value(">=2015").build()))
					.build();
			count = citations.fetch(0, Integer.MAX_VALUE, filters, null, null).get(TIMEOUT, SECONDS);
			assertThat("number of fetched elements (with filter) coincides with expected", count, equalTo(1));
			assertThat("number of fetched elements (with filter) coincides with expected", citations.size(), equalTo(count));
			assertThat("found citation coincides with expected", citations.get(0).getLvlId(), equalTo(citation1.getLvlId()));			

			// list with filters (logical AND)
			filters = Filters.builder()
					.filters(newArrayList(Filter.builder().type(FILTER_TEXT).fieldName("pubmed.medlineCitation.article.abstract.abstractText").value("paper").build(), Filter.builder().type(FILTER_COMPARE).fieldName("pubmed.medlineCitation.pmid.value").value("= EFGH5678  ").build()))
					.type(LOGICAL_AND)
					.build();
			count = citations.fetch(0, Integer.MAX_VALUE, filters, null, null).get(TIMEOUT, SECONDS);
			assertThat("number of fetched elements (with filter) coincides with expected", count, equalTo(1));
			assertThat("number of fetched elements (with filter) coincides with expected", citations.size(), equalTo(count));
			assertThat("found citation coincides with expected", citations.get(0).getLvlId(), equalTo(citation1.getLvlId()));

			// list with filters (logical OR)			
			filters = Filters.builder()
					.filters(newArrayList(Filter.builder().type(FILTER_COMPARE).fieldName("pubmed.medlineCitation.pmid.value").value("= ABCD1234").build(), Filter.builder().type(FILTER_COMPARE).fieldName("pubmed.medlineCitation.pmid.value").value(" = EFGH5678").build()))
					.type(LOGICAL_OR)
					.build();
			count = citations.fetch(0, Integer.MAX_VALUE, filters, null, null).get(TIMEOUT, SECONDS);
			assertThat("number of fetched elements (with filter) coincides with expected", count, equalTo(2));
			assertThat("number of fetched elements (with filter) coincides with expected", citations.size(), equalTo(count));

			// list with projection
			count = citations.fetch(0, Integer.MAX_VALUE, null, null, ImmutableMap.<String, Boolean>builder()
					.put("lvl", true)
					.put("namespace", true)
					.put("lvlId", true)
					.put("location", true)
					.put("status", true)
					.put("pubmed.medlineCitation.article.articleTitle", true)
					.put("pubmed.medlineCitation.pmid.value", true)
					.build()).get(TIMEOUT, SECONDS);
			assertThat("number of fetched elements (with projection) coincides with expected", count, equalTo(2));
			assertThat("number of fetched elements (with projection) coincides with expected", citations.size(), equalTo(count));
			assertThat("original article details were filtered from database response", citations.get(0).getPubmed().getMedlineCitation()
					.getArticle().getAbstract(), nullValue());
			/* Uncomment for additional output */
			System.out.println(" >> Fetched citations (with projection):\n" + citations.toJson(JSON_PRETTY_PRINTER));			

			// list with sorting
			count = citations.fetch(0, Integer.MAX_VALUE, null, ImmutableMap.of(LVL_GUID_FIELD, true), null).get(TIMEOUT, SECONDS);
			assertThat("number of fetched elements (with sorting) coincides with expected", count, equalTo(2));
			assertThat("number of fetched elements (with sorting) coincides with expected", citations.size(), equalTo(count));
			assertThat("first citation coincides with expected", citations.get(0).getLvlId(), equalTo(citation2.getLvlId()));

			count = citations.fetch(0, Integer.MAX_VALUE, null, ImmutableMap.of(PUBMED_KEY, false), null).get(TIMEOUT, SECONDS);
			assertThat("number of fetched elements (with sorting) coincides with expected", count, equalTo(2));
			assertThat("number of fetched elements (with sorting) coincides with expected", citations.size(), equalTo(count));
			assertThat("first citation coincides with expected", citations.get(0).getLvlId(), equalTo(citation2.getLvlId()));

			// list with all features (filter, projection and sorting)
			filters = Filters.builder()
					.filters(newArrayList(Filter.builder().type(FILTER_REGEX).fieldName(LVL_GUID_FIELD).value("(?i)([a-z])*_([0-9]){1,2}").build()))
					.build();
			count = citations.fetch(0, Integer.MAX_VALUE, filters, ImmutableMap.of(LVL_GUID_FIELD, false), ImmutableMap.of("pubmed", false, "lvl", false)).get(TIMEOUT, SECONDS);
			assertThat("number of fetched elements (with all features) coincides with expected", count, equalTo(2));
			assertThat("number of fetched elements (with all features) coincides with expected", citations.size(), equalTo(count));
			assertThat("original article was filtered from database response", citations.get(0).getPubmed(), nullValue());
			assertThat("additional annotations were filtered from database response", citations.get(0).getLvl(), nullValue());
			assertThat("first citation coincides with expected", citations.get(0).getLvlId(), equalTo(citation1.getLvlId()));			

			// update the citation
			citation1.approve().save().get(TIMEOUT, SECONDS);

			// find after update
			citation3 = Citation.builder().lvlId("CITATION_1").build();
			citation3.fetch().get(TIMEOUT, SECONDS);
			assertThat("inserted last modified field is not null", citation3.getLastModified(), notNullValue());
			citation1.setLastModified(citation3.getLastModified());
			assertThat("fetched citation (after update) coincides with expected", citation3, equalTo(citation1));
			/* Uncomment for additional output */
			System.out.println(" >> Fetched citation (after update):\n" + citation3.toJson(JSON_PRETTY_PRINTER));

			// remove
			citation1.delete().get(TIMEOUT, SECONDS);			
			long totalCount = citations.totalCount().get(TIMEOUT, SECONDS).longValue();			
			assertThat("number of elements stored in the database coincides with expected", totalCount, equalTo(1l));

			// pagination
			final List<String> ids = newArrayList();
			for (int i = 0; i < 11; i++) {
				final PubmedArticle articleX = PUBMED_XML_FACTORY.createPubmedArticle()
						.withMedlineCitation(PUBMED_XML_FACTORY.createMedlineCitation()
								.withPMID(PUBMED_XML_FACTORY.createPMID().withvalue(Integer.toString(i)))
								.withArticle(PUBMED_XML_FACTORY.createArticle().withPubModel("Electronic-Print").withArticleTitle("Paper number " + i).withJournal(PUBMED_XML_FACTORY.createJournal().withTitle("Journal of Awesomeness")).withAbstract(PUBMED_XML_FACTORY.createAbstract().withAbstractText("Abstract.")).withAuthorList(PUBMED_XML_FACTORY.createAuthorList().withAuthor(PUBMED_XML_FACTORY.createAuthor().withLastNameOrForeNameOrInitialsOrSuffixOrNameIDOrCollectiveName("Jane Doe"))).withPublicationTypeList(PUBMED_XML_FACTORY.createPublicationTypeList().withPublicationType(PUBMED_XML_FACTORY.createPublicationType().withvalue("Journal Article"))).withLanguage(PUBMED_XML_FACTORY.createLanguage().withvalue("eng")).withArticleDate(PUBMED_XML_FACTORY.createArticleDate().withYear(PUBMED_XML_FACTORY.createYear().withvalue("2013")))));						
				final Citation citationX = Citation.builder()
						.lvlId("X_" + i)
						.pubmed(articleX)
						.build();			
				ids.add(citationX.getLvlId());
				citationX.save().get(TIMEOUT, SECONDS);
			}
			final int size = 3;
			int start = 0;
			do {
				citations.fetch(start, size, null, null, null).get(TIMEOUT, SECONDS);				
				if (citations.size() != 0) {
					System.out.println("Paging: first item " + start + ", showing " + citations.size() + " of " + citations.getTotalCount() + " items");
				}
				start += citations.size();
			} while (!citations.getElements().isEmpty());

			// delete all the elements inserted in the previous test (pagination)
			for (int i = 0; i < ids.size(); i++) {
				Citation.builder().lvlId(ids.get(i)).build().delete().get(TIMEOUT, SECONDS);
			}			
			count = citations.fetch(0, Integer.MAX_VALUE, null, null, null).get(TIMEOUT, SECONDS);
			assertThat("number of fetched elements coincides with expected", count, equalTo(1));

			// collect statistics about the collection
			final ListenableFuture<MongoCollectionStats> statsFuture = citations.stats();
			final MongoCollectionStats stats = statsFuture.get(TIMEOUT, SECONDS);
			assertThat("collection statistics are not null", stats, notNullValue());			
			System.out.println(" >> Collection statistics:\n" + objectToJson(stats, JSON_PRETTY_PRINTER));

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("CitationCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("CitationCollectionTest.test() has finished");
		}
	}

}