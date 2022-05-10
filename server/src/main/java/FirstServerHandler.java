import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FirstServerHandler extends SimpleChannelInboundHandler<Message> {
    private int counter = 0;
    private RandomAccessFile accessFile;
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("New active channel");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws IOException {
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
        if (msg instanceof FileRequestMessage) { //получаем сообщение типа запроса на передачу
            FileRequestMessage frm = (FileRequestMessage) msg; //создаём объект сообщения типа запроса из сообщения
            System.out.println(frm.getPath());
            if (accessFile == null) { //если объект доступа к файлу пустой
                final File file = new File(frm.getPath()); //задаём файл с адресом
                accessFile = new RandomAccessFile(file, "r"); //создаём объект доступа к файлу в режиме чтения
                sendFile(ctx); //вызываем метод отправки файла
            }
        }
    }

    private void sendFile(ChannelHandlerContext ctx) throws IOException {
            if (accessFile != null) { //если объект доступа к файлу не нулевой
                final byte[] fileContent; //создаём массив байт для передачи пакета
                final long available = accessFile.length()-accessFile.getFilePointer(); //вычисляем остаток байт для передачи
                if (available > 64 * 1024) {    //если остаток больше заданного размера пакета
                    fileContent = new byte[64 * 1024]; //размер пакета будет равен заданному размеру
                 } else {
                    fileContent = new byte [(int)available]; //иначе размер пакета равен остатку байт
                }
                final FileContentMessage message = new FileContentMessage(); //создаём объект сообщения типа содержимого сообщения
                message.setStartPosition(accessFile.getFilePointer()); //задаём место чтения в сообщении
                accessFile.read(fileContent); //читаем из файла в пакет заданного размера
                message.setContent(fileContent); //записываем считанный пакет в содержимое сообщения
                final boolean last = accessFile.getFilePointer()==accessFile.length();
                message.setLast(last); //устанавливаем флаг последнего байта если файл считан до длины файла

                ctx.writeAndFlush(message) //ставим в очередь задачу передачи
                        .addListener(new ChannelFutureListener() { //добавляем листенер, который при окончании операции вызывает метод
                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {
                                if (!last) {
                                    sendFile(ctx); //если байт не последний, то снова вызываем метод отправки файла
                                }
                            }
                        });
                if (last) {
                    accessFile.close();
                    accessFile = null;
                }
                System.out.println("Message sent "+ ++counter);
        }
        }





    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws IOException {
        System.out.println("client disconnect");
        if (accessFile != null) {
            accessFile.close();
        }
    }
}
