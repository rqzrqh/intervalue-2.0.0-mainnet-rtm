package one.inve.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import one.inve.bean.message.MessageType;
import one.inve.bean.message.SnapshotMessage;
import one.inve.beans.dao.TransactionSplit;
import one.inve.core.Config;
import one.inve.core.EventBody;
import one.inve.core.EventKeyPair;
import one.inve.db.transaction.MysqlHelper;
import one.inve.db.transaction.QueryTableSplit;
import one.inve.rocksDB.RocksJavaUtil;
import one.inve.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 快照消息持久化逻辑处理类
 * @author Clare
 * @date   2018/11/2 0002.
 */
public class SnapshotDbService {
    private static final Logger logger = LoggerFactory.getLogger(SnapshotDbService.class);

    /**
     * 查询最新快照消息
     *
     * @param dbId 数据库ID
     * @return 最新快照消息
     */
    public static SnapshotMessage queryLatestSnapshotMessage(String dbId) {
        logger.info("queryLatestSnapshotMessage...");
        SnapshotMessage maxSnapshot = null;
        try {
            String snapHash = queryLatestSnapshotMessageHash(dbId);
            if(StringUtils.isNotEmpty(snapHash)) {
                String json = querySnapshotMessageFormatStringByHash(dbId, snapHash);
                if(StringUtils.isNotEmpty(json)){
                    String smapshotStr = JSON.parseObject(json).getString("message");
                    maxSnapshot = JSON.parseObject(smapshotStr, SnapshotMessage.class);
                } else {
                    logger.error("node-{}: snapshotMessage in mysql diff from in Rocksdb. " +
                                    "in mysql: {}, but in Rocksdb is null", dbId, snapHash);
                    System.exit(-1);
                }
            }
            return maxSnapshot;
        } catch (Exception e) {
            logger.error(">>>>>> queryLatestSnapshotMessage() ERROR: {}", e);
            return maxSnapshot;
        }
    }

    /**
     * 查询最新快照消息的hash值
     *
     * @param dbId 数据库ID
     * @return 最新快照消息的hash值
     */
    public static String queryLatestSnapshotMessageHash(String dbId){
        //查找最大的TransactionSplit
        TransactionSplit split = QueryTableSplit.tableExist(dbId);
        if (null == split) {
            logger.warn("messages_* tables not exist!!!");
            return null;
        }
        int type = MessageType.SNAPSHOT.getIndex();
        String initSql = "select hash from %s where type = '%d' order by id desc limit 1";
        String sql = String.format(initSql, split.getTableName(), type);
        MysqlHelper h = null;
        try {
            h = new MysqlHelper(dbId);
            List<String> messageHashs =  h.executeQuery(sql, (rs, index) -> rs.getString("hash"));
            BigInteger i = split.getTableIndex().subtract(BigInteger.ONE);
            while(i.compareTo(BigInteger.ZERO)>=0 && (messageHashs==null||messageHashs.size()<=0) ){
                sql = String.format(initSql, split.getTableNamePrefix()+Config.SPLIT+i.toString(), type);
                messageHashs =  h.executeQuery(sql, (rs, index) -> rs.getString("hash"));
                i = i.subtract(BigInteger.ONE);
            }
            if(messageHashs!=null&&messageHashs.size()>0){
                return messageHashs.get(0);
            }else{
                logger.warn("local db {} latest snapshot message not exist!!!", dbId);
                return null;
            }
        } catch (SQLException e) {
            logger.error("querySnapshotMessageHashByVersion(): {}", e);
            return null;
        } finally {
            if(h!=null) {
                h.destroyed();
            }
        }
    }

