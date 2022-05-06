import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FirstServerHandler extends SimpleChannelInboundHandler<Message> {
    private int counter = 0;
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("New active channel");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        /*if (msg instanceof TextMessage) {
            TextMessage message = (TextMessage) msg;
            System.out.println("incoming text message: " + message.getText());
            ctx.writeAndFlush(msg);
        }
        if (msg instanceof DateMessage) {
            DateMessage message = (DateMessage) msg;
            System.out.println("incoming date message: " + message.getDate());
            ctx.writeAndFlush(msg);
        }
        if (msg instanceof AuthMessage) {
            AuthMessage message = (AuthMessage) msg;
            System.out.println("incoming login message: " + message.getLogin());
            System.out.println("incoming password message: " + message.getPassword());
            ctx.writeAndFlush(msg);
        }*/
        if (msg instanceof FileRequestMessage) {

            FileRequestMessage frm = (FileRequestMessage) msg;
            System.out.println(frm.getPath());
            final File file = new File(frm.getPath());
            try (RandomAccessFile accessFile = new RandomAccessFile(file, "r")) {
               while (accessFile.getFilePointer() != accessFile.length()){
                   final byte[] fileContent;
                   final long available = accessFile.length()-accessFile.getFilePointer();
                   if (available > 64 * 1024) {
                       fileContent = new byte[64 * 1024];
                   } else {
                       fileContent = new byte [(int)available];
                   }
                   final FileContentMessage message = new FileContentMessage();
                   message.setStartPosition(accessFile.getFilePointer());
                   accessFile.read(fileContent);
                   message.setContent(fileContent);
                   message.setLast(accessFile.getFilePointer()==accessFile.length());
                   ctx.writeAndFlush(message);
                   System.out.println("Message sent "+ ++counter);
                   }
               } catch (IOException e) {
                throw new RuntimeException();
            }
            }
        }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("client disconnect");
    }
}
