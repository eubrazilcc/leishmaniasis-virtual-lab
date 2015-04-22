//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.04.22 at 01:25:24 PM CEST 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.pubmed;

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
    "yearOrMonthOrDayOrSeasonOrMedlineDate"
})
@XmlRootElement(name = "PubDate")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-04-22T01:25:23+02:00", comments = "JAXB RI v2.2.11")
public class PubDate {

    @XmlElements({
        @XmlElement(name = "Year", required = true, type = Year.class),
        @XmlElement(name = "Month", required = true, type = Month.class),
        @XmlElement(name = "Day", required = true, type = Day.class),
        @XmlElement(name = "Season", required = true, type = Season.class),
        @XmlElement(name = "MedlineDate", required = true, type = MedlineDate.class)
    })
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-04-22T01:25:23+02:00", comments = "JAXB RI v2.2.11")
    protected List<java.lang.Object> yearOrMonthOrDayOrSeasonOrMedlineDate;

    /**
     * Gets the value of the yearOrMonthOrDayOrSeasonOrMedlineDate property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the yearOrMonthOrDayOrSeasonOrMedlineDate property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getYearOrMonthOrDayOrSeasonOrMedlineDate().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Year }
     * {@link Month }
     * {@link Day }
     * {@link Season }
     * {@link MedlineDate }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-04-22T01:25:23+02:00", comments = "JAXB RI v2.2.11")
    public List<java.lang.Object> getYearOrMonthOrDayOrSeasonOrMedlineDate() {
        if (yearOrMonthOrDayOrSeasonOrMedlineDate == null) {
            yearOrMonthOrDayOrSeasonOrMedlineDate = new ArrayList<java.lang.Object>();
        }
        return this.yearOrMonthOrDayOrSeasonOrMedlineDate;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-04-22T01:25:23+02:00", comments = "JAXB RI v2.2.11")
    public PubDate withYearOrMonthOrDayOrSeasonOrMedlineDate(java.lang.Object... values) {
        if (values!= null) {
            for (java.lang.Object value: values) {
                getYearOrMonthOrDayOrSeasonOrMedlineDate().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-04-22T01:25:23+02:00", comments = "JAXB RI v2.2.11")
    public PubDate withYearOrMonthOrDayOrSeasonOrMedlineDate(Collection<java.lang.Object> values) {
        if (values!= null) {
            getYearOrMonthOrDayOrSeasonOrMedlineDate().addAll(values);
        }
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-04-22T01:25:23+02:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-04-22T01:25:23+02:00", comments = "JAXB RI v2.2.11")
    public boolean equals(java.lang.Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-04-22T01:25:23+02:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