    /**
     * 根据快照消息hash值，查询对应的快照消息，并以字符串形式返回
     *
     * @param dbId 数据库ID
     * @param hash 快照消息hash值
     * @return 快照消息（以字符串形式返回）
     */
    public static String querySnapshotMessageFormatStringByHash(String dbId, String hash) {
//        logger.info("querySnapshotMessageFormatStringByHash...hash: {}", hash);
        String snapshotStr = null;
        try {

            RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dbId);
            byte[] snapshotVersionByte = rocksJavaUtil.get(hash);
            if(snapshotVersionByte!=null){
                snapshotStr = new String(snapshotVersionByte);
            }

            return snapshotStr;
        } catch (Exception e) {
            logger.error(">>>>>> querySnapshotMessageFormatStringByHash() ERROR: {}", e);
            return null;
        }
    }

    /**
     * 根据快照消息hash值，查询对应的快照消息，并以实体对象形式返回
     *
     * @param dbId 数据库ID
     * @param hash 快照消息hash值
     * @return 快照消息（以实体对象形式返回）
     */
    public static SnapshotMessage querySnapshotMessageByHash(String dbId, String hash) {
//        logger.info("querySnapshotMessageByHash...hash: {}", hash);
        String json = querySnapshotMessageFormatStringByHash(dbId, hash);
        if(StringUtils.isNotEmpty(json)){
            return JSON.parseObject(JSON.parseObject(json).getString("message"), SnapshotMessage.class);
        } else {
            return null;
        }
    }

    /**
     * 根据快照消息版本号，查询对应的快照消息，并以字符串形式返回
     *
     * @param dbId 数据库ID
     * @param version 快照消息版本号
     * @return 版本{version}快照消息，并以字符串形式返回
     */
    public static String querySnapshotMessageFormatStringByVersion(String dbId, String version){
//        logger.info("querySnapshotMessageFormatStringByVersion...version: {}", version);
        String hash = querySnapshotMessageHashByVersion(dbId, version);
        if (StringUtils.isNotEmpty(hash)) {
            return querySnapshotMessageFormatStringByHash(dbId, hash);
        } else {
            return null;
        }
    }

    /**
     * 根据快照消息hash值，查询对应的快照消息，并以实体对象形式返回
     *
     * @param dbId 数据库ID
     * @param version 快照消息版本号
     * @return 版本{version}快照消息（以实体对象形式返回）
     */
    public static SnapshotMessage querySnapshotMessageByVersion(String dbId, String version) {
//        logger.info("querySnapshotMessageByVersion...version: {}", version);
        String json = querySnapshotMessageFormatStringByVersion(dbId, version);
        if(StringUtils.isNotEmpty(json)){
            return JSON.parseObject(JSON.parseObject(json).getString("message"), SnapshotMessage.class);
        } else {
            return null;
        }
    }

    /**
     * 根据快照消息版本号，查询对应的快照消息hash值
     *
     * @param dbId 数据库ID
     * @param version 快照消息版本号
     * @return 版本{version}快照消息hash值
     */
    public static String querySnapshotMessageHashByVersion(String dbId, String version){
        //查找最大的TransactionSplit
        TransactionSplit split = QueryTableSplit.tableExist(dbId);
        if (null == split) {
            logger.error("messages_* tables not exist!!!");
            return null;
        }
        int type = MessageType.SNAPSHOT.getIndex();
        String initSql = "select hash from %s where type = '%d' and snapshot = '%s' order by id desc limit 1";
        String sql = String.format(initSql, split.getTableName(), type, version);
        MysqlHelper h = null;
        try {
            h = new MysqlHelper(dbId);
            List<String> messageHashs =  h.executeQuery(sql, (rs, index) -> rs.getString("hash"));
            BigInteger i = split.getTableIndex().subtract(BigInteger.ONE);
            while(i.compareTo(BigInteger.ZERO)>=0 && (messageHashs==null||messageHashs.size()<=0) ){
                sql = String.format(initSql, split.getTableNamePrefix()+Config.SPLIT+i.toString(), type, version);
                messageHashs =  h.executeQuery(sql, (rs, index) -> rs.getString("hash"));
                i = i.subtract(BigInteger.ONE);
            }
            if (messageHashs!=null && messageHashs.size()>0){
                return messageHashs.get(0);
            } else {
                logger.error("local db {} snapshot message {} not exist!!!", dbId, version);
                return null;
            }
        } catch (SQLException e) {
            logger.error("querySnapshotMessageHashByVersion(): {}", e);
            return null;
        } finally {
            if(h!=null) {
                h.destroyed();
            }
        }
    }

    /**
     * 删除快照点Event之前的所有Event
     *
     * @param dbId 数据库ID
     * @param eb 快照点Event
     * @param nValue 分片总节点数
     */
    public static void deleteEventsBeforeSnapshotPointEvent(String dbId, EventBody eb, int nValue) {
        logger.info("deleteEventsBeforeSnapshotPointEvent...{}", JSON.toJSONString(eb));

        try {
            if (eb != null) {
                RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dbId);

                Map<Long, EventKeyPair> map = new HashMap<>();
                map = getPrevEventKeyPairsForEachNode(dbId, eb, map, nValue);

                logger.warn("delete event max map values: {} ", JSONArray.toJSONString(map.values()));
                for (EventKeyPair keyPair : map.values()) {
                    for (int seq = 0; seq <= keyPair.seq; seq++) {
                        EventKeyPair keyPairOther = new EventKeyPair(keyPair.shardId, keyPair.creatorId, seq);
                        rocksJavaUtil.delete(keyPairOther.toString());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("deleteEventsBeforeSnapshotPointEvent save error: {}", e);
        }
    }


    /**
     * 递归获取每根柱子上离得最近的第一个event
     *
     * @param dbId 数据库ID
     * @param eb
     * @param map
     * @param nValue 分片总节点数
     * @return 每根柱子上离指定event最近的前一个event
     */
    public static Map<Long, EventKeyPair> getPrevEventKeyPairsForEachNode(String dbId,
                                                                          EventBody eb,
                                                                          Map<Long, EventKeyPair> map,
                                                                          int nValue) {
        int sharId      = eb.getShardId();
        Long creatId    = eb.getCreatorId();
        Long creatSeq   = eb.getCreatorSeq();
        Long otherSeq   = eb.getOtherSeq();
        Long otherId    = eb.getOtherId();
        EventKeyPair keyPair = new EventKeyPair(sharId, creatId, creatSeq);
        if (creatId != null && map.get(creatId) == null) {
            map.put(creatId, keyPair);
        }
        if (map.keySet().size() == nValue) {
            return map;
        }
        EventKeyPair keyPairOther = new EventKeyPair(sharId, otherId, otherSeq);
        RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dbId);
        byte[] keyPairByte = rocksJavaUtil.get(keyPairOther.toString());
        if (keyPairByte != null) {
            String json = new String(keyPairByte);
            eb = JSON.parseObject(json, EventBody.class);
            getPrevEventKeyPairsForEachNode(dbId, eb, map, nValue);
        }
        return map;
    }

    public static void main(String[] args) {
        String dbId = "0_0";

        String hash = queryLatestSnapshotMessageHash(dbId);
        System.out.println("hash: " + hash);

        SnapshotMessage sm0 = null;
        do {
            if (StringUtils.isEmpty(hash)) {
                break;
            }
            sm0 = querySnapshotMessageByHash(dbId, hash);
            if (null == sm0) {
                break;
            }
            String root = sm0.getSnapshotPoint().getMsgHashTreeRoot();
            BigInteger version = sm0.getSnapVersion();
            System.out.println("\nversion: " + version + ", tree root : " + root);
//            System.out.println("snapshotPoint : " + JSON.toJSONString(sm0.getSnapshotPoint()));

            EventBody eb = sm0.getSnapshotPoint().getEventBody();
            int sharId      = eb.getShardId();
            Long creatId    = eb.getCreatorId();
            Long creatSeq   = eb.getCreatorSeq();
            EventKeyPair keyPair = new EventKeyPair(sharId, creatId, creatSeq);
            System.out.println("keyPair: " + keyPair + ", transCount: " + eb.getTransCount());
            RocksJavaUtil rocksJavaUtil = new RocksJavaUtil(dbId);
            byte[] keyPairByte = rocksJavaUtil.get(keyPair.toString());
            if (keyPairByte != null) {
                EventBody ebWhole = JSON.parseObject(new String(keyPairByte), EventBody.class);
                byte[][] trans = ebWhole.getTrans();
                if (null!=trans && trans.length>0) {
                    String msg = new String(trans[trans.length-1]);
                    System.out.println("msg: " + msg);
                } else {
                    System.out.println("msg: null");
                }
            }

            String eHash = sm0.geteHash();
            EventBody eb1 = JSON.parseObject(new String(keyPairByte), EventBody.class);
            int sharId1      = eb1.getShardId();
            Long creatId1    = eb1.getCreatorId();
            Long creatSeq1   = eb1.getCreatorSeq();
            EventKeyPair keyPair1 = new EventKeyPair(sharId1, creatId1, creatSeq1);
            System.out.println("eHash: " + eHash + ", keyPair: " + keyPair1 + ", transCount: " + eb1.getTransCount());


            hash = sm0.getPreHash();
        } while (StringUtils.isNotEmpty(sm0.getPreHash()));

//        for (int i = 0; i < 2; i++) {
//            SnapshotMessage sm = querySnapshotMessageByHash(dbId, hash);
//            String root = sm.getSnapshotPoint().getMsgHashTreeRoot();
//            BigInteger version = sm.getSnapVersion();
//            System.out.println("version: " + version + ", tree root : " + root);
//
//            hash = sm.getPreHash();
//        }
//
//        String hash7 = querySnapshotMessageHashByVersion(dbId, "2");
//        System.out.println("\nhash7: \n" + hash7);
//
//        SnapshotMessage sm = queryLatestSnapshotMessage(dbId);
//        System.out.println("\nsm: \n" + JSON.toJSONString(sm));
//
//        BigInteger snapVersion = sm.getSnapVersion();
//        System.out.println("\nsnapVersion: \n" + snapVersion);
//        String preHash = sm.getPreHash();
//        System.out.println("\npreHash: \n" + preHash);
//        String snapHash = sm.getHash();
//        System.out.println("\nsnapHash: \n" + snapHash);
//        String eHash = sm.geteHash();
//        System.out.println("\neHash: \n" + eHash);
//        SnapshotPoint sp = sm.getSnapshotPoint();
//        System.out.println("\nsp: \n" + JSON.toJSONString(sp));
//        EventBody eb = sp.getEventBody();
//        System.out.println("\neb: \n" + JSON.toJSONString(eb));
//
//        SnapshotMessage sm1 = querySnapshotMessageByHash(dbId, snapHash);
//        System.out.println("\nsp: \n" + JSON.toJSONString(sm1 != null ? sm1.getSnapshotPoint() : null));
//
//        SnapshotMessage sm2 = querySnapshotMessageByVersion(dbId, "7");
//        System.out.println("\nsp: \n" + JSON.toJSONString(sm2 != null ? sm2.getSnapshotPoint() : null));
//
//        String snapmsg = querySnapshotMessageFormatStringByHash(dbId, snapHash);
//        System.out.println("\nsnap msg: \n" + snapmsg);
//
//        String snapmsg1 = querySnapshotMessageFormatStringByVersion(dbId, "7");
//        System.out.println("\nsnap msg1: \n" + snapmsg1);
    }

}
