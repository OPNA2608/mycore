/**
 * 
 * $Revision: 28688 $ $Date: 2013-12-18 15:27:20 +0100 (Wed, 18 Dec 2013) $
 *
 * This file is part of ** M y C o R e **
 * Visit our homepage at http://www.mycore.de/ for details.
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
 * along with this program, normally in the file license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 *
 **/
package org.mycore.datamodel.niofs;

import static org.mycore.datamodel.niofs.MCRAbstractFileSystem.SEPARATOR;
import static org.mycore.datamodel.niofs.MCRAbstractFileSystem.SEPARATOR_STRING;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;

;

/**
 * @author Thomas Scheffler (yagee)
 * 
 * @version $Revision: 28688 $ $Date: 2013-12-18 15:27:20 +0100 (Wed, 18 Dec 2013) $
 */
public class MCRPathXML {

    static Logger LOGGER = Logger.getLogger(MCRPathXML.class);

    private MCRPathXML() {
    }

    public static Document getDirectoryXML(MCRPath path) throws IOException {
        BasicFileAttributes attr = path.getFileSystem().provider().readAttributes(path, BasicFileAttributes.class);
        return getDirectoryXML(path, attr);
    }

    /**
     * Sends the contents of an MCRDirectory as XML data to the client
     * @throws IOException 
     */
    public static Document getDirectoryXML(MCRPath path, BasicFileAttributes attr) throws IOException {
        LOGGER.debug("MCRDirectoryXML: start listing of directory " + path.toString());

        Element root = new Element("mcr_directory");
        Document doc = new org.jdom2.Document(root);

        addString(root, "uri", path.toUri().toString());
        addString(root, "ownerID", path.getOwner());
        MCRPath relativePath = path.getRoot().relativize(path);
        boolean isRoot = relativePath.toString().isEmpty();
        addString(root, "name", (isRoot ? "" : relativePath.getFileName().toString()));
        addString(root, "path", toStringValue(relativePath));
        if (!isRoot) {
            addString(root, "parentPath", toStringValue(relativePath.getParent()));
        }
        addBasicAttributes(root, attr, path);

        Element nodes = new Element("children");
        root.addContent(nodes);
        SortedMap<MCRPath, MCRFileAttributes<?>> directories = new TreeMap<>();
        SortedMap<MCRPath, MCRFileAttributes<?>> files = new TreeMap<>();
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(path)) {
            for (Path child : dirStream) {
                MCRFileAttributes<?> childAttrs = Files.readAttributes(child, MCRFileAttributes.class);
                if (childAttrs.isDirectory()) {
                    directories.put(MCRPath.toMCRPath(child), childAttrs);
                } else {
                    files.put(MCRPath.toMCRPath(child), childAttrs);
                }
            }
        }
        for (Map.Entry<MCRPath, MCRFileAttributes<?>> dirEntry : directories.entrySet()) {
            Element child = new Element("child");
            child.setAttribute("type", "directory");
            addString(child, "name", dirEntry.getKey().getFileName().toString());
            addString(child, "uri", dirEntry.getKey().toUri().toString());
            nodes.addContent(child);
            addBasicAttributes(child, dirEntry.getValue(), dirEntry.getKey());
        }
        for (Map.Entry<MCRPath, MCRFileAttributes<?>> fileEntry : files.entrySet()) {
            Element child = new Element("child");
            child.setAttribute("type", "file");
            addString(child, "name", fileEntry.getKey().getFileName().toString());
            addString(child, "uri", fileEntry.getKey().toUri().toString());
            nodes.addContent(child);
            addAttributes(child, fileEntry.getValue(), fileEntry.getKey());
        }

        LOGGER.debug("MCRDirectoryXML: end listing of directory " + path);

        return doc;

    }

    private static String toStringValue(MCRPath relativePath) {
        if (relativePath == null) {
            return SEPARATOR_STRING;
        }
        String pathString = relativePath.toString();
        if (pathString.isEmpty()) {
            return SEPARATOR_STRING;
        }
        if (pathString.equals(SEPARATOR_STRING)) {
            return pathString;
        }
        return SEPARATOR + pathString + SEPARATOR;
    }

    private static void addBasicAttributes(Element root, BasicFileAttributes attr, MCRPath path) throws IOException {
        addString(root, "size", String.valueOf(attr.size()));
        addDate(root, "created", attr.creationTime());
        addDate(root, "lastModified", attr.lastModifiedTime());
        addDate(root, "lastAccessed", attr.lastAccessTime());
        if (attr.isRegularFile()) {
            addString(root, "contentType", MCRContentTypes.probeContentType(path));
        }
    }

    private static void addAttributes(Element root, MCRFileAttributes<?> attr, MCRPath path) throws IOException {
        addBasicAttributes(root, attr, path);
        addString(root, "md5", attr.md5sum());
    }

    private static void addDate(Element parent, String type, FileTime date) {
        Element xDate = new Element("date");
        parent.addContent(xDate);
        xDate.setAttribute("type", type);
        xDate.addContent(date.toString());
    }

    private static void addString(Element parent, String itemName, String content) {
        if (content == null) {
            return;
        }

        parent.addContent(new Element(itemName).addContent(content.trim()));
    }
}