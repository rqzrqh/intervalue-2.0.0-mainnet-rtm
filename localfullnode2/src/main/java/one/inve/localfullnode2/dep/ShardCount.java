package one.inve.localfullnode2.dep;

public class ShardCount extends DependentItem {
	private int shardCount;

	public int get() {
		return shardCount;
	}

	public void set(int shardCount) {
		this.shardCount = shardCount;
		nodifyAll();
	}
}
