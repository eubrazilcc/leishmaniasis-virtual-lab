//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.05.02 at 01:47:04 PM CEST 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.esearch;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "value"
})
@XmlRootElement(name = "FieldNotFound")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-05-02T01:47:04+02:00", comments = "JAXB RI v2.2.4-2")
public class FieldNotFound {

    @XmlValue
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-05-02T01:47:04+02:00", comments = "JAXB RI v2.2.4-2")
    protected String value;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-05-02T01:47:04+02:00", comments = "JAXB RI v2.2.4-2")
    public String getvalue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-05-02T01:47:04+02:00", comments = "JAXB RI v2.2.4-2")
    public void setvalue(String value) {
        this.value = value;
    }

}
