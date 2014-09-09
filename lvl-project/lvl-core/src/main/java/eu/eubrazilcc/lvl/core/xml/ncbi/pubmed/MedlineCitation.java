//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.09.08 at 06:22:46 PM CEST 
//


package eu.eubrazilcc.lvl.core.xml.ncbi.pubmed;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "pmid",
    "dateCreated",
    "dateCompleted",
    "dateRevised",
    "article",
    "medlineJournalInfo",
    "chemicalList",
    "citationSubset",
    "commentsCorrectionsList",
    "geneSymbolList",
    "meshHeadingList",
    "numberOfReferences",
    "personalNameSubjectList",
    "otherID",
    "otherAbstract",
    "keywordList",
    "spaceFlightMission",
    "investigatorList",
    "generalNote"
})
@XmlRootElement(name = "MedlineCitation")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
public class MedlineCitation {

    @XmlAttribute(name = "Owner")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected String owner;
    @XmlAttribute(name = "Status", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected String status;
    @XmlElement(name = "PMID", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected PMID pmid;
    @XmlElement(name = "DateCreated", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected DateCreated dateCreated;
    @XmlElement(name = "DateCompleted")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected DateCompleted dateCompleted;
    @XmlElement(name = "DateRevised")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected DateRevised dateRevised;
    @XmlElement(name = "Article", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected Article article;
    @XmlElement(name = "MedlineJournalInfo", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected MedlineJournalInfo medlineJournalInfo;
    @XmlElement(name = "ChemicalList")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected ChemicalList chemicalList;
    @XmlElement(name = "CitationSubset")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected List<CitationSubset> citationSubset;
    @XmlElement(name = "CommentsCorrectionsList")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected CommentsCorrectionsList commentsCorrectionsList;
    @XmlElement(name = "GeneSymbolList")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected GeneSymbolList geneSymbolList;
    @XmlElement(name = "MeshHeadingList")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected MeshHeadingList meshHeadingList;
    @XmlElement(name = "NumberOfReferences")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected String numberOfReferences;
    @XmlElement(name = "PersonalNameSubjectList")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected PersonalNameSubjectList personalNameSubjectList;
    @XmlElement(name = "OtherID")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected List<OtherID> otherID;
    @XmlElement(name = "OtherAbstract")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected List<OtherAbstract> otherAbstract;
    @XmlElement(name = "KeywordList")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected List<KeywordList> keywordList;
    @XmlElement(name = "SpaceFlightMission")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected List<SpaceFlightMission> spaceFlightMission;
    @XmlElement(name = "InvestigatorList")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected InvestigatorList investigatorList;
    @XmlElement(name = "GeneralNote")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    protected List<GeneralNote> generalNote;

    /**
     * Gets the value of the owner property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public String getOwner() {
        if (owner == null) {
            return "NLM";
        } else {
            return owner;
        }
    }

    /**
     * Sets the value of the owner property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public void setOwner(String value) {
        this.owner = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public String getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public void setStatus(String value) {
        this.status = value;
    }

    /**
     * Gets the value of the pmid property.
     * 
     * @return
     *     possible object is
     *     {@link PMID }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public PMID getPMID() {
        return pmid;
    }

    /**
     * Sets the value of the pmid property.
     * 
     * @param value
     *     allowed object is
     *     {@link PMID }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public void setPMID(PMID value) {
        this.pmid = value;
    }

    /**
     * Gets the value of the dateCreated property.
     * 
     * @return
     *     possible object is
     *     {@link DateCreated }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public DateCreated getDateCreated() {
        return dateCreated;
    }

    /**
     * Sets the value of the dateCreated property.
     * 
     * @param value
     *     allowed object is
     *     {@link DateCreated }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public void setDateCreated(DateCreated value) {
        this.dateCreated = value;
    }

    /**
     * Gets the value of the dateCompleted property.
     * 
     * @return
     *     possible object is
     *     {@link DateCompleted }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public DateCompleted getDateCompleted() {
        return dateCompleted;
    }

    /**
     * Sets the value of the dateCompleted property.
     * 
     * @param value
     *     allowed object is
     *     {@link DateCompleted }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public void setDateCompleted(DateCompleted value) {
        this.dateCompleted = value;
    }

    /**
     * Gets the value of the dateRevised property.
     * 
     * @return
     *     possible object is
     *     {@link DateRevised }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public DateRevised getDateRevised() {
        return dateRevised;
    }

    /**
     * Sets the value of the dateRevised property.
     * 
     * @param value
     *     allowed object is
     *     {@link DateRevised }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public void setDateRevised(DateRevised value) {
        this.dateRevised = value;
    }

    /**
     * Gets the value of the article property.
     * 
     * @return
     *     possible object is
     *     {@link Article }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public Article getArticle() {
        return article;
    }

    /**
     * Sets the value of the article property.
     * 
     * @param value
     *     allowed object is
     *     {@link Article }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public void setArticle(Article value) {
        this.article = value;
    }

    /**
     * Gets the value of the medlineJournalInfo property.
     * 
     * @return
     *     possible object is
     *     {@link MedlineJournalInfo }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public MedlineJournalInfo getMedlineJournalInfo() {
        return medlineJournalInfo;
    }

    /**
     * Sets the value of the medlineJournalInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link MedlineJournalInfo }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public void setMedlineJournalInfo(MedlineJournalInfo value) {
        this.medlineJournalInfo = value;
    }

    /**
     * Gets the value of the chemicalList property.
     * 
     * @return
     *     possible object is
     *     {@link ChemicalList }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public ChemicalList getChemicalList() {
        return chemicalList;
    }

    /**
     * Sets the value of the chemicalList property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChemicalList }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public void setChemicalList(ChemicalList value) {
        this.chemicalList = value;
    }

    /**
     * Gets the value of the citationSubset property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the citationSubset property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCitationSubset().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CitationSubset }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public List<CitationSubset> getCitationSubset() {
        if (citationSubset == null) {
            citationSubset = new ArrayList<CitationSubset>();
        }
        return this.citationSubset;
    }

    /**
     * Gets the value of the commentsCorrectionsList property.
     * 
     * @return
     *     possible object is
     *     {@link CommentsCorrectionsList }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public CommentsCorrectionsList getCommentsCorrectionsList() {
        return commentsCorrectionsList;
    }

    /**
     * Sets the value of the commentsCorrectionsList property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommentsCorrectionsList }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public void setCommentsCorrectionsList(CommentsCorrectionsList value) {
        this.commentsCorrectionsList = value;
    }

    /**
     * Gets the value of the geneSymbolList property.
     * 
     * @return
     *     possible object is
     *     {@link GeneSymbolList }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public GeneSymbolList getGeneSymbolList() {
        return geneSymbolList;
    }

    /**
     * Sets the value of the geneSymbolList property.
     * 
     * @param value
     *     allowed object is
     *     {@link GeneSymbolList }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public void setGeneSymbolList(GeneSymbolList value) {
        this.geneSymbolList = value;
    }

    /**
     * Gets the value of the meshHeadingList property.
     * 
     * @return
     *     possible object is
     *     {@link MeshHeadingList }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public MeshHeadingList getMeshHeadingList() {
        return meshHeadingList;
    }

    /**
     * Sets the value of the meshHeadingList property.
     * 
     * @param value
     *     allowed object is
     *     {@link MeshHeadingList }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public void setMeshHeadingList(MeshHeadingList value) {
        this.meshHeadingList = value;
    }

    /**
     * Gets the value of the numberOfReferences property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public String getNumberOfReferences() {
        return numberOfReferences;
    }

    /**
     * Sets the value of the numberOfReferences property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public void setNumberOfReferences(String value) {
        this.numberOfReferences = value;
    }

    /**
     * Gets the value of the personalNameSubjectList property.
     * 
     * @return
     *     possible object is
     *     {@link PersonalNameSubjectList }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public PersonalNameSubjectList getPersonalNameSubjectList() {
        return personalNameSubjectList;
    }

    /**
     * Sets the value of the personalNameSubjectList property.
     * 
     * @param value
     *     allowed object is
     *     {@link PersonalNameSubjectList }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public void setPersonalNameSubjectList(PersonalNameSubjectList value) {
        this.personalNameSubjectList = value;
    }

    /**
     * Gets the value of the otherID property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the otherID property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOtherID().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OtherID }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public List<OtherID> getOtherID() {
        if (otherID == null) {
            otherID = new ArrayList<OtherID>();
        }
        return this.otherID;
    }

    /**
     * Gets the value of the otherAbstract property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the otherAbstract property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOtherAbstract().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OtherAbstract }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public List<OtherAbstract> getOtherAbstract() {
        if (otherAbstract == null) {
            otherAbstract = new ArrayList<OtherAbstract>();
        }
        return this.otherAbstract;
    }

    /**
     * Gets the value of the keywordList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the keywordList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getKeywordList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link KeywordList }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public List<KeywordList> getKeywordList() {
        if (keywordList == null) {
            keywordList = new ArrayList<KeywordList>();
        }
        return this.keywordList;
    }

    /**
     * Gets the value of the spaceFlightMission property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the spaceFlightMission property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSpaceFlightMission().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SpaceFlightMission }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public List<SpaceFlightMission> getSpaceFlightMission() {
        if (spaceFlightMission == null) {
            spaceFlightMission = new ArrayList<SpaceFlightMission>();
        }
        return this.spaceFlightMission;
    }

    /**
     * Gets the value of the investigatorList property.
     * 
     * @return
     *     possible object is
     *     {@link InvestigatorList }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public InvestigatorList getInvestigatorList() {
        return investigatorList;
    }

    /**
     * Sets the value of the investigatorList property.
     * 
     * @param value
     *     allowed object is
     *     {@link InvestigatorList }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public void setInvestigatorList(InvestigatorList value) {
        this.investigatorList = value;
    }

    /**
     * Gets the value of the generalNote property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the generalNote property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGeneralNote().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GeneralNote }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2014-09-08T06:22:46+02:00", comments = "JAXB RI v2.2.4-2")
    public List<GeneralNote> getGeneralNote() {
        if (generalNote == null) {
            generalNote = new ArrayList<GeneralNote>();
        }
        return this.generalNote;
    }

}
