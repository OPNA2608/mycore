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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRUsageException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.datamodel.ifs2.MCRMetadataVersion.MCRMetadataVersionState;

/**
 * JUnit test for MCRVersioningMetadataStore
 *
 * @author Frank Lützenkirchen
 */
public class MCRVersioningMetadataStoreTest extends MCRIFS2VersioningTestCase {

    @Test
    public void createDocument() throws Exception {
        System.out.println("createDocument");
        Document testXmlDoc = new Document(new Element("root"));
        MCRContent testContent = new MCRJDOMContent(testXmlDoc);

        System.out.println("Highest: " + getVersStore().getHighestStoredID());
        int nextID = getVersStore().getNextFreeID();
        System.out.println("Next: " + nextID);
        System.out.println("Exists already: " + getVersStore().exists(nextID));
        assertFalse(getVersStore().exists(nextID));

        MCRMetadata versionedMetadata = getVersStore().create(nextID, testContent);
        assertTrue(getVersStore().exists(nextID));
        assertNotNull(versionedMetadata);

        MCRContent contentFromStore = getVersStore().retrieve(versionedMetadata.getID()).read();
        String contentStrFromStore = contentFromStore.asString();

        MCRContent mcrContent = new MCRJDOMContent(testXmlDoc);
        String expectedContentStr = mcrContent.asString();

        assertEquals(expectedContentStr, contentStrFromStore);

        assertTrue(versionedMetadata.getID() > 0);
        assertTrue(versionedMetadata.getRevision() > 0);

        MCRMetadata vm3 = getVersStore().create(new MCRJDOMContent(testXmlDoc));
        assertTrue(vm3.getID() > versionedMetadata.getID());
        // revision == SVN revision??
        // assertTrue(vm3.getRevision() > versionedMetadata.getRevision());
    }

