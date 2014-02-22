package org.ow2.choreos.ee.preparer;

import java.util.concurrent.ExecutorService;

import org.ow2.choreos.utils.Concurrency;
import org.ow2.choreos.utils.TimeoutsAndTrials;

class PreparerWaiter {

    private String chorId;
    private ExecutorService executor;

    public PreparerWaiter(String chorId, ExecutorService executor) {
        this.chorId = chorId;
        this.executor = executor;
    }

    public void waitPreparement() {
        int totalTimeout = getTotalTimeout();
        String message = "Could not properly configure all the services of chor " + chorId;
        Concurrency.waitExecutor(executor, totalTimeout, message);
    }

    private int getTotalTimeout() {
        int nodeCreationTotalTimeout = TimeoutsAndTrials.getTotalTimeout("NODE_CREATION");
        int firstSshTimeout = TimeoutsAndTrials.getTimeout("FIRST_CONNECT_SSH");
        int bootstrapTotalTimeout = TimeoutsAndTrials.getTotalTimeout("BOOTSTRAP");
        int prepareTotalTimeout = TimeoutsAndTrials.getTotalTimeout("PREPARE_DEPLOYMENT");
        int oneReqPerSec = 2 * 100;
        int totalTimeout = nodeCreationTotalTimeout + firstSshTimeout + bootstrapTotalTimeout + prepareTotalTimeout
                + oneReqPerSec;
        totalTimeout += totalTimeout * 0.2;
        return totalTimeout;
    }

}
