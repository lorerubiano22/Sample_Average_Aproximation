
// Warming the sequence of jobs_ el problema aca es que estoy confundiendo la sequencia de los trabajo con la posición de cada trabajo
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import com.dashoptimization.XPRB;
import com.dashoptimization.XPRBexpr;
import com.dashoptimization.XPRBprob;
import com.dashoptimization.XPRBvar;

/**
 * @author Lorena
 *
 */
/**
 * @author Lorena
 *
 */
public class ExactAllocation {
	int n = 0; // total of jobs
	public LinkedList<Jobs> jobsList;
	public LinkedList<Jobs> proposedSequence;
	private Test test;
	private Inputs input;
	private LinkedList<SubRoute> walkingRoutes= new LinkedList<SubRoute>(); // list of selected walking routes
	private SubRoute subR;
	
	// slots selection
	XPRBvar[] WR;// Variable <-Binary. 1 if the slot is selected i is assigned to position j
	// [i][j]
	XPRBexpr slotJob; // Constraint: Each position is once over all slots

	// sequence
	XPRBvar[][] x;// Variable <-Binary. 1 if the job is i is assigned to position j [i][j]
	XPRBvar[] y; // Variable <- start time of service
	XPRBexpr lobj;// Objective function
	XPRBexpr job;// Constraint: Each job has a position
	XPRBexpr position; // Constraint: Each position has one job
	XPRBexpr job2job; // Constraint: Maximum travel time between two jobs
	XPRBexpr maxRoute; // Constraint: Maximum route length
	XPRBexpr timeWindow; // Constraint: Time window
	XPRB bcl;
	XPRBprob p;

	public ExactAllocation(Test t, Inputs inp) {
		test = t;
		input = inp;
		bcl = new XPRB();
		p = bcl.newProb("Schedulling");
	}


	/*
	 * Model to select slots
	 */
	public void selectionWalkingRoutes(LinkedList<SubRoute> jobSlots) {
		try
		  {
		
		bcl = new XPRB();
		p = bcl.newProb("Slots selection");

		LinkedList<Jobs> dummy = testing(jobSlots);

		// 0. Input data
		int[][] coverage = new int[jobSlots.size()][dummy.size()];

		generationCoverageMatrix(jobSlots, coverage); // coverage matrix slot vs jobs

		int[] jobsList = new int[dummy.size()]; // vector of job to in the slots
		jobsInWalkingRoutes(jobsList, dummy);

		// 1. Creation of variables
		creatingVariables(jobSlots);

		// 2. Objective function
		for (int i = 0; i < jobSlots.size(); i++) {
			lobj.add(WR[i].mul(jobSlots.get(i).getDurationWalkingRoute()));
			p.setObj(lobj);
		}

		// 3: Constraint
		// all jobs have to be once in the set of slots selected
		for (int j = 0; j < dummy.size(); j++) {
			slotJob = new XPRBexpr();
			for (int i = 0; i < jobSlots.size(); i++) {
				if(coverage[i][j]==1) {
				slotJob.add(WR[i].mul(1));}
			}
		p.newCtr("slot", slotJob.eql(jobsList[j]));

		}
		
		
		
		p.print();          /* Problem formulation */
		  p.exportProb(XPRB.MPS,"slot"); /* Output the matrix in MPS format */
		  p.exportProb(XPRB.LP,"slot");  /* Output the matrix in LP format */
		p.setSense(XPRB.MINIM); /* Choose the sense of the optimization */
		//p.setSense(XPRB.MAXIM); /* Choose the sense of the optimization */
		p.mipOptimize(""); /* Solve the MIP-problem */
		
		System.out.println("Objective: " + p.getObjVal()); /* Get objective value */

		if ((p.getMIPStat() == XPRB.MIP_SOLUTION) || (p.getMIPStat() == XPRB.MIP_OPTIMAL)) {
			System.out.println("Model solved: " + p.getObjVal());
			/* Print out the solutions found */
			for (int i = 0; i < jobSlots.size(); i++) {
				if (WR[i].getSol() > 0) {
					System.out.print(WR[i].getName() + ":" + WR[i].getSol() + " ");
					subR= new SubRoute();
					int jobPosition=0;
					for(Jobs j:jobSlots.get(i).getJobSequence()) {
						subR.addJobSequence(j, jobPosition, j.getstartServiceTime());
						jobPosition++;
					}
					this.walkingRoutes.add(subR);
				}
			}
		} else {
			System.out.println("Model not solved");

		}
	}
	  catch(IOException e)
	  {
	   System.err.println(e.getMessage());
	   System.exit(1);
	  }
	}

	private LinkedList<Jobs> testing(LinkedList<SubRoute> jobSlots) {
		LinkedList<Jobs> dummy = new LinkedList<Jobs>();
		HashMap<Integer,Jobs> jobsList= new HashMap<>();

		for(SubRoute slot:jobSlots) {
			for(Jobs j:slot.getJobSequence()) {
				jobsList.put(j.getId(), j);
			}
		}
		for(Jobs j:jobsList.values()) {
			dummy.add(j);
		}
		return dummy;
	}

	private void jobsInWalkingRoutes(int[] jobsList, LinkedList<Jobs> dummy) {
		for (int i = 0; i < dummy.size(); i++) {
			jobsList[dummy.get(i).getId()-1] = 1;
		}
	}

	private void generationCoverageMatrix(LinkedList<SubRoute> jobSlots, int[][] coverage) {
		for (SubRoute slot : jobSlots) {
			for (Jobs j : slot.getJobSequence()) {
				coverage[slot.getSlotID()][j.getId()-1] = 1;
			}
		}

	}

	private void creatingVariables(LinkedList<SubRoute> jobSlots) {
		WR = new XPRBvar[jobSlots.size()];
		for (int i = 0; i < jobSlots.size(); i++) {
			WR[i] = p.newVar("WR_(" + i + ")", XPRB.BV);
		}
		lobj = new XPRBexpr();
	}

	
	// getters
	
	public LinkedList<SubRoute> getWalkingRoutes() {return walkingRoutes;}
}
