package org.jys.example.kafka.common;



import org.jys.example.kafka.entity.Vehicle;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

/**
 * @author YueSong Jiang
 * @date 2019/9/23
 */
public class VehicleRandom {

    public static Vehicle getRandomVehicle(){
        //创建随机数
        Random random = new Random();
        long time= Instant.now().minus(random.nextInt(10*60), ChronoUnit.SECONDS).getEpochSecond();
        long id=RandomUtils.getRandomRecordId(time);
        Vehicle vehicle=new Vehicle();
        //数据唯一标识
        int x=random.nextInt(2);
        vehicle.setPassTime(Instant.now().getEpochSecond());
        vehicle.setRecordID(id);
        //关联卡口编号
        vehicle.setTollgateID(Integer.toString(random.nextInt(1_0)));
        //设备编号
        vehicle.setDeviceID(vehicle.getTollgateID());
        //有无车牌
        vehicle.setHasPlate(random.nextInt(2));
        //号牌种类
        vehicle.setPlateClass(random.nextInt(27)+1);
        //车牌颜色
        vehicle.setPlateColor(random.nextInt(15));
        //车牌号
        String plateNo = "浙" + (char) (Math.random() * 26 + 'A') + (int) (Math.random() * 10000 + 90000);
        vehicle.setPlateNo(plateNo);

        //行驶方向
        vehicle.setDirection(random.nextInt(9));
        //车辆品牌
        vehicle.setVehicleBrand(random.nextInt(108));
        //车身颜色
        vehicle.setVehicleColor(random.nextInt(15));
        //颜色深浅
        vehicle.setVehicleColorDepth(random.nextInt(2));

        vehicle.setStorageUrlCloseShot("/picture/2018-08-17T08-29-20Z/1534495880605_3865.jpg");
        vehicle.setLaneNo(0);
        vehicle.setSpeed(0.0000);
        vehicle.setVehicleLeftTopX(0.0000);
        vehicle.setVehicleRightBtmX(0.0000);
        vehicle.setVehicleLeftTopY(0.0000);
        vehicle.setVehicleRightBtmY(0.0000);
        vehicle.setVehicleClass(99);
        vehicle.setVehicleModel(0);
        vehicle.setVehicleAppearTime(0L);
        vehicle.setVehicleDisappearTime(0L);
        vehicle.setPlateReliability(0);
        vehicle.setBrandReliability(0);
        return vehicle;
    }
}
