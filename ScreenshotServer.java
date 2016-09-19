import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.Scanner;

import javax.imageio.ImageIO;


public class ScreenshotServer extends Thread
{
       private ServerSocket serverSocket;
       Socket server;
       private String clientIP;
       private byte messageType;
       private final static String FILE_TO_SEND = "results.txt";  
       private int counter = 0;
       private String[] store;
       private String everything;

       public ScreenshotServer(int port) throws IOException, SQLException, ClassNotFoundException, Exception
       {
          serverSocket = new ServerSocket(port);
          serverSocket.setSoTimeout(30000);
       }

       public void run()
       {
           while(true)
          { 
               try
               {
                  server = serverSocket.accept();
                  DataInputStream dIn = new DataInputStream(server.getInputStream());
                  DataOutputStream output = new DataOutputStream(server.getOutputStream());
//                  FileInputStream fis = null; //NEW ADDITIONS
//                  BufferedInputStream bis = null;
//                  OutputStream os = null;
                  messageType = dIn.readByte();
                  switch(messageType){
                  	case 1:
                  		clientIP = dIn.readUTF();
                  		System.out.println(clientIP);
                  		messageType = dIn.readByte();
                  		System.out.println("Type:Photo");
                  		if(messageType == -1){
//                  			BufferedImage image = ImageIO.read(ImageIO.createImageInputStream(server.getInputStream()));
//                            System.out.println("Received " + image.getHeight() + "x" + image.getWidth() + ": " + System.currentTimeMillis());
//                            ImageIO.write(image, "jpg", new File("input.jpg"));
                  			File file = new File("input.jpg");
                  			ObjectInputStream ois = new ObjectInputStream(server.getInputStream());
                  			System.out.println("Input connected");
                  		    byte[] bytes;
                  		    FileOutputStream fos = null;
                  		    try {
                  		     bytes = (byte[])ois.readObject();
                  		     System.out.println("Reading");
                  		     fos = new FileOutputStream(file);
                  		     fos.write(bytes);
                  		     System.out.println("Writing");
                  		     fos.flush();
                  		     System.out.println("Received: " + System.currentTimeMillis());
                  		    } catch (ClassNotFoundException e) {
                  		     // TODO Auto-generated catch block
                  		     e.printStackTrace();
                  		    }  catch (EOFException e){
                  		    	e.printStackTrace();
                  		    } catch(IOException e){
                  		    	e.printStackTrace();
                  		    }
                  		    if (!file.exists()) {
								System.out.println("Transfer failed!");
								System.exit(1);
							}
                            String command = "sudo python classify_image.py ";
                            Process p = Runtime.getRuntime().exec(command + "--image_file " + "input.jpg");
                            System.out.println("Classified");
//                            server.shutdownInput();
                            p.waitFor();
                          
                            //DATA ADDITIONS
                            try(BufferedReader br = new BufferedReader(new FileReader("results.txt"))) {
                                StringBuilder sb = new StringBuilder();
                                String line = br.readLine();

                                while (line != null) {
                                    sb.append(line);
                                    sb.append(System.lineSeparator());
                                    line = br.readLine();
                                    ++counter;
                                }
                                everything = sb.toString();
                                System.out.println(everything);
                                output.writeByte(counter);
                                output.flush();
                                output.writeUTF(everything);
                                output.flush();
                                
                                File file1 = new File("results.txt");
                                file.delete();
                                file1.delete();
                                System.out.println("Images deleted");
                                
                            } catch(IOException e1){
                            	e1.printStackTrace();
                            }
                  		   }

                  		else{
                  			System.out.println("Error");
                  			restart();
                  		}
                  		break;
                	default:
                		System.out.println("Invalid IP processing, exit");
                		restart();
                  }
                  
                  
              }
             catch(SocketTimeoutException st)
             {
                   System.out.println("Socket timed out!");
                  break;
             }
             catch(IOException e)
             {
                  e.printStackTrace();
                  break;
             }
             catch(Exception ex)
            {
                  System.out.println(ex);
            }
          }
       }

       public static void main(String [] args) throws IOException, SQLException, ClassNotFoundException, Exception
       {
              Thread t = new ScreenshotServer(12345);
              t.start();
              System.out.println("Initialised");
//              Send t2 = new Send("BTC");
//              t2.start();
       }
       
       public String returnIp(){
    	   return clientIP;
       }
       
       public void restart() {
    	   StringBuilder cmd = new StringBuilder();
           cmd.append(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java ");
           for (String jvmArg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
               cmd.append(jvmArg + " ");
           }
           cmd.append("-cp ").append(ManagementFactory.getRuntimeMXBean().getClassPath()).append(" ");
           cmd.append(ScreenshotServer.class.getName()).append(" ");
           try{
        	   Runtime.getRuntime().exec(cmd.toString());
               System.out.println("restarting");
           } catch (IOException e){
        	   e.printStackTrace();
           }
           System.exit(0);
       }
}
