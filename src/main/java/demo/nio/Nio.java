package demo.nio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

/**
 * Created by jiashiran on 2016/10/28.
 */
public class Nio {

    public static void main(String[] args) {
       Nio io = new Nio();

        //io.buffer();
        //io.transfter();
        //io.selector();
        io.asynchronous();
        CountDownLatch c = new CountDownLatch(1);
        /*try {
            c.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        //因为AIO不会阻塞调用进程，因此必须在主进程阻塞，才能保持进程存活。
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void selector(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try (Selector selector = Selector.open();
                     ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                     ServerSocketChannel s1 = ServerSocketChannel.open()){

                    serverSocketChannel.configureBlocking(false);
                    serverSocketChannel.socket().setReuseAddress(true);
                    serverSocketChannel.socket().bind(new InetSocketAddress(8080));
                    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

                    s1.configureBlocking(false);
                    s1.socket().setReuseAddress(true);
                    s1.socket().bind(new InetSocketAddress(8088));
                    s1.register(selector, SelectionKey.OP_ACCEPT);

                    while (selector.select() > 0 ){

                        Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
                        while (selectionKeys.hasNext()){
                            SelectionKey key = selectionKeys.next();
                            selectionKeys.remove();//删除select得到的selectionKey

                            if(key.isAcceptable()){
                                log("a Acceptable channel");
                                ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                                SocketChannel socketChannel = channel.accept();
                                socketChannel.configureBlocking(false);
                                ByteBuffer bf = ByteBuffer.allocate(1024);
                                String a = null;
                                while (socketChannel.read(bf) > 0){
                                    bf.flip();byte[] s = bf.array();
                                    a = new String(s);
                                    log(a);
                                    bf.clear();
                                }
                                if(a != null && a.length() > 0){
                                    socketChannel.register(selector,SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                                }else {
                                    socketChannel.close();
                                }

                            }else if(key.isConnectable()){
                                log("a Connectable channel");
                                SocketChannel socketChannel = (SocketChannel) key.channel();
                                socketChannel.configureBlocking(false);
                                //如果正在连接，则完成连接
                                if(socketChannel.isConnectionPending()){
                                    socketChannel.finishConnect();
                                }
                                //连接成功后，注册接收服务器消息的事件
                                //socketChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                                log("客户端连接成功");
                            }else  if(key.isReadable()){
                                log("a Readable channel");
                                SocketChannel socketChannel = (SocketChannel) key.channel();
                                socketChannel.configureBlocking(false);
                                ByteBuffer bf = ByteBuffer.allocate(1024);
                                while (socketChannel.read(bf) > 0){
                                    bf.flip();
                                    byte[] s = bf.array();
                                    log(new String(s));
                                    bf.clear();
                                }
                                socketChannel.register(selector,SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                            }else if(key.isWritable()){
                                log("a Writable channel");
                                SocketChannel socketChannel = (SocketChannel) key.channel();
                                socketChannel.configureBlocking(false);
                                ByteBuffer bf = ByteBuffer.allocate(1024);
                                bf.put("<html><body><h1>a Writable channel</h1></body></html>".getBytes());
                                bf.flip();
                                while (bf.hasRemaining()){
                                    socketChannel.write(bf);
                                }
                                socketChannel.close();
                                //socketChannel.register(selector,SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                            }
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void asynchronous(){
        try {
            AsynchronousChannelGroup group = AsynchronousChannelGroup.withCachedThreadPool(
                    Executors.newCachedThreadPool(),10);
            AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open(group);
            server.setOption(StandardSocketOptions.SO_REUSEADDR,true);
            server.setOption(StandardSocketOptions.SO_RCVBUF,16 * 1024);

            server.bind(new InetSocketAddress("localhost",8080));
            log("server start");
            server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>(){
                ByteBuffer bf = ByteBuffer.allocate(1024);
                /**
                 * Invoked when an operation has completed.
                 *
                 * @param result     The result of the I/O operation.
                 * @param attachment
                 */
                @Override
                public void completed(AsynchronousSocketChannel result, Object attachment) {
                    server.accept(null,this);
                    try{
                        log("completed");
                        bf.clear();
                        result.read(bf);
                        bf.flip();
                        log(new String(bf.array()));
                        //bf.clear();
                        //bf.put("asaef".getBytes());
                        result.write(bf);
                        //bf.flip();
                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        try {
                            result.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }

                /**
                 * Invoked when an operation fails.
                 *
                 * @param exc        The exception to indicate why the I/O operation failed
                 * @param attachment
                 */
                @Override
                public void failed(Throwable exc, Object attachment) {
                    log("fail");
                    exc.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void transfter(){
        try {
            RandomAccessFile file = new RandomAccessFile("C:\\go1.7/api/go1.7.txt","rw");
            FileChannel from = file.getChannel();
            RandomAccessFile toFile = new RandomAccessFile("C:\\a.txt","rw");
            FileChannel to = toFile.getChannel();
            long position = 0;
            long count = from.size();
            long i = to.transferFrom(from,position,count);
            System.out.println(i);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buffer(){
        ByteBuffer bf = ByteBuffer.allocate(1024);

        bf.put(Byte.valueOf("70"));bf.put(Byte.valueOf("71"));bf.put(Byte.valueOf("73"));
        bf.flip();
        //bf.clear();
       // System.out.println(bf.get());
        log(new String(bf.array()));
    }

    public static void log(String ...args){
        for (String a : args){
            System.out.print(a);
        }
        System.out.println();
    }

}
