package fuzzy3;

public class RoomModel {
	private static final double StartingTemperature = 24.0;

	/* daca diferenta dintre apa incalzita si temperatura camerei este 1C atunci temperature camerei va urca cu <heaterConstant> in fiecare minut*/
	private static final double heaterConstant = 0.01;

	/* daca diferenta dintre temperature de afara si temperature camerei este 1C atunci temperature camerei va urca constant cu <wallConstant> in fiecare minut   */
	private static final double wallConstant = 0.00055;

	/*daca diferenta dintre temperature de afara si temperature camerei este de 1C atunci geamul este deschis si temperature va scadea cu <windowConstant> in fiecare minut   */
	private static final double windowConstant = 0.01;
	double currentTemaprature;

	public RoomModel() { currentTemaprature = StartingTemperature;  }

	public void updateModel(boolean heatingOn, double heaterWaterTemp, boolean windowOpen, double outSideTemp) {
		double delatHeater = (heatingOn) ? (heaterWaterTemp - currentTemaprature) : 0.0;
		double outsideDelta = currentTemaprature - outSideTemp;
		currentTemaprature += delatHeater * heaterConstant - outsideDelta * wallConstant -
				((windowOpen) ? (outsideDelta * windowConstant) : 0.0);  }

	public double getCurrentTemperature() {
		return currentTemaprature;  }
}