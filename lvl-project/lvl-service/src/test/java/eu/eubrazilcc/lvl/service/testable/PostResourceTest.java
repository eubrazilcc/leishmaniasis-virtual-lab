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

package eu.eubrazilcc.lvl.service.testable;

import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_DEFAULT_NS;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.LAST;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static eu.eubrazilcc.lvl.core.util.UrlUtils.getPath;
import static eu.eubrazilcc.lvl.core.util.UrlUtils.getQueryParams;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.toJson;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JsonOptions.JSON_PRETTY_PRINTER;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.HEADER_AUTHORIZATION;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager.bearerHeader;
import static java.lang.Integer.parseInt;
import static java.lang.Math.min;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;

import java.net.URI;
import java.util.Date;
import java.util.Random;

import javax.ws.rs.Path;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;

import eu.eubrazilcc.lvl.core.community.Post;
import eu.eubrazilcc.lvl.core.community.PostCategory;
import eu.eubrazilcc.lvl.core.community.PostLevel;
import eu.eubrazilcc.lvl.service.Posts;
import eu.eubrazilcc.lvl.service.TotalCount;
import eu.eubrazilcc.lvl.service.rest.PostResource;
import eu.eubrazilcc.lvl.test.TestContext;
import eu.eubrazilcc.lvl.test.Testable;

/**
 * Tests access to paper citations collection in the REST API.
 * @author Erik Torres <ertorser@upv.es>
 */
public class PostResourceTest extends Testable {

	public PostResourceTest(final TestContext testCtxt) {
		super(testCtxt, PostResourceTest.class, true);
	}

