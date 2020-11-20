package net.virtuemed.jt808.client.vo;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.virtuemed.jt808.config.JT808Const;
import net.virtuemed.jt808.vo.DataPacket;
import org.apache.commons.lang3.StringUtils;

@Data
public class ClientAuthMsg extends DataPacket {
    private String authCode; //鉴权码
    @Override
    public ByteBuf toByteBufMsg() {
        ByteBuf bb = super.toByteBufMsg();
        if (StringUtils.isNotBlank(authCode)) {//成功才写入鉴权码
            bb.writeBytes(authCode.getBytes(JT808Const.DEFAULT_CHARSET));
        }
        return bb;
    }
    public ClientAuthMsg() {
        this.getHeader().setMsgId(JT808Const.TERNIMAL_MSG_AUTH);
    }
    public static ClientAuthMsg zipClientAuthMsg(short flowId,String terminalPhone){
        ClientAuthMsg clientAuthMsg = new ClientAuthMsg();
        clientAuthMsg.getHeader().setTerminalPhone(terminalPhone);
        clientAuthMsg.getHeader().setFlowId(flowId);
        return clientAuthMsg;
    }
}
