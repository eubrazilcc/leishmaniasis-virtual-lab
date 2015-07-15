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

package eu.eubrazilcc.lvl.core;

import static eu.eubrazilcc.lvl.core.util.UrlUtils.getPath;
import static eu.eubrazilcc.lvl.core.util.UrlUtils.getQueryParams;
import static eu.eubrazilcc.lvl.core.util.UrlUtils.isFileProtocol;
import static eu.eubrazilcc.lvl.core.util.UrlUtils.isValid;
import static eu.eubrazilcc.lvl.core.util.UrlUtils.parseURL;
import static javax.ws.rs.core.UriBuilder.fromPath;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.toFile;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.io.FilenameUtils.normalizeNoEndSeparator;
import static org.apache.commons.lang.RandomStringUtils.random;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests URL utilities.
 * @author Erik Torres <ertorser@upv.es>
 */
public class UrlUtilsTest {

	private static final String TEST_OUTPUT_DIR = concat(System.getProperty("java.io.tmpdir"),
			UrlUtilsTest.class.getSimpleName() + "_" + random(8, true, true));

	@Before
	public void setUp() {
		deleteQuietly(new File(TEST_OUTPUT_DIR));
	}

	@Test
	public void test() {
		System.out.println("URLUtilsTest.test()");
		try {			
			// parse URL
			final String[][] paths = { 
					{ "file:///foo", "/foo" }, 
					{ "/foo//", "/foo/" }, 
					{ "/foo/./", "/foo/" },
					{ "/foo/../bar", "/bar" }, 
					{ "/foo/../bar/", "/bar/" },
					{ "/foo/../bar/../baz", "/baz" },
					{ "//foo//./bar", "/foo/bar" },
					{ "/../", null },
					{ "../foo", concat(System.getProperty("user.dir"), "../foo") },
					{ "foo/bar/..", concat(System.getProperty("user.dir"), "foo/bar/..") },
					{ "foo/../../bar", concat(System.getProperty("user.dir"), "foo/../../bar") },
					{ "foo/../bar", concat(System.getProperty("user.dir"), "foo/../bar") },
					{ "//server/foo/../bar", "/server/bar" },
					{ "//server/../bar", null }, 
					{ "C:\\foo\\..\\bar", "/bar" }, 
					{ "C:\\..\\bar", null }, 
					{ "~/foo/../bar/", concat(System.getProperty("user.home"), "foo/../bar/") }, 
					{ "~/../bar", concat(System.getProperty("user.home"), "../bar") },
					{ "http://localhost", null }
			};
			for (int i = 0; i < paths.length; i++) {
				URL url = null;
				try {
					url = parseURL(paths[i][0]);					
					System.out.println("PATH='" + paths[i][0] 
							+ "', URL='" + (url != null ? url.toString() : "NULL") 
							+ "', EXPECTED='" + paths[i][1] + "'");
					assertThat("URL is not null", url, notNullValue());
					if (isFileProtocol(url)) {
						final File file = toFile(url);
						assertThat("file is not null", file, notNullValue());
						assertThat("file name coincides", normalizeNoEndSeparator(file.getCanonicalPath(), true)
								.equals(normalizeNoEndSeparator(paths[i][1], true)));
					}
				} catch (IOException ioe) {
					if (paths[i][1] != null) {
						throw ioe;
					} else {
						System.out.println("PATH='" + paths[i][0] + "' thrown the expected exception");
					}
				}
			}

			// validate URL
			assertThat("valid URL", isValid("http://www.google.com", true));
			assertThat("valid URL", !isValid("http://invalidURL^$&%$&^", true));
			assertThat("valid URL", isValid("http://example.com/file[/].html", false));
			assertThat("valid URL", !isValid("http://example.com/file[/].html", true));

			// get path (absolute path)
			UriBuilder uriBuilder = fromPath("http://example.org/service/version/path")
					.queryParam("page", "{page}")
					.queryParam("per_page", "{per_page}")
					.queryParam("q", "{q}")
					.queryParam("sort", "{sort}")
					.queryParam("order", "{order}");
			URI uri = uriBuilder.clone().build(0, 12, "papatasi", "sort_field", "desc");
			String path = getPath(uri);
			assertThat("absolute path is not null", path, notNullValue());
			assertThat("absolute path is not empty", isNotBlank(path), equalTo(true));
			assertThat("absolute path coincides with expected", path, equalTo("/service/version/path"));

			// get path parameters (relative path)
			uriBuilder = fromPath("/service/version/path")
					.queryParam("page", "{page}")
					.queryParam("per_page", "{per_page}")
					.queryParam("q", "{q}")
					.queryParam("sort", "{sort}")
					.queryParam("order", "{order}");
			uri = uriBuilder.clone().build(0, 12, "papatasi", "sort_field", "desc");
			path = getPath(uri);
			assertThat("relative path is not null", path, notNullValue());
			assertThat("relative path is not empty", isNotBlank(path), equalTo(true));
			assertThat("relative path coincides with expected", path, equalTo("/service/version/path"));			

			// get query parameters (decoded URL)
			uriBuilder = fromPath("/service/version/path")
					.queryParam("page", "{page}")
					.queryParam("per_page", "{per_page}")
					.queryParam("q", "{q}")
					.queryParam("sort", "{sort}")
					.queryParam("order", "{order}");
			uri = uriBuilder.clone().build(0, 12, "papatasi", "", "desc");
			Map<String, String> params = getQueryParams(uri);
			assertThat("query parameters is not null", params, notNullValue());
			assertThat("query parameters is not empty", params.isEmpty(), equalTo(false));
			assertThat("number of query parameters coincides with expected", params.size(), equalTo(5));
			assertThat("query parameters coincide with expected", params.get("page"), equalTo("0"));
			assertThat("query parameters coincide with expected", params.get("per_page"), equalTo("12"));
			assertThat("query parameters coincide with expected", params.get("q"), equalTo("papatasi"));
			assertThat("query parameters coincide with expected", params.get("sort"), equalTo(""));
			assertThat("query parameters coincide with expected", params.get("order"), equalTo("desc"));

			// get query parameters (encoded URL)
			uri = new URI("/lvl-service/rest/v1/sequences%3Fpage=3&per_page=2&sort=&order=asc&q=");
			params = getQueryParams(uri);
			assertThat("query parameters is not null", params, notNullValue());
			assertThat("query parameters is not empty", params.isEmpty(), equalTo(false));						
			assertThat("number of query parameters coincides with expected", params.size(), equalTo(5));
			assertThat("query parameters coincide with expected", params.get("page"), equalTo("3"));
			assertThat("query parameters coincide with expected", params.get("per_page"), equalTo("2"));
			assertThat("query parameters coincide with expected", params.get("q"), equalTo(""));
			assertThat("query parameters coincide with expected", params.get("sort"), equalTo(""));
			assertThat("query parameters coincide with expected", params.get("order"), equalTo("asc"));			

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("URLUtilsTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("URLUtilsTest.test() has finished");
		}		
	}

	@After
	public void cleanUp() {
		deleteQuietly(new File(TEST_OUTPUT_DIR));
	}

}