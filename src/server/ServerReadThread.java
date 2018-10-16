
package server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;


import com.alibaba.fastjson.JSONObject;


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
    
    /**
     * 并下发命令
     * 
     * @param lineString
     * @throws InterruptedException
     */
    private void excuteReceiveDatas(String lineString) throws InterruptedException {
        System.out.println("当期时间："+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"---"+lineString);
    }
    
    @Override
    public void run() {
        
        while (true) {
            try {
            	// 解析数据包（按字节读取，根据长度解析，避免粘包）
                byte[] head = new byte[] { 0, 0, 0, 0 };
                reader.read(head, 0, 2);
                byte[] len = new byte[] { 0, 0, 0, 0 };
                reader.read(len, 0, len.length);
                int length = Int2ByteUtil.byteArrayToInt(len);
                int surplusLen = length-2-4;//-head-length
                //剩余字节长度
                byte[] surplusByte = new byte[surplusLen];
                reader.read(surplusByte, 0, surplusLen);
                /**
                 * tag 0-3
                 * cmd 4,5
                 * ut 6
                 * s1 7
                 * s2 8
                 * s3 9
                 */
                byte[] tag = new byte[] { 0, 0, 0, 0 };
                tag[0]=surplusByte[0];
                tag[1]=surplusByte[1];
                tag[2]=surplusByte[2];
                tag[3]=surplusByte[3];
                /**
                 * 指令 服务端心跳：0x03 客户端心跳回复：0x02 客户端发送消息：0x06
                 */
                byte[] cmd = new byte[] { 0, 0, 0, 0 };
                cmd[0]=surplusByte[4];
                cmd[1]=surplusByte[5];
                int command = Int2ByteUtil.byteArrayToInt(cmd);
                System.out.println("command:"+command);
                
                byte[] ut = new byte[] { 0, 0, 0, 0 };
                ut[0] = surplusByte[6];
                // 预留字段
                byte[] s1 = new byte[] { 0, 0, 0, 0 };
                byte[] s2 = new byte[] { 0, 0, 0, 0 };
                byte[] s3 = new byte[] { 0, 0, 0, 0 };
                
                if (command == 0x66) {
                    int datalen = length - 0x10;
                    byte[] data = null;
                    if (datalen > 0) {
                        data = new byte[datalen];
                        for(int datai=0;datai<datalen;datai++) {
                            data[datai]=surplusByte[10+datai];
                        }
                    }
                    // 客户端发送消息包处理（处理上报数据）
//                    String lineString = new String(data);
//                    excuteReceiveDatas(lineString);
//                    System.out.println("当期时间："+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"---"+Int2ByteUtil.byte2StringHex(data));
                }
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