    @Test
    public void createDocumentInt() throws Exception {
        final int id = getVersStore().getNextFreeID();
        assertTrue(id > 0);
        Document xml1 = new Document(new Element("root"));
        MCRMetadata vm1 = getVersStore().create(id, new MCRJDOMContent(xml1));
        MCRContent xml2 = getVersStore().retrieve(id).read();

        assertNotNull(vm1);
        assertEquals(new MCRJDOMContent(xml1).asString(), xml2.asString());
        getVersStore().create(id + 1, new MCRJDOMContent(xml1));
        try {
            MCRContent xml3 = getVersStore().retrieve(id + 1).read();
            assertEquals(new MCRJDOMContent(xml1).asString(), xml3.asString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // SVN store lacks hardwired XML store "cache" now
    // `exist` of deleted IDs on versioned store evaluates to true (existed at some point, versioning data preserved)
    @Test
    public void delete() throws Exception {
        System.out.println("TEST DELETE");
        Document xml1 = new Document(new Element("root"));
        MCRMetadata metadata = getVersStore().create(new MCRJDOMContent(xml1));
        int id = metadata.getID();
        assertTrue(getVersStore().exists(id));
        assertFalse(metadata.getVersion().getState() == MCRMetadataVersionState.DELETED);
        assertFalse(metadata.getVersionLast().getState() == MCRMetadataVersionState.DELETED);
        getVersStore().delete(id);
        assertTrue(getVersStore().exists(id));
        assertFalse(metadata.getVersion().getState() == MCRMetadataVersionState.DELETED);
        assertTrue(metadata.getVersionLast().getState() == MCRMetadataVersionState.DELETED);
    }

    @Test
    public void update() throws Exception {
        Document xml1 = new Document(new Element("root"));
        MCRMetadata vm = getVersStore().create(new MCRJDOMContent(xml1));
        Document xml3 = new Document(new Element("update"));
        long rev = vm.getRevision();
        vm.update(new MCRJDOMContent(xml3));
        assertTrue(vm.getRevision() > rev);
        MCRContent xml4 = getVersStore().retrieve(vm.getID()).read();
        assertEquals(new MCRJDOMContent(xml3).asString(), xml4.asString());
    }

    @Test
    public void retrieve() throws Exception {
        Document xml1 = new Document(new Element("root"));
        int id;
        try {
            id = getVersStore().create(new MCRJDOMContent(xml1)).getID();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        MCRMetadata sm1 = getVersStore().retrieve(id);
        MCRContent xml2 = sm1.read();
        assertEquals(new MCRJDOMContent(xml1).asString(), xml2.asString());
    }

    @Test
    public void versioning() throws Exception {
        Document xml1 = new Document(new Element("bingo"));
        MCRMetadata vm = getVersStore().create(new MCRJDOMContent(xml1));
        long baseRev = vm.getRevision();
        // SVN + XML decoupled, obsoleted
        // assertTrue(vm.isUpToDate());

        List<MCRMetadataVersion> versions = vm.getVersions();
        assertNotNull(versions);
        assertEquals(1, versions.size());
        MCRMetadataVersion mv = versions.get(0);
        // MCRMetadata constructed on-the-fly, not the same object references but same contents
        // assertSame(mv.getMetadata(), vm);
        assertEquals(mv.getMetadata().read().asString(), vm.read().asString());
        assertEquals(baseRev, mv.getRevision());
        assertEquals(MCRSessionMgr.getCurrentSession().getUserInformation().getUserID(), mv.getUser());
        assertEquals('C', mv.getType());
        assertEquals(MCRMetadataVersionState.CREATED, mv.getState());

        bzzz();
        Document xml2 = new Document(new Element("bango"));
        vm.update(new MCRJDOMContent(xml2));
        assertTrue(vm.getRevision() > baseRev);
        // SVN + XML decoupled, obsoleted(??)
        // assertTrue(vm.isUpToDate());

        versions = vm.getVersions();
        assertEquals(2, versions.size());
        mv = versions.get(0);
        assertEquals(baseRev, mv.getRevision());
        mv = versions.get(1);
        assertEquals((long) vm.getRevision(), mv.getRevision());
        assertEquals('U', mv.getType());
        assertEquals(MCRMetadataVersionState.UPDATED, mv.getState());

        bzzz();
        Document xml3 = new Document(new Element("bongo"));
        vm.update(new MCRJDOMContent(xml3));

        versions = vm.getVersions();
        assertEquals(3, versions.size());
        mv = versions.get(0);
        assertEquals(baseRev, mv.getRevision());
        mv = versions.get(2);
        assertEquals((long) vm.getRevision(), mv.getRevision());
        assertTrue(versions.get(0).getRevision() < versions.get(1).getRevision());
        assertTrue(versions.get(1).getRevision() < versions.get(2).getRevision());
        assertTrue(versions.get(0).getDate().before(versions.get(1).getDate()));
        assertTrue(versions.get(1).getDate().before(versions.get(2).getDate()));

        xml1 = versions.get(0).getMetadata().read().asXML();
        assertNotNull(xml1);
        assertEquals("bingo", xml1.getRootElement().getName());
        xml2 = versions.get(1).getMetadata().read().asXML();
        assertNotNull(xml2);
        assertEquals("bango", xml2.getRootElement().getName());
        xml3 = versions.get(2).getMetadata().read().asXML();
        assertNotNull(xml1);
        assertEquals("bongo", xml3.getRootElement().getName());

        bzzz();
        versions.get(1).restore();
        // MCRMetadata self-updates on CUD-invocations
        // assertTrue(vm.getRevision() > versions.get(2).getRevision());
        // assertTrue(vm.getLastModified().after(versions.get(2).getDate()));
        assertTrue(vm.getRevision() == versions.get(2).getRevision());
        assertTrue(vm.getLastModified().compareTo(versions.get(2).getDate()) == 0);
        // assertEquals("bango", vm.read().asXML().getRootElement().getName());
        assertEquals("bongo", vm.read().asXML().getRootElement().getName());
        assertEquals("bango", vm.getVersion(2).getMetadata().read().asXML().getRootElement().getName());
        assertEquals(4, vm.getVersions().size());
    }

    @Test
    public void createUpdateDeleteCreate() throws Exception {
        Element root = new Element("bingo");
        Document xml1 = new Document(root);
        MCRMetadata vm = getVersStore().create(new MCRJDOMContent(xml1));
        root.setName("bango");
        vm.update(new MCRJDOMContent(xml1));
        vm.delete();
        root.setName("bongo");
        vm = getVersStore().create(vm.getID(), new MCRJDOMContent(xml1));
        List<MCRMetadataVersion> versions = vm.getVersions();
        assertEquals(4, versions.size());
        assertEquals(MCRMetadataVersionState.CREATED, versions.get(0).getState());
        assertEquals(MCRMetadataVersionState.UPDATED, versions.get(1).getState());
        assertEquals(MCRMetadataVersionState.DELETED, versions.get(2).getState());
        assertEquals(MCRMetadataVersionState.CREATED, versions.get(3).getState());
        versions.get(1).restore();
        // MCRMetadata objects self-update when invoking their CRUD-methods
        // assertEquals("bango", vm.read().asXML().getRootElement().getName());
        assertEquals("bango",
            getVersStore().getMetadataVersionLast(vm.getID()).getMetadata().read().asXML().getRootElement().getName());
    }

    // SVN store lacks hardwired XML store "cache" now
    // `exist` of deleted IDs on versioned store evaluates to true (existed at some point, versioning data preserved)
    @Test
    public void deletedVersions() throws Exception {
        Element root = new Element("bingo");
        Document xml1 = new Document(root);
        MCRMetadata vm = getVersStore().create(new MCRJDOMContent(xml1));
        int id = vm.getID();
        assertTrue(getVersStore().exists(id));
        assertFalse(vm.getVersion().getState() == MCRMetadataVersionState.DELETED);
        assertFalse(vm.getVersionLast().getState() == MCRMetadataVersionState.DELETED);

        getVersStore().delete(id);
        assertTrue(getVersStore().exists(id));
        assertFalse(vm.getVersion().getState() == MCRMetadataVersionState.DELETED);
        assertTrue(vm.getVersionLast().getState() == MCRMetadataVersionState.DELETED);

        vm = getVersStore().retrieve(vm.getID());
        assertTrue(vm.isDeletedInRepository());
        List<MCRMetadataVersion> versions = vm.getVersions();
        MCRMetadataVersion v1 = versions.get(0);
        MCRMetadataVersion v2 = versions.get(1);

        boolean cannotRestoreDeleted = false;
        try {
            v2.restore();
        } catch (MCRUsageException ex) {
            cannotRestoreDeleted = true;
        }
        assertTrue(cannotRestoreDeleted);

        v1.restore();
        assertFalse(vm.isDeletedInRepository());
        // vm -> deleted revision, contains no MCRContent
        // assertEquals(root.getName(), vm.read().asXML().getRootElement().getName());
        assertEquals(root.getName(), getVersStore().retrieve(vm.getID()).read().asXML().getRootElement().getName());
    }

    @Test
    public void verifyRevPropsFail() throws Exception {
        getVersStore().verify();
        deletedVersions();
        getVersStore().verify();
        File baseDIR = new File(URI.create(getVersStore().repURL.toString()));
        File revProp = new File(baseDIR.toURI().resolve("db/revprops/0/2"));
        assertTrue("is not a file " + revProp, revProp.isFile());
        revProp.setWritable(true);
        new PrintWriter(revProp).close();
        revProp.setWritable(false);
        try {
            getVersStore().verify();
        } catch (MCRPersistenceException e) {
            return;
        }
        fail("Verify finished without error");
    }

    @Test
    public void verifyRevFail() throws Exception {
        getVersStore().verify();
        deletedVersions();
        getVersStore().verify();
        File baseDIR = new File(URI.create(getVersStore().repURL.toString()));
        File revProp = new File(baseDIR.toURI().resolve("db/revs/0/2"));
        assertTrue("is not a file " + revProp, revProp.isFile());
        new PrintWriter(revProp).close();
        try {
            getVersStore().verify();
        } catch (MCRPersistenceException e) {
            return;
        }
        fail("Verify finished without error");
    }
}
