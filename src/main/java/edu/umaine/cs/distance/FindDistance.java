/**
 * 
 */
package edu.umaine.cs.distance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Find the distances of vehicle locations in a CSV file to a single location on
 * the map. This program makes use of the Google Maps API, so it requires a
 * network connection to run properly.
 * 
 * @author Mark Royer
 *
 */
public class FindDistance {

	static Map<String, Route> cachedInfo;

	/**
	 * Usage: java edu.umaine.cs.distance.FindDistance input_file.csv 'City, State
	 * Abbreviation'
	 * 
	 * For example,
	 * 
	 * java edu.umaine.cs.FindDistance exampleInput.csv 'Orono, ME'
	 * 
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws Exception {

		if (args.length < 3) {
			System.err.println(
					"Usage: java edu.umaine.cs.FindDistance input_file.csv 'City, State Abbreviation' YOUR_API_KEY");
			System.err.println();
			System.err.println("For example,");
			System.err.println();
			System.err.println("java edu.umaine.cs.FindDistance exampleInput.csv \"Orono, ME\" YOUR_API_KEY");
			System.exit(-1);
		}

		final String INPUT_FILE_NAME = args[0];
		final String CITY_STATE = args[1];
		final String API_KEY = args[2];
		final String CACHE_FILE_NAME = String.format("%s-%s-cachedLocations.json", extractCity(CITY_STATE),
				extractState(CITY_STATE));
		File outputFile = new File(INPUT_FILE_NAME.replace(".csv", "Out.csv"));
		File cachedFile = new File(CACHE_FILE_NAME);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		if (cachedFile.exists()) {

			System.out.printf("Using cached locations file '%s'.\n", cachedFile.getAbsolutePath());

			BufferedReader br = new BufferedReader(new FileReader(cachedFile));

			Type mapType = new TypeToken<Map<String, Route>>() {
			}.getType();

			// convert the JSON string back into an object
			cachedInfo = gson.fromJson(br, mapType);

		} else {

			System.out.printf("Cached locations file was not found.  Creating new file and saving at '%s'.\n",
					cachedFile.getAbsolutePath());

			cachedInfo = new HashMap<>();
		}

		List<Row> staff = readCSVFile(INPUT_FILE_NAME);

		PrintStream out = new PrintStream(outputFile);

		writeHeader(out);

		try {

			for (Row row : staff) {

				Route r = cachedInfo.get(row.city.toLowerCase());
				if (r == null) {
					r = Ajax.getRoute(row.city.toLowerCase() + "," + row.state, CITY_STATE, API_KEY);
					cachedInfo.put(row.city.toLowerCase(), r);
					Thread.sleep(80); // Slow and steady...
				}
				writeRow(out, row, r);
//				writeRow(System.out, row, r);

			}

			out.flush();
			out.close();

		} finally { // Write out whatever we have to the cache no matter what

			FileWriter cacheOut = new FileWriter(cachedFile);

			cacheOut.write(gson.toJson(cachedInfo));

			cacheOut.flush();
			cacheOut.close();

		}

		System.out.printf("Finished finding distances for %d locations.  Output is in the file at '%s'.\n",
				staff.size(), outputFile.getAbsolutePath());
	}

	private static String extractState(String cityState) {

		String[] parts = cityState.split(",");

		if (parts.length != 2) {
			throw new RuntimeException(
					"Expected a single comma in the city state specificaion. For example, \"Orono, ME\"");
		}
		
		String result = parts[1].trim().toLowerCase();
		
		if (result.matches(".*\\s+.*")) {
			throw new RuntimeException("State should not contain spaces.  Should be two letter code like ME.");
		}
		if (result.length() != 2) {
			throw new RuntimeException(
					"Expected a two letter state code like ME. ");
		}

		return result;
	}

	/**
	 * @param cityState Comma separated city state. For example, "Orono, ME"
	 * @return The city portion of the string in lower case. If the string contains
	 *         whitespace, that white space is replaced with underscore characters.
	 *         For example, "Old Town, ME" becomes "old_town".
	 */
	private static String extractCity(String cityState) {

		String[] parts = cityState.split(",");

		if (parts.length != 2) {
			throw new RuntimeException(
					"Expected a single comma in the city state specificaion. For example, \"Orono, ME\"");
		}

		return parts[0].trim().replaceAll("\\s", "_").toLowerCase();
	}

	private static void writeHeader(PrintStream out) {
		out.print("id");
		out.print(",");
		out.print("Make");
		out.print(",");
		out.print("Model");
		out.print(",");
		out.print("Plate");
		out.print(",");
		out.print("Year");
		out.print(",");
		out.print("City");
		out.print(",");
		out.print("State");
		out.print(",");
		out.print("Distance (m)");
		out.print(",");
		out.print("Time (s)");
		out.print(",");
		out.print("Origin");
		out.print(",");
		out.print("Destination");
		out.print(",");
		out.print("Status");
		out.println();
	}

	private static void writeRow(PrintStream out, Row row, Route r) {
		out.print(row.id);
		out.print(",");
		out.print(row.description1);
		out.print(",");
		out.print(row.description2);
		out.print(",");
		out.print(row.plate);
		out.print(",");
		out.print(row.year);
		out.print(",");
		out.print(row.city);
		out.print(",");
		out.print(row.state);
		out.print(",");
		out.print(r.distance);
		out.print(",");
		out.print(r.duration);
		out.print(",");
		out.print(quote(r.origin));
		out.print(",");
		out.print(quote(r.destination));
		out.print(",");
		out.print(r.status);
		out.println();
	}

	private static String quote(String str) {
		return "\"" + str.replace("\"", "\\\"") + "\"";
	}

	static List<Row> readCSVFile(String fileName) throws FileNotFoundException {

		List<Row> result = new ArrayList<Row>();

		File file = new File(fileName);

		Scanner scan = new Scanner(file);

		scan.nextLine();
		scan.useDelimiter(",");
		while (scan.hasNextLine()) {
			String id = scan.next();
			String d1 = scan.next();
			String d2 = scan.next();
			String plate = scan.next();
			String year = scan.next();
			String city = scan.next();
			String state = scan.nextLine().substring(1); // Remove leading comma

			Row r = new Row(id, d1, d2, plate, year, city, state);

			result.add(r);
		}

		scan.close();

		return result;

	}

	static class Row {
		String id;
		String description1;
		String description2;
		String plate;
		String year;
		String city;
		String state;

		public Row(String id, String description1, String description2, String plate, String year, String city,
				String state) {
			super();
			this.id = id;
			this.description1 = description1;
			this.description2 = description2;
			this.plate = plate;
			this.year = year;
			this.city = city;
			this.state = state;
		}

		@Override
		public String toString() {
			return String.format("{id: %s, d1: %s, d2: %s, plate: %s, year: %s, city: %s, state: %s}", id, description1,
					description2, plate, year, city, state);
		}

	}
}
