
package server.nbtest_zhilian;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import com.alibaba.fastjson.JSONObject;

import server.ClientSocketUtil;
import server.Int2ByteUtil;


public class MyServerSocketZhilian {
    
    private int                                      port                 = 20000;
    
    private LinkedBlockingQueue<byte[]>              queue                = new LinkedBlockingQueue<byte[]>();
    
    private DatagramSocket                             socket         = null;
    
    public LinkedBlockingQueue<byte[]> getQueue() {
        
        return queue;
    }
    
    public void setQueue(LinkedBlockingQueue<byte[]> queue) {
        
        this.queue = queue;
    }
    
    public void start() {
        
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                
                try {
                    socket =new DatagramSocket(port);
                    System.out.println("服务端UDP服务启动监听：" + port);
                    while (true) {
                        DatagramPacket packet =new DatagramPacket(new byte[512],512);
                        socket.receive(packet);
                        System.out.println("发送方："+packet.getSocketAddress());
                        byte[] upByte = packet.getData();
                        
                        System.out.println("rec:"+Int2ByteUtil.byte2StringHex(packet.getData()));
                        packet.setData("DDDD0000".getBytes());
                        socket.send(packet);
//                        System.out.println("client连入成功:" + socket.getLocalPort());
//                        new Thread(new ServerRunnable(socket)).start();
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }).start();
    }
    
    private class ServerRunnable implements Runnable {
        
        private Socket       socket;
        
        private OutputStream writer;
        
        public ServerRunnable(Socket socket) throws IOException {
            
            super();
            this.socket = socket;
            this.writer = socket.getOutputStream();
        }
        
        @Override
        public void run() {
            
            new Thread(new ServerReadThread(socket, queue)).start();
            new Thread(new ServerWriteThread(socket, queue)).start();
        }
        
    }
    
    public static void main(String[] args) {
        
        new MyServerSocketZhilian().start();
    }
}
