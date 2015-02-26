//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.26 at 02:41:07 PM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.pubmed;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
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
    "volume",
    "issue",
    "pubDate"
})
@XmlRootElement(name = "JournalIssue")
public class JournalIssue {

    @XmlAttribute(name = "CitedMedium", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String citedMedium;
    @XmlElement(name = "Volume")
    protected String volume;
    @XmlElement(name = "Issue")
    protected String issue;
    @XmlElement(name = "PubDate", required = true)
    protected PubDate pubDate;

    /**
     * Gets the value of the citedMedium property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCitedMedium() {
        return citedMedium;
    }

    /**
     * Sets the value of the citedMedium property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCitedMedium(String value) {
        this.citedMedium = value;
    }

    /**
     * Gets the value of the volume property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVolume() {
        return volume;
    }

    /**
     * Sets the value of the volume property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVolume(String value) {
        this.volume = value;
    }

    /**
     * Gets the value of the issue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIssue() {
        return issue;
    }

    /**
     * Sets the value of the issue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIssue(String value) {
        this.issue = value;
    }

    /**
     * Gets the value of the pubDate property.
     * 
     * @return
     *     possible object is
     *     {@link PubDate }
     *     
     */
    public PubDate getPubDate() {
        return pubDate;
    }

    /**
     * Sets the value of the pubDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link PubDate }
     *     
     */
    public void setPubDate(PubDate value) {
        this.pubDate = value;
    }

    public JournalIssue withCitedMedium(String value) {
        setCitedMedium(value);
        return this;
    }

    public JournalIssue withVolume(String value) {
        setVolume(value);
        return this;
    }

    public JournalIssue withIssue(String value) {
        setIssue(value);
        return this;
    }

    public JournalIssue withPubDate(PubDate value) {
        setPubDate(value);
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    public boolean equals(java.lang.Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
