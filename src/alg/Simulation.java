package alg;

import umontreal.iro.lecuyer.randvar.LognormalGen;
import umontreal.iro.lecuyer.rng.RandomStream;

public class Simulation {

	public static double[] generatingScenarios(Edge e, Test aTest) {
		double[] timesScenario= new double [aTest.getLongSim()];
		for(int i=0;i<Math.max(aTest.getLongSim(),1);i++) {
			if(Math.max(aTest.getLongSim(),1)==1) {
				timesScenario[i]=e.getMeanCosts();
			}
			else {
				double newArc =getStochasticValue(aTest.getRandomStream(),e.getMeanCosts(),aTest.getVariance());	
				if(newArc>e.getMeanCosts()) {
					timesScenario[i]=newArc;
				}
				else{
					timesScenario[i]=e.getMeanCosts();
				}
			}
		}	
		return timesScenario;
	}

	public static double getStochasticValue(RandomStream stream, double mean, float variance) {
		double squareSigma = Math.log(1 + (variance / Math.pow(mean, 2)));
		double mu = Math.log(mean) - squareSigma / 2;
		double sigma = Math.sqrt(squareSigma);
		return LognormalGen.nextDouble(stream, mu, sigma);
	}




}
