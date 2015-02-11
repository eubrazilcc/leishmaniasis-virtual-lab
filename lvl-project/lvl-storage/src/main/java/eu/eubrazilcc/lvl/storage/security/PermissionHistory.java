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

package eu.eubrazilcc.lvl.storage.security;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.Date;
import java.util.Objects;

import eu.eubrazilcc.lvl.storage.mongodb.MongoDBMap;
import eu.eubrazilcc.lvl.storage.mongodb.MongoDBMapKey;

/**
 * Tracks permission changes.
 * @author Erik Torres <ertorser@upv.es> 
 */
public class PermissionHistory {

	private MongoDBMap<MongoDBMapKey, PermissionModification> history = new MongoDBMap<>();

	public MongoDBMap<MongoDBMapKey, PermissionModification> getHistory() {
		return history;
	}

	public void setHistory(final MongoDBMap<MongoDBMapKey, PermissionModification> history) {		
		this.history = history != null ? new MongoDBMap<MongoDBMapKey, PermissionModification>(history) : new MongoDBMap<MongoDBMapKey, PermissionModification>();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof PermissionHistory)) {
			return false;
		}
		final PermissionHistory other = PermissionHistory.class.cast(obj);
		return Objects.equals(history, other.history);
	}

	@Override
	public int hashCode() {
		return Objects.hash(history);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("history", history)
				.toString();
	}

	/* Inner classes */	

	/**
	 * Holds details about permission modification.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class PermissionModification {

		private Date modificationDate;
		private PermissionModificationType modificationType;

		public PermissionModification() {
		}

		public PermissionModification(final Date modificationDate, final PermissionModificationType modificationType) {
			this.modificationDate = modificationDate;
			this.modificationType = modificationType;
		}

		public Date getModificationDate() {
			return modificationDate;
		}
		public void setModificationDate(final Date modificationDate) {
			this.modificationDate = modificationDate;
		}
		public PermissionModificationType getModificationType() {
			return modificationType;
		}
		public void setModificationType(final PermissionModificationType modificationType) {
			this.modificationType = modificationType;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null || !(obj instanceof PermissionModification)) {
				return false;
			}
			final PermissionModification other = PermissionModification.class.cast(obj);
			return Objects.equals(modificationDate, other.modificationDate)
					&& Objects.equals(modificationType, other.modificationType);
		}

		@Override
		public int hashCode() {
			return Objects.hash(modificationDate, modificationType);
		}

		@Override
		public String toString() {
			return toStringHelper(this)
					.add("modificationDate", modificationDate)
					.add("modificationType", modificationType)
					.toString();
		}

	}

	/**
	 * Modification type: permission was granted, removed, etc.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static enum PermissionModificationType {
		GRANTED,
		REMOVED
	}

}