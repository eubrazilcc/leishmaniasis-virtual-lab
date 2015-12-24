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

package eu.eubrazilcc.lvl.core.support;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newTreeMap;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.compactRandomUUID;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;

import eu.eubrazilcc.lvl.core.Linkable;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;

/**
 * Reported issue.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Issue implements Linkable<Issue> {

	@InjectLinks({
		@InjectLink(value="support/issues/{urlSafeId}", rel=SELF, type=APPLICATION_JSON, 
				bindings={@Binding(name="urlSafeId", value="${instance.urlSafeId}")})
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links; // HATEOAS links

	@JsonIgnore
	private String urlSafeId;

	private String id;
	private String email;
	private Date opened;
	private String browser;
	private String system;
	private Optional<String> configuration = absent();
	private Optional<String> steps = absent();
	private String description;
	private Optional<String> screenshot = absent();
	private IssueStatus status;

	private Optional<String> owner = absent();
	private Optional<Date> closed = absent();
	private Map<Long, String> followUp = newTreeMap();

	@Override
	public List<Link> getLinks() {
		return links;
	}

	@Override
	public void setLinks(final List<Link> links) {
		this.links = links != null ? newArrayList(links) : null;		
	}

	public String getUrlSafeId() {
		return urlSafeId;
	}

	public void setUrlSafeId(final String urlSafeId) {
		this.urlSafeId = urlSafeId;
	}

	public String getId() {
		return id;
	}
	public void setId(final String id) {
		this.id = id;
		setUrlSafeId(urlEncodeUtf8(trimToEmpty(id)));
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(final String email) {
		this.email = email;
	}	
	public Date getOpened() {
		return opened;
	}
	public void setOpened(final Date opened) {
		this.opened = opened;
	}
	public String getBrowser() {
		return browser;
	}
	public void setBrowser(final String browser) {
		this.browser = browser;
	}
	public String getSystem() {
		return system;
	}
	public void setSystem(final String system) {
		this.system = system;
	}
	public String getConfiguration() {
		return configuration.orNull();
	}
	public void setConfiguration(final @Nullable String configuration) {
		this.configuration = fromNullable(configuration);
	}
	public String getSteps() {
		return steps.orNull();
	}
	public void setSteps(final @Nullable String steps) {
		this.steps = fromNullable(steps);
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(final String description) {
		this.description = description;
	}
	public String getScreenshot() {
		return screenshot.orNull();
	}
	public void setScreenshot(final @Nullable String screenshot) {
		this.screenshot = fromNullable(screenshot);
	}
	public IssueStatus getStatus() {
		return status;
	}
	public void setStatus(final IssueStatus status) {
		this.status = status;
	}
	public String getOwner() {
		return owner.orNull();
	}
	public void setOwner(final @Nullable String owner) {
		this.owner = fromNullable(owner);
	}
	public Date getClosed() {
		return closed.orNull();
	}
	public void setClosed(final @Nullable Date closed) {
		this.closed = fromNullable(closed);
	}
	public Map<Long, String> getFollowUp() {
		return followUp;
	}
	public void setFollowUp(final Map<Long, String> followUp) {
		this.followUp.clear();
		if (followUp != null) {
			this.followUp.putAll(followUp);
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Issue)) {
			return false;
		}
		final Issue other = Issue.class.cast(obj);
		return Objects.equals(links, other.links)
				&& Objects.equals(urlSafeId, other.urlSafeId)
				&& equalsIgnoringVolatile(other);
	}

	@Override
	public boolean equalsIgnoringVolatile(final Issue other) {
		return Objects.equals(id, other.id)
				&& Objects.equals(email, other.email)
				&& Objects.equals(opened, other.opened)
				&& Objects.equals(browser, other.browser)
				&& Objects.equals(system, other.system)				
				&& Objects.equals(configuration.orNull(), other.configuration.orNull())
				&& Objects.equals(steps.orNull(), other.steps.orNull())
				&& Objects.equals(description, other.description)
				&& Objects.equals(status, other.status)
				&& Objects.equals(owner.orNull(), other.owner.orNull())
				&& Objects.equals(closed.orNull(), other.closed.orNull())
				&& Objects.equals(followUp, other.followUp)
				&& Objects.equals(screenshot.orNull(), other.screenshot.orNull());
	}

	@Override
	public int hashCode() {
		return Objects.hash(links, urlSafeId, id, email, opened, browser, system, configuration, steps, description, 
				screenshot, status, owner, closed, followUp);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("links", links)
				.add("urlSafeId", urlSafeId)
				.add("id", id)
				.add("email", email)
				.add("opened", opened)
				.add("browser", browser)
				.add("system", system)
				.add("configuration", configuration.orNull())
				.add("steps", steps.orNull())
				.add("description", description)
				.add("screenshot", screenshot.orNull())
				.add("status", status)
				.add("owner", owner.orNull())
				.add("closed", closed.orNull())
				.add("followUp", followUp)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder {

		private final Issue instance = new Issue();

		public Builder newId() {
			instance.setId(compactRandomUUID());
			return this;
		}

		public Builder id(final String id) {
			String id2 = null;
			checkArgument(isNotBlank(id2 = trimToNull(id)), "Uninitialized or invalid id");
			instance.setId(id2);
			return this;
		}

		public Builder email(final String email) {
			String email2 = null;
			checkArgument(isNotBlank(email2 = trimToNull(email)), "Uninitialized or invalid email");
			instance.setEmail(email2);
			return this;
		}

		public Builder opened(final Date opened) {
			instance.setOpened(checkNotNull(opened, "Uninitialized opened date"));
			return this;
		}

		public Builder browser(final String browser) {
			String browser2 = null;
			checkArgument(isNotBlank(browser2 = trimToNull(browser)), "Uninitialized or invalid browser");
			instance.setBrowser(browser2);
			return this;
		}

		public Builder system(final String system) {
			String system2 = null;
			checkArgument(isNotBlank(system2 = trimToNull(system)), "Uninitialized or invalid system");
			instance.setSystem(system2);
			return this;
		}

		public Builder configuration(final @Nullable String configuration) {
			instance.setConfiguration(configuration);
			return this;
		}

		public Builder steps(final @Nullable String steps) {
			instance.setSteps(steps);
			return this;
		}

		public Builder description(final String description) {
			String description2 = null;
			checkArgument(isNotBlank(description2 = trimToNull(description)), "Uninitialized or invalid description");
			instance.setDescription(description2);
			return this;
		}

		public Builder screenshot(final @Nullable String screenshot) {
			instance.setScreenshot(screenshot);
			return this;
		}

		public Builder status(final IssueStatus status) {			
			instance.setStatus(checkNotNull(status, "Uninitialized status"));
			return this;
		}

		public Builder owner(final @Nullable String owner) {
			instance.setOwner(owner);
			return this;
		}

		public Builder closed(final @Nullable Date closed) {
			instance.setClosed(closed);
			return this;
		}

		public Builder setFollowUp(final @Nullable Map<Long, String> followUp) {
			instance.setFollowUp(followUp);
			return this;
		}

		public Issue build() {
			return instance;
		}

	}

}