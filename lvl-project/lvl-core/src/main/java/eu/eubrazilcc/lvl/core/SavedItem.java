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

package eu.eubrazilcc.lvl.core;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.util.Date;
import java.util.Objects;

import com.google.common.base.Optional;

/**
 * Saved item.
 * @author Erik Torres <ertorser@upv.es>
 */
public abstract class SavedItem {

	public static final String TYPE_SEPARATOR = ";";

	protected String id;
	protected String type;
	protected Optional<String> description = absent();
	protected Optional<Date> saved = absent();	

	public String getId() {
		return id;
	}

	public abstract void setId(String id);

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public String getDescription() {
		return description.orNull();
	}

	public void setDescription(final String description) {
		this.description = fromNullable(description);
	}

	public Date getSaved() {
		return saved.orNull();
	}

	public void setSaved(final Date saved) {
		this.saved = fromNullable(saved);
	}

	public static String parseType(final String type) {
		String type2 = null;
		checkArgument(isNotBlank(type2 = trimToEmpty(type)
				.replaceAll("(\\w)(\\s+)(" + TYPE_SEPARATOR+ ")", "$1$3")
				.replaceAll("(" + TYPE_SEPARATOR+ ")(\\s+)(\\w)", "$1$3")				
				.replaceAll("\\s+", "_")), "Uninitialized or invalid type");
		checkArgument(type2.matches("(\\w+" + TYPE_SEPARATOR + "*\\w+)+"), "Invalid type found: '" + type2 + "'");
		return type2;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof SavedItem)) {
			return false;
		}
		final SavedItem other = SavedItem.class.cast(obj);
		return Objects.equals(id, other.id)
				&& Objects.equals(type, other.type)				
				&& Objects.equals(description.orNull(), other.description.orNull())
				&& Objects.equals(saved.orNull(), other.saved.orNull());
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, type, description, saved);
	}

	@Override
	public String toString() {
		return toStringHelper(this)				
				.add("id", id)
				.add("type", type)
				.add("description", description.orNull())
				.add("saved", saved.orNull())
				.toString();
	}

}