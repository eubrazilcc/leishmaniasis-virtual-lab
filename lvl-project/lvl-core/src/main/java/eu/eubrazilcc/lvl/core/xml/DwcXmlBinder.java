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

package eu.eubrazilcc.lvl.core.xml;

import static com.google.common.base.Preconditions.checkArgument;
import static eu.eubrazilcc.lvl.core.geocoding.ReverseGeocodingHelper.rgeocode;
import static eu.eubrazilcc.lvl.core.util.LocaleUtils.getLocale;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.Locale;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBElement;

import eu.eubrazilcc.lvl.core.Sample;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.xml.tdwg.dwc.ObjectFactory;
import eu.eubrazilcc.lvl.core.xml.tdwg.dwc.SimpleDarwinRecord;
import eu.eubrazilcc.lvl.core.xml.tdwg.dwc.SimpleDarwinRecordSet;

/**
 * TDWG Darwin Core data format standard for the exchange of species occurrence data - XML binding helper.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://www.tdwg.org/activities/darwincore">DarwinCore Group - DwC</a>
 */
public class DwcXmlBinder extends XmlBinder {

	private static final Class<?>[] SUPPORTED_CLASSES = {
			SimpleDarwinRecordSet.class,
			SimpleDarwinRecord.class
	};

	public static final ObjectFactory DWC_XML_FACTORY = new ObjectFactory();

	public static final DwcXmlBinder DWC_XMLB = new DwcXmlBinder();

	private DwcXmlBinder() {
		super(SUPPORTED_CLASSES);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <T> JAXBElement<T> createType(final T obj) {
		Object element = null;
		Class<? extends Object> clazz = obj.getClass();
		if (clazz.equals(SimpleDarwinRecordSet.class)) {
			element = DWC_XML_FACTORY.createSimpleDarwinRecordSet();
		} else if (clazz.equals(SimpleDarwinRecord.class)) {
			element = DWC_XML_FACTORY.createSimpleDarwinRecord();
		} else {
			throw new IllegalArgumentException("Unsupported type: " + clazz.getCanonicalName());
		}
		return (JAXBElement<T>) element;
	}

	public static final <T extends Sample> T parseSample(final SimpleDarwinRecord record, final String collection, final Sample.Builder<T> builder) {
		requireNonNull(record, "Valid record expected");
		checkArgument(isNotBlank(collection), "Non-empty collection expected");
		requireNonNull(builder, "Valid builder expected");
		final Point location = Point.builder().coordinates(LngLatAlt.builder().coordinates(record.getDecimalLongitude(), record.getDecimalLatitude()).build()).build();
		return builder
				.collectionId(collection.trim())
				.catalogNumber(record.getCatalogNumber())											
				.location(location)
				.locale(countryToLocale(record.getCountry(), location))
				.sample(record)
				.build();		
	}

	/**
	 * Converts country to Java {@link Locale}. Java {@link Locale} allows latter to export the country to several different formats, including 
	 * a two-letter code compatible with ISO 3166-1 alpha-2 standard.
	 * @param country - value of country field
	 * @param location - geographic coordinates
	 * @return a Java {@link Locale} inferred from the input sample.
	 */
	public static final Locale countryToLocale(final @Nullable String country, final @Nullable Point location) {
		Locale locale = null;
		if (isNotBlank(country)) {
			locale = getLocale("Brasil".equalsIgnoreCase(country) ? "Brazil" : country);
		}
		if (locale == null && location != null) {
			final String country2 = rgeocode(location).orElse(null);
			if (isNotBlank(country2)) {
				locale = getLocale(country2);
			}
		}
		return locale;
	}

}