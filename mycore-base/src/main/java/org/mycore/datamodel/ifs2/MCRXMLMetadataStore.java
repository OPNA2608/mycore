/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mycore.datamodel.ifs2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRUsageException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRPathContent;
import org.mycore.datamodel.ifs2.MCRMetadataVersion.MCRMetadataVersionState;
import org.xml.sax.SAXException;

/**
 * An {@link MCRMetadataStore} extension that uses a local, unversioned storage implementation.
 * 
 * @author Frank Lützenkirchen
 * @author Christoph Neidahl (OPNA2608)
 */

public class MCRXMLMetadataStore extends MCRMetadataStore {

    protected static final Logger LOGGER = LogManager.getLogger();

    @Override
    protected void createContent(MCRMetadata metadata, MCRContent content) throws MCRPersistenceException {
        checkMetadata(metadata, false);
        if (forceXML) {
            content = checkContent(content);
        }

        try {
            Path path = getSlot(metadata.getID());
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            content.sendTo(path);
        } catch (IOException e) {
            throw new MCRPersistenceException("Failed to create " + metadata.getFullID() + "!", e);
        }
    }

    @Override
    protected MCRContent readContent(MCRMetadata metadata) throws MCRPersistenceException {
        checkMetadata(metadata, true);

        MCRPathContent pathContent = new MCRPathContent(getSlot(metadata.getID()));
        pathContent.setDocType(forceDocType);
        return pathContent;
    }

    @Override
    protected void updateContent(MCRMetadata metadata, MCRContent content) throws MCRPersistenceException {
        checkMetadata(metadata, true);
        if (forceXML) {
            content = checkContent(content);
        }

        try {
            Path path = getSlot(metadata.getID());
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            content.sendTo(path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new MCRPersistenceException("Failed to update " + metadata.getFullID() + "!", e);
        }
    }

    @Override
    protected void deleteContent(MCRMetadata metadata) throws MCRPersistenceException {
        checkMetadata(metadata, true);

        try {
            delete(getSlot(metadata.getID()));
        } catch (IOException e) {
            throw new MCRPersistenceException("Failed to delete " + metadata.getFullID() + "!", e);
        }
        if (this.existsXML(metadata)) {
            throw new MCRPersistenceException("Deleted object " + metadata.getFullID() + " still exists in store!");
        }
    }

    @Override
    public List<MCRMetadataVersion> getMetadataVersions(int id) throws MCRPersistenceException {
        checkMetadata(new MCRMetadata(this, id));

        if (exists(id)) {
            try {
                String user = MCRSessionMgr.getCurrentSession().getUserInformation().getUserID();
                Date date = getLastModified(id);
                return new ArrayList<>(Arrays.asList(new MCRMetadataVersion(new MCRMetadata(this, id), 1,
                    user, date, MCRMetadataVersionState.CREATED)));
            } catch (Exception e) {
                throw new MCRPersistenceException("Failed to enumerate metadata versions!", e);
            }
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void verify() throws MCRPersistenceException, MCRUsageException {
        throw new MCRUsageException("Store does not implement integrity verification!");
    }

    @Override
    public Date getLastModified(MCRMetadata metadata) throws MCRPersistenceException {
        checkMetadata(metadata, true);

        try {
            return Date.from(Files.getLastModifiedTime(getSlot(metadata.getID())).toInstant());
        } catch (IOException e) {
            throw new MCRPersistenceException("Failed to get last modification date!", e);
        }
    }

    @Override
    protected void setLastModified(MCRMetadata metadata, Date date) throws MCRUsageException {
        checkMetadata(metadata, true);

        try {
            Files.setLastModifiedTime(getSlot(metadata.getID()), FileTime.from(date.toInstant()));
        } catch (IOException e) {
            throw new MCRPersistenceException("Failed to set last modification date!", e);
        }
    }

    private boolean existsXML(MCRMetadata metadata) throws MCRPersistenceException {
        return Files.exists(getSlot(metadata.getID()));
    }

    @Override
    public boolean exists(MCRMetadata metadata) throws MCRPersistenceException {
        return existsXML(metadata);
    }

    protected void checkMetadata(MCRMetadata metadata) throws MCRPersistenceException {
        int id = metadata.getID();
        if (id <= 0) {
            throw new MCRPersistenceException("ID " + id + " < 1!");
        }
    }

    protected void checkMetadata(MCRMetadata metadata, boolean shouldExist) throws MCRPersistenceException {
        checkMetadata(metadata);
        boolean doesExist = existsXML(metadata);
        if (shouldExist && !doesExist) {
            throw new MCRPersistenceException("ID does not exist in store!");
        } else if (!shouldExist && doesExist) {
            throw new MCRPersistenceException("ID already exists in store!");
        }
    }

    protected MCRContent checkContent(MCRContent content) throws MCRPersistenceException {
        try {
            return content.ensureXML();
        } catch (SAXException | IOException | JDOMException e) {
            throw new MCRPersistenceException("Requested content failed to parse as XML!", e);
        }
    }
}
