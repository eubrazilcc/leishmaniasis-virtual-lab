package eu.eubrazilcc.lvl.core.rest;

import static eu.eubrazilcc.lvl.core.http.HttpMethod.allowedHttpMethods;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import eu.eubrazilcc.lvl.core.http.HttpMethod;

/**
 * Apply this filter to the server response to enable Cross-origin resource sharing (CORS).
 * Registering this filter as a provider with a JAX-RS application will enable CORS in all
 * the HTTP methods allowed in the application: {@link HttpMethod#allowedHttpMethods()}.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://www.w3.org/TR/cors/">W3C -- Cross-Origin Resource Sharing</a>
 */
@Provider
public class CrossDomainFilter implements ContainerResponseFilter {

	/**
	 * Adds a set of response headers "Access-Control-Allow-*" to each generated response.
	 * in order to enable CORS in a JAX-RS application.
	 * @param requestContext - the container request (input)
	 * @param responseContext - the container response (output)
	 */
	@Override
	public void filter(final ContainerRequestContext requestContext,
			final ContainerResponseContext responseContext) throws IOException {
		responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
		responseContext.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
		responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
		responseContext.getHeaders().add("Access-Control-Allow-Methods", allowedHttpMethods());
		// store CORS response in client cache and use it for subsequent requests within 1 week
		responseContext.getHeaders().add("Access-Control-Max-Age", "604800");
	}

}