//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.02 at 08:40:23 AM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.pubmed;

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
public class Journal {

    @XmlElement(name = "ISSN")
    protected ISSN issn;
    @XmlElement(name = "JournalIssue", required = true)
    protected JournalIssue journalIssue;
    @XmlElement(name = "Title")
    protected String title;
    @XmlElement(name = "ISOAbbreviation")
    protected String isoAbbreviation;

    /**
     * Gets the value of the issn property.
     * 
     * @return
     *     possible object is
     *     {@link ISSN }
     *     
     */
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
    public void setISOAbbreviation(String value) {
        this.isoAbbreviation = value;
    }

    public Journal withISSN(ISSN value) {
        setISSN(value);
        return this;
    }

    public Journal withJournalIssue(JournalIssue value) {
        setJournalIssue(value);
        return this;
    }

    public Journal withTitle(String value) {
        setTitle(value);
        return this;
    }

    public Journal withISOAbbreviation(String value) {
        setISOAbbreviation(value);
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
