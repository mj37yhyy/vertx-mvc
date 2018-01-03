package org.mermaid.vertxmvc;

import org.mermaid.vertxmvc.converters.WebDataBinder;

public interface VertxMvcAppConfig {
    void initBinder(WebDataBinder binder);
}
