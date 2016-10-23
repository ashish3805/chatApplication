import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.lang.*;

public class Server
{
	protected LinkedBlockingQueue<Message> messages;
	protected ArrayList<ClientConnection> clients;
	protected ServerSocket serverSocket;
	protected int clientNo;

	Server(int port){
		messages = new LinkedBlockingQueue<Message>();
		clients = new ArrayList<ClientConnection>();
		clientNo=0;
		try{
			serverSocket= new ServerSocket(port);
		}catch(IOException e){
			e.printStackTrace();
		}

		Thread accept=new Thread(){
			public void run(){
				while(true){
					try{
						Socket clientConnection=serverSocket.accept();
						clients.add(new ClientConnection(clientConnection,"User"+(++clientNo),clients.size()));
						System.out.println("User"+clientNo+" connected");
					}catch(IOException e){
						e.printStackTrace();
					}
				}
			}
		};

		accept.start();

		Thread messageHandler = new Thread(){
			public void run(){
				while(true){
					try{
						Message message= messages.take();
						System.out.println("Message["+message.getUser()+"]:"+message.getText());
						sendToAllOthers(message);
					}catch(InterruptedException e){
						e.printStackTrace();
					}
				}
			}
		};

		messageHandler.setDaemon(true);
		messageHandler.start();
	}


	private class Message{
		protected String user;
		protected Object msg;
		protected int clientIndex;

		Message(String user,int clientIndex,Object msg){
			this.user=user;
			this.clientIndex=clientIndex;
			this.msg=msg;
		}

		Object getText(){
			return msg;
		};
		String getUser(){
			return user;
		}
		int getClientIndex(){
			return clientIndex;
		}
	}

	private class ClientConnection{
		ObjectInputStream in;
		ObjectOutputStream out;
		Socket socket;
		String name;
		int index;

		ClientConnection(Socket socket,String name,int index) throws IOException{
			this.socket = socket;
			this.name=name;
			this.index=index;

			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());

			Thread read = new Thread(){
				public void run(){
					while(true){
						try{
							Object obj= in.readObject();
							Message m=new Message(name,index,obj);
							messages.put(m);
						}catch(IOException e){
							try{
								out.close();
								in.close();
							}catch(IOException err){
								e.printStackTrace();
							}
							clients.remove(index);
							System.out.println(name+" disconnected");
							Thread.currentThread().interrupt();
							return;
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

		public void write(Object obj){
			try{
				out.writeObject(obj);
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}

	public void sendToOne(int index, Message message) throws IndexOutOfBoundsException{
		clients.get(index).write(message.getText());
	}
	public void sendToAll(Message message){
		for(ClientConnection client: clients)
			client.write(message.getText());
	}
	public void sendToAllOthers(Message message){
		for(int i=0;i<clients.size();i++){
			if(i==message.getClientIndex())
				continue;
			clients.get(i).write(message.getUser()+": "+message.getText());
		}
	} 
}
