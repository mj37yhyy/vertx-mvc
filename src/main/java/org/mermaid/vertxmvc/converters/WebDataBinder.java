package org.mermaid.vertxmvc.converters;

import java.util.HashMap;
import java.util.Map;

public class WebDataBinder {

	public void registerCustomConverter(
			Class clazz,
			Converter converter) {
		customConverter.put(clazz, converter);
	}

	public static Map<Class, Converter> getCustomConverter() {
		return customConverter;
	}

	private final static Map<Class, Converter> customConverter = new HashMap<>();
}
