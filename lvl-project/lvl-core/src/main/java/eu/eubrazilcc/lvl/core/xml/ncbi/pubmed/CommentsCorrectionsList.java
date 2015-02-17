//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.17 at 06:39:18 PM CET 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.pubmed;

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
    "commentsCorrections"
})
@XmlRootElement(name = "CommentsCorrectionsList")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
public class CommentsCorrectionsList {

    @XmlElement(name = "CommentsCorrections", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    protected List<CommentsCorrections> commentsCorrections;

    /**
     * Gets the value of the commentsCorrections property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the commentsCorrections property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCommentsCorrections().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CommentsCorrections }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public List<CommentsCorrections> getCommentsCorrections() {
        if (commentsCorrections == null) {
            commentsCorrections = new ArrayList<CommentsCorrections>();
        }
        return this.commentsCorrections;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public CommentsCorrectionsList withCommentsCorrections(CommentsCorrections... values) {
        if (values!= null) {
            for (CommentsCorrections value: values) {
                getCommentsCorrections().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public CommentsCorrectionsList withCommentsCorrections(Collection<CommentsCorrections> values) {
        if (values!= null) {
            getCommentsCorrections().addAll(values);
        }
        return this;
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public boolean equals(java.lang.Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-02-17T06:39:18+01:00", comments = "JAXB RI v2.2.11")
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
