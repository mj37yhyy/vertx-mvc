package org.mermaid.vertxmvc.converters;

import org.mermaid.vertxmvc.utils.JsonBinder;

public class JsonResponseConverter implements ResponseConverter {

	@Override
	public String convert(Object obj) {
		return binder.toJson(obj);
	}

	private JsonBinder binder = JsonBinder.buildNormalBinder(false);
}
