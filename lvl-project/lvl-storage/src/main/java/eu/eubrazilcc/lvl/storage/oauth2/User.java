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

package eu.eubrazilcc.lvl.storage.oauth2;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.JaxbAdapter;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Objects;

/**
 * User identity provider. Include JAXB annotations to serialize this class to XML and JSON.
 * Most JSON processing libraries like Jackson support these JAXB annotations.
 * @author Erik Torres <ertorser@upv.es>
 */
@XmlRootElement
public class User implements Serializable {

	private static final long serialVersionUID = -8320525767063830149L;

	private Link link;
	private String pictureUrl;
	
	private String username;
	private String password;
	private String email;
	private String fullname;
	private Set<String> scopes;

	public User() { }

	@XmlElement(name="link")
	@XmlJavaTypeAdapter(JaxbAdapter.class)
	public Link getLink() {
		return link;
	}
	public void setLink(final Link link) {
		this.link = link;
	}
	public String getPictureUrl() {
		return pictureUrl;
	}
	public void setPictureUrl(final String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(final String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(final String password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(final String email) {
		this.email = email;
	}
	public String getFullname() {
		return fullname;
	}
	public void setFullname(final String fullname) {
		this.fullname = fullname;
	}
	public Set<String> getScopes() {
		return scopes;
	}
	public void setScopes(final Set<String> scopes) {
		this.scopes = scopes;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof User)) {
			return false;
		}
		final User other = User.class.cast(obj);
		return Objects.equal(link, other.link)
				&& Objects.equal(pictureUrl, other.pictureUrl)
				&& equalsIgnoreVolatile(other);
	}

	public boolean equalsIgnoreVolatile(final User other) {
		if (other == null) {
			return false;
		}
		return Objects.equal(username, other.username)
				&& Objects.equal(password, other.password)
				&& Objects.equal(email, other.email)				
				&& Objects.equal(fullname, other.fullname)
				&& Objects.equal(scopes, other.scopes);
	}

	public boolean equalsToAnonymous(final User other) {
		if (other == null) {
			return false;
		}
		return Objects.equal(username, other.username)			
				&& Objects.equal(fullname, other.fullname)
				&& Objects.equal(scopes, other.scopes);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(link, pictureUrl, username, password, email, fullname, scopes);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("link", link)
				.add("pictureUrl", pictureUrl)
				.add("username", username)
				.add("password", password)
				.add("email", email)				
				.add("fullname", fullname)
				.add("scopes", scopes)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder {

		private final User user = new User();

		public Builder() {			
			user.setScopes(new HashSet<String>());
		}

		public Builder link(final Link link) {
			user.setLink(link);
			return this;
		}
		
		public Builder pictureUrl(final String pictureUrl) {
			user.setPictureUrl(pictureUrl);
			return this;
		}

		public Builder username(final String username) {
			checkArgument(isNotBlank(username), "Uninitialized or invalid username");
			user.setUsername(username);
			return this;
		}

		public Builder password(final String password) {
			checkArgument(isNotBlank(password), "Uninitialized or invalid password");
			user.setPassword(password);
			return this;
		}

		public Builder email(final String email) {
			checkArgument(isNotBlank(email), "Uninitialized or invalid email");
			user.setEmail(email);
			return this;
		}

		public Builder fullname(final String fullname) {
			checkArgument(isNotBlank(fullname), "Uninitialized or invalid fullname");
			user.setFullname(fullname);
			return this;
		}

		public Builder scope(final String scope) {
			checkArgument(isNotBlank(scope), "Uninitialized or invalid scope");
			user.getScopes().add(scope);
			return this;
		}

		public Builder scopes(final Collection<String> scopes) {
			checkArgument(scopes != null && !isEmpty(scopes), "Uninitialized scopes");
			user.getScopes().addAll(scopes);
			return this;
		}

		public User build() {
			return user;
		}

	}

}