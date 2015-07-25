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
    "keyword"
})
@XmlRootElement(name = "KeywordList")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
public class KeywordList {

    @XmlAttribute(name = "Owner")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    protected String owner;
    @XmlElement(name = "Keyword", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    protected List<Keyword> keyword;

    /**
     * Gets the value of the owner property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public String getOwner() {
        if (owner == null) {
            return "NLM";
        } else {
            return owner;
        }
    }

    /**
     * Sets the value of the owner property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public void setOwner(String value) {
        this.owner = value;
    }

    /**
     * Gets the value of the keyword property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the keyword property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getKeyword().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Keyword }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public List<Keyword> getKeyword() {
        if (keyword == null) {
            keyword = new ArrayList<Keyword>();
        }
        return this.keyword;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public KeywordList withOwner(String value) {
        setOwner(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public KeywordList withKeyword(Keyword... values) {
        if (values!= null) {
            for (Keyword value: values) {
                getKeyword().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-23T01:04:58+02:00", comments = "JAXB RI v2.2.11")
    public KeywordList withKeyword(Collection<Keyword> values) {
        if (values!= null) {
            getKeyword().addAll(values);
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
