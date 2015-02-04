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

import java.util.Date;
import java.util.Objects;

/**
 * Capable of being shared.
 * @author Erik Torres <ertorser@upv.es>
 */
public abstract class Shareable {

	private String subject;
	private Date sharedDate;
	private SharedAccess accessType;

	/**
	 * Gets who is receiving access to the object being shared.
	 * @return identity of the user who is receiving access to the object being shared.
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * Sets who is receiving access to the object being shared.
	 * @param subject - the identity of the user who is receiving access to the object being shared
	 */
	public void setSubject(final String subject) {
		this.subject = subject;
	}

	/**
	 * Gets the moment when the object was shared.
	 * @return the moment when the object was shared.
	 */
	public Date getSharedDate() {
		return sharedDate;
	}

	/**
	 * Sets the moment when the object was shared.
	 * @param sharedDate - the moment when the object was shared
	 */
	public void setSharedDate(final Date sharedDate) {
		this.sharedDate = sharedDate;
	}

	/**
	 * Gets the type of access granted over a shared object.
	 * @return the type of access granted over a shared object.
	 */
	public SharedAccess getAccessType() {
		return accessType;
	}

	/**
	 * Sets the type of access granted over a shared object.
	 * @param accessType - the type of access granted over a shared object
	 */
	public void setAccessType(final SharedAccess accessType) {
		this.accessType = accessType;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Shareable)) {
			return false;
		}
		final Shareable other = Shareable.class.cast(obj);
		return  Objects.equals(subject, other.subject)
				&& Objects.equals(sharedDate, other.sharedDate)
				&& Objects.equals(accessType, other.accessType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(subject, sharedDate, accessType);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("subject", subject)
				.add("sharedDate", sharedDate)
				.add("accessType", accessType)
				.toString();
	}

	/**
	 * Defines the type of access granted over a shared object.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static enum SharedAccess {
		/**
		 * Grants access to view the shared object.
		 */
		VIEW_SHARE,
		/**
		 * Grants access to view and modify the shared object.
		 */
		EDIT_SHARE
	}	

}