/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ow2.choreos.chors.datamodel;

import java.util.List;

import org.ow2.choreos.services.datamodel.Service;

/**
 * When dealing with legacy services, there is no real Service object; these
 * only exist when they are created by the DeploymentManager. But we want to
 * handle both cases with a homogeneous interface on the ChoreographyDeployer,
 * so this class is a "mock" class to encapsulate the data we need to represent
 * legacy services.
 */
public class LegacyService extends Service {

    private List<String> URIs;

    public LegacyService(LegacyServiceSpec serviceSpec) {
        super(serviceSpec);
        URIs = serviceSpec.getNativeURIs();
    }

    public LegacyService() {

    }

    @Override
    public List<String> getUris() {
        return URIs;
    }

}
