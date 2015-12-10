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

import java.util.Objects;

/**
 * Stores the details of a sample preparation.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SamplePreparation {

	private String sex;
	private int individualCount;
	private String collectingMethod;
	private String preparationType;
	private String materialType;

	public String getSex() {
		return sex;
	}

	public void setSex(final String sex) {
		this.sex = sex;
	}

	public int getIndividualCount() {
		return individualCount;
	}

	public void setIndividualCount(final int individualCount) {
		this.individualCount = individualCount;
	}

	public String getCollectingMethod() {
		return collectingMethod;
	}

	public void setCollectingMethod(final String collectingMethod) {
		this.collectingMethod = collectingMethod;
	}

	public String getPreparationType() {
		return preparationType;
	}

	public void setPreparationType(final String preparationType) {
		this.preparationType = preparationType;
	}

	public String getMaterialType() {
		return materialType;
	}

	public void setMaterialType(final String materialType) {
		this.materialType = materialType;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof SamplePreparation)) {
			return false;
		}
		final SamplePreparation other = SamplePreparation.class.cast(obj);
		return Objects.equals(sex, other.sex)
				&& Objects.equals(individualCount, other.individualCount)
				&& Objects.equals(collectingMethod, other.collectingMethod)
				&& Objects.equals(preparationType, other.preparationType)
				&& Objects.equals(materialType, other.materialType);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(sex, individualCount, collectingMethod, preparationType, materialType);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("sex", sex)
				.add("individualCount", individualCount)
				.add("collectingMethod", collectingMethod)
				.add("preparationType", preparationType)
				.add("materialType", materialType)				
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder {

		private final SamplePreparation instance = new SamplePreparation();

		public Builder sex(final String sex) {
			instance.setSex(sex);
			return this;
		}

		public Builder individualCount(final int individualCount) {
			instance.setIndividualCount(individualCount);
			return this;
		}

		public Builder collectingMethod(final String collectingMethod) {
			instance.setCollectingMethod(collectingMethod);
			return this;
		}

		public Builder preparationType(final String preparationType) {
			instance.setPreparationType(preparationType);
			return this;
		}

		public Builder materialType(final String materialType) {
			instance.setMaterialType(materialType);
			return this;
		}

		public SamplePreparation build() {
			return instance;
		}

	}

}