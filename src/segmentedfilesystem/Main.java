package segmentedfilesystem;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Main {

	private static boolean f1Done;
	private static boolean f2Done;
	private static boolean f3Done;

	public static void main(String[] args) throws UnknownHostException {
		int port = 6014;
		InetAddress address = InetAddress.getByName("heartofgold.morris.umn.edu");

		client(port, address);
	}
	
	public static class packObj{
		int fileID;
		boolean lastData;
		byte[] data;
		String filename;
		int packNum;
		
		public packObj(){
			fileID = 0;
			lastData = false;
			data = null;
			filename = "";
			packNum = 0;
		}			
	}
	
	public static void client(int port, InetAddress address) {
		try {  	
			DatagramSocket socket = new DatagramSocket(port);

			f1Done = false;
			f2Done = false;
			f3Done = false;
			
			//Make a new byte array the size of the largest packet that can be sent
			byte[] outByteData = new byte[1004];

			outByteData = "".getBytes();
			
			//Send an empty packet to the server to tell the server to start sending packets back
			DatagramPacket outPacket = new DatagramPacket(outByteData, outByteData.length, 
					address, port);

			socket.send(outPacket);

			//Create ArrayLists to store all of the packets being received
			ArrayList<Integer> IDArr = new ArrayList<Integer>();

			ArrayList<String> filenameArr = new ArrayList<String>();

			ArrayList<packObj> f1Packets = new ArrayList<packObj>();
			ArrayList<packObj> f2Packets = new ArrayList<packObj>();
			ArrayList<packObj> f3Packets = new ArrayList<packObj>();

			//Loops until all of the packets have been received from the server
			for (int i = 0; i < 20; i++) {
				//while(!f1Done && !f2Done && !f3Done) {
				byte[] inByteData = new byte[1028];
				
				//Set inbyte Data
				DatagramPacket inPacket = new DatagramPacket(inByteData, inByteData.length);

				socket.receive(inPacket);

				inByteData = inPacket.getData();

				packObj packet = new packObj();
				//------------------------------------
				//test string receiving functionalityint fileID = ((int) inByteData[1]);
				//String testText = new String(inByteData, 0, inPacket.getLength());

				//System.out.println("byte: " + Integer.toBinaryString(inByteData[0]));
				//System.out.println("byte: " + inByteData[0]);
				//System.out.println("packet text: " + testText);
				//-------------------------------------
				
				//add file id if it is new
				packet.fileID = ((int) inByteData[1]);
				if (IDArr.contains(packet.fileID)!=true) {
					IDArr.add(packet.fileID);
				}
				
				checkFileCompletion(inPacket, packet);

				//check if header (the last bit of the status byte)
				if(inByteData[0]==0){					
					byte[] fn = Arrays.copyOfRange(inByteData, 2, inPacket.getLength());
					packet.filename = new String(fn, "US-ASCII");
					filenameArr.add(packet.filename);
				
				} else {
					//it is a data file
					//packet number is bytes 3-4, data is every byte after 4 
					byte[] pn = Arrays.copyOfRange(inByteData, 2, 3);
					//packet.packNum = (int) pn[0] + (int) pn[1];
					
					
					//check if it is the last data packet
					if(inByteData[0]==3){
						packet.lastData = true;
					} 
					//store packet data
					byte[] data = Arrays.copyOfRange(inByteData, 4, inPacket.getLength());
					packet.data = data;
					//check file id and add to respective array list
					storeData(packet, packet.fileID, f1Packets, f2Packets, f3Packets);
				}
			}    
			//all three ArrayLists should contain respective files data

			//sort f1Bytes, f2Btyes, f3Bytes by packet number
			//Collections.sort(f1Bytes, new Comparator<packet>() {
			//    @Override
			//    public int compare(packet left, packet right) {
			//        return left.packNum > right.packNum ? 1 : (left.packNum < right.packNum) ? -1 : 0;
			//    }
			//}
			
			//write files out to their filenames
			//try (FileOutputStream os = new FileOutputStream("relative path" + filenameArr[0])) {
			//	   os.write(f1Bytes);
			//	   os.close();
			//	}
			
			System.out.println("f1 packet: ");
			for(int i = 0; i < f1Packets.size(); i++){
				System.out.println(f1Packets.get(i).fileID);
			}
			
			System.out.println("f2 packet: ");
			for(int i = 0; i < f2Packets.size(); i++){
				System.out.println(f2Packets.get(i).fileID);
			}
			
			System.out.println("f3 packet: ");
			for(int i = 0; i < f3Packets.size(); i++){
				System.out.println(f3Packets.get(i).fileID);
			}
			socket.close();	
			System.out.println("DONE");

		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public static void checkFileCompletion(DatagramPacket lastPacket, packObj packet) {
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
	
	public static void storeData(packObj packet, int fileID, ArrayList<packObj> f1, ArrayList<packObj> f2, ArrayList<packObj> f3 ) {
		if(f1.size() == 0){
			f1.add(packet);
		}
		else if(f1.get(0).fileID == packet.fileID){
			f1.add(packet);
		}
		else if(f2.size() == 0){
			f2.add(packet);
		}
		else if(f2.get(0).fileID == packet.fileID){
			f2.add(packet);
		}
		else if(f3.size() == 0){
			f3.add(packet);
		}		
		else if(f3.get(0).fileID == packet.fileID){
			f3.add(packet);
		}
	}
}


