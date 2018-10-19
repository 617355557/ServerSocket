
package server.nbtest_zhilian;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import server.Int2ByteUtil;


public class ServerReadThread implements Runnable {
    
    private BufferedInputStream         reader = null;
    
    private LinkedBlockingQueue<byte[]> queue  = null;
    
    private Socket                      socket = null;
    
    /**
     * 服务端读取数据类 接收上报数据
     * 
     * @param socket
     * @param queue
     */
    public ServerReadThread(Socket socket, LinkedBlockingQueue<byte[]> queue) {
        
        super();
        this.queue = queue;
        this.socket = socket;
        try {
            this.reader = new BufferedInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("server read thread constructor:" + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        
        while (true) {
            try {
            	// 解析数据包（按字节读取，根据长度解析，避免粘包）
                byte[] head = new byte[4];
                reader.read(head, 0, 4);
                queue.put(head);
                System.out.println(Int2ByteUtil.byte2StringHex(head));
            } catch (Exception e) {
                System.out.println("server read thread:" + e.getMessage());
                try {
                    reader.close();reader = null;
                    socket.close();socket = null;
                } catch (IOException e1) {
                    System.out.println(e1.getMessage());
                }
                break;
            }
        }
    }
}
