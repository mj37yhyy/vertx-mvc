package org.mermaid.vertxmvc.converters;

public interface Converter<T> {

    T convert(String text);
}
