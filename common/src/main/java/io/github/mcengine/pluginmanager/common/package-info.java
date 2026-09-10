/**
 * The implementation behind the shared contract in
 * {@code io.github.mcengine.pluginmanager.api}.
 *
 * <p>Unlike the API package this one is unrestricted: classes here may be public,
 * package-private, or final, may hold mutable state, and may take dependencies
 * the API package refuses. What they must not do is leak: nothing outside this
 * module holds a type from this package, because every platform reaches the
 * implementation through
 * {@link io.github.mcengine.pluginmanager.MCPluginManagerProvider} instead.</p>
 *
 * <p>That facade sits one package up, at the root of the namespace rather than
 * inside this package, so a developer opening the source tree finds the single
 * entry point before they find anything they should not depend on.</p>
 */
package io.github.mcengine.pluginmanager.common;
