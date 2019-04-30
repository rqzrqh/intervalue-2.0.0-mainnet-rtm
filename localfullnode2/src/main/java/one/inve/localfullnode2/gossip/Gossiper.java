package one.inve.localfullnode2.gossip;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;

import one.inve.cluster.Member;
import one.inve.localfullnode2.conf.Config;
import one.inve.localfullnode2.gossip.communicator.GossipCommunicationConsumable;
import one.inve.localfullnode2.gossip.vo.AppointEvent;
import one.inve.localfullnode2.gossip.vo.GossipObj;
import one.inve.localfullnode2.store.EventBody;
import one.inve.localfullnode2.store.IEventFlow;
import one.inve.localfullnode2.utilities.Cryptos;
import one.inve.localfullnode2.utilities.StringUtils;
import one.inve.localfullnode2.vo.Event;
import one.inve.utils.DSA;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: handle gossip communication and result process regardless of in
 *               one sharding or across shardings.
 *               <p>
 *               <code>one.inve.threads.gossip.GossipEventThread</code>for
 *               reference only
 * @author: Francis.Deng
 * @date: April 29, 2018 2:41:49 AM
 * @version: V1.0
 */
public class Gossiper {
	private static final Logger logger = LoggerFactory.getLogger("gossip");

	private GossipDependent dep;
	public String gossipAppointEventKey;
	private byte[] txCache;
	public boolean gossipAppointEventFlag;

	public void talkGossip(GossipDependent dep) {
		this.dep = dep;

//		logger.info(">>> start {}GossipEventThread...", pre);
//		// 等待分片信息
//		while (node.getShardId() < 0) {
//			logger.info(">>> {}GossipEventThread : shardId is null or gossipNode is null.", pre);
//			try {
//				sleep(1000);
//			} catch (InterruptedException e) {
//				logger.error("--- node shard id is empty.");
//			}
//		}
		// node.maxSeqs = new long[node.getShardCount()][node.getnValue()];

		// 初始化获取连接
		Random rand = new Random();
		int numNeighbors = 1;

		// while (true) {
		// if (gossipFlag) {
		Instant firstTime = Instant.now();

		List<Member> members = dep.getMembers(dep.getGossipType());
		int memberSize = members.size();
//		if (memberSize <= 0) {
////                logger.warn("{}there is no neighbor.", pre);
//			long handleInterval = Duration.between(firstTime, Instant.now()).toMillis();
//			if (handleInterval < 2000) {
//				try {
//					sleep(2000 - handleInterval);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//			continue;
//		}

//		ArrayList<Address> toBeRemove = new ArrayList<>();
//		for (Address addr : prxMap.keySet()) {
//			boolean exist = false;
//			for (Member m : members) {
//				if (addr.equals(m.address())) {
//					exist = true;
//				}
//			}
//			if (!exist) {
//				toBeRemove.add(addr);
//			}
//		}
//		for (Address addr : toBeRemove) {
//			prxMap.remove(addr);
//		}

		// 随机挑选一个其他片(用于向随机选择的邻居请求该分片的未知event数据)
		int otherShardId = -1;
		if (dep.getGossipType() == Config.GOSSIP_GLOBAL_SHARD) {
			do {
				otherShardId = rand.nextInt(dep.getShardCount());
			} while (otherShardId == dep.getShardId());
		}

		StringBuilder ips = new StringBuilder();
		int[] neighborIdxes = rand.ints(0, memberSize).limit(numNeighbors).toArray();

		CompletableFuture<?>[] evtResults = new CompletableFuture<?>[numNeighbors];
		boolean[] connflag = new boolean[numNeighbors];
		for (int ni = 0; ni < numNeighbors; ni++) {
			Member neighbor = dep.getMember(dep.getGossipType(), neighborIdxes[ni]);
//			if (StringUtils.isNotEmpty(ips)) {
//				ips.append("|");
//			}
//			ips.append(neighbor.address().host()).append(":").append(neighbor.address().port());
//			// 获取未知events
//			if (!prxMap.containsKey(neighbor.address())) {
//				Local2localPrx nprx = RpcConnectionService.buildConnection2localFullNode(node.getCommunicator(),
//						neighbor);
//				if (null != nprx) {
//					prxMap.put(neighbor.address(), nprx);
//				} else {
//					logger.warn("build new connection to local full node failure. ");
//					continue;
//				}
//			}
			try {
//				if (null == prxMap.get(neighbor.address())) {
//					logger.error("{}neighbor is empty.", pre);
//				}
				GossipCommunicationConsumable communication = dep.getGossipCommunication();

				if (dep.getGossipType() == Config.GOSSIP_IN_SHARD) {
//					long[] seqs = node.getEventStore().getLastSeqsByShardId(node.getShardId());
//
//					logger.info(
//							"call gossipMyMaxSeqList4Consensus({}) remotely with parameters of pubkey={},snapshotVersion={},lastSeqs={}",
//							ips.toString(), HnKeyUtils.getString4PublicKey(node.publicKey),
//							node.getCurrSnapshotVersion().toString(), JSONObject.toJSONString(seqs));
					long[] seqs = dep.getLastSeqsByShardId(dep.getShardId());

//					evtResults[ni] = prxMap.get(neighbor.address()).gossipMyMaxSeqList4ConsensusAsync(
//							HnKeyUtils.getString4PublicKey(node.publicKey), "",
//							node.getCurrSnapshotVersion().toString(), null, seqs);
					evtResults[ni] = communication.gossipMyMaxSeqList4ConsensusAsync(neighbor, dep.getPublicKey(), "",
							dep.getCurrSnapshotVersion().toString(), null, seqs);
				} else {
//					long[] seqs = node.getEventStore().getLastSeqsByShardId(otherShardId);
//					evtResults[ni] = prxMap.get(neighbor.address()).gossipMyMaxSeqList4SyncAsync(
//							HnKeyUtils.getString4PublicKey(node.publicKey), "", otherShardId,
//							node.getCurrSnapshotVersion().toString(), null, seqs);

					long[] seqs = dep.getLastSeqsByShardId(otherShardId);
					evtResults[ni] = communication.gossipMyMaxSeqList4SyncAsync(neighbor, dep.getPublicKey(), "",
							otherShardId, dep.getCurrSnapshotVersion().toString(), null, seqs);
				}
				connflag[ni] = true;
			} catch (Exception e) {
//				logger.error("{}gossipMyMaxSeqList ConnectionRefusedException: {}", pre, neighbor.address().host());
//				logger.warn("remove gossipAddress: {}", JSON.toJSONString(neighbor.address()));
//				prxMap.remove(neighbor.address());
				connflag[ni] = false;
			}
		}
//		if (logger.isDebugEnabled()) {
//			long itv = Duration.between(firstTime, Instant.now()).toMillis();
//			if (itv > 10) {
//				logger.debug("node-({}, {}) {}: 1 gossip events (from neighbor) cost: {}", node.getShardId(),
//						node.getCreatorId(), pre, itv);
//			}
//		}

		// 处理gossip回来的结果
		String ret = handleEventsCompletableFuture(neighborIdxes, connflag, evtResults, numNeighbors, otherShardId,
				/* prxMap, */ true);

		// 检测时间
		long handleInterval = Duration.between(firstTime, Instant.now()).toMillis();
//		if (handleInterval > 100) {
//			logger.info("node-({}, {}) {}: {}, evts = {}, size = {} KB, time = {}sec, seq: {}", node.getShardId(),
//					node.getCreatorId(), pre, ips.toString(), ret.split("_")[0],
//					Long.parseLong(ret.split("_")[1]) >> 10, handleInterval / 1000.0,
//					JSONArray.toJSONString(node.maxSeqs));
//		}
//		if (handleInterval < Config.DEFAULT_GOSSIP_EVENT_INTERVAL) {
//			try {
//				sleep(Config.DEFAULT_GOSSIP_EVENT_INTERVAL - handleInterval);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
		// }
		// }
	}

