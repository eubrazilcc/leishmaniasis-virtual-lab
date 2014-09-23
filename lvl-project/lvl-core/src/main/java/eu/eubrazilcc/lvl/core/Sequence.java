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

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.DataSource.Notation.NOTATION_SHORT;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.toId;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;

/**
 * Stores a nucleotide sequence as a subset of GenBank fields (since most sequences comes from GenBank) 
 * annotated with additional fields. A sequence is uniquely identified by the combination of the data
 * source and the accession (i.e. GenBank, U49845). GenBank sequences can be annotated with a country 
 * feature, however this annotation is optional and is often absent. When present, country is represented 
 * by a short name instead of a computer-friendly code. This class solves this limitation including
 * a Java {@link Locale} that can be used later to export the country as a two-letter code that 
 * represents a country name with ISO 3166-1 alpha-2 standard. The original GenBank country feature is 
 * also included in the class. What is more important, a GeoJSON point is included that allows callers 
 * to georeference the sequence. Jackson annotations are included to serialize this class to JSON. These
 * annotations are preferred to JAXB annotations (which are supported in most JSON processing libraries
 * and besides JSON, they also support XML serialization) due to compatibility issues with the current
 * version of JAX-RS that produces errors when unmarshaling {@link Link}. In addition, Jersey annotations
 * are included to inject links from a RESTful resource.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://opengeocode.org/download.php">Americas Open Geocode (AOG) database</a>
 * @see <a href="http://geojson.org/">GeoJSON open standard format for encoding geographic data structures</a>
 * @see <a href="http://www.ncbi.nlm.nih.gov/genbank/">GenBank collection of publicly available DNA sequences</a>
 */
public class Sequence implements Linkable<Sequence> {

	@InjectLinks({
		@InjectLink(value="sequences/{id}", rel=SELF, type=APPLICATION_JSON, bindings={@Binding(name="id", value="${instance.id}")})
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links;      // HATEOAS links

	private String id;             // Resource identifier

	private String dataSource;     // Database where the original sequence is stored
	private String definition;     // GenBank definition field
	private String accession;      // GenBank accession number	
	private String version;        // GenBank version
	private int gi;                // GenBank GenInfo Identifier (Entrez default search field)
	private String organism;       // GenBank organism
	private String countryFeature; // GenBank country feature
	private Point location;        // Geospatial location
	private Locale locale;         // Represents country with standards

	public Sequence() { }

	@Override
	public List<Link> getLinks() {
		return links;
	}

	@Override
	public void setLinks(final List<Link> links) {
		if (links != null) {
			this.links = newArrayList(links);
		} else {
			this.links = null;
		}
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(final String dataSource) {
		this.dataSource = dataSource;
		updateId();
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(final String definition) {
		this.definition = definition;
	}

	public String getAccession() {
		return accession;
	}

	public void setAccession(final String accession) {
		this.accession = accession;
		updateId();
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	public int getGi() {
		return gi;
	}

	public void setGi(final int gi) {
		this.gi = gi;
	}

	public String getOrganism() {
		return organism;
	}

	public void setOrganism(final String organism) {
		this.organism = organism;
	}

	public String getCountryFeature() {
		return countryFeature;
	}

	public void setCountryFeature(final String countryFeature) {
		this.countryFeature = countryFeature;
	}

	public Point getLocation() {
		return location;
	}

	public void setLocation(final Point location) {
		this.location = location;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(final Locale locale) {
		this.locale = locale;
	}	

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Sequence)) {
			return false;
		}
		final Sequence other = Sequence.class.cast(obj);
		return Objects.equals(links, other.links)
				&& equalsIgnoringVolatile(other);
	}

	@Override
	public boolean equalsIgnoringVolatile(final Sequence other) {
		if (other == null) {
			return false;
		}
		return Objects.equals(id, other.id)
				&& Objects.equals(dataSource, other.dataSource)
				&& Objects.equals(definition, other.definition)
				&& Objects.equals(accession, other.accession)
				&& Objects.equals(version, other.version)
				&& Objects.equals(gi, other.gi)
				&& Objects.equals(organism, other.organism)
				&& Objects.equals(countryFeature, other.countryFeature)
				&& Objects.equals(location, other.location)
				&& Objects.equals(locale, other.locale);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, links, dataSource, definition, accession, version, gi, organism, 
				countryFeature, location, locale);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("id", id)
				.add("links", links)
				.add("dataSource", dataSource)
				.add("definition", definition)
				.add("accession", accession)
				.add("version", version)
				.add("gi", gi)
				.add("organism", organism)
				.add("countryFeature", countryFeature)
				.add("location", location)
				.add("locale", locale)
				.toString();
	}

	private void updateId() {
		id = dataSource != null && isNotBlank(accession) ? toId(dataSource, accession, NOTATION_SHORT) : null;
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder {

		private final Sequence instance = new Sequence();

		public Builder links(final List<Link> links) {
			instance.setLinks(links);
			return this;
		}

		public Builder dataSource(final String dataSource) {
			instance.setDataSource(dataSource);
			return this;
		}

		public Builder definition(final String definition) {
			instance.setDefinition(definition);
			return this;
		}

		public Builder accession(final String accession) {
			instance.setAccession(accession);
			return this;
		}	

		public Builder version(final String version) {
			instance.setVersion(version);
			return this;
		}

		public Builder gi(final int gi) {
			instance.setGi(gi);
			return this;
		}

		public Builder organism(final String organism) {
			instance.setOrganism(organism);
			return this;
		}

		public Builder countryFeature(final String countryFeature) {
			instance.setCountryFeature(countryFeature);
			return this;
		}

		public Builder location(final Point location) {
			instance.setLocation(location);
			return this;
		}

		public Builder locale(final Locale locale) {
			instance.setLocale(locale);
			return this;
		}

		public Sequence build() {
			return instance;
		}

	}

}