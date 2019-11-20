package chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class Chat {
	public static CopyOnWriteArrayList<Channel> list=new CopyOnWriteArrayList<>();
	public static void main(String[] args) throws Exception {
		System.out.println("---------server-------------");
		ServerSocket server=new ServerSocket(8888);
		boolean flag=true;
		int num=0;
		while(flag) {
			Socket client=server.accept();
			num++;
			Channel c=new Channel(client);
			list.add(c);
			System.out.println(num+"个客户连接");
			new Thread(c).start();
		}
		
	}
	static class Channel implements Runnable{
		private Socket client;
		private DataInputStream dis;
		private DataOutputStream dos;
		private boolean flag=true;
		private String name="";
		public Channel(Socket client) {
			this.client = client;
			try {
				dis=new DataInputStream(client.getInputStream());
				dos=new DataOutputStream(client.getOutputStream());
				name=this.receive();
				send("欢迎来到聊天室");
				sendOthers(name+"来到了聊天室",true);
			} catch (IOException e) {
				release();
				
			}
		}
		private String receive() {
			String msg="";
			try {
				msg=dis.readUTF();
			} catch (IOException e) {
				System.out.println("----receive-----");
				release();
			}
			return msg;
		}
		private void send(String msg) {
			try {
				dos.writeUTF(msg);
			} catch (IOException e) {
				System.out.println("--------send----------");
			}
		}
		private void sendOthers(String msg,boolean isSys) {
			boolean isPrivate=msg.startsWith("@");
			if(isPrivate) {
				msg=msg.substring(1);
				String[] str=msg.split(":");
				for(Channel other:list) {
					if(other.name.equals(str[0])) {
						other.send(this.name+":"+str[1]);
					}
				}
			}else{
				for(Channel other:list) {
				if(other==this) {
					continue;
				}
				if(!isSys){
					other.send(this.name+":"+msg);
				}else {
					other.send("系统提示："+msg);
				}
				}
			}
		}
		private void release() {
			Utils.close(dis,dos,client);
		}
		
		public void run() {
			while(flag) {
			String msg=receive();
			sendOthers(msg,false);
			}
			release();
		}
		
	}
}
