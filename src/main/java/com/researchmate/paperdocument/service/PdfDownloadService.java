package com.researchmate.paperdocument.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

@Service
public class PdfDownloadService {

    private static final Set<String> ALLOWED_HOSTS = Set.of(
            "arxiv.org",
            "export.arxiv.org"
    );

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 30_000;
    private static final int MAX_REDIRECTS = 3;

    @Value("${researchmate.documents.temp-directory}")
    private String tempDirectory;

    @Value("${researchmate.documents.max-pdf-size-bytes}")
    private long maxPdfSize;

    public Path downloadPdf(String pdfUrl) throws IOException {
        Path directory = Path.of(tempDirectory);
        Files.createDirectories(directory);

        Path tempFile = Files.createTempFile(directory, "paper-", ".pdf");
        boolean successful = false;

        try {
            downloadToFile(pdfUrl, tempFile);
            successful = true;
            return tempFile;
        } finally {
            if (!successful) {
                Files.deleteIfExists(tempFile);
            }
        }
    }

    private void downloadToFile(String initialUrl, Path destination)
            throws IOException {

        String currentUrl = initialUrl;

        for (int redirectCount = 0; redirectCount <= MAX_REDIRECTS;
             redirectCount++) {

            URI uri = validateUrl(currentUrl);
            URL url = uri.toURL();

            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty(
                    "User-Agent", "ResearchMate/1.0"
            );
            connection.setRequestProperty(
                    "Accept", "application/pdf"
            );

            try {
                int status = connection.getResponseCode();

                if (status == 301 || status == 302
                        || status == 303 || status == 307
                        || status == 308) {

                    String location = connection.getHeaderField("Location");

                    if (location == null || redirectCount == MAX_REDIRECTS) {
                        throw new IOException(
                                "Invalid or excessive PDF redirects"
                        );
                    }

                    currentUrl = uri.resolve(location).toString();
                    continue;
                }

                if (status != HttpURLConnection.HTTP_OK) {
                    throw new IOException(
                            "PDF download failed with HTTP status " + status
                    );
                }

                long contentLength = connection.getContentLengthLong();

                if (contentLength > maxPdfSize) {
                    throw new IOException("PDF exceeds the maximum allowed size");
                }

                String contentType = connection.getContentType();

                if (contentType != null
                        && !contentType.toLowerCase(Locale.ROOT)
                        .contains("pdf")
                        && !contentType.toLowerCase(Locale.ROOT)
                        .contains("octet-stream")) {
                    throw new IOException("URL did not return a PDF response");
                }

                try (InputStream input = connection.getInputStream();
                     OutputStream output = Files.newOutputStream(destination)) {

                    byte[] buffer = new byte[8192];
                    long totalBytes = 0;
                    int bytesRead;
                    byte[] signature = new byte[5];
                    int signatureLength = 0;

                    while ((bytesRead = input.read(buffer)) != -1) {
                        totalBytes += bytesRead;

                        if (totalBytes > maxPdfSize) {
                            throw new IOException(
                                    "PDF exceeds the maximum allowed size"
                            );
                        }

                        if (signatureLength < signature.length) {
                            int copyLength = Math.min(
                                    bytesRead,
                                    signature.length - signatureLength
                            );

                            System.arraycopy(
                                    buffer, 0, signature,
                                    signatureLength, copyLength
                            );
                            signatureLength += copyLength;
                        }

                        output.write(buffer, 0, bytesRead);
                    }

                    if (signatureLength < 5
                            || signature[0] != '%'
                            || signature[1] != 'P'
                            || signature[2] != 'D'
                            || signature[3] != 'F'
                            || signature[4] != '-') {
                        throw new IOException(
                                "Downloaded file is not a valid PDF"
                        );
                    }
                }

                return;
            } finally {
                connection.disconnect();
            }
        }

        throw new IOException("PDF download failed");
    }

    private URI validateUrl(String rawUrl) throws IOException {
        try {
            URI uri = URI.create(rawUrl).normalize();

            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || uri.getHost() == null
                    || uri.getUserInfo() != null
                    || uri.getPort() != -1 && uri.getPort() != 443) {
                throw new IOException("Only valid HTTPS PDF URLs are allowed");
            }

            String host = uri.getHost().toLowerCase(Locale.ROOT);

            if (!ALLOWED_HOSTS.contains(host)) {
                throw new IOException("PDF host is not allowed: " + host);
            }

            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (address.isAnyLocalAddress()
                        || address.isLoopbackAddress()
                        || address.isLinkLocalAddress()
                        || address.isSiteLocalAddress()
                        || address.isMulticastAddress()) {
                    throw new IOException(
                            "PDF host resolved to a non-public address"
                    );
                }
            }

            return uri;
        } catch (IllegalArgumentException exception) {
            throw new IOException("Invalid PDF URL", exception);
        }
    }
}