package one.inve.localfullnode2.p2pcluster.ic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import one.inve.localfullnode2.p2pcluster.ic.P2PClusterClientBridge.Peer;

/**
 * 

 * Copyright © INVE FOUNDATION. All rights reserved.  
 *   
 * @Description: make preparation(simulating 3 nodes):
 * @formatter:off
 * dlv debug cluster_daemon.go -- -h=192.168.207.129 -p=33010 -icp=43010
 * dlv debug cluster_daemon.go -- -h=192.168.207.129 -p=33011 -g=192.168.207.129:33010 -icp=43011
 * dlv debug cluster_daemon.go -- -h=192.168.207.129 -p=33012 -g=192.168.207.129:33010 -icp=43012
 * @formatter:on
 *  
 * @author: Francis.Deng 
 * @date: Aug 5, 2019 1:12:20 AM 
 * @version: V1.0
 */
public class P2PClusterClientBridgeTest {
	private static final Logger logger = LoggerFactory.getLogger(P2PClusterClientBridgeTest.class);
	private final int port = 43010;
	private P2PClusterClientBridge target;

	@Before
	public void init() {
		target = new P2PClusterClientBridge(port);

	}

	@Test
	public void testSetMeta() {

		Map<String, String> meta = new HashMap<>();
		meta.put("king", "don");
		meta.put("queen", "yan");

		target.setMeta(meta);

		logger.info("Done");
	}

	@Test
	public void testGetMembers() {

		List<Peer> aliveMembers = target.getMembers().alive();
		List<Peer> suspectedMembers = target.getMembers().suspected();

		System.out.println(JSON.toJSONString(aliveMembers));
		System.out.println(JSON.toJSONString(suspectedMembers));
	}

}
