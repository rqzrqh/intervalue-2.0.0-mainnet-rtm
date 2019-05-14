package one.inve.localfullnode2.snapshot;

import java.util.concurrent.CompletableFuture;

import com.alibaba.fastjson.JSON;
import com.zeroc.Ice.Communicator;

import one.inve.cluster.Member;
import one.inve.localfullnode2.rpc.Local2localPrx;
import one.inve.localfullnode2.rpc.RpcConnectionService;
import one.inve.localfullnode2.snapshot.vo.SnapObj;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: access peer node to get sync snap object via zeroc
 *               <p>
 *               {@link GossipEventThread::gossip2Local}
 * @author: Francis.Deng
 * @date: May 7, 2019 1:42:45 AM
 * @version: V1.0
 */
public class SnapshotSyncConsumer implements SnapshotSyncConsumable {
	static final Logger logger = LoggerFactory.getLogger(SnapshotSyncConsumer.class);

	final private Communicator communicator;

	public SnapshotSyncConsumer(Communicator communicator) {
		this.communicator = communicator;
	}

	@Override
	public CompletableFuture<SnapObj> gossipMySnapVersion4SnapAsync(Member neighbor, String pubkey, String sig,
			String hash, String messageMaxId) {

		Local2localPrx nprx = RpcConnectionService.buildConnection2localFullNode(communicator, neighbor);

		CompletableFuture<SnapObj> snapResult = nprx.gossipMySnapVersion4SnapAsync(pubkey, sig, hash, messageMaxId);

		logger.info(">>>>>RETURN<<<<<gossipMySnapVersion4SnapAsync:\n snapResult: {}", JSON.toJSONString(snapResult));
		return snapResult;
	}

}
