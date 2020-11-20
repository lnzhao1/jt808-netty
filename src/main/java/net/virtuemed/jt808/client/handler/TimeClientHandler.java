package net.virtuemed.jt808.client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author z
 */
public class TimeClientHandler extends ChannelInboundHandlerAdapter {

    /**
     * 客户端与服务器TCP链路链接成功后调用该方法
     * @param ctx
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        byte[] req = "7e0102000301983002708400013132336e7e".getBytes();
        ByteBuf firstMsg = Unpooled.buffer(req.length);
        firstMsg.writeBytes(req);
        ctx.writeAndFlush(firstMsg);//写入缓冲并且发送到socketchannel
    }

    /**
     * 读取到服务端相应后执行该方法
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        String body = new String(bytes, "UTF-8");
        System.out.println("服务端返回："+body);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("Unexpected exception from downstream : " + cause.getMessage());
        ctx.close();
    }
}
