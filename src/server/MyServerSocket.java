
package server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;


import com.alibaba.fastjson.JSONObject;


public class MyServerSocket {
    
    private int                                      port                 = 17921;
    
    private LinkedBlockingQueue<byte[]>              queue                = new LinkedBlockingQueue<byte[]>();
    
    private ServerSocket                             serverSocket         = null;
    
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
                    serverSocket = new ServerSocket(port);
                    System.out.println("服务端服务启动监听：" + port);
                    while (true) {
                        Socket socket = serverSocket.accept();
                        System.out.println("client连入成功:" + socket.getLocalPort());
                        new Thread(new ServerRunnable(socket)).start();
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
            
//            new Thread(new ServerReadThread(socket, queue)).start();
            new Thread(new ServerWriteThread(socket, queue)).start();
            testSend();
        }
        
    }
    
    public void testSend() {
    	new Thread(new Runnable() {
			@Override
			public void run() {
			    int i = 0;
				while(i<5) {
					try {
						//如果socket被关闭就不发送
						JSONObject test = new JSONObject();
						test.put("cpsn", 359369081974939L);
						test.put("datahex", "DDDD00000F133BF3B84B0000000001");
						queue.put(ClientSocketUtil.sendDataMessage(test.toString().getBytes()));
					} catch (Exception e) {
						System.out.println("server发送包："+e.getMessage());
						break;
					}
					i++;
				}
			}
		}).start();
    }
    
    public static void main(String[] args) {
        
        new MyServerSocket().start();
    }
}
