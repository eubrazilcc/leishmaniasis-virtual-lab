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

package eu.eubrazilcc.lvl.storage;

import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO.RESOURCE_OWNER_DAO;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.mutable.MutableLong;
import org.junit.Test;

import eu.eubrazilcc.lvl.storage.oauth2.ResourceOwner;
import eu.eubrazilcc.lvl.storage.oauth2.User;
import eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO;

/**
 * Tests OAuth2 resource owner collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ResourceOwnerCollectionTest {

	@Test
	public void test() {
		System.out.println("ResourceOwnerCollectionTest.test()");
		try {
			final Collection<String> scopes = newArrayList("scope1", "scope2");

			// insert
			final ResourceOwner resourceOwner = ResourceOwner.builder()
					.id("username")
					.user(User.builder()
							.username("username")
							.password("password")
							.email("username@example.com")
							.fullname("Fullname")
							.scopes(scopes)
							.build()).build();
			RESOURCE_OWNER_DAO.insert(resourceOwner);

			// find
			ResourceOwner resourceOwner2 = RESOURCE_OWNER_DAO.find(resourceOwner.getOwnerId());
			assertThat("resource owner is not null", resourceOwner2, notNullValue());
			assertThat("resource owner coincides with original", resourceOwner2, equalTo(resourceOwner));
			System.out.println(resourceOwner2.toString());

			// find with volatile values
			resourceOwner2 = RESOURCE_OWNER_DAO.baseUri(new URI("http://localhost/"))
					.useGravatar(true).find(resourceOwner.getOwnerId());
			assertThat("resource owner with volatile values is not null", resourceOwner2, notNullValue());
			assertThat("resource owner link is not null", resourceOwner2.getUser().getLink(), notNullValue());
			assertThat("resource owner picture URL is not null", resourceOwner2.getUser().getPictureUrl(), notNullValue());
			assertThat("resource owner picture URL is not empty", isNotBlank(resourceOwner2.getUser().getPictureUrl()));
			assertThat("resource owner with volatile values coincides with original", 
					resourceOwner2.getUser().equalsIgnoringVolatile(resourceOwner.getUser()));
			System.out.println(resourceOwner2.toString());

			// update
			resourceOwner.getUser().setPassword("new_password");
			RESOURCE_OWNER_DAO.update(resourceOwner);

			// find after update
			resourceOwner2 = RESOURCE_OWNER_DAO.reset().find(resourceOwner.getOwnerId());
			assertThat("resource owner is not null", resourceOwner2, notNullValue());
			assertThat("resource owner coincides with original", resourceOwner2, equalTo(resourceOwner));
			System.out.println(resourceOwner2.toString());

			// check validity using owner Id and username
			boolean validity = RESOURCE_OWNER_DAO.isValid(resourceOwner.getOwnerId(), 
					resourceOwner.getUser().getUsername(), 
					resourceOwner.getUser().getPassword(), 
					false, 
					null);
			assertThat("resource owner is valid (using owner Id & username)", validity);

			// check validity using email address
			validity = RESOURCE_OWNER_DAO.isValid(null, 
					resourceOwner.getUser().getEmail(), 
					resourceOwner.getUser().getPassword(), 
					true, 
					null);
			assertThat("resource owner is valid (using email)", validity);			

			// add scopes
			RESOURCE_OWNER_DAO.addScopes(resourceOwner.getOwnerId(), "scope3");

			// remove scopes
			RESOURCE_OWNER_DAO.removeScopes(resourceOwner.getOwnerId(), "scope2");

			// get OAuth scope
			resourceOwner2 = RESOURCE_OWNER_DAO.find(resourceOwner.getOwnerId());
			final String oauthScope = ResourceOwnerDAO.oauthScope(resourceOwner2, true);
			assertThat("resource owner OAuth scope is not null", oauthScope, notNullValue());
			assertThat("resource owner OAuth scope is not blank", isNotBlank(oauthScope));
			assertThat("resource owner OAuth scope coincided with expected", oauthScope, equalTo("scope1 scope3"));
			System.out.println("OAuth scope: '" + oauthScope + "'");

			// remove
			RESOURCE_OWNER_DAO.delete(resourceOwner.getOwnerId());

			// pagination
			final List<String> ids = newArrayList();
			for (int i = 0; i < 11; i++) {
				final ResourceOwner resourceOwner3 = ResourceOwner.builder()
						.id(Integer.toString(i))
						.user(User.builder()
								.username(Integer.toString(i))
								.password("password")
								.email("username" + i + "@example.com")
								.fullname("Fullname")
								.scopes(scopes)
								.build()).build();								
				ids.add(resourceOwner3.getOwnerId());
				RESOURCE_OWNER_DAO.insert(resourceOwner3);
			}
			final int size = 3;
			int start = 0;
			List<ResourceOwner> resourceOwners = null;
			final MutableLong count = new MutableLong(0l);
			do {
				resourceOwners = RESOURCE_OWNER_DAO.list(start, size, count);
				if (resourceOwners.size() != 0) {
					System.out.println("Paging " + start + " - " + resourceOwners.size() + " of " + count.getValue());
				}
				start += resourceOwners.size();
			} while (!resourceOwners.isEmpty());
			for (final String id2 : ids) {			
				RESOURCE_OWNER_DAO.delete(id2);
			}
			RESOURCE_OWNER_DAO.stats(System.out);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("ResourceOwnerCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("ResourceOwnerCollectionTest.test() has finished");
		}
	}

}