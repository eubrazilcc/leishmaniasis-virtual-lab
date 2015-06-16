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

import static eu.eubrazilcc.lvl.storage.prov.Provenance.GB_PREFIX;
import static eu.eubrazilcc.lvl.storage.prov.Provenance.LVL_PREFIX;
import static eu.eubrazilcc.lvl.storage.prov.Provenance.PROVENANCE;
import static eu.eubrazilcc.lvl.storage.prov.Provenance.randomProvId;
import static java.util.Arrays.asList;

import javax.annotation.Nullable;

import org.openprovenance.prov.interop.InteropFramework;
import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Agent;
import org.openprovenance.prov.model.Attribute;
import org.openprovenance.prov.model.Bundle;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.Role;
import org.openprovenance.prov.model.Statement;
import org.openprovenance.prov.model.StatementOrBundle;
import org.openprovenance.prov.model.Used;
import org.openprovenance.prov.model.WasAssociatedWith;
import org.openprovenance.prov.model.WasInformedBy;

import com.google.common.collect.Lists;

import eu.eubrazilcc.lvl.core.entrez.EntrezHelper;
import eu.eubrazilcc.lvl.core.geocoding.GeocodingHelper;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.http.client.HttpClientProvider;
import eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder;
import eu.eubrazilcc.lvl.storage.dao.SequenceDAO;
import eu.eubrazilcc.lvl.storage.security.User;

