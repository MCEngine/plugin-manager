package io.github.mcengine.universal.api;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Dispatches {@link TemplateAction} so implementations do not each write the
 * same switch.
 *
 * <p>{@link #handle(TemplateRequest)} is final and exhaustive over the enum. The
 * switch has no {@code default} branch on purpose: adding a constant to
 * {@link TemplateAction} then fails to compile here until a handler exists for
 * it, which is the behaviour you want from a protocol shared by four platforms.
 * A {@code default} would turn that compile error into a runtime surprise on
 * whichever platform received the new action first.</p>
 */
public abstract class AbstractTemplateService implements TemplateService {

    /**
     * Routes a request to the handler for its action.
     *
     * @param request The action to perform.
     * @return A future carrying the server's answer.
     */
    @Override
    public final CompletableFuture<TemplateResponse> handle(TemplateRequest request) {
        Objects.requireNonNull(request, "request cannot be null");
        return switch (request.action()) {
            case PING -> ping(request);
            case GREET -> greet(request);
        };
    }

    /**
     * Answers a {@link TemplateAction#PING}.
     *
     * @param request The originating request.
     * @return A future carrying the answer.
     */
    protected abstract CompletableFuture<TemplateResponse> ping(TemplateRequest request);

    /**
     * Answers a {@link TemplateAction#GREET}.
     *
     * @param request The originating request.
     * @return A future carrying the answer.
     */
    protected abstract CompletableFuture<TemplateResponse> greet(TemplateRequest request);
}
