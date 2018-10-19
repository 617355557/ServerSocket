
package server.nbtest_zhilian;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;


public class ServerWriteThread implements Runnable {
    
    private BufferedOutputStream        writer = null;
    
    private LinkedBlockingQueue<byte[]> queue  = null;
    
    private Socket                      socket = null;
    
    /**
     * 服务端发送数据类 下发命令
     * 
     * @param socket
     * @param queue
     */
    public ServerWriteThread(Socket socket, LinkedBlockingQueue<byte[]> queue) {
        
        super();
        this.queue = queue;
        this.socket = socket;
        try {
            this.writer = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    
    @Override
    public void run() {
        
        while (true) {
            byte[] element = null;
            try {
                // 防止下次写数据的时候write关闭，将queue元素take出来
                if (!socket.isClosed()) {
                    element = queue.take();
                    //System.out.println("即将发送数据element:"+element.hashCode());
                    writer.write(element);
                    writer.flush();
                }else {
                    throw new Exception("socket关闭");
                }
            } catch (Exception e) {
                //将queue.take对象放回去
                System.out.println("server write thread:"+e.getMessage());
                try {
                    //System.out.println("回收数据element:"+element.hashCode());
                    queue.put(element);
                    writer.close();
                } catch (IOException | InterruptedException e1) {
                    writer=null;
                    System.out.println("关闭writer异常："+e1.getMessage());
                }
                break;
            }
        }
    }
    
}
