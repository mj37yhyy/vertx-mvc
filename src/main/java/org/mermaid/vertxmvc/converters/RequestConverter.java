package org.mermaid.vertxmvc.converters;

import io.vertx.reactivex.core.buffer.Buffer;

public interface RequestConverter {

    <T> T convert(Buffer buffer, Class<T> clazz);

}
