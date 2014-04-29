/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.engine;

import com.google.common.base.Joiner;
import org.jpaste.exceptions.PasteException;
import org.jpaste.pastebin.PasteExpireDate;
import org.jpaste.pastebin.Pastebin;
import org.jpaste.pastebin.PastebinLink;
import org.jpaste.pastebin.PastebinPaste;
import org.terasology.engine.paths.PathManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Displays a detailed error message and provides some options to communicate with devs.
 * Errors are reported to {@link System#err}
 *
 * @author Martin Steiger
 */
public final class CrashReporter {

    /**
     * Username Terasology
     * eMail pastebin@terasology.org
     */
    private static final String PASTEBIN_DEVELOPER_KEY = "1ed92217030bd6c2570fac91bcbfee78";

    private static final String REPORT_ISSUE_LINK = "https://github.com/MovingBlocks/Terasology/issues/new";
    private static final String JOIN_IRC_LINK = "https://webchat.freenode.net/?channels=terasology";

    private CrashReporter() {
        // don't create any instances
    }

    public static void report(final Throwable t) {

        // Swing element methods must be called in the swing thread
        try {
            final String logFileContent = getLogFileContent();

            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    LookAndFeel oldLaF = UIManager.getLookAndFeel();
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                    showModalDialog(t, logFileContent);
                    try {
                        UIManager.setLookAndFeel(oldLaF);
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }

    private static void showModalDialog(Throwable exception, final String logFileContent) {

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Replace newline chars. with html newline elements (not needed in most cases)
        String text = exception.toString().replaceAll("\\r?\\n", "<br/>");
        JLabel message = new JLabel("<html><h3>A fatal error occurred</h3><br/>" + text + "</html>");
        mainPanel.add(message);
        message.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Tab pane
        final JTabbedPane tabPane = new JTabbedPane();
        tabPane.setPreferredSize(new Dimension(600, 250));
        tabPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        // StackTrace tab 
        JTextArea stackTraceArea = new JTextArea();
        stackTraceArea.setText(Joiner.on(System.lineSeparator()).join(exception.getStackTrace()));
        stackTraceArea.setEditable(false);
        stackTraceArea.setCaretPosition(0);
        tabPane.addTab("StackTrace", new JScrollPane(stackTraceArea));

        // Logfile tab
        final JTextArea logArea = new JTextArea();
        logArea.setText(logFileContent);
        tabPane.addTab("Logfile", new JScrollPane(logArea));

        mainPanel.add(tabPane);
        mainPanel.add(new JLabel("NOTE: you can edit the content of the log file before uploading"));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setLayout(new GridLayout(1, 3, 20, 0));
        final JButton pastebinUpload = new JButton("Upload log file to PasteBin");
        pastebinUpload.setIcon(loadIcon("icons/pastebin.png"));
        pastebinUpload.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {

                String title = "Terasology Error Report";
                PastebinPaste paste = Pastebin.newPaste(PASTEBIN_DEVELOPER_KEY, logArea.getText(), title);
                paste.setPasteFormat("apache"); // Apache Log File Format - this is the closest I could find
                paste.setPasteExpireDate(PasteExpireDate.ONE_MONTH);
                uploadPaste(paste);
            }
        });
        // disable upload if log area text field is empty
        logArea.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }

            private void update() {
                pastebinUpload.setEnabled(!logArea.getText().isEmpty());
            }
        });
        pastebinUpload.setEnabled(!logArea.getText().isEmpty());        // initial update of the button

        buttonPanel.add(pastebinUpload);
        JButton githubIssueButton = new JButton("File an issue on GitHub");
        githubIssueButton.setIcon(loadIcon("icons/github.png"));
        githubIssueButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                openInBrowser(REPORT_ISSUE_LINK);
            }
        });
        buttonPanel.add(githubIssueButton);
        JButton enterIrc = new JButton("Enter IRC channel");
        enterIrc.setIcon(loadIcon("icons/irc.png"));
        enterIrc.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                openInBrowser(JOIN_IRC_LINK);
            }
        });
        buttonPanel.add(enterIrc);

        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Custom close button
        JButton closeButton = new JButton("Close", loadIcon("icons/close.png"));

        showDialog(mainPanel, closeButton, "Fatal Error", JOptionPane.ERROR_MESSAGE);
    }

    private static void showDialog(Component mainPanel, JButton closeButton, String title, int messageType) {
        Object[] opts = new Object[]{closeButton};

        // The error-message pane
        final JOptionPane pane = new JOptionPane(mainPanel, messageType, JOptionPane.DEFAULT_OPTION, null, opts, opts[0]);
        closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // calling setValue() closes the dialog
                pane.setValue("CLOSE"); // the actual value doesn't matter
            }
        });

        // wrap it all in a dialog
        JDialog dialog = pane.createDialog(null, title);
//        dialog.setResizable(true);      // disabled by default
        dialog.setVisible(true);
        dialog.dispose();
    }

    private static Icon loadIcon(String fname) {
        try {
            String fullPath = "/" + fname;
            URL rsc = CrashReporter.class.getResource(fullPath);
            if (rsc == null) {
                throw new FileNotFoundException(fullPath);
            }
            BufferedImage image = ImageIO.read(rsc);
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }

    protected static void uploadPaste(final PastebinPaste paste) {
        final JLabel label = new JLabel("Uploading file - please wait ...");
        label.setPreferredSize(new Dimension(250, 50));

        final JButton closeButton = new JButton("Close", loadIcon("icons/close.png"));
        closeButton.setEnabled(false);

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    final PastebinLink link = paste.paste();

                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            closeButton.setEnabled(true);
                            final String url = link.getLink().toString();
                            label.setText(String.format("<html>Paste uploaded to <a href=\"%s\">%s</a></html>", url, url));
                            label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                            label.addMouseListener(new MouseAdapter() {
                                public void mouseClicked(java.awt.event.MouseEvent e) {
                                    openInBrowser(url);
                                }

                                ;
                            });
                        }
                    });
                } catch (final PasteException e) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            closeButton.setEnabled(true);
                            label.setText("<html>Upload failed: <br/> " + e.getLocalizedMessage() + "</html>");
                        }
                    });
                }
            }
        };

        Thread thread = new Thread(runnable, "Upload paste");
        thread.start();

        showDialog(label, closeButton, "Pastebin Upload", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void openInBrowser(String url) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();

            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(new URI(url));
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace(System.err);
                }
            }
        }
    }

    private static String getLogFileContent() {
        StringBuilder builder = new StringBuilder();

        try {
            Path logDirectory = PathManager.getInstance().getLogPath();
            if (logDirectory != null) {
                Path logPath = PathManager.getInstance().getLogPath().resolve("Terasology.log");
                if (Files.exists(logPath)) {
                    for (String line : Files.readAllLines(logPath, Charset.defaultCharset())) {
                        builder.append(line);
                        builder.append(System.lineSeparator());
                    }
                }
            }
        } catch (Exception e) {
            // we catch all here, because we want to continue execution in all cases 
            e.printStackTrace(System.err);
        }

        return builder.toString();
    }
}
