import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.SecretKey;

public class FarashGateway3 {

	public static double acosh(double x) {
		return Math.log(x + Math.sqrt(x * x - 1.0));
	}

	public static double chebyshev(double x, int z, int n) {
		return Math.cosh(n * acosh(x) % z);
	}

	public static String XOREncode(String st, String key) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < key.length(); i++)
			sb.append((char) (st.charAt(i) ^ key.charAt(i)));
		String str = sb.toString();
		str = str + st.substring(key.length());
		// System.out.println(st.substring(key.length()));
		return str;
	}

	public static String XORDecodekey(String st, String key) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < key.length(); i++)
			sb.append((char) (st.charAt(i) ^ key.charAt(i)));
		String str = sb.toString();
		return str;
	}

	public static String XORDecodeString(String st, String key) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < key.length(); i++)
			sb.append((char) (st.charAt(i) ^ key.charAt(i)));
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

	public static void main(String[] args) throws IOException {
		
		final int PORT = 4086;
				
		ServerSocket serverSocket = new ServerSocket(PORT);
		Socket clientSocket = serverSocket.accept();
		DataInputStream din = new DataInputStream(clientSocket.getInputStream());
		DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());
		
		String input = "", str2 = "";
		while (!input.equals("stop")) {
			
			////////////////receives from trusted device //////////////////////
			input = din.readUTF();
			System.out.println("Received at GW: "+ input);

			if (input.equalsIgnoreCase("stop")) {
				break;
			}else {
				String received[] = input.split("<-->"); //M1;M2;M3;T1;T2;ESIDj;M4;M5
				String M1 = received[0];
				String M2 = received[1];
				String M3 = received[2];
				long T1 = Long.parseLong(received[3]);
				long T2 = Long.parseLong(received[4]);
				String ESIDj = received[5];
				String M4 = received[6];
				String M5 = received[7];
				String content = new Scanner(new File("store.txt")).useDelimiter("\\Z").next();
				int Xgwn = Integer.parseInt(content);
				long T3 = System.currentTimeMillis();
				
				if ((T3-T2)>1000) {
	        		System.out.println("System time out...."+(T3-T2));
	        		break;
	        	}
				
				String hXgwn1 = getSha256(""+Xgwn+1);
				String ESIDj1 = getSha256(hXgwn1+T2);
				//System.out.println("T2: "+ T2);
				//System.out.println("hXgwn1: "+ hXgwn1);
				//System.out.println("ESIDj1: "+ ESIDj1);
				
				String SIDjp1 = XORDecodeString(ESIDj, ESIDj1).trim();
				int SIDjp = Integer.parseInt(SIDjp1);
				String xjp = getSha256(""+SIDjp+Xgwn);
				String M41 = getSha256(xjp+T1+T2);
				String Kj1 = XORDecodeString(M4, M41).trim();
				int Kjp = Integer.parseInt(Kj1);
				String M5p = getSha256(SIDjp+M4+T1+T2+Kjp);
				if(!M5p.equals(M5)) {
	    			System.out.println("M5p != M5");
	    			break;
	    		}
				System.out.println("M5p == M5");
				
				String HXgwn = getSha256(""+Xgwn);
				String M11 = getSha256(HXgwn+T1);
				String IDip = XORDecodeString(M1, M11).trim();
				String dip = getSha256(IDip+Xgwn);
				String M22 = getSha256(dip+T1);
				String Kip = XORDecodeString(M2, M22).trim();
				String M3p = getSha256(M1+M2+Kip+T1);
				
				if(!M3p.equals(M3)) {
	    			System.out.println("M3p != M3");
	    			break;
	    		}
				System.out.println("M3p == M3");

				String M66 = getSha256(dip+T3);
				String M6 = XOREncode(M66, ""+Kjp);
				String M77 = getSha256(xjp+T3);
				String M7 = XOREncode(M77, ""+Kip);
				String M8 = getSha256(M6+dip+T3);
				String M9 = getSha256(M7+xjp+T3);
				
				System.out.println("Kj: "+Kjp);
				System.out.println("xjp: "+xjp);
				System.out.println("SIDjp: "+SIDjp);
				System.out.println("IDip: "+ IDip);
				System.out.println("Kip: "+ Kip);
				////////////////sending to trusted device //////////////////////					
					dout.writeUTF(M6+"<-->"+M7+"<-->"+M8+"<-->"+M9+"<-->"+T3); // send to trusted device
					dout.flush();
					System.out.println("Sent to D from G: G -> D");

				

			}
			
			
		}
		
	}

}
