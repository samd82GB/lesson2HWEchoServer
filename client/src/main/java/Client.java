import handler.JsonDecoder;
import handler.JsonEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.ReferenceCountUtil;
import message.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

public class Client {
    public static void main(String[] args) {
        new Client().start();
    }

    public void start() {
        //Клиенту достаточно одного ThreadPool для обработки сообщений
        final NioEventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 3, 0, 3),
                                    new LengthFieldPrepender(3),
                                    new JsonDecoder(),
                                    new JsonEncoder(),
                                    new SimpleChannelInboundHandler<Message>() {
                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                            final FileRequestMessage message = new FileRequestMessage();
                                            message.setPath("g:\\GeekBrains\\06_Git\\progit_v2.1.73.pdf");
                                            ctx.writeAndFlush(message);
                                        }

                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
                                            System.out.println("receive msg: " + msg);
                                            if (msg instanceof FileContentMessage){
                                                FileContentMessage fcm = (FileContentMessage)msg;
                                                try (final RandomAccessFile accessFile = new RandomAccessFile("g:\\GeekBrains\\06_Git\\progit.pdf","rw")) {
                                                    System.out.println(fcm.getStartPosition());
                                                    accessFile.seek(fcm.getStartPosition());
                                                    accessFile.write(fcm.getContent());
                                                if (fcm.isLast()) {
                                                    ctx.close();
                                                    }

                                                } catch (IOException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }

                                            /*if (msg instanceof TextMessage) {
                                                TextMessage message = (TextMessage) msg;

                                            }
                                            if (msg instanceof DateMessage) {
                                                DateMessage message = (DateMessage) msg;
                                                System.out.println("receive date: " + message.getDate());
                                            }
                                            if (msg instanceof AuthMessage) {
                                                AuthMessage message = (AuthMessage) msg;
                                                System.out.println("receive login: " + message.getLogin());
                                                System.out.println("receive password: " + message.getPassword());
                                            }*/

                                        }
                                    }
                            );
                        }
                    });

            System.out.println("Client started");

            //ChannelFuture channelFuture = bootstrap.connect("localhost", 9000).sync();
            Channel channel = bootstrap.connect("localhost",9000).sync().channel();
            channel.closeFuture().sync();

            /*while (channelFuture.channel().isActive()) {
                TextMessage textMessage = new TextMessage();
                textMessage.setText(String.format("[%tD] %s", LocalDateTime.now(), Thread.currentThread().getName()));
                System.out.println("Try to send message: " + textMessage.getText());
                channelFuture.channel().writeAndFlush(textMessage);

                DateMessage dateMessage = new DateMessage();
                dateMessage.setDate(new Date());
                System.out.println("Try to send message: " + dateMessage.getDate());
                channelFuture.channel().write(dateMessage);

                AuthMessage authMessage = new AuthMessage();
                authMessage.setLogin("myLogin");
                authMessage.setPassword("myPassword");
                System.out.println("Try to send message: login: " + authMessage.getLogin() +", password: " + authMessage.getPassword());
                channelFuture.channel().write(authMessage);

                channelFuture.channel().flush();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }*/

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
