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
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Ordering.from;
import static java.util.Collections.checkedSet;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trimToNull;

import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ComparisonChain;

/**
 * Tracks permission changes.
 * @author Erik Torres <ertorser@upv.es> 
 */
public class PermissionHistory {

	private Set<PermissionModification> history = checkedSet(new HashSet<PermissionModification>(), PermissionModification.class);

	public Set<PermissionModification> getHistory() {
		return history;
	}

	public void setHistory(final Set<PermissionModification> history) {		
		this.history = checkedSet(history != null ? new HashSet<PermissionModification>(history) 
				: new HashSet<PermissionModification>(), PermissionModification.class);
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

	public static @Nullable PermissionModification latestModification(final Set<PermissionModification> history, final String permission) {
		String permission2 = null;
		checkArgument(history != null, "Uninitialized or invalid history");
		checkArgument(isNotBlank(permission2 = trimToNull(permission)), "Uninitialized or invalid permission");
		final List<PermissionModification> sortedList = from(new Comparator<PermissionModification>() {
			@Override
			public int compare(final PermissionModification pm1, final PermissionModification pm2) {
				if (pm1 == pm2) return 0;
				return ComparisonChain.start()
						.compare(pm1.getModificationDate(), pm2.getModificationDate())
						.compare(pm1.getPermission(), pm2.getPermission())
						.compare(pm1.getModificationType(), pm2.getModificationType())
						.result();
			}
		}).reverse().sortedCopy(history);
		PermissionModification latestModification = null;
		for (int i = 0; i < sortedList.size() && latestModification == null; i++) {
			final PermissionModification item = sortedList.get(i);
			if (permission2.equals(item.getPermission())) {
				latestModification = item;
			}
		}
		return latestModification;
	}

	/* Inner classes */

	/**
	 * Holds details about permission modification.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class PermissionModification {

		private String permission;
		private Date modificationDate;
		private PermissionModificationType modificationType;

		public PermissionModification() {
		}

		public String getPermission() {
			return permission;
		}
		public void setPermission(final String permission) {
			this.permission = permission;
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
			return Objects.equals(permission, other.permission)
					&& Objects.equals(modificationDate, other.modificationDate)
					&& Objects.equals(modificationType, other.modificationType);
		}

		@Override
		public int hashCode() {
			return Objects.hash(permission, modificationDate, modificationType);
		}

		@Override
		public String toString() {
			return toStringHelper(this)					
					.add("permission", permission)
					.add("modificationDate", modificationDate)
					.add("modificationType", modificationType)
					.toString();
		}

		/* Fluent API */

		public static Builder builder() {
			return new Builder();
		}	

		public static class Builder {

			private final PermissionModification instance = new PermissionModification();

			public Builder permission(final String permission) {
				instance.setPermission(permission);				
				return this;
			}

			public Builder modificationDate(final Date modificationDate) {
				instance.setModificationDate(modificationDate);				
				return this;
			}

			public Builder modificationType(final PermissionModificationType modificationType) {
				instance.setModificationType(modificationType);				
				return this;
			}

			public PermissionModification build() {
				return instance;
			}

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