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
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang.StringUtils.trimToNull;

import java.util.Date;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Provides the basis for metadata storage, including a {@link Versionable#getIsLastestVersion() property} to label the latest 
 * version of a file.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Metadata extends Versionable {

	private String originalFilename;
	private String openAccessLink;
	private Date openAccessDate;

	private String editor;
	private Set<String> tags = newHashSet();
	private String description;
	private Target target;

	public @Nullable String getOriginalFilename() {
		return originalFilename;
	}

	public void setOriginalFilename(final @Nullable String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public @Nullable String getOpenAccessLink() {
		return openAccessLink;
	}

	public void setOpenAccessLink(final @Nullable String openAccessLink) {
		this.openAccessLink = openAccessLink;
		setOpenAccessDate(new Date());
	}

	public @Nullable Date getOpenAccessDate() {
		return openAccessDate;
	}

	public void setOpenAccessDate(final @Nullable Date openAccessDate) {
		this.openAccessDate = openAccessDate;
	}

	public @Nullable String getEditor() {
		return editor;
	}

	public void setEditor(final @Nullable String editor) {
		this.editor = editor;
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(final Set<String> tags) {
		if (tags != null) {
			this.tags = newHashSet(tags);
		} else {
			this.tags = newHashSet();
		}
	}

	public @Nullable String getDescription() {
		return description;
	}

	public void setDescription(final @Nullable String description) {
		this.description = description;
	}

	public @Nullable Target getTarget() {
		return target;
	}

	public void setTarget(final @Nullable Target target) {
		this.target = target;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Metadata)) {
			return false;
		}
		final Metadata other = Metadata.class.cast(obj);
		return super.equals((Versionable)other)
				&& Objects.equals(originalFilename, other.originalFilename)
				&& Objects.equals(openAccessLink, other.openAccessLink)
				&& Objects.equals(openAccessDate, other.openAccessDate)
				&& Objects.equals(editor, other.editor)
				&& Objects.equals(tags, other.tags)					
				&& Objects.equals(description, other.description)
				&& Objects.equals(target, other.target);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(originalFilename, openAccessLink, openAccessDate, editor, tags, description, target);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("Versionable", super.toString())
				.add("originalFilename", originalFilename)
				.add("openAccessLink", openAccessLink)
				.add("openAccessDate", openAccessDate)
				.add("editor", editor)
				.add("tags", tags)					
				.add("description", description)
				.add("target", target)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final Metadata instance = new Metadata();

		public Builder isLastestVersion(final String isLastestVersion) {
			instance.setIsLastestVersion(isLastestVersion);
			return this;
		}

		public Builder editor(final String editor) {
			instance.setEditor(trimToNull(editor));
			return this;
		}

		public Builder tags(final Set<String> tags) {
			instance.setTags(tags);
			return this;
		}

		public Builder originalFilename(final String originalFilename) {
			instance.setOriginalFilename(trimToNull(originalFilename));
			return this;
		}

		public Builder openAccessLink(final String openAccessLink) {
			instance.setOpenAccessLink(trimToNull(openAccessLink));
			return this;
		}

		public Builder openAccessDate(final Date openAccessDate) {
			instance.setOpenAccessDate(openAccessDate);
			return this;
		}

		public Builder description(final String description) {
			instance.setDescription(trimToNull(description));
			return this;
		}

		public Builder target(final Target target) {
			instance.setTarget(target);
			return this;
		}

		public Metadata build() {
			return instance;
		}

	}

}