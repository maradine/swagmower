import com.dropbox.core.*;
import java.io.*;
import java.util.Locale;
import java.util.ArrayList;

class DropboxHelper {

	public static int getGameDate(String appKey, String appSecret, String accessToken, String gamePath, String gamePrefix) throws DbxException {
		
		DbxAppInfo appInfo = new DbxAppInfo(appKey, appSecret);
		DbxRequestConfig config = new DbxRequestConfig("Swagmower/1.0", Locale.getDefault().toString());
		DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
		DbxClient client = new DbxClient(config, accessToken);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			DbxEntry.File downloadedFile = client.getFile(gamePath+gamePrefix+".hst", null, outputStream);
		} catch (DbxException de) {
			System.out.println("I tried to get a file from Dropbox but failed: "+de);
			throw de;
		} catch (IOException ioe) {
			System.out.println("I couldn't write to my output stream, which is a memory representation that I myself instantiated, so, you know, fuck it.");
		}
		
		byte[] buf = outputStream.toByteArray();
		int year = buf[12] & 0xFF;
		return year;
	}
	
	public static int getTurnsOutstanding(String appKey, String appSecret, String accessToken, String gamePath, String gamePrefix) throws DbxException {
		
		DbxAppInfo appInfo = new DbxAppInfo(appKey, appSecret);
		DbxRequestConfig config = new DbxRequestConfig("Swagmower/1.0", Locale.getDefault().toString());
		DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
		DbxClient client = new DbxClient(config, accessToken);

		DbxEntry.WithChildren listing = client.getMetadataWithChildren(gamePath);
        
		ArrayList<String> fileNames = new ArrayList<String>();

		// get all entries that are a file and match the prefix, put in list
		for (DbxEntry child : listing.children) {
			if (child.isFile()) {
				DbxEntry.File file = child.asFile();
			
				String name = file.name.toLowerCase();
				if (name.startsWith(gamePrefix.toLowerCase())){
					fileNames.add(name);
				}
	
			}
	        }

		ArrayList<String> justX = new ArrayList<String>();
		ArrayList<String> justM = new ArrayList<String>();
		for (String name: fileNames) {
			int prefixSize = gamePrefix.length();
			int offset = prefixSize+1;
			//dont catch the xy file
			if (name.substring(offset,offset+1).equals("x") && !name.substring(offset+1,offset+2).equals("y")) {
				justX.add(name);
			}
			if (name.substring(offset,offset+1).equals("m")) {
				justM.add(name);
			}
		}

		return justM.size()-justX.size();

	}

}
