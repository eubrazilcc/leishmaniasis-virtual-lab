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
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO.RESOURCE_OWNER_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO.updatePassword;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.core.Link;

import org.apache.commons.lang.mutable.MutableLong;
import org.junit.Test;

import eu.eubrazilcc.lvl.core.DatasetShare;
import eu.eubrazilcc.lvl.storage.dao.WriteResult;
import eu.eubrazilcc.lvl.storage.oauth2.ResourceOwner;
import eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO;
import eu.eubrazilcc.lvl.storage.security.User;

/**
 * Tests OAuth2 resource owner collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ResourceOwnerCollectionTest {

	@Test
	public void test() {
		System.out.println("ResourceOwnerCollectionTest.test()");
		try {
			final Collection<String> roles = newArrayList("role1", "role2");
			final Collection<String> permissions = newArrayList("sequences:leishmania:public:*:view", "sequences:sandflies:public:*:view");
			// insert (no salt)
			final ResourceOwner resourceOwner = ResourceOwner.builder()
					.user(User.builder()
							.userid("username")
							.password("password")
							.email("username@example.com")
							.fullname("Fullname")
							.roles(roles)
							.permissions(permissions)
							.build()).build();
			WriteResult<ResourceOwner> result = RESOURCE_OWNER_DAO.insert(resourceOwner);
			assertThat("insert resource owner result is not null", result, notNullValue());
			assertThat("insert resource owner result id is not null", result.getId(), notNullValue());
			assertThat("insert resource owner result id is not empty", isNotBlank(result.getId()), equalTo(true));
			assertThat("insert resource owner result element is not null", result.getElement(), notNullValue());
			assertThat("insert resource owner result user is not null", result.getElement().getUser(), notNullValue());
			assertThat("insert resource owner result hashed password", result.getElement().getUser().getPassword(), notNullValue());
			assertThat("insert resource owner result hashed password", isNotBlank(result.getElement().getUser().getPassword()), equalTo(true));			
			assertThat("insert resource owner result salt", result.getElement().getUser().getSalt(), notNullValue());
			assertThat("insert resource owner result salt", isNotBlank(result.getElement().getUser().getSalt()), equalTo(true));
			assertThat("inserted resource owner coincides with original (ignoring password & salt)", 
					resourceOwner.equalsToUnprotected(result.getElement()), equalTo(true));
			final ResourceOwner hashed = result.getElement();			

			// find (no salt)
			ResourceOwner resourceOwner2 = RESOURCE_OWNER_DAO.find(resourceOwner.getOwnerId());
			assertThat("resource owner is not null", resourceOwner2, notNullValue());
			assertThat("resource owner coincides with original", resourceOwner2, equalTo(hashed));
			System.out.println(resourceOwner2.toString());

			// find (no salt) with volatile values
			resourceOwner2 = RESOURCE_OWNER_DAO.useGravatar(true).find(resourceOwner.getOwnerId());
			assertThat("resource owner with volatile values is not null", resourceOwner2, notNullValue());
			assertThat("resource owner links are null", resourceOwner2.getUser().getLinks(), nullValue());
			assertThat("resource owner picture URL is not null", resourceOwner2.getUser().getPictureUrl(), notNullValue());
			assertThat("resource owner picture URL is not empty", isNotBlank(resourceOwner2.getUser().getPictureUrl()));
			assertThat("resource owner with volatile values coincides with original", 
					resourceOwner2.getUser().equalsIgnoringVolatile(hashed.getUser()));
			System.out.println(resourceOwner2.toString());

			// insert element with hard link
			final ResourceOwner resourceOwner1 = ResourceOwner.builder()
					.user(User.builder()
							.links(newArrayList(Link.fromUri("http://example.com/users/username1").rel(SELF).type(APPLICATION_JSON).build()))
							.userid("username1")
							.password("password1")
							.email("username1@example.com")
							.fullname("Fullname 1")
							.roles(roles)
							.permissions(permissions)
							.build()).build();

			result = RESOURCE_OWNER_DAO.insert(resourceOwner1);
			resourceOwner1.getUser().setLinks(null);
			assertThat("resource owner result inserted with hard link is not null", result, notNullValue());
			assertThat("resource owner inserted with hard link is not null", result.getElement(), notNullValue());
			assertThat("resource owner inserted with hard link coincides with original (ignoring password & salt)", 
					resourceOwner1.equalsToUnprotected(result.getElement()), equalTo(true));
			System.out.println(resourceOwner2.toString());

			RESOURCE_OWNER_DAO.delete(resourceOwner1.getOwnerId());

			// update
			final String plainPassword = "new_password";
			updatePassword(hashed, plainPassword);
			RESOURCE_OWNER_DAO.update(hashed);

			// find after update
			resourceOwner2 = RESOURCE_OWNER_DAO.reset().find(resourceOwner.getOwnerId());
			assertThat("resource owner is not null", resourceOwner2, notNullValue());
			assertThat("resource owner coincides with original", resourceOwner2, equalTo(hashed));
			System.out.println(resourceOwner2.toString());

			// check validity using owner Id and username
			AtomicReference<String> ownerIdRef = new AtomicReference<String>();
			boolean validity = RESOURCE_OWNER_DAO.isValid(hashed.getOwnerId(), 
					hashed.getUser().getUserid(), 
					plainPassword, 
					false, 
					null,
					ownerIdRef);
			assertThat("resource owner is valid (using owner Id & username)", validity, equalTo(true));
			assertThat("resource owner Id passed as reference coincides with expected", ownerIdRef.get(), equalTo(hashed.getOwnerId()));

			// check validity using email address
			ownerIdRef = new AtomicReference<String>();
			validity = RESOURCE_OWNER_DAO.isValid(null, 
					hashed.getUser().getEmail(), 
					plainPassword, 
					true, 
					null,
					ownerIdRef);
			assertThat("resource owner is valid (using email)", validity, equalTo(true));
			assertThat("resource owner Id passed as reference coincides with expected", ownerIdRef.get(), equalTo(hashed.getOwnerId()));

			// add roles
			RESOURCE_OWNER_DAO.addRoles(resourceOwner.getOwnerId(), "role3");

			// remove roles
			RESOURCE_OWNER_DAO.removeRoles(resourceOwner.getOwnerId(), "role2");

			// test listing unexisting shared datasets
			List<DatasetShare> shares = RESOURCE_OWNER_DAO.listDatashares("otheruser@lvl", "mysequences.xml", 0, Integer.MAX_VALUE, null, null, null);
			assertThat("dataset shares is null", shares, notNullValue());
			assertThat("dataset shares is empty", shares.isEmpty(), equalTo(true));
			// uncomment for additional output
			System.out.println(" >> Dataset shares (before permissions are granted): " + shares.toString());

			// share dataset by adding permissions to resource owner
			RESOURCE_OWNER_DAO.addPermissions(resourceOwner.getOwnerId(), "datasets:files:otheruser@lvl:mysequences.xml:view");
			resourceOwner2 = RESOURCE_OWNER_DAO.reset().find(resourceOwner.getOwnerId());
			assertThat("resource owner is not null", resourceOwner2, notNullValue());
			// uncomment for additional output
			System.out.println(" >> Owner with permissions to view shared dataset: " + resourceOwner2.toString());

			// test listing shared datasets
			shares = RESOURCE_OWNER_DAO.listDatashares("otheruser@lvl", "mysequences.xml", 0, Integer.MAX_VALUE, null, null, null);
			assertThat("dataset shares is not null", shares, notNullValue());
			assertThat("number of dataset shares coincides with expected", shares.size(), equalTo(1));
			// uncomment for additional output
			System.out.println(" >> Dataset shares (after permissions are granted): " + shares.toString());

			// insert redundant permissions
			RESOURCE_OWNER_DAO.addPermissions(resourceOwner.getOwnerId(), "datasets:files:otheruser@lvl:mysequences.xml:view,edit");

			// test getting information about a specific share
			DatasetShare share = RESOURCE_OWNER_DAO.findDatashare("otheruser@lvl", "mysequences.xml", resourceOwner.getOwnerId());
			assertThat("dataset share is not null", share, notNullValue());
			// uncomment for additional output
			System.out.println(" >> Dataset share (after adding redundant permissions): " + share.toString());

			// test modifying a share
			// not available
			
			// remove all permissions and stop sharing
			RESOURCE_OWNER_DAO.removePermissions(resourceOwner.getOwnerId(), "datasets:files:otheruser@lvl:mysequences.xml:view",
					"datasets:files:otheruser@lvl:mysequences.xml:view,edit");
			shares = RESOURCE_OWNER_DAO.listDatashares("otheruser@lvl", "mysequences.xml", 0, Integer.MAX_VALUE, null, null, null);
			assertThat("dataset shares is null", shares, notNullValue());
			assertThat("dataset shares is empty", shares.isEmpty(), equalTo(true));
			// uncomment for additional output
			System.out.println(" >> Dataset shares (after permissions are removed): " + shares.toString());

			// get OAuth scope
			resourceOwner2 = RESOURCE_OWNER_DAO.find(resourceOwner.getOwnerId());
			final String oauthScope = ResourceOwnerDAO.oauthScope(resourceOwner2, true);
			assertThat("resource owner OAuth scope is not null", oauthScope, notNullValue());
			assertThat("resource owner OAuth scope is not blank", isNotBlank(oauthScope));
			assertThat("resource owner OAuth scope coincided with expected", oauthScope, equalTo("role1 role3"));
			System.out.println("OAuth scope: '" + oauthScope + "'");

			// remove (default LVL administrator is not removed)
			RESOURCE_OWNER_DAO.delete(resourceOwner.getOwnerId());
			final long numRecords = RESOURCE_OWNER_DAO.count();
			assertThat("number of resource owners stored in the database coincides with expected", numRecords, equalTo(1l));

			// insert (with salt)
			result = RESOURCE_OWNER_DAO.insert(hashed);
			assertThat("insert resource owner result (with salt) is not null", result, notNullValue());
			assertThat("insert resource owner result (with salt) id is not null", result.getId(), notNullValue());
			assertThat("insert resource owner result (with salt) id is not empty", isNotBlank(result.getId()), equalTo(true));

			// find (with salt)
			resourceOwner2 = RESOURCE_OWNER_DAO.find(hashed.getOwnerId());
			assertThat("resource owner (with salt) is not null", resourceOwner2, notNullValue());
			assertThat("resource owner (with salt) coincides with original", resourceOwner2, equalTo(hashed));
			System.out.println(resourceOwner2.toString());

			// pagination
			final List<String> ids = newArrayList();
			for (int i = 0; i < 11; i++) {
				final ResourceOwner resourceOwner3 = ResourceOwner.builder()
						.user(User.builder()
								.userid(Integer.toString(i))
								.password("password")
								.email("username" + i + "@example.com")
								.fullname("Fullname")
								.roles(roles)
								.permissions(permissions)
								.build()).build();								
				ids.add(resourceOwner3.getOwnerId());
				RESOURCE_OWNER_DAO.insert(resourceOwner3);
			}
			final int size = 3;
			int start = 0;
			List<ResourceOwner> resourceOwners = null;
			final MutableLong count = new MutableLong(0l);
			do {
				resourceOwners = RESOURCE_OWNER_DAO.list(start, size, null, null, null, count);
				if (resourceOwners.size() != 0) {
					System.out.println("Paging: first item " + start + ", showing " + resourceOwners.size() + " of " + count.getValue() + " items");
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