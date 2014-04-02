package org.ow2.choreos.invoker;

import java.util.concurrent.Callable;


/**
 * Retrieves configuration from timeouts_and_trials
 * 
 * @author leonardo
 * 
 * @param <T>
 */
public class StaticInvokerConfigurator<T> implements InvokerConfigurator<T> {

    private String taskName;
    private Callable<T> task;
    private InvokerBuilder<T> builder;

    @Override
    public Invoker<T> getConfiguredInvoker(String taskName, Callable<T> task) {
        this.taskName = taskName;
        this.task = task;
        initBuilder();
        setTrials();
        setPause();
        return builder.build();
    }

    private void initBuilder() {
        int timeout = InvokerConfiguration.getTimeout(taskName);
        builder = new InvokerBuilder<T>(taskName, task, timeout);
    }

    private void setTrials() {
        try {
            int trials = InvokerConfiguration.getTrials(taskName);
            builder.trials(trials);
        } catch (IllegalArgumentException e) {
            // pas du problem: InvokerBuilder will use default value
        }
    }
    
    private void setPause() {
        try {
            int pause = InvokerConfiguration.getPauseBetweenTrials(taskName);
            builder.pauseBetweenTrials(pause);
        } catch (IllegalArgumentException e) {
            // pas du problem: InvokerBuilder will use default value
        }
    }

}
