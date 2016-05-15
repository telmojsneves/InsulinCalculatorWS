package pt.server;

import javax.jws.WebService;

@WebService(endpointInterface = "pt.server.InsulinInterface")

public class InsulinDoseCalculatorImpl implements InsulinInterface{
	
	
	

	/**
	 * Calculates the number of insulin units needed after one meal.
	 * <p>
	 * This method takes the amount of carbohydrate in a given meal, and returns
	 * the number of units of insulin needed after that meal. The returned
	 * number of units of insulin equals the carbohydrate dose plus the high
	 * blood sugar dose, which are computed as follows.
	 * <p>
	 * The carbohydrate dose equals the total grams of carbohydrates in the meal
	 * divided by the amount of carbohydrate disposed by one unit of insulin,
	 * corrected by taking into account the personal sensitivity to insulin.
	 * This dose equals <code>carbohydrateAmount / carbohydrateToInsulinRatio /
	 * personalSensitivity x 50</code>.
	 * <p>
	 * The high blood sugar dose equals the difference between the pre-meal
	 * blood sugar level and the target blood sugar level, divided by the
	 * personal sensitivity to insulin. This equals <code>(preMealBloodSugar -
	 * targetBloodSugar) / personalSensitivity</code>. The personal sensitivity
	 * may be estimated using <code>personalSensitivityToInsulin()</code>.
	 * <p>
	 * In the special case when the target blood sugar level is greater than the
	 * pre-meal blood sugar level, the return value of this method is zero (no
	 * insulin).
	 *
	 * @param carbohydrateAmount amount of carbohydrate in the meal, in grams
	 * @param carbohydrateToInsulinRatio carbohydrate grams disposed by one unit
	 * @param preMealBloodSugar pre-meal measured blood sugar level, in mg/dl
	 * @param targetBloodSugar prescribed target blood sugar level, in mg/dl
	 * @param personalSensitivity personal sensitivity to insulin
	 * @return the mealtime units of insulin needed, or -1 in case of error
	 */
	@Override
	public int mealtimeInsulinDose(
									int carbohydrateAmount,
									int carbohydrateToInsulinRatio,
									int preMealBloodSugar,
									int targetBloodSugar,
									int personalSensitivity){

		//apply a range to this
		double carbohydrateAmountDouble = CheckAndTransform(carbohydrateAmount, 60, 120);
		double carbohydrateToInsulinRatioDouble = CheckAndTransform(carbohydrateToInsulinRatio, 10, 15);
		double preMealBloodSugarDouble = CheckAndTransform(preMealBloodSugar, 120, 250);
		double targetBloodSugarDouble = CheckAndTransform(targetBloodSugar, 80, 120);
		double personalSensitivityDouble = CheckAndTransform(personalSensitivity, 15, 100);

		double []values = {
			carbohydrateAmountDouble,
			carbohydrateToInsulinRatioDouble,
			preMealBloodSugarDouble,
			targetBloodSugarDouble,
			personalSensitivityDouble
		};

		if (!isArrayCorrect(values)){
			return -1;
		}

		double highBloodSugarDose = (preMealBloodSugarDouble - targetBloodSugarDouble) / personalSensitivityDouble;

		double carbohydrateDose = ((carbohydrateAmountDouble / carbohydrateToInsulinRatioDouble) * 50) / personalSensitivityDouble;
		//is something missing here
		//if 12g drop 50mg/dl

		double insulinUnitsRequired = highBloodSugarDose + carbohydrateDose;

		return doubleToInt(insulinUnitsRequired);

	}

	/**
		 * Calculates the total number of units of insulin needed between meals.
		 * <p>
		 * The total insulin units required in one day equals <code>0.55 x body
		 * weight</code> in kilograms. This method returns 50% of that number, since
		 * the background need for insulin, between meals, is around half of the
		 * daily total.
		 *
		 * @param bodyWeight the person's weight in kilograms
		 * @return the background units of insulin needed, or -1 in case of error
	*/

	@Override
	public int backgroundInsulinDose(int bodyWeight){

		double generalApproachValue = 0.55;
		double genericValue = 0.5;

		double bodyWeightDouble = CheckAndTransform(bodyWeight, 40, 130);

		double []values = {
			bodyWeightDouble
		};

		if (!isArrayCorrect(values)){
			return -1;
		}

		double totalInsulinPerDay = bodyWeightDouble * generalApproachValue;

		double backgroundInsulinDose = totalInsulinPerDay * genericValue;

		return doubleToInt(backgroundInsulinDose);

	}


