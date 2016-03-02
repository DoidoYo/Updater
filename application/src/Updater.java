import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Locale;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;

public class Updater {

	private static final String APP_KEY = "tcp2wrmvnh7krkl";  
	private static final String APP_SECRET = "026il9q5zlwvh31";  
	private static final String accessToken = "Kekqp91K7bAAAAAAAAAADOvvtOtAz5J4R0kfWky-YEJSnfzIxyJX9pEyDGkvIXw1";
	
	File configFile;
	DbxClient client;
	
	public Updater() throws IOException, DbxException {
		
		DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);

        DbxRequestConfig config = new DbxRequestConfig(
            "JavaTutorial/1.0", Locale.getDefault().toString());
        DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
        
        client = new DbxClient(config, accessToken);
        System.out.println("Linked account: " + client.getAccountInfo().displayName);
        
        DbxEntry.WithChildren listing = client.getMetadataWithChildren("/");
        System.out.println("Files in the root path:");
        
        DbxEntry latest_version = null;
        int latest_int = 0;
        
        for (DbxEntry child : listing.children) {
            System.out.println("	" + child.name + ": " + child.toString());
            String s = child.name.replace(".", "");
            
            int i = Integer.parseInt(s);
            
            if(i > latest_int) {
            	latest_version = child;
            	latest_int = i;
            }
        }
        
        //read current version
        configFile = new File("config");
        if(!configFile.exists()) {
    		configFile.createNewFile();
    		BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
    		writer.write("version: 0.0.0");
    		writer.flush();
    		writer.close();
    	}
        BufferedReader br = new BufferedReader(new FileReader("config"));
        
        int current_int = 0;
        
        try {
            String line = br.readLine();
            
            String current_string = line.substring(line.indexOf(":")+1, line.length()); 
            current_string = current_string.replace(".", "");
            current_string = current_string.replace(" ", "");
            
            current_int = Integer.parseInt(current_string);
            
        } finally {
            br.close();
        }
        
        //see if new version is greater than current
        /*if(latest_int > current_int) {
        	System.out.println("NEW VERSION FOUND!");
        	
        	//GUI For new version download
        	
        	//change config version
        	setVersion(latest_version.name);
        	
        	//download files 
        	deleteDirectory(new File("application"));
        	total_files = countFilesInDropBoxDir("/" + latest_version.name);
        	
        	System.out.println(total_files);
        	
        	downloadFolder("/" + latest_version.name, "application");
        }*/
        
        deleteDirectory(new File("application"));
    	total_files = countFilesInDropBoxDir("/" + latest_version.name);
    	
    	System.out.println(total_files);
    	
    	downloadFolder("/" + latest_version.name, "application");
        
        
	}
	
	public void setVersion(String v) throws IOException {
		//change config version
    	File tempConfigFile = new File("tempConfig");
    	
    	BufferedReader reader = new BufferedReader(new FileReader(configFile));
    	BufferedWriter writer = new BufferedWriter(new FileWriter(tempConfigFile));
    	
    	String currentLine;
    	
    	while((currentLine = reader.readLine()) != null) {
    	    //System.out.println(currentLine);
    	    String trimmedLine = currentLine.trim();
    	    if(trimmedLine.contains("version")) {
    	    	writer.write("version: " + v + System.getProperty("line.separator"));
    	    } else {
    	    	writer.write(currentLine + System.getProperty("line.separator"));
    	    }
    	}
    	
    	writer.flush();
    	writer.close(); 
    	reader.close(); 
    	configFile.delete();
    	tempConfigFile.renameTo(new File("config"));
	}
	
	int total_files = 0;
	int progress_files = 0;
	
	public int countFilesInDropBoxDir(String path) {
		int i = 0;
		try {
	        for (DbxEntry child : client.getMetadataWithChildren(path).children) {
	            if (child instanceof DbxEntry.Folder) {
	                // recurse
	            	countFilesInDropBoxDir(path + "/" + child.name);
	            } else if (child instanceof DbxEntry.File) {
	            	i++;
	            }
	        }
	    } catch (DbxException e) {
	        System.out.println(e.getMessage());
	    }
		return i;
	}
	
	public void downloadFolder(String path, String destination) {
		new File(destination).mkdirs();
	    try {
	        for (DbxEntry child : client.getMetadataWithChildren(path).children) {
	            if (child instanceof DbxEntry.Folder) {
	                // recurse
	                downloadFolder(path + "/" + child.name, destination + "/" + child.name);
	            } else if (child instanceof DbxEntry.File) {
	                // download an individual file
	                OutputStream outputStream = new FileOutputStream(
	                    destination + "/" + child.name);
	                try {
	                    DbxEntry.File downloadedFile = client.getFile(
	                        path + "/" + child.name, null, outputStream);
	                } finally {
	                    outputStream.close();
	                }
	            }
	        }
	    } catch (DbxException e) {
	        System.out.println(e.getMessage());
	    } catch (IOException e) {
	        System.out.println(e.getMessage());
	    }
	}
	
	public static boolean deleteDirectory(File directory) {
	    if(directory.exists()){
	        File[] files = directory.listFiles();
	        if(null!=files){
	            for(int i=0; i<files.length; i++) {
	                if(files[i].isDirectory()) {
	                    deleteDirectory(files[i]);
	                }
	                else {
	                    files[i].delete();
	                }
	            }
	        }
	    }
	    return(directory.delete());
	}
	
	public static void main(String[] args) throws IOException, DbxException {
		new Updater();
	}
	
}
