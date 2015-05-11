package strategy;

import interfaces.RequestTaskBase;

public interface ITaskEndedObserver {
	void endedTask(FutureRequestTask<RequestTaskBase, Void> killed);
}
