/**
 * 
 */
package edu.umaine.cs.distance;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Mark Royer
 *
 */
public class Ajax {

	public static void main(String[] args) throws IOException {

		System.out.println(getRoute("Boston, MA", "New York, NY"));
		System.out.println(getRoute("Portland, OR", "Detroit, MI"));
		System.out.println(getRoute("Portland, ME", "Orono, ME"));
	}

	public static Route getRoute(String origin, String destination) throws IOException {
		// build a URL
		String s = "http://maps.googleapis.com/maps/api/distancematrix/json?origins=";
		s += encodeURLParameter(origin);
		s += "&destinations=";
		s += encodeURLParameter(destination);
		s += "&mode=driving";
		URL url = new URL(s);

		// read from the URL
		Scanner scan = new Scanner(url.openStream());
		StringBuilder str = new StringBuilder();
		while (scan.hasNext())
			str.append(scan.nextLine());
		scan.close();

		JsonElement jelement = new JsonParser().parse(str.toString());
		JsonObject obj = jelement.getAsJsonObject();

		// @formatter:off		
		// JSON should look like the following...
		//		
		//		{
		//			   "destination_addresses" : [ "New York, NY, USA" ],
		//			   "origin_addresses" : [ "Boston, MA, USA" ],
		//			   "rows" : [
		//			      {
		//			         "elements" : [
		//			            {
		//			               "distance" : {
		//			                  "text" : "347 km",
		//			                  "value" : 346658
		//			               },
		//			               "duration" : {
		//			                  "text" : "3 hours 48 mins",
		//			                  "value" : 13692
		//			               },
		//			               "status" : "OK"
		//			            }
		//			         ]
		//			      }
		//			   ],
		//			   "status" : "OK"
		//			}
		//@formatter:on


		String status = obj.get("status").getAsString();
		
		if (!status.equals("OK")) {
			System.err.println("Not OK " + origin);
			return new Route(origin, destination, 0,0, status);
		}
		
		String orig = obj.getAsJsonArray("origin_addresses").get(0).getAsString();
		String dest = obj.getAsJsonArray("destination_addresses").get(0).getAsString();
		long distance = 0;
		long duration = 0;

		JsonObject disDur = obj.getAsJsonArray("rows").get(0).getAsJsonObject().getAsJsonArray("elements").get(0)
				.getAsJsonObject();

		if (disDur.get("status").toString().equals("\"OK\"")) {
			distance = disDur.getAsJsonObject("distance").get("value").getAsLong();
			duration = disDur.getAsJsonObject("duration").get("value").getAsLong();
		} else {
			status = disDur.get("status").toString();
			System.err.println("Unable to find route for " + origin + " status is " + disDur.get("status").toString());
			System.err.println(obj);
		}

		return new Route(orig, dest, distance, duration, status);

	}

	public static String encodeURLParameter(String t) throws UnsupportedEncodingException {
		// Javascript doesn't decode + symbols.
		return URLEncoder.encode(t, "UTF-8").replace("+", "%20");
	}
}
