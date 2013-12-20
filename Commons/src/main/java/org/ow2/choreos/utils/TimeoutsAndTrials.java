/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.ow2.choreos.utils;

/**
 * 
 * @author leonardo
 * 
 */
public class TimeoutsAndTrials {

    private static final String FILE_PATH = "timeouts_and_trials.properties";

    private final Configuration configuration;

    private static TimeoutsAndTrials INSTANCE = new TimeoutsAndTrials();

    private TimeoutsAndTrials() {
        this.configuration = new Configuration(FILE_PATH);
    }

    private static int get(String key) {
        try {
            String valueStr = INSTANCE.configuration.get(key);
            if (valueStr != null)
                return Integer.parseInt(valueStr.trim());
            else
                throw new IllegalArgumentException(key + " not configured on " + FILE_PATH);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + " not configured on " + FILE_PATH);
        }
    }

    public static void set(String key, int value) {
        INSTANCE.configuration.set(key, Integer.toString(value));
    }

    /**
     * The timeout of one trial.
     * 
     * @param taskName
     * @return
     */
    public static int getTimeout(String taskName) {
        return get(taskName = "_TIMEOUT");
    }

    public static int getTrials(String taskName) {
        return get(taskName = "_TRIALS");
    }

    public static int getPauseBetweenTrials(String taskName) {
        return get(taskName = "_PAUSE");
    }

    /**
     * The time to complete all the trials, that is (timeout + pause) * trials.
     * 
     * @param taskName
     * @return
     */
    public static int getTotalTimeout(String taskName) {
        int timeout = TimeoutsAndTrials.getTimeout(taskName);
        int trials = TimeoutsAndTrials.getTrials(taskName);
        int pause = TimeoutsAndTrials.getPauseBetweenTrials(taskName);
        int totalTimeout = (timeout + pause) * trials;
        return totalTimeout;
    }

}
