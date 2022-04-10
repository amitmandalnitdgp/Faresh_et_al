import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;

import javax.crypto.SecretKey;

public class FarashDevice3 {

	public int Di, PW, Rd, T, PID, PIN;
	public String com1;

	public static double acosh(double x)
	{
		return Math.log(x + Math.sqrt(x*x - 1.0));
	}

	public static double chebyshev(double x, int z, int n) {
		return Math.cosh(n*acosh(x)%z);
	}

	public static String XOREncode(String st, String key) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < key.length(); i++)
			sb.append((char)(st.charAt(i) ^ key.charAt(i)));
		String str = sb.toString();
		str = str + st.substring(key.length());
		//System.out.println(st.substring(key.length()));
		return str;
	}

	public static String XORDecodekey(String st, String key) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < key.length(); i++)
			sb.append((char)(st.charAt(i) ^ key.charAt(i)));
		String str = sb.toString();
		return str;
	}

	public static String XORDecodeString(String st, String key) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < key.length(); i++)
			sb.append((char)(st.charAt(i) ^ key.charAt(i)));
		String str = sb.toString();
		str = str + st.substring(key.length());
		return str;
	}

	public static String getSha256(String str) {
		MessageDigest digest;
		String encoded = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(str.getBytes(StandardCharsets.UTF_8));
			encoded = Base64.getEncoder().encodeToString(hash);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return encoded;
	} 

	public static void main(String[] args) throws Exception {
		
		double Eelec = 50.0;
		double Eamp = 0.1;
		double d = 1.0;
		long size1 = -1, size2 = -1, size3 = -1, size4 = -1;

		final String HOST = "127.0.0.1";
		final int PORTin = 4085;
		final int PORTout = 4086;
		int SIDj = 2222;
    	int Xgwnsj = 6543;
/////////////////////// sockets for the new device ///////////////////////////////////////////////////////////////
		ServerSocket trustedServerSocket = new ServerSocket(PORTin);
		Socket trustedClientSocket = trustedServerSocket.accept();
		DataInputStream Device_indata=new DataInputStream(trustedClientSocket.getInputStream());  
		DataOutputStream Device_outdata=new DataOutputStream(trustedClientSocket.getOutputStream());  

/////////////////////// sockets for the Gateway ///////////////////////////////////////////////////////////////		
		
		Socket GWsocket = new Socket(HOST, PORTout);
		DataInputStream GWindata=new DataInputStream(GWsocket.getInputStream());  
		DataOutputStream GWoutdata=new DataOutputStream(GWsocket.getOutputStream()); 
		
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
		String input = "", input2 = "";
		while (!input.equals("stop")) {
			
			//////////////// receives from User //////////////////////
			input = Device_indata.readUTF();
			System.out.println("Received at D from U: "+ input);
			
			if (input.equalsIgnoreCase("stop")) {
				GWoutdata.writeUTF(input);
				GWoutdata.flush();
				break;
				
			}else {
				String received[] = input.split("<-->"); // M1 M2 M3 T1
				long T2 = System.currentTimeMillis();
				long T1 = Long.parseLong(received[3]);
				
				size1 = (received[0].length()+received[1].length()+received[2].length()+received[3].length())*16;
				
				if ((T2-T1)>1000) {
            		System.out.println("System time out...."+(T2-T1));
            		break;
            	}
				
				String content = new Scanner(new File("mem.txt")).useDelimiter("\\Z").next();
				String storeRead[] = content.split("-"); //xj+"-"+hXgwn1;
				String xj = storeRead[0];
				String hXgwn1 = storeRead[1];
				//System.out.println("T2: "+ T2);
				//System.out.println("hXgwn1: "+ hXgwn1);
				System.out.println("xj: "+ xj);
				String ESIDj1 = getSha256(hXgwn1+T2);
				//System.out.println("ESIDj1: "+ ESIDj1);
				String ESIDj = XOREncode(ESIDj1, ""+SIDj);
				Random rnd = new SecureRandom();
    			int Kj = BigInteger.probablePrime(15, rnd).intValue();
    			String M41 = getSha256(xj+T1+T2);
    			String M4 = XOREncode(M41, ""+Kj);
    			System.out.println("Kj: "+ Kj);
    			String M5 = getSha256(SIDj+M4+T1+T2+Kj);
				////////////////Sending to Gateway //////////////////////
				
    			String sendtoGWsize = received[0]+received[1]+received[2]+T1+T2+ESIDj+M4+M5;
				size2 = sendtoGWsize.length()*16;
    			
    			GWoutdata.writeUTF(received[0]+"<-->"+received[1]+"<-->"+received[2]+"<-->"+T1+"<-->"+T2+"<-->"+ESIDj+"<-->"+M4+"<-->"+M5);
				GWoutdata.flush();
				System.out.println(received[0]+"<-->"+received[1]+"<-->"+received[2]+"<-->"+T1+"<-->"+T2+"<-->"+ESIDj+"<-->"+M4+"<-->"+M5);;
				
				////////////////receives from Gateway //////////////////////
				input2 = GWindata.readUTF(); // M6+"<-->"+M7+"<-->"+M8+"<-->"+M9+"<-->"+T3
				System.out.println("received from GW: "+ input2);
				String receivedGW[] = input2.split("<-->");
				long T3 = Long.parseLong(receivedGW[4]);
				if ((T3-T2)>1000) {
            		System.out.println("System time out...."+(T2-T1));
            		break;
            	}
				String M6 = receivedGW[0];
				String M7 = receivedGW[1];
				String M8 = receivedGW[2];
				String M9 = receivedGW[3];
				
				size3 = (receivedGW[0].length()+receivedGW[1].length()+receivedGW[2].length()+receivedGW[3].length()+receivedGW[4].length())*16;
				
				String M9p = getSha256(M7+xj+T3);
				if(!M9p.equals(M9)) {
	    			System.out.println("M9p != M9");
	    			break;
	    		}
				System.out.println("M9p == M9");
				String M77 = getSha256(xj+T3);
				String Kip = XORDecodeString(M7, M77).trim();
				String SK = getSha256(XOREncode(Kip,""+Kj));
				long T4 = System.currentTimeMillis();
				String M10 = getSha256(SK+M6+M8+T3+T4);
				
				System.out.println("Kip: "+Kip);
				System.out.println("SK: "+SK);
				////////////////Sending to User //////////////////////
				
				String sizemsgtoGW = M6+M8+M10+T3+T4; 
				size4 = sizemsgtoGW.length()*16;
				
				Device_outdata.writeUTF(M6+"<-->"+M8+"<-->"+M10+"<-->"+T3+"<-->"+T4);
				Device_outdata.flush();
				System.out.println("Send from D to U: D - > U");
				
				long receiveMsgSize = size1+size3;
				long sendMsgSize = size2+size4;
				long afterUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
				double memKB = Math.round(((afterUsedMem/(8*1024))*100))/100.0 ;
				double sendEnergy = (Eelec*sendMsgSize)+(Eamp*sendMsgSize*d*d);
				double receiveEnergy = Eelec*receiveMsgSize;
				double totalEnergy = sendEnergy+receiveEnergy;
				
				System.out.println("memory usage: " + memKB + " KB");
				System.out.println("Communication cost (send message size): " + sendMsgSize + " bytes");
				System.out.println("receive message size: " + receiveMsgSize + " bytes");
				System.out.println("Sending Energy: " + sendEnergy + " nJ");
				System.out.println("Receiving Energy: " + receiveEnergy + " nJ");
				System.out.println("Total Energy: " + totalEnergy + " nJ");
				
				String store = memKB+"\t"+sendMsgSize+"\t"+receiveMsgSize+"\t"+sendEnergy+"\t"+receiveEnergy+"\t"+totalEnergy;
				Writer output;
				output = new BufferedWriter(new FileWriter("Results.txt", true));  //clears file every time
				output.append(store+"\n");
				output.close();
				
			}
		}

	}

}
