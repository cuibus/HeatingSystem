package fuzzy3;

import core.TableParser;
import core.FuzzyPetriLogic.FuzzyDriver;
import core.FuzzyPetriLogic.Executor.AsyncronRunnableExecutor;
import core.FuzzyPetriLogic.PetriNet.FuzzyPetriNet;
import core.FuzzyPetriLogic.PetriNet.Recorders.FullRecorder;
import core.FuzzyPetriLogic.FuzzyToken;
import core.FuzzyPetriLogic.Tables.OneXOneTable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ORC {
	//	componenta ORC

	String orcTable = ""//
			// x1 \ x2
			//x2 = NL     NM     ZR      PM     PL
			+ "{[<ZR,PL><ZR,PM><ZR,ZR><ZR,NM><ZR,NL>]" //
			+ " [<ZR,PL><ZR,PM><ZR,ZR><ZR,NM><ZR,NL>]" //
			+ " [<ZR,PL><ZR,PM><ZR,ZR><ZR,NM><ZR,NL>]"//
			+ " [<ZR,PL><ZR,PM><ZR,ZR><ZR,NM><ZR,NL>]"//
			+ " [<ZR,PL><ZR,PM><ZR,ZR><ZR,NM><ZR,NL>]}";

	private AsyncronRunnableExecutor execcutor;
	private FullRecorder rec;
	private FuzzyDriver outsideTemperatureDriver = FuzzyDriver.createDriverFromMinMax(-30,  30);
	private int p1;
	private FuzzyPetriNet net;

	public ORC(HeaterTankControllerComponent HTC, long simPeriod) {
		// se construieste reteaua Petri pentru ORC component
		TableParser parser = new TableParser();
		net = new FuzzyPetriNet();
		
		int p0 = net.addPlace();
		p1 = net.addInputPlace();
		int p2 = net.addPlace();
		int p3 = net.addPlace();
		
		int t0 = net.addTransition(0, parser.parseTwoXTwoTable(orcTable));
		int t1 = net.addTransition(1,  OneXOneTable.defaultTable());
		int t2 = net.addOuputTransition(OneXOneTable.defaultTable());
		
		net.setInitialMarkingForPlace(p0, FuzzyToken.zeroToken());
		net.addArcFromPlaceToTransition(p0, t0, 1);
		net.addArcFromPlaceToTransition(p1, t0, 1);
		net.addArcFromTransitionToPlace(t0, p2);
		net.addArcFromTransitionToPlace(t0, p3);
		net.addArcFromPlaceToTransition(p2, t1, 1);
		net.addArcFromPlaceToTransition(p3, t2, 1);
		net.addArcFromTransitionToPlace(t1, p0);
		
		
		//se specifica limitele pentru fuzzyficare
		FuzzyDriver waterTempDriver = FuzzyDriver.createDriverFromMinMax(-75, 75);
		rec = new FullRecorder();
		//se creaza executorul
		execcutor = new AsyncronRunnableExecutor(net, simPeriod);
		execcutor.setRecorder(rec);
		//se adauga o actiune tranzitiei de iesire t2 – comanda pentru gaz
		net.addActionForOuputTransition(t2, new Consumer<FuzzyToken>() {
			@Override
			public void accept(FuzzyToken tk) {
				// set waterTempRef for HTC
				HTC.setWaterRefTemp(waterTempDriver.defuzzify(tk));
				// plant.setHeaterGasCmd(tankCommandDriver.defuzzify(tk));
			}
		});
	}
	public void start() {    (new Thread(execcutor)).start();  }

	public void stop() {    execcutor.stop();  }

	public void setOutsideTemp(double outsideTemp){

		Map<Integer, FuzzyToken> inps = new HashMap<Integer, FuzzyToken>();
		inps.put(p1, outsideTemperatureDriver.fuzzifie(outsideTemp));
		execcutor.putTokenInInputPlace(inps);  }
	//metode pentru vizualizarea retelei Petri
	public FuzzyPetriNet getNet() {    return net;  }

	public FullRecorder getRecorder() {    return rec;  }
}
