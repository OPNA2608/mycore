/*
 *  This file is part of ***  M y C o R e  ***
 *  See http://www.mycore.de/ for details.
 *
 *  This program is free software; you can use it, redistribute it
 *  and / or modify it under the terms of the GNU General Public License
 *  (GPL) as published by the Free Software Foundation; either version 2
 *  of the License or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program, in a file called gpl.txt or license.txt.
 *  If not, write to the Free Software Foundation Inc.,
 *  59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 *
 */

package org.mycore.iiif.presentation.model.attributes;

import org.mycore.iiif.model.MCRIIIFBase;

public class MCRIIIFLDURI extends MCRIIIFBase {

    private String format = null;

    public MCRIIIFLDURI(String uri, String type, String format) {
        super(uri, type, MCRIIIFBase.API_PRESENTATION_2);
        this.format = format;
    }

    public MCRIIIFLDURI(String uri, String type) {
        super(uri, type, MCRIIIFBase.API_PRESENTATION_2);
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}