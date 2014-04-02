package org.ow2.choreos.invoker;

import java.util.concurrent.Callable;

public interface InvokerConfigurator<T> {

    public Invoker<T> getConfiguredInvoker(String taskName, Callable<T> task);

}
