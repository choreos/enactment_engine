package org.ow2.choreos.invoker.timeouted;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.ow2.choreos.invoker.Invoker;
import org.ow2.choreos.invoker.InvokerException;
import org.ow2.choreos.utils.Concurrency;

/**
 * Performs a task (supposed to invoke an external system) until a given timeout. 
 *
 */
class TimeoutedInvoker<T> implements Invoker<T> {

    private static Logger logger = Logger.getLogger(TimeoutedInvoker.class);
    
    private String taskName;
    private Callable<T> task;
    private int timeout;
    private TimeUnit timeUnit;
    
    public TimeoutedInvoker(String taskName, Callable<T> task, int timeout, TimeUnit timeUnit) {
        this.taskName = taskName;
        this.task = task;
        this.timeout = timeout;    
        this.timeUnit = timeUnit;
    }

    @Override
    public T invoke() throws InvokerException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<T> future = executor.submit(task);
        String errorMessage = "Invoker could not properly invoke " + taskName;
        Concurrency.waitExecutor(executor, timeout, timeUnit, logger, errorMessage);
        try {
            T result = Concurrency.checkAndGetFromFuture(future);
            return result;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            throw new InvokerException(cause);
        }
    }

}
