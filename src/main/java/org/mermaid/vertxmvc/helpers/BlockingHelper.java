package org.mermaid.vertxmvc.helpers;

import org.mermaid.vertxmvc.VertxMvc;

public class BlockingHelper {

	public static void executeBlocking(
			BlockingCodeDataHandler blockingCodeDataHandler,
			BlockingResultHandler blockingResultHandler) {

		VertxMvc.getVertx().executeBlocking(
				future -> future.complete(blockingCodeDataHandler.handle()),
				res -> blockingResultHandler.handle(res.result()));
	}

	public static void executeBlocking(
			BlockingCodeHandler blockingCodeHandler) {

		VertxMvc.getVertx().executeBlocking(
				future -> blockingCodeHandler.handle(),
				res -> {});
	}

	public interface BlockingCodeDataHandler<T> {
		T handle();
	}

	public interface BlockingCodeHandler<T> {
		void handle();
	}

	public interface BlockingResultHandler<T> {
		void handle(T t);
	}
}
