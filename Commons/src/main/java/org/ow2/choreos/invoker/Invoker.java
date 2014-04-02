package org.ow2.choreos.invoker;

/**
 * Encapsulates error handling strategy when invoking an external system.
 * 
 * @param <T> The type returned by the task that encapsulates the external invocation.
 */
public interface Invoker<T> {

    public T invoke() throws InvokerException;
    
}