	@Override
	public void test() throws Exception {
		// test create new post
		final Post post = Post.builder()
				.category(PostCategory.ANNOUNCEMENT)
				.author(testCtxt.ownerid("user1"))
				.level(PostLevel.NORMAL)
				.body("New version released!")
				.build();

		final Path path = PostResource.class.getAnnotation(Path.class);		
		Response response = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS)).request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.post(entity(post, APPLICATION_JSON));
		assertThat("Create new post response is not null", response, notNullValue());
		assertThat("Create new post response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create new post response is not empty", response.getEntity(), notNullValue());
		String payload = response.readEntity(String.class);
		assertThat("Create new post response entity is not null", payload, notNullValue());
		assertThat("Create new post response entity is empty", isBlank(payload));
		// uncomment for additional output			
		printMsg(" >> Create new post response body (JSON), empty is OK: " + payload);
		printMsg(" >> Create new post response JAX-RS object: " + response);
		printMsg(" >> Create new post HTTP headers: " + response.getStringHeaders());

		URI locationUri = new URI((String)response.getHeaders().get("Location").get(0));
		assertThat("Created location is not null", locationUri, notNullValue());
		assertThat("Created location path is not empty", trim(locationUri.getPath()), allOf(notNullValue(), not(equalTo(""))));
		String postId = getName(locationUri.toURL().getPath());
		assertThat("Created Id is not empty", trim(postId), allOf(notNullValue(), not(equalTo(""))));

		// test get post by id (Java object)
		Post post2 = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS)).path(postId)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user2")))
				.get(Post.class);
		assertThat("Get post by Id result is not null", post2, notNullValue());
		assertThat("Post Id is not empty", trim(post2.getId()), allOf(notNullValue(), not(equalTo(""))));
		assertThat("Post creation date is not null", post2.getCreated(), notNullValue());
		post.setId(post2.getId());
		post.setCreated(post2.getCreated());		

		assertThat("Get post by Id coincides with expected", post2.equalsIgnoringVolatile(post));
		// uncomment for additional output
		printMsg(" >> Get post by Id result: " + post2.toString());

		// test list all posts (JSON encoded)
		response = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS)).request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user2")))
				.get();
		assertThat("Get posts response is not null", response, notNullValue());
		assertThat("Get posts response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Get posts response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Get posts response entity is not null", payload, notNullValue());
		assertThat("Get posts response entity is not empty", isNotBlank(payload));
		// uncomment for additional output			
		printMsg(" >> Get posts response body (JSON): " + payload);
		printMsg(" >> Get posts response JAX-RS object: " + response);
		printMsg(" >> Get posts HTTP headers: " + response.getStringHeaders());

		// test list all posts (Java object)
		Posts posts = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS)).request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user3")))
				.get(Posts.class);
		assertThat("Get posts result is not null", posts, notNullValue());
		assertThat("Get posts list is not null", posts.getElements(), notNullValue());
		assertThat("Get posts list is not empty", !posts.getElements().isEmpty());
		assertThat("Get posts items count coincide with list size", posts.getElements().size(), equalTo(posts.getTotalCount()));
		// uncomment for additional output			
		printMsg(" >> Get posts result: " + posts.toString());			

		// test update post
		post.setBody("New body");
		response = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS)).path(postId)
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.put(entity(post, APPLICATION_JSON));
		assertThat("Update post response is not null", response, notNullValue());
		assertThat("Update post response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Update post response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Update post response entity is not null", payload, notNullValue());
		assertThat("Update post response entity is empty", isBlank(payload));
		// uncomment for additional output			
		printMsg(" >> Update post response body (JSON), empty is OK: " + payload);
		printMsg(" >> Update post response JAX-RS object: " + response);
		printMsg(" >> Update post HTTP headers: " + response.getStringHeaders());

		// test get post by Id after update
		post2 = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS)).path(postId)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(Post.class);
		assertThat("Get post by Id after update result is not null", post2, notNullValue());
		assertThat("Get post by Id after update coincides with expected", post2.equalsIgnoringVolatile(post));
		// uncomment for additional output
		printMsg(" >> Get post by Id after update result: " + post2.toString());

		// test delete post
		response = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS)).path(postId)
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.delete();
		assertThat("Delete post response is not null", response, notNullValue());
		assertThat("Delete post response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Delete post response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Delete post response entity is not null", payload, notNullValue());
		assertThat("Delete post response entity is empty", isBlank(payload));
		// uncomment for additional output			
		printMsg(" >> Delete post response body (JSON), empty is OK: " + payload);
		printMsg(" >> Delete post response JAX-RS object: " + response);
		printMsg(" >> Delete post HTTP headers: " + response.getStringHeaders());

		// create a larger dataset to test complex operations
		Date created7 = null;		
		final Random random = new Random();
		final PostCategory[] categories = PostCategory.values();
		final PostLevel[] levels = PostLevel.values();
		final int numItems = 11;
		for (int i = 0; i < numItems; i++) {
			final Post post3 = Post.builder()
					.category(i < 3 ? PostCategory.INCIDENCE : categories[random.nextInt(categories.length)])
					.level(i < 5 ? PostLevel.PROMOTED : levels[random.nextInt(levels.length)])
					.body("Body-" + i)
					.build();
			if (i == 7) created7 = new Date();
			Thread.sleep(500l);
			testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS)).request()
			.header(HEADER_AUTHORIZATION, bearerHeader(i%2 == 0 ? testCtxt.token("user1") : testCtxt.token("user2")))
			.post(entity(post3, APPLICATION_JSON));			
		}

		// test pagination (JSON encoded)
		final int perPage = 2;
		response = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("per_page", perPage)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user2")))
				.get();
		assertThat("Paginate posts first page response is not null", response, notNullValue());
		assertThat("Paginate posts first page response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Paginate posts first page response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Paginate posts first page response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));			
		posts = testCtxt.jsonMapper().readValue(payload, Posts.class);
		assertThat("Paginate posts first page result is not null", posts, notNullValue());
		assertThat("Paginate posts first page list coincides with expected", posts.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(min(perPage, posts.getTotalCount()))));		
		// uncomment for additional output			
		printMsg(" >> Paginate posts first page response body (JSON): " + payload);

		assertThat("Paginate posts first page links coincides with expected", posts.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));		
		Link lastLink = null;
		for (int i = 0; i < posts.getLinks().size() && lastLink == null; i++) {
			final Link link = posts.getLinks().get(i);
			if (LAST.equalsIgnoreCase(link.getRel())) {
				lastLink = link;
			}
		}
		assertThat("Paginate posts first page link to last page is not null", lastLink, notNullValue());

		response = testCtxt.target().path(getPath(lastLink).substring(testCtxt.service().length()))
				.queryParam("page", parseInt(getQueryParams(lastLink).get("page")))
				.queryParam("per_page", parseInt(getQueryParams(lastLink).get("per_page")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user2")))
				.get();
		assertThat("Paginate posts last page response is not null", response, notNullValue());
		assertThat("Paginate posts last page response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Paginate posts last page response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Paginate posts last page response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));
		posts = testCtxt.jsonMapper().readValue(payload, Posts.class);
		assertThat("Paginate posts last page result is not null", posts, notNullValue());
		assertThat("Paginate posts last page list is not empty", posts.getElements(), allOf(notNullValue(), not(empty())));		
		// uncomment for additional output			
		printMsg(" >> Paginate posts last page response body (JSON): " + payload);

		assertThat("Paginate posts last page links coincide with expected", posts.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));

		// test pagination (Java object)
		posts = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("per_page", perPage)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user3")))
				.get(Posts.class);
		assertThat("Paginate posts first page result is not null", posts, notNullValue());
		assertThat("Paginate posts first page list coincides with expected", posts.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(min(perPage, posts.getTotalCount()))));		
		// uncomment for additional output
		printMsg(" >> Paginate posts first page result: " + toJson(posts, JSON_PRETTY_PRINTER));

		assertThat("Paginate posts first page links coincide with expected", posts.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));
		lastLink = null;
		for (int i = 0; i < posts.getLinks().size() && lastLink == null; i++) {
			final Link link = posts.getLinks().get(i);
			if (LAST.equalsIgnoreCase(link.getRel())) {
				lastLink = link;
			}
		}
		assertThat("Paginate posts first page link to last page is not null", lastLink, notNullValue());

		posts = testCtxt.target().path(getPath(lastLink).substring(testCtxt.service().length()))
				.queryParam("page", parseInt(getQueryParams(lastLink).get("page")))
				.queryParam("per_page", parseInt(getQueryParams(lastLink).get("per_page")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user3")))
				.get(Posts.class);
		assertThat("Paginate posts last page result is not null", posts, notNullValue());
		assertThat("Paginate posts last page list is not empty", posts.getElements(), allOf(notNullValue(), not(empty())));		
		// uncomment for additional output
		printMsg(" >> Paginate posts last page result: " + toJson(posts, JSON_PRETTY_PRINTER));

		assertThat("Paginate posts last page links coincide with expected", posts.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));

		// filter posts created after a given time
		posts = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("per_page", 100)
				.queryParam("q", String.format("created:\">%d\"", created7.getTime()))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Posts.class);
		assertThat("Filter by creation time result is not null", posts, notNullValue());
		assertThat("Filter by creation time list coincides with expected", posts.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(posts.getTotalCount()), hasSize(4)));
		for (final Post p : posts.getElements()) {
			assertThat("posts are properly filtered by creation date", p.getCreated().after(created7));
		}
		printMsg(" >> Search posts result: " + toJson(posts, JSON_PRETTY_PRINTER));

		// hide category and sort by date
		posts = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("per_page", 100)
				.queryParam("q", String.format("category:\"!%s\"", PostCategory.ANNOUNCEMENT.name()))
				.queryParam("sort", "created")
				.queryParam("order", "asc")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Posts.class);
		assertThat("Hide category result is not null", posts, notNullValue());
		assertThat("Hide category list coincides with expected", posts.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(posts.getTotalCount()), hasSize(greaterThanOrEqualTo(3)))); // there are (at least) 3 incidences in the test dataset
		printMsg(" >> Search posts result: " + toJson(posts, JSON_PRETTY_PRINTER));

		response = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("per_page", 100)
				.queryParam("q", String.format("category:\"!%s\"", PostCategory.ANNOUNCEMENT.name()))
				.queryParam("sort", "created")
				.queryParam("order", "asc")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get();
		assertThat("Hide category (JSON encoded) response is not null", response, notNullValue());
		assertThat("Hide category (JSON encoded) response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Hide category (JSON encoded) response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Hide category (JSON encoded) response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));		
		posts = testCtxt.jsonMapper().readValue(payload, Posts.class);
		assertThat("Hide category (JSON encoded) result is not null", posts, notNullValue());
		assertThat("Hide category (JSON encoded) items coincide with expected", posts.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(min(100, posts.getTotalCount())), hasSize(greaterThanOrEqualTo(3)))); // there are (at least) 3 incidences in the test dataset
		printMsg(" >> Search posts response body (JSON): " + payload);

		// hide author and sort by date
		posts = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("per_page", 100)
				.queryParam("q", String.format("author:\"!%s\"", "user2@lvl"))
				.queryParam("sort", "created")
				.queryParam("order", "asc")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Posts.class);
		assertThat("Hide author result is not null", posts, notNullValue());
		assertThat("Hide author list coincides with expected", posts.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(min(100, posts.getTotalCount())), hasSize(greaterThanOrEqualTo(3)))); // there are (at least) 3 incidences in the test dataset
		printMsg(" >> Search posts result: " + toJson(posts, JSON_PRETTY_PRINTER));

		response = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("per_page", 100)
				.queryParam("q", String.format("author:\"!%s\"", "user2@lvl"))
				.queryParam("sort", "created")
				.queryParam("order", "asc")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get();
		assertThat("Hide author (JSON encoded) response is not null", response, notNullValue());
		assertThat("Hide author (JSON encoded) response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Hide author (JSON encoded) response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Hide author (JSON encoded) response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));		
		posts = testCtxt.jsonMapper().readValue(payload, Posts.class);
		assertThat("Hide author (JSON encoded) result is not null", posts, notNullValue());
		assertThat("Hide author (JSON encoded) items coincide with expected", posts.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(min(100, posts.getTotalCount())), hasSize(greaterThanOrEqualTo(3)))); // there are (at least) 3 incidences in the test dataset
		printMsg(" >> Search posts response body (JSON): " + payload);

		// hide level and author and sort by date
		posts = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("per_page", 100)
				.queryParam("q", String.format("level:\"!%s\" author:\"!%s\"", PostLevel.NORMAL.name(), "user1@lvl"))
				.queryParam("sort", "created")
				.queryParam("order", "asc")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Posts.class);
		assertThat("Hide level and author result is not null", posts, notNullValue());
		assertThat("Hide level and author list coincides with expected", posts.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(min(100, posts.getTotalCount())), hasSize(greaterThanOrEqualTo(2)))); // there are (at least) 2 promoted posts of authored by this person
		printMsg(" >> Search posts result: " + toJson(posts, JSON_PRETTY_PRINTER));

		response = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("per_page", 100)
				.queryParam("q", String.format("level:\"!%s\" author:\"!%s\"", PostLevel.NORMAL.name(), "user1@lvl"))
				.queryParam("sort", "created")
				.queryParam("order", "asc")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get();
		assertThat("Hide level and author (JSON encoded) response is not null", response, notNullValue());
		assertThat("Hide level and author (JSON encoded) response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Hide level and author (JSON encoded) response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Hide level and author (JSON encoded) response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));		
		posts = testCtxt.jsonMapper().readValue(payload, Posts.class);
		assertThat("Hide level and author (JSON encoded) result is not null", posts, notNullValue());
		assertThat("Hide level and author (JSON encoded) items coincide with expected", posts.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(min(100, posts.getTotalCount())), hasSize(greaterThanOrEqualTo(2)))); // there are (at least) 2 promoted posts of authored by this person
		printMsg(" >> Search posts response body (JSON): " + payload);

		// get count after date
		response = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.path(Long.toString(created7.getTime())).path("after")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get();
		assertThat("Count after date (JSON encoded) response is not null", response, notNullValue());
		assertThat("Count after date (JSON encoded) response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Count after date (JSON encoded) response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Count after date (JSON encoded) response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));
		TotalCount count = testCtxt.jsonMapper().readValue(payload, TotalCount.class);		
		assertThat("Count after date (JSON encoded) result is not null", count, notNullValue());
		assertThat("Count after date (JSON encoded) coincides with expected", count.getTotalCount(), equalTo(4l));
		printMsg(" >> Count after date response body (JSON): " + payload);

		// get count before date
		response = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.path(Long.toString(created7.getTime())).path("before")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get();
		assertThat("Count before date (JSON encoded) response is not null", response, notNullValue());
		assertThat("Count before date (JSON encoded) response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Count before date (JSON encoded) response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Count before date (JSON encoded) response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));
		count = testCtxt.jsonMapper().readValue(payload, TotalCount.class);		
		assertThat("Count before date (JSON encoded) result is not null", count, notNullValue());
		assertThat("Count before date (JSON encoded) coincides with expected", count.getTotalCount(), equalTo(7l));
		printMsg(" >> Count before date response body (JSON): " + payload);
	}

}