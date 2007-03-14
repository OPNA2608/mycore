package org.mycore.datamodel.classifications;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.classifications.query.Classification;
import org.mycore.datamodel.classifications.query.ClassificationTransformer;
import org.mycore.datamodel.classifications.query.MCRClassificationQuery;

/**
 * @author Radi Radichev
 *
 */

public class MCRClassificationPool {
    
    private HashMap<String, Classification> classifications=new HashMap<String, Classification>(); // A Hash map to store all edited classifications
    static Logger LOGGER=Logger.getLogger(MCRClassificationPool.class);
    MCRClassification cl=new MCRClassification();
    
    public MCRClassificationPool() {
        MCRSessionMgr.getCurrentSession().put("classifications",classifications); //Put the hash map in the current session
    }
    
    /**
     * This methode returns all ClassificationIDs. From the database and pool together.
     * @return
     */
    public Set<String> getAllIDs() {
        String[] dbIDs=MCRClassification.getAllClassificationID();
        Set<String> ids=new HashSet<String>();
        for (String id:dbIDs){
            ids.add(id);
        }
        ids.addAll(classifications.keySet());
        return ids;
    }
    /**
     * Save all changes to the database.
     * @return
     */
    public boolean saveAll() {
        Iterator iter=classifications.keySet().iterator();
              
        while(iter.hasNext()) {
            Classification clas=(Classification)classifications.get(iter.next().toString());
            Document doc=ClassificationTransformer.getMetaDataDocument(clas);
            try {
                cl.updateFromJDOM(doc);
                iter.remove();
            } catch (Exception e) {
                e.printStackTrace();
                return false; 
            }
        }
        return true;
    }
    
    /**
     * Cancel all changes. Clear the hashtable.
     * @return
     */
    public boolean purgeAll() {
        
        LOGGER.debug("Purging all in progress...");
        synchronized (classifications) {
            classifications.clear();
        }
        return true;
    }
    /**
     * Check if the Classification with classID is already edited and in the session.
     * @param classID Classification ID to check for
     * @return <code>true</code> when Classification is edited, <code>false</code> when not.
     */
    public boolean isEdited(String classID) {
       return classifications.containsKey(classID);
    }
    
    /**
     * Put the classification in the Session. This method is executed every time a Classification
     * or a Category is edited
     * @param cl Classification to be stored in the session.
     */
    public void updateClassification(Classification cl) { 
        classifications.put(cl.getId(),cl);
        LOGGER.debug("Classification: "+cl.getId()+" wurde in den Session hinzugefügt!");
        
    }
    
    /**
     * This method checks to see if the classification which is expected is in the Session (when edited) or 
     * it takes the classification from the database
     * @param clid
     * @return Document classif
     */
    
    public Classification getClassificationAsPojo(String clid) {
        Classification classif=(Classification)classifications.get(clid);
        
        if(classif!=null) {  
            LOGGER.info("Classification with ID: "+classif.getId()+" comes from the session.");
            return classif;
        } else {
            Classification cl=MCRClassificationQuery.getClassification(clid,-1,true);
            LOGGER.info("Classification with ID: "+cl.getId()+" comes from the database.");
            return cl;
        }
    }
  
  

}
