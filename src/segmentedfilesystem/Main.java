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
	
	public static void main(String[] args) throws UnknownHostException {
		//call client back-end on desired host address and port
		int port = 6014;
		InetAddress address = InetAddress.getByName("heartofgold.morris.umn.edu");
		client(port, address);
	}
	
	public static class packObj{
		int fileID;
		byte[] data;
		String filename;
		int packNum;
		
		//constructor with initialized sub-fields
		public packObj(){
			fileID = 0;
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
			
			//initialize file completion values
			f1Done = false;
			f2Done = false;
			f3Done = false;
			
			//Make a new empty byte array the size of the largest packet that can be sent
			byte[] outByteData = new byte[1004];
			outByteData = "".getBytes();
			
			//Send an empty packet to the server to tell the server to start sending packets back
			DatagramPacket outPacket = new DatagramPacket(outByteData, outByteData.length, address, port);
			socket.send(outPacket);

			//Create ArrayLists to store all of the packets being received
			ArrayList<packObj> lastPackets = new ArrayList<packObj>();;
			ArrayList<packObj> headerPackets = new ArrayList<packObj>();

			ArrayList<packObj> f1Packets = new ArrayList<packObj>();
			ArrayList<packObj> f2Packets = new ArrayList<packObj>();
			ArrayList<packObj> f3Packets = new ArrayList<packObj>();

			//loops until all of the packets have been received from the server
			while(!f1Done && !f2Done && !f3Done) {
				
				//create buffer to store incoming data
				byte[] inByteData = new byte[1028];
				
				//receive and store data from incoming packet
				DatagramPacket inPacket = new DatagramPacket(inByteData, inByteData.length);	
				socket.receive(inPacket);
				inByteData = inPacket.getData();
				
				//create packet object to store sub-fields
				packObj packet = new packObj();

				//add file id if it is new
				packet.fileID = ((int) inByteData[1]);
				
				//make sure you still need to check packets
				checkFileCompletion(f1Packets, f2Packets, f3Packets, lastPackets);

				//check if header (the last bit of the status byte)
				if(inByteData[0]==0){
					byte[] fn = Arrays.copyOfRange(inByteData, 2, inPacket.getLength());
					packet.filename = new String(fn, "UTF-8");
					headerPackets.add(packet);			
				} else {
					//it is a data file
					//packet number is bytes 3-4, data is every byte after 4 (copyOfRange was breaking for this data) 
					byte[] pn = new byte[2];
					pn[0] = inByteData[2];
					pn[1] = inByteData[3];
					
					//turn byte array into integer
					packet.packNum = new BigInteger(pn).intValue();		

					//check if it is the last data packet
					if(inByteData[0]%4==3){
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
			
			//sort f1Bytes, f2Btyes, f3Bytes by packet number
			Collections.sort(f1Packets, packObj.packetCompare);
			Collections.sort(f2Packets, packObj.packetCompare);
			Collections.sort(f3Packets, packObj.packetCompare);
			
			//write files out to their filenames, given data, headers, and file number
			writeFile(f1Packets, headerPackets, 1);
			writeFile(f2Packets, headerPackets, 2);
			writeFile(f3Packets, headerPackets, 3);
			
			//close socket
			socket.close();	

		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	public static void writeFile(ArrayList<packObj> filePackets, ArrayList<packObj> headerPackets, int fileNumber) {
		//open filestream for specified filename
		try (FileOutputStream os = new FileOutputStream("src/" + headerPackets.get(fileNumber-1).filename)) {
			   //make sure your header packet matches your file packets
			   if(filePackets.get(0).fileID == headerPackets.get(fileNumber-1).fileID) {
				   //write data to stream
				   for(int n = 0; n < filePackets.size(); n++) {
					   os.write(filePackets.get(n).data);
				   }
			   } else {
				   //check for improper parameter inputs
				   System.out.println("Wrong file number");
			   }
			   //close stream
			   os.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	
	}

	public static void checkFileCompletion(ArrayList<packObj> f1, ArrayList<packObj> f2, ArrayList<packObj> f3, ArrayList<packObj> lastPackets) {

		//check and see if all last packets have been received
		if(lastPackets.size()==3){
				
				//gather file sizes
				ArrayList<Integer> sizes = new ArrayList<Integer>();
				sizes.add(f1.size());
				sizes.add(f2.size());
				sizes.add(f3.size());
				
				//check if all files gathered for f1 gathered
				if(sizes.contains(lastPackets.get(0).packNum)){
					f1Done = true;
				} 
				//check if all files gathered for f2 gathered
				if(sizes.contains(lastPackets.get(1).packNum)){
					f2Done = true;
				}
				//check if all files gathered for f3 gathered
				if(sizes.contains(lastPackets.get(2).packNum)){
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


