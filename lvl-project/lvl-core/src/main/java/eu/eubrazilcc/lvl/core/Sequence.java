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
import static eu.eubrazilcc.lvl.core.DataSource.Notation.NOTATION_SHORT;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.toId;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import javax.ws.rs.core.Link;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSeq;

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
 * version of JAX-RS that produces errors when unmarshaling {@link Link}.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://opengeocode.org/download.php">Americas Open Geocode (AOG) database</a>
 * @see <a href="http://geojson.org/">GeoJSON open standard format for encoding geographic data structures</a>
 * @see <a href="http://www.ncbi.nlm.nih.gov/genbank/">GenBank collection of publicly available DNA sequences</a>
 */
public class Sequence implements Localizable<Point> {

	private String id;             // Resource identifier

	private String dataSource;     // Database where the original sequence is stored
	private String definition;     // GenBank definition field
	private String accession;      // GenBank accession number
	private String version;        // GenBank version
	private int gi;                // GenBank GenInfo Identifier (Entrez default search field)
	private String organism;       // GenBank organism
	private int length;            // Sequence length
	private Set<String> gene;      // List of gene names
	private String countryFeature; // GenBank country feature
	private Point location;        // Geospatial location
	private Locale locale;         // Represents country with standards
	private Set<String> pmids;     // References mentioning this sequence in PubMed

	private GBSeq sequence;        // Original GenBank sequence

	public Sequence() { }	

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

	public int getLength() {
		return length;
	}

	public void setLength(final int length) {
		this.length = length;
	}

	public Set<String> getGene() {
		return gene;
	}

	public void setGene(final Set<String> gene) {
		this.gene = gene;
	}

	public String getCountryFeature() {
		return countryFeature;
	}

	public void setCountryFeature(final String countryFeature) {
		this.countryFeature = countryFeature;
	}

	@Override
	public Point getLocation() {
		return location;
	}

	@Override
	public void setLocation(final Point location) {
		this.location = location;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(final Locale locale) {
		this.locale = locale;
	}

	public Set<String> getPmids() {
		return pmids;
	}

	public void setPmids(final Set<String> pmids) {
		this.pmids = pmids;
	}	

	public GBSeq getSequence() {
		return sequence;
	}

	public void setSequence(final GBSeq sequence) {
		this.sequence = sequence;
	}

	@JsonIgnore
	@Override
	public String getTag() {
		return toId(this, NOTATION_SHORT);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Sequence)) {
			return false;
		}
		final Sequence other = Sequence.class.cast(obj);
		return Objects.equals(id, other.id)
				&& Objects.equals(dataSource, other.dataSource)
				&& Objects.equals(definition, other.definition)
				&& Objects.equals(accession, other.accession)
				&& Objects.equals(version, other.version)
				&& Objects.equals(gi, other.gi)
				&& Objects.equals(organism, other.organism)
				&& Objects.equals(length, other.length)
				&& Objects.equals(gene, other.gene)
				&& Objects.equals(countryFeature, other.countryFeature)
				&& Objects.equals(location, other.location)
				&& Objects.equals(locale, other.locale)
				&& Objects.equals(pmids, other.pmids);
		// sequence
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, dataSource, definition, accession, version, gi, organism, length, gene,
				countryFeature, location, locale, pmids); // sequence
	}	

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("id", id)
				.add("dataSource", dataSource)
				.add("definition", definition)
				.add("accession", accession)
				.add("version", version)
				.add("gi", gi)
				.add("organism", organism)
				.add("length", length)
				.add("gene", gene)
				.add("countryFeature", countryFeature)
				.add("location", location)
				.add("locale", locale)
				.add("pmids", pmids)
				.add("sequence", "<<original sequence is not displayed>>")
				.toString();
	}

	private void updateId() {
		id = dataSource != null && isNotBlank(accession) ? toId(dataSource, accession, NOTATION_SHORT) : null;
	}

	/* Fluent API */

	public static class Builder<T extends Sequence> {

		protected final T instance;

		public Builder(final Class<T> clazz) {
			T tmp = null;
			try {
				tmp = clazz.newInstance();
			} catch (Exception ignore) { }
			instance = tmp;
		}

		public Builder<T> dataSource(final String dataSource) {
			instance.setDataSource(dataSource);
			return this;
		}

		public Builder<T> definition(final String definition) {
			instance.setDefinition(definition);
			return this;
		}

		public Builder<T> accession(final String accession) {
			instance.setAccession(accession);
			return this;
		}	

		public Builder<T> version(final String version) {
			instance.setVersion(version);
			return this;
		}

		public Builder<T> gi(final int gi) {
			instance.setGi(gi);
			return this;
		}

		public Builder<T> organism(final String organism) {
			instance.setOrganism(organism);
			return this;
		}

		public Builder<T> length(final int length) {
			instance.setLength(length);
			return this;
		}

		public Builder<T> gene(final Set<String> gene) {
			instance.setGene(gene);
			return this;
		}

		public Builder<T> countryFeature(final String countryFeature) {
			instance.setCountryFeature(countryFeature);
			return this;
		}

		public Builder<T> location(final Point location) {
			instance.setLocation(location);
			return this;
		}

		public Builder<T> locale(final Locale locale) {
			instance.setLocale(locale);
			return this;
		}

		public Builder<T> pmids(final Set<String> pmids) {
			instance.setPmids(pmids);
			return this;
		}
		
		public Builder<T> sequence(final GBSeq sequence) {
			instance.setSequence(sequence);
			return this;
		}

		public T build() {
			return instance;
		}

	}

}