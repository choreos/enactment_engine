package org.ow2.choreos.invoker;

import java.util.concurrent.Callable;

public class AdaptiveInvokerConfigurator<T> implements InvokerConfigurator<T> {

    @Override
    public Invoker<T> getConfiguredInvoker(String taskName, Callable<T> task) {
        adaptValues();
        InvokerConfigurator<T> staticConfigurator = new StaticInvokerConfigurator<T>();
        return staticConfigurator.getConfiguredInvoker(taskName, task);
    }

    private void adaptValues() {
        // Analyze the history of time consumed to perform the task in previous
        // invocations, using InvokerHistory.
        // If necessary, changes parameters to invoke the task,
        // using InvokerConfiguration.
    }

}
