package strategy;

import interfaces.HttpResponseBase;
import interfaces.IHttpRequest;
import interfaces.IRequestTask;
import interfaces.IResourceRoute;
import protocol.HttpResponseFactory;
import protocol.HttpStatusCode;
import protocol.Protocol;

public class NoRouteStrategy extends ResourceStrategyBase {
	@Override
	public IRequestTask prepareEvaluation(IHttpRequest request,
			IResourceRoute fromRoute) {
		return new NoRouteTask(request);
	}

	class NoRouteTask extends RequestTaskBase {

		public NoRouteTask(IHttpRequest request) {
			super(request);
		}
		
		@Override
		public void run() {
			completed = true;
			super.run();
		}

		@Override
		public HttpResponseBase getResponse() {
			return HttpResponseFactory.createGenericErrorResponse(
					HttpStatusCode.NOT_FOUND, Protocol.CLOSE);
		}

	}
}
