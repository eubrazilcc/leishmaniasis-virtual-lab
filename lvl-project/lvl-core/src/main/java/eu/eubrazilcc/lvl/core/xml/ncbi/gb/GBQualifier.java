//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.04.28 at 08:19:58 PM CEST 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.gb;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "gbQualifierName",
    "gbQualifierValue"
})
@XmlRootElement(name = "GBQualifier")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
public class GBQualifier {

    @XmlElement(name = "GBQualifier_name", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbQualifierName;
    @XmlElement(name = "GBQualifier_value")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbQualifierValue;

    /**
     * Gets the value of the gbQualifierName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBQualifierName() {
        return gbQualifierName;
    }

    /**
     * Sets the value of the gbQualifierName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBQualifierName(String value) {
        this.gbQualifierName = value;
    }

    /**
     * Gets the value of the gbQualifierValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBQualifierValue() {
        return gbQualifierValue;
    }

    /**
     * Sets the value of the gbQualifierValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBQualifierValue(String value) {
        this.gbQualifierValue = value;
    }

}