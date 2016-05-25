package server;

import server.InsulinDoseCalculator;
import javax.xml.ws.Endpoint;


public class InsulinEndpoint {
	public static void main(String[] args) {
		InsulinDoseCalculator insulinDoseCalculator = new InsulinDoseCalculator();
		Endpoint endpoint = Endpoint.publish("http://localhost:8081/InsulinDoseCalculator", insulinDoseCalculator);
	}
}
