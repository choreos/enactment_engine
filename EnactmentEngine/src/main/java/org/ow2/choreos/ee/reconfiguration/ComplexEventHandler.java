package org.ow2.choreos.ee.reconfiguration;

import org.ow2.choreos.chors.datamodel.Choreography;

public abstract class ComplexEventHandler {

    protected ChoreographyRegistryHelper registryHelper = new ChoreographyRegistryHelper();

    public abstract void handleEvent(HandlingEvent event, Choreography chor);

}
