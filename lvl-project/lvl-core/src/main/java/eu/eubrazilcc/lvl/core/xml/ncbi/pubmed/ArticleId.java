//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.08 at 06:22:46 PM CEST 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.pubmed;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "value"
})
@XmlRootElement(name = "ArticleId")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
public class ArticleId {

    @XmlAttribute(name = "IdType")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected String idType;
    @XmlValue
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected String value;

    /**
     * Gets the value of the idType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public String getIdType() {
        if (idType == null) {
            return "pubmed";
        } else {
            return idType;
        }
    }

    /**
     * Sets the value of the idType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public void setIdType(String value) {
        this.idType = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
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
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public void setvalue(String value) {
        this.value = value;
    }

}
