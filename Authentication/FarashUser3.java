import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Writer;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;


public class FarashUser3 {

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
		
		Instant responseStart = Instant.now();
		Instant responseEnd = Instant.now();
		long handshakeDuration = -1;
		long sendMsgSize = -1, receiveMsgSize = -1;;
		// memory usage before execution
		long beforeUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
		double Eelec = 50.0;
		double Eamp = 0.1;
		double d = 1.0;
		
		
		long count = 0, total = 0, avgElapsedTime = 0, n= 1;
		final String HOST = "127.0.0.1";
		final int PORT = 4085;
		
		 int IDi = 111;
	     int PWi = 12345;
		
		String exitStatus= "";
		Socket socket = new Socket(HOST, PORT);
		DataInputStream indata=new DataInputStream(socket.getInputStream());  
		DataOutputStream outdata=new DataOutputStream(socket.getOutputStream());  
		BufferedReader brk=new BufferedReader(new InputStreamReader(System.in)); 
		
		while (count<n) {
			
			
			//exitStatus=brk.readLine();//keyboard input

			if (exitStatus.equalsIgnoreCase("stop")) {
				outdata.writeUTF(exitStatus);
				outdata.flush();
				break;
			}
			
			Instant start = Instant.now();
			
			////////////////Sending to trusted device //////////////////////
			String content = new Scanner(new File("SC.txt")).useDelimiter("\\Z").next();
    		System.out.println("\n----> "+content);
    		
    		String storeRead[] = content.split("-1-");
    		//System.out.println("\n----> length: "+recvd.length);
    		int ri = Integer.parseInt(storeRead[0]);
    		String ei = storeRead[1];
    		String fi = storeRead[2];
    		String gi = storeRead[3];
    		
    		String MPip =  getSha256(""+ri+PWi);
    		String eip = getSha256(""+MPip+IDi);
    		if(!eip.equals(ei)) {
    			System.out.println("eip != ei");
    			break;
    		}
    		System.out.println("eip == ei");
    		String di1 = getSha256(MPip+ei);
    		String di = XOREncode(fi, di1);
    		String HXgwn1 = getSha256(MPip+di);
    		String HXgwn = XOREncode(gi, HXgwn1);
    		long T1 = System.currentTimeMillis();
    		String M11 = getSha256(HXgwn+T1);
    		String M1 = XOREncode(M11, ""+IDi);
    		
    		Random rnd = new SecureRandom();
			int Ki = BigInteger.probablePrime(15, rnd).intValue();
			
			String M21 = getSha256(di+T1);
			String M2 = XOREncode(M21, ""+Ki);
			String M3 = getSha256(M1+M2+Ki+T1);
    		
    				
			//String[] storeRead = storetemp.split("-");
			//System.out.println("length: "+storeRead.length+"  "+ri);
			//System.out.println("storetemp: "+content);
			
			String sendsize = M1+M2+M3+T1;
			sendMsgSize = sendsize.length()*16;
    		
    		responseStart = Instant.now(); // start of response time
			
			outdata.writeUTF(M1+"<-->"+M2+"<-->"+M3+"<-->"+T1);
			outdata.flush();
			System.out.println("Sent: "+ M1+"<-->"+M2+"<-->"+M3+"<-->"+T1);
				
////////////////Receiving from trusted device //////////////////////		
			String input2 = indata.readUTF(); //M6+"<-->"+M8+"<-->"+M10+"<-->"+T3+"<-->"+T4
			
			responseEnd = Instant.now(); // End of response time
			
			System.out.println("Received from D: "+input2);
			
			String receivedDev[] = input2.split("<-->");
			String M6 = receivedDev[0];
			String M8 = receivedDev[1];
			String M10 = receivedDev[2];
			long T3 = Long.parseLong(receivedDev[3]);
			long T4 = Long.parseLong(receivedDev[4]);
			
			String receivesize = receivedDev[0]+receivedDev[1]+receivedDev[2]+receivedDev[3]+receivedDev[4];
			receiveMsgSize = receivesize.length()*16;
			
			if ((T4-T1)>1000) {
        		System.out.println("System time out...."+(T4-T1));
        		break;
        	}
			
			String M8p = getSha256(M6+di+T3);
			if(!M8p.equals(M8)) {
    			System.out.println("M8p != M8");
    			break;
    		}
			System.out.println("M8p == M8");
			
			String M66 = getSha256(di+T3);
			String Kjp = XORDecodeString(M6, M66).trim();
			String SK = getSha256(XOREncode(""+Ki,Kjp));
			String M10p = getSha256(SK+M6+M8+T3+T4);
			if(!M10p.equals(M10)) {
    			System.out.println("M10p != M10");
    			break;
    		}
			System.out.println("M10p == M10");
			
			System.out.println("Kjp: "+Kjp);
			System.out.println("SK: "+SK);
			
			long T5 = System.currentTimeMillis();
			//System.out.println("Time: "+(T5-T1)+" millisecond");
			
			Instant finish = Instant.now();
			long timeElapsed = Duration.between(start, finish).toMillis();
			System.out.println("timeElapsed: "+timeElapsed+" milliseconds");
			
			if(count>0) {
				total = total+timeElapsed;
			}
			count++;
			
			avgElapsedTime = total/(n);
			//System.out.println("avgElapsedTime: "+avgElapsedTime+" milliseconds");
			outdata.writeUTF("stop");
			outdata.flush();
			
			handshakeDuration = Duration.between(start, finish).toMillis();
			long afterUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
			long actualMemUsed=afterUsedMem-beforeUsedMem;
			double memKB = Math.round(((afterUsedMem/(8*1024))*100))/100.0 ;
			long responseTime = Duration.between(responseStart, responseEnd).toMillis();
			double sendEnergy = (Eelec*sendMsgSize)+(Eamp*sendMsgSize*d*d);
			double receiveEnergy = Eelec*receiveMsgSize;
			double totalEnergy = sendEnergy+receiveEnergy;
			
			System.out.println("\nresponse time: "+responseTime+" milliseconds");
			System.out.println("handshake duration: "+handshakeDuration+" milliseconds");
			System.out.println("memory usage: " + memKB + " KB");
			System.out.println("Communication cost (send message size): " + sendMsgSize + " bytes");
			System.out.println("receive message size: " + receiveMsgSize + " bytes");
			System.out.println("Sending Energy: " + sendEnergy + " nJ");
			System.out.println("Receiving Energy: " + receiveEnergy + " nJ");
			System.out.println("Total Energy: " + totalEnergy + " nJ");
			
			String store = responseTime+"\t"+handshakeDuration+"\t"+memKB+"\t"+sendMsgSize+"\t"+receiveMsgSize+"\t"+sendEnergy+"\t"+receiveEnergy+"\t"+totalEnergy;
			Writer output;
			output = new BufferedWriter(new FileWriter("Results.txt", true));  //clears file every time
			output.append(store+"\n");
			output.close();
		}
		
	} 

}
