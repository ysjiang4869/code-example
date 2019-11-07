package org.jys.example.kafka.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * @author YueSong Jiang
 * @date 2019/9/23
 */
public class RandomUtils {

    private static final Random RANDOM =new Random();

    private static final Logger logger= LoggerFactory.getLogger(RandomUtils.class);

    public static final int MAX_SERVICE=1<<3;
    public static final int MAX_NODE_NUM=1<<12;
    private static final int MAX_RANDOM_NUM=1<<17;

    /**
     * get record id based on pass time
     * 3 bit service num
     * 12 bit node num
     * 32 bit pass time
     * 17 bit random num
     * @param passTime
     */
    public static long getRandomRecordId(long passTime){
        int randomNum=getSeqNum();
        return getRandomRecordId(passTime,randomNum);
    }

    public static long getRandomRecordId(long passTime, int randomNum){
        int serviceNum=2;
        int nodeNum=1;
        logger.debug("using service name {}, node num {}, pass time {}, random num {}",serviceNum,nodeNum,passTime,randomNum);
        long id=setBit(0,serviceNum,0,3);
        id=setBit(id,nodeNum,3,12);
        id=setBit(id,passTime,15,32);
        id=setBit(id,randomNum,47,17);
        logger.debug("get record id {},hex string is {}",id,Long.toHexString(id));
        return id;
    }
    public static int num=-1;
    public static int getSeqNum(){
        synchronized ("xx"){
            if(num==MAX_RANDOM_NUM){
                num=-1;
            }
            num++;
        }
        return num;
    }


    public static long getTimeFromId(long id){
        return (id<<15)>>32;
    }

    private static  long setBit(long raw, long v, int start,int len){
        v=v&((((long)1)<<len)-1);
        return raw|(v<<(64-start-len));
    }

    private static String[] codes=new String[]{"1234ds", "xxccfdeww","xww3ed","737dhsyegd6y3",
            "ddr3edrf4fd", "334455fdctvt","xe3fdrfdf3","xedf43dwqf54f","xed32 ded","xe33434f5g","we332dsdfr"};


    public static String getRandomMac(){
        String[] mac = {
                String.format("%02x", RANDOM.nextInt(0xff)&(~2)),
                String.format("%02x", RANDOM.nextInt(0xff)),
                String.format("%02x", RANDOM.nextInt(0xff)),
                String.format("%02x", RANDOM.nextInt(0xff)),
                String.format("%02x", RANDOM.nextInt(0xff)),
                String.format("%02x", RANDOM.nextInt(0xff)),
//                String.format("%02x",random.nextInt(0xff)),
//                String.format("%02x",random.nextInt(0xff)),
        };
        return String.join("-",mac).toUpperCase();
    }

    public static String getRandomIndetity(){
        int index= RANDOM.nextInt(codes.length);
        return codes[index];
    }
}
