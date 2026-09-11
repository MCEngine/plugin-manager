package io.github.mcengine.pluginmanager.common.manager;

import java.util.Comparator;
import java.util.Locale;

/**
 * Comparing plugin versions.
 *
 * <p>Compared as text, {@code 1.9.0} sorts above {@code 1.10.0} — so a plugin
 * comparing versions that way updates <em>backwards</em>. Each numeric segment
 * is therefore compared as a number, which is the same rule
 * {@code product_versions.version_norm} encodes on the server side; the two must
 * agree or the server will offer an update this plugin then refuses.</p>
 */
public final class Versions {

    private Versions() {
    }

    /** Orders two versions: negative when {@code a} is older. */
    public static final Comparator<String> COMPARATOR = Versions::compare;

    /**
     * Compares two version strings.
     *
     * <p>A pre-release suffix makes a version <em>older</em> than the same
     * version without one, per semantic versioning — {@code 1.0.0-beta} comes
     * before {@code 1.0.0}.</p>
     *
     * @param a one version
     * @param b the other
     * @return negative, zero or positive
     */
    public static int compare(String a, String b) {
        String coreA = core(a);
        String coreB = core(b);

        String[] partsA = coreA.split("\\.");
        String[] partsB = coreB.split("\\.");
        int length = Math.max(partsA.length, partsB.length);

        for (int i = 0; i < length; i++) {
            long valueA = segment(partsA, i);
            long valueB = segment(partsB, i);
            if (valueA != valueB) {
                return valueA < valueB ? -1 : 1;
            }
        }

        String suffixA = suffix(a);
        String suffixB = suffix(b);
        if (suffixA.equals(suffixB)) {
            return 0;
        }
        // An empty suffix is the released version, and it is newer than any
        // pre-release of the same core.
        if (suffixA.isEmpty()) {
            return 1;
        }
        if (suffixB.isEmpty()) {
            return -1;
        }
        return suffixA.compareTo(suffixB);
    }

    /** True when {@code candidate} is strictly newer than {@code installed}. */
    public static boolean isNewer(String candidate, String installed) {
        if (candidate == null) {
            return false;
        }
        if (installed == null) {
            return true;
        }
        return compare(candidate, installed) > 0;
    }

    private static String normalize(String version) {
        if (version == null) {
            return "";
        }
        String trimmed = version.trim().toLowerCase(Locale.ROOT);
        return trimmed.startsWith("v") ? trimmed.substring(1) : trimmed;
    }

    private static String core(String version) {
        String normalized = normalize(version);
        int dash = normalized.indexOf('-');
        return dash < 0 ? normalized : normalized.substring(0, dash);
    }

    private static String suffix(String version) {
        String normalized = normalize(version);
        int dash = normalized.indexOf('-');
        return dash < 0 ? "" : normalized.substring(dash + 1);
    }

    private static long segment(String[] parts, int index) {
        if (index >= parts.length) {
            return 0;
        }
        // A non-numeric segment counts as zero rather than throwing: a version
        // this plugin cannot parse should not stop it managing every other one.
        try {
            return Long.parseLong(parts[index].replaceAll("[^0-9]", "").isEmpty()
                ? "0"
                : parts[index].replaceAll("[^0-9]", ""));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
