//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.23 at 01:05:14 PM CEST 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.taxonomy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "genbankCommonName",
    "genbankAcronym",
    "blastName",
    "equivalentNameOrSynonymOrAcronymOrMisspellingOrAnamorphOrIncludesOrCommonNameOrInpartOrMisnomerOrTeleomorphOrGenbankSynonymOrGenbankAnamorph",
    "name"
})
@XmlRootElement(name = "OtherNames")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
public class OtherNames {

    @XmlElement(name = "GenbankCommonName")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
    protected String genbankCommonName;
    @XmlElement(name = "GenbankAcronym")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
    protected String genbankAcronym;
    @XmlElement(name = "BlastName")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
    protected String blastName;
    @XmlElements({
        @XmlElement(name = "EquivalentName", type = EquivalentName.class),
        @XmlElement(name = "Synonym", type = Synonym.class),
        @XmlElement(name = "Acronym", type = Acronym.class),
        @XmlElement(name = "Misspelling", type = Misspelling.class),
        @XmlElement(name = "Anamorph", type = Anamorph.class),
        @XmlElement(name = "Includes", type = Includes.class),
        @XmlElement(name = "CommonName", type = CommonName.class),
        @XmlElement(name = "Inpart", type = Inpart.class),
        @XmlElement(name = "Misnomer", type = Misnomer.class),
        @XmlElement(name = "Teleomorph", type = Teleomorph.class),
        @XmlElement(name = "GenbankSynonym", type = GenbankSynonym.class),
        @XmlElement(name = "GenbankAnamorph", type = GenbankAnamorph.class)
    })
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
    protected List<Object> equivalentNameOrSynonymOrAcronymOrMisspellingOrAnamorphOrIncludesOrCommonNameOrInpartOrMisnomerOrTeleomorphOrGenbankSynonymOrGenbankAnamorph;
    @XmlElement(name = "Name")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
    protected List<Name> name;

    /**
     * Gets the value of the genbankCommonName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
    public String getGenbankCommonName() {
        return genbankCommonName;
    }

    /**
     * Sets the value of the genbankCommonName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
    public void setGenbankCommonName(String value) {
        this.genbankCommonName = value;
    }

    /**
     * Gets the value of the genbankAcronym property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
    public String getGenbankAcronym() {
        return genbankAcronym;
    }

    /**
     * Sets the value of the genbankAcronym property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
    public void setGenbankAcronym(String value) {
        this.genbankAcronym = value;
    }

    /**
     * Gets the value of the blastName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
    public String getBlastName() {
        return blastName;
    }

    /**
     * Sets the value of the blastName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
    public void setBlastName(String value) {
        this.blastName = value;
    }

    /**
     * Gets the value of the equivalentNameOrSynonymOrAcronymOrMisspellingOrAnamorphOrIncludesOrCommonNameOrInpartOrMisnomerOrTeleomorphOrGenbankSynonymOrGenbankAnamorph property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the equivalentNameOrSynonymOrAcronymOrMisspellingOrAnamorphOrIncludesOrCommonNameOrInpartOrMisnomerOrTeleomorphOrGenbankSynonymOrGenbankAnamorph property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEquivalentNameOrSynonymOrAcronymOrMisspellingOrAnamorphOrIncludesOrCommonNameOrInpartOrMisnomerOrTeleomorphOrGenbankSynonymOrGenbankAnamorph().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EquivalentName }
     * {@link Synonym }
     * {@link Acronym }
     * {@link Misspelling }
     * {@link Anamorph }
     * {@link Includes }
     * {@link CommonName }
     * {@link Inpart }
     * {@link Misnomer }
     * {@link Teleomorph }
     * {@link GenbankSynonym }
     * {@link GenbankAnamorph }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
    public List<Object> getEquivalentNameOrSynonymOrAcronymOrMisspellingOrAnamorphOrIncludesOrCommonNameOrInpartOrMisnomerOrTeleomorphOrGenbankSynonymOrGenbankAnamorph() {
        if (equivalentNameOrSynonymOrAcronymOrMisspellingOrAnamorphOrIncludesOrCommonNameOrInpartOrMisnomerOrTeleomorphOrGenbankSynonymOrGenbankAnamorph == null) {
            equivalentNameOrSynonymOrAcronymOrMisspellingOrAnamorphOrIncludesOrCommonNameOrInpartOrMisnomerOrTeleomorphOrGenbankSynonymOrGenbankAnamorph = new ArrayList<Object>();
        }
        return this.equivalentNameOrSynonymOrAcronymOrMisspellingOrAnamorphOrIncludesOrCommonNameOrInpartOrMisnomerOrTeleomorphOrGenbankSynonymOrGenbankAnamorph;
    }

    /**
     * Gets the value of the name property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the name property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getName().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Name }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
    public List<Name> getName() {
        if (name == null) {
            name = new ArrayList<Name>();
        }
        return this.name;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
    public OtherNames withGenbankCommonName(String value) {
        setGenbankCommonName(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
    public OtherNames withGenbankAcronym(String value) {
        setGenbankAcronym(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
    public OtherNames withBlastName(String value) {
        setBlastName(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
    public OtherNames withEquivalentNameOrSynonymOrAcronymOrMisspellingOrAnamorphOrIncludesOrCommonNameOrInpartOrMisnomerOrTeleomorphOrGenbankSynonymOrGenbankAnamorph(Object... values) {
        if (values!= null) {
            for (Object value: values) {
                getEquivalentNameOrSynonymOrAcronymOrMisspellingOrAnamorphOrIncludesOrCommonNameOrInpartOrMisnomerOrTeleomorphOrGenbankSynonymOrGenbankAnamorph().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
    public OtherNames withEquivalentNameOrSynonymOrAcronymOrMisspellingOrAnamorphOrIncludesOrCommonNameOrInpartOrMisnomerOrTeleomorphOrGenbankSynonymOrGenbankAnamorph(Collection<Object> values) {
        if (values!= null) {
            getEquivalentNameOrSynonymOrAcronymOrMisspellingOrAnamorphOrIncludesOrCommonNameOrInpartOrMisnomerOrTeleomorphOrGenbankSynonymOrGenbankAnamorph().addAll(values);
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
    public OtherNames withName(Name... values) {
        if (values!= null) {
            for (Name value: values) {
                getName().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
    public OtherNames withName(Collection<Name> values) {
        if (values!= null) {
            getName().addAll(values);
        }
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:05:14+02:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
