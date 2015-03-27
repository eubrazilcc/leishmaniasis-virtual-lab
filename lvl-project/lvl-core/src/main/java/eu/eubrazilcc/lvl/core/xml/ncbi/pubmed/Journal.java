//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.26 at 04:30:37 PM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.pubmed;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
    "issn",
    "journalIssue",
    "title",
    "isoAbbreviation"
})
@XmlRootElement(name = "Journal")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
public class Journal {

    @XmlElement(name = "ISSN")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    protected ISSN issn;
    @XmlElement(name = "JournalIssue", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    protected JournalIssue journalIssue;
    @XmlElement(name = "Title")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    protected String title;
    @XmlElement(name = "ISOAbbreviation")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    protected String isoAbbreviation;

    /**
     * Gets the value of the issn property.
     * 
     * @return
     *     possible object is
     *     {@link ISSN }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public ISSN getISSN() {
        return issn;
    }

    /**
     * Sets the value of the issn property.
     * 
     * @param value
     *     allowed object is
     *     {@link ISSN }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public void setISSN(ISSN value) {
        this.issn = value;
    }

    /**
     * Gets the value of the journalIssue property.
     * 
     * @return
     *     possible object is
     *     {@link JournalIssue }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public JournalIssue getJournalIssue() {
        return journalIssue;
    }

    /**
     * Sets the value of the journalIssue property.
     * 
     * @param value
     *     allowed object is
     *     {@link JournalIssue }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public void setJournalIssue(JournalIssue value) {
        this.journalIssue = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the isoAbbreviation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public String getISOAbbreviation() {
        return isoAbbreviation;
    }

    /**
     * Sets the value of the isoAbbreviation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public void setISOAbbreviation(String value) {
        this.isoAbbreviation = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public Journal withISSN(ISSN value) {
        setISSN(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public Journal withJournalIssue(JournalIssue value) {
        setJournalIssue(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public Journal withTitle(String value) {
        setTitle(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public Journal withISOAbbreviation(String value) {
        setISOAbbreviation(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(java.lang.Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-26T04:30:37+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
