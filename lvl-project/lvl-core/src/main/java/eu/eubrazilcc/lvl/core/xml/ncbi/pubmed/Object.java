//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.24 at 09:06:22 AM CET 
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
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
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
    "param"
})
@XmlRootElement(name = "Object")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
public class Object {

    @XmlAttribute(name = "Type", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    protected String type;
    @XmlElement(name = "Param")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    protected List<Param> param;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the param property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the param property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParam().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Param }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public List<Param> getParam() {
        if (param == null) {
            param = new ArrayList<Param>();
        }
        return this.param;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public Object withType(String value) {
        setType(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public Object withParam(Param... values) {
        if (values!= null) {
            for (Param value: values) {
                getParam().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public Object withParam(Collection<Param> values) {
        if (values!= null) {
            getParam().addAll(values);
        }
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(java.lang.Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-24T09:06:22+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
