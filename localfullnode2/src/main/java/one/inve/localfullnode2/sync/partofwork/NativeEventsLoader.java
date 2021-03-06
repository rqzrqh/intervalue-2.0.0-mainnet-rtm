package one.inve.localfullnode2.sync.partofwork;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import one.inve.core.EventBody;
import one.inve.localfullnode2.store.rocks.RocksJavaUtil;
import one.inve.localfullnode2.store.rocks.key.EventIndexes;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: NativeEventsLoader
 * @Description: constantly load sorted event into {@code queue}
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Sep 9, 2019
 *
 */
public class NativeEventsLoader {
	private static final Logger logger = LoggerFactory.getLogger(NativeEventsLoader.class);

	protected void loadEventsInto(BlockingQueue<EventBody> queue, String dbId) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dbId);

				Map<String, String> m = rocksJavaUtil.startWith(EventIndexes.getConcensusEventSortPrefix());
				String[] keys = m.keySet().toArray(new String[m.size()]);
				Arrays.parallelSort(keys, new Comparator<String>() {

					@Override
					public int compare(String s1, String s2) {
						return EventIndexes.compareConcensusEventSortKey(s1, s2);
					}

				});

				for (String key : keys) {
					String pairStr = EventIndexes.getConcensusEventPair(key);
					byte[] evt = rocksJavaUtil.get(pairStr);
					if (null != evt && evt.length > 0) {
						EventBody event = JSONObject.parseObject(new String(evt), EventBody.class);
						try {
							queue.put(event);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

			}

		});
		t.start();
	}

}
