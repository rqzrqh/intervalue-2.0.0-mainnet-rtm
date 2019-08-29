package one.inve.localfullnode2.sync.source;

import com.alibaba.fastjson.JSONObject;

import one.inve.core.EventBody;
import one.inve.localfullnode2.sync.DistributedObjects;
import one.inve.localfullnode2.sync.measure.Distribution;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: ISyncSource
 * @Description: if some bad blocks are detected,leave the blocks empty
 * @author Francis.Deng
 * @mailbox francis_xiiiv@163.com
 * @date Aug 15, 2019
 *
 */
public interface ISyncSource {
	ISyncSourceProfile getSyncSourceProfile();// call it once

	DistributedObjects<EventBody> getNotInDistributionEvents(Distribution dist);

	DistributedObjects<JSONObject> getNotInDistributionMessages(Distribution dist);
}
