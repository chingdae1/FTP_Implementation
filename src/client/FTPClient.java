package client;
import java.io.*;
import java.net.*;
public class FTPClient {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String command; 
		String response = "";
		
		Socket clientSocket = new Socket("127.0.0.1", 9999);
		
		BufferedReader inFromUser = new BufferedReader(
				                    new InputStreamReader(System.in));
		
		DataOutputStream outToServer = new DataOutputStream(
				                           clientSocket.getOutputStream());
		
		BufferedReader inFromServer = new BufferedReader(
				                      new InputStreamReader(
				                          clientSocket.getInputStream()));
		
		command = inFromUser.readLine();
		
		outToServer.writeBytes(command + '\n');
		
		String line = "";
		//출력이 제대로 안된다.. 
		while(true) {
			line = inFromServer.readLine();
			if(line.startsWith("[EndOfData]")) {
				break;
			}
			response += (line+"\n");
		}

		System.out.println(response);
		
		clientSocket.close();
	}

}
