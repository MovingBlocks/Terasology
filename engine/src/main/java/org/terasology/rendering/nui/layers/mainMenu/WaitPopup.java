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
package org.terasology.rendering.nui.layers.mainMenu;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILabel;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

/**
 * A popup message that is shown while a long-term background operation is running.
 * Some of them can be cancelled.
 * @author Martin Steiger
 */
public class WaitPopup<T> extends CoreScreenLayer {

    public static final AssetUri ASSET_URI = new AssetUri(AssetType.UI_ELEMENT, "engine:waitPopup");

    private static final Logger logger = LoggerFactory.getLogger(WaitPopup.class);

    private FutureTask<T> parallelTask;

    private Thread thread;

    private Function<T, Void> resultEvent;

    private UILabel titleLabel;
    private UILabel messageLabel;
    private UIButton cancelButton;

    @Override
    protected void initialise() {
        titleLabel = find("title", UILabel.class);
        Preconditions.checkNotNull(titleLabel, "UILabel 'title' not found");

        messageLabel = find("message", UILabel.class);
        Preconditions.checkNotNull(messageLabel, "UILabel 'message' not found");

        cancelButton = find("cancel", UIButton.class);
        Preconditions.checkNotNull(cancelButton, "UIButton 'cancel' not found");
    }
    
    public void setMessage(String title, String message) {
        titleLabel.setText(title);
        messageLabel.setText(message);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        
        if (parallelTask == null) {
            return; // idle
        }
        
        if (!parallelTask.isDone()) {
            return; // still running
        } 
            
        if (parallelTask.isCancelled()) {
            // wait for the thread to die
            if (!thread.isAlive()) {
                getManager().popScreen();
            }
            return;
        }

        try {
            T result = parallelTask.get();
            getManager().popScreen();
            if (resultEvent != null) {
                resultEvent.apply(result);
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("An error occurred during execution", e);
            getManager().popScreen();
        }
    }
    
    /**
     * @param runnable will be called once the result is available
     */
    public void onSuccess(Function<T, Void> runnable) {
        this.resultEvent = runnable;
    }

    /**
     * @param operation the operation to run - the executing thread will be interrupted when the operation is cancelled
     * @param canBeCancelled true if the operation is aborted when the {@link Thread#isInterrupted()} flag is set 
     * @throws NullPointerException if operation is null
     * @throws IllegalArgumentException if startOperation() was called before
     */
    public void startOperation(Callable<T> operation, boolean canBeCancelled) {
        Preconditions.checkState(parallelTask == null, "startOperation() cannot be called twice");

        cancelButton.setVisible(canBeCancelled);
            
        parallelTask = new FutureTask<>(operation);

        thread = new Thread(parallelTask, "Parallel Operation");
        thread.start();
        
        WidgetUtil.trySubscribe(this, "cancel", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                parallelTask.cancel(true);
            }
        });
    }

    @Override
    public void onClosed() {
        super.onClosed();

        // don't save this asset in the cache -> don't persist changes to this class
        Assets.dispose(Assets.get(WaitPopup.ASSET_URI));
    }

    public boolean canBeCancelled() {
        return cancelButton.isVisible();
    }
}
