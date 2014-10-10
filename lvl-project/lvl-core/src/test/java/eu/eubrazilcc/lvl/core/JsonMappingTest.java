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

package eu.eubrazilcc.lvl.core;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static eu.eubrazilcc.lvl.core.DataSource.GENBANK;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.eubrazilcc.lvl.core.PublicLink.Target;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;

/**
 * Tests JSON mapping capabilities.
 * @author Erik Torres <ertorser@upv.es>
 */
public class JsonMappingTest {

	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

	@Test
	public void test() {
		System.out.println("JsonMappingTest.test()");
		try {
			// create test dataset
			final Link seqLink = Link.fromUri(UriBuilder.fromUri("http://localhost/sequence").path("gb:ABC12345678").build())
					.rel(SELF).type(APPLICATION_JSON).build();
			final Link refLink = Link.fromUri(UriBuilder.fromUri("http://localhost/paper").path("ADGJ87950").build())
					.rel(SELF).type(APPLICATION_JSON).build();
			final Link publicLinkLink = Link.fromUri(UriBuilder.fromUri("http://localhost/public_link").path("fjihknqswre1dvqp/353470160.fasta.gz").build())
					.rel(SELF).type(APPLICATION_JSON).build();

			final Point point = Point.builder().coordinates(LngLatAlt.builder().coordinates(-122.913837d, 38.081473d).build()).build();

			final Sequence sequence = Sequence.builder()
					.dataSource(GENBANK)
					.accession("ABC12345678")
					.version("3.0")
					.gi(1239841)
					.definition("definition")
					.organism("organism")
					.countryFeature("Spain: Murcia")
					.location(point)
					.locale(new Locale("es", "ES"))
					.pmids(newHashSet("1234R", "AV99O0"))
					.build();
			assertThat("sequence is not null", sequence, notNullValue());

			final Reference reference = Reference.builder()
					.pubmedId("ADGJ87950")
					.title("The best paper in the world!")
					.publicationYear(1984)
					.seqids(newHashSet("gb:ABC12345678"))
					.build();
			assertThat("reference is not null", reference, notNullValue());

			final PublicLink publicLink = PublicLink.builder()
					.created(new Date())
					.target(Target.builder().type("sequence").ids(newArrayList("gb:JP540074", "gb:JP553239")).filter("export_fasta").compression("gzip").build())
					.description("Optional description")
					.build();
			assertThat("public link is not null", publicLink, notNullValue());			

			// test sequence with no links
			testSequence(sequence);

			// test sequence with links
			sequence.setLinks(newArrayList(seqLink));
			testSequence(sequence);

			// test references with no links
			testReference(reference);

			// test references with links
			reference.setLinks(newArrayList(refLink));
			testReference(reference);

			// test public links with no links
			testPublicLink(publicLink);

			// test public links with links
			publicLink.setLinks(newArrayList(publicLinkLink));			
			testPublicLink(publicLink);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("JsonMappingTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("JsonMappingTest.test() has finished");
		}
	}

	private void testSequence(final Sequence sequence) throws IOException {
		// test sequence JSON serialization
		final String payload = JSON_MAPPER.writeValueAsString(sequence);
		assertThat("serialized sequence is not null", payload, notNullValue());
		assertThat("serialized sequence is not empty", isNotBlank(payload), equalTo(true));
		/* uncomment for additional output */
		System.out.println(" >> Serialized sequence (JSON): " + payload);

		// test sequence JSON deserialization
		final Sequence sequence2 = JSON_MAPPER.readValue(payload, Sequence.class);
		assertThat("deserialized sequence is not null", sequence2, notNullValue());
		assertThat("deserialized sequence coincides with expected", sequence2, equalTo(sequence));
	}

	private void testReference(final Reference reference) throws IOException {
		// test reference JSON serialization
		final String payload = JSON_MAPPER.writeValueAsString(reference);
		assertThat("serialized reference is not null", payload, notNullValue());
		assertThat("serialized reference is not empty", isNotBlank(payload), equalTo(true));
		/* uncomment for additional output */
		System.out.println(" >> Serialized reference (JSON): " + payload);

		// test reference JSON deserialization
		final Reference reference2 = JSON_MAPPER.readValue(payload, Reference.class);
		assertThat("deserialized reference is not null", reference2, notNullValue());
		assertThat("deserialized reference coincides with expected", reference2, equalTo(reference));
	}

	private void testPublicLink(final PublicLink publicLink) throws IOException {
		// test public link JSON serialization
		final String payload = JSON_MAPPER.writeValueAsString(publicLink);
		assertThat("serialized public link is not null", payload, notNullValue());
		assertThat("serialized public link is not empty", isNotBlank(payload), equalTo(true));
		/* uncomment for additional output */
		System.out.println(" >> Serialized public link (JSON): " + payload);

		// test public link JSON deserialization
		final PublicLink publicLink2 = JSON_MAPPER.readValue(payload, PublicLink.class);
		assertThat("deserialized public link is not null", publicLink2, notNullValue());
		assertThat("deserialized public link coincides with expected", publicLink2, equalTo(publicLink));		
	}

}