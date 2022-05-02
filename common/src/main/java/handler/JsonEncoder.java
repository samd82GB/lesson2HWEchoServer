package handler;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import message.AuthMessage;
import message.DateMessage;
import message.Message;
import message.TextMessage;


public class JsonEncoder extends MessageToByteEncoder<Message> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        if (msg instanceof TextMessage) {
            TextMessage message = (TextMessage) msg;
            System.out.println("send text: " + message.getText());
        }
        if (msg instanceof DateMessage) {
            DateMessage message = (DateMessage) msg;
            System.out.println("send date: " + message.getDate());
        }
        if (msg instanceof AuthMessage) {
            AuthMessage message = (AuthMessage) msg;
            System.out.println("send login: " + message.getLogin());
            System.out.println("send password: " + message.getPassword());
        }
        byte[] value = OBJECT_MAPPER.writeValueAsBytes(msg);
        out.writeBytes(value);
    }


}


