import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.lang.*;


public class Client{
	private ServerConnection server;
	private LinkedBlockingQueue<Object> messages;
	private Socket socket;

	public Client(String IPAddress, int port) throws IOException{

		socket= new Socket(IPAddress,port);
		messages= new LinkedBlockingQueue<Object>();
		server= new ServerConnection(socket);

		Thread messageHandler= new Thread(){
			public void run(){
				while(true){
					try{
						Object message = messages.take();
						System.out.println(message);
					}catch(InterruptedException e){
						e.printStackTrace();
					}
				}
			}
		};
		messageHandler.setDaemon(true);
		messageHandler.start();
	}

	private class ServerConnection{
		ObjectInputStream in;
		ObjectOutputStream out;
		Socket socket;

		ServerConnection(Socket socket) throws IOException{
			this.socket=socket;
			in=new ObjectInputStream(socket.getInputStream());
			out=new ObjectOutputStream(socket.getOutputStream());

			Thread read=new Thread(){
				public void run(){
					while(true){
						try{
							Object obj=in.readObject();
							messages.put(obj);
						}catch(IOException e){
							e.printStackTrace();
						}catch(ClassNotFoundException e){
							e.printStackTrace();
						}catch(InterruptedException e){
							e.printStackTrace();
						}
					}
				}
			};

			read.setDaemon(true);
			read.start();
		}

		private void write(Object obj){
			try{
				out.writeObject(obj);
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	public void send(Object obj){
		server.write(obj);
	}
}