	/**
	 * Determines an individual's sensitivity to one unit of insulin.
	 * <p>
	 * One unit of insulin typically drops blood sugar by 50 mg/dl, but this
	 * value depends on each individual's sensitivity and daily physical
	 * activity. This method predicts the blood sugar drop (in mg/dl) that will
	 * result from one unit of insulin, for a given physical activity level.
	 * <p>
	 * To predict the blood sugar drop, this method accepts two arrays with
	 * K samples of (physical activity level, blood sugar drop). The two arrays
	 * must therefore have the same length. First, a simple linear regression
	 * (least squares method) is performed to compute alpha and beta. Then, the
	 * return value is <code>alpha + beta x physicalActivityLevel</code>.
	 * <p>
	 * The physical activity levels, including the ones in the array of samples,
	 * and the blood sugar drop values are non-negative integers. The return
	 * value of this method may be passed to <code>mealtimeInsulinDose()</code>
	 * as a parameter.
	 *
	 * @param physicalActivityLevel most recent activity level (the predictor)
	 * @param physicalActivitySamples K samples of past physical activity
	 * @param bloodSugarDropSamples corresponding K samples of blood sugar drop
	 * @return the blood sugar drop in mg/dl, or -1 in case of error
	 */
	@Override
	public int personalSensitivityToInsulin(int physicalActivityLevel, int[] physicalActivitySamples, int[] bloodSugarDropSamples){

		if (!isSameSizeArrays(physicalActivitySamples, bloodSugarDropSamples)){
			return -1;
		}

		if (!isArrayBetweenSize(physicalActivitySamples,1,10) || !isArrayBetweenSize(bloodSugarDropSamples,1,10)){

			return -1;

		}

		double physicalActivityLevelDouble = CheckAndTransform(physicalActivityLevel, 2, 10);

		//TODO validate each element of each array

		double []values = {
			physicalActivityLevelDouble,
		};

		if (!isArrayCorrect(values)){
			return -1;
		}

		double simpleLinearRegression[] = LinearRegression(physicalActivitySamples, bloodSugarDropSamples);

		double alpha = simpleLinearRegression[0];
		double beta = simpleLinearRegression[1];

		double individualSensitivity = alpha + beta * physicalActivityLevelDouble;

		return doubleToInt(individualSensitivity);

	}


	private boolean isArrayCorrect(double[] values){

		for (int i = 0; i < values.length ; i++){
			if (values[i] == -1){
				return false;
			}
		}

		return true;

	}

	private double CheckAndTransform(int value, int min, int max){
		if (ValidateIntData(value, min, max) == true){
			return intToDouble(value);
		}

		return intToDouble(-1);

	}

	private boolean isArrayBetweenSize(int[] array, int min, int max){

		int size = array.length;

		return ValidateIntData(size, min, max);

	}

	private boolean isSameSizeArrays(int[] a, int[] b){

		if (a.length == b.length){
			return true;
		}
		else{
			return false;
		}
	}

	private boolean ValidateIntData(int value,int min, int max){
		if (value >= min && value <= max){
			return true;
		}
		return false;

	}

	private double intToDouble(int value){
		return (double) value;
	}

	private int doubleToInt(double value){
		return (int) Math.round(value);
	}


	/**
	* http://www2.warwick.ac.uk/fac/soc/economics/staff/vetroeger/teaching/po906_week567.pdf
	* https://github.com/fracpete/princeton-java-algorithms/blob/master/src/main/java/edu/princeton/cs/algorithms/LinearRegression.java
	* Performs a linear regression on the data points <tt>(y[i], x[i])</tt>.
	* @param x the values of the predictor variable
	* @param y the corresponding values of the response variable
	*/
	private double[] LinearRegression(int[] x, int[] y) {

		int size = x.length;

		// first pass
		double sumx = 0.0, sumy = 0.0;

		for (int i = 0; i < size; i++){
			sumx  += x[i];
			sumy  += y[i];
		}

		double xbar = sumx / size;
		double ybar = sumy / size;

		double xxbar = 0.0, xybar = 0.0;
		for (int i = 0; i < size; i++) {
			xxbar += (x[i] - xbar) * (x[i] - xbar);
			xybar += (x[i] - xbar) * (y[i] - ybar);
		}
		double beta  = xybar / xxbar;
		double alpha = ybar - beta * xbar;

		double[] regressionResult = {alpha,beta};

		return regressionResult;

	}


	/*
		http://dtc.ucsf.edu/types-of-diabetes/type2/treatment-of-type-2-diabetes/medications-and-therapies/type-2-insulin-rx/calculating-insulin-dose/
		dont forget javadoc
		what to Test
			parameters income
			if they exist, or not, if they are in correct type
			test linear regression input and results
			the output is well performed


	*/

	
}
