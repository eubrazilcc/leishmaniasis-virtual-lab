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

package eu.eubrazilcc.lvl.storage.security;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Range.open;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trimToEmpty;

import java.util.List;

import com.google.common.base.Splitter;

/**
 * Identity provider helper.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class IdentityProviderHelper {

	public static final String LVL_IDENTITY_PROVIDER = "lvl";
	public static final String LINKEDIN_IDENTITY_PROVIDER = "linkedin";

	public static final String IDENTITY_SEPARATOR = "@";
	
	public static final String OWNERID_EL_TEMPLATE = "${user.userid}" + IDENTITY_SEPARATOR + "${user.provider}";

	public static final String defaultIdentityProvider() {
		return LVL_IDENTITY_PROVIDER;
	}

	public static final String toResourceOwnerId(final String userid) {
		return toResourceOwnerId(defaultIdentityProvider(), userid);
	}

	public static final String toResourceOwnerId(final User user) {
		checkArgument(user != null, "Uninitialized or invalid user");
		final String provider = user.getProvider();
		return isNotBlank(provider) ? toResourceOwnerId(provider, user.getUserid()) : toResourceOwnerId(user.getUserid());	
	}

	public static final String toResourceOwnerId(final String provider, final String userid) {
		checkArgument(isNotBlank(provider), "Uninitialized or invalid provider");
		checkArgument(isNotBlank(userid), "Uninitialized or invalid user Id");
		return userid.trim() + IDENTITY_SEPARATOR + provider.trim();
	}

	public static final String getIdentityProvider(final String ownerid) {
		assertValidResourceOwnerId(ownerid);
		final List<String> tokens = Splitter.on(IDENTITY_SEPARATOR)
				.trimResults()
				.omitEmptyStrings()
				.splitToList(ownerid);
		checkArgument(tokens != null && tokens.size() == 2, "Invalid resource owner Id: " + ownerid);
		return tokens.get(1);				
	}

	public static String assertValidResourceOwnerId(final String ownerid) {
		final String ownerid2 = trimToEmpty(ownerid);
		checkArgument(isNotBlank(ownerid2), "Uninitialized or invalid resource owner Id");
		checkArgument(open(0, ownerid2.length()).contains(ownerid2.indexOf(IDENTITY_SEPARATOR)) && !ownerid2.matches(".*\\s+.*"), 
				"Invalid resource owner Id: " + ownerid);
		return ownerid2;
	}

}