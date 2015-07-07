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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.getFirst;
import static eu.eubrazilcc.lvl.storage.prov.Provenance.LVL_PREFIX;
import static eu.eubrazilcc.lvl.storage.prov.Provenance.PROVENANCE;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.annotation.Nullable;

import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Agent;
import org.openprovenance.prov.model.Attribute;
import org.openprovenance.prov.model.Bundle;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.Statement;
import org.openprovenance.prov.model.StatementOrBundle;
import org.openprovenance.prov.model.WasAssociatedWith;
import org.openprovenance.prov.model.WasInformedBy;

import com.google.common.collect.Lists;

import eu.eubrazilcc.lvl.core.entrez.EntrezHelper;
import eu.eubrazilcc.lvl.core.geocoding.GeocodingHelper;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.http.client.HttpClientProvider;
import eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder;
import eu.eubrazilcc.lvl.core.xml.PubMedXmlBinder;
import eu.eubrazilcc.lvl.storage.Citation;
import eu.eubrazilcc.lvl.storage.Sequence;
import eu.eubrazilcc.lvl.storage.security.User;

/**
 * Provides provenance factory methods.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class ProvFactory {

	public static Document newProvDocument() {
		final Document graph = PROVENANCE.factory().newDocument();

		// system initialization
		final String provId = randomProvId();
		final Bundle bundle = PROVENANCE.factory().newNamedBundle(PROVENANCE.qn(provId), PROVENANCE.ns(), Lists.<Statement>newArrayList());
		final Agent system = PROVENANCE.lvlAgent();
		final Entity provBundle = PROVENANCE.entity(LVL_PREFIX, provId, PROVENANCE.bundleType());		
		bundle.getStatement().addAll(asList(new Statement[] { 
				system, provBundle,
				PROVENANCE.factory().newWasAttributedTo(PROVENANCE.qn("attr"), provBundle.getId(), system.getId())
		}));

		graph.getStatementOrBundle().add(bundle);
		graph.setNamespace(PROVENANCE.ns());
		return graph;
	}

	public static ProvDataSource newGenBankSequence(final String objectId, final String collection) {
		ProvDataSource ds = null;
		try {
			ds = new ProvDataSource("GenBank", new URL("http://www.ncbi.nlm.nih.gov/genbank/"), objectId, Sequence.class.getSimpleName(),
					EntrezHelper.class, HttpClientProvider.class, GbSeqXmlBinder.class, "GBSeqXML", 
					Sequence.class, collection);
		} catch (MalformedURLException ignore) { }
		return ds;
	}

	public static ProvDataSource newPubMedArticle(final String objectId) {
		ProvDataSource ds = null;
		try {
			ds = new ProvDataSource("PubMed", new URL("http://www.ncbi.nlm.nih.gov/pubmed/"), objectId, Citation.class.getSimpleName(),
					EntrezHelper.class, HttpClientProvider.class, PubMedXmlBinder.class, "PubMedXML", 
					Citation.class, Citation.COLLECTION);
		} catch (MalformedURLException ignore) { }
		return ds;
	}

	public static ProvGeocoding newGeocoding(final Point point) {
		return new ProvGeocoding(point, GeocodingHelper.class);
	}

	public static void addObjectImportProv(final Document graph, final ProvDataSource ds, final String lvlId, final @Nullable ProvGeocoding gc) {
		final Bundle bundle = getBundle(graph);
		final ProvDataSource ds2 = ProvDataSource.validate(ds);
		final String lvlId2 = parseParam(lvlId);
		final ProvGeocoding gc2 = (gc != null ? ProvGeocoding.validate(gc) : null);
		final Agent system = PROVENANCE.lvlAgent();

		// import activity
		final Agent importer = PROVENANCE.softwareAgent(ds2.importer);
		bundle.getStatement().addAll(asList(new Statement[] { 
				importer, 
				PROVENANCE.factory().newActedOnBehalfOf(null, importer.getId(), system.getId())
		}));

		final Activity assignAct = PROVENANCE.factory().newActivity(PROVENANCE.qn("assign"));
		final WasAssociatedWith assignAssoc = PROVENANCE.factory().newWasAssociatedWith(null, assignAct.getId(), system.getId());
		final Activity importAct = PROVENANCE.factory().newActivity(PROVENANCE.qn("import"));
		final WasInformedBy importInf = PROVENANCE.factory().newWasInformedBy(null, assignAct.getId(), importAct.getId());
		final WasAssociatedWith importAssoc = PROVENANCE.factory().newWasAssociatedWith(null, importAct.getId(), importer.getId());		
		bundle.getStatement().addAll(asList(new Statement[] {
				assignAct, assignAssoc, importAct, importInf, importAssoc
		}));

		final Agent dataSource = PROVENANCE.factory().newAgent(PROVENANCE.qn(ds2.name), asList(new Attribute[] { 
				PROVENANCE.dataCatalogType(),
				PROVENANCE.urlAttr(ds2.url)
		}));
		final Entity entity = PROVENANCE.entity(LVL_PREFIX, ds2.type);
		final Entity ent1 = PROVENANCE.entity(LVL_PREFIX, ds2.objectId, PROVENANCE.datasetType());
		bundle.getStatement().addAll(asList(new Statement[] {
				dataSource, entity, ent1, 
				PROVENANCE.factory().newSpecializationOf(ent1.getId(), entity.getId()),
				PROVENANCE.factory().newWasAttributedTo(null, ent1.getId(), dataSource.getId()),
				PROVENANCE.factory().newUsed(null, importAct.getId(), ent1.getId())
		}));

		// download activity
		final Agent downloader = PROVENANCE.softwareAgent(ds2.downloader);
		final Activity downloadAct = PROVENANCE.factory().newActivity(PROVENANCE.qn("download"));
		PROVENANCE.factory().addType(downloadAct, PROVENANCE.downloadActionType());
		final Entity sequenceFormat = PROVENANCE.entity(LVL_PREFIX, ds2.format, PROVENANCE.type(LVL_PREFIX, "DataFormat"));
		final Entity entRaw = PROVENANCE.entity(LVL_PREFIX, "file1");
		bundle.getStatement().addAll(asList(new Statement[] {
				downloader, downloadAct, sequenceFormat, entRaw,
				PROVENANCE.factory().newActedOnBehalfOf(null, downloader.getId(), importer.getId()),
				PROVENANCE.factory().newWasAssociatedWith(null, downloadAct.getId(), downloader.getId()),
				PROVENANCE.factory().newUsed(null, downloadAct.getId(), ent1.getId()),
				PROVENANCE.factory().newSpecializationOf(entRaw.getId(), sequenceFormat.getId()),
				PROVENANCE.factory().newWasGeneratedBy(null, entRaw.getId(), downloadAct.getId(), PROVENANCE.factory().newTimeNow(), null)
		}));

		// parse activity
		final Agent parser = PROVENANCE.softwareAgent(ds2.parser);
		final Activity parseAct = PROVENANCE.factory().newActivity(PROVENANCE.qn("parse"));
		final Entity entObj1 = PROVENANCE.entity(LVL_PREFIX, "obj1");
		bundle.getStatement().addAll(asList(new Statement[] {
				parser, parseAct, entObj1,
				PROVENANCE.factory().newActedOnBehalfOf(null, parser.getId(), importer.getId()),
				PROVENANCE.factory().newWasAssociatedWith(null, parseAct.getId(), parser.getId()),
				PROVENANCE.factory().newUsed(null, parseAct.getId(), entRaw.getId()),
				PROVENANCE.factory().newWasGeneratedBy(null, entObj1.getId(), parseAct.getId())
		}));

		Entity coord = null;
		if (gc2 != null) {
			// infer geographic coordinates (geocode) activity
			final Agent geocoder = PROVENANCE.softwareAgent(gc2.geocoder);
			final Activity geocodeAct = PROVENANCE.factory().newActivity(PROVENANCE.qn("geocode"));
			coord = PROVENANCE.entity(LVL_PREFIX, "coord1", PROVENANCE.locationAttr(gc2.point));
			bundle.getStatement().addAll(asList(new Statement[] {
					geocoder, geocodeAct, coord,
					PROVENANCE.factory().newActedOnBehalfOf(null, geocoder.getId(), importer.getId()),
					PROVENANCE.factory().newWasAssociatedWith(null, geocodeAct.getId(), geocoder.getId()),
					PROVENANCE.factory().newUsed(null, geocodeAct.getId(), entObj1.getId()),
					PROVENANCE.factory().newWasGeneratedBy(null, coord.getId(), geocodeAct.getId())
			}));
		}

		// create database record combining the different sources of information				
		final Activity combineAct = PROVENANCE.factory().newActivity(PROVENANCE.qn("combine"));		
		final Entity entObj2 = PROVENANCE.entity(LVL_PREFIX, "obj2");
		final List<Statement> statements = Lists.<Statement>newArrayList(
				combineAct, entObj2,
				PROVENANCE.factory().newWasAssociatedWith(null, combineAct.getId(), importer.getId()),
				PROVENANCE.factory().newUsed(null, combineAct.getId(), entObj1.getId()),						
				PROVENANCE.factory().newWasGeneratedBy(null, entObj2.getId(), combineAct.getId())
				);
		if (coord != null) statements.add(PROVENANCE.factory().newUsed(null, combineAct.getId(), coord.getId()));
		bundle.getStatement().addAll(statements);

		// add provenance information
		final Activity createProvAct = PROVENANCE.factory().newActivity(PROVENANCE.qn("createProv"));
		final Entity prov = PROVENANCE.entity(LVL_PREFIX, "prov1", PROVENANCE.bundleType());

		final Activity embedProvAct = PROVENANCE.factory().newActivity(PROVENANCE.qn("embedProv"));
		final Entity entObj3 = PROVENANCE.entity(LVL_PREFIX, "obj3");

		bundle.getStatement().addAll(asList(new Statement[] {
				createProvAct, prov,
				PROVENANCE.factory().newWasAssociatedWith(null, createProvAct.getId(), importer.getId()),
				PROVENANCE.factory().newUsed(null, createProvAct.getId(), entObj2.getId()),
				PROVENANCE.factory().newWasGeneratedBy(null, prov.getId(), createProvAct.getId()),
				embedProvAct, entObj3,
				PROVENANCE.factory().newWasAssociatedWith(null, embedProvAct.getId(), importer.getId()),
				PROVENANCE.factory().newUsed(null, embedProvAct.getId(), entObj2.getId()),
				PROVENANCE.factory().newUsed(null, embedProvAct.getId(), prov.getId()),
				PROVENANCE.factory().newWasDerivedFrom(null, entObj3.getId(), entObj2.getId(), embedProvAct.getId(), null, null, null),
				PROVENANCE.factory().newWasDerivedFrom(null, entObj3.getId(), prov.getId(), embedProvAct.getId(), null, null, null),
				PROVENANCE.factory().newWasGeneratedBy(null, entObj3.getId(), embedProvAct.getId())
		}));

		// insert sequence draft in the database
		final Agent collection = PROVENANCE.factory().newAgent(PROVENANCE.qn(ds2.collection), asList(new Attribute[] { 
				PROVENANCE.dataCatalogType(),
		}));
		final Agent dbClient = PROVENANCE.softwareAgent(ds2.dbObject);
		final Activity insertIntoDbAct = PROVENANCE.factory().newActivity(PROVENANCE.qn("register"));
		final Entity entityDocument = PROVENANCE.entity(LVL_PREFIX, "Document");
		final Entity entObj4 = PROVENANCE.entity(LVL_PREFIX, lvlId2, PROVENANCE.datasetType());
		bundle.getStatement().addAll(asList(new Statement[] {
				collection, dbClient, insertIntoDbAct, entityDocument, entObj4,
				PROVENANCE.factory().newActedOnBehalfOf(null, dbClient.getId(), importer.getId()),
				PROVENANCE.factory().newWasAssociatedWith(null, insertIntoDbAct.getId(), dbClient.getId()),
				PROVENANCE.factory().newUsed(null, insertIntoDbAct.getId(), entObj3.getId()),
				PROVENANCE.factory().newSpecializationOf(entObj4.getId(), entityDocument.getId()),
				PROVENANCE.factory().newWasAttributedTo(null, entObj4.getId(), collection.getId()),
				PROVENANCE.factory().newWasGeneratedBy(null, entObj4.getId(), importAct.getId()),
				PROVENANCE.factory().newWasDerivedFrom(null, entObj4.getId(), entObj3.getId(), insertIntoDbAct.getId(), null, null, null),
				PROVENANCE.factory().newWasEndedBy(null, importAct.getId(), entObj4.getId(), insertIntoDbAct.getId())
		}));
	}

	public static void addUserCreatedProv(final Document graph, final User creator, final String lvlId) {
		final Bundle bundle = getBundle(graph);
		final String lvlId2 = parseParam(lvlId);		
		final Agent system = PROVENANCE.lvlAgent();


		// TODO
	}

	private static Bundle getBundle(final Document graph) {
		checkArgument(graph != null, "Uninitialized provenance document");
		final StatementOrBundle node = getFirst(graph.getStatementOrBundle(), null);
		checkArgument(node != null && node instanceof Bundle, "Uninitialized or invalid provenance bundle");
		return (Bundle)node;
	}

	private static String parseParam(final String param) {
		String param2 = null;
		checkArgument((param2 = trimToNull(param)) != null, "Uninitialized or invalid parameter");
		return param2;
	}

	private static String randomProvId() {
		return "PROV-" + randomUUID().toString().replace("-", "");
	}

	public static class ProvDataSource {		
		private final String name;
		private final URL url;
		private final String objectId;
		private final String type;

		private final Class<?> importer;
		private final Class<?> downloader;
		private final Class<?> parser;
		private final String format;

		private final Class<?> dbObject;
		private final String collection;

		public ProvDataSource(final String name, final URL url, final String objectId, final String type,
				final Class<?> importer, final Class<?> downloader, final Class<?> parser, final String format,
				final Class<?> dbObject, final String collection) {
			this.name = name;
			this.url = url;
			this.objectId = objectId;
			this.type = type;
			this.importer = importer;
			this.downloader = downloader;
			this.parser = parser;
			this.format = format;
			this.dbObject = dbObject;
			this.collection = collection;
		}

		public static ProvDataSource validate(final ProvDataSource original) {
			checkArgument(original != null, "Uninitialized data source");
			String name2 = null;
			checkArgument((name2 = trimToNull(original.name)) != null, "Uninitialized or invalid name");
			checkArgument(original.url != null, "Uninitialized URL");
			String objectId2 = null;
			checkArgument((objectId2 = trimToNull(original.objectId)) != null, "Uninitialized or invalid object Id");
			String type2 = null;
			checkArgument((type2 = trimToNull(original.type)) != null, "Uninitialized or invalid type");
			checkArgument(original.importer != null, "Uninitialized importer");
			checkArgument(original.downloader != null, "Uninitialized downloader");
			checkArgument(original.parser != null, "Uninitialized parser");
			String format2 = null;
			checkArgument((format2 = trimToNull(original.format)) != null, "Uninitialized or invalid data format");
			checkArgument(original.dbObject != null, "Uninitialized database object");
			String collection2 = null;
			checkArgument((collection2 = trimToNull(original.collection)) != null, "Uninitialized or invalid collection");
			return new ProvDataSource(name2, original.url, objectId2, type2, 
					original.importer, original.downloader, original.parser, format2, 
					original.dbObject, collection2);
		}
	}

	public static class ProvGeocoding {
		private final Point point;
		private final Class<?> geocoder;

		public ProvGeocoding(final Point point, final Class<?> geocoder) {
			this.point = point;
			this.geocoder = geocoder;
		}

		public static ProvGeocoding validate(final ProvGeocoding original) {
			checkArgument(original != null, "Uninitialized geocoding");
			checkArgument(original.point != null, "Uninitialized location point");
			checkArgument(original.geocoder != null, "Uninitialized geocoder");
			return original;
		}
	}

}