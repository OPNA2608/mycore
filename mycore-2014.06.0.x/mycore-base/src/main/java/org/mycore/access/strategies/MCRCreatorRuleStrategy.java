/*
 *
 * $Revision$ $Date$
 *
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */

package org.mycore.access.strategies;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRUserInformation;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.classifications2.MCRCategLinkReference;
import org.mycore.datamodel.classifications2.MCRCategLinkService;
import org.mycore.datamodel.classifications2.MCRCategLinkServiceFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.ifs2.MCRMetadataVersion;
import org.mycore.datamodel.metadata.MCRObjectID;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 *
 * First a check is done if user is in role "submitter", the given object is in
 * status "submitted" and current user is creator. If not
 * it will be tried to check the permission against the rule ID
 * <code>default_&lt;ObjectType&gt;</code> if it exists. If not the last
 * fallback is done against <code>default</code>.
 *
 * Specify classification and category for status "submitted":
 * MCR.Access.Strategy.SubmittedCategory=status:submitted
 *
 * @author Thomas Scheffler (yagee)
 * @author Kathleen Neumann (mcrkrebs)
 *
 * @version $Revision$ $Date$
 */
public class MCRCreatorRuleStrategy implements MCRAccessCheckStrategy {
    private static final Logger LOGGER = Logger.getLogger(MCRCreatorRuleStrategy.class);

    private static final MCRCategoryID SUBMITTED_CATEGORY = MCRCategoryID.fromString(MCRConfiguration.instance()
        .getString("MCR.Access.Strategy.SubmittedCategory", "status:submitted"));

    private static final String CREATOR_ROLE = MCRConfiguration.instance().getString("MCR.Access.Strategy.CreatorRole",
        "submitter");

    private static final MCRCategLinkService LINK_SERVICE = MCRCategLinkServiceFactory.getInstance();

    private static final MCRObjectTypeStrategy BASE_STRATEGY = new MCRObjectTypeStrategy();

    private static LoadingCache<MCRObjectID, String> CREATOR_CACHE = CacheBuilder.newBuilder().weakKeys()
        .maximumSize(5000).build(new CacheLoader<MCRObjectID, String>() {

            @Override
            public String load(MCRObjectID mcrObjectID) throws Exception {
                MCRXMLMetadataManager metadataManager = MCRXMLMetadataManager.instance();
                List<MCRMetadataVersion> versions = metadataManager.listRevisions(mcrObjectID);
                if (versions != null && !versions.isEmpty()) {
                    Collections.reverse(versions); //newest revision first
                    for (MCRMetadataVersion version : versions) {
                        //time machine: go back in history
                        if (version.getType() == MCRMetadataVersion.CREATED) {
                            LOGGER.info("Found creator " + version.getUser() + " in revision " + version.getRevision()
                                + " of " + mcrObjectID);
                            return version.getUser();
                        }
                    }
                }
                LOGGER.info("Could not get creator information.");
                return null;
            }
        });

    /*
     * (non-Javadoc)
     *
     * @see org.mycore.access.strategies.MCRAccessCheckStrategy#checkPermission(java.lang.String,
     *      java.lang.String)
     */
    public boolean checkPermission(String id, String permission) {
        LOGGER.debug("check permission " + permission + " for MCRBaseID " + id);
        if (id == null || id.length() == 0 || permission == null || permission.length() == 0) {
            return false;
        }
        if (!MCRAccessManager.PERMISSION_WRITE.equals(permission)) {
            //if not checking for write permission, use base strategy
            return BASE_STRATEGY.checkPermission(id, permission);
        }
        //our decoration for write permission
        if (BASE_STRATEGY.checkPermission(id, permission)) {
            return true;
        }
        MCRObjectID mcrObjectId = null;
        try {
            mcrObjectId = MCRObjectID.getInstance(id);
            MCRUserInformation currentUser = MCRSessionMgr.getCurrentSession().getUserInformation();
            if (currentUser.isUserInRole(CREATOR_ROLE) && objectStatusIsSubmitted(mcrObjectId)) {
                if (isCurrentUserCreator(mcrObjectId, currentUser)) {
                    return true;
                }
            }
        } catch (RuntimeException e) {
            if (mcrObjectId == null) {
                LOGGER.debug("id is not a valid object ID", e);
            } else {
                LOGGER.warn("Eror while checking permission.", e);
            }
        }
        return false;
    }

    private static boolean objectStatusIsSubmitted(MCRObjectID mcrObjectID) {
        MCRCategLinkReference reference = new MCRCategLinkReference(mcrObjectID);
        return LINK_SERVICE.isInCategory(reference, SUBMITTED_CATEGORY);
    }

    private static boolean isCurrentUserCreator(MCRObjectID mcrObjectID, MCRUserInformation currentUser) {
        try {
            String creator = CREATOR_CACHE.get(mcrObjectID);
            return currentUser.getUserID().equals(creator);
        } catch (ExecutionException e) {
            LOGGER.error("Error while getting creator information.", e);
            return false;
        }
    }
}
