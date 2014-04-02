package org.ow2.choreos.invoker.timeouted;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.ow2.choreos.invoker.Invoker;
import org.ow2.choreos.invoker.InvokerConfiguration;
import org.ow2.choreos.invoker.InvokerConfigurator;


/**
 * Retrieves configuration from timeouts_and_trials
 * 
 * @author leonardo
 * 
 * @param <T>
 */
public class TimeoutedInvokerConfigurator<T> implements InvokerConfigurator<T> {

    private static final TimeUnit timeUnit = TimeUnit.SECONDS;

    @Override
    public Invoker<T> getConfiguredInvoker(String taskName, Callable<T> task) {
        int timeout = InvokerConfiguration.getTimeout(taskName);
        return new TimeoutedInvoker<T>(taskName, task, timeout, timeUnit);
    }

}
