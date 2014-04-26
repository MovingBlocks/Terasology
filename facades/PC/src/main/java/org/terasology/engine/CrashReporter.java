
package org.terasology.engine;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jpaste.exceptions.PasteException;
import org.jpaste.pastebin.PasteExpireDate;
import org.jpaste.pastebin.Pastebin;
import org.jpaste.pastebin.PastebinLink;
import org.jpaste.pastebin.PastebinPaste;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;

import com.google.common.base.Joiner;

/**
 * Displays a detailed error message and provides some options to communicate with devs.
 * @author Martin Steiger
 */
public class CrashReporter {

    /**
     * Username Terasology
     * eMail pastebin@terasology.org
     */
    private static final String PASTEBIN_DEVELOPER_KEY = "1ed92217030bd6c2570fac91bcbfee78";

    private static final String REPORT_ISSUE_LINK = "https://github.com/MovingBlocks/Terasology/issues/new";
    private static final String JOIN_IRC_LINK = "https://webchat.freenode.net/?channels=terasology";
    
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
                        // ignore
                    }
                    showModalDialog(t, logFileContent);
                    try {
                        UIManager.setLookAndFeel(oldLaF);
                    } catch (Exception e) {
                        // ignore
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
        
        // Replace newline chars. with html newline elements
        String text = exception.toString().replaceAll("\\r?\\n", "<br/>");
        JLabel message = new JLabel("<html><h3>A fatal error occurred</h3><br/>" + text + "</html>");
        mainPanel.add(message);
        message.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        final JTabbedPane mainSettings = new JTabbedPane();

        JTextArea stackTraceArea = new JTextArea();
        stackTraceArea.setText(Joiner.on(System.lineSeparator()).join(exception.getStackTrace()));
        stackTraceArea.setEditable(false);
        stackTraceArea.setCaretPosition(0);
        mainSettings.addTab("StackTrace", new JScrollPane(stackTraceArea));

        final JTextArea logArea = new JTextArea();
        logArea.setText(logFileContent);

        mainSettings.addTab("Logfile", new JScrollPane(logArea));
        mainSettings.setPreferredSize(new Dimension(550, 250));
        mainSettings.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainPanel.add(mainSettings);
        
        mainPanel.add(new JLabel("NOTE: you can edit the content of the log file before uploading"));

        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setLayout(new GridLayout(1, 3, 20, 0));
        JButton pastebinUpload = new JButton("Upload log file to PasteBin");
        pastebinUpload.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent event) {

                String title = "Terasology Error Report";
                PastebinPaste paste = Pastebin.newPaste(PASTEBIN_DEVELOPER_KEY, logArea.getText(), title);
                paste.setPasteFormat("apache"); // Apache Log File Format - this is the closest I could find
//                paste.setPasteExpireDate(PasteExpireDate.ONE_MONTH);
                paste.setPasteExpireDate(PasteExpireDate.TEN_MINUTES);
                uploadPaste(paste);
            }
        });
        buttonPanel.add(pastebinUpload);
        JButton githubIssueButton = new JButton("File an issue on GitHub");
        githubIssueButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                openInBrowser(REPORT_ISSUE_LINK);
            }
        });
        buttonPanel.add(githubIssueButton);
        JButton enterIrc = new JButton("Enter IRC channel");
        enterIrc.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                openInBrowser(JOIN_IRC_LINK);
            }
        });
        buttonPanel.add(enterIrc);

        mainPanel.add(buttonPanel);
        
        Object[] opts = new Object[] { "Close" };
        Object opt = opts[0];
        JOptionPane.showOptionDialog(null, mainPanel, "Fatal Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, opts, opt);
    }

    protected static void uploadPaste(final PastebinPaste paste) {
        final JLabel label = new JLabel("Uploading file - please wait ...");
        label.setPreferredSize(new Dimension(250, 50));

        Runnable runnable = new Runnable() {
            
            @Override
            public void run() {
                String message;
                try {
                    final PastebinLink link = paste.paste();
                    final String url = link.getLink().toString();
                    message = String.format("Paste uploaded to <a href=\"%s\">%s</a>", url, url);
                    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    label.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent e) {
                            openInBrowser(url);
                        };
                    });
                } catch (PasteException e) {
                    message = "Uploading failed: <br/> " + e.getLocalizedMessage();
                }
                
                final String finalMessage = "<html>" + message + "</html>";
                SwingUtilities.invokeLater(new Runnable() {
                    
                    @Override
                    public void run() {
                        label.setText(finalMessage);
                    }
                });
            }
        };
        
        Thread thread = new Thread(runnable, "Upload paste");
        thread.start();

        // TODO: use a resizing JDialog and disable "Close" button while uploading
        JOptionPane.showMessageDialog(null, label, "Pastebin Upload", JOptionPane.INFORMATION_MESSAGE);
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
        
        List<String> lines;
        try {
            String logFile = getLogFilename();

            lines = Files.readAllLines(Paths.get(logFile), Charset.defaultCharset());
            for (String line : lines) {
                builder.append(line);
                builder.append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        
        return builder.toString();
    }

    private static String getLogFilename() {
        String logFile = null;
        
        org.slf4j.Logger logger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        // We try to get log filename directly from the Logback system
        // PathManager only knows the path, and determining the file name is not 
        // straightforward for RollingFileAppenders
        
        if (logger instanceof Logger) {
            Logger logbackLogger = (Logger) logger;
            Iterator<Appender<ILoggingEvent>> it = logbackLogger.iteratorForAppenders();
            while (it.hasNext()) {
                Appender<ILoggingEvent> app = it.next();
                
                if (app instanceof FileAppender) {
                    FileAppender<ILoggingEvent> fileApp = (FileAppender<ILoggingEvent>) app;
                    if (logFile == null) {
                        logFile = fileApp.getFile();
                    } else {
                        System.err.println("Multiple log files found!");
                    }
                }
            }
        } else {
            System.err.println("Logger ist not a Logback logger, but " + logger.getClass().getName());
        }
        
        return logFile;
    }
}