/**
 * Provenance for biological sequences.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SequenceProv {

	public Document importFromGenbank(final String gbSeqId, final @Nullable Point point, final String recordId) {
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

		// import activity
		final Agent importer = PROVENANCE.softwareAgent(EntrezHelper.class);
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

		final Agent genbank = PROVENANCE.factory().newAgent(PROVENANCE.qnGenBank(), asList(new Attribute[] { 
				PROVENANCE.dataCatalogType(),
				PROVENANCE.urlAttr("http://www.ncbi.nlm.nih.gov/nuccore/")
		}));
		final Entity sequence = PROVENANCE.entity(GB_PREFIX, "Sequence");
		final Entity seq1 = PROVENANCE.entity(GB_PREFIX, gbSeqId, PROVENANCE.datasetType());
		bundle.getStatement().addAll(asList(new Statement[] {
				genbank, sequence, seq1, 
				PROVENANCE.factory().newSpecializationOf(seq1.getId(), sequence.getId()),
				PROVENANCE.factory().newWasAttributedTo(null, seq1.getId(), genbank.getId()),
				PROVENANCE.factory().newUsed(null, importAct.getId(), seq1.getId())
		}));

		// download activity
		final Agent downloader = PROVENANCE.softwareAgent(HttpClientProvider.class);
		final Activity downloadAct = PROVENANCE.factory().newActivity(PROVENANCE.qn("download"));
		PROVENANCE.factory().addType(downloadAct, PROVENANCE.downloadActionType());
		final Entity sequenceFormat = PROVENANCE.entity(GB_PREFIX, "GBSeqXML", PROVENANCE.type(LVL_PREFIX, "DataFormat"));
		final Entity seqRaw = PROVENANCE.entity(GB_PREFIX, "file1");
		bundle.getStatement().addAll(asList(new Statement[] {
				downloader, downloadAct, sequenceFormat, seqRaw,
				PROVENANCE.factory().newActedOnBehalfOf(null, downloader.getId(), importer.getId()),
				PROVENANCE.factory().newWasAssociatedWith(null, downloadAct.getId(), downloader.getId()),
				PROVENANCE.factory().newUsed(null, downloadAct.getId(), seq1.getId()),
				PROVENANCE.factory().newSpecializationOf(seqRaw.getId(), sequenceFormat.getId()),
				PROVENANCE.factory().newWasGeneratedBy(null, seqRaw.getId(), downloadAct.getId(), PROVENANCE.factory().newTimeNow(), null)
		}));

		// parse activity
		final Agent parser = PROVENANCE.softwareAgent(GbSeqXmlBinder.class);
		final Activity parseAct = PROVENANCE.factory().newActivity(PROVENANCE.qn("parse"));
		final Entity seqObj1 = PROVENANCE.entity(GB_PREFIX, "obj1");
		bundle.getStatement().addAll(asList(new Statement[] {
				parser, parseAct, seqObj1,
				PROVENANCE.factory().newActedOnBehalfOf(null, parser.getId(), importer.getId()),
				PROVENANCE.factory().newWasAssociatedWith(null, parseAct.getId(), parser.getId()),
				PROVENANCE.factory().newUsed(null, parseAct.getId(), seqRaw.getId()),
				PROVENANCE.factory().newWasGeneratedBy(null, seqObj1.getId(), parseAct.getId())
		}));

		// infer geographic coordinates (geocode) activity
		final Agent geocoder = PROVENANCE.softwareAgent(GeocodingHelper.class);
		final Activity geocodeAct = PROVENANCE.factory().newActivity(PROVENANCE.qn("geocode"));
		final Entity coord = PROVENANCE.entity(LVL_PREFIX, "coord1", PROVENANCE.locationAttr(point));
		bundle.getStatement().addAll(asList(new Statement[] {
				geocoder, geocodeAct, coord,
				PROVENANCE.factory().newActedOnBehalfOf(null, geocoder.getId(), importer.getId()),
				PROVENANCE.factory().newWasAssociatedWith(null, geocodeAct.getId(), geocoder.getId()),
				PROVENANCE.factory().newUsed(null, geocodeAct.getId(), seqObj1.getId()),
				PROVENANCE.factory().newWasGeneratedBy(null, coord.getId(), geocodeAct.getId())
		}));

		// create database record combining the different sources of information				
		final Activity combineAct = PROVENANCE.factory().newActivity(PROVENANCE.qn("combine"));		
		final Entity seqObj2 = PROVENANCE.entity(LVL_PREFIX, "obj2");
		bundle.getStatement().addAll(asList(new Statement[] {
				combineAct, seqObj2,
				PROVENANCE.factory().newWasAssociatedWith(null, combineAct.getId(), importer.getId()),
				PROVENANCE.factory().newUsed(null, combineAct.getId(), seqObj1.getId()),
				PROVENANCE.factory().newUsed(null, combineAct.getId(), coord.getId()),				
				PROVENANCE.factory().newWasGeneratedBy(null, seqObj2.getId(), combineAct.getId())
		}));

		// add provenance information
		final Activity createProvAct = PROVENANCE.factory().newActivity(PROVENANCE.qn("createProv"));
		final Entity prov = PROVENANCE.entity(LVL_PREFIX, "prov1", PROVENANCE.bundleType());

		final Activity embedProvAct = PROVENANCE.factory().newActivity(PROVENANCE.qn("embedProv"));
		final Entity seqObj3 = PROVENANCE.entity(LVL_PREFIX, "obj3");

		bundle.getStatement().addAll(asList(new Statement[] {
				createProvAct, prov,
				PROVENANCE.factory().newWasAssociatedWith(null, createProvAct.getId(), importer.getId()),
				PROVENANCE.factory().newUsed(null, createProvAct.getId(), seqObj2.getId()),
				PROVENANCE.factory().newWasGeneratedBy(null, prov.getId(), createProvAct.getId()),
				embedProvAct, seqObj3,
				PROVENANCE.factory().newWasAssociatedWith(null, embedProvAct.getId(), importer.getId()),
				PROVENANCE.factory().newUsed(null, embedProvAct.getId(), seqObj2.getId()),
				PROVENANCE.factory().newUsed(null, embedProvAct.getId(), prov.getId()),
				PROVENANCE.factory().newWasDerivedFrom(null, seqObj3.getId(), seqObj2.getId(), embedProvAct.getId(), null, null, null),
				PROVENANCE.factory().newWasDerivedFrom(null, seqObj3.getId(), prov.getId(), embedProvAct.getId(), null, null, null),
				PROVENANCE.factory().newWasGeneratedBy(null, seqObj3.getId(), embedProvAct.getId())
		}));

		// insert sequence draft in the database
		final Agent collection = PROVENANCE.factory().newAgent(PROVENANCE.qn("SequenceCollection"), asList(new Attribute[] { 
				PROVENANCE.dataCatalogType(),
		}));
		final Agent dbClient = PROVENANCE.softwareAgent(SequenceDAO.class);
		final Activity insertIntoDbAct = PROVENANCE.factory().newActivity(PROVENANCE.qn("register"));
		final Entity sequenceDocument = PROVENANCE.entity(LVL_PREFIX, "Document");
		final Entity seqObj4 = PROVENANCE.entity(LVL_PREFIX, recordId, PROVENANCE.datasetType());
		bundle.getStatement().addAll(asList(new Statement[] {
				collection, dbClient, insertIntoDbAct, sequenceDocument, seqObj4,
				PROVENANCE.factory().newActedOnBehalfOf(null, dbClient.getId(), importer.getId()),
				PROVENANCE.factory().newWasAssociatedWith(null, insertIntoDbAct.getId(), dbClient.getId()),
				PROVENANCE.factory().newUsed(null, insertIntoDbAct.getId(), seqObj3.getId()),
				PROVENANCE.factory().newSpecializationOf(seqObj4.getId(), sequenceDocument.getId()),
				PROVENANCE.factory().newWasAttributedTo(null, seqObj4.getId(), collection.getId()),
				PROVENANCE.factory().newWasGeneratedBy(null, seqObj4.getId(), importAct.getId()),
				PROVENANCE.factory().newWasDerivedFrom(null, seqObj4.getId(), seqObj3.getId(), insertIntoDbAct.getId(), null, null, null),
				PROVENANCE.factory().newWasEndedBy(null, importAct.getId(), seqObj4.getId(), insertIntoDbAct.getId())
		}));

		graph.getStatementOrBundle().add(bundle);
		graph.setNamespace(PROVENANCE.ns());
		return graph;
	}

	public Document uploadUserSequence(final QualifiedName qn, final String userId) {
		// TODO
		return null;
	}
	
	public Document modifySequenceDraft(final String recordId, final User curator, final String newRecordId) {
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
		
		// edit activity
		final Agent curatorAgent = PROVENANCE.personAgent(curator);
		final Entity seqObj1 = PROVENANCE.entity(LVL_PREFIX, recordId);
		final Entity seqObj2 = PROVENANCE.entity(LVL_PREFIX, newRecordId);
		final Activity editAct = PROVENANCE.factory().newActivity(PROVENANCE.qn("edit"));
		final Role curatorRole = PROVENANCE.factory().newRole("Curator", PROVENANCE.qn("Curator"));
		final Used usedSequence = PROVENANCE.factory().newUsed(null, editAct.getId(), seqObj1.getId());
		PROVENANCE.factory().addRole(usedSequence, curatorRole);
		bundle.getStatement().addAll(asList(new Statement[] {
				curatorAgent, editAct, seqObj1, seqObj2,
				PROVENANCE.factory().newActedOnBehalfOf(null, curatorAgent.getId(), system.getId()),
				PROVENANCE.factory().newWasAssociatedWith(null, editAct.getId(), curatorAgent.getId()),
				usedSequence,
				PROVENANCE.factory().newWasInvalidatedBy(null, seqObj1.getId(), editAct.getId()),
				PROVENANCE.factory().newWasGeneratedBy(null, seqObj2.getId(), editAct.getId(), PROVENANCE.factory().newTimeNow(), null),
				PROVENANCE.factory().newWasDerivedFrom(null, seqObj2.getId(), seqObj1.getId(), editAct.getId(), null, null, null),
		}));		
		
		graph.getStatementOrBundle().add(bundle);
		graph.setNamespace(PROVENANCE.ns());
		return graph;
	}

	public Document approveSequence(final String recordId, final User curator, final String sequenceId) {
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

		// check activity
		final Agent curatorAgent = PROVENANCE.personAgent(curator);
		final Entity curationPolicy = PROVENANCE.entity(LVL_PREFIX, "CurationPolicy", Lists.<Attribute>newArrayList());
		final Entity seqObj1 = PROVENANCE.entity(LVL_PREFIX, recordId);
		final Entity seqObj2 = PROVENANCE.entity(LVL_PREFIX, "obj1");
		final Activity checkAct = PROVENANCE.factory().newActivity(PROVENANCE.qn("check"), null, null, asList(new Attribute[] { 
				PROVENANCE.approvalType()
		}));
		final Role curatorRole = PROVENANCE.factory().newRole("Curator", PROVENANCE.qn("Curator"));
		final Used usedSequence = PROVENANCE.factory().newUsed(null, checkAct.getId(), seqObj1.getId());
		PROVENANCE.factory().addRole(usedSequence, curatorRole);
		bundle.getStatement().addAll(asList(new Statement[] {
				curatorAgent, curationPolicy, checkAct, seqObj1, seqObj2,
				PROVENANCE.factory().newActedOnBehalfOf(null, curatorAgent.getId(), system.getId()),
				PROVENANCE.factory().newWasAttributedTo(PROVENANCE.qn("attr"), curationPolicy.getId(), system.getId()),
				PROVENANCE.factory().newWasAssociatedWith(null, checkAct.getId(), curatorAgent.getId(), curationPolicy.getId(), null),
				usedSequence,
				PROVENANCE.factory().newWasInvalidatedBy(null, seqObj1.getId(), checkAct.getId()),
				PROVENANCE.factory().newWasGeneratedBy(null, seqObj2.getId(), checkAct.getId()),
				PROVENANCE.factory().newWasDerivedFrom(null, seqObj2.getId(), seqObj1.getId(), checkAct.getId(), null, null, null),
		}));
		
		// publish activity		
		final Activity publishAct = PROVENANCE.factory().newActivity(PROVENANCE.qn("publish"));		
		final Entity seqObj3 = PROVENANCE.entity(LVL_PREFIX, sequenceId);
		bundle.getStatement().addAll(asList(new Statement[] {
				publishAct, seqObj3,
				PROVENANCE.factory().newWasAssociatedWith(null, publishAct.getId(), curatorAgent.getId()),
				PROVENANCE.factory().newUsed(null, publishAct.getId(), seqObj3.getId()),
				PROVENANCE.factory().newWasGeneratedBy(null, seqObj3.getId(), publishAct.getId(), PROVENANCE.factory().newTimeNow(), null),
				PROVENANCE.factory().newWasDerivedFrom(null, seqObj3.getId(), seqObj2.getId(), publishAct.getId(), null, null, null)
		}));

		graph.getStatementOrBundle().add(bundle);
		graph.setNamespace(PROVENANCE.ns());
		return graph;
	}

	public Document combine(final Document... documents) {
		final Document graph = PROVENANCE.factory().newDocument();
		for (final Document doc : documents) {
			for (final StatementOrBundle item : doc.getStatementOrBundle()) {
				graph.getStatementOrBundle().add(item);
			}
		}
		graph.setNamespace(PROVENANCE.ns());
		return graph;
	}

	/* TODO sudo apt-get install graphviz */
	public void exportToFile(final Document document, final String file) {
		final InteropFramework intF = new InteropFramework();
		intF.writeDocument(file, document);     
		// intF.writeDocument(System.out, ProvFormat.JSON, document);
		// intF.writeDocument(System.out, ProvFormat.PROVN, document);
	}

}