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

package eu.eubrazilcc.lvl.storage.transform;

import java.util.List;

import javax.ws.rs.core.Link;

import eu.eubrazilcc.lvl.storage.oauth2.UserType;

/**
 * A {@link TransientStore} that extracts from an entity that implements {@link UserType} the volatile information referred to the user. This 
 * information is stored in this class and can be reinserted at any time in the entity.
 * @author Erik Torres <ertorser@upv.es>
 * @param <T> - the type of elements in this store
 */
public class UserTransientStore<T extends UserType> extends TransientStore<T> {

	private List<Link> links;
	private String prictureUrl;

	public UserTransientStore(final T element) {
		super(element);
	}

	public List<Link> getLinks() {
		return links;
	}

	public String getPrictureUrl() {
		return prictureUrl;
	}

	public T purge() {
		// store
		links = element.getUser().getLinks();
		prictureUrl = element.getUser().getPictureUrl();
		// remove
		element.getUser().setLinks(null);
		element.getUser().setPictureUrl(null);			
		return element;
	}

	public T restore() {
		element.getUser().setLinks(links);
		element.getUser().setPictureUrl(prictureUrl);
		return element;
	}

	/* Fluent API */

	public static <T extends UserType> UserTransientStore<T> startStore(final T element) {
		return new UserTransientStore<T>(element);
	}

}