package org.mermaid.vertxmvc;

import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.EventBus;

public class VertxMvc {

    private static Vertx vertx;
    private static EventBus eventBus;

    static void setVertx(Vertx vertx) {
        VertxMvc.vertx = vertx;
    }

    static void setEventBus(EventBus eventBus) {
        VertxMvc.eventBus = eventBus;
    }

    public static Vertx getVertx() {
        return vertx;
    }

    public static EventBus getEventBus() {
        return eventBus;
    }
}
