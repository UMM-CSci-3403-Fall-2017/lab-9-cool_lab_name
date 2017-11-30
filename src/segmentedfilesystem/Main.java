package segmentedfilesystem;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Main {
    
    private static boolean f1Done;
    private static boolean f2Done;
    private static boolean f3Done;

	public static void main(String[] args) throws UnknownHostException {
        int port = 6014;
        InetAddress address = InetAddress.getLocalHost();
		
        client(port, address);
    }
    
	public static void client(int port, InetAddress address) {
    	try {  	
    		DatagramSocket socket = new DatagramSocket(port);

    		Boolean f1Done = false;
    		Boolean f2Done = false;
    		Boolean f3Done = false;     
            
    		//byte[] inByteData = new byte[1004];
    		byte[] outByteData = new byte[1004];
            
    		outByteData = "".getBytes();
    		
    		DatagramPacket outPacket = new DatagramPacket(outByteData, outByteData.length, 
          		  address, port);
    		
    		socket.send(outPacket);
    		
    		ArrayList<Integer> IDArr = new ArrayList<Integer>();
    		
    		ArrayList<String> filenameArr = new ArrayList<String>();
    		
    		ArrayList<byte[]> f1Bytes = new ArrayList<byte[]>();
    		ArrayList<byte[]> f2Bytes = new ArrayList<byte[]>();
    		ArrayList<byte[]> f3Bytes = new ArrayList<byte[]>();

            while(!f1Done && !f2Done && !f3Done) {
            	
            	byte[] inByteData = new byte[1004];
            	
            	//set inbyte Data
            	DatagramPacket inPacket = new DatagramPacket(inByteData, inByteData.length);
              
            	socket.receive(inPacket);
            	inByteData = inPacket.getData();
            	
            	//------------------------------------
            	//test string receiving functionality
            	String testText = new String(inByteData, 0, inPacket.getLength());
              
            	System.out.println("packet text: " + testText);
            	
            	//add file id if it is new
                if (IDArr.contains((int) inByteData[1])!=true) {
            		IDArr.add((int) inByteData[1]);
            	}
                
            	//checkFileCompletion(inPacket, IDArr);
            	
                
                
            	//check if header
            	//if (the last bit of inByteData[0] is 0) {
            	
            	//filename is from the third byte until the end of the packet
            	//add filename to filenameArr
                
            	//} else {
            	//it is then a data file
            	
            	//packet number is bytes 3-4, data is every byte after 4 
            	
                //check if it is the last data packet
                //if (the 2nd to last bit of inByteData[0] is 1) {
            	
            	//set last packet field to true
            	
            	//} 
                
                //store data in respective file Arraylist
                
            	//}
     
            }    
            
            //all three ArrayLists should contain respective files data
                
            //sort f1Bytes, f2Btyes, f3Btyes by packet number
           
            //write files out to their filenames
            
            
            socket.close();	
    		
    	} catch (IOException e) {
    		System.out.println(e);
    	}
	}
    	
    	public static void checkF1(DatagramPacket lastPacket, ArrayList<Integer> IDArr) {
    		//check if all files gathered for f1 gathered
    		
    		//if file is complete set appropriate bool to true
    		f1Done = true;
    		
    		//check if all files gathered for f2 gathered
    		
    		//if file is complete set appropriate bool to true
    		f2Done = true;
    		
    		//check if all files gathered for f3 gathered
    		
    		//if file is complete set appropriate bool to true
    		f3Done = true;     
    		
    	}
    	
    	

}
