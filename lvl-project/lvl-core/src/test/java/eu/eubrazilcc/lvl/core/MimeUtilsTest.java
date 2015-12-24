package eu.eubrazilcc.lvl.core;

import static eu.eubrazilcc.lvl.core.util.MimeUtils.isTextFile;
import static eu.eubrazilcc.lvl.core.util.MimeUtils.mimeType;
import static eu.eubrazilcc.lvl.core.util.TestUtils.getTextFiles;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;

import org.junit.Test;

import eu.eubrazilcc.lvl.core.util.MimeUtils;
import eu.eubrazilcc.lvl.test.LeishvlTestCase;

/**
 * Test utilities to discover MIME types with class {@link MimeUtils}.
 * @author Erik Torres <ertorser@upv.es>
 */
public class MimeUtilsTest extends LeishvlTestCase {

	public MimeUtilsTest() {
		super(false);
	}

	@Test
	public void test() {
		printMsg("MimeUtilsTest.test()");
		try {
			// test discover MIME type from known text files
			final Collection<File> files = getTextFiles();
			for (final File file : files) {
				printMsg(" >> File: " + file.getCanonicalPath());
				final String mime = mimeType(file);
				assertThat("MIME type is not null", mime, notNullValue());
				assertThat("MIME type is not empty", isNotBlank(mime), equalTo(true));
				final boolean isText = isTextFile(file);
				assertThat("MIME type coincides with expected", isText, equalTo(true));
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("MimeUtilsTest.test() failed: " + e.getMessage());
		} finally {			
			printMsg("MimeUtilsTest.test() has finished");
		}
	}

}