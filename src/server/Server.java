package server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Server extends Thread {
	//command 에 대한 connection 소켓 
	private Socket commandConnectionSocket;
	//data 에 대한 connection 소켓 
	private Socket dataConnectionSocket;
	//클라이언에 대한 command 입력 스트림
	private BufferedReader commandInFromClient;
	//클라이언에 대한 data 입력 스트림
	private DataInputStream dataInFromClient;
	//클라이언에 대한 command 출력 스트림
	private DataOutputStream commandOutToClient;
	//클라이언에 대한 data 출력 스트림
	private DataOutputStream dataOutToClient;
	
	public Server(Socket commandConnectionSocket,
				  Socket 	dataConnectionSocket) throws IOException {
		this.commandConnectionSocket = commandConnectionSocket;
		this.dataConnectionSocket 	 = 	  dataConnectionSocket;
		
		this.commandInFromClient = new BufferedReader(
	  					    	   new InputStreamReader(
	  					    	   this.commandConnectionSocket.getInputStream()));
		this.dataInFromClient    = new DataInputStream(
								   this.dataConnectionSocket.getInputStream());
		this.commandOutToClient  = new DataOutputStream(
	   					    	   this.commandConnectionSocket.getOutputStream());
		this.dataOutToClient 	 = new DataOutputStream(
								   this.dataConnectionSocket.getOutputStream());
	}
	
	public void run() {
		while(true){
			String 	  clientCommand;
			String responseToClient;
			try{
				clientCommand = commandInFromClient.readLine();
				//GET
				if(clientCommand.startsWith("GET")) {
					String[] path = clientCommand.split("GET");
					path[1] 	  = path[1].trim();
					sendData(path[1]);
				}
				//PUT
				else if(clientCommand.startsWith("PUT")) {
					String[] fileName = clientCommand.split("PUT");
					fileName[1] 	  = fileName[1].trim();
					receiveData(fileName[1]);
				}
				else {
					//CD
					if(clientCommand.startsWith("CD")) {
						responseToClient = changeDirectory(clientCommand);
					}
					//LIST
					else if(clientCommand.startsWith("LIST")) {
						responseToClient = listFilesDirectories(clientCommand);
					}
					else {
						responseToClient = "Invalid command.\n"; 
					}
					commandOutToClient.writeBytes(responseToClient + "[EndOfData]\n");
				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}
	public String changeDirectory(String clientCommand) throws IOException {
		System.out.println("Change working directory.");
		//절대경로를 통해 이동
		if(clientCommand.startsWith("CD /")) {
			//입력된 command 에서 path 파싱
			String[] destinationPath = new String[2];
			destinationPath    = clientCommand.split("CD");
			destinationPath[1] = destinationPath[1].trim();
			//디렉토리 존재유무 확인 
			if(!isDirectoryExists(destinationPath[1])) { 
				return "FAILED - Directory name is invalid.\n"; 
			}
			//working directory 변경 
			System.setProperty("user.dir", destinationPath[1]);
		} 
		//상위 디렉토리로 이동
		else if(clientCommand.startsWith("CD ..")) { 
			System.setProperty("user.dir", new File("..").getCanonicalPath());
		} 
		//현재 디렉토리로 이동
		else if(clientCommand.startsWith("CD .")) { 
			//Do Nothing.
		}
		//상대경로를 통해 이동
		else { 
			//먼저 디렉토리 이름 파싱 
			String[] destinationDirectory = new String[2];
			destinationDirectory 		  = clientCommand.split("CD");
			//최종 목적지 경로 
			String destinationPath 		  = (new File(".").getCanonicalPath() 
											+ "/" + destinationDirectory[1].trim());
			//디렉토리 존재유무 확인 
			if(!isDirectoryExists(destinationPath)) { 
				return "FAILED - Directory name is invalid.\n"; 
			}
			//working directory 변경
			System.setProperty("user.dir", destinationPath);
		}
		System.out.println("Changing working directory completed.");
		return "OK - "+ new File(".").getCanonicalPath() + "/\n";
	}
	
	public String listFilesDirectories(String clientCommand) throws IOException {
		//입력된 command 에서 path 파싱 
		String[] path = new String[2];
		path    = clientCommand.split("LIST"); 
		path[1] = path[1].trim();
		//'.'이 입력된 경우 현재 경로로 바꿈 
		if(path[1].equals(".")) {
			path[1] = new File(".").getCanonicalPath();
		}
		//'..'이 입력된 경우 상위 경로로 바꿈 
		else if(path[1].equals("..")) {
			path[1] = new File("..").getCanonicalPath();
		}
		//상대경로로 입력된 경우 
		else if(!path[1].startsWith("/")) {
			path[1] = new File(".").getCanonicalPath() + "/" + path[1];
		}
		System.out.println("List files and directories in " + path[1]);
		//디렉토리 존재 유무 확인 
		if(!isDirectoryExists(path[1])) {
			return "FAILED -Directory name is invalid.\n";
		}
		File directory = new File(path[1]);
		//File배열에 타겟 path에 들어있는 모든 파일을 담음 
		File[] fList   = directory.listFiles();
		//디렉토리가 비어있는 경우
		if(fList.length == 0) {
			return "Empty Directory.\n";
		}
		StringBuilder listOfFiles 		= new StringBuilder("");
		StringBuilder listOfDirectories = new StringBuilder("");
		//파일 리스트와 디렉토리 리스트를 각각 다른 StringBuilder 에 append
	    for (File file : fList) {
	        if (file.isFile()) {
	            listOfFiles.append(file.getName() + "," + file.length() + "\n");
	        } else if (file.isDirectory()) {
	            listOfDirectories.append(file.getName() + "," + "-\n");
	        }
	    }
	    //파일 리스트와 디렉토리 리스트 concatenate 
	    listOfFiles.append(listOfDirectories.toString());
	    System.out.println("Listing files and directories completed.");
	    return listOfFiles.toString();
	}
	
	private boolean isDirectoryExists(String path) {
		File dir = new File(path);
		if(dir.isDirectory()){
			return true;
		}
		return false;
	}
	
	public void sendData(String filePath) throws IOException {
		//입력된 경로가 subdir/a.jpg 와 같은 형태일 때
		if(!filePath.startsWith("/")) {
			//절대 경로로 수정 
			filePath = new File(".").getCanonicalPath() + "/" + filePath;
		}
        File f = new File(filePath);
        //파일 존재 유무 확인 
        if(!f.isFile()) {
        	commandOutToClient.writeBytes("NOT EXISTS\n");
        	System.out.println("File does not exist.");
        	return;
        }
        System.out.println("Send " + filePath +" to client.");
        FileInputStream 	fis = new FileInputStream(f);
        BufferedInputStream bis = new BufferedInputStream(fis);
        //client에게 해당 파일의 size를 전송 
        commandOutToClient.writeBytes(f.length() + "\n");
        //파일을 읽어 buff에 저장 
        byte [] buff  = new byte [(int)f.length()];
        bis.read(buff, 0, buff.length);
        bis.close();
        //client 에게 파일 전송 
        dataOutToClient.write(buff, 0, buff.length);
        dataOutToClient.flush();
        System.out.println("File sending completed.");
	}
	
	public void receiveData(String fileName) throws IOException {
		//현재 working directory에 파일 저장 
		String downloadPath = new File(".").getCanonicalPath() + "/" + fileName;
		File f 				= new File(downloadPath);
		System.out.println("Receiving : " + downloadPath);
		//파일 존재 여부 확인 
		if(f.isFile()) {
			System.out.println("File already exists.");
			commandOutToClient.writeBytes("ALREADY EXISTS\n");
			return;
		}
		else {
			System.out.println("OK");
			commandOutToClient.writeBytes("OK\n");
		}
		int fileSize;
		//client로부터 파일 size 수신
		fileSize = Integer.parseInt(commandInFromClient.readLine());
		//client로부터 파일 수신해서 buff에 저장
		byte [] buff 		 = new byte [fileSize];
        FileOutputStream fos = new FileOutputStream(f);
        dataInFromClient.read(buff);
        //새로 만든 파일에 buff를 write
        fos.write(buff);
        fos.close();
	}
}
