package one.inve.localfullnode2.postconsensus.sorting;

import java.util.concurrent.BlockingQueue;

import one.inve.core.EventBody;

public interface EventsSortingDependent {
	int getShardCount();

	// the source
	BlockingQueue<EventBody> getShardSortQueue(int shardId);

	// the destination
	BlockingQueue<EventBody> getConsEventHandleQueue();
}
