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

package eu.eubrazilcc.lvl.storage.oauth2.security;

import static com.google.common.base.Joiner.on;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Iterators.any;
import static com.google.common.collect.Ordering.natural;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.regex.Pattern.matches;
import static org.apache.commons.lang.StringUtils.chop;
import static org.apache.commons.lang.StringUtils.endsWith;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.startsWith;
import static org.apache.commons.lang.StringUtils.substring;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.apache.commons.lang.StringUtils.trimToNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;

/**
 * Defines access scopes.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class ScopeManager {

	private final static Logger LOGGER = LoggerFactory.getLogger(ScopeManager.class);

	public static final String USERS             = "users";
	public static final String SEQUENCES         = "sequences";
	public static final String PENDING_SEQUENCES = "pending_sequences";
	public static final String MY_SEQUENCES      = "my_sequences";
	public static final String PIPELINES         = "pipelines";

	public static final String ALL               = "*";

	public static final char SCOPE_SEPARATOR     = ' ';
	public static final char PATH_SEPARATOR      = '/';
	public static final char ATTRIBUTE_SEPARATOR = ',';

	public static final String FULL_ACCESS_GRANT = "a";

	public static final String MATCH_ALL_MATCHER = PATH_SEPARATOR + "\\" + ALL + "$";
	public static final String MATCH_ALL_REGEX   = "(|/.+)";

	public static final String all() {
		return on(SCOPE_SEPARATOR).skipNulls().join(
				grantFullAccess(inherit(USERS, ALL)),
				grantFullAccess(SEQUENCES), 
				grantFullAccess(PENDING_SEQUENCES),
				grantFullAccess(inherit(MY_SEQUENCES, ALL)),
				grantFullAccess(PIPELINES));
	}

	public static final String user(final String username) {
		checkArgument(isNotBlank(username), "Uninitialized or invalid username");
		return on(SCOPE_SEPARATOR).skipNulls().join(
				grantFullAccess(inherit(USERS, username)),
				SEQUENCES,
				PIPELINES,
				grantFullAccess(inherit(MY_SEQUENCES, username)));
	}

	public static final String dataCurator() {
		return on(SCOPE_SEPARATOR).skipNulls().join(
				grantFullAccess(SEQUENCES), 
				grantFullAccess(PENDING_SEQUENCES));
	}

	public static final String defaultScope() {
		return on(SCOPE_SEPARATOR).skipNulls().join(
				SEQUENCES,
				PIPELINES);
	}

	public static final String grantFullAccess(final String scope) {
		checkArgument(isNotBlank(scope), "Uninitialized or invalid scope");
		return scope.trim() + ATTRIBUTE_SEPARATOR + FULL_ACCESS_GRANT;
	}

	public static final String inherit(final String parent, final String scope) {
		final String pT = cleanScope(parent, true);
		final String sT = cleanScope(scope, false);
		checkArgument(isNotBlank(pT), "Uninitialized or invalid parent scope");
		checkArgument(isNotBlank(sT), "Uninitialized or invalid scope");		
		return pT + PATH_SEPARATOR + sT;
	}

	private static final String cleanScope(final String str, final boolean isParent) {
		final String separator = Character.toString(PATH_SEPARATOR);
		String str2 = trimToEmpty(str);
		if (isParent) {
			while (isNotBlank(str2) && endsWith(str2, separator)) str2 = chop(str2);
		} else {
			while (isNotBlank(str2) && startsWith(str2, separator)) str2 = substring(str2, 1);
		}
		return str2;
	}

	public static final String asOAuthString(final String scope1, final String scope2) {
		checkArgument(isNotBlank(scope1) && isNotBlank(scope2), "Uninitialized or invalid scopes");
		return on(SCOPE_SEPARATOR).skipNulls().join(scope1.trim(), scope2.trim());
	}

	public static final String asOAuthString(final Collection<String> collection) {
		return asOAuthString(collection, false);
	}

	public static final String asOAuthString(final Collection<String> collection, final boolean sort) {
		checkArgument(collection != null && !collection.isEmpty(), "Uninitialized or invalid scope collection");
		final Iterable<String> filtered = filter(transform(collection, new Function<String, String>() {
			@Override
			public String apply(final String scope) {
				return trimToNull(scope);
			}			
		}), notNull());
		return on(SCOPE_SEPARATOR).skipNulls()
				.join(sort ? natural().nullsFirst().immutableSortedCopy(filtered) : filtered);
	}

	public static final List<String> asList(final String scope) {
		checkArgument(isNotBlank(scope), "Uninitialized or invalid scope");
		return Splitter.on(SCOPE_SEPARATOR)
				.omitEmptyStrings()
				.trimResults()
				.splitToList(scope);
	}

	public static final Set<String> asSet(final String scope) {
		final List<String> list = asList(scope);
		if (list != null) {
			return newHashSet(list);
		}
		return newHashSet();
	}

	public static final String resourceScope(final Class<?> resourceType) {
		checkArgument(resourceType != null, "Uninitialized or invalid resource type");
		final Path path = resourceType.getAnnotation(Path.class);
		checkState(path != null && isNotBlank(path.value()), 
				"Invalid path or the specified type does not contain a resource");
		return cleanScope(path.value(), false);
	}

	/**
	 * Extracts the resource path and uses it to check whether or not a resource is accessible 
	 * from the specified scope.
	 * @param resourceType - the type of resource to be accessed
	 * @param scope - the scope
	 * @param requestFullAccess - when {@code true} indicates that the caller is requesting access
	 *        not only for retrieving data, but also it can perform write and control operations 
	 *        on the resource
	 * @return {@code true} is the resource 
	 */
	public static final boolean isAccessible(final Class<?> resourceType, final String scope,
			final boolean requestFullAccess) {
		checkArgument(resourceType != null, "Uninitialized or invalid resource type");
		checkArgument(isNotBlank(scope), "Uninitialized or invalid scope");
		return isAccessible(resourceScope(resourceType), scope, requestFullAccess);
	}

	public static final boolean isAccessible(final String targetScope, final String scope,
			final boolean requestFullAccess) {
		checkArgument(isNotBlank(targetScope), "Uninitialized or invalid target scope");
		checkArgument(isNotBlank(scope), "Uninitialized or invalid scope");
		return isAccessible(targetScope, asList(scope), requestFullAccess);		
	}

	public static final boolean isAccessible(final String targetScope, final Collection<String> availableScopes,
			final boolean requestFullAccess) {
		checkArgument(isNotBlank(targetScope), "Uninitialized or invalid target scope");
		if (availableScopes != null && !availableScopes.isEmpty()) {
			final boolean granted = any(availableScopes.iterator(), new Predicate<String>() {
				@Override
				public boolean apply(final String str) {
					final Iterator<String> iterator = Splitter
							.on(ATTRIBUTE_SEPARATOR)
							.omitEmptyStrings()
							.trimResults()
							.split(str).iterator();
					return scopeMatches(iterator.next(), targetScope) && (!requestFullAccess || isFullAccessGranted(iterator));
				}
			});
			LOGGER.trace((requestFullAccess ? "Full" : "Read only") + " access to scope '" + targetScope + "' from profile " 
					+ Arrays.toString(availableScopes.toArray(new String[availableScopes.size()])) + " was granted: " + granted);
			return granted;
		}
		return false;
	}

	private static final boolean scopeMatches(final String scope, final String targetScope) {
		return matches(scope.replaceFirst(MATCH_ALL_MATCHER, MATCH_ALL_REGEX), targetScope);
	}

	private static final boolean isFullAccessGranted(final Iterator<String> attributes) {
		if (attributes.hasNext()) {
			return attributes.next().trim().equals(FULL_ACCESS_GRANT) ? true : isFullAccessGranted(attributes);
		}
		return false;
	}	

}