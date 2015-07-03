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

package org.terasology.engine.splash;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Queue;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.google.common.collect.Queues;

/**
 * The actual implementation of {@link SplashScreenCore} that
 * uses a Swing timer to animate through a list of messages.
 * Each message is shown at least <code>n</code> frames.
 * @author Martin Steiger
 */
final class SplashScreenImpl implements SplashScreenCore {

    /**
     * In milli-seconds
     */
    private final int updateFreq = 100;

    /**
     * In frames
     */
    private final int msgUpdateFreq = 3;

    private final Timer timer;

    private final Font font = new Font("Serif", Font.BOLD, 14);

    private int frame;

    private final Queue<String> messageQueue = Queues.newConcurrentLinkedQueue();

    public SplashScreenImpl() {
        ActionListener action = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SplashScreen splashScreen = SplashScreen.getSplashScreen();

                if (splashScreen != null) {
                    update(splashScreen);
                } else {
                    timer.stop();
                }
            }
        };
        timer = new Timer(updateFreq, action);
        timer.setInitialDelay(0);
        timer.start();
    }

    @Override
    public void post(String message) {
        messageQueue.add(message);
    }

    @Override
    public void close() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                SplashScreen splashScreen = SplashScreen.getSplashScreen();

                if (splashScreen != null) {
                    splashScreen.close();
                }
            }
        });
    }

    private void update(SplashScreen splashScreen) {
        frame++;

        if (frame % msgUpdateFreq == 0 && messageQueue.size() > 1) {
            messageQueue.poll();
        }

        String message = messageQueue.peek();

        Rectangle rc = splashScreen.getBounds();
        Graphics2D g = splashScreen.createGraphics();
        try {
            repaint(g, rc, message);
            splashScreen.update();
        } finally {
            g.dispose();
        }
    }

    private void repaint(Graphics2D g, Rectangle rc, String text) {
        int width = 600;
        int height = 30;
        int maxTextWidth = 450;

        Color textShadowColor = new Color(224, 224, 224);

        g.setFont(font);
        g.translate(10, rc.height - height - 10);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.GRAY);
        g.drawRect(0, 0, width, height);

        if (text != null) {
            FontMetrics fm = g.getFontMetrics();

            String printedText = truncateToMax(fm, text, maxTextWidth);
            int asc = g.getFontMetrics().getAscent();
            int shadowOff = 1;

            // draw shadow first
            g.setColor(textShadowColor);
            g.drawString(printedText, 10 + shadowOff, 5 + shadowOff + asc);

            // and actual text on top
            g.setColor(Color.BLACK);
            g.drawString(printedText, 10, 5 + asc);
        }

        Rectangle boxRc = new Rectangle(20 + maxTextWidth, 0, width - maxTextWidth - 30, height);
        drawBoxes(g, boxRc);
    }

    private String truncateToMax(FontMetrics fm, String text, int maxTextWidth) {
        if (text.length() < 2) {
            return text;
        }

        int texLen = text.length();
        char[] data = new char[texLen];
        text.getChars(0, texLen, data, 0);

        for (int len = 2; len <= texLen; len++) {
            if (fm.charsWidth(data, 0, len) >= maxTextWidth) {
                return text.substring(0, len - 2) + "..";
            }
        }

        return text;
    }

    private void drawBoxes(Graphics2D g, Rectangle rc) {

        int boxCount = 7;
        int boxHeight = 18;
        int boxWidth = 10;
        int space = 8;
        int dx = boxWidth + space;
        int left = rc.x + rc.width - boxCount * dx + space;

        double animSpeed = 0.05;

        for (int i = 0; i < boxCount; i++) {
            float sat = (float) Math.sin((frame - i) * Math.PI * animSpeed);
            sat = sat * sat;
            float hue = 0.6f;
            float bright = 1.0f;
            int rgb = Color.HSBtoRGB(hue, sat, bright);
            Color animColor = new Color(rgb);

            int sizeDiff = (int) Math.abs(1.0 - 2 * sat) * 2;
            int x = left + i * dx - sizeDiff / 2;
            int y = rc.y + (rc.height - boxHeight - sizeDiff) / 2;

            g.setColor(animColor);
            g.fillRect(x, y, boxWidth + sizeDiff, boxHeight + sizeDiff);

            g.setColor(Color.BLACK);
            g.drawRect(x, y, boxWidth + sizeDiff, boxHeight + sizeDiff);
        }
    }
}
