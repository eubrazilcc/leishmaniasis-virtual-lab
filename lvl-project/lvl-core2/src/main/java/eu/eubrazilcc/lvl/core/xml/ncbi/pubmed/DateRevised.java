//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.20 at 09:54:44 AM CEST 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.pubmed;

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
    "year",
    "month",
    "day"
})
@XmlRootElement(name = "DateRevised")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
public class DateRevised {

    @XmlElement(name = "Year", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    protected Year year;
    @XmlElement(name = "Month", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    protected Month month;
    @XmlElement(name = "Day", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    protected Day day;

    /**
     * Gets the value of the year property.
     * 
     * @return
     *     possible object is
     *     {@link Year }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public void setDay(Day value) {
        this.day = value;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public DateRevised withYear(Year value) {
        setYear(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public DateRevised withMonth(Month value) {
        setMonth(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public DateRevised withDay(Day value) {
        setDay(value);
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public boolean equals(java.lang.Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
