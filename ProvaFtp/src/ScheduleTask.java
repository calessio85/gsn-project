import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;



public class ScheduleTask{
	
	Date now;
	
	public static void main(String[] args){
		FTPClient client = new FTPClient();
		File appunto;
		FileOutputStream fos = null;
		String[] rel_tb = new String[2];
		Boolean var = false;
		
		
		try{
			System.out.println("Tento la connessione al server...");
			client.connect("158.110.20.93");
			client.enterLocalPassiveMode();
			client.login("AWS","LaMare");
			System.out.println("Collegato al server");
			
			FTPFile[] file_tb1 = client.listFiles("/CR1000_2_Table1.dat");
			FTPFile[] file_tb2 = client.listFiles("/CR1000_2_Table2.dat"); 
			
			String path_tb1 = "/home/calessio/Scrivania/CR1000.dat";
			String path_tb2 = "/home/calessio/Scrivania/CR1001.dat";
			
			long time_tb1 = file_tb1[0].getTimestamp().getTimeInMillis();
			long time_tb2 = file_tb2[0].getTimestamp().getTimeInMillis();
			
			appunto = new File("dDown.txt");
			if(!appunto.exists()){
				if(!appunto.createNewFile()){
					System.out.println("Impossibile creare il file!!");
					return;
				}
			}
			
			try {
				BufferedReader input = new BufferedReader(new FileReader(appunto));
				rel_tb[0] = input.readLine();
				rel_tb[1] = input.readLine();
				input.close();
				
			} catch (IOException ioException) {
				System.out.println("Errore lettura file!!");
			}
			
			
			if ((rel_tb[0] != null) && (rel_tb[0].compareTo(String.valueOf(time_tb1)) == 0))
				System.out.println("File CR1000.dat aggiornato alla data corrente");
			else{
				System.out.println("Download CR1000.dat aggiornato");
				fos = new FileOutputStream(path_tb1);
				client.retrieveFile("/CR1000_2_Table1.dat",fos);
				var = true;
			}	
			
			if ((rel_tb[1] != null) && (rel_tb[1].compareTo(String.valueOf(time_tb2)) == 0))
				System.out.println("File CR1001.dat aggiornato alla data corrente");
			else{
				System.out.println("Download CR1001.dat aggiornato");
				fos = new FileOutputStream(path_tb2);
				client.retrieveFile("/CR1000_2_Table2.dat",fos);
				var = true;
			}	
			if(var){
				appunto.delete();
				appunto.createNewFile();
				FileWriter fw = new FileWriter(appunto);
			    fw.write(String.valueOf(time_tb1)+"\n");
			    fw.write(String.valueOf(time_tb2)+"\n");
			    fw.flush();
			    fw.close();
			    SimpleDateFormat sim = new SimpleDateFormat("dd/MM/yyyy' 'HH:MM:ss");
	            System.out.println("File CR1000.dat aggiornato alla data "+sim.format(time_tb1));
	            System.out.println("File CR1001.dat aggiornato alla data "+sim.format(time_tb2));
			}
			
			
			
			
			
			
		
		}catch (IOException e){
			e.printStackTrace();
			System.out.println("Esecuzione fallita!!");
		} finally{
			try{
				if( fos != null)
					fos.close();
				client.disconnect();
				
			}catch (IOException e){
				e.printStackTrace();
				System.out.println("Problemi di chiusura!!");
			}
		}
	}

}
