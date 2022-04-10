import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;
import java.io.*;
import java.math.BigInteger;  

class FarashUserRegistrationGW{  
	
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
	
	
	public static void main(String args[])throws Exception{  
        final int PORT = 4082;
        int Xgwn = 4321;
        ServerSocket serverSocket = new ServerSocket(PORT);
        Socket clientSocket = serverSocket.accept();
        DataInputStream din=new DataInputStream(clientSocket.getInputStream());  
		DataOutputStream dout=new DataOutputStream(clientSocket.getOutputStream());  
		
		String input="",str2="";  
		while(!input.equals("stop")){  
			
			input=din.readUTF(); // receive option
			System.out.println("input: "+input);
			
			if (input.equalsIgnoreCase("stop")) {
            	serverSocket.close();
            	System.out.println("---->>> connection aborted.......");
                break;
            } 
//////////////////////////////////User Registration /////////////////////////////////			
			else if (input.equals("u")){
				input=din.readUTF(); // receive data
            	String received[] = input.split("-"); // Mpi, IDi
    			String ei = getSha256(received[0]+received[1]);
    			String di = getSha256(received[1]+Xgwn);
    			String gi1 = getSha256(""+Xgwn);
    			String gi2 = getSha256(received[0]+di);
    			String gi = XOREncode(gi1, gi2);
    			String fi1 = getSha256(received[0]+ei);
    			String fi = XOREncode(di, fi1);
            	
            	dout.writeUTF(ei+"-1-"+fi+"-1-"+gi); //send
				dout.flush();	
				System.out.println("77 "+ ei +" 77");
            	System.out.println("77 "+ fi +" 77");
            	System.out.println("77 "+ gi +" 77");
				System.out.println("user registration completed.");
            } 
//////////////////////////////////Sensor Registration /////////////////////////////////			
			else if(input.equals("s")) {
				input=din.readUTF(); // receive data
            	int SIDj = 2222;
            	int Xgwnsj = 6543;
            	long T2 = System.currentTimeMillis();
            	String received[] = input.split("-"); // 0-SIDj - 1-MPj - 2-MNj - 3-T1
            	long T1 = Long.parseLong(received[3]);
            	if ((T2-T1)>100) {
            		System.out.println("System time out....");
            		break;
            	}
            	System.out.println("time: "+ (T2-T1));
            	String rjp = XOREncode(received[2], ""+Xgwnsj);
            	String MPj = getSha256(""+Xgwnsj+rjp+received[0]+received[3]);
            	System.out.println("received[1] = "+received[1]);
            	System.out.println("MPj = "+MPj);
            	if(!received[1].equals(MPj)) {
            		System.out.println("MPj mismatch... ");
            		break;
            	}
            	String xj = getSha256(received[0]+Xgwn);
        		String ej = XOREncode(xj, ""+Xgwnsj);
        		String dj1 = getSha256(""+Xgwn+1);
        		String dj2 = getSha256(""+Xgwnsj+T2);
        		String dj = XOREncode(dj1, dj2);
            	String fj = getSha256(xj+dj+Xgwnsj+T2);
            	
            	dout.writeUTF(ej+"-"+fj+"-"+dj+"-"+T2); //send
            	System.out.println("77 "+ ej +" 77");
            	System.out.println("77 "+ fj +" 77");
            	System.out.println("77 "+ dj +" 77");
            	
				dout.flush();	
				System.out.println("xj: "+ xj);
				System.out.println("dj1: "+ dj1);
				System.out.println("Sensor registration completed.");
            	
            } else {
            	System.out.println("type 'u' for user registration then hit enter");
            	System.out.println("type 's' for sensor registration then hit enter");
            }
		}
		din.close();  
		clientSocket.close();  
		serverSocket.close();

	}
}  