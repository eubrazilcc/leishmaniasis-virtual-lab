//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.24 at 06:08:24 PM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.pubmed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
public class History {

    @XmlElement(name = "PubMedPubDate", required = true)
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
    public List<PubMedPubDate> getPubMedPubDate() {
        if (pubMedPubDate == null) {
            pubMedPubDate = new ArrayList<PubMedPubDate>();
        }
        return this.pubMedPubDate;
    }

    public History withPubMedPubDate(PubMedPubDate... values) {
        if (values!= null) {
            for (PubMedPubDate value: values) {
                getPubMedPubDate().add(value);
            }
        }
        return this;
    }

    public History withPubMedPubDate(Collection<PubMedPubDate> values) {
        if (values!= null) {
            getPubMedPubDate().addAll(values);
        }
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
