//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.12 at 09:34:25 AM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.pubmed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    "pubMedPubDate"
})
@XmlRootElement(name = "History")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-12T09:34:25+01:00", comments = "JAXB RI v2.2.11")
public class History {

    @XmlElement(name = "PubMedPubDate", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-12T09:34:25+01:00", comments = "JAXB RI v2.2.11")
    protected List<PubMedPubDate> pubMedPubDate;

    /**
     * Gets the value of the pubMedPubDate property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pubMedPubDate property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPubMedPubDate().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PubMedPubDate }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-12T09:34:25+01:00", comments = "JAXB RI v2.2.11")
    public List<PubMedPubDate> getPubMedPubDate() {
        if (pubMedPubDate == null) {
            pubMedPubDate = new ArrayList<PubMedPubDate>();
        }
        return this.pubMedPubDate;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-12T09:34:25+01:00", comments = "JAXB RI v2.2.11")
    public History withPubMedPubDate(PubMedPubDate... values) {
        if (values!= null) {
            for (PubMedPubDate value: values) {
                getPubMedPubDate().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-12T09:34:25+01:00", comments = "JAXB RI v2.2.11")
    public History withPubMedPubDate(Collection<PubMedPubDate> values) {
        if (values!= null) {
            getPubMedPubDate().addAll(values);
        }
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-12T09:34:25+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-12T09:34:25+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(java.lang.Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-03-12T09:34:25+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
