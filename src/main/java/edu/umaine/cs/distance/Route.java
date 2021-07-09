/**
 * 
 */
package edu.umaine.cs.distance;

import java.io.Serializable;

/**
 * @author Mark Royer
 *
 */
public class Route  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String origin;

	String destination;

	long distance; // meters

	long duration; // seconds

	String status; // OK | ZERO_RESULTS

	public Route(String origin, String destination, long distance, long duration, String status) {
		super();
		this.origin = origin;
		this.destination = destination;
		this.distance = distance;
		this.duration = duration;
		this.status = status;
	}

	public String getOrigin() {
		return origin;
	}

	public String getDestination() {
		return destination;
	}

	public long getDistance() {
		return distance;
	}

	public long getDuration() {
		return duration;
	}

	public String getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return String.format("{ origin: \"%s\", destination: \"%s\", distance: %d, duration: %d, status: \"%s\" }",
				origin, destination, distance, duration, status);
	}

}
