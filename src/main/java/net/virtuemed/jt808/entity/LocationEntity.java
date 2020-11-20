package net.virtuemed.jt808.entity;

import lombok.Data;
import net.virtuemed.jt808.vo.req.LocationMsg;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @Author: Zpsw
 * @Date: 2019-05-15
 * @Description:
 * @Version: 1.0
 */
@Data
@Entity
@Table(name = "location_log")
public class LocationEntity extends AbstractPersistable<Long> {

    private String terminalPhone; // 终端手机号
    private Integer alarm;//报警
    private Integer statusField;//状态
    private Float latitude;//纬度
    private Float longitude;//经度
    private Short elevation;//高程
    private Short speed;//速度
    private Short direction;//方向
    private String time;//时间

    public static LocationEntity parseFromLocationMsg(LocationMsg msg) {
        LocationEntity location = new LocationEntity();
        location.setTerminalPhone(msg.getHeader().getTerminalPhone());
        BeanUtils.copyProperties(msg, location);
        return location;
    }
}
