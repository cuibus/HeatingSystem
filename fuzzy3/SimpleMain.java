package fuzzy3;

import java.util.List;

import Main.FuzzyPVizualzer;
import Main.Plotter;
import View.MainView;

public class SimpleMain {
	private static final int SIM_PERIOD = 10;

	public static void main(String[] args) {
		Scenario scenario = Scenario.winterDay();
		System.out.println("winter day");
		Plant plant = new Plant(SIM_PERIOD, scenario);
		HeaterTankControllerComponent tankController = new HeaterTankControllerComponent(plant, SIM_PERIOD);
		RoomTemperatureControllerComponent roomController = new RoomTemperatureControllerComponent(plant, SIM_PERIOD);
		//ORC orc = new ORC(tankController, SIM_PERIOD);
		
		//orc.start();
		roomController.start();
		tankController.start();
		plant.start();
		double waterRefTemp = 60.0;
		double roomTemperatureRef = 30.0;

		for (int i = 0; i < scenario.getScenarioLength(); i++) {
			tankController.setWaterRefTemp(waterRefTemp);
			tankController.setTankWaterTemp(plant.getTankWaterTemperature());
			roomController.setInput(roomTemperatureRef, plant.getRoomTemperature());
			//orc.setOutsideTemp(scenario.getOutSideTemepratue(i));
			
			try {Thread.sleep(10);
			} catch (InterruptedException e) { e.printStackTrace();	}
		}
		tankController.stop();
		roomController.stop();
		//orc.stop();

		MainView windowTankController = FuzzyPVizualzer.visualize(tankController.getNet(),
				tankController.getRecorder());
		MainView windowTermostat = FuzzyPVizualzer.visualize(roomController.getNet(), roomController.getRecorder());
		//MainView windowOrc = FuzzyPVizualzer.visualize(orc.getNet(), orc.getRecorder());
		
		Plotter plotterTemperatureLog = new Plotter(plant.getTemeartureLogs());
		Plotter plotterCommandLog = new Plotter(plant.getCommandLogs());
		windowTankController.addInteractivePanel("TempLogs", plotterTemperatureLog.makeInteractivePlot());
		windowTermostat.addInteractivePanel("TempLogs", plotterTemperatureLog.makeInteractivePlot());
		windowTankController.addInteractivePanel("ComandLogs", plotterCommandLog.makeInteractivePlot());
		windowTermostat.addInteractivePanel("ComandLogs", plotterCommandLog.makeInteractivePlot());

		double[] tankTempStats = SimpleMain.calcStatistics(plant.getTemeartureLogs().get("tankTemp"));
		double[] rommTempStsats = SimpleMain.calcStatistics(plant.getTemeartureLogs().get("roomTemp"));

		System.out.println("max tank temp :" + tankTempStats[0]);
		System.out.println("min tank temp :" + tankTempStats[1]);
		System.out.println("avg tank temp :" + tankTempStats[2]);
		System.out.println("max room temp :" + rommTempStsats[0]);
		System.out.println("min room temp :" + rommTempStsats[1]);
		System.out.println("avg room temp :" + rommTempStsats[2]);
		System.out.println("heater on ratio:" + plant.heatingOnRatio());
		System.out.println("max nr of mins continous heating on:" + plant.maxContiniousHeaterOn());
		System.out.println("all consunption ::" + plant.gasConsumption());
		System.out.println("avg consunption in  a min ::" + plant.gasConsumption() / scenario.getScenarioLength());	}

	public static double[] calcStatistics(List<Double> list) {
		double min = 1000.0;
		double max = 0.0;
		double sum = 0.0;
		for (Double d : list) {
			min = (min > d) ? d : min;
			max = (max < d) ? d : max;
			sum += d;		}
		return new double[] { max, min, sum / list.size() };	}
}
