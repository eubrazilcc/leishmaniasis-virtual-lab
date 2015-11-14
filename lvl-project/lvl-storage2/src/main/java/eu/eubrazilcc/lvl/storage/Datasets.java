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

import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.FIRST;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.LAST;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.NEXT;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.PREVIOUS;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.eubrazilcc.lvl.storage.base.LvlFiles;
import eu.eubrazilcc.lvl.storage.ws.rs.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.storage.ws.rs.jackson.LinkListSerializer;

/**
 * Wraps a collection of {@link Dataset}.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Datasets extends LvlFiles<Dataset> {

	@InjectLinks({
		@InjectLink(value="/datasets/objects", method="getDatasets", bindings={
				@Binding(name="page", value="${instance.page - 1}"),
				@Binding(name="per_page", value="${instance.perPage}")
		}, rel=PREVIOUS, type=APPLICATION_JSON, condition="${instance.page > 0}"),
		@InjectLink(value="/datasets/objects", method="getDatasets", bindings={
				@Binding(name="page", value="${0}"),
				@Binding(name="per_page", value="${instance.perPage}")
		}, rel=FIRST, type=APPLICATION_JSON, condition="${instance.page > 0}"),
		@InjectLink(value="/datasets/objects", method="getDatasets", bindings={
				@Binding(name="page", value="${instance.page + 1}"),
				@Binding(name="per_page", value="${instance.perPage}")
		}, rel=NEXT, type=APPLICATION_JSON, condition="${instance.pageFirstEntry + instance.perPage < instance.totalCount}"),
		@InjectLink(value="/datasets/objects", method="getDatasets", bindings={
				@Binding(name="page", value="${instance.totalPages - 1}"),
				@Binding(name="per_page", value="${instance.perPage}")
		}, rel=LAST, type=APPLICATION_JSON, condition="${instance.pageFirstEntry + instance.perPage < instance.totalCount}")
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links; // HATEOAS links

	public Datasets() {
		this(null);
	}

	public Datasets(final String namespace) {
		super(namespace, Dataset.class, Dataset.CONFIGURER, getLogger(Dataset.class));
	}

	@Override
	public List<Link> getLinks() {
		return links;
	}

	@Override
	public void setLinks(final List<Link> links) {
		this.links = (links != null ? newArrayList(links) : null);		
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private String namespace = null;

		public Builder namespace(final String namespace) {
			this.namespace = namespace;
			return this;
		}

		public Datasets build() {
			return new Datasets(namespace);
		}

	}

}