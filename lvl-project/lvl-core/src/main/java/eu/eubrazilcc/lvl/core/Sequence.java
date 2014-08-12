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

import java.util.Locale;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.JaxbAdapter;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Objects;

import eu.eubrazilcc.lvl.core.geojson.Point;

/**
 * Stores a nucleotide sequence as a subset of GenBank fields (since most sequences comes from GenBank) 
 * annotated with additional fields. A sequence is uniquely identified by the combination of the data
 * source and the accession (i.e. GenBank, U49845). GenBank sequences can be annotated with a country 
 * feature, however this annotation is optional and is often absent. When present, country is represented 
 * by a short name instead of a computer-friendly code. This class solves this limitation including
 * a Java {@link Locale} that can be used later to export the country as a two-letter code that 
 * represents a country name with ISO 3166-1 alpha-2 standard. The original GenBank country feature is 
 * also included in the class. What is more important, a GeoJSON point is included that allows callers 
 * to georeference the sequence. Include JAXB annotations to serialize this class to XML and JSON.
 * Most JSON processing libraries like Jackson support these JAXB annotations.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://opengeocode.org/download.php">Americas Open Geocode (AOG) database</a>
 */
@XmlRootElement
public class Sequence {

	private Link link;             // RESTful link
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

	@XmlElement(name="link")
	@XmlJavaTypeAdapter(JaxbAdapter.class)
	public Link getLink() {
		return link;
	}

	public void setLink(final Link link) {
		this.link = link;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(final String dataSource) {
		this.dataSource = dataSource;
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
		return Objects.equal(link, other.link)
				&& equalsIgnoreLink(other);
	}

	public boolean equalsIgnoreLink(final Sequence other) {
		if (other == null) {
			return false;
		}
		return Objects.equal(dataSource, other.dataSource)
				&& Objects.equal(definition, other.definition)
				&& Objects.equal(accession, other.accession)
				&& Objects.equal(version, other.version)
				&& Objects.equal(gi, other.gi)
				&& Objects.equal(organism, other.organism)
				&& Objects.equal(countryFeature, other.countryFeature)
				&& Objects.equal(location, other.location)
				&& Objects.equal(locale, other.locale);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(link, dataSource, definition, accession, version, gi, organism, 
				countryFeature, location, locale);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("link", link)
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

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final Sequence instance = new Sequence();

		public Builder link(final Link link) {
			instance.setLink(link);
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