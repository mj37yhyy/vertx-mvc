package org.mermaid.vertxmvc.converters;

import io.vertx.reactivex.core.buffer.Buffer;
import org.mermaid.vertxmvc.utils.JsonBinder;

public class JsonRequestConverter implements RequestConverter {
    @Override
    public <T> T convert(Buffer buffer, Class<T> clazz) {
        return binder.fromJson(buffer.toString(), clazz);
    }
    private JsonBinder binder = JsonBinder.buildNormalBinder(false);
}
