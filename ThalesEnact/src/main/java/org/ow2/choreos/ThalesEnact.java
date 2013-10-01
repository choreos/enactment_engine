package org.ow2.choreos;

import org.ow2.choreos.chors.ChoreographyDeployer;
import org.ow2.choreos.chors.ChoreographyNotFoundException;
import org.ow2.choreos.chors.EnactmentException;
import org.ow2.choreos.chors.client.ChorDeployerClient;
import org.ow2.choreos.chors.datamodel.Choreography;
import org.ow2.choreos.chors.datamodel.ChoreographySpec;
import org.ow2.choreos.utils.Alarm;
import org.ow2.choreos.utils.CommandLineException;

public class ThalesEnact {

    public static void main(String[] args) throws EnactmentException, ChoreographyNotFoundException, CommandLineException {
        
        // setup
        final String CHOR_DEPLOYER_URI = "http://localhost:9102/choreographydeployer";
        ChoreographyDeployer chorDeployer = new ChorDeployerClient(CHOR_DEPLOYER_URI);
        ThalesSpecs thalesSpecs = new ThalesSpecs();
        ChoreographySpec chorSpec = thalesSpecs.getChorSpec(); 
        
        // action
        String chorId = chorDeployer.createChoreography(chorSpec);
        Choreography chor = chorDeployer.enactChoreography(chorId);
        
        // output
        System.out.println(chor);

        // alarm
        Alarm alarm = new Alarm();
        alarm.play();        
    }

}
