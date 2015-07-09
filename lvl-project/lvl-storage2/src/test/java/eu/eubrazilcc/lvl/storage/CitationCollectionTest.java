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

import static com.google.common.collect.Iterables.frequency;
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
import static eu.eubrazilcc.lvl.storage.base.CollectionOperators.allOperator;
import static eu.eubrazilcc.lvl.storage.base.CollectionOperators.releasesOperator;
import static eu.eubrazilcc.lvl.storage.base.LvlObject.LVL_GUID_FIELD;
import static eu.eubrazilcc.lvl.storage.base.ObjectState.DRAFT;
import static eu.eubrazilcc.lvl.storage.base.ObjectState.OBSOLETE;
import static eu.eubrazilcc.lvl.storage.base.ObjectState.RELEASE;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoJsonMapper.JSON_MAPPER;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoJsonMapper.objectToJson;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoJsonOptions.JSON_PRETTY_PRINTER;
import static eu.eubrazilcc.lvl.storage.base.DeleteOptions.DELETE_ALL;
import static eu.eubrazilcc.lvl.storage.base.DeleteOptions.ON_DELETE_CASCADE;
import static eu.eubrazilcc.lvl.storage.prov.ProvFactory.newObjectImportProv;
import static eu.eubrazilcc.lvl.storage.prov.ProvFactory.newPubMedArticle;
import static eu.eubrazilcc.lvl.storage.prov.ProvFactory.newGeocoding;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.hamcrest.number.OrderingComparison.lessThanOrEqualTo;
import static org.junit.Assert.fail;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.FixMethodOrder;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;

import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;
import eu.eubrazilcc.lvl.core.xml.ncbi.pubmed.PubmedArticle;
import eu.eubrazilcc.lvl.storage.base.CollectionOperator;
import eu.eubrazilcc.lvl.storage.base.LvlObject;
import eu.eubrazilcc.lvl.storage.base.ObjectState;
import eu.eubrazilcc.lvl.storage.mongodb.MongoCollectionStats;

