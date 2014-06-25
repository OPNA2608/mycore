//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.06.18 at 04:23:29 PM CEST 
//


package org.mycore.solr.index.document.jaxb;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.mycore.solr.index.document.jaxb package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Add_QNAME = new QName("", "add");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.mycore.solr.index.document.jaxb
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MCRSolrInputDocumentList }
     * 
     */
    public MCRSolrInputDocumentList createMCRSolrInputDocumentList() {
        return new MCRSolrInputDocumentList();
    }

    /**
     * Create an instance of {@link MCRSolrInputDocument }
     * 
     */
    public MCRSolrInputDocument createMCRSolrInputDocument() {
        return new MCRSolrInputDocument();
    }

    /**
     * Create an instance of {@link MCRSolrInputField }
     * 
     */
    public MCRSolrInputField createMCRSolrInputField() {
        return new MCRSolrInputField();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MCRSolrInputDocumentList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "add")
    public JAXBElement<MCRSolrInputDocumentList> createAdd(MCRSolrInputDocumentList value) {
        return new JAXBElement<MCRSolrInputDocumentList>(_Add_QNAME, MCRSolrInputDocumentList.class, null, value);
    }

}
