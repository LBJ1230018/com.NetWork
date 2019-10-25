package bank;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import dao.BankDAO;

public class Server {
	public static void main(String[] args) throws IOException {
		new Server().start();
		
	}
	private BankDAO dao=new BankDAO();
	private DataOutputStream dos;
	public void start() throws IOException{
		//创建套接字服务器
		 //启动服务器 
		 ServerSocket server=new ServerSocket(8888);
		 System.out.println("服务器启动完成，监听端口：8888");
		 
		 boolean running =true;
		 while(running){
			 //当前线程进入阻塞状态
			 Socket client=server.accept();
			 //
			 new Thread(){
				 @SuppressWarnings("deprecation")
				 public void run(){
					 //获取网络地址对象
					 InetAddress addr=client.getInetAddress();
					 System.out.println("客户端的主机地址："+addr.getHostAddress());
					 System.out.println("客户端的IP地址："+Arrays.toString(addr.getAddress()));
					 try{
						 InputStream in=client.getInputStream();
						 OutputStream out=client.getOutputStream();
						 dos=new DataOutputStream(out);
						 boolean running =true;
						 while(running){
							 /**
							  * 
							  */
							 DataInputStream dis=new DataInputStream(in);
							 try{
								 String command=dis.readUTF();
								 switch(command){
								 case "register":
								 case "diposit" :
									 diposit(dis.readUTF(),dis.readFloat());
									 break;
								 case "withdraw":
									 withdraw(dis.readUTF(),dis.readFloat());
								 case "transfer":
								 }
							 }catch(EOFException e){
								 break;
							 }
						 }
						 
					 }catch(IOException e){
						 e.printStackTrace();
					 }
				 }
			 }.start();
		 }
	}
	//开户
	public void register(String cardno,String password){
		
	}
	//存款
	public void diposit(String cardno,float money) throws IOException{
		dao.update(cardno, money);
		dos.writeUTF("存款成功！！");
		dos.flush();
	}
	//取款
    public void withdraw(String cardno,float money) throws IOException{
		dao.update1(cardno, money);
		dos.writeUTF("取款成功");
		dos.flush();
	}
   //转账
    public void transfer(String cardno1,String cardno2,float money){
	
    }
}
