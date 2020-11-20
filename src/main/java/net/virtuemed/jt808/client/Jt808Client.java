package net.virtuemed.jt808.client;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import net.virtuemed.jt808.client.codex.JT808ClientDecoder;
import net.virtuemed.jt808.client.vo.ClientAuthMsg;
import net.virtuemed.jt808.codec.JT808Decoder;
import net.virtuemed.jt808.codec.JT808Encoder;
import net.virtuemed.jt808.config.JT808Const;

public class Jt808Client {
    public static void main(String[] args) throws Exception {
        String host = "127.0.0.1";
        int port = 9009;
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            /**
             * 如果你只指定了一个EventLoopGroup，
             * 那他就会即作为一个‘boss’线程，
             * 也会作为一个‘workder’线程，
             * 尽管客户端不需要使用到‘boss’线程。
             */
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            /**
             * 代替NioServerSocketChannel的是NioSocketChannel,这个类在客户端channel被创建时使用
             */
            b.channel(NioSocketChannel.class); // (3)
            /**
             * 不像在使用ServerBootstrap时需要用childOption()方法，
             * 因为客户端的SocketChannel没有父channel的概念。
             */
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
//                    ch.pipeline().addLast(new TimeClientHandler());
                    ch.pipeline().addLast(
                            new DelimiterBasedFrameDecoder(1100, Unpooled.copiedBuffer(new byte[]{JT808Const.PKG_DELIMITER}),
                                    Unpooled.copiedBuffer(new byte[]{JT808Const.PKG_DELIMITER, JT808Const.PKG_DELIMITER})));
                    ch.pipeline().addLast(new JT808ClientDecoder());
                    ch.pipeline().addLast(new JT808Encoder());
                }
            });
            //用connect()方法代替了bind()方法
            ChannelFuture f = b.connect(host, port).sync();
            if (f.isSuccess()) {
                System.err.println("连接服务器成功");
            }
//            sendAuthMessage(f);
            sendLocationMessage(f);
            //等到运行结束，关闭
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static void sendAuthMessage(ChannelFuture f) {
        ClientAuthMsg authMsg =ClientAuthMsg.zipClientAuthMsg((short) 1,"13002447017");
        authMsg.setAuthCode("asdf");
        f.channel().writeAndFlush(authMsg);
    }
    public static void sendLocationMessage(ChannelFuture f) {
        byte[]  req = ByteBufUtil.decodeHexDump("7e02000042019830027084014b000000000000000201dd1a6a0732396000000000014a190516033245010400000000eb1e000c00b2898607b40318c176948300060089ffffffff000600c5ffffffffa77e");
        ByteBuf firstMsg = Unpooled.buffer(req.length);
        firstMsg.writeBytes(req);
        System.out.println(ByteBufUtil.hexDump(firstMsg));
        f.channel().writeAndFlush(firstMsg);
    }
    /**
     * 补全位数不够的定长参数
     * byte[]
     *
     * @param length
     * @param pwdByte
     * @return 2016年10月12日 by fox_mt
     */
    private byte[] getBytesWithLengthAfter(int length, byte[] pwdByte) {
        byte[] lengthByte = new byte[length];
        for (int i = 0; i < pwdByte.length; i++) {
            lengthByte[i] = pwdByte[i];
        }
        for (int i = 0; i < (length - pwdByte.length); i++) {
            lengthByte[pwdByte.length + i] = 0x00;
        }
        return lengthByte;
    }

}
