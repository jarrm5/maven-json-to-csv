package com.stord;

import java.net.URL;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import org.json.JSONObject;
import com.github.opendevl.JFlat;

public class App {
	
	public static final String urlStr = "https://api.yelp.com/v3/businesses/search?term=Taqueria%20Del%20Sol&location=Atlanta&limit=1";
	
	public static void main(String args[]) {
		try {
			//capture Yelp API key passed as command line argument to the program 
			String APIkey = args[0];
			
			//Set url and http connection configurations
			URL url = new URL(urlStr);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", "Bearer " + APIkey);
			connection.setConnectTimeout(5000);
			
			//connect and check the response code; only proceed with 200 OK code
			connection.connect();
			
			int responseCode = connection.getResponseCode();
			
			if(responseCode == 200) {
				
				System.out.println("HTTP GET returned 200");
				
				String line = "";
				
				//Read the response into StringBuilder object
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				
		        StringBuilder response = new StringBuilder();
		        
		        while ((line = in.readLine()) != null) {
		           response.append(line);
		        }
		        
		        //Convert the response to org.JSONObject, drill down to the "business" node 
		        JSONObject jsonObject = new JSONObject(response.toString());
		        
		        JFlat flatMe = new JFlat(jsonObject.get("businesses").toString());

		        //Get the 2D representation of JSON document
		        List<Object[]> json2csv = flatMe.json2Sheet().getJsonAsSheet();
		      
		        //JFlat returns the csv headers using full json path (i.e. categories/title)
		        //Strip off the path before the last '/' to clean up the header row
		        for (int i = 0; i < json2csv.get(0).length; i++) {
		        	String header = json2csv.get(0)[i].toString();
		        	json2csv.get(0)[i] = header.substring(header.lastIndexOf('/')+1);
		        }
		      
		        //Write the 2D representation in csv format
		        //Unfortunately the data is out of order because of the way JSONObject is implemented in Java
		        flatMe.write2csv("result.csv");
		        System.out.println("output written to result.csv");
			}
			else {
				throw new RuntimeException("Response code returned " + responseCode);
			}
			connection.disconnect();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
