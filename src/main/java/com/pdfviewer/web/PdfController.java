package com.pdfviewer.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class PdfController {

    private final Path allowedRoot;

    public PdfController(@Value("${pdf.root:}") String pdfRoot) throws IOException {
        if (pdfRoot == null || pdfRoot.isBlank()) {
            this.allowedRoot = null;
        } else {
            this.allowedRoot = new File(pdfRoot.trim()).getCanonicalFile().toPath();
        }
    }

    @GetMapping("/base-url")
    public Map<String, String> baseUrl(HttpServletRequest req) {
        return Map.of("baseUrl", buildBaseUrl(req));
    }

    @GetMapping(value = "/open-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> openPdf(
            @RequestParam(value = "dir", required = false) String pdfDirectory,
            @RequestParam(value = "name", required = false) String fileName) throws IOException {

        if (pdfDirectory == null || fileName == null || fileName.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        fileName = new File(fileName).getName();
        if (fileName.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        pdfDirectory = sanitizePath(pdfDirectory);
        File pdfFile = new File(pdfDirectory, fileName);
        if (!pdfFile.isFile()) {
            return ResponseEntity.notFound().build();
        }

        Path canonicalFile = pdfFile.getCanonicalFile().toPath();
        if (allowedRoot != null && !canonicalFile.startsWith(allowedRoot)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        InputStream in = Files.newInputStream(canonicalFile);
        InputStreamResource body = new InputStreamResource(in);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName.replace("\"", "") + "\"")
                .contentLength(pdfFile.length())
                .body(body);
    }

    private static String buildBaseUrl(HttpServletRequest req) {
        String scheme = req.getScheme();
        String serverName = req.getServerName();
        int serverPort = req.getServerPort();
        String contextPath = req.getContextPath();
        if (contextPath == null) {
            contextPath = "";
        }
        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && serverPort == 80)
                || ("https".equalsIgnoreCase(scheme) && serverPort == 443);
        if (defaultPort) {
            return String.format("%s://%s%s", scheme, serverName, contextPath);
        }
        return String.format("%s://%s:%d%s", scheme, serverName, serverPort, contextPath);
    }

    private static String sanitizePath(String path) {
        if (path == null) {
            return "";
        }
        try {
            return new File(path).getCanonicalPath();
        } catch (IOException e) {
            return path;
        }
    }
}
