package org.mycore.iview2.frontend;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.servlets.MCRContentServlet;
import org.xml.sax.SAXException;

public class MCRIViewClientServlet extends MCRContentServlet {

    private static final int EXPIRE_METADATA_CACHE_TIME = 10; // in seconds

    private static final long serialVersionUID = 1L;

    private static final String JSON_CONFIG_ELEMENT_NAME = "jsonConfig";

    private static Document buildResponseDocument(MCRIViewClientConfiguration config) throws JDOMException,
        IOException, SAXException, JAXBException {
        String configJson = config.toJSON();
        Element startIviewClientElement = new Element("IViewConfig");
        Element configElement = new Element(JSON_CONFIG_ELEMENT_NAME);
        startIviewClientElement.addContent(configElement);
        startIviewClientElement.addContent(config.toXMLContent().asXML().getRootElement().detach());
        configElement.addContent(configJson);
        Document startIviewClientDocument = new org.jdom2.Document(startIviewClientElement);
        return startIviewClientDocument;
    }

    @Override
    public MCRContent getContent(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final MCRIViewClientConfiguration config = MCRConfiguration.instance().getInstanceOf(
            "MCR.Module-iview2.configuration", MCRIViewMetsClientConfiguration.class.getName());
        config.setup(req);

        config.lang = MCRSessionMgr.getCurrentSession().getCurrentLanguage();
        final MCRObjectID derivateID = MCRObjectID.getInstance(config.derivate);
        if (!MCRMetadataManager.exists(derivateID)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "derivate not found " + derivateID);
            return null;
        }
        final MCRObjectID objectID = MCRMetadataManager.getObjectId(derivateID, EXPIRE_METADATA_CACHE_TIME,
            TimeUnit.SECONDS);
        if (objectID == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "mycore object not found " + objectID);
            return null;
        }
        config.objId = objectID.toString();
        try {
            MCRJDOMContent source = new MCRJDOMContent(buildResponseDocument(config));
            return getLayoutService().getTransformedContent(req, resp, source);
        } catch (TransformerException | SAXException | JDOMException | JAXBException e) {
            throw new IOException(e);
        }
    }

}