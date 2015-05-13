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

package eu.eubrazilcc.lvl.core.workflow;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_DEFAULT_NS;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static org.apache.commons.lang.StringUtils.trimToEmpty;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.eubrazilcc.lvl.core.Linkable;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;

/**
 * Executed workflow.
 * @author Erik Torres <ertorser@upv.es>
 */
public class WorkflowRun implements Linkable<WorkflowRun> {

	@InjectLinks({
		@InjectLink(value="pipeline_runs/{urlSafeNamespace}/{urlSafeId}", rel=SELF, type=APPLICATION_JSON, bindings={
				@Binding(name="urlSafeNamespace", value="${instance.urlSafeNamespace}"),
				@Binding(name="urlSafeId", value="${instance.urlSafeId}")
		})
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links; // HATEOAS links

	@JsonIgnore
	private String urlSafeNamespace;
	@JsonIgnore
	private String urlSafeId;

	private String namespace;

	private String id;
	private String workflowId;
	private int version;
	private String invocationId;
	private WorkflowParameters parameters;
	private String submitter;
	private Date submitted;
	private WorkflowStatus status;
	private List<WorkflowProduct> products;

	public WorkflowRun() {
		super();
		setNamespace(LVL_DEFAULT_NS);
	}

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

	public String getUrlSafeNamespace() {
		return urlSafeNamespace;
	}

	public void setUrlSafeNamespace(final String urlSafeNamespace) {
		this.urlSafeNamespace = urlSafeNamespace;
	}	

	public String getUrlSafeId() {
		return urlSafeId;
	}

	public void setUrlSafeId(final String urlSafeId) {
		this.urlSafeId = urlSafeId;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(final String namespace) {
		this.namespace = namespace;
		setUrlSafeNamespace(urlEncodeUtf8(defaultIfBlank(namespace, LVL_DEFAULT_NS).trim()));
	}

	public String getId() {
		return id;
	}
	public void setId(final String id) {
		this.id = id;
		setUrlSafeId(urlEncodeUtf8(trimToEmpty(id)));		
	}
	public String getWorkflowId() {
		return workflowId;
	}
	public void setWorkflowId(final String workflowId) {
		this.workflowId = workflowId;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(final int version) {
		this.version = version;
	}
	public String getInvocationId() {
		return invocationId;
	}
	public void setInvocationId(final String invocationId) {
		this.invocationId = invocationId;
	}
	public WorkflowParameters getParameters() {
		return parameters;
	}
	public void setParameters(final WorkflowParameters parameters) {
		this.parameters = parameters;
	}
	public String getSubmitter() {
		return submitter;
	}
	public void setSubmitter(final String submitter) {
		this.submitter = submitter;
	}
	public Date getSubmitted() {
		return submitted;
	}
	public void setSubmitted(final Date submitted) {
		this.submitted = submitted;
	}
	public WorkflowStatus getStatus() {
		return status;
	}
	public void setStatus(final WorkflowStatus status) {
		this.status = status;
	}
	public List<WorkflowProduct> getProducts() {
		return products;
	}
	public void setProducts(final List<WorkflowProduct> products) {
		this.products = products;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof WorkflowRun)) {
			return false;
		}
		final WorkflowRun other = WorkflowRun.class.cast(obj);
		return Objects.equals(urlSafeNamespace, other.urlSafeNamespace)
				&& Objects.equals(urlSafeId, other.urlSafeId)
				&& Objects.equals(links, other.links)
				&& equalsIgnoringVolatile(other);
	}

	@Override
	public boolean equalsIgnoringVolatile(final WorkflowRun other) {
		return Objects.equals(id, other.id)
				&& Objects.equals(workflowId, other.workflowId)
				&& Objects.equals(version, other.version)
				&& Objects.equals(invocationId, other.invocationId)
				&& Objects.equals(parameters, other.parameters)
				&& Objects.equals(submitter, other.submitter)
				&& Objects.equals(submitted, other.submitted)
				&& Objects.equals(status, other.status)
				&& Objects.equals(products, other.products);
	}

	@Override
	public int hashCode() {
		return Objects.hash(links, urlSafeNamespace, urlSafeId, id, workflowId, version, invocationId, parameters, submitted, status, products);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("links", links)
				.add("urlSafeNamespace", urlSafeNamespace)
				.add("urlSafeId", urlSafeId)
				.add("id", id)
				.add("workflowId", workflowId)
				.add("version", version)
				.add("invocationId", invocationId)
				.add("parameters", parameters)
				.add("submitter", submitter)
				.add("submitted", submitted)
				.add("status", status)
				.add("products", products)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder {

		private final WorkflowRun instance = new WorkflowRun();

		public Builder namespace(final String namespace) {
			instance.setNamespace(trimToEmpty(namespace));
			return this;
		}

		public Builder id(final String id) {
			instance.setId(id);
			return this;
		}

		public Builder workflowId(final String workflowId) {
			instance.setWorkflowId(workflowId);
			return this;
		}

		public Builder version(final int version) {
			instance.setVersion(version);
			return this;
		}

		public Builder invocationId(final String invocationId) {
			instance.setInvocationId(invocationId);
			return this;
		}

		public Builder parameters(final WorkflowParameters parameters) {
			instance.setParameters(parameters);
			return this;
		}

		public Builder submitter(final String submitter) {
			instance.setSubmitter(submitter);			
			return this;
		}

		public Builder submitted(final Date submitted) {
			instance.setSubmitted(submitted);			
			return this;
		}

		public Builder status(final WorkflowStatus status) {
			instance.setStatus(status);		
			return this;
		}

		public Builder products(final List<WorkflowProduct> products) {
			instance.setProducts(products);
			return this;
		}

		public WorkflowRun build() {
			return instance;
		}

	}

}