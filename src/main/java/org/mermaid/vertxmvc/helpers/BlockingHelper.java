package org.mermaid.vertxmvc.helpers;

import org.mermaid.vertxmvc.VertxMvc;

public class BlockingHelper {

	public static void executeBlocking(
			BlockingCodeHandler blockingCodeHandler,
			BlockingResultHandler blockingResultHandler) {

		VertxMvc.getVertx().executeBlocking(
				future -> future.complete(blockingCodeHandler.handle()),
				res -> blockingResultHandler.handle(res.result()));
	}

	public interface BlockingCodeHandler<T> {
		T handle();
	}

	public interface BlockingResultHandler<T> {
		void handle(T t);
	}
}
