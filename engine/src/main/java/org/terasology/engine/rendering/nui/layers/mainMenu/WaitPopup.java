// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UILabel;
import org.terasology.engine.rendering.nui.CoreScreenLayer;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;


/**
 * A popup message that is shown while a long-term background operation is running.
 * Some of them can be cancelled.
 *
 */
public class WaitPopup<T> extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:waitPopup!instance");

    private static final Logger logger = LoggerFactory.getLogger(WaitPopup.class);

    private FutureTask<T> parallelTask;

    private Thread thread;

    private Consumer<T> resultEvent;

    private UILabel titleLabel;
    private UILabel messageLabel;
    private UIButton cancelButton;

    @Override
    public void initialise() {
        titleLabel = find("title", UILabel.class);
        Preconditions.checkNotNull(titleLabel, "UILabel 'title' not found");

        messageLabel = find("message", UILabel.class);
        Preconditions.checkNotNull(messageLabel, "UILabel 'message' not found");

        cancelButton = find("cancel", UIButton.class);
        Preconditions.checkNotNull(cancelButton, "UIButton 'cancel' not found");
    }

    public void setMessage(String title, String message) {
        setTitleText(title);
        bindMessageText(new ReadOnlyBinding<String>() {

            @Override
            public String get() {
                return message;
            }
        });
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
                resultEvent.accept(result);
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.warn("An error occurred during execution", e);
            getManager().popScreen();
            getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Error", e.getMessage());
        }
    }

    /**
     * @param runnable will be called once the result is available
     */
    public void onSuccess(Consumer<T> runnable) {
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

        WidgetUtil.trySubscribe(this, "cancel", button -> parallelTask.cancel(true));
    }

    public boolean canBeCancelled() {
        return cancelButton.isVisible();
    }

    public void setTitleText(String text) {
        titleLabel.setText(text);
    }

    public void bindMessageText(Binding<String> binding) {
        messageLabel.bindText(binding);
    }

    public void setCancelText(String text) {
        cancelButton.setText(text);
    }
}
