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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "year",
    "month",
    "day"
})
@XmlRootElement(name = "ArticleDate")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
public class ArticleDate {

    @XmlAttribute(name = "DateType")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected String dateType;
    @XmlElement(name = "Year", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected Year year;
    @XmlElement(name = "Month", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected Month month;
    @XmlElement(name = "Day", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected Day day;

    /**
     * Gets the value of the dateType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public String getDateType() {
        if (dateType == null) {
            return "Electronic";
        } else {
            return dateType;
        }
    }

    /**
     * Sets the value of the dateType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public void setDateType(String value) {
        this.dateType = value;
    }

    /**
     * Gets the value of the year property.
     * 
     * @return
     *     possible object is
     *     {@link Year }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public Year getYear() {
        return year;
    }

    /**
     * Sets the value of the year property.
     * 
     * @param value
     *     allowed object is
     *     {@link Year }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public void setYear(Year value) {
        this.year = value;
    }

    /**
     * Gets the value of the month property.
     * 
     * @return
     *     possible object is
     *     {@link Month }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public Month getMonth() {
        return month;
    }

    /**
     * Sets the value of the month property.
     * 
     * @param value
     *     allowed object is
     *     {@link Month }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public void setMonth(Month value) {
        this.month = value;
    }

    /**
     * Gets the value of the day property.
     * 
     * @return
     *     possible object is
     *     {@link Day }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public Day getDay() {
        return day;
    }

    /**
     * Sets the value of the day property.
     * 
     * @param value
     *     allowed object is
     *     {@link Day }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public void setDay(Day value) {
        this.day = value;
    }

}
