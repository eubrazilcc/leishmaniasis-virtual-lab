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

package eu.eubrazilcc.lvl.oauth2.rest;

import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JSON_MAPPER;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.LinkedInStateDAO.LINKEDIN_STATE_DAO;
import static java.lang.System.currentTimeMillis;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.Response.status;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.apache.http.client.fluent.Form.form;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;

import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.storage.oauth2.linkedin.LinkedInState;

/**
 * Provides an authorization service using LinkedIn. 
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/linkedin")
public class LinkedinAuthz {

	/**
	 * The lifetime in seconds of the state.
	 */
	public static final long STATE_EXPIRATION_SECONDS = 1800l; // half an hour	

	public static final String RESOURCE_NAME = ConfigurationManager.LVL_NAME + " LinkedIn AuthZ Resource";
	
	protected final static Logger LOGGER = getLogger(LinkedinAuthz.class);
	
	@POST
	@Path("state")
	@Consumes(APPLICATION_FORM_URLENCODED)
	public Response saveState(final @Context UriInfo uriInfo, final MultivaluedMap<String, String> form) {
		final String secret = parseForm(form, "state");
		final LinkedInState state = LinkedInState.builder()
				.state(secret)
				.issuedAt(currentTimeMillis() / 1000l)
				.expiresIn(STATE_EXPIRATION_SECONDS)
				.build();
		LINKEDIN_STATE_DAO.insert(state);
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(secret);
		return Response.created(uriBuilder.build()).build();
	}

	@GET
	@Path("callback")
	public Response authorize(final @Context HttpServletRequest request) {
		final String code = parseQuery(request, "code");
		final String state = parseQuery(request, "state");
		if (!LINKEDIN_STATE_DAO.isValid(state)) {
			throw new NotAuthorizedException(status(UNAUTHORIZED).build());
		}
		try {
			final List<NameValuePair> form = form()
					.add("grant_type", "authorization_code")
					.add("code", code)
					.add("redirect_uri", "http://lvl.i3m.upv.es/lvl-auth/oauth2/v1/linkedin/callback") // TODO
					.add("client_id", CONFIG_MANAGER.getLinkedInAPIKey())
					.add("client_secret", CONFIG_MANAGER.getLinkedInSecretKey())
					.build();
			final String response = Request.Post("https://www.linkedin.com/uas/oauth2/accessToken")
					.addHeader("Accept", "application/json")
					.bodyForm(form)
					.execute()
					.returnContent()
					.asString();
			final Map<String, String> map = JSON_MAPPER.readValue(response, new TypeReference<HashMap<String,String>>() {});
			
			// TODO
			LOGGER.info("\n\nDONE: access_token=" + map.get("access_token") + ", expires_in=" + map.get("expires_in"));
			// TODO
			
		} catch (IOException e) {
			throw new WebApplicationException(status(INTERNAL_SERVER_ERROR)
					.header("WWW-Authenticate", "Bearer realm='" + RESOURCE_NAME + "', error='invalid-code'")
					.build());
		}

		// TODO http://lvl.i3m.upv.es/auth/linkedin/callback?code=AQQ02yxDKNeICp3N3k60nlbiMwJBRE8w6cI_BX9fSnAiSjax9H5frRQRD-D8c_u4HmhZS0DgvoLEkayho46LrIveDsTWV4jrxFZkl8-gFwgvDdZbmjw&state=k5ovHo2F

		
		/*
		$ curl -H 'Authorization: Bearer AQVGutR88BQv-Qyi1ZK-U3xnyBnp_6yCQdyPaJBMnnpbWwDO8Z5itZL9VYUE4eRgVEdGXi1oy8LO6GDXn3tdH2LYrDmmVM50kCAFfMPmJr0H0-j_X4_Z7hOsDH2RwARiRcLxVzcH8NcrxKG-BQIUVt3ltcYn8ytnLZc3aHfaTGOcAsX6AMA' https://www.linkedin.com/v1/people/~
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<person>
  <id>-pTE7eaWAl</id>
  <first-name>Erik</first-name>
  <last-name>Torres Serrano</last-name>
  <headline>Researcher at Institute of Instrumentation for Molecular Imaging (I3M)</headline>
  <site-standard-profile-request>
    <url>https://www.linkedin.com/profile/view?id=86908844&amp;authType=name&amp;authToken=ifuY&amp;trk=api*a3611313*s3682043*</url>
  </site-standard-profile-request>
</person
		*/
		
		/*
		$ curl -H 'Authorization: Bearer AQVGutR88BQv-Qyi1ZK-U3xnyBnp_6yCQdyPaJBMnnpbWwDO8Z5itZL9VYUE4eRgVEdGXi1oy8LO6GDXn3tdH2LYrDmmVM50kCAFfMPmJr0H0-j_X4_Z7hOsDH2RwARiRcLxVzcH8NcrxKG-BQIUVt3ltcYn8ytnLZc3aHfaTGOcAsX6AMA' https://www.linkedin.com/v1/people/~?format=json
{
  "firstName": "Erik",
  "headline": "Researcher at Institute of Instrumentation for Molecular Imaging (I3M)",
  "id": "-pTE7eaWAl",
  "lastName": "Torres Serrano",
  "siteStandardProfileRequest": {"url": "https://www.linkedin.com/profile/view?id=86908844&authType=name&authToken=ifuY&trk=api*a3611313*s3682043*"}
		*/
		
		/*		
		<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<error>
  <status>401</status>
  <timestamp>1424794394250</timestamp>
  <request-id>5LFR2CG8A5</request-id>
  <error-code>0</error-code>
  <message>Invalid access token.</message>
</error>		
		*/
		
		/*
		access_token=AQVGutR88BQv-Qyi1ZK-U3xnyBnp_6yCQdyPaJBMnnpbWwDO8Z5itZL9VYUE4eRgVEdGXi1oy8LO6GDXn3tdH2LYrDmmVM50kCAFfMPmJr0H0-j_X4_Z7hOsDH2RwARiRcLxVzcH8NcrxKG-BQIUVt3ltcYn8ytnLZc3aHfaTGOcAsX6AMA, expires_in=5183999
		 */
		
		return Response.ok().build();
	}

	private static String parseForm(final MultivaluedMap<String, String> form, final String field) {
		String value = null;
		if (form == null || form.get(field) == null || form.get(field).size() != 1 || isBlank(trimToNull(value = form.getFirst(field)))) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		return value;
	}

	private static String parseQuery(final HttpServletRequest request, final String param) {
		String value = null;
		if (request == null || request.getParameterValues(param) == null || request.getParameterValues(param).length != 1 
				|| isBlank(trimToNull(value = request.getParameter(param)))) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		return value;
	}	

}