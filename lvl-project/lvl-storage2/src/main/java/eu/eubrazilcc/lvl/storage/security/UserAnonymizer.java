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

package eu.eubrazilcc.lvl.storage.security;

import com.google.common.base.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import eu.eubrazilcc.lvl.storage.oauth2.ResourceOwner;

/**
 * Removes the information that can be used to impersonate a user, such as passwords 
 * and e-mail addresses, enabling the transfer of information across a boundary and 
 * reducing the risk of unintended disclosure of user details.
 * @author Erik Torres <ertorser@upv.es>
 */
public class UserAnonymizer implements Function<ResourceOwner, User> {	
	
	private AnonymizationLevel level = AnonymizationLevel.HARD;	
	
	public AnonymizationLevel getLevel() {
		return level;
	}

	public void setLevel(final AnonymizationLevel level) {
		this.level = checkNotNull(level, "Anonymization level cannot be null");
	}

	@Override
	public User apply(final ResourceOwner owner) {
		if (owner != null && owner.getUser() != null) {
			final User user = owner.getUser();
			switch (level) {
			case MEDIUM:
				user.setPassword("***");
				user.setSalt("***");
				break;
			case HARD:
				user.setPassword("***");
				user.setSalt("***");
				user.setEmail("***");
				break;
			case NONE:
			default:
				break;
			}
			return user;
		}		
		return null;
	}
	
	public static final UserAnonymizer start(final AnonymizationLevel level) {		
		final UserAnonymizer anonymizer = new UserAnonymizer();
		anonymizer.setLevel(level);
		return anonymizer;
	}
	
	public static enum AnonymizationLevel {
		/**
		 * Password and e-mail addresses are removed from users.
		 */
		HARD,
		/**
		 * Password is removed from users.
		 */
		MEDIUM,
		/**
		 * Nothing is removed.
		 */
		NONE;
	}
	
}