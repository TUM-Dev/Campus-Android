package de.tum.in.tumcampusapp.api.app;

import androidx.annotation.NonNull;

import java.io.EOFException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.concurrent.TimeUnit;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.platform.Platform;
import okio.Buffer;
import okio.BufferedSource;

import static okhttp3.internal.platform.Platform.INFO;

/**
 * Copied from OkHttp internal classes, as suggested in
 * https://github.com/square/okhttp/issues/5246
 * for OkHttp 4+ support
 * **/
final class HttpHeaders {
    private HttpHeaders() {}

    private static long contentLength(Headers headers) {
        return stringToLong(headers.get("Content-Length"));
    }

    private static long stringToLong(String s) {
        if (s == null) {
            return -1;
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Returns true if the response must have a (possibly 0-length) body. See RFC 7231. */
    public static boolean hasBody(Response response) {
        // HEAD requests never yield a body regardless of the response headers.
        if (response.request().method().equals("HEAD")) {
            return false;
        }

        int responseCode = response.code();
        if ((responseCode < 100 || responseCode >= 200)
            && responseCode != HttpURLConnection.HTTP_NO_CONTENT
            && responseCode != HttpURLConnection.HTTP_NOT_MODIFIED) {
            return true;
        }

        // If the Content-Length or Transfer-Encoding headers disagree with the response code, the
        // response is malformed. For best compatibility, we honor the headers.
        return contentLength(response.headers()) != -1
               || "chunked".equalsIgnoreCase(response.header("Transfer-Encoding"));
    }
}
public final class TumHttpLoggingInterceptor implements Interceptor {

    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final boolean LOG_BODY = true;
    private static final boolean LOG_HEADERS = true;
    private final Logger logger;

    public interface Logger {
        void log(String message);

        /**
         * A {@link Logger} defaults output appropriate for the current platform.
         */
        Logger DEFAULT = message -> Platform.get()
                                            .log(message, INFO, null);
    }

    public TumHttpLoggingInterceptor(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();

        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;

        Connection connection = chain.connection();
        Protocol protocol = connection == null ? Protocol.HTTP_1_1 : connection.protocol();
        String requestStartMessage = "--> " + request.method() + ' ' + request.url() + ' ' + protocol;
        if (!LOG_HEADERS && hasRequestBody) {
            requestStartMessage += " (" + requestBody.contentLength() + "-byte body)";
        }
        logger.log(requestStartMessage);

        if (LOG_HEADERS) {
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody.contentType() != null) {
                    logger.log("Content-Type: " + requestBody.contentType());
                }
                if (requestBody.contentLength() != -1) {
                    logger.log("Content-Length: " + requestBody.contentLength());
                }
            }

            Headers headers = request.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                String name = headers.name(i);
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                    logger.log(name + ": " + headers.value(i));
                }
            }

            if (LOG_BODY && hasRequestBody) {
                if (bodyEncoded(request.headers())) {
                    logger.log("--> END " + request.method() + " (encoded body omitted)");
                } else {
                    Buffer buffer = new Buffer();
                    requestBody.writeTo(buffer);

                    Charset charset = UTF8;
                    MediaType contentType = requestBody.contentType();
                    if (contentType != null) {
                        charset = contentType.charset(UTF8);
                    }

                    logger.log("");
                    if (isPlaintext(buffer)) {
                        assert charset != null;
                        logger.log(buffer.readString(charset));
                        logger.log("--> END " + request.method()
                                   + " (" + requestBody.contentLength() + "-byte body)");
                    } else {
                        logger.log("--> END " + request.method() + " (binary "
                                   + requestBody.contentLength() + "-byte body omitted)");
                    }
                }
            } else {
                logger.log("--> END " + request.method());
            }
        }

        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            logger.log("<-- HTTP FAILED: " + e);
            throw e;
        }
        long startNs = System.nanoTime();
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();
        assert responseBody != null;
        long contentLength = responseBody.contentLength();
        String bodySize = contentLength == -1 ? "unknown-length" : contentLength + "-byte";
        logger.log(String.format("<-- %d %s %s (%dms%s)", response.code(), response.message(), response.request().url(), tookMs,
                                 LOG_HEADERS ? "" : ", " + bodySize + " body"));

        if (LOG_HEADERS) {
            Headers headers = response.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                logger.log(headers.name(i) + ": " + headers.value(i));
            }

            if (LOG_BODY && HttpHeaders.hasBody(response)) {
                if (bodyEncoded(response.headers())) {
                    logger.log("<-- END HTTP (encoded body omitted)");
                } else {
                    BufferedSource source = responseBody.source();
                    source.request(Long.MAX_VALUE); // Buffer the entire body.

                    Charset charset = UTF8;
                    MediaType contentType = responseBody.contentType();
                    if (contentType != null) {
                        try {
                            charset = contentType.charset(UTF8);
                        } catch (UnsupportedCharsetException e) {
                            logger.log("");
                            logger.log("Couldn't decode the response body; charset is likely malformed.");
                            logger.log("<-- END HTTP");

                            return response;
                        }
                    }

                    Buffer buffer = source.getBuffer();
                    if (!isPlaintext(buffer)) {
                        logger.log("");
                        logger.log("<-- END HTTP (binary " + buffer.size() + "-byte body omitted)");
                        return response;
                    }

                    if (contentLength != 0) {
                        logger.log("");
                        logger.log(buffer.clone()
                                         .readString(charset));
                    }

                    logger.log("<-- END HTTP (" + buffer.size() + "-byte body)");
                }
            } else {
                logger.log("<-- END HTTP");
            }
        }

        return response;
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    private static boolean isPlaintext(Buffer buffer) throws EOFException {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

    private static boolean bodyEncoded(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null && !contentEncoding.equalsIgnoreCase("identity");
    }
}
