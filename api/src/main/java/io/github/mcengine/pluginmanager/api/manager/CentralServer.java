package io.github.mcengine.pluginmanager.api.manager;

import java.net.URI;
import java.util.Objects;

/**
 * One central server this plugin talks to, and the token it presents.
 *
 * <p>A server may be configured with several of these. They are tried in the
 * order they appear in {@code config.yml}, so the first one that can answer for
 * a plugin is the one used — which is what lets a private catalogue take
 * precedence over a public one without either knowing about the other.</p>
 *
 * @param name     a label for logs and command output; unique within the config
 * @param baseUrl  the service root, without a trailing slash
 * @param token    the API token, presented as a bearer credential
 * @param priority lower is tried first
 */
public record CentralServer(String name, URI baseUrl, String token, int priority) {

    /**
     * @throws IllegalArgumentException if the URL is not absolute, or is not
     *                                  {@code https} outside of localhost
     */
    public CentralServer {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(baseUrl, "baseUrl");
        Objects.requireNonNull(token, "token");

        if (name.isBlank()) {
            throw new IllegalArgumentException("A central server needs a name.");
        }
        if (!baseUrl.isAbsolute()) {
            throw new IllegalArgumentException("A central server URL must be absolute: " + baseUrl);
        }

        String scheme = baseUrl.getScheme();
        String host = baseUrl.getHost() == null ? "" : baseUrl.getHost();
        boolean local = host.equals("localhost") || host.equals("127.0.0.1") || host.equals("::1");

        // This plugin sends a bearer token and receives code it will install.
        // Over http, both are readable and alterable by anything on the path.
        // localhost is exempt because there is no path.
        if (!"https".equalsIgnoreCase(scheme) && !local) {
            throw new IllegalArgumentException(
                "A central server must use https (except on localhost): " + baseUrl);
        }
        if (token.isBlank()) {
            throw new IllegalArgumentException("A central server needs a token: " + name);
        }
    }

    /** Resolves a path against this server's root. */
    public URI resolve(String path) {
        String root = baseUrl.toString();
        if (root.endsWith("/")) {
            root = root.substring(0, root.length() - 1);
        }
        return URI.create(root + (path.startsWith("/") ? path : "/" + path));
    }

    /**
     * A description safe to put in a log line or a command response.
     *
     * <p>{@link #toString()} on a record prints every component, and one of them
     * is a credential. This is what any message about a server should use.</p>
     */
    public String describe() {
        return name + " (" + baseUrl + ")";
    }
}
