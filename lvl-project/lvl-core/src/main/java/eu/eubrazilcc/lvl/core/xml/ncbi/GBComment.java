//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.04.28 at 08:19:58 PM CEST 
//


package eu.eubrazilcc.lvl.core.xml.ncbi;

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
    "gbCommentType",
    "gbCommentParagraphs"
})
@XmlRootElement(name = "GBComment")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
public class GBComment {

    @XmlElement(name = "GBComment_type")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected String gbCommentType;
    @XmlElement(name = "GBComment_paragraphs", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    protected GBCommentParagraphs gbCommentParagraphs;

    /**
     * Gets the value of the gbCommentType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public String getGBCommentType() {
        return gbCommentType;
    }

    /**
     * Sets the value of the gbCommentType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBCommentType(String value) {
        this.gbCommentType = value;
    }

    /**
     * Gets the value of the gbCommentParagraphs property.
     * 
     * @return
     *     possible object is
     *     {@link GBCommentParagraphs }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public GBCommentParagraphs getGBCommentParagraphs() {
        return gbCommentParagraphs;
    }

    /**
     * Sets the value of the gbCommentParagraphs property.
     * 
     * @param value
     *     allowed object is
     *     {@link GBCommentParagraphs }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-04-28T08:19:58+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGBCommentParagraphs(GBCommentParagraphs value) {
        this.gbCommentParagraphs = value;
    }

}
