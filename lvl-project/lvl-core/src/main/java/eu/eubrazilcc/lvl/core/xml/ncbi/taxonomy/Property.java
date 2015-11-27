//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.11.27 at 05:37:45 PM CET 
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
    "propName",
    "propValueIntOrPropValueBoolOrPropValueString"
})
@XmlRootElement(name = "Property")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
public class Property {

    @XmlElement(name = "PropName", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    protected String propName;
    @XmlElements({
        @XmlElement(name = "PropValueInt", required = true, type = PropValueInt.class),
        @XmlElement(name = "PropValueBool", required = true, type = PropValueBool.class),
        @XmlElement(name = "PropValueString", required = true, type = PropValueString.class)
    })
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    protected List<Object> propValueIntOrPropValueBoolOrPropValueString;

    /**
     * Gets the value of the propName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    public String getPropName() {
        return propName;
    }

    /**
     * Sets the value of the propName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    public void setPropName(String value) {
        this.propName = value;
    }

    /**
     * Gets the value of the propValueIntOrPropValueBoolOrPropValueString property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the propValueIntOrPropValueBoolOrPropValueString property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPropValueIntOrPropValueBoolOrPropValueString().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PropValueInt }
     * {@link PropValueBool }
     * {@link PropValueString }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    public List<Object> getPropValueIntOrPropValueBoolOrPropValueString() {
        if (propValueIntOrPropValueBoolOrPropValueString == null) {
            propValueIntOrPropValueBoolOrPropValueString = new ArrayList<Object>();
        }
        return this.propValueIntOrPropValueBoolOrPropValueString;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    public Property withPropName(String value) {
        setPropName(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    public Property withPropValueIntOrPropValueBoolOrPropValueString(Object... values) {
        if (values!= null) {
            for (Object value: values) {
                getPropValueIntOrPropValueBoolOrPropValueString().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    public Property withPropValueIntOrPropValueBoolOrPropValueString(Collection<Object> values) {
        if (values!= null) {
            getPropValueIntOrPropValueBoolOrPropValueString().addAll(values);
        }
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-11-27T05:37:45+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
