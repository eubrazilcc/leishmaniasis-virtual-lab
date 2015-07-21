//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.07.20 at 09:54:44 AM CEST 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.pubmed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "journal",
    "articleTitle",
    "paginationOrELocationID",
    "_abstract",
    "affiliation",
    "authorList",
    "language",
    "dataBankList",
    "grantList",
    "publicationTypeList",
    "vernacularTitle",
    "articleDate"
})
@XmlRootElement(name = "Article")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
public class Article {

    @XmlAttribute(name = "PubModel", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    protected String pubModel;
    @XmlElement(name = "Journal", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    protected Journal journal;
    @XmlElement(name = "ArticleTitle", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    protected String articleTitle;
    @XmlElements({
        @XmlElement(name = "Pagination", required = true, type = Pagination.class),
        @XmlElement(name = "ELocationID", required = true, type = ELocationID.class)
    })
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    protected List<java.lang.Object> paginationOrELocationID;
    @XmlElement(name = "Abstract")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    protected Abstract _abstract;
    @XmlElement(name = "Affiliation")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    protected String affiliation;
    @XmlElement(name = "AuthorList")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    protected AuthorList authorList;
    @XmlElement(name = "Language", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    protected List<Language> language;
    @XmlElement(name = "DataBankList")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    protected DataBankList dataBankList;
    @XmlElement(name = "GrantList")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    protected GrantList grantList;
    @XmlElement(name = "PublicationTypeList", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    protected PublicationTypeList publicationTypeList;
    @XmlElement(name = "VernacularTitle")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    protected String vernacularTitle;
    @XmlElement(name = "ArticleDate")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    protected List<ArticleDate> articleDate;

    /**
     * Gets the value of the pubModel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public String getPubModel() {
        return pubModel;
    }

    /**
     * Sets the value of the pubModel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public void setPubModel(String value) {
        this.pubModel = value;
    }

    /**
     * Gets the value of the journal property.
     * 
     * @return
     *     possible object is
     *     {@link Journal }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public Journal getJournal() {
        return journal;
    }

    /**
     * Sets the value of the journal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Journal }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public void setJournal(Journal value) {
        this.journal = value;
    }

    /**
     * Gets the value of the articleTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public String getArticleTitle() {
        return articleTitle;
    }

    /**
     * Sets the value of the articleTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public void setArticleTitle(String value) {
        this.articleTitle = value;
    }

    /**
     * Gets the value of the paginationOrELocationID property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the paginationOrELocationID property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPaginationOrELocationID().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Pagination }
     * {@link ELocationID }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public List<java.lang.Object> getPaginationOrELocationID() {
        if (paginationOrELocationID == null) {
            paginationOrELocationID = new ArrayList<java.lang.Object>();
        }
        return this.paginationOrELocationID;
    }

    /**
     * Gets the value of the abstract property.
     * 
     * @return
     *     possible object is
     *     {@link Abstract }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public Abstract getAbstract() {
        return _abstract;
    }

    /**
     * Sets the value of the abstract property.
     * 
     * @param value
     *     allowed object is
     *     {@link Abstract }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public void setAbstract(Abstract value) {
        this._abstract = value;
    }

    /**
     * Gets the value of the affiliation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public String getAffiliation() {
        return affiliation;
    }

    /**
     * Sets the value of the affiliation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public void setAffiliation(String value) {
        this.affiliation = value;
    }

    /**
     * Gets the value of the authorList property.
     * 
     * @return
     *     possible object is
     *     {@link AuthorList }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public AuthorList getAuthorList() {
        return authorList;
    }

    /**
     * Sets the value of the authorList property.
     * 
     * @param value
     *     allowed object is
     *     {@link AuthorList }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public void setAuthorList(AuthorList value) {
        this.authorList = value;
    }

    /**
     * Gets the value of the language property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the language property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLanguage().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Language }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public List<Language> getLanguage() {
        if (language == null) {
            language = new ArrayList<Language>();
        }
        return this.language;
    }

    /**
     * Gets the value of the dataBankList property.
     * 
     * @return
     *     possible object is
     *     {@link DataBankList }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public DataBankList getDataBankList() {
        return dataBankList;
    }

    /**
     * Sets the value of the dataBankList property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataBankList }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public void setDataBankList(DataBankList value) {
        this.dataBankList = value;
    }

    /**
     * Gets the value of the grantList property.
     * 
     * @return
     *     possible object is
     *     {@link GrantList }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public GrantList getGrantList() {
        return grantList;
    }

    /**
     * Sets the value of the grantList property.
     * 
     * @param value
     *     allowed object is
     *     {@link GrantList }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public void setGrantList(GrantList value) {
        this.grantList = value;
    }

    /**
     * Gets the value of the publicationTypeList property.
     * 
     * @return
     *     possible object is
     *     {@link PublicationTypeList }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public PublicationTypeList getPublicationTypeList() {
        return publicationTypeList;
    }

    /**
     * Sets the value of the publicationTypeList property.
     * 
     * @param value
     *     allowed object is
     *     {@link PublicationTypeList }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public void setPublicationTypeList(PublicationTypeList value) {
        this.publicationTypeList = value;
    }

    /**
     * Gets the value of the vernacularTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public String getVernacularTitle() {
        return vernacularTitle;
    }

    /**
     * Sets the value of the vernacularTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public void setVernacularTitle(String value) {
        this.vernacularTitle = value;
    }

    /**
     * Gets the value of the articleDate property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the articleDate property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getArticleDate().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ArticleDate }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public List<ArticleDate> getArticleDate() {
        if (articleDate == null) {
            articleDate = new ArrayList<ArticleDate>();
        }
        return this.articleDate;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public Article withPubModel(String value) {
        setPubModel(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public Article withJournal(Journal value) {
        setJournal(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public Article withArticleTitle(String value) {
        setArticleTitle(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public Article withPaginationOrELocationID(java.lang.Object... values) {
        if (values!= null) {
            for (java.lang.Object value: values) {
                getPaginationOrELocationID().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public Article withPaginationOrELocationID(Collection<java.lang.Object> values) {
        if (values!= null) {
            getPaginationOrELocationID().addAll(values);
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public Article withAbstract(Abstract value) {
        setAbstract(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public Article withAffiliation(String value) {
        setAffiliation(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public Article withAuthorList(AuthorList value) {
        setAuthorList(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public Article withLanguage(Language... values) {
        if (values!= null) {
            for (Language value: values) {
                getLanguage().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public Article withLanguage(Collection<Language> values) {
        if (values!= null) {
            getLanguage().addAll(values);
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public Article withDataBankList(DataBankList value) {
        setDataBankList(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public Article withGrantList(GrantList value) {
        setGrantList(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public Article withPublicationTypeList(PublicationTypeList value) {
        setPublicationTypeList(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public Article withVernacularTitle(String value) {
        setVernacularTitle(value);
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public Article withArticleDate(ArticleDate... values) {
        if (values!= null) {
            for (ArticleDate value: values) {
                getArticleDate().add(value);
            }
        }
        return this;
    }

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2015-07-20T09:54:44+02:00", comments = "JAXB RI v2.2.11")
    public Article withArticleDate(Collection<ArticleDate> values) {
        if (values!= null) {
            getArticleDate().addAll(values);
        }
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
