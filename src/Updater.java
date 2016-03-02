import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

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
	public static DbxClient client;
	
	DbxEntry latest_version;
	String current_string;
	UpdaterWindow window;
	
	public static void main(String[] args) throws IOException, DbxException {
		new Updater();
	}
	
	public Updater() throws IOException, DbxException {
		
		DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);

        DbxRequestConfig config = new DbxRequestConfig(
            "JavaTutorial/1.0", Locale.getDefault().toString());
        DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
        
        client = new DbxClient(config, accessToken);
        System.out.println("Linked account: " + client.getAccountInfo().displayName);
        
        DbxEntry.WithChildren listing = client.getMetadataWithChildren("/");
        System.out.println("Files in the root path:");
        
        latest_version = null;
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
        current_string = "";
        
        try {
            String line = br.readLine();
            
            String string = line.substring(line.indexOf(":")+1, line.length()); 
            string = string.replace(" ", "");
            current_string = string;
            string = string.replace(".", "");
            
            current_int = Integer.parseInt(string);
            
        } finally {
            br.close();
        }
        
        //setVersion("0.0.0");
        
        //see if new version is greater than current
        //if(latest_int > current_int) {
        if(latest_int > current_int) {
        	
        	Updater up = this;
        	
        	Thread w = new Thread(new Runnable() {
				@Override
				public void run() {
					window.createWindow(up, latest_version.name, current_string);
				}
			});
        	
        	w.start();
        }
        
        //open normal program
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
	
	
	
}
