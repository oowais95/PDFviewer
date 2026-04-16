package com.pdf;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Opens a link to a PDF served by this web application in the default system browser.
 * Intended for desktop-side use (e.g. from a Swing/JavaFX client or a utility main).
 *
 * @param link Query string to append after {@code /open-pdf}, e.g. {@code ?dir=C:\docs&name=file.pdf}
 */
public class OpenPdf {

    private static final Logger logger = Logger.getLogger(OpenPdf.class.getName());

    /**
     * @param link The path and query for the PDF servlet (e.g. {@code ?dir=...&name=...}).
     *             If it does not start with {@code ?}, a leading {@code ?} is added.
     */
    public void openLink(String link) {
        String ipAddress;
        String httpPort;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            ipAddress = localHost.getHostAddress();
            httpPort = System.getProperty("jboss.http.port", "8081");
            String query = link == null ? "" : link.trim();
            if (!query.isEmpty() && !query.startsWith("?")) {
                query = "?" + query;
            }
            String servletUrl = "http://" + ipAddress + ":" + httpPort + "/pdfViewerWebApp/open-pdf" + query;

            String os = System.getProperty("os.name", "").toLowerCase();
            String command;
            if (os.contains("win")) {
                command = "rundll32 url.dll,FileProtocolHandler " + servletUrl;
            } else if (os.contains("mac")) {
                command = "open " + servletUrl;
            } else if (os.contains("nix") || os.contains("nux")) {
                command = "xdg-open " + servletUrl;
            } else {
                throw new UnsupportedOperationException("Unsupported OS: " + os);
            }
            Runtime.getRuntime().exec(command);
            logger.info("Executed command: " + command);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error opening URL: " + e.getMessage(), e);
        }
    }

    /**
     * Optional entry point for manual testing (the WAR does not use this; Jetty/servlet container runs the web app).
     * <p>
     * Usage: {@code java -cp target/classes com.pdf.OpenPdf <dir> <fileName>}
     * </p>
     * If the server listens on a port other than 8081, set {@code -Djboss.http.port=8081} (must match the app).
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java com.pdf.OpenPdf <directory> <pdfFileName>");
            System.err.println("Example: java com.pdf.OpenPdf C:\\\\Docs report.pdf");
            System.err.println("Set -Djboss.http.port=<port> if the server is not on 8081.");
            System.exit(1);
        }
        String q = "?dir=" + URLEncoder.encode(args[0], StandardCharsets.UTF_8)
                + "&name=" + URLEncoder.encode(args[1], StandardCharsets.UTF_8);
        new OpenPdf().openLink(q);
    }
}
