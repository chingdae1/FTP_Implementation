package client;
import java.io.*;
import java.net.*;
public class FTPClient {
	
	public static void main(String[] args) throws Exception{
		//command 에 대한 소켓 
		Socket commandClientSocket;
		//data 에 대한 소켓 
		Socket dataClientSocket;
		
		if(args.length < 2){
			commandClientSocket = new Socket("127.0.0.1", 2121);
			dataClientSocket    = new Socket("127.0.0.1", 2020);
		}
		else {
			commandClientSocket = new Socket("127.0.0.1", Integer.parseInt(args[0]));
			dataClientSocket    = new Socket("127.0.0.1", Integer.parseInt(args[1]));
		}
		//사용자의 입력에 대한 스트림 
		BufferedReader inFromUser = new BufferedReader(
				                    new InputStreamReader(System.in));
		//서버에 대한 command 출력 스트림 
		DataOutputStream commandOutToServer = new DataOutputStream(
				                              commandClientSocket.getOutputStream());
		//서버에 대한 data 출력 스트림 
		DataOutputStream dataOutToServer = new DataOutputStream(
                						   dataClientSocket.getOutputStream());
		//서버에 대한 command 입력 스트림 
		BufferedReader commandInFromServer = new BufferedReader(
											 new InputStreamReader(
											 commandClientSocket.getInputStream()));
		//서버에 대한 data 입력 스트림 
		DataInputStream dataInFromServer = new DataInputStream(
                						   dataClientSocket.getInputStream());
		
		printInfo();
		
		while(true) {
			String command; 
			String response = "";
			
			command = inFromUser.readLine();
			
			if(command.equals("q")) {
				break;
			}
			//GET
			else if(command.startsWith("GET")) {
				//파일 이름 파싱 
				String[] path = command.split("GET");
				path[1] = path[1].trim();
				String[] parsedPath = path[1].split("/");
				String fileName = parsedPath[parsedPath.length - 1];
				File f = new File(fileName);
				//파일 존재 여부 확인 
				if(f.isFile()) {
					System.out.println("FAILED - File already exists. (Client)");
					System.out.println("");
					continue;
				}
				commandOutToServer.writeBytes(command + '\n');
				receiveData(fileName, commandInFromServer, dataInFromServer);
				System.out.println("");
			}
			//PUT
			else if(command.startsWith("PUT")) {
				//파일 이 파싱 
				String[] path = command.split("PUT");
				path[1]       = path[1].trim();
				File f 		  = new File(path[1]);
				//파일 존재 여부 확인 
				if(!f.isFile()) {
					System.out.println("FAILED - File does not exist. (Client)");
					System.out.println("");
					continue;
				}
				commandOutToServer.writeBytes(command + '\n');
				sendData(path[1], commandOutToServer, dataOutToServer, commandInFromServer);
				System.out.println("");
			}
			//CD & LIST
			else{
				commandOutToServer.writeBytes(command + '\n');
				
				String line = "";
				while(true) {
					line = commandInFromServer.readLine();
					if(line.startsWith("[EndOfData]")) {
						break;
					}
					response += (line + "\n");
				}
				System.out.println(response);
			}
		}
		commandClientSocket.close();
		dataClientSocket.close();
	}
	
	public static void receiveData(String fileName, BufferedReader commandInFromServer, DataInputStream dataInFromServer) throws IOException {
		File f = new File(fileName);
		String getFileSize;
        int       fileSize; 
        //server로부터 파일 size 수신 
        getFileSize = commandInFromServer.readLine();
        //server에 해당 파일이 존재하지 않는 경우 
        if(getFileSize.equals("NOT EXISTS")) {
        	System.out.println("FAILED - File does not exist. (Server)");
        	return;
        }
        //server에 해당 파일이 존재하는 경우 
        else {
        	fileSize = Integer.parseInt(getFileSize);
        }
        System.out.println("Receiving data. Please wait..");
        //server로부터 파일 수신해서 buff에 저장 
        byte [] buff  = new byte [fileSize];
        FileOutputStream fos = new FileOutputStream(f);
        dataInFromServer.read(buff);
        //새로 만든 파일에 buff를 write
        fos.write(buff);
        fos.close();
        System.out.println("Received " + fileName + " / " + f.length() + "byte(s)");
	}
	
	public static void sendData(String fileName, DataOutputStream commandOutToServer,
								DataOutputStream dataOutToServer, BufferedReader commandInFromServer) throws IOException {
		//서버의 현재 working directory에 보내고자 하는 파일이 이미 존재하는 경우
		if(commandInFromServer.readLine().equals("ALREADY EXISTS")) {
			System.out.println("FAILED - File already exists. (Server)");
			return;
		}
		File f 					= new File(fileName);
		FileInputStream fis 	= new FileInputStream(f);
        BufferedInputStream bis = new BufferedInputStream(fis);
        //server에게 해당 파일의 size를 전송 
        commandOutToServer.writeBytes(f.length() + "\n");
        //파일을 읽어 buff에 저장 
        byte [] buff  = new byte [(int)f.length()];
        bis.read(buff, 0, buff.length);
        bis.close();
        //server에게 파일 전송
        dataOutToServer.write(buff, 0, buff.length);
        dataOutToServer.flush();
        System.out.println("Sent " + fileName + " / " + f.length() + "byte(s)");
	}
	
	public static void printInfo() {
		System.out.println("*********************************************************************************************************************************");
		System.out.println("");
		System.out.println("CD [path]   => Change current working directory to [path]. Both relative and absolute path & '.', '..' are available.");
		System.out.println("LIST [path] => List all files and directories in [path]. Both relative and absolute path & '.', '..' are available.");
		System.out.println("GET [path]  => Download file from [path]. Both relative and absolute path are available.");
		System.out.println("PUT [file]  => Upload file located in client's current working directory to server's current working directory.");
		System.out.println("q           => Exit.");
		System.out.println("");
		System.out.println("*********************************************************************************************************************************");
	}
}

// 미흡한 부분 
// 서버의 웰커밍 소켓을 적절하게 close하는 방법을 모르겠다. 
// 과제 명세서에서 말하는 쓰레드 구조를 제대로 이해하고 구현했는지 확신이 서지 않는다.
// class를 바람직하게 디자인한 것인지 잘 모르겠다. 프로그램 디자인의 미숙함. 
// 클라이언트 소켓이 종료되었을 때 서버에서 감지되는 exception을 catch해서 
