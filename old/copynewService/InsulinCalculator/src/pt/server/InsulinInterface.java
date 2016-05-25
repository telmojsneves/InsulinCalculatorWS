package pt.server;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public interface InsulinInterface {

	
	@WebMethod 
	int mealtimeInsulinDose(int carbohydrateAmount,
										int carbohydrateToInsulinRatio,
										int preMealBloodSugar,
										int targetBloodSugar,
										int personalSensitivity);
	
	@WebMethod
	int backgroundInsulinDose(int bodyWeight);
	
	@WebMethod
	int personalSensitivityToInsulin(int physicalActivityLevel, int[] physicalActivitySamples, int[] bloodSugarDropSamples);
	

	
}
