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

/**
 * 
 */
package org.mycore.restapi.v1;

import java.util.stream.Stream;

import javax.ws.rs.ApplicationPath;

import org.mycore.common.config.MCRConfiguration;
import org.mycore.restapi.MCRJerseyRestApp;
import org.mycore.restapi.MCRNormalizeMCRObjectIDsFilter;
import org.mycore.restapi.converter.MCRWrappedXMLWriter;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;

/**
 * @author Thomas Scheffler (yagee)
 *
 */
@ApplicationPath("/api/v1")
public class MCRRestV1App extends MCRJerseyRestApp {

    public MCRRestV1App() {
        super();
        register(MCRNormalizeMCRObjectIDsFilter.class);
    }
	
    @Override
    protected String getVersion() {
        return "v1";
    }
    
    @Override
    protected String[] getRestPackages() {
        return Stream
            .concat(
                Stream.of(MCRWrappedXMLWriter.class.getPackage().getName(),
                    OpenApiResource.class.getPackage().getName()),
                MCRConfiguration.instance().getStrings("MCR.RestAPI.Resource.Packages").stream())
            .toArray(String[]::new);
    }
}
