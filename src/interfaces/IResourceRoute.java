package interfaces;

import java.util.List;

public interface IResourceRoute {

	public abstract Class<?> getStrategyClass();

	public abstract String getRouteMatch();

	public abstract String getStrategyOption(String option);

	public abstract List<String> getMethods();

	public abstract boolean respondsToMethod(String method);

}