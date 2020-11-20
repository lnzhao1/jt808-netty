package net.virtuemed.jt808.vo.resp;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.virtuemed.jt808.config.JT808Const;
import net.virtuemed.jt808.util.BCD;
import net.virtuemed.jt808.vo.DataPacket;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: Zpsw
 * @Date: 2019-05-15
 * @Description:注册响应包
 * @Version: 1.0
 */
@Data
public class RegisterResp extends DataPacket {

    public static final byte SUCCESS = 0;//成功
    public static final byte VEHICLE_ALREADY_REGISTER = 1;//车辆已被注册
    public static final byte NOT_IN_DB = 2;//数据库无该车辆
    public static final byte TERMINAL_ALREADY_REGISTER = 3;//终端已被注册

    private short replyFlowId; //应答流水号 2字节
    private byte result;    //结果 1字节
    private String authCode; //鉴权码

    public RegisterResp() {
        this.getHeader().setMsgId(JT808Const.SERVER_RESP_REGISTER);
    }

    public RegisterResp(ByteBuf byteBuf) {
        super(byteBuf);
    }
    @Override
    public ByteBuf toByteBufMsg() {
        ByteBuf bb = super.toByteBufMsg();
        bb.writeShort(replyFlowId);
        bb.writeByte(result);
        if (result == SUCCESS && StringUtils.isNotBlank(authCode)) {//成功才写入鉴权码
            bb.writeBytes(authCode.getBytes(JT808Const.DEFAULT_CHARSET));
        }
        return bb;
    }

    public static RegisterResp success(DataPacket msg, short flowId) {
        RegisterResp resp = new RegisterResp();
        resp.getHeader().setTerminalPhone(msg.getHeader().getTerminalPhone());
        resp.getHeader().setFlowId(flowId);
        resp.setReplyFlowId(msg.getHeader().getFlowId());
        resp.setResult(SUCCESS);
        resp.setAuthCode("SUCCESS");
        return resp;
    }
    @Override
    public void parseBody() {
        ByteBuf bb = this.payload;
        this.setReplyFlowId(bb.readShort());
        this.setResult(bb.readByte());
        this.setAuthCode(dealEmpty(new String(bb.readBytes(bb.readableBytes()).array(), JT808Const.DEFAULT_CHARSET)));
    }

    /**
     * 处理空字符串
     * @param str
     * @return
     */
    public String dealEmpty(String str){
        Pattern pattern = Pattern.compile("([^\u0000]*)");
        Matcher matcher = pattern.matcher(str);
        if(matcher.find(0)){
            try {
                str = new String(matcher.group(1).getBytes("utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return str;
    }
}
