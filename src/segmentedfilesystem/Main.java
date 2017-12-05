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

	public static void client(int port, InetAddress address) {
		try {  	
			DatagramSocket socket = new DatagramSocket(port);

			f1Done = false;
			f2Done = false;
			f3Done = false;

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

			for (int i = 0; i < 20; i++) {
				//while(!f1Done && !f2Done && !f3Done) {
				byte[] inByteData = new byte[1028];
				String filename = "";
				boolean lastData = false;
				
				//set inbyte Data
				DatagramPacket inPacket = new DatagramPacket(inByteData, inByteData.length);

				socket.receive(inPacket);

				inByteData = inPacket.getData();

				//------------------------------------
				//test string receiving functionality
				//String testText = new String(inByteData, 0, inPacket.getLength());

				//System.out.println("byte: " + Integer.toBinaryString(inByteData[0]));
				//System.out.println("byte: " + inByteData[0]);
				//System.out.println("packet text: " + testText);
				//-------------------------------------
				
				//add file id if it is new
				int fileID = ((int) inByteData[1]);
				if (IDArr.contains(fileID)!=true) {
					IDArr.add(fileID);
				}
				
				checkFileCompletion(inPacket, IDArr);

				//check if header (the last bit of the status byte)
				if(inByteData[0]==0){					
					byte[] fn = Arrays.copyOfRange(inByteData, 2, inPacket.getLength());
					filename = new String(fn, "US-ASCII");
					filenameArr.add(filename);
				
				} else {
					//it is a data file
					//packet number is bytes 3-4, data is every byte after 4 
					//check if it is the last data packet
					if(inByteData[0]==3){
						lastData = true;
					} 
					//store packet data
					byte[] data = Arrays.copyOfRange(inByteData, 4, inPacket.getLength());
					
					//TODO: write "storeData" function to check file id and add to respective array list
					//storeData(inpacket, fileID)
					
					//for now add to file1 arrayList for testing	
					f1Bytes.add(data);
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

			socket.close();	
			System.out.println("DONE");

		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public static void checkFileCompletion(DatagramPacket lastPacket, ArrayList<Integer> IDArr) {
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
	
	public static void storeData(DatagramPacket packet, int fileID) {
		//TODO: check file id and add data to respective array list
	}
}
