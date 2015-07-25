//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.23 at 01:04:59 PM CEST 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.pubmed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "lastNameOrForeNameOrInitialsOrSuffixOrNameIDOrCollectiveName"
})
@XmlRootElement(name = "Author")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
public class Author {

    @XmlAttribute(name = "ValidYN")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    protected String validYN;
    @XmlElements({
        @XmlElement(name = "LastName", required = true, type = LastName.class),
        @XmlElement(name = "ForeName", required = true, type = ForeName.class),
        @XmlElement(name = "Initials", required = true, type = Initials.class),
        @XmlElement(name = "Suffix", required = true, type = Suffix.class),
        @XmlElement(name = "NameID", required = true, type = NameID.class),
        @XmlElement(name = "CollectiveName", required = true, type = CollectiveName.class)
    })
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    protected List<java.lang.Object> lastNameOrForeNameOrInitialsOrSuffixOrNameIDOrCollectiveName;

    /**
     * Gets the value of the validYN property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public String getValidYN() {
        if (validYN == null) {
            return "Y";
        } else {
            return validYN;
        }
    }

    /**
     * Sets the value of the validYN property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public void setValidYN(String value) {
        this.validYN = value;
    }

    /**
     * Gets the value of the lastNameOrForeNameOrInitialsOrSuffixOrNameIDOrCollectiveName property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lastNameOrForeNameOrInitialsOrSuffixOrNameIDOrCollectiveName property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLastNameOrForeNameOrInitialsOrSuffixOrNameIDOrCollectiveName().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LastName }
     * {@link ForeName }
     * {@link Initials }
     * {@link Suffix }
     * {@link NameID }
     * {@link CollectiveName }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public List<java.lang.Object> getLastNameOrForeNameOrInitialsOrSuffixOrNameIDOrCollectiveName() {
        if (lastNameOrForeNameOrInitialsOrSuffixOrNameIDOrCollectiveName == null) {
            lastNameOrForeNameOrInitialsOrSuffixOrNameIDOrCollectiveName = new ArrayList<java.lang.Object>();
        }
        return this.lastNameOrForeNameOrInitialsOrSuffixOrNameIDOrCollectiveName;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public Author withValidYN(String value) {
        setValidYN(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public Author withLastNameOrForeNameOrInitialsOrSuffixOrNameIDOrCollectiveName(java.lang.Object... values) {
        if (values!= null) {
            for (java.lang.Object value: values) {
                getLastNameOrForeNameOrInitialsOrSuffixOrNameIDOrCollectiveName().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public Author withLastNameOrForeNameOrInitialsOrSuffixOrNameIDOrCollectiveName(Collection<java.lang.Object> values) {
        if (values!= null) {
            getLastNameOrForeNameOrInitialsOrSuffixOrNameIDOrCollectiveName().addAll(values);
        }
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public boolean equals(java.lang.Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
