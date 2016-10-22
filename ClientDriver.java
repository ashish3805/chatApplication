import java.util.*;

class ClientDriver{
	public static void main(String args[]){
		Client chatClient;
		try{
			chatClient= new Client("127.0.0.1",5001);
			System.out.println("start chatting:");
			Scanner sc=new Scanner(System.in);
			String str;
			do{
				str=sc.nextLine();
				chatClient.send(str);
			}while(true);
		}catch(Exception e){
			System.err.println("Client start error.");
		}
	}
};
