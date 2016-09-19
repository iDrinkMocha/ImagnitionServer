import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class EchoThread extends Thread {
    
	protected Socket server;
    private String clientIP, filetype;
    private byte messageType;
//    private final static String FILE_TO_SEND = "results.txt";  
    private int counter = 0;
    private int filecounter = 0;
    private String everything;
    

    public EchoThread(Socket clientSocket) {
        this.server = clientSocket;
        PictureServer ps = new PictureServer();
        filecounter = ps.filecount;
    }

    public void run() {
    	 while(true)
         { 
              try
              {
                 DataInputStream dIn = new DataInputStream(server.getInputStream());
                 DataOutputStream output = new DataOutputStream(server.getOutputStream());
                 messageType = dIn.readByte();
                 switch(messageType){
                 	case 1:
                 		clientIP = dIn.readUTF();
                 		System.out.println(clientIP);
                 		messageType = dIn.readByte();
                 		
                 		if(messageType == -1){
             
                 			File file = new File("input" + filecounter + ".jpg");
                 			ObjectInputStream ois = new ObjectInputStream(server.getInputStream());
                 			System.out.println("Input connected");
                 		    byte[] bytes;
                 		    FileOutputStream fos = null;
                 		    try {
                 		     bytes = (byte[])ois.readObject();
//                 		     ois.close();
                 		     System.out.println("Reading");
                 		     fos = new FileOutputStream(file);
                 		     fos.write(bytes);
                 		     System.out.println("Writing");
                 		     fos.flush();
                 		     Date dNow = new Date( );
                 		     SimpleDateFormat ft = new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
                 		     System.out.println("Received: " + ft.format(dNow));
                 		    } catch (ClassNotFoundException e) {
                 		     // TODO Auto-generated catch block
                 		     //e.printStackTrace();
                 		     System.out.println("class not found");
                 		    }  catch (EOFException e){
                 		    	//e.printStackTrace();
                 		    	 System.out.println("EOF");
                 		    } catch(IOException e){
                 		    	//e.printStackTrace();
                 		    	 System.out.println("IOExc");
                 		    }
                 		    if (!file.exists()) {
								System.out.println("File already exists!");
								
							}
                           String command = "sudo python classify_image.py ";
                           Process p = Runtime.getRuntime().exec(command + "--image_file " + "input" + filecounter + ".jpg");
                           System.out.println("Classified");
//                           server.shutdownInput();
                           p.waitFor();
                         
                           //DATA ADDITIONS
                           try(BufferedReader br = new BufferedReader(new FileReader("input" + filecounter + ".jpg" +"r.txt"))) {
                               StringBuilder sb = new StringBuilder();
                               String line = br.readLine();

                               while (line != null) {
                                   sb.append(line);
                                   sb.append(System.lineSeparator());
                                   line = br.readLine();
                                   ++counter;
                               }
                               everything = sb.toString();
                  
                               String[] display;
                               display = everything.split(Pattern.quote("\n"));
                               output.writeByte(3); //3 for good
                               output.writeByte(counter);
                               output.flush();
                               for(int i = 0; i < display.length; ++i){
                            	   System.out.println(i + " " + display[i]);
                               }
                               output.writeUTF("|" + "IMAGNITIONS|" + display[0] + "|" + display[2] + "|" + display[4] + "|" + display[6] + "|" + display[8] + "|IMAGNITION" + "|");
                               output.flush();
                           
                               
                               File file1 = new File("input" + filecounter + ".jpg" +"r.txt");
                               file.delete();
                               file1.delete();
                               System.out.println("Images deleted");
                               
                               
                           } 
                           catch(FileNotFoundException fe){
                        	   System.out.println("----FAILED-----");
                        	   output.writeByte(-3); //for bad
                               output.flush();
                               file.delete();
                               System.out.println("--EXCESS FILES DELETED---");
                           }catch(IOException e1){
                           	e1.printStackTrace();
                           	file.delete();
                           	File file1 = new File("input" + filecounter + ".jpg" +"r.txt");
                           	file1.delete();
                           	return;
                           } 
                           	 
                 		   }

                 		else{
                 			System.out.println("Error");
                 			System.exit(1);
                 		}
                 		break;
               	default:
               		System.out.println("Invalid IP processing, exit" + messageType);
               		break;
                 }
                 
                 return;
             }
            catch(SocketTimeoutException st)
            {
                  System.out.println("Socket timed out!");
                 break;
            }
            catch(EOFException ee){
            	System.out.println("EOF");
            	break;
            }
            catch(StreamCorruptedException sce){
            	System.out.println("File corrupt");
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
    	 return;
    }
}