package strategy;

import protocol.HttpResponseFactory;
import protocol.HttpStatusCode;
import protocol.Protocol;
import interfaces.HttpResponseBase;
import interfaces.IRequestTask;
import request.HTTPRequest;
import configuration.ResourceStrategyRoute;

public class NoRouteStrategy extends ResourceStrategyBase {
	@Override
	public IRequestTask prepareEvaluation(HTTPRequest request,
			ResourceStrategyRoute fromRoute) {
		return new NoRouteTask(request);
	}

	class NoRouteTask extends RequestTaskBase {

		public NoRouteTask(HTTPRequest request) {
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
