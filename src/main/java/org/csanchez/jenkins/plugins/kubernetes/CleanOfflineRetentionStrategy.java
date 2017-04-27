package org.csanchez.jenkins.plugins.kubernetes;

import static hudson.util.TimeUnit2.MINUTES;
import static java.util.logging.Level.WARNING;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jenkinsci.plugins.durabletask.executors.ContinuableExecutable;

import hudson.model.Executor;
import hudson.model.ExecutorListener;
import hudson.model.OneOffExecutor;
import hudson.model.Queue;
import hudson.model.Queue.Task;
import hudson.slaves.AbstractCloudComputer;
import hudson.slaves.AbstractCloudSlave;
import hudson.slaves.CloudRetentionStrategy;
import hudson.slaves.EphemeralNode;

public final class CleanOfflineRetentionStrategy extends CloudRetentionStrategy implements ExecutorListener {
	private static final Logger LOGGER = Logger.getLogger(CleanOfflineRetentionStrategy.class.getName());
	
	private transient boolean terminating;
	
	private int idleMinutes;
	
	public CleanOfflineRetentionStrategy(int idleMinutes) {
		super(idleMinutes);
		this.idleMinutes = idleMinutes;
	}
	
	@Override
	public long check(final AbstractCloudComputer c) {
        AbstractCloudSlave computerNode = c.getNode();
        if (c.isOffline() && !disabled && computerNode != null) {
            final long idleMilliseconds = System.currentTimeMillis() - c.getIdleStartMilliseconds();
            if (idleMilliseconds > MINUTES.toMillis(idleMinutes)) {
                LOGGER.log(Level.INFO, "Disconnecting {0} as it has been offline for {1} minutes", new Object[]{c.getName(), idleMinutes});
                done(c);
            }
        }
        // Return one because we want to check every minute if idle.
		return 1;
	}
	
    @Override
    public void start(AbstractCloudComputer c) {
        if (c.getNode() instanceof EphemeralNode) {
            throw new IllegalStateException("May not use OnceRetentionStrategy on an EphemeralNode: " + c);
        }
        super.start(c);
    }

	@Override
	public void taskAccepted(Executor executor, Task task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void taskCompleted(Executor executor, Task task, long durationMS) {
		done(executor);
	}

	@Override
	public void taskCompletedWithProblems(Executor executor, Task task, long durationMS, Throwable problems) {
		done(executor);
	}
	
    private void done(Executor executor) {
        final AbstractCloudComputer<?> c = (AbstractCloudComputer) executor.getOwner();
        Queue.Executable exec = executor.getCurrentExecutable();
        if (executor instanceof OneOffExecutor) {
            LOGGER.log(Level.FINE, "not terminating {0} because {1} was a flyweight task", new Object[] {c.getName(), exec});
            return;
        }
        if (exec instanceof ContinuableExecutable && ((ContinuableExecutable) exec).willContinue()) {
            LOGGER.log(Level.FINE, "not terminating {0} because {1} says it will be continued", new Object[] {c.getName(), exec});
            return;
        }
        LOGGER.log(Level.FINE, "terminating {0} since {1} seems to be finished", new Object[] {c.getName(), exec});
        done(c);
    }
    
    private void done(final AbstractCloudComputer<?> c) {
        c.setAcceptingTasks(false); // just in case
        synchronized (this) {
            if (terminating) {
                return;
            }
            terminating = true;
        }

        try {
            AbstractCloudSlave computerNode = c.getNode();
            if (computerNode != null) {
                computerNode.terminate();
            }
        } catch (InterruptedException e) {
            LOGGER.log(WARNING,"Failed to terminate "+c.getName(),e);
            synchronized(this) {
                terminating = false;
            }
        } catch (IOException e) {
            LOGGER.log(WARNING,"Failed to terminate "+c.getName(),e);
            synchronized(this) {
                terminating = false;
            }
        }
    }
}