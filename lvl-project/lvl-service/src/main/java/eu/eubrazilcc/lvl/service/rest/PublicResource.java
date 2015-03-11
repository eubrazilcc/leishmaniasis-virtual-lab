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

import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_NAME;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.parseParam;
import static eu.eubrazilcc.lvl.storage.ResourceIdPattern.URL_FRAGMENT_PATTERN;
import static eu.eubrazilcc.lvl.storage.dao.DatasetDAO.DATASET_DAO;
import static eu.eubrazilcc.lvl.storage.urlshortener.UrlShortener.shortenUrl;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;

import eu.eubrazilcc.lvl.core.Dataset;

/**
 * Public resources.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/public")
public class PublicResource {

	private final static Logger LOGGER = getLogger(PublicResource.class);

	public static final String RESOURCE_NAME = LVL_NAME + " Public Resource";

	@GET
	@Path("datasets/{secret: " + URL_FRAGMENT_PATTERN + "}")
	@Produces(APPLICATION_OCTET_STREAM)
	public Response downloadDataset(final @PathParam("secret") String secret) {
		final String secret2 = parseParam(secret);
		// get from database
		final Dataset dataset = DATASET_DAO.findOpenAccess(secret2);		
		if (dataset == null || dataset.getOutfile() == null) {
			throw new WebApplicationException("Element not found", NOT_FOUND);
		}
		// attach file to response
		final StreamingOutput stream = new StreamingOutput() {
			@Override
			public void write(final OutputStream os) throws IOException {
				try (final FileInputStream is = new FileInputStream(dataset.getOutfile())) {
					final byte[] buffer = new byte[1024];
					int noOfBytes = 0;
					while ((noOfBytes = is.read(buffer)) != -1) {
						os.write(buffer, 0, noOfBytes);
					}
				} catch (Exception e) {
					throw new WebApplicationException("Failed to write file to output", INTERNAL_SERVER_ERROR);
				}
			}
		};
		LOGGER.trace("Downloading open access dataset: ns=" + dataset.getNamespace() + ", fn=" + dataset.getFilename());
		return Response.ok(stream, isNotBlank(dataset.getContentType()) ? dataset.getContentType(): APPLICATION_OCTET_STREAM)
				.header("content-disposition", "attachment; filename = " + dataset.getFilename())
				.build();
	}

	@GET
	@Path("datasets/{secret: " + URL_FRAGMENT_PATTERN + "}/shortened_url")
	@Produces(TEXT_PLAIN)
	public String shortenedUrl(final @PathParam("secret") String secret, final @Context UriInfo uriInfo) {
		final String secret2 = parseParam(secret);
		// get from database
		final Dataset dataset = DATASET_DAO.findOpenAccess(secret2);		
		if (dataset == null || dataset.getOutfile() == null) {
			throw new WebApplicationException("Element not found", NOT_FOUND);
		}
		// shorten URL		
		final UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
				.path(getClass())
				.path("datasets")
				.path(urlEncodeUtf8(secret));
		
		// TODO
		final String uri = uriBuilder.build().toString();
		final String shortened = shortenUrl(uri);
		System.err.println("\n\n >> URL: " + uri + ", SHORTENED: " + shortened + "\n");
		// TODO
		
		return shortened;
	}

}