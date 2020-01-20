package org.jys.example.common.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author YueSong Jiang
 * @date 2020/1/20
 * all long about time is timestamp(unit is ms)
 */
public class SnowFlakeId {

    private static final Logger logger= LoggerFactory.getLogger(SnowFlakeId.class);

    /**
     * 机器id
     */
    private long workerId;

    /**
     * 数据中心id
     */
    private long dataCenterId;

    /**
     * 自增序列
     */
    private long sequence=0;

    private long startEpoch=1579511766213L;

    private long workerIdBits=5L;

    private long dataCenterIdBits=5L;

    private long sequenceBits=12L;

    private long workerIdShift = sequenceBits;

    private long dataCenterIdShift = sequenceBits + workerIdBits;

    private long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;


    /**
     * 得到0000000000000000000000000000000000000000000000000000111111111111
     * 用于对自增顺序位按位前几位置0
     */
    private long sequenceMask = ~(-1L << sequenceBits);

    /**
     * 上一次生成ID的时间
     */
    private long lastTimestamp = -1L;

    public SnowFlakeId(long workerId, long dataCenterId) {
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    public synchronized long nextId(){
        long timestamp=timeGen();
        if(timestamp < lastTimestamp){
            logger.error("clock is moving backwards.  Rejecting requests until {}.", lastTimestamp);
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds",
                    lastTimestamp - timestamp));
        }

        if(lastTimestamp == timestamp){
            //时间没有变化
            sequence=(sequence+1) & sequenceMask;
            if(sequence==0){
                //上一秒已经满了
                //等到下一秒，把组成id的时间置为下一秒
                timestamp =tilNextMills(lastTimestamp);
            }
        }else {
            //新的时间了，设置顺序位位0
            sequence=0;
        }

        lastTimestamp=timestamp;
        //注意这里减掉了起始时间，以获得更长时间的支持
        return ((timestamp-startEpoch) << timestampLeftShift) |
                (dataCenterId << dataCenterIdShift) |
                (workerId << workerIdShift) |
                sequence;
    }

    private long tilNextMills(long lastTimestamp){
        long timestamp=timeGen();
        while (timestamp <= lastTimestamp){
            timestamp=timeGen();
        }
        return timestamp;
    }

    public long timeGen(){
        return System.currentTimeMillis();
    }
}
