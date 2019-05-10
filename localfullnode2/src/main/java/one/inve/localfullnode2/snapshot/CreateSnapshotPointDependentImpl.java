package one.inve.localfullnode2.snapshot;

import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.Contribution;
import one.inve.bean.message.SnapshotPoint;
import one.inve.bean.node.LocalFullNode;
import one.inve.core.EventBody;
import one.inve.localfullnode2.conf.Config;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class CreateSnapshotPointDependentImpl implements CreateSnapshotPointDependent{

    private BigInteger currSnapshotVersion;
    @Override
    public BigInteger getCurrSnapshotVersion() {
        currSnapshotVersion = new BigInteger("318");
        return this.currSnapshotVersion;
    }

    private BigInteger totalConsEventCount;
    @Override
    public BigInteger getTotalConsEventCount() {
        totalConsEventCount = new BigInteger("30");
        return this.totalConsEventCount;
    }

    private int shardCount;
    @Override
    public int getShardCount() {
        shardCount = 1;
        return this.shardCount;
    }

    private int nValue;
    @Override
    public int getnValue() {
        nValue = 10;
        return this.nValue;
    }

    private HashSet<Contribution> contributions;
    @Override
    public HashSet<Contribution> getContributions() {
        contributions = new HashSet<Contribution>();
        long[] otherIds = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        long otherSeq = 13384096;
        for (int i = 0; i < 10; i++) {
            Contribution contribution = new Contribution();
            contribution.setShardId(getShardId());
            contribution.setCreatorId(getCreatorId());
            contribution.setOtherId(otherIds[(int) (1 + Math.random() * otherIds.length - 1)]);
            contribution.setOtherSeq(otherSeq);
            contributions.add(contribution);
            otherSeq += 10;
        }
        return this.contributions;
    }
    @Override
    public void setContributions(HashSet<Contribution> contributions) {
        this.contributions = contributions;
    }

    private List<LocalFullNode> localFullNodes;
    @Override
    public List<LocalFullNode> getLocalFullNodes() {
        localFullNodes = new ArrayList<LocalFullNode>();
        for (int i = 0; i < 10; i++) {
            LocalFullNode localFullNode = new LocalFullNode.Builder()
                    .shard(String.valueOf(getShardId()))
                    .index(String.valueOf(i))
                    .address(Config.CREATION_ADDRESSES.get(i))
                    .build();
            localFullNodes.add(localFullNode);
        }
        return this.localFullNodes;
    }

    private HashMap<BigInteger, SnapshotPoint> snapshotPointMap = new HashMap<BigInteger, SnapshotPoint>();
    @Override
    public HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap() {
        return this.snapshotPointMap;
    }

    private HashMap<BigInteger, String> treeRootMap = new HashMap<BigInteger, String>();
    @Override
    public HashMap<BigInteger, String> getTreeRootMap() {
        return this.treeRootMap;
    }

    private BigInteger consMessageMaxId;
    @Override
    public BigInteger getConsMessageMaxId() {
        return this.consMessageMaxId;
    }

    private LinkedBlockingQueue<JSONObject> consMessageVerifyQueue;
    @Override
    public LinkedBlockingQueue<JSONObject> getConsMessageVerifyQueue() {
        consMessageVerifyQueue = new LinkedBlockingQueue<JSONObject>();
        return this.consMessageVerifyQueue;
    }

    private long creatorId;
    @Override
    public long getCreatorId() {
        creatorId = 2L;
        return this.creatorId;
    }

    private int shardId;
    @Override
    public int getShardId() {
        shardId = 0;
        return this.shardId;
    }

    @Override
    public EventBody getEventBody() {
        return null;
    }

}
