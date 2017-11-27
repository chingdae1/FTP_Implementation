package server;
import java.net.*;
import server.Server;
public class FTPServer {

	public static void main(String[] args) throws Exception {
		//command 에 대한 welcoming 소켓 
		ServerSocket commandWelcomeSocket;
		//data 에 대한 welcoming 소켓 
		ServerSocket    dataWelcomeSocket;
		
		if(args.length < 2) {
			commandWelcomeSocket = new ServerSocket(2121);
			dataWelcomeSocket 	 = new ServerSocket(2020);
		}
		else {
			commandWelcomeSocket = new ServerSocket(Integer.parseInt(args[0]));
			dataWelcomeSocket 	 = new ServerSocket(Integer.parseInt(args[1]));
		}
	
		
		while(true) {
			Server server = new Server(commandWelcomeSocket.accept(), dataWelcomeSocket.accept());
			server.start();
		}
	}
}