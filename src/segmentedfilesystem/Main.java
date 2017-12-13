package segmentedfilesystem;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class Main {

	private static boolean f1Done;
	private static boolean f2Done;
	private static boolean f3Done;
	
	private static int fileIndex;

	public static void main(String[] args) throws UnknownHostException {
		//call client back-end on desired host address and port
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
		
		//constructor with initialized sub-fields
		public packObj(){
			fileID = 0;
			lastData = false;
			data = null;
			filename = "";
			packNum = 0;
		}			
		
		//custom comparator to sort by packet number
		static Comparator<packObj> packetCompare = new Comparator<packObj>() {
			@Override
		    public int compare(packObj left, packObj right) {
		        return left.packNum > right.packNum ? 1 : (left.packNum < right.packNum) ? -1 : 0;
		    }
		};
	}
	
	public static void client(int port, InetAddress address) {
		try {  
			
			//establish socket
			DatagramSocket socket = new DatagramSocket(port);
			
			//initialize var values
			f1Done = false;
			f2Done = false;
			f3Done = false;
			
			fileIndex = 0;
			
			//Make a new empty byte array the size of the largest packet that can be sent
			byte[] outByteData = new byte[1004];
			outByteData = "".getBytes();
			
			//Send an empty packet to the server to tell the server to start sending packets back
			DatagramPacket outPacket = new DatagramPacket(outByteData, outByteData.length, 
					address, port);

			socket.send(outPacket);

			//Create ArrayLists to store all of the packets being received
			ArrayList<packObj> lastPackets = new ArrayList<packObj>();
			int[] fileLengths = new int[3];

			ArrayList<String> filenameArr = new ArrayList<String>();

			ArrayList<packObj> f1Packets = new ArrayList<packObj>();
			ArrayList<packObj> f2Packets = new ArrayList<packObj>();
			ArrayList<packObj> f3Packets = new ArrayList<packObj>();

			//Loops until all of the packets have been received from the server
			
			//placeholder loop conditions for testing
			for (int i = 0; i < 70; i++) {
				
				//run until file completion confirmed
				//while(!f1Done && !f2Done && !f3Done) {
				
				//create buffer to store incoming data
				byte[] inByteData = new byte[1028];
				
				//receive and store data from incoming packet
				DatagramPacket inPacket = new DatagramPacket(inByteData, inByteData.length);	
				socket.receive(inPacket);
				inByteData = inPacket.getData();
				
				//create packet object to store sub-fields
				packObj packet = new packObj();
				
				//------------------------------------
				//test string receiving functionality
				//int fileID = ((int) inByteData[1]);
				//String testText = new String(inByteData, 0, inPacket.getLength());

				//System.out.println("byte: " + Integer.toBinaryString(inByteData[0]));
				//System.out.println("byte: " + inByteData[0]);
				//System.out.println("packet text: " + testText);
				//-------------------------------------
				
				//add file id if it is new
				packet.fileID = ((int) inByteData[1]);
				
				//checkFileCompletion(f1Packets, f2Packets, f3Packets, lastPackets);

				//check if header (the last bit of the status byte)
				if(inByteData[0]==0){
					byte[] fn = Arrays.copyOfRange(inByteData, 2, inPacket.getLength());
					packet.filename = new String(fn, "UTF-8");
					filenameArr.add(packet.filename);			
				} else {
					//it is a data file
					//packet number is bytes 3-4, data is every byte after 4 
					byte[] pn = new byte[2];
					pn[0] = inByteData[2];
					pn[1] = inByteData[3];
					
					//turn byte array into integer
					packet.packNum = new BigInteger(pn).intValue();		

					//check if it is the last data packet
					if(inByteData[0]%4==3){
						packet.lastData = true;
						fileLengths[fileIndex] = packet.packNum;
						fileIndex++;
						//store it so we can access its sub-fields
						lastPackets.add(packet);
					} 
					//store packet data
					byte[] data = Arrays.copyOfRange(inByteData, 4, inPacket.getLength());
					packet.data = data;
					
					//check file id and add to respective array list
					storeData(packet, packet.fileID, f1Packets, f2Packets, f3Packets);
				}
			}

			//test code/print statements, will be removed
			
			//System.out.println("file 1 packets should have the id: " + lastPackets.get(0).fileID + 
			//		"   and the filename: " + filenameArr.get(0));
			
			//System.out.println("file 2 packets should have the id: " + lastPackets.get(1).fileID + 
			//		"   and the filename: " + filenameArr.get(1));
			
			//System.out.println(lastPackets.size() + "   "  + filenameArr.size());
			
			//if (lastPackets.size()==3 && filenameArr.size()==3){
			//	System.out.println("file 3 packets should have the id: " + lastPackets.get(2).fileID + 
			//		"   and the filename: " + filenameArr.get(2));
			//}
			
			//all three ArrayLists should contain respective files data
			
			for (packObj p: f2Packets){
				System.out.println("packNum is " + p.packNum);
			}
			
			System.out.println("----------------------");
			
			//sort f1Bytes, f2Btyes, f3Bytes by packet number
			Collections.sort(f2Packets, packObj.packetCompare);
			
			for (packObj p: f2Packets){
				System.out.println("packNum is " + p.packNum + " " + (fileLengths[1]) + " " + (lastPackets.get(1).packNum));
			}
			
			
			//write files out to their filenames
			//try (FileOutputStream os = new FileOutputStream("relative path" + filenameArr[0])) {
			//	   os.write(f1Bytes);
			//	   os.close();
			//	}
			
			//System.out.println("f1 packet: ");
			//for(int i = 0; i < f1Packets.size(); i++){
				//System.out.println(f1Packets.get(i).fileID);
			//}
			
			//System.out.println("f2 packet: ");
			//for(int i = 0; i < f2Packets.size(); i++){
				//System.out.println(f2Packets.get(i).fileID);
			//}
			
			//System.out.println("f3 packet: ");
			//for(int i = 0; i < f3Packets.size(); i++){
				//System.out.println(f3Packets.get(i).fileID);
			//}
			socket.close();	
			System.out.println("DONE");

		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public static void checkFileCompletion(ArrayList<packObj> f1, ArrayList<packObj> f2, ArrayList<packObj> f3, ArrayList<packObj> lastPackets) {
		//check and see if all last packets have been received
		if(lastPackets.size()==3){
				//check if all files gathered for f1 gathered
				if(lastPackets.get(0).packNum == f1.size()){
					f1Done = true;
				} 
				//check if all files gathered for f2 gathered
				if (lastPackets.get(1).packNum == f2.size()){
					f2Done = true;
				}
				//check if all files gathered for f3 gathered
				if (lastPackets.get(2).packNum == f3.size()){
					f3Done = true;
				}
		}
	}
	
	public static void storeData(packObj packet, int fileID, ArrayList<packObj> f1, ArrayList<packObj> f2, ArrayList<packObj> f3 ) {
		//start adding to first file if it is empty
		if(f1.size() == 0){
			f1.add(packet);
		}
		//add packet to file 1 if it has the same file ID
		else if(f1.get(0).fileID == packet.fileID){
			f1.add(packet);
		}
		//start adding to second file if it is empty
		else if(f2.size() == 0){
			f2.add(packet);
		}
		//add packet to file 2 if it has the same file ID
		else if(f2.get(0).fileID == packet.fileID){
			f2.add(packet);
		}
		//start adding to third file if it is empty
		else if(f3.size() == 0){
			f3.add(packet);
		}		
		//add packet to file 3 if it has the same file ID
		else if(f3.get(0).fileID == packet.fileID){
			f3.add(packet);
		}
	}
}


