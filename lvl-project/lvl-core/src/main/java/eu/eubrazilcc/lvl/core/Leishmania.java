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
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;

/**
 * Stores Leishmania nucleotide sequences that includes specific information related to this family. In addition, Jersey annotations
 * are included to inject links from a RESTful resource.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Leishmania extends Sequence implements Linkable<Leishmania> {

	@InjectLinks({
		@InjectLink(value="leishmania/{id}", rel=SELF, type=APPLICATION_JSON, bindings={@Binding(name="id", value="${instance.id}")})
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links;       // HATEOAS links

	private List<String> codes;     // list of references in other databases (e.g. CLIOC)
	private String intCode;         // international code
	private String host;            // host
	private String clinicalForm;    // clinical form
	private boolean hivCoinfection; // HIV coinfection

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

	public List<String> getCodes() {
		return codes;
	}

	public void setCodes(final List<String> codes) {
		this.codes = codes;
	}

	public String getIntCode() {
		return intCode;
	}

	public void setIntCode(final String intCode) {
		this.intCode = intCode;
	}

	public String getHost() {
		return host;
	}

	public void setHost(final String host) {
		this.host = host;
	}

	public String getClinicalForm() {
		return clinicalForm;
	}

	public void setClinicalForm(final String clinicalForm) {
		this.clinicalForm = clinicalForm;
	}

	public boolean isHivCoinfection() {
		return hivCoinfection;
	}

	public void setHivCoinfection(final boolean hivCoinfection) {
		this.hivCoinfection = hivCoinfection;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Leishmania)) {
			return false;
		}
		final Leishmania other = Leishmania.class.cast(obj);
		return Objects.equals(links, other.links)
				&& equalsIgnoringVolatile(other);
	}

	@Override
	public boolean equalsIgnoringVolatile(final Leishmania other) {
		if (other == null) {
			return false;
		}
		return super.equals((Sequence)other)
				&& Objects.equals(codes, other.codes)
				&& Objects.equals(intCode, other.intCode)
				&& Objects.equals(host, other.host)
				&& Objects.equals(clinicalForm, other.clinicalForm)
				&& Objects.equals(hivCoinfection, other.hivCoinfection);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(links, codes, host, clinicalForm, hivCoinfection);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("Sequence", super.toString())
				.add("links", links)
				.add("codes", codes)
				.add("intCode", intCode)
				.add("host", host)
				.add("clinicalForm", clinicalForm)
				.add("hivCoinfection", hivCoinfection)
				.toString();
	}

	/* Fluent API */

	public static LeishmaniaBuilder builder() {
		return new LeishmaniaBuilder();
	}

	public static class LeishmaniaBuilder extends Builder<Leishmania> {

		public LeishmaniaBuilder() {
			super(Leishmania.class);
		}

		public LeishmaniaBuilder links(final List<Link> links) {
			instance.setLinks(links);
			return this;
		}

		public LeishmaniaBuilder codes(final List<String> codes) {
			instance.setCodes(codes);
			return this;
		}

		public LeishmaniaBuilder intCode(final String intCode) {
			instance.setIntCode(intCode);
			return this;
		}

		public LeishmaniaBuilder host(final String host) {
			instance.setHost(host);
			return this;
		}

		public LeishmaniaBuilder clinicalForm(final String clinicalForm) {
			instance.setClinicalForm(clinicalForm);
			return this;
		}

		public LeishmaniaBuilder hivCoinfection(final boolean hivCoinfection) {
			instance.setHivCoinfection(hivCoinfection);			
			return this;
		}		

	}

}