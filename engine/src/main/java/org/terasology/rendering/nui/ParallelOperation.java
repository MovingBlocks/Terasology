/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.rendering.nui;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.rendering.nui.layers.mainMenu.MessagePopup;
import org.terasology.rendering.nui.widgets.ActivateEventListener;

/**
 * Executes a {@link Callable} (that can be cancelled) in a separate {@link Thread}
 * and shows message box while doing that. 
 * @author Martin Steiger
 */
public class ParallelOperation<V> {

    private static final Logger logger = LoggerFactory.getLogger(ParallelOperation.class);

    public enum Status {
        IDLE(false),
        RUNNING(false),
        CANCELLED(true),
        ERROR(true),
        SUCCESSFUL(true);
        
        private boolean finished;

        private Status(boolean finished) {
            this.finished = finished;
        }
        
        public boolean isFinished() {
            return finished;
        }
    }
    
    private FutureTask<V> parallelTask;
    private V result;
    
    /**
     * Remains in IDLE state forever
     */
    public ParallelOperation() {
    }

    /**
     * @param callable the callable that will be run - it should check Thread.currentThread().isInterrupted() on a regular basis
     */
    public ParallelOperation(Callable<V> callable) {
        parallelTask = new FutureTask<>(callable);

        Thread thread = new Thread(parallelTask, "Parallel Operation");
        thread.start();
    }
    
    public Status getStatus() {
        if (parallelTask == null) {
            return Status.IDLE;
        }
        
        if (!parallelTask.isDone()) {
            return Status.RUNNING;
        } 
            
        if (parallelTask.isCancelled()) {
            return Status.CANCELLED;
        }

        try {
            result = parallelTask.get();
            return Status.SUCCESSFUL;
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("An error occurred during execution", e);
            return Status.ERROR;
        }
    }

    /**
     * @return the result - only available when {@link Status} is <code>SUCCESSFUL</code>
     */
    public V getResult() {
        return result;
    }

    public MessagePopup showPopup(NUIManager manager, String title, String message) {
      MessagePopup popup = manager.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class);
      // the popup is already initialized at this point
      popup.setMessage(title, message);
      popup.setCloseButtonText("Cancel");
      popup.setCloseAction(new ActivateEventListener() {
          
          @Override
          public void onActivated(UIWidget widget) {
              parallelTask.cancel(true);
          }
      });
      return popup;
    }
}
