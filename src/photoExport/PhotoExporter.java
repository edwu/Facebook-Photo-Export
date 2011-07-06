package photoExport;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Code written in March 2010.
 * If there are lots of images, it may take a long time. 
 * Could just download each photo as it's being pulled instead
 * of storing all the destinations, then downloading.
 * @author Edward Wu
 *
 */

public class PhotoExporter {
	
	private static String limit = "5000";
	private static String host = "https://graph.facebook.com/";

	static class User{
		protected String fb_id;
		protected String fb_access_token;

		public User(String fb_id, String fb_access_token){
			this.fb_id = fb_id;
			this.fb_access_token = fb_access_token;
		}
	}
	
	public static JSONObject getJSON(User user) throws HttpException, IOException, JSONException{
		HttpClient client = new HttpClient();
		String encoded = URLEncoder.encode(user.fb_access_token,"UTF-8");
		String url = host+user.fb_id+"/photos?"+"limit="+limit+"&access_token="+encoded;
		GetMethod method = new GetMethod(url);
		int returnCode = client.executeMethod(method);
		if(returnCode == HttpStatus.SC_NOT_IMPLEMENTED){
			System.err.println("Does not support a get request");
			System.exit(0);
		}
		JSONObject jsonObject = new JSONObject(method.getResponseBodyAsString());
		return jsonObject;
	}
	public static ArrayList<String> retrievePhotos (JSONObject json) throws JSONException{
		ArrayList<String> photos = new ArrayList<String>();
		JSONArray data = (JSONArray) json.get("data");
		for (int i = 0 ; i<data.length(); i++){
			JSONObject record = (JSONObject)data.get(i);
			String location = (String) record.get("source");
			photos.add(location);
		}
		return photos;
	}
	private static void downloadAllImages(ArrayList<String> allPhotos) throws IOException{
		for (int i = 0; i<= allPhotos.size();i++){
			String path = allPhotos.get(i);
			BufferedImage image = downloadImage(path);
			String file = "images/image" + i + ".jpg"; 
			saveImage(image,new File(file));
		}
	}
	 private static BufferedImage downloadImage(String path) { 
	        BufferedImage image = null;
	        try {
	            URL url = new URL(path);
	            image = ImageIO.read(url);
	        } catch (IOException ex) {
	            System.out.println("can not read : " + path);
	            System.out.println(ex.getMessage());
	        } 
	        
	        return image;
	    }
	 
	public static void saveImage(BufferedImage image, File file) throws IOException{
		ImageIO.write(image, "jpg", file);
	}
	public static void main(String[] args){
		User edward = new User("your Fb Id","your Fb_Access_Token");
		try {
			JSONObject json = getJSON(edward);
			ArrayList<String> photos = retrievePhotos(json);
			downloadAllImages(photos);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
