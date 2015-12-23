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

package eu.eubrazilcc.lvl.storage;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.storage.dao.PostDAO.POST_DAO;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.mutable.MutableLong;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.Sorting.Order;
import eu.eubrazilcc.lvl.core.community.Post;
import eu.eubrazilcc.lvl.core.community.PostCategory;
import eu.eubrazilcc.lvl.core.community.PostLevel;
import eu.eubrazilcc.lvl.storage.dao.WriteResult;

/**
 * Tests {@link Post} collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class PostCollectionTest {

	@Test
	public void test() {
		System.out.println("PostCollectionTest.test()");
		try {
			// insert
			final Post post = Post.builder()
					.newId()
					.created(new SimpleDateFormat("yyyy-mm-dd").parse("2014-01-01"))
					.category(PostCategory.ANNOUNCEMENT)
					.author("someone@lvl")
					.level(PostLevel.NORMAL)
					.body("New version released!")
					.build();
			WriteResult<Post> writeResult = POST_DAO.insert(post);
			assertThat("insert write result is not null", writeResult, notNullValue());
			assertThat("insert write result Id is not null", writeResult.getId(), notNullValue());
			assertThat("insert write result Id is not empty", isNotBlank(writeResult.getId()), equalTo(true));

			// insert ignoring duplicates			
			try {
				POST_DAO.insert(post, true);
				fail("Expected Exception due to duplicate insertion");
			} catch (RuntimeException expected) {
				System.out.println("Expected exception caught: " + expected.getClass());
			}

			// find
			Post post2 = POST_DAO.find(post.getId());
			assertThat("post is not null", post2, notNullValue());
			assertThat("post coincides with original", post2, equalTo(post));
			System.out.println(post2.toString());

			// update
			post.setBody("Include version number");
			POST_DAO.update(post);

			// find after update
			post2 = POST_DAO.find(post.getId());
			assertThat("post is not null", post2, notNullValue());
			assertThat("post coincides with original", post2, equalTo(post));
			System.out.println(post2.toString());

			// remove
			POST_DAO.delete(post.getId());
			final long numRecords = POST_DAO.count();
			assertThat("number of posts stored in the database coincides with expected", numRecords, equalTo(0l));

			// pagination
			final Random random = new Random();
			final PostCategory[] categories = PostCategory.values();
			final PostLevel[] levels = PostLevel.values();
			final List<String> ids = newArrayList();
			for (int i = 0; i < 11; i++) {				
				final Post post3 = Post.builder()
						.id("post-" + i)
						.created(new Date())
						.category(i < 3 ? PostCategory.INCIDENCE : categories[random.nextInt(categories.length)])
						.author(i%2 == 0 ? "user1@lvl" : "user2@lvl")
						.level(i < 5 ? PostLevel.PROMOTED : levels[random.nextInt(levels.length)])
						.body("Body-" + i)
						.build();
				ids.add(post3.getId());
				POST_DAO.insert(post3);
			}
			final int size = 3;
			int start = 0;
			List<Post> posts = null;
			final MutableLong count = new MutableLong(0l);
			do {
				posts = POST_DAO.list(start, size, null, null, null, count);
				if (posts.size() != 0) {
					System.out.println("Paging: first item " + start + ", showing " + posts.size() + " of " + count.getValue() + " items");
				}
				start += posts.size();
			} while (!posts.isEmpty());

			// hide category and sort by date
			ImmutableMap<String, String> filter = of("category", String.format("!%s", PostCategory.ANNOUNCEMENT.name()));
			Sorting sorting = Sorting.builder()					
					.field("created")
					.order(Order.ASC)
					.build();
			posts = POST_DAO.list(0, Integer.MAX_VALUE, filter, sorting, null, null);
			assertThat("Hide category list coincides with expected", posts, allOf(notNullValue(), not(empty()), 
					hasSize(greaterThanOrEqualTo(3)))); // there are (at least) 3 incidences in the test dataset

			// hide author and sort by date
			filter = of("author", String.format("!%s", "user2@lvl"));			
			posts = POST_DAO.list(0, Integer.MAX_VALUE, filter, sorting, null, null);
			assertThat("Hide author list coincides with expected", posts, allOf(notNullValue(), not(empty()), 
					hasSize(6))); // there are 5 posts of authored by this person in the test dataset

			// hide level and author and sort by date
			filter = of("level", String.format("!%s", PostLevel.NORMAL.name()), "author", String.format("!%s", "user1@lvl"));			
			posts = POST_DAO.list(0, Integer.MAX_VALUE, filter, sorting, null, null);
			assertThat("Hide author list coincides with expected", posts, allOf(notNullValue(), not(empty()), 
					hasSize(greaterThanOrEqualTo(2)))); // there are (at least) 2 promoted posts of authored by this person

			// delete entries from database
			for (final String id2 : ids) {			
				POST_DAO.delete(id2);
			}

			// collection statistics
			POST_DAO.stats(System.out);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("PostCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("PostCollectionTest.test() has finished");
		}
	}

}