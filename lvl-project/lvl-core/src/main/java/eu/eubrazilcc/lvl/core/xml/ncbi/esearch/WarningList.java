//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.10 at 02:13:39 PM CEST 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.esearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    "phraseIgnored",
    "quotedPhraseNotFound",
    "outputMessage"
})
@XmlRootElement(name = "WarningList")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
public class WarningList {

    @XmlElement(name = "PhraseIgnored")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    protected List<PhraseIgnored> phraseIgnored;
    @XmlElement(name = "QuotedPhraseNotFound")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    protected List<QuotedPhraseNotFound> quotedPhraseNotFound;
    @XmlElement(name = "OutputMessage")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    protected List<OutputMessage> outputMessage;

    /**
     * Gets the value of the phraseIgnored property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the phraseIgnored property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPhraseIgnored().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PhraseIgnored }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public List<PhraseIgnored> getPhraseIgnored() {
        if (phraseIgnored == null) {
            phraseIgnored = new ArrayList<PhraseIgnored>();
        }
        return this.phraseIgnored;
    }

    /**
     * Gets the value of the quotedPhraseNotFound property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the quotedPhraseNotFound property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getQuotedPhraseNotFound().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QuotedPhraseNotFound }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public List<QuotedPhraseNotFound> getQuotedPhraseNotFound() {
        if (quotedPhraseNotFound == null) {
            quotedPhraseNotFound = new ArrayList<QuotedPhraseNotFound>();
        }
        return this.quotedPhraseNotFound;
    }

    /**
     * Gets the value of the outputMessage property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the outputMessage property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOutputMessage().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OutputMessage }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public List<OutputMessage> getOutputMessage() {
        if (outputMessage == null) {
            outputMessage = new ArrayList<OutputMessage>();
        }
        return this.outputMessage;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public WarningList withPhraseIgnored(PhraseIgnored... values) {
        if (values!= null) {
            for (PhraseIgnored value: values) {
                getPhraseIgnored().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public WarningList withPhraseIgnored(Collection<PhraseIgnored> values) {
        if (values!= null) {
            getPhraseIgnored().addAll(values);
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public WarningList withQuotedPhraseNotFound(QuotedPhraseNotFound... values) {
        if (values!= null) {
            for (QuotedPhraseNotFound value: values) {
                getQuotedPhraseNotFound().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public WarningList withQuotedPhraseNotFound(Collection<QuotedPhraseNotFound> values) {
        if (values!= null) {
            getQuotedPhraseNotFound().addAll(values);
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public WarningList withOutputMessage(OutputMessage... values) {
        if (values!= null) {
            for (OutputMessage value: values) {
                getOutputMessage().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public WarningList withOutputMessage(Collection<OutputMessage> values) {
        if (values!= null) {
            getOutputMessage().addAll(values);
        }
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public boolean equals(Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-10T02:13:39+02:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
