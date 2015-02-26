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
import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.apache.commons.lang.StringUtils.trimToNull;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;

import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;

/**
 * A generic LVL instance that can play several roles.
 * @author Erik Torres <ertorser@upv.es>
 */
public class LvlInstance implements Localizable<Point>, Linkable<LvlInstance> {

	@InjectLinks({
		@InjectLink(value="instances/{urlSafeInstanceId}", rel=SELF, type=APPLICATION_JSON, bindings={
				@Binding(name="urlSafeInstanceId", value="${instance.urlSafeInstanceId}")
		})
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links; // HATEOAS links

	@JsonIgnore
	private String urlSafeInstanceId;

	private String instanceId;
	private Set<String> roles = newHashSet();
	private Optional<Date> heartbeat = absent();
	private Point location;

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

	public String getUrlSafeInstanceId() {
		return urlSafeInstanceId;
	}

	public void setUrlSafeInstanceId(final String urlSafeInstanceId) {
		this.urlSafeInstanceId = urlSafeInstanceId;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(final String instanceId) {
		this.instanceId = instanceId;
		setUrlSafeInstanceId(urlEncodeUtf8(trimToEmpty(instanceId)));
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(final Set<String> roles) {
		if (roles != null) {
			this.roles = newHashSet(roles);
		} else {
			this.roles = null;
		}
	}

	public Date getHeartbeat() {
		return heartbeat.or(new Date(0l));
	}

	public void setHeartbeat(final Date heartbeat) {
		this.heartbeat = fromNullable(heartbeat);
	}	

	@Override
	public Point getLocation() {
		return location;
	}

	@Override
	public void setLocation(final Point location) {
		this.location = location;
	}
	
	@JsonIgnore
	@Override
	public String getTag() {
		return instanceId;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof LvlInstance)) {
			return false;
		}
		final LvlInstance other = LvlInstance.class.cast(obj);
		return  Objects.equals(urlSafeInstanceId, other.urlSafeInstanceId)				
				&& equalsIgnoringVolatile(other);
	}

	@Override
	public boolean equalsIgnoringVolatile(final LvlInstance other) {
		if (other == null) {
			return false;
		}
		return Objects.equals(instanceId,other.instanceId)
				&& Objects.equals(roles, other.roles)
				&& Objects.equals(links, other.links)
				&& Objects.equals(heartbeat, other.heartbeat)
				&& Objects.equals(location, other.location);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(links, urlSafeInstanceId, instanceId, roles, heartbeat, location);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("links", links)
				.add("urlSafeId", urlSafeInstanceId)
				.add("instanceId", instanceId)
				.add("roles",roles)
				.add("heartbeat", heartbeat.orNull())
				.add("location", location)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder {

		private final LvlInstance instance = new LvlInstance();

		public Builder links(final List<Link> links) {
			instance.setLinks(links);
			return this;
		}

		public Builder instanceId(final String instanceId) {
			String instanceId2 = null;
			checkArgument(isNotBlank(instanceId2 = trimToNull(instanceId)), "Uninitialized or invalid instanceId");
			instance.setInstanceId(instanceId2);
			return this;
		}

		public Builder roles(final Set<String> roles) {
			instance.setRoles(roles);
			return this;
		}

		public Builder heartbeat(final Date heartbeat) {
			instance.setHeartbeat(heartbeat);
			return this;
		}

		public Builder location(final Point location) {
			instance.setLocation(location);
			return this;
		}

		public LvlInstance build() {
			return instance;
		}

	}

}