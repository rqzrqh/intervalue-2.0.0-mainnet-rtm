package one.inve.localfullnode2.snapshot;


import com.alibaba.fastjson.JSONObject;
import one.inve.bean.message.Contribution;
import one.inve.bean.message.SnapshotPoint;
import one.inve.bean.node.LocalFullNode;
import one.inve.core.EventBody;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public interface CreateSnapshotPointDependent{

    BigInteger getCurrSnapshotVersion();

    void setContributions(HashSet<Contribution> contributions);

    EventBody getEventBody();

    BigInteger getTotalConsEventCount();

    int getShardCount();

    int getnValue();

    HashSet<Contribution> getContributions();

    List<LocalFullNode> getLocalFullNodes();

    HashMap<BigInteger, SnapshotPoint> getSnapshotPointMap();

    HashMap<BigInteger, String> getTreeRootMap();

    BigInteger getConsMessageMaxId();

    LinkedBlockingQueue<JSONObject> getConsMessageVerifyQueue();

    long getCreatorId();

    int getShardId();

}
