package configuration;

import strategy.BadRequestStrategy;

public class ResourceStrategyRouteInvalid extends ResourceStrategyRoute {

	public ResourceStrategyRouteInvalid() {
		super(null, null, null, null);
	}

	@Override
	public Class<?> getStrategyClass() {
		return BadRequestStrategy.class;
	}
}
