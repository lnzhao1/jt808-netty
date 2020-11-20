package net.virtuemed.jt808.client.codex;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import net.virtuemed.jt808.util.JT808Util;
import net.virtuemed.jt808.vo.DataPacket;
import net.virtuemed.jt808.vo.req.*;
import net.virtuemed.jt808.vo.resp.CommonResp;
import net.virtuemed.jt808.vo.resp.RegisterResp;

import java.util.List;

import static net.virtuemed.jt808.config.JT808Const.*;

/**
 * @Author: Zpsw
 * @Date: 2019-05-15
 * @Description:JT808协议解码器,转义规则: 0x7d 0x01 -> 0x7d
 * 0x7d 0x02 -> 0x7e
 * @Version: 1.0
 */
@Slf4j
public class JT808ClientDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        log.debug("<<<<< ip:{},hex:{}", ctx.channel().remoteAddress(), ByteBufUtil.hexDump(in));
        DataPacket msg = decode(in);
        if (msg != null) {
            out.add(msg);
        }
    }

    private DataPacket decode(ByteBuf in) {
        if (in.readableBytes() < 12) { //包头最小长度
            return null;
        }
        //转义
        byte[] raw = new byte[in.readableBytes()];
        in.readBytes(raw);
        ByteBuf escape = revert(raw);
        //校验
        byte pkgCheckSum = escape.getByte(escape.writerIndex() - 1);
        escape.writerIndex(escape.writerIndex() - 1);//排除校验码
        byte calCheckSum = JT808Util.XorSumBytes(escape);
        if (pkgCheckSum != calCheckSum) {
            log.warn("校验码错误,pkgCheckSum:{},calCheckSum:{}", pkgCheckSum, calCheckSum);
            ReferenceCountUtil.safeRelease(escape);
            return null;
        }
        //解码
        return parse(escape);
    }

    /**
     * 将接收到的原始转义数据还原
     *
     * @param raw
     * @return
     */
    public ByteBuf revert(byte[] raw) {
        int len = raw.length;
        ByteBuf buf = ByteBufAllocator.DEFAULT.heapBuffer(len);//DataPacket parse方法回收
        for (int i = 0; i < len; i++) {
            //这里如果最后一位是0x7d会导致index溢出，说明原始报文转义有误
            if (raw[i] == 0x7d && raw[i + 1] == 0x01) {
                buf.writeByte(0x7d);
                i++;
            } else if (raw[i] == 0x7d && raw[i + 1] == 0x02) {
                buf.writeByte(0x7e);
                i++;
            } else {
                buf.writeByte(raw[i]);
            }
        }
        return buf;
    }

    public DataPacket parse(ByteBuf bb) {
        DataPacket packet = null;
        short msgId = bb.getShort(bb.readerIndex());
        switch (msgId) {
            case SERVER_RESP_COMMON:
                packet = new CommonResp(bb);
                break;
            case SERVER_RESP_REGISTER:
                packet = new RegisterResp(bb);
                break;
            default:
                packet = new DataPacket(bb);
                break;
        }
        packet.parse();
        return packet;
    }

    /**
     * 字节转16进制
     * @param bArr
     * @return
     */
    public String bytesToHexString(byte[] bArr) {
        StringBuffer sb = new StringBuffer(bArr.length);
        String sTmp;

        for (int i = 0; i < bArr.length; i++) {
            sTmp = Integer.toHexString(0xFF & bArr[i]);
            if (sTmp.length() < 2)
                sb.append(0);
            sb.append(sTmp.toUpperCase());
        }

        return sb.toString();
    }
}