/**
 * Tests {@link Citation} collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
@FixMethodOrder(NAME_ASCENDING)
public class CitationCollectionTest {

	public static final long TIMEOUT = 5l;

	public static final String ID_0 = "CITATION_0";
	public static final String ID_1 = "CITATION_1";

	/**
	 * Tests creation, modification and search of objects which state is draft.
	 */
	@Test
	public void test01DraftState() {
		System.out.println("CitationCollectionTest.test01DraftState()");
		try {
			// create test data
			final TestDataset ds = new TestDataset(true);			

			// insert test data-set into the database
			insertTestDatasets(ds);

			// operate on the test data-set
			final Resultset rs = new Resultset(new TestScenario[]{
					new TestScenario("TestDraft", TestState.ALL, new ObjectState[] { DRAFT, DRAFT }),
					new TestScenario("TestDraft", TestState.RELEASES, new ObjectState[] { DRAFT, DRAFT })
			});
			operateOnTestDatasets(ds, rs, true);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("CitationCollectionTest.test01DraftState() failed: " + e.getMessage());
		} finally {
			try {
				new Citations().drop().get(TIMEOUT, SECONDS);
			} catch (Exception ignore) { }
			System.out.println("CitationCollectionTest.test01DraftState() has finished");
		}
	}

	/**
	 * Tests creation, modification and search of objects which state is release.
	 */
	@Test
	public void test02ReleaseState() {
		try {
			System.out.println("CitationCollectionTest.test02ReleaseState()");

			// create test data
			final TestDataset ds = new TestDataset(false);			

			// insert test data-set into the database
			insertTestDatasets(ds);	

			// approve and save the release to the database			
			final Date lastModified = ds.citation0.getLastModified();
			final String dbId = ds.citation0.getDbId();
			ds.citation0.approve().save().get(TIMEOUT, SECONDS);
			assertThat("saved Id is not empty", trim(ds.citation0.getDbId()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("saved version is not empty", trim(ds.citation0.getVersion()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("saved Id is different in the new version", ds.citation0.getDbId(), not(equalTo(dbId)));
			assertThat("last modified field is not null", ds.citation0.getLastModified(), notNullValue());
			assertThat("updated last modified value is in the future", ds.citation0.getLastModified().after(lastModified), equalTo(true));			
			// Uncomment for additional output
			System.out.println(" >> Saved citation (" + ds.citation0.getDbId() + "):\n" + ds.citation0.toJson(JSON_PRETTY_PRINTER));

			// find citation after update
			ds.citation2 = Citation.builder().lvlId(ID_0).build();
			ds.citation2.fetch().get(TIMEOUT, SECONDS);			
			assertThat("fetched citation (after update) coincides with expected", ds.citation2, equalTo(ds.citation0));
			// Uncomment for additional output
			System.out.println(" >> Fetched citation (after update, " + ds.citation2.getDbId() + "):\n" + ds.citation2.toJson(JSON_PRETTY_PRINTER));

			// saving a draft after a release should fail
			try {
				ds.citation2.setState(DRAFT);
				ds.citation2.save().get(TIMEOUT, SECONDS);
				fail("Expected exception due to unsupported behavior");
			} catch (Exception expected) {
				assertThat("exception cause is not null", expected.getCause(), notNullValue());
				assertThat("exception cause coincides with expected", expected.getCause() instanceof UnsupportedOperationException, equalTo(true));				
				System.out.println("Expected exception caught: " + expected.getCause().getMessage());
			}

			// operate on the test data-set
			ds.versions.put(ID_0, 2);
			final Resultset rs = new Resultset(new TestScenario[]{
					new TestScenario("TestRelease", TestState.ALL, new ObjectState[] { RELEASE, DRAFT }),
					new TestScenario("TestRelease", TestState.RELEASES, new ObjectState[] { RELEASE, DRAFT })
			});
			operateOnTestDatasets(ds, rs, false);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("CitationCollectionTest.test02ReleaseState() failed: " + e.getMessage());
		} finally {
			try {
				new Citations().drop().get(TIMEOUT, SECONDS);
			} catch (Exception ignore) { }
			System.out.println("CitationCollectionTest.test02ReleaseState() has finished");
		}
	}

	/**
	 * Tests creation, modification and search of objects which state is obsolete.
	 */
	@Test
	public void test03ObsoleteState() {
		try {
			System.out.println("CitationCollectionTest.test03ObsoleteState()");

			// create test data
			final TestDataset ds = new TestDataset(false);			

			// insert test data-set into the database
			insertTestDatasets(ds);

			// invalidate and save the object to the database			
			final Date lastModified = ds.citation0.getLastModified();
			final String dbId = ds.citation0.getDbId();
			ds.citation0.invalidate().save().get(TIMEOUT, SECONDS);
			assertThat("saved Id is not empty", trim(ds.citation0.getDbId()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("saved version is not empty", trim(ds.citation0.getVersion()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("saved Id is different in the new version", ds.citation0.getDbId(), not(equalTo(dbId)));
			assertThat("last modified field is not null", ds.citation0.getLastModified(), notNullValue());
			assertThat("updated last modified value is in the future", ds.citation0.getLastModified().after(lastModified), equalTo(true));
			// Uncomment for additional output
			System.out.println(" >> Saved citation (" + ds.citation0.getDbId() + "):\n" + ds.citation0.toJson(JSON_PRETTY_PRINTER));			

			// find citation after invalidation
			ds.citation2 = Citation.builder().lvlId(ID_0).build();
			ds.citation2.fetch().get(TIMEOUT, SECONDS);			
			assertThat("fetched citation (after invalidation) coincides with expected", ds.citation2, equalTo(ds.citation0));
			// Uncomment for additional output
			System.out.println(" >> Fetched citation (after invalidation, " + ds.citation2.getDbId() + "):\n" + ds.citation2.toJson(JSON_PRETTY_PRINTER));

			// saving a draft after invalidation should fail
			try {
				ds.citation2.setState(DRAFT);
				ds.citation2.save().get(TIMEOUT, SECONDS);
				fail("Expected exception due to unsupported behavior");
			} catch (Exception expected) {
				assertThat("exception cause is not null", expected.getCause(), notNullValue());
				assertThat("exception cause coincides with expected", expected.getCause() instanceof UnsupportedOperationException, equalTo(true));				
				System.out.println("Expected exception caught: " + expected.getCause().getMessage());
			}

			// operate on the test data-set
			ds.versions.put(ID_0, 2);
			final Resultset rs = new Resultset(new TestScenario[]{
					new TestScenario("TestObsolete", TestState.ALL, new ObjectState[] { OBSOLETE, DRAFT }),
					new TestScenario("TestObsolete", TestState.RELEASES, new ObjectState[] { OBSOLETE, DRAFT })
			});
			operateOnTestDatasets(ds, rs, false);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("CitationCollectionTest.test03ObsoleteState() failed: " + e.getMessage());
		} finally {
			try {
				new Citations().drop().get(TIMEOUT, SECONDS);
			} catch (Exception ignore) { }
			System.out.println("CitationCollectionTest.test03ObsoleteState() has finished");
		}
	}

	/**
	 * Tests creation, modification and search of objects that go through the complete life-cycle.
	 */
	@Test
	public void test04CompleteLifeCycle() {
		try {
			System.out.println("CitationCollectionTest.test04CompleteLifeCycle()");

			// create test data
			final TestDataset ds = new TestDataset(false);			

			// insert test data-set into the database
			insertTestDatasets(ds);

			// approve and save the release to the database			
			Date lastModified = ds.citation0.getLastModified();
			String dbId = ds.citation0.getDbId();
			ds.citation0.approve().save().get(TIMEOUT, SECONDS);
			assertThat("saved Id is not empty", trim(ds.citation0.getDbId()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("saved version is not empty", trim(ds.citation0.getVersion()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("saved Id is different in the new version", ds.citation0.getDbId(), not(equalTo(dbId)));
			assertThat("last modified field is not null", ds.citation0.getLastModified(), notNullValue());
			assertThat("updated last modified value is in the future", ds.citation0.getLastModified().after(lastModified), equalTo(true));			
			// Uncomment for additional output
			System.out.println(" >> Saved citation (" + ds.citation0.getDbId() + "):\n" + ds.citation0.toJson(JSON_PRETTY_PRINTER));

			// find citation after update
			ds.citation2 = Citation.builder().lvlId(ID_0).build();
			ds.citation2.fetch().get(TIMEOUT, SECONDS);			
			assertThat("fetched citation (after update) coincides with expected", ds.citation2, equalTo(ds.citation0));
			// Uncomment for additional output
			System.out.println(" >> Fetched citation (after update, " + ds.citation2.getDbId() + "):\n" + ds.citation2.toJson(JSON_PRETTY_PRINTER));

			// saving a draft after a release should fail
			try {
				ds.citation2.setState(DRAFT);
				ds.citation2.save().get(TIMEOUT, SECONDS);
				fail("Expected exception due to unsupported behavior");
			} catch (Exception expected) {
				assertThat("exception cause is not null", expected.getCause(), notNullValue());
				assertThat("exception cause coincides with expected", expected.getCause() instanceof UnsupportedOperationException, equalTo(true));				
				System.out.println("Expected exception caught: " + expected.getCause().getMessage());
			}

			// invalidate and save the object to the database			
			lastModified = ds.citation0.getLastModified();
			dbId = ds.citation0.getDbId();
			ds.citation0.invalidate().save().get(TIMEOUT, SECONDS);
			assertThat("saved Id is not empty", trim(ds.citation0.getDbId()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("saved version is not empty", trim(ds.citation0.getVersion()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("saved Id is different in the new version", ds.citation0.getDbId(), not(equalTo(dbId)));
			assertThat("last modified field is not null", ds.citation0.getLastModified(), notNullValue());
			assertThat("updated last modified value is in the future", ds.citation0.getLastModified().after(lastModified), equalTo(true));
			// Uncomment for additional output
			System.out.println(" >> Saved citation (" + ds.citation0.getDbId() + "):\n" + ds.citation0.toJson(JSON_PRETTY_PRINTER));			

			// find citation after invalidation
			ds.citation2 = Citation.builder().lvlId(ID_0).build();
			ds.citation2.fetch().get(TIMEOUT, SECONDS);			
			assertThat("fetched citation (after invalidation) coincides with expected", ds.citation2, equalTo(ds.citation0));
			// Uncomment for additional output
			System.out.println(" >> Fetched citation (after invalidation, " + ds.citation2.getDbId() + "):\n" + ds.citation2.toJson(JSON_PRETTY_PRINTER));

			// saving a draft after invalidation should fail
			try {
				ds.citation2.setState(DRAFT);
				ds.citation2.save().get(TIMEOUT, SECONDS);
				fail("Expected exception due to unsupported behavior");
			} catch (Exception expected) {
				assertThat("exception cause is not null", expected.getCause(), notNullValue());
				assertThat("exception cause coincides with expected", expected.getCause() instanceof UnsupportedOperationException, equalTo(true));				
				System.out.println("Expected exception caught: " + expected.getCause().getMessage());
			}

			// operate on the test data-set
			ds.versions.put(ID_0, 3);
			final Resultset rs = new Resultset(new TestScenario[]{
					new TestScenario("TestLifecycle", TestState.ALL, new ObjectState[] { OBSOLETE, DRAFT }),
					new TestScenario("TestLifecycle", TestState.RELEASES, new ObjectState[] { OBSOLETE, DRAFT })
			});
			operateOnTestDatasets(ds, rs, false);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("CitationCollectionTest.test04CompleteLifeCycle() failed: " + e.getMessage());
		} finally {
			try {
				new Citations().drop().get(TIMEOUT, SECONDS);
			} catch (Exception ignore) { }
			System.out.println("CitationCollectionTest.test04CompleteLifeCycle() has finished");
		}
	}

	/**
	 * Test JSON mapping using the global object mapper.
	 */
	@Test
	public void test05JsonMapping() {
		System.out.println("CitationCollectionTest.test05JsonMapping()");
		try {
			// create test data-set
			final TestDataset ds = new TestDataset(false);

			// insert test data-set into the database
			insertTestDatasets(ds);	

			ds.citation2 = Citation.builder().lvlId(ID_0).build();
			ds.citation2.fetch().get(TIMEOUT, SECONDS);
			assertThat("retrieved object is not null", ds.citation2, notNullValue());

			// test Java object to JSON serialization
			final String payload = ds.citation2.toJson(JSON_PRETTY_PRINTER);			
			assertThat("serialized JSON object is not null", payload, notNullValue());
			assertThat("serialized JSON object is not empty", isNotBlank(payload), equalTo(true));
			/* uncomment for additional output */
			System.out.println(" >> Serialized JSON object:\n" + payload);

			// test JSON to Java object deserialization			
			final Citation citation2 = JSON_MAPPER.readValue(payload, Citation.class);
			assertThat("deserialized Java object is not null", citation2, notNullValue());
			assertThat("deserialized Java object coincides with expected", citation2, equalTo(ds.citation2));

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("CitationCollectionTest.test05JsonMapping() failed: " + e.getMessage());
		} finally {
			try {
				new Citations().drop().get(TIMEOUT, SECONDS);
			} catch (Exception ignore) { }
			System.out.println("CitationCollectionTest.test05JsonMapping() has finished");
		}
	}

	private void insertTestDatasets(final TestDataset ds) throws Exception {
		// insert new citation in the database
		ds.citation0.save().get(TIMEOUT, SECONDS);
		assertThat("inserted Id is not empty", trim(ds.citation0.getDbId()), allOf(notNullValue(), not(equalTo(""))));
		assertThat("inserted version is not empty", trim(ds.citation0.getVersion()), allOf(notNullValue(), not(equalTo(""))));
		assertThat("last modified field is not null", ds.citation0.getLastModified(), notNullValue());
		/* Uncomment for additional output */
		System.out.println(" >> Inserted citation (" + ds.citation0.getDbId() + "):\n" + ds.citation0.toJson(JSON_PRETTY_PRINTER));

		// find citation by global id
		ds.citation2 = Citation.builder().lvlId(ID_0).build();
		ds.citation2.fetch().get(TIMEOUT, SECONDS);
		assertThat("fetched citation coincides with expected", ds.citation2, equalTo(ds.citation0));
		/* Uncomment for additional output */
		System.out.println(" >> Fetched citation (" + ds.citation2.getDbId() + "):\n" + ds.citation2.toJson(JSON_PRETTY_PRINTER));

		// insert a second citation in the database
		ds.citation1.save().get(TIMEOUT, SECONDS);
		assertThat("inserted Id is not empty", trim(ds.citation1.getDbId()), allOf(notNullValue(), not(equalTo(""))));
		assertThat("inserted version is not empty", trim(ds.citation1.getVersion()), allOf(notNullValue(), not(equalTo(""))));
		assertThat("last modified field is not null", ds.citation1.getLastModified(), notNullValue());
		/* Uncomment for additional output */
		System.out.println(" >> Inserted citation (" + ds.citation1.getDbId() + "):\n" + ds.citation1.toJson(JSON_PRETTY_PRINTER));

		System.out.println("CitationCollectionTest.insertTestDatasets() has finished");
	}

	private void operateOnTestDatasets(final TestDataset ds, final Resultset rs, final boolean testUpdate) throws Exception {
		CollectionOperator<Citation> op = null;
		FeatureCollection features = null;
		int count = 0;
		for (final TestScenario scenario : rs.scenarios) {
			// setup collection
			switch (scenario.state) {
			case ALL:
				op = allOperator(ds.citations);
				break;
			case RELEASES:
				op = releasesOperator(ds.citations);
				break;
			default:
				throw new IllegalStateException("Unknown test state: " + scenario.state);
			}

			// search by proximity
			System.out.println(" >> Search by proximity: " + scenario.toString());
			features = op.getNear(ds.vlcPoint, 0.0d, 1000000.0d).get(TIMEOUT, SECONDS);
			assertThat("feature collection is not null", features, notNullValue());
			assertThat("feature collection list is not null", features.getFeatures(), notNullValue());
			assertThat("feature collection list is not empty", features.getFeatures().isEmpty(), equalTo(scenario.numItems() == 0));
			assertThat("number of features coincides with expected", features.getFeatures().size(), equalTo(scenario.numItems()));
			/* Uncomment for additional output */
			System.out.println(" >> Feature collection (geoNear):\n" + objectToJson(features, JSON_PRETTY_PRINTER));

			features = op.getNear(ds.vlcPoint, 303000.0d, 305000.0d).get(TIMEOUT, SECONDS);
			assertThat("feature collection is not null", features, notNullValue());
			assertThat("feature collection list is not null", features.getFeatures(), notNullValue());
			assertThat("feature collection list is not empty", features.getFeatures().isEmpty(), equalTo(scenario.numItems(1) == 0));
			assertThat("number of features coincides with expected", features.getFeatures().size(), equalTo(scenario.numItems(1)));
			/* Uncomment for additional output */
			System.out.println(" >> Feature collection (geoNear):\n" + objectToJson(features, JSON_PRETTY_PRINTER));

			// find elements within a polygon
			System.out.println(" >> Find elements within a polygon: " + scenario.toString());
			features = op.getWithin(ds.polygon).get(TIMEOUT, SECONDS);
			assertThat("feature collection is not null", features, notNullValue());
			assertThat("feature collection list is not null", features.getFeatures(), notNullValue());
			assertThat("feature collection list is not empty", features.getFeatures().isEmpty(), equalTo(scenario.numItems() == 0));
			assertThat("number of features coincides with expected", features.getFeatures().size(), equalTo(scenario.numItems()));			
			/* Uncomment for additional output */
			System.out.println(" >> Feature collection (getWithin):\n" + objectToJson(features, JSON_PRETTY_PRINTER));

			// type ahead
			System.out.println(" >> Typeahead: " + scenario.toString());
			final List<String> values = op.typeahead(LVL_GUID_FIELD, "ion_", 10).get(TIMEOUT, SECONDS);
			assertThat("typeahead values are not null", values, notNullValue());
			assertThat("typeahead values are not empty", values.isEmpty(), equalTo(scenario.numItems() == 0));
			assertThat("number of typeahead values coincides with expected", values.size(), equalTo(scenario.numItems()));
			// Uncomment for additional output
			System.out.println(" >> Typeahead:\n" + objectToJson(values, JSON_PRETTY_PRINTER));

			// list with all properties
			System.out.println(" >> List with all properties: " + scenario.toString());
			count = op.fetch(0, Integer.MAX_VALUE, null, null, null).get(TIMEOUT, SECONDS);
			assertThat("number of fetched elements coincides with expected", count, equalTo(scenario.numItems()));
			assertThat("number of fetched elements coincides with expected", op.collection().size(), equalTo(count));
			// Uncomment for additional output
			System.out.println(" >> Fetched citations:\n" + op.collection().toJson(JSON_PRETTY_PRINTER));

			// list with filters (equal)
			System.out.println(" >> List with filters (equal): " + scenario.toString());
			Filters filters = Filters.builder()
					.filters(newArrayList(Filter.builder().type(FILTER_COMPARE).fieldName(PUBMED_KEY).value("=EFGH5678").build()))
					.build();
			count = op.fetch(0, Integer.MAX_VALUE, filters, null, null).get(TIMEOUT, SECONDS);
			assertThat("number of fetched elements (with filter) coincides with expected", count, equalTo(scenario.numItems(1)));
			assertThat("number of fetched elements (with filter) coincides with expected", op.collection().size(), equalTo(count));
			if (count > 0) {
				assertThat("found citation coincides with expected", op.collection().get(0).getLvlId(), equalTo(ds.citation0.getLvlId()));
			}

			// list with filters (regular expression)
			System.out.println(" >> List with filters (regular expression): " + scenario.toString());
			filters = Filters.builder()
					.filters(newArrayList(Filter.builder().type(FILTER_REGEX).fieldName(LVL_GUID_FIELD).value("(?i)([a-z])*_0").build()))
					.build();
			count = op.fetch(0, Integer.MAX_VALUE, filters, null, null).get(TIMEOUT, SECONDS);			
			assertThat("number of fetched elements (with filter) coincides with expected", count, equalTo(scenario.numItems(1)));
			assertThat("number of fetched elements (with filter) coincides with expected", op.collection().size(), equalTo(count));
			if (count > 0) {
				assertThat("found citation coincides with expected", op.collection().get(0).getLvlId(), equalTo(ds.citation0.getLvlId()));
			}

			// list with filters (text)
			System.out.println(" >> List with filters (text): " + scenario.toString());
			filters = Filters.builder()
					.filters(newArrayList(Filter.builder().type(FILTER_TEXT).fieldName("pubmed.medlineCitation.article.articleTitle").value("ROCK").build()))
					.build();
			count = op.fetch(0, Integer.MAX_VALUE, filters, null, null).get(TIMEOUT, SECONDS);
			assertThat("number of fetched elements (with filter) coincides with expected", count, equalTo(scenario.numItems(0)));
			assertThat("number of fetched elements (with filter) coincides with expected", op.collection().size(), equalTo(count));
			if (count > 0) {
				assertThat("found citation coincides with expected", op.collection().get(0).getLvlId(), equalTo(ds.citation1.getLvlId()));
			}

			// list with filters (compare)
			System.out.println(" >> List with filters (compare): " + scenario.toString());
			filters = Filters.builder()
					.filters(newArrayList(Filter.builder().type(FILTER_COMPARE).fieldName("pubmed.medlineCitation.dateCreated.year.value").value(">=2015").build()))
					.build();
			count = op.fetch(0, Integer.MAX_VALUE, filters, null, null).get(TIMEOUT, SECONDS);
			assertThat("number of fetched elements (with filter) coincides with expected", count, equalTo(scenario.numItems(1)));
			assertThat("number of fetched elements (with filter) coincides with expected", op.collection().size(), equalTo(count));
			if (count > 0) {
				assertThat("found citation coincides with expected", op.collection().get(0).getLvlId(), equalTo(ds.citation0.getLvlId()));
			}

			// list with filters (logical AND)
			System.out.println(" >> List with filters (logical AND): " + scenario.toString());
			filters = Filters.builder()
					.filters(newArrayList(Filter.builder().type(FILTER_TEXT).fieldName("pubmed.medlineCitation.article.abstract.abstractText").value("paper").build(), Filter.builder().type(FILTER_COMPARE).fieldName("pubmed.medlineCitation.pmid.value").value("= EFGH5678  ").build()))
					.type(LOGICAL_AND)
					.build();
			count = op.fetch(0, Integer.MAX_VALUE, filters, null, null).get(TIMEOUT, SECONDS);
			assertThat("number of fetched elements (with filter) coincides with expected", count, equalTo(scenario.numItems(1)));
			assertThat("number of fetched elements (with filter) coincides with expected", op.collection().size(), equalTo(count));
			if (count > 0) {
				assertThat("found citation coincides with expected", op.collection().get(0).getLvlId(), equalTo(ds.citation0.getLvlId()));
			}

			// list with filters (logical OR)
			System.out.println(" >> List with filters (logical OR): " + scenario.toString());
			filters = Filters.builder()
					.filters(newArrayList(Filter.builder().type(FILTER_COMPARE).fieldName("pubmed.medlineCitation.pmid.value").value("= ABCD1234").build(), Filter.builder().type(FILTER_COMPARE).fieldName("pubmed.medlineCitation.pmid.value").value(" = EFGH5678").build()))
					.type(LOGICAL_OR)
					.build();
			count = op.fetch(0, Integer.MAX_VALUE, filters, null, null).get(TIMEOUT, SECONDS);
			assertThat("number of fetched elements (with filter) coincides with expected", count, equalTo(scenario.numItems()));
			assertThat("number of fetched elements (with filter) coincides with expected", op.collection().size(), equalTo(count));

			// list with projection
			System.out.println(" >> List with projection: " + scenario.toString());
			count = op.fetch(0, Integer.MAX_VALUE, null, null, ImmutableMap.<String, Boolean>builder()
					.put("lvl", true)
					.put("namespace", true)
					.put("lvlId", true)
					.put("location", true)
					.put("status", true)
					.put("pubmed.medlineCitation.article.articleTitle", true)
					.put("pubmed.medlineCitation.pmid.value", true)
					.build()).get(TIMEOUT, SECONDS);
			assertThat("number of fetched elements (with projection) coincides with expected", count, equalTo(scenario.numItems()));
			assertThat("number of fetched elements (with projection) coincides with expected", op.collection().size(), equalTo(count));
			if (count > 0) {
				assertThat("original article details were filtered from database response", op.collection().get(0).getPubmed().getMedlineCitation()
						.getArticle().getAbstract(), nullValue());
			}
			// Uncomment for additional output
			System.out.println(" >> Fetched citations (with projection):\n" + op.collection().toJson(JSON_PRETTY_PRINTER));			

			// list with sorting
			System.out.println(" >> List with sorting: " + scenario.toString());
			count = op.fetch(0, Integer.MAX_VALUE, null, ImmutableMap.of(LVL_GUID_FIELD, true), null).get(TIMEOUT, SECONDS);
			assertThat("number of fetched elements (with sorting) coincides with expected", count, equalTo(scenario.numItems()));
			assertThat("number of fetched elements (with sorting) coincides with expected", op.collection().size(), equalTo(count));
			if (count > 0) {
				String previous = null, current = null;
				for (final Citation citation : op.collection().getElements()) {
					current = citation.getLvlId();
					if (previous != null) {
						assertThat("sorting order coincides with expected", current.compareTo(previous), lessThanOrEqualTo(0));
					}
					previous = current;
				}
			}

			count = op.fetch(0, Integer.MAX_VALUE, null, ImmutableMap.of(PUBMED_KEY, false), null).get(TIMEOUT, SECONDS);
			assertThat("number of fetched elements (with sorting) coincides with expected", count, equalTo(scenario.numItems()));
			assertThat("number of fetched elements (with sorting) coincides with expected", op.collection().size(), equalTo(count));
			if (count > 0) {
				String previous = null, current = null;
				for (final Citation citation : op.collection().getElements()) {
					current = citation.getPubmed().getMedlineCitation().getPMID().getvalue();
					if (previous != null) {
						assertThat("sorting order coincides with expected", current.compareTo(previous), greaterThanOrEqualTo(0));
					}
					previous = current;
				}
			}

			// list with all features (filter, projection and sorting)
			System.out.println(" >> List with all features (filter, projection and sorting): " + scenario.toString());
			filters = Filters.builder()
					.filters(newArrayList(Filter.builder().type(FILTER_REGEX).fieldName(LVL_GUID_FIELD).value("(?i)([a-z])*_([0-9]){1,2}").build()))
					.build();
			count = op.fetch(0, Integer.MAX_VALUE, filters, ImmutableMap.of(LVL_GUID_FIELD, false), ImmutableMap.of("pubmed", false, "lvl", false)).get(TIMEOUT, SECONDS);
			assertThat("number of fetched elements (with all features) coincides with expected", count, equalTo(scenario.numItems()));
			assertThat("number of fetched elements (with all features) coincides with expected", op.collection().size(), equalTo(count));
			if (count > 0) {
				assertThat("original article was filtered from database response", op.collection().get(0).getPubmed(), nullValue());
				assertThat("additional annotations were filtered from database response", op.collection().get(0).getLvl(), nullValue());
				String previous = null, current = null;
				for (final Citation citation : op.collection().getElements()) {
					current = citation.getLvlId();
					if (previous != null) {
						assertThat("sorting order coincides with expected", current.compareTo(previous), greaterThanOrEqualTo(0));
					}
					previous = current;
				}
			}
		}

		// list all available versions
		System.out.println(" >> List versions: " + rs.toString());
		ds.citation2 = Citation.builder().lvlId(ID_0).build();
		List<LvlObject> versions = ds.citation2.versions().get(TIMEOUT, SECONDS);
		assertThat("versions are not null", versions, notNullValue());
		assertThat("number of versions coincides with expected", versions.size(), equalTo(ds.versions.get(ID_0)));
		// Uncomment for additional output
		System.out.println(" >> Versions (GUID=" + ds.citation2.getLvlId() + "): " + rs.toString() + ", count=" + versions.size() 
				+ "\n" + objectToJson(versions, JSON_PRETTY_PRINTER));

		if (testUpdate) {
			// update the citation
			System.out.println(" >> Update the citation: " + rs.toString());
			final Date lastModified = ds.citation0.getLastModified();
			ds.citation0.getLvl().getCited().add("NEW_SEQ");
			ds.citation0.save().get(TIMEOUT, SECONDS);
			assertThat("last modified field is not null", ds.citation0.getLastModified(), notNullValue());
			assertThat("updated last modified value is in the future", ds.citation0.getLastModified().after(lastModified), equalTo(true));

			// find after update
			System.out.println(" >> Find after update: " + rs.toString());
			ds.citation2 = Citation.builder().lvlId(ID_0).build();
			ds.citation2.fetch().get(TIMEOUT, SECONDS);
			assertThat("fetched citation (after update) coincides with expected", ds.citation2, equalTo(ds.citation0));
			// Uncomment for additional output
			System.out.println(" >> Fetched citation (after update, " + ds.citation2.getDbId() + "):\n" + ds.citation2.toJson(JSON_PRETTY_PRINTER));
		}

		// pagination
		final List<String> ids = newArrayList();
		final int insertedCount = 10, stateCount[] = new int[3];
		for (int i = 0; i < insertedCount; i++) {
			ObjectState state;
			switch (i % 3) {
			case 0:
				state = DRAFT;
				stateCount[0]++;
				break;
			case 1:
				state = RELEASE;
				stateCount[1]++;
				break;
			case 2:
				state = OBSOLETE;
				stateCount[2]++;
				break;
			default:
				throw new IllegalStateException("Unsupported state found");
			}
			final PubmedArticle articleX = PUBMED_XML_FACTORY.createPubmedArticle()
					.withMedlineCitation(PUBMED_XML_FACTORY.createMedlineCitation()
							.withPMID(PUBMED_XML_FACTORY.createPMID().withvalue(Integer.toString(i)))
							.withArticle(PUBMED_XML_FACTORY.createArticle().withPubModel("Electronic-Print").withArticleTitle("Paper number " + i).withJournal(PUBMED_XML_FACTORY.createJournal().withTitle("Journal of Awesomeness")).withAbstract(PUBMED_XML_FACTORY.createAbstract().withAbstractText("Abstract.")).withAuthorList(PUBMED_XML_FACTORY.createAuthorList().withAuthor(PUBMED_XML_FACTORY.createAuthor().withLastNameOrForeNameOrInitialsOrSuffixOrNameIDOrCollectiveName("Jane Doe"))).withPublicationTypeList(PUBMED_XML_FACTORY.createPublicationTypeList().withPublicationType(PUBMED_XML_FACTORY.createPublicationType().withvalue("Journal Article"))).withLanguage(PUBMED_XML_FACTORY.createLanguage().withvalue("eng")).withArticleDate(PUBMED_XML_FACTORY.createArticleDate().withYear(PUBMED_XML_FACTORY.createYear().withvalue("2013")))));
			final Citation citationX = Citation.builder()
					.lvlId("X_" + i)
					.pubmed(articleX)
					.state(state)
					.build();			
			ids.add(citationX.getLvlId());
			citationX.save().get(TIMEOUT, SECONDS);
		}
		for (final TestScenario scenario : rs.scenarios) {
			System.out.println(" >> Pagination: " + scenario.toString());
			final int size = 3;
			int start = 0;
			do {
				// setup collection
				switch (scenario.state) {
				case ALL:
					op = allOperator(ds.citations);
					break;
				case RELEASES:
					op = releasesOperator(ds.citations);
					break;
				default:
					throw new IllegalStateException("Unknown test state: " + scenario.state);
				}
				op.fetch(start, size, null, null, null).get(TIMEOUT, SECONDS);
				if (op.collection().size() != 0) {					
					assertThat("number of fetched elements coincides with expected", op.collection().size(), allOf(greaterThanOrEqualTo(1), lessThanOrEqualTo(size)));
					assertThat("total number of fetched elements coincides with expected", op.collection().getTotalCount(), equalTo(scenario.numItems(stateCount)));
					System.out.println("Paging: first item " + start + ", showing " + op.collection().size() + " of " + op.collection().getTotalCount() + " items\n"
							+ "Items: " + join(op.collection().ids(), ", "));
				}
				start += op.collection().size();
			} while (!op.collection().getElements().isEmpty());
		}

		// delete all the elements inserted for pagination test
		System.out.println(" >> Delete all the elements inserted in the previous test (pagination): " + rs.toString());
		for (int i = 0; i < ids.size(); i++) {
			Citation.builder().lvlId(ids.get(i)).build().delete().get(TIMEOUT, SECONDS);
		}
		count = ds.citations.fetch(0, Integer.MAX_VALUE, null, null, null).get(TIMEOUT, SECONDS);
		assertThat("number of fetched elements coincides with expected", count, equalTo(2));

		// remove active version (last modified version)
		System.out.println(" >> Remove last modified version: " + rs.toString());
		ds.citation0.delete().get(TIMEOUT, SECONDS);
		long totalCount = ds.citations.totalCount().get(TIMEOUT, SECONDS).longValue();
		assertThat("number of elements stored in the database coincides with expected", totalCount, equalTo(ds.versions.get(ID_0) > 1 ? 2l : 1l));
		
		// get versions after removing the active version
		ds.citation2 = Citation.builder().lvlId(ID_0).build();
		versions = ds.citation2.versions().get(TIMEOUT, SECONDS);
		assertThat("versions are not null", versions, notNullValue());
		assertThat("number of versions coincides with expected", versions.size(), equalTo(ds.versions.get(ID_0) - 1));		

		// remove all with cascade delete
		System.out.println(" >> Remove: " + rs.toString());
		ds.citation0.delete(DELETE_ALL, ON_DELETE_CASCADE).get(TIMEOUT, SECONDS);
		totalCount = ds.citations.totalCount().get(TIMEOUT, SECONDS).longValue();			
		assertThat("number of elements stored in the database coincides with expected", totalCount, equalTo(1l));

		// collect statistics about the collection
		System.out.println(" >> Collect statistics about the collection: " + rs.toString());
		final ListenableFuture<MongoCollectionStats> statsFuture = ds.citations.stats();
		final MongoCollectionStats stats = statsFuture.get(TIMEOUT, SECONDS);
		assertThat("collection statistics are not null", stats, notNullValue());
		System.out.println(" >> Collection statistics:\n" + objectToJson(stats, JSON_PRETTY_PRINTER));

		// clean-up the last element from the database
		System.out.println(" >> Clean-up the last element from the database: " + rs.toString());
		ds.citation1.delete().get(TIMEOUT, SECONDS);
		totalCount = ds.citations.totalCount().get(TIMEOUT, SECONDS).longValue();			
		assertThat("number of elements stored in the database coincides with expected", totalCount, equalTo(0l));

		System.out.println("CitationCollectionTest.operateOnTestDatasets() has finished");
	}

	private static class TestDataset {
		// create geographic objects		
		private final Point bcnPoint = Point.builder().coordinates(LngLatAlt.builder().coordinates( 2.1734034999999494d, 41.3850639d).build()).build();
		private final Point madPoint = Point.builder().coordinates(LngLatAlt.builder().coordinates(-3.7037901999999576d, 40.4167754d).build()).build();
		private final Point vlcPoint = Point.builder().coordinates(LngLatAlt.builder().coordinates(-0.3762881000000107d, 39.4699075d).build()).build();
		private final Polygon polygon = Polygon.builder().exteriorRing(
				LngLatAlt.builder().coordinates(-4.0d, 30.0d).build(),
				LngLatAlt.builder().coordinates( 3.0d, 30.0d).build(),
				LngLatAlt.builder().coordinates( 3.0d, 42.0d).build(),
				LngLatAlt.builder().coordinates(-4.0d, 42.0d).build(),
				LngLatAlt.builder().coordinates(-4.0d, 30.0d).build()).build();

		// create original PubMed articles
		private final PubmedArticle article0 = PUBMED_XML_FACTORY.createPubmedArticle()
				.withMedlineCitation(PUBMED_XML_FACTORY.createMedlineCitation()
						.withPMID(PUBMED_XML_FACTORY.createPMID().withvalue("EFGH5678"))
						.withDateCreated(PUBMED_XML_FACTORY.createDateCreated().withYear(PUBMED_XML_FACTORY.createYear().withvalue("2015")))
						.withArticle(PUBMED_XML_FACTORY.createArticle().withPubModel("Electronic-Print").withArticleTitle("The best paper in the world").withJournal(PUBMED_XML_FACTORY.createJournal().withTitle("Journal of Awesomeness")).withAbstract(PUBMED_XML_FACTORY.createAbstract().withAbstractText("This paper presents a text.")).withAuthorList(PUBMED_XML_FACTORY.createAuthorList().withAuthor(PUBMED_XML_FACTORY.createAuthor().withLastNameOrForeNameOrInitialsOrSuffixOrNameIDOrCollectiveName("John Doe"))).withPublicationTypeList(PUBMED_XML_FACTORY.createPublicationTypeList().withPublicationType(PUBMED_XML_FACTORY.createPublicationType().withvalue("Journal Article"))).withLanguage(PUBMED_XML_FACTORY.createLanguage().withvalue("eng")).withArticleDate(PUBMED_XML_FACTORY.createArticleDate().withYear(PUBMED_XML_FACTORY.createYear().withvalue("2015")))));		

		private final PubmedArticle article1 = PUBMED_XML_FACTORY.createPubmedArticle()
				.withMedlineCitation(PUBMED_XML_FACTORY.createMedlineCitation()
						.withPMID(PUBMED_XML_FACTORY.createPMID().withvalue("ABCD1234"))
						.withDateCreated(PUBMED_XML_FACTORY.createDateCreated().withYear(PUBMED_XML_FACTORY.createYear().withvalue("2014")))
						.withArticle(PUBMED_XML_FACTORY.createArticle().withPubModel("Electronic-Print").withArticleTitle("Rocket science rocks").withJournal(PUBMED_XML_FACTORY.createJournal().withTitle("Journal of Awesomeness")).withAbstract(PUBMED_XML_FACTORY.createAbstract().withAbstractText("It rocks! There is no much to say.")).withAuthorList(PUBMED_XML_FACTORY.createAuthorList().withAuthor(PUBMED_XML_FACTORY.createAuthor().withLastNameOrForeNameOrInitialsOrSuffixOrNameIDOrCollectiveName("Jane Doe"))).withPublicationTypeList(PUBMED_XML_FACTORY.createPublicationTypeList().withPublicationType(PUBMED_XML_FACTORY.createPublicationType().withvalue("Journal Article"))).withLanguage(PUBMED_XML_FACTORY.createLanguage().withvalue("eng")).withArticleDate(PUBMED_XML_FACTORY.createArticleDate().withYear(PUBMED_XML_FACTORY.createYear().withvalue("2014")))));

		// create citations
		String pmid = article0.getMedlineCitation().getPMID().getvalue();
		private final Citation citation0 = Citation.builder()
				.lvlId(ID_0)
				.lvl(LvlCitation.builder().cited(newArrayList("SEQ_0", "SEQ_1")).build())
				.pubmed(article0)
				.location(bcnPoint)
				.state(DRAFT)
				.provenance(newObjectImportProv(newPubMedArticle("PMID|" + pmid), "lvl|pm|" + pmid, newGeocoding(bcnPoint)))
				.references(Maps.<String, List<String>>newHashMap(ImmutableMap.of("sequences", newArrayList("lvl|sf|gb|SEQ_0", "lvl|sf|gb|SEQ_1"))))
				.build();

		private final Citation citation1 = Citation.builder()
				.lvlId(ID_1)
				.pubmed(article1)
				.location(madPoint)
				.build();

		private Citation citation2 = null;
		private final Citations citations = new Citations();

		private Map<String, Integer> versions = Maps.newHashMap(ImmutableMap.of(ID_0, 1, ID_1, 1));

		public TestDataset(final boolean verbose) throws IOException {
			if (verbose) {
				/* Uncomment for additional output */
				System.out.println(" >> Original PubMed article:\n" + prettyPrint(PUBMED_XMLB.typeToXml(article0)));
			}
		}		
	}

	private static class Resultset {

		private TestScenario[] scenarios;

		public Resultset(final TestScenario[] scenarios) {
			this.scenarios = scenarios;			
		}

		public String toString() {
			return Arrays.toString(scenarios);
		}

	}

	private static class TestScenario {

		private final String name;
		private final TestState state;
		private final ObjectState[] states;

		public TestScenario(final String name, final TestState state, final ObjectState[] states) {
			this.name = name;
			this.state = state;
			this.states = states;
		}

		public int total() {
			return states.length;
		}

		public int numReleases() {
			return frequency(asList(states), RELEASE);
		}

		public int numItems(final Integer... excludes) {
			return numItems(null, excludes);
		}

		public int numItems(final int[] stateCount, final Integer... excludes) {
			int count = 0;
			switch (state) {
			case ALL:
				count = total() + sum(stateCount) - (excludes != null ? excludes.length : 0);
				break;
			case RELEASES:
				count = numReleases() + (stateCount != null ? stateCount[1] : 0) - numExcluded(excludes, RELEASE);
				break;
			default:
				throw new IllegalStateException("Unknown test state");
			}
			return count;
		}

		private int numExcluded(final Integer[] excludes, final ObjectState state) {
			int numExcluded = 0;
			if (excludes != null) {
				for (final int pos : excludes) {
					numExcluded += (pos < states.length && states[pos].equals(state) ? 1 : 0);
				}
			}
			return numExcluded;
		}

		private int sum(final int[] stateCount) {
			int sum = 0;
			if (stateCount != null) {
				for (int i = 0; i < stateCount.length; i++) {
					sum += stateCount[i];
				}
			}
			return sum;
		}

		public String toString() {
			return "[name=" + this.name + ", state=" + this.state + "]";
		}

	}

	private static enum TestState {
		ALL,
		RELEASES
	}

}