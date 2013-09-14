package org.terasology.signalling.gui;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.*;
import org.terasology.signalling.components.SignalTimeDelayComponent;

import javax.vecmath.Vector2f;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class UIDelayConfiguration extends UIWindow {

    private UILabel titleLabel;
    private UIButton left;
    private UIButton right;
    private UILabel time;

    private EntityRef blockEntity;

    private static final long MINIMUM_DELAY=500;
    private static final long DELAY_STEP = 100;
    private long timeMs;

    public UIDelayConfiguration() {
        setId("signalling:delayConfiguration");
        setBackgroundColor(new Color(0, 0, 0, 200));
        setModal(true);
        maximize();
        setCloseBinds(new String[]{"engine:frob"});
        setCloseKeys(new int[]{Keyboard.KEY_ESCAPE});

        int screenWidth = Display.getWidth();
        int screenHeight = Display.getHeight();

        int width = 200;
        int height = 80;
        this.setPosition(new Vector2f((screenWidth-width)/2, (screenHeight-height)/2));
        this.setSize(new Vector2f(width, height));

        titleLabel = new UILabel("[text]");
        titleLabel.setVisible(true);
        titleLabel.setHorizontalAlign(EHorizontalAlign.CENTER);
        titleLabel.setVerticalAlign(EVerticalAlign.TOP);

        left = new UIButton(new Vector2f(30, 30), UIButton.ButtonType.NORMAL);
        left.setVisible(true);
        left.setHorizontalAlign(EHorizontalAlign.LEFT);
        left.setVerticalAlign(EVerticalAlign.CENTER);
        left.getLabel().setText("<");
        left.addClickListener(
                new ClickListener() {
                    @Override
                    public void click(UIDisplayElement element, int button) {
                        decrease();
                    }
                });

        time = new UILabel("[value]");
        time.setVisible(true);
        time.setHorizontalAlign(EHorizontalAlign.CENTER);
        time.setVerticalAlign(EVerticalAlign.CENTER);

        right = new UIButton(new Vector2f(30, 30), UIButton.ButtonType.NORMAL);
        right.setVisible(true);
        right.setHorizontalAlign(EHorizontalAlign.RIGHT);
        right.setVerticalAlign(EVerticalAlign.CENTER);
        right.getLabel().setText(">");
        right.addClickListener(
                new ClickListener() {
                    @Override
                    public void click(UIDisplayElement element, int button) {
                        increase();
                    }
                });

        addDisplayElement(titleLabel);
        addDisplayElement(left);
        addDisplayElement(time);
        addDisplayElement(right);

        layout();
    }

    public void attachToEntity(String title, EntityRef blockEntity) {
        this.blockEntity = blockEntity;

        titleLabel.setText(title);
        SignalTimeDelayComponent timeDelay = blockEntity.getComponent(SignalTimeDelayComponent.class);
        if (timeDelay != null) {
            timeMs = timeDelay.delaySetting;
            time.setText(String.valueOf(timeMs)+"ms");
        }
    }

    private void setTime(long timeToSet) {
        timeMs = timeToSet;
        time.setText(String.valueOf(timeMs)+"ms");
        this.blockEntity.send(new SetSignalDelayEvent(timeMs));
    }

    private void increase() {
        setTime(timeMs + DELAY_STEP);
    }

    private void decrease() {
        setTime(Math.max(MINIMUM_DELAY, timeMs-DELAY_STEP));
    }
}
