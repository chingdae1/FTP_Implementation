package server;
import java.io.*;
import java.net.*;
public class FTPServer {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String clientCommand;
		String responseToClient;
		
		ServerSocket welcomeSocket = new ServerSocket(9999);
		
		while(true) {
			Socket connectionSocket = welcomeSocket.accept();
			
			BufferedReader inFromClient = new BufferedReader(
										  new InputStreamReader(
										  connectionSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(
										   connectionSocket.getOutputStream());
			
			clientCommand = inFromClient.readLine();
			System.out.println(clientCommand);
			if(clientCommand.startsWith("CD")) {
				responseToClient = FTPServer.changeDirectory(clientCommand);
			}
			else if(clientCommand.startsWith("LIST")) {
				responseToClient = FTPServer.listFilesDirectories(clientCommand);
			}
			else {
				responseToClient = "Invalid command.\n"; 
			}
			System.out.println(responseToClient+"\n\n");
			outToClient.writeBytes(responseToClient + "[EndOfData]\n");
		}
	}
	
	public static String changeDirectory(String clientCommand) throws IOException {
		//절대경로를 통해 이동
		if(clientCommand.startsWith("CD /")) {
			//입력된 command 에서 path 파싱
			String[] destinationPath = new String[2];
			destinationPath = clientCommand.split("CD");
			destinationPath[1] = destinationPath[1].trim();
			//디렉토리 존재유무 확인 
			if(!isDirectoryExists(destinationPath[1])) { 
				return "Directory name is invalid.\n"; 
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
			destinationDirectory = clientCommand.split("CD");
			//최종 목적지 경로 
			String destinationPath = (new File(".").getCanonicalPath() 
											+ "/" + destinationDirectory[1].trim());
			//디렉토리 존재유무 확인 
			if(!isDirectoryExists(destinationPath)) { 
				return "Directory name is invalid.\n"; 
			}
			//working directory 변경
			System.setProperty("user.dir", destinationPath);
		}
		
		return new File(".").getCanonicalPath() + "/\n";
	}
	
	public static String listFilesDirectories(String clientCommand) throws IOException {
		//입력된 command 에서 path 파싱 
		String[] path = new String[2];
		path = clientCommand.split("LIST"); 
		path[1] = path[1].trim();
		//'.'이 입렫된 경우 현재 경로로 바꿈 
		if(path[1].equals(".")) {
			path[1] = new File(".").getCanonicalPath();
		}
		//'..'이 입렫된 경우 상 경로로 바꿈 
		else if(path[1].equals("..")) {
			path[1] = new File("..").getCanonicalPath();
		}
		//디렉토리 존재 유무 확인 
		if(!isDirectoryExists(path[1])) {
			return "Directory name is invalid.\n";
		}
		File directory = new File(path[1]);
		//File배열에 타겟 path에 들어있는 모든 파일을 담음 
		File[] fList = directory.listFiles();
		//디렉토리가 비어있는 경우
		if(fList.length == 0) {
			return "Empty Directory.\n";
		}
		StringBuilder listOfFiles = new StringBuilder("");
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
	    
	    return listOfFiles.toString();
	}
	
	private static boolean isDirectoryExists(String path) {
		File dir = new File(path);
		if(dir.isDirectory()){
			return true;
		}
		return false;
	}
}
