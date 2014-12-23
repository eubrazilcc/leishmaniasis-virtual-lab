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
import static eu.eubrazilcc.lvl.storage.dao.PublicLinkDAO.PUBLIC_LINK_DAO;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.mutable.MutableLong;
import org.junit.Test;

import eu.eubrazilcc.lvl.core.PublicLink;
import eu.eubrazilcc.lvl.core.Target;

/**
 * Tests public link collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class PublicLinkCollectionTest {

	@Test
	public void test() {
		System.out.println("PublicLinkCollectionTest.test()");
		try {
			// insert
			final PublicLink publicLink = PublicLink.builder()
					.path("path/files.gz")
					.owner("username1")
					.created(new Date())
					.mime("application/x-gzip")
					.description("Optional description")
					.target(Target.builder().type("sequence").id("JP540074").filter("export_fasta").compression("gzip").build())
					.build();			
			PUBLIC_LINK_DAO.insert(publicLink);

			// find
			PublicLink publicLink2 = PUBLIC_LINK_DAO.find(publicLink.getPath());
			assertThat("public link is not null", publicLink2, notNullValue());
			assertThat("public link coincides with original", publicLink2, equalTo(publicLink));
			System.out.println(publicLink2.toString());

			// update
			publicLink.setDescription("Diferent description");
			PUBLIC_LINK_DAO.update(publicLink);

			// find after update
			publicLink2 = PUBLIC_LINK_DAO.find(publicLink.getPath());
			assertThat("public link is not null", publicLink2, notNullValue());
			assertThat("public link coincides with original", publicLink2, equalTo(publicLink));
			System.out.println(publicLink2.toString());

			// list all by owner Id
			List<PublicLink> publicLinks = PUBLIC_LINK_DAO.findAll(publicLink.getOwner());
			assertThat("public links are not null", publicLinks, notNullValue());
			assertThat("public links are not empty", !publicLinks.isEmpty(), equalTo(true));
			assertThat("number of public links coincides with expected", publicLinks.size(), equalTo(1));
			assertThat("public links coincide with original", publicLinks.get(0), equalTo(publicLink));

			// remove
			PUBLIC_LINK_DAO.delete(publicLink.getPath());
			final long numRecords = PUBLIC_LINK_DAO.count();
			assertThat("number of public links stored in the database coincides with expected", numRecords, equalTo(0l));

			// pagination
			final List<String> ids = newArrayList();
			for (int i = 0; i < 11; i++) {
				final PublicLink publicLink3 = PublicLink.builder()
						.path(Integer.toString(i)).build();
				ids.add(publicLink3.getPath());
				PUBLIC_LINK_DAO.insert(publicLink3);
			}
			final int size = 3;
			int start = 0;
			publicLinks = null;
			final MutableLong count = new MutableLong(0l);
			do {
				publicLinks = PUBLIC_LINK_DAO.list(start, size, null, null, count);
				if (publicLinks.size() != 0) {
					System.out.println("Paging: first item " + start + ", showing " + publicLinks.size() + " of " + count.getValue() + " items");
				}
				start += publicLinks.size();
			} while (!publicLinks.isEmpty());
			for (final String id2 : ids) {			
				PUBLIC_LINK_DAO.delete(id2);
			}
			PUBLIC_LINK_DAO.stats(System.out);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("PublicLinkCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("PublicLinkCollectionTest.test() has finished");
		}
	}

}