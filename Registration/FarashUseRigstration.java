
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.io.*;
import java.math.BigInteger;  

class FarashUseRigstration{  
	
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
	
	public static void main(String args[])throws Exception{  
		
		
		final String HOST = "127.0.0.1";
        final int PORT = 4082;
        
        int IDi = 111;
        int PWi = 12345;
        
        Socket socket = new Socket(HOST, PORT);
		DataInputStream din=new DataInputStream(socket.getInputStream());  
		DataOutputStream dout=new DataOutputStream(socket.getOutputStream());  
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));  

		String input="";  
		while(!input.equals("stop")){  
			input=br.readLine();  
			
			if (input.equalsIgnoreCase("stop")) {
				dout.writeUTF(input);
				dout.flush();
				System.out.println("---->>> connection aborted.......");
            	break;
            } 
////////////////////////////////////User Registration /////////////////////////////////
			else if (input.equals("u")){
				dout.writeUTF("u"); // send option
				dout.flush();
				
            	Random rnd = new SecureRandom();
    			int ri = BigInteger.probablePrime(15, rnd).intValue();
    			String MPi = getSha256(""+ri+PWi);
    			
            	dout.writeUTF(MPi+"-"+IDi); // send
				dout.flush();
				
				
				String received = din.readUTF(); // receive
				String store = ""+ri+"-1-"+received;
				System.out.println("SC: "+store);
				
				Writer output;
	    		output = new BufferedWriter(new FileWriter("SC.txt"));  //clears file every time
	    		output.append(store);
	    		output.close(); 
				/*File file = new File("SC.txt");
				FileWriter fileWriter = new FileWriter(file,true);
				fileWriter.write("\r\n");*/
	    		System.out.println("user registration completed.");
	    		
				/*
				 * String content = new Scanner(new File("SC.txt")).useDelimiter("\\Z").next();
				 * System.out.println("\n----> "+content);
				 * 
				 * String recvd[] = content.split("-1-");
				 * System.out.println("\n----> length: "+recvd.length);
				 */
            } 
//////////////////////////////////// Sensor Registration /////////////////////////////////
            else if(input.equals("s")) { 
            	dout.writeUTF("s"); // send option
				dout.flush();
            	int SIDj = 2222;
            	int Xgwnsj = 6543;
            	Random rnd = new SecureRandom();
    			int rj = BigInteger.probablePrime(15, rnd).intValue();
    			long T1 = System.currentTimeMillis();
            	String MPj = getSha256(""+Xgwnsj+rj+SIDj+T1);
            	String MNj = XOREncode(""+rj, ""+Xgwnsj);
            	System.out.println("MPj = "+MPj);
            	dout.writeUTF(SIDj+"-"+MPj+"-"+MNj+"-"+T1); // send
				dout.flush();
				
				String rev = din.readUTF(); // receive
				String received[] = rev.split("-"); // 0-ej - 1-fj - 2-dj - 3-T2
				long T3 = System.currentTimeMillis();
				long T2 = Long.parseLong(received[3]);
            	if ((T3-T2)>100) {
            		System.out.println("System time out....");
            		break;
            	}
            	System.out.println("time: "+ (T2-T1));
            	
            	String xj = XORDecodeString(received[0], ""+Xgwnsj);
            	System.out.println("xj: "+ xj);
            	String fj = getSha256(xj+received[2]+Xgwnsj+T2);
            	if(!received[1].equals(fj)) {
            		System.out.println("fj mismatch... ");
            		break;
            	}
            	String hXgwn12 = getSha256(""+Xgwnsj+T2);
            	String hXgwn1 = XORDecodekey(received[2], hXgwn12);
            	System.out.println("hXgwn1: "+ hXgwn1);
            	
            	String store = xj+"-"+hXgwn1;
            	Writer output;
	    		output = new BufferedWriter(new FileWriter("mem.txt"));  //clears file every time
	    		output.append(store);
	    		output.close();
	    		System.out.println("Sensor registration completed.");
    			
            } else {
            	dout.writeUTF(""); // send option
				dout.flush();
            	System.out.println("type 'u' for user registration then hit enter");
            	System.out.println("type 's' for sensor registration then hit enter");
            }
		}

		dout.close();  
		socket.close();
	
	}
} 