	private String handleEventsCompletableFuture(int[] neighborIdxes, boolean[] connflag,
			CompletableFuture<?>[] evtResults, int numNeighbors, int otherShardId,
			/* HashMap<Address, Local2localPrx> prxMap, */boolean isNewLocalEvent) {
		int eventSize = 0;
		int eventSpaces = 0;
		for (int ni = 0; ni < numNeighbors; ni++) {
			if (connflag[ni]) {
				Instant first = Instant.now();
//				Member neighbor = (type == Config.GOSSIP_IN_SHARD) ? node.inshardNeighborPools.get(neighborIdxes[ni])
//						: node.globalNeighborPools.get(neighborIdxes[ni]);
				Member neighbor = dep.getMember(dep.getGossipType(), neighborIdxes[ni]);
				// Local2localPrx prx = prxMap.get(neighbor.address());

				logger.info("gossip to a peer [{}]\n", neighbor.toString());

				String result = gossip2Local(neighbor, evtResults[ni], /* prx, */ otherShardId, isNewLocalEvent);
				eventSize += result.split("_")[0] == null ? 0 : Integer.parseInt(result.split("_")[0]);
				eventSpaces += result.split("_")[1] == null ? 0 : Integer.parseInt(result.split("_")[1]);
			} else {
				logger.warn("numNeighbors is null.");
			}
		}
		return eventSize + "_" + eventSpaces;
	}

	private String gossip2Local(Member neighbor, CompletableFuture<?> evtResult, /** Local2localPrx prx, **/
	int otherShardId, boolean isNewLocalEvent) {

		int eventSize = 0;
		int eventSpaces = 0;
		boolean flag = false;
		GossipObj gossipObj = null;
		Instant first = Instant.now();
		try {
			// 异步获取返回结果
			gossipObj = (GossipObj) evtResult.get(30000, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			// logger.error("gossip2Local {} completableFuture.get() Exception: {}:{}-{} ",
			// pre, neighbor.address().host(),neighbor.address().port(), e);
//			logger.error("gossip2Local {} completableFuture.get() Exception: host={},rpcPort={},e={} ", pre,
//					neighbor.address().host(), neighbor.metadata().get("rpcPort"), e);

			return eventSize + "_" + eventSpaces;
		}

		// Francis.Deng 4/3/2019
		// diagnosing system problem - tracking gossipObj(DSPTTG)
//		logger.info("The node currSnapshotVersion={}", node.getCurrSnapshotVersion().toString());
//		logger.info("The composition of gossipObj={}", JSONObject.toJSONString(gossipObj));

		if (gossipObj != null) {
			if (StringUtils.isEmpty(gossipObj.snapVersion)) {
				logger.error("gossipObj.snapVersion is null");
				return eventSize + "_" + eventSpaces;
			}

			// key condition - sync snapshot if possible

//			BigInteger snapVers = new BigInteger(gossipObj.snapVersion);
//			if (snapVers.compareTo(dep.getCurrSnapshotVersion().add(BigInteger.ONE)) > 0) {
//				logger.warn("node-({}, {}): neighbor node snapshot version bigger than mine...", node.getShardId(),
//						node.getCreatorId());
//				// 记录比自己快照版本高过1个版本的节点数
//				if (gossipObj.snapHash == null || gossipObj.snapHash.length == 0) {
//					logger.error("gossipObj.snapHash is null");
//					return eventSize + "_" + eventSpaces;
//				}
//				if (snapVersionMap.get(new String(gossipObj.snapHash)) == null) {
//					HashSet<String> pubKeySet = new HashSet<>();
//					pubKeySet.add(neighbor.metadata().get("pubkey"));
//					snapVersionMap.put(new String(gossipObj.snapHash), pubKeySet);
//				} else {
//					HashSet<String> pubKeySet = snapVersionMap.get(new String(gossipObj.snapHash));
//					pubKeySet.add(neighbor.metadata().get("pubkey"));
//					snapVersionMap.put(new String(gossipObj.snapHash), pubKeySet);
//				}
//				// 当收集到超过f+1个节点的快照版本比我高过1个时,直接同步快照
//				logger.warn("1:{}", new String(gossipObj.snapHash));
//				logger.warn("2:{}", JSONObject.toJSONString(snapVersionMap.get(new String(gossipObj.snapHash))));
//				logger.warn("3:{}", gossipObj.snapHash.length);
//				if (snapVersionMap.get(new String(gossipObj.snapHash)) != null
//						&& snapVersionMap.get(new String(gossipObj.snapHash))
//								.size() > (node.getShardCount() * node.getnValue()) / 3 + 1) {
//					logger.warn("node-({}, {}): more than f+1 neighbor node's snapshot version bigger than mine, "
//							+ "neighbor synchronize snapshot...", node.getShardId(), node.getCreatorId());
//					logger.warn("node-({}, {}): new String(gossipObj.snapHash):{}", node.getShardId(),
//							node.getCreatorId(), new String(gossipObj.snapHash));
//					CompletableFuture<?> snapResult = prxMap.get(neighbor.address()).gossipMySnapVersion4SnapAsync(
//							HnKeyUtils.getString4PublicKey(node.publicKey), "", new String(gossipObj.snapHash),
//							node.getConsMessageMaxId().toString());
//					SnapObj snapObj = null;
//					try {
//						snapObj = (SnapObj) snapResult.get(30000, TimeUnit.MILLISECONDS);
//					} catch (Exception e) {
//						// logger.error("gossip2Local for snapshot {} completableFuture.get() Exception:
//						// {}-{} ", pre, neighbor.address().host(), e);
//						logger.error(
//								"gossip2Local for snapshot {} completableFuture.get() Exception: host={},rpcPort={},e={} ",
//								pre, neighbor.address().host(), neighbor.metadata().get("rpcPort"), e);
//
//						return eventSize + "_" + eventSpaces;
//					}
//					if (snapObj != null) {
//						logger.warn("snapObj:{}", JSONObject.toJSONString(snapObj));
//						String snapMessageStr = snapObj.snapMessage;
//						String originalSnapshotStr = JSON.parseObject(snapMessageStr).getString("message");
//						logger.warn("snapMessageStr:{}", snapMessageStr);
//						SnapshotMessage snapshotMessage = JSONObject.parseObject(originalSnapshotStr,
//								SnapshotMessage.class);
//						String MsgHashTreeRoot = snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot();
//						if (StringUtils.isEmpty(MsgHashTreeRoot)) {
//							return eventSize + "_" + eventSpaces;
//						}
//						List<JSONObject> messages = null;
//						if (!StringUtils.isEmpty(snapObj.messages)) {
//							messages = JSONArray.parseArray(snapObj.messages, JSONObject.class);
//						}
//
//						// 正在快照后
//						if (SignUtil.verify(originalSnapshotStr)) {
//							// transaction入库
//							if (messages != null) {
//								for (JSONObject msg : messages) {
//									try {
//										logger.error(">>>>>each of messages in GossipEventThread= " + msg);
//
//										node.getConsMessageVerifyQueue().put(msg);
//									} catch (InterruptedException e) {
//										e.printStackTrace();
//									}
//								}
//							}
//							try {
//								logger.error(
//										"node.getConsMessageVerifyQueue().put(JSONObject.parseObject(snapMessageStr))"
//												+ snapMessageStr);
//
//								node.getConsMessageVerifyQueue().put(JSONObject.parseObject(snapMessageStr));
//							} catch (InterruptedException e) {
//								logger.error("", e);
//							}
//							// 更新本节点当前快照信息
//							node.setSnapshotMessage(snapshotMessage);
//							node.getSnapshotPointMap().put(snapshotMessage.getSnapVersion(),
//									snapshotMessage.getSnapshotPoint());
//							node.getTreeRootMap().put(snapshotMessage.getSnapVersion(),
//									snapshotMessage.getSnapshotPoint().getMsgHashTreeRoot());
//							snapVersionMap.clear();
//						} else {
//							return eventSize + "_" + eventSpaces;
//						}
//					}
//				}
//			}

//			long itv = Duration.between(first, Instant.now()).toMillis();
//			if (itv > 50) {
//				logger.debug("node-({}, {}) {}: 2 get events (async future) cost: {}", node.getShardId(),
//						node.getCreatorId(), pre, itv);
//			}

			Event[] evtRet = gossipObj.events;
			if (evtRet.length > Config.DEFAULT_SYNC_EVENT_COUNT) {
				// logger.warn("sync part events...");
				logger.warn(
						"gossiped event number value({}) exceeds system threshold value({}),which leads to the issue of UST",
						evtRet.length, Config.DEFAULT_SYNC_EVENT_COUNT);
			}
//            if (null != evtRet && evtRet.length > 0 && evtRet.length <=Config.DEFAULT_SYNC_EVENT_COUNT ) {
			if (null != evtRet && evtRet.length > 0 && evtRet.length <= Config.DEFAULT_SYNC_EVENT_COUNT) {
				// 将获取到的event添加到hashnet
				flag = addEvent2Hashnet(evtRet,
						(dep.getGossipType() == Config.GOSSIP_IN_SHARD) ? dep.getShardId() : otherShardId);

				// Francis.Deng 4/2/2019
				logger.info("{}=addEvent2Hashnet({},{}),which impacts the generation of 'newLocalEvent'",
						Boolean.toString(flag), String.valueOf(dep.getShardId()), String.valueOf(otherShardId));

				eventSize += evtRet.length;
				eventSpaces += JSONArray.toJSONString(evtRet).length();

				if (isNewLocalEvent && flag && dep.getGossipType() == Config.GOSSIP_IN_SHARD) {
					// 本地产生一个新的event
					newLocalEvent(Integer.parseInt(neighbor.metadata().get("index")));
				}
			}

		} else {
			logger.warn("gossipObj is null.");
		}
		return eventSize + "_" + eventSpaces;
	}

	private boolean addEvent2Hashnet(Event[] events, int shardId) {
		boolean flag = false;
		GossipCommunicationConsumable communication = dep.getGossipCommunication();

		try {
			Instant first = Instant.now();
			// 将从邻居接收的未知共识事件记入自己hg
			IEventFlow myFlow = dep.getEventFlow();
			for (Event evt : events) {
				if (evt.selfSeq == 0 || evt.selfSeq == dep.getLastSeqs()[shardId][(int) evt.selfId] + 1) {
					// node.maxSeqs[shardId][(int) evt.selfId] = evt.selfSeq;
					dep.getLastSeqs()[shardId][(int) evt.selfId] = evt.selfSeq;
				} else {
//					logger.warn("======== (shardId, creatorId) = ({}, {}) : max_seq = {}, receive seq = {}", shardId,
//							evt.selfId, node.maxSeqs[shardId][(int) evt.selfId], evt.selfSeq);
					// node.maxSeqs[shardId][(int) evt.selfId] = evt.selfSeq;
					dep.getLastSeqs()[shardId][(int) evt.selfId] = evt.selfSeq;
				}
				EventBody eb = new EventBody.Builder().shardId(evt.shardId).creatorId(evt.selfId)
						.creatorSeq(evt.selfSeq).otherId(evt.otherId).otherSeq(evt.otherSeq)
						.timeCreated(Instant.ofEpochSecond(evt.timeCreatedSecond, evt.timeCreatedNano))
						.trans((null == evt.messages || evt.messages.length == 0) ? null : evt.messages)
						.signature(evt.sign).isFamous(evt.isFamous).generation(evt.generation).hash(evt.hash)
						.consTimestamp((evt.consensusTimestampSecond < 0) ? null
								: Instant.ofEpochSecond(evt.consensusTimestampSecond, evt.consensusTimestampNano))
						.build();

				// Francis.Deng 04/26/2019
				// back up gossipy EventBody intentionally
				// ValuableDataOutputters.getInstance().outputGossipyEventBody(eb);

				Map<String, String> resultMap = myFlow.addEvent(eb);

				logger.info(
						"{}=Map<String, String> resultMap = myFlow.addEvent(eb),eg,'selfMissing','otherMissing','hashErr','signatureErr'",
						resultMap);

				String data = Config.GOSSIP_SIGNATURE_DATA;
				// String pubkey = node.publicKey == null ? "" :
				// HnKeyUtils.getString4PublicKey(node.publicKey);
				String pubkey = dep.getPublicKey();
				byte[] sig = Cryptos.sign(data.getBytes(), dep.getPrivateKey());
				if ("selfMissing".equals(resultMap.get("result"))) {
//					List<Member> members = (type == Config.GOSSIP_IN_SHARD) ? node.inshardNeighborPools
//							: node.globalNeighborPools;
					List<Member> members = dep.getMembers(dep.getGossipType());
					List<Member> successList = new ArrayList<>();
					int nodes = members.size();
					// 存放調接口成功返回Event的對象的pubey,其中map的key爲Event的hash值,Set爲pubkey集合
					Map<String, Set<String>> eventHashMap = new HashMap<>();
					Map<String, Event> eventMap = new HashMap<>();
					// 存放調接口返回版本號的對象的pubkey
					Set<String> snapVersionSet = new HashSet<>();

					while (successList.size() < nodes) {
						for (Member member : members) {
//							Local2localPrx nprx = RpcConnectionService
//									.buildConnection2localFullNode(node.getCommunicator(), member);
							try {
								AppointEvent appointEvent = communication.gossip4AppointEvent(member, pubkey,
										sig == null ? "" : DSA.encryptBASE64(sig), shardId, evt.selfId,
										evt.selfSeq - 1);
								successList.add(member);
								if (appointEvent != null && appointEvent.event != null
										&& appointEvent.event.hash != null && appointEvent.event.hash.length > 0) {
									String hash = DSA.encryptBASE64(appointEvent.event.hash);
									if (eventHashMap.get(hash) != null) {
										eventHashMap.get(hash).add(member.metadata().get("pubkey"));
									} else {
										Set<String> set = new HashSet<>();
										set.add(member.metadata().get("pubkey"));
										eventHashMap.put(hash, set);
									}
									eventMap.put(hash, appointEvent.event);
								} else if (appointEvent != null && !StringUtils.isEmpty(appointEvent.snapVersion)) {
									snapVersionSet.add(member.metadata().get("pubkey"));
								}
							} catch (Exception e) {
								logger.warn("gossip4AppointEventErr:{}", e);
							}

						}
						members.removeAll(successList);
						for (String hash : eventHashMap.keySet()) {
							if (eventHashMap.get(hash).size() > nodes / 3 + 1) {
								// Event加入EventStore
								Event event = eventMap.get(hash);
								myFlow.addEvent2Store(new EventBody.Builder().shardId(event.shardId)
										.creatorId(event.selfId).creatorSeq(event.selfSeq).otherId(event.otherId)
										.otherSeq(event.otherSeq)
										.timeCreated(
												Instant.ofEpochSecond(event.timeCreatedSecond, event.timeCreatedNano))
										.trans((null == event.messages || event.messages.length == 0) ? null
												: event.messages)
										.signature(event.sign).isFamous(event.isFamous).generation(event.generation)
										.hash(event.hash)
										.consTimestamp((event.consensusTimestampSecond < 0) ? null
												: Instant.ofEpochSecond(event.consensusTimestampSecond,
														event.consensusTimestampNano))
										.build());
								resultMap = myFlow.addEvent(eb);
								break;
							}
						}
						if (snapVersionSet.size() > nodes / 3 + 1) {
							return flag;
						}
					}
				}
				if ("otherMissing".equals(resultMap.get("result"))) {
//					List<Member> members = (type == Config.GOSSIP_IN_SHARD) ? node.inshardNeighborPools
//							: node.globalNeighborPools;
					List<Member> members = dep.getMembers(dep.getGossipType());
					List<Member> successList = new ArrayList<>();
					int nodes = members.size();
					// 存放調接口成功返回Event的對象的pubey,其中map的key爲Event的hash值,Set爲pubkey集合
					Map<String, Set<String>> eventHashMap = new HashMap<>();
					Map<String, Event> eventMap = new HashMap<>();
					// 存放調接口返回版本號的對象的pubkey
					Set<String> snapVersionSet = new HashSet<>();
					while (successList.size() < nodes) {
						for (Member member : members) {
//							Local2localPrx nprx = RpcConnectionService
//									.buildConnection2localFullNode(node.getCommunicator(), member);
							try {
								AppointEvent appointEvent = communication.gossip4AppointEvent(member, pubkey,
										sig == null ? "" : DSA.encryptBASE64(sig), shardId, evt.otherId, evt.otherSeq);
								successList.add(member);
								if (appointEvent != null && appointEvent.event != null
										&& appointEvent.event.hash != null && appointEvent.event.hash.length > 0) {
									String hash = DSA.encryptBASE64(appointEvent.event.hash);
									if (eventHashMap.get(hash) != null) {
										eventHashMap.get(hash).add(member.metadata().get("pubkey"));
									} else {
										Set<String> set = new HashSet<>();
										set.add(member.metadata().get("pubkey"));
										eventHashMap.put(hash, set);
									}
									eventMap.put(hash, appointEvent.event);
								} else if (appointEvent != null && !StringUtils.isEmpty(appointEvent.snapVersion)) {
									snapVersionSet.add(member.metadata().get("pubkey"));
								}
							} catch (Exception e) {
								logger.warn("gossip4AppointEventErr:{}", e);
							}

						}
						members.removeAll(successList);
						for (String hash : eventHashMap.keySet()) {
							if (eventHashMap.get(hash).size() > nodes / 3 + 1) {
								// Event加入EventStore
								Event event = eventMap.get(hash);
								myFlow.addEvent2Store(new EventBody.Builder().shardId(event.shardId)
										.creatorId(event.selfId).creatorSeq(event.selfSeq).otherId(event.otherId)
										.otherSeq(event.otherSeq)
										.timeCreated(
												Instant.ofEpochSecond(event.timeCreatedSecond, event.timeCreatedNano))
										.trans((null == event.messages || event.messages.length == 0) ? null
												: event.messages)
										.signature(event.sign).isFamous(event.isFamous).generation(event.generation)
										.hash(event.hash)
										.consTimestamp((event.consensusTimestampSecond < 0) ? null
												: Instant.ofEpochSecond(event.consensusTimestampSecond,
														event.consensusTimestampNano))
										.build());
								resultMap = myFlow.addEvent(eb);
								break;
							}
						}
						if (snapVersionSet.size() > nodes / 3 + 1) {
							return flag;
						}
					}
				}

				// temporal comment

//				if ("hashErr".equals(resultMap.get("result"))) {
//					/*
//					 * one.inve.rpc.localfullnode.Event event =
//					 * gossipEvent(neighbor,shardId,evt.otherId,evt.otherSeq); int count = 0;
//					 * //如果event为空，则继续获取event，继续三次后停止。 while(event==null) { if(count>3) { break; }
//					 * Thread.sleep(100); count++; event =
//					 * gossipEvent(neighbor,shardId,evt.otherId,evt.otherSeq); }
//					 * //若多次请求后event仍为空，则直接退出本次添加的循环，找其他节点gossip，若存在分叉，与其他节点gossip仍能再次发现。
//					 * if(event==null) { break; }
//					 */
//					// if(event.hash!=null&&!DSA.encryptBASE64(event.hash).equals(resultMap.get("otherHash")))
//					// {
//					Instant start = Instant.now();
//					PublicKey pubKey = null;
//					long creatorId = 0;
//					long creatorSeq = 0;
//					if ("otherChild".equals(resultMap.get("splitType"))) {
//						logger.info("find split attack:local event creatorId--" + evt.otherId + "--creatorSeq--"
//								+ evt.otherSeq);
//						pubKey = dep.getEventFlow().getPubKeys()[shardId][(int) evt.otherId];
//						gossipAppointEventKey = shardId + "_" + evt.otherId + "_" + evt.otherSeq;
//						creatorId = evt.otherId;
//						creatorSeq = evt.otherSeq;
//					} else {
//						logger.info("find split attack:local event creatorId--" + evt.selfId + "--creatorSeq--"
//								+ (evt.selfSeq - 1));
//						pubKey = dep.getEventFlow().getPubKeys()[shardId][(int) evt.selfId];
//						gossipAppointEventKey = shardId + "_" + evt.selfId + "_" + (evt.selfSeq - 1);
//						creatorId = evt.selfId;
//						creatorSeq = evt.selfSeq - 1;
//					}
//					if (pubKey != null) {
//						dep.getBlackList4PubKey().add(HnKeyUtils.getString4PublicKey(pubKey));
//						logger.info("write publicKey in blackList:" + pubKey);
//						DbUtils.writeBlackInDb(HnKeyUtils.getString4PublicKey(pubKey), 2, node.nodeParameters.dbId);
//						HashMap<Address, Local2localPrx> prxMap = new HashMap<>();
//						if ("0".equals(node.getShardId()) && "0".equals(node.getCreatorId())) {
//							((Main) this.node).getConsensusThread().gossipFlag = false;
//							((Main) this.node).getSyncThread().gossipFlag = false;
//							gossipAppointEventFlag = true;
//							Map<String, Set<String>> appointEventMap = new HashMap<>();
//							EventBody localEvent = node.getEventStore().getEvent(shardId, creatorId, creatorSeq);
//							if (localEvent != null) {
//								Set<String> set = new HashSet<>();
//								set.add("self");
//								appointEventMap.put(DSA.encryptBASE64(localEvent.getHash()), set);
//							}
//							int totalResultCount = 0;
//							int localFullNodeCount = (node.getShardCount() * node.getnValue());
//							prxMap.putAll(((Main) node).getConsensusThread().getPrxMap());
//							prxMap.putAll(((Main) node).getSyncThread().getPrxMap());
//							Local2localPrx prx = RpcConnectionService.buildConnection2localFullNode(
//									node.getCommunicator(), node.nodeParameters.selfGossipAddress.pubIP,
//									node.nodeParameters.selfGossipAddress.rpcPort);
//							sig = Cryptos.sign(data.getBytes(), node.privateKey);
//							String Event = JSONObject.toJSONString(
//									((Main) node).getEventStore().getEvent(shardId, creatorId, creatorSeq));
//							prx.gossipReport4splitAsync(pubkey, sig == null ? "" : DSA.encryptBASE64(sig), data,
//									shardId, Event);
//							while (gossipAppointEventFlag) {
//								if (totalResultCount >= localFullNodeCount - 1) {
//									break;
//								}
//								for (Address address : prxMap.keySet()) {
//									CompletableFuture<?> evtResult = prxMap.get(address).gossip4AppointEventAsync(
//											pubkey, sig == null ? "" : DSA.encryptBASE64(sig), shardId, creatorId,
//											creatorSeq);
//									one.inve.rpc.localfullnode.Event event = (one.inve.rpc.localfullnode.Event) evtResult
//											.get(30000, TimeUnit.MILLISECONDS);
//									if (event != null && event.hash != null) {
//										Set<String> addressSet = appointEventMap.get(DSA.encryptBASE64(event.hash));
//										if (appointEventMap.get(DSA.encryptBASE64(event.hash)) != null) {
//											addressSet.add(address.host() + "_" + address.port());
//											totalResultCount++;
//											prxMap.remove(address);
//											if (addressSet.size() >= ((node.getShardCount() * node.getnValue()) / 3)
//													+ 1) {
//												//
//												gossipAppointEventFlag = false;
//												gossip4SplitDel(shardId, creatorId, creatorSeq,
//														DSA.encryptBASE64(event.hash));
//												flag = false;
//												HashMap<Address, Local2localPrx> noticePrxMap = new HashMap<>();
//												noticePrxMap.putAll(((Main) node).getConsensusThread().getPrxMap());
//												noticePrxMap.putAll(((Main) node).getSyncThread().getPrxMap());
//												for (Address noticeAddress : noticePrxMap.keySet()) {
//													noticePrxMap.get(noticeAddress).gossip4SplitDelAsync(pubkey,
//															sig == null ? "" : DSA.encryptBASE64(sig), data, shardId,
//															creatorId, creatorSeq,
//															event.hash == null ? "" : DSA.encryptBASE64(event.hash),
//															false);
//												}
//												break;
//											}
//										} else {
//											addressSet = new HashSet<>();
//											addressSet.add(address.host() + "_" + address.port());
//											totalResultCount++;
//											prxMap.remove(address);
//											appointEventMap.put(DSA.encryptBASE64(event.hash), addressSet);
//										}
//										break;
//									} else {
//										logger.error("gossip Appoint Event or Hash is null!");
//									}
//								}
//							}
//						} else {
//							prxMap.putAll(
//									type == Config.GOSSIP_IN_SHARD ? ((Main) node).getConsensusThread().getPrxMap()
//											: ((Main) node).getSyncThread().getPrxMap());
//							List<Member> allMembers = type == Config.GOSSIP_IN_SHARD ? node.inshardNeighborPools
//									: node.globalNeighborPools;
//							Member centerNeighbor = null;
//							PublicKey centerPubkey = node.getEventFlow().getPubKeys()[0][0];
//							for (Member member : allMembers) {
//								if (centerPubkey != null && HnKeyUtils.getString4PublicKey(centerPubkey)
//										.equals(member.metadata().get("pubkey"))) {
//									centerNeighbor = member;
//								}
//							}
//							// 调用中心化节点的申请接口
//							if (centerNeighbor != null && prxMap.get(centerNeighbor.address()) != null) {
//								String Event = JSONObject.toJSONString(
//										((Main) node).getEventStore().getEvent(shardId, creatorId, creatorSeq));
//								prxMap.get(centerNeighbor.address()).gossipReport4splitAsync(pubkey,
//										sig == null ? "" : DSA.encryptBASE64(sig), data, shardId, Event);
//								((Main) this.node).getConsensusThread().gossipFlag = false;
//								((Main) this.node).getSyncThread().gossipFlag = false;
//								gossipAppointEventFlag = true;
//								Map<String, Set<String>> appointEventMap = new HashMap<>();
//								EventBody localEvent = node.getEventStore().getEvent(shardId, creatorId, creatorSeq);
//								if (localEvent != null) {
//									Set<String> set = new HashSet<>();
//									set.add("self");
//									appointEventMap.put(DSA.encryptBASE64(localEvent.getHash()), set);
//								}
//								// 获取
//								int totalResultCount = 0;
//								int localFullNodeCount = (node.getShardCount() * node.getnValue());
//								prxMap.clear();
//								prxMap.putAll(((Main) node).getConsensusThread().getPrxMap());
//								prxMap.putAll(((Main) node).getSyncThread().getPrxMap());
//								while (gossipAppointEventFlag) {
//									if (totalResultCount >= localFullNodeCount - 1) {
//										break;
//									}
//
//									/*
//									 * allMembers = node.inshardNeighborPools; String myPubkey =
//									 * DSA.encryptBASE64(node.publicKey); for (Member member : allMembers) { if
//									 * (!StringUtils.isEmpty(myPubkey) &&
//									 * myPubkey.equals(member.metadata().get("pubkey"))) { neighbor = member; } }
//									 */
//									for (Address address : prxMap.keySet()) {
//										/*
//										 * if (neighbor != null && neighbor.address() == address) { continue; }
//										 */
//										CompletableFuture<?> evtResult = prxMap.get(address).gossip4AppointEventAsync(
//												pubkey, sig == null ? "" : DSA.encryptBASE64(sig), shardId, creatorId,
//												creatorSeq);
//										AppointEvent appointEvent = (AppointEvent) evtResult.get(30000,
//												TimeUnit.MILLISECONDS);
//										Event event = null;
//										if (appointEvent != null) {
//											event = appointEvent.event;
//										}
//										if (event != null && event.hash != null) {
//											Set<String> addressSet = appointEventMap.get(DSA.encryptBASE64(event.hash));
//											if (appointEventMap.get(DSA.encryptBASE64(event.hash)) != null) {
//												addressSet.add(address.host() + "_" + address.port());
//												totalResultCount++;
//												prxMap.remove(address);
//												if (addressSet.size() >= ((node.getShardCount() * node.getnValue()) / 3)
//														+ 1) {
//													//
//													gossipAppointEventFlag = false;
//													gossip4SplitDel(shardId, creatorId, creatorSeq,
//															DSA.encryptBASE64(event.hash));
//													flag = false;
//													break;
//												}
//											} else {
//												addressSet = new HashSet<>();
//												addressSet.add(address.host() + "_" + address.port());
//												totalResultCount++;
//												prxMap.remove(address);
//												appointEventMap.put(DSA.encryptBASE64(event.hash), addressSet);
//											}
//											break;
//										} else {
//											logger.error("gossip Appoint Event or Hash is null!");
//										}
//									}
//								}
//							} else {
//								logger.error("neighbor is null!");
//							}
//						}
//					}
//					logger.error("splitUsedTime:" + Duration.between(start, Instant.now()));
//					break;
//					// 若没有分叉，则代表event内容被当前与我gossip的节点篡改且hash未被篡改，或者与我gossip的节点不诚实，未将真实的分叉父event发给我。
//					// 无论何种情况，都代表当前与我gossip的节点作恶，将其加入黑名单。
//					/*
//					 * }else { logger.info("find tamper ! hashErr ip:"+neighbor.address().host()
//					 * +"--wirte in blackPubKeyList!");
//					 * logger.info("event creatorId:"+event.selfId+"===creatorSeq:"+event.selfSeq+
//					 * "======hash:"+DSA.encryptBASE64(event.hash));
//					 * //GossipLocalFullNodeThread.blackPubKeyList.add(neighbor.metadata().get(
//					 * "pubkey")); //writeBlackInDb(neighbor.metadata().get("pubkey"),2); break; }
//					 */
//				}
				if ("signatureErr".equals(resultMap.get("result"))) {

					break;
				}
				flag = true;
			}
//			if (logger.isDebugEnabled()) {
//				long itv = Duration.between(first, Instant.now()).toMillis();
//				if (itv > 30) {
//					logger.debug("node-({}, {}) {}: 3 add events (from neighbor) into hashnet cost: {} ms",
//							node.getShardId(), node.getCreatorId(), pre, itv);
//				}
//			}
			return flag;
		} catch (Exception e) {
			logger.error("--- shard-{}: addEvent2Hashnet error", shardId, e);
			return flag;
		}
	}

	private void newLocalEvent(int otherId) {
		// int shardId = node.getShardId();
		int shardId = dep.getShardId();
		try {
			ArrayList<byte[]> transactionlist = new ArrayList<>();
			int index = 0;
			Instant time = Instant.now();
			long size = 0L;
			while (!dep.getMessageQueue().isEmpty()
					&& Duration.between(time, Instant.now()).toMillis() < Config.DEFAULT_GOSSIP_EVENT_INTERVAL) {
				if (null != txCache) {
					transactionlist.add(txCache);
					size += txCache.length;
					txCache = null;
				} else {
					byte[] unit = dep.getMessageQueue().poll();
					if (null != unit) {
						size += unit.length;
						if (size > Config.DEFAULT_EVENT_MAX_PAYLOAD) {
							txCache = unit;
							break;
						} else {
							transactionlist.add(unit);
						}
					}
				}
				index++;
				if (index >= Config.DEFAULT_TRANSACTIONS_PER_EVENT) {
					break;
				}
			}
			byte[][] bytes = transactionlist.toArray(new byte[0][0]);
			dep.getEventFlow().newEvent(shardId, dep.getCreatorId(), otherId, (0 == index) ? null : bytes);
			if (index > 10) {
				logger.info("node-({}, {}): txs: {}, space: {} KB, new evt cost: {} ms, Pending txs queue size: {}",
						dep.getShardId(), dep.getCreatorId(), index, size >> 10,
						Duration.between(time, Instant.now()).toMillis(), dep.getMessageQueue().size());
			}
		} catch (Exception e) {
			logger.error("--- shard-{}: in shard local consensus error: {}", shardId, e);
		}
	}
}