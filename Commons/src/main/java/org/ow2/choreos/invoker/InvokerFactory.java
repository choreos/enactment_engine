package org.ow2.choreos.invoker;

import java.util.concurrent.Callable;

import org.ow2.choreos.invoker.timeouted.TimeoutedInvokerConfigurator;

public class InvokerFactory<T> {

    private static enum InvokerType {DEFAULT, TIMEOUTED};
    private static final InvokerType invokerType = InvokerType.DEFAULT; 
    
    public Invoker<T> geNewInvokerInstance(String taskName, Callable<T> task) {
        InvokerConfigurator<T> invokerConfigurator = null;
        if (invokerType == InvokerType.DEFAULT)
            invokerConfigurator = new AdaptiveInvokerConfigurator<T>();
        if (invokerType == InvokerType.TIMEOUTED)
            invokerConfigurator = new TimeoutedInvokerConfigurator<T>();
        return invokerConfigurator.getConfiguredInvoker(taskName, task);
    }

}
