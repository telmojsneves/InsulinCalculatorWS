package server;

import server.InsulinWS;
import javax.xml.ws.Endpoint;


public class InsulinEndpoint {
	public static void main(String[] args) {
		InsulinWS insulinWS = new InsulinWS();
		Endpoint endpoint = Endpoint.publish("http://localhost:8081/insulin", insulinWS);
	}
}
