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

import java.util.Arrays;

import javax.ws.rs.core.Link;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Objects;

import eu.eubrazilcc.lvl.core.geospatial.Point;
import eu.eubrazilcc.lvl.core.xml.LinkAdapter;

/**
 * Stores a GenBank sequence. GenBank sequences can be annotated with a country feature, however
 * this annotation is optional and it's often absent. When present, country is represented by a
 * short name instead of a computer-friendly code. This class solves this limitation including
 * a two-letter code that represents a country name with ISO 3166-1 alpha-2 standard. The original
 * GenBank country feature is also included in the class. What is more important, a GeoJSON point
 * is included that allows callers to georeference the sequence.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://opengeocode.org/download.php">Americas Open Geocode (AOG) database</a>
 */
public class Sequence {

	private Link link;
	private String definition;
	private String accession;
	private String version;
	private String organism;		
	private Point location;
	private char[] country;
	private String locationDescription;

	public Sequence() { }

	@XmlJavaTypeAdapter(LinkAdapter.class)
	public Link getLink() {
		return link;
	}

	public void setLink(final Link link) {
		this.link = link;
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

	public String getOrganism() {
		return organism;
	}

	public void setOrganism(final String organism) {
		this.organism = organism;
	}

	public Point getLocation() {
		return location;
	}

	public void setLocation(final Point location) {
		this.location = location;
	}

	public char[] getCountry() {
		return country;
	}

	public void setCountry(final char[] country) {
		this.country = country;
	}

	public String getLocationDescription() {
		return locationDescription;
	}

	public void setLocationDescription(final String locationDescription) {
		this.locationDescription = locationDescription;
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
		return Objects.equal(definition, other.definition)
				&& Objects.equal(accession, other.accession)
				&& Objects.equal(version, other.version)
				&& Objects.equal(organism, other.organism)
				&& Objects.equal(location, other.location)
				&& Arrays.equals(country, other.country)
				&& Objects.equal(locationDescription, other.locationDescription);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(link, definition, accession, version, organism, location,
				country, locationDescription);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("link", link)
				.add("definition", definition)
				.add("accession", accession)
				.add("version", version)
				.add("organism", organism)
				.add("location", location)
				.add("country", country)
				.add("locationDescription", locationDescription)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final Sequence sequence = new Sequence();

		public Builder link(final Link link) {
			sequence.setLink(link);
			return this;
		}

		public Builder definition(final String definition) {
			sequence.setDefinition(definition);
			return this;
		}

		public Builder accession(final String accession) {
			sequence.setAccession(accession);
			return this;
		}	

		public Builder version(final String version) {
			sequence.setVersion(version);
			return this;
		}

		public Builder organism(final String organism) {
			sequence.setOrganism(organism);
			return this;
		}

		public Builder location(final Point location) {
			sequence.setLocation(location);
			return this;
		}

		public Builder country(final char[] country) {
			sequence.setCountry(country);
			return this;
		}

		public Builder locationDescription(final String locationDescription) {
			sequence.setLocationDescription(locationDescription);
			return this;
		}

		public Sequence build() {
			return sequence;
		}

	}

}