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

package eu.eubrazilcc.lvl.service.rest;

import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.Format.PUBMED_XML;
import static eu.eubrazilcc.lvl.core.xml.PubMedXmlBinder.PUBMED_XMLB;
import static eu.eubrazilcc.lvl.service.rest.ResourceIdentifierPattern.CITATION_ID_PATTERN;
import static eu.eubrazilcc.lvl.storage.dao.ReferenceDAO.REFERENCE_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Gatekeeper.authorize;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.REFERENCES;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;

import eu.eubrazilcc.lvl.core.Reference;
import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.core.xml.ncbi.pubmed.PubmedArticle;

/**
 * PubMed citations resource.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/pm_citations")
public class PmCitationsResource {

	public static final String RESOURCE_NAME = ConfigurationManager.LVL_NAME + " PubMed Citation Resource";
	public static final String RESOURCE_SCOPE = REFERENCES;

	private final static Logger LOGGER = getLogger(PmCitationsResource.class);

	@GET
	@Path("{id: " + CITATION_ID_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public PubmedArticle getCitation(final @PathParam("id") String id, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		authorize(request, null, headers, RESOURCE_SCOPE, false, false, RESOURCE_NAME);
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		// get from database
		final Reference reference = REFERENCE_DAO.find(id);
		if (reference == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// get from file-system
		final File file = new File(CONFIG_MANAGER.getPubMedDir(PUBMED_XML), reference.getPubmedId() + ".xml");
		if (!file.canRead()) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		PubmedArticle pmArticle = null;
		try {
			pmArticle = PUBMED_XMLB.typeFromFile(file);
		} catch (IOException e) {
			LOGGER.error("Failed to load PubMed citation from file", e);
		}
		if (pmArticle == null) {
			throw new WebApplicationException("Unable to complete the operation", Response.Status.INTERNAL_SERVER_ERROR);
		}
		return pmArticle;
	}

}