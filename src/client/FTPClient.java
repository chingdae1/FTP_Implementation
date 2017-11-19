package client;
import java.io.*;
import java.net.*;
public class FTPClient {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		Socket clientSocket = new Socket("127.0.0.1", 9999);
		
		BufferedReader inFromUser = new BufferedReader(
				                    new InputStreamReader(System.in));
		
		DataOutputStream outToServer = new DataOutputStream(
				                           clientSocket.getOutputStream());
		
		BufferedReader inFromServer = new BufferedReader(
				                      new InputStreamReader(
				                          clientSocket.getInputStream()));
		
		while(true) {
			String command; 
			String response = "";
			
			command = inFromUser.readLine();
			if(command.equals("q")) {
				break;
			}
			outToServer.writeBytes(command + '\n');
			
			String line = "";
			System.out.println("command ==>" + command);
			System.out.println("response ==>" + response);
			while(true) {
				line = inFromServer.readLine();
				System.out.println("line ==>" + line);
				if(line.startsWith("[EndOfData]")) {
					break;
				}
				response += (line + "\n");
			}
			System.out.println(response);
		}
		clientSocket.close();
	}

}

// to do list
// 서버쓰레드로 구현.. 
// 클라이언트에서 while 로 계속 command 보내기가 안 