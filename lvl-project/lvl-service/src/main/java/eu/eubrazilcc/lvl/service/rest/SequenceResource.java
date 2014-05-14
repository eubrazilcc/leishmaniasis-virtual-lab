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

import static eu.eubrazilcc.lvl.core.util.NumberUtils.roundUp;
import static eu.eubrazilcc.lvl.storage.dao.SequenceDAO.SEQUENCE_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.resourceScope;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.net.URI;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.mutable.MutableLong;

import eu.eubrazilcc.lvl.core.Paginable;
import eu.eubrazilcc.lvl.core.Sequence;
import eu.eubrazilcc.lvl.core.Sequences;
import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.core.geospatial.FeatureCollection;
import eu.eubrazilcc.lvl.core.geospatial.Point;
import eu.eubrazilcc.lvl.core.http.LinkRelation;
import eu.eubrazilcc.lvl.storage.SequenceKey;
import eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Gatekeeper;

/**
 * Sequences resource. Since a sequence is uniquely identified by the combination of the data source and 
 * the accession (i.e. GenBank, U49845), this class uses the reserved character ',' allowed in an URI 
 * segment to delimit or dereference the sequence identifier. For example, the following URIs are valid
 * and identifies the previous sequence:
 * <ul>
 * <li>https://localhost/webapp/sequences/GenBank,U49845</li>
 * </ul>
 * Identifiers that don't follow this convention will be rejected by this server with an HTTP Error 400 
 * (Bad request).
 * @author Erik Torres <ertorser@upv.es>
 * @see {@link Sequence} class
 * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.3">RFC3986 - Uniform Resource Identifier (URI): Generic Syntax; Section 3.3 - Path</a>
 */
@Path("/sequences")
public class SequenceResource {

	public static final String RESOURCE_NAME = ConfigurationManager.LVL_NAME + " Sequence Resource";
	public static final String RESOURCE_SCOPE = resourceScope(SequenceResource.class);
	
	public static final char ID_SEPARATOR = ',';
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Sequences getSequences(final @QueryParam("start") @DefaultValue("0") int start,
			final @QueryParam("size") @DefaultValue("10") int size, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		OAuth2Gatekeeper.authorize(request, null, headers, RESOURCE_SCOPE, false, RESOURCE_NAME);
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder()
				.queryParam("start", "{start}")
				.queryParam("size", "{size}");
		// get sequences from database
		final MutableLong count = new MutableLong(0l);
		final List<Sequence> sequences = SEQUENCE_DAO.baseUri(uriInfo.getAbsolutePath()).list(start, size, count);
		final int total = ((Long)count.getValue()).intValue();
		// previous link
		final Paginable paginable = new Paginable();
		if (start > 0) {
			int previous = start - size;
			if (previous < 0) previous = 0;
			final URI previousUri = uriBuilder.clone().build(previous, size);
			paginable.setPrevious(Link.fromUri(previousUri).rel(LinkRelation.PREVIOUS).type(MediaType.APPLICATION_JSON).build());
			final URI firstUri = uriBuilder.clone().build(0, size);
			paginable.setFirst(Link.fromUri(firstUri).rel(LinkRelation.FIRST).type(MediaType.APPLICATION_JSON).build());
		}
		// next link
		if (start + size < total) {
			int next = start + size;
			final URI nextUri = uriBuilder.clone().build(next, size);
			paginable.setNext(Link.fromUri(nextUri).rel(LinkRelation.NEXT).type(MediaType.APPLICATION_JSON).build());
			final int pages = roundUp(total, size);
			final URI lastUri = uriBuilder.clone().build(pages * size, size);
			paginable.setLast(Link.fromUri(lastUri).rel(LinkRelation.LAST).type(MediaType.APPLICATION_JSON).build());
		}		
		return Sequences.start().paginable(paginable).sequences(sequences).build();
	}

	@GET
	@Path("{id: [a-zA-Z_0-9]+,[a-zA-Z_0-9]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Sequence getSequence(final @PathParam("id") String id, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		OAuth2Gatekeeper.authorize(request, null, headers, RESOURCE_SCOPE, false, RESOURCE_NAME);
		if (isBlank(id)) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		// get from database
		final Sequence sequence = SEQUENCE_DAO
				.baseUri(uriInfo.getBaseUri())
				.find(SequenceKey.builder().parse(id, ID_SEPARATOR));
		if (sequence == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		return sequence;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createSequence(final Sequence sequence, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		OAuth2Gatekeeper.authorize(request, null, headers, RESOURCE_SCOPE, true, RESOURCE_NAME);
		if (sequence == null || isBlank(sequence.getAccession())) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		// create sequence in the database
		SEQUENCE_DAO.insert(sequence);
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(sequence.getAccession());		
		return Response.created(uriBuilder.build()).build();
	}

	@PUT
	@Path("{id: [a-zA-Z_0-9]+,[a-zA-Z_0-9]+}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateSequence(final @PathParam("id") String id, final Sequence update,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		OAuth2Gatekeeper.authorize(request, null, headers, RESOURCE_SCOPE, true, RESOURCE_NAME);
		if (isBlank(id)) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}		
		final SequenceKey sequenceKey = SequenceKey.builder().parse(id, ID_SEPARATOR);
		if (sequenceKey == null || !sequenceKey.getDataSource().equals(update.getDataSource()) 
				|| !sequenceKey.getAccession().equals(update.getAccession())) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		// get from database
		final Sequence current = SEQUENCE_DAO.find(sequenceKey);
		if (current == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		// update
		SEQUENCE_DAO.update(update);			
	}

	@DELETE
	@Path("{id: [a-zA-Z_0-9]+,[a-zA-Z_0-9]+}")
	public void deleteSequence(final @PathParam("id") String id, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		OAuth2Gatekeeper.authorize(request, null, headers, RESOURCE_SCOPE, true, RESOURCE_NAME);
		if (isBlank(id)) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		final SequenceKey sequenceKey = SequenceKey.builder().parse(id, ID_SEPARATOR);
		// get from database
		final Sequence current = SEQUENCE_DAO.find(sequenceKey);
		if (current == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		// delete
		SEQUENCE_DAO.delete(sequenceKey);
	}

	@GET
	@Path("nearby/{longitude}/{latitude}")
	public FeatureCollection findNearbySequences(final @PathParam("longitude") double longitude, 
			final @PathParam("latitude") double latitude, 
			final @QueryParam("maxDistance") @DefaultValue("1000.0d") double maxDistance, 
			final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		OAuth2Gatekeeper.authorize(request, null, headers, RESOURCE_SCOPE, false, RESOURCE_NAME);
		// get from database
		final List<Sequence> sequences = SEQUENCE_DAO.getNear(Point.builder()
				.coordinate(longitude, latitude).build(), maxDistance);
		final FeatureCollection.Builder builder = FeatureCollection.builder().wgs84();
		for (final Sequence sequence : sequences) {
			builder.feature(sequence.getLocation());
		}
		return builder.build();
	}	

}