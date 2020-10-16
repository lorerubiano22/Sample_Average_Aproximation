package alg;

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
public class XpressSolution {
	static int V=0; // set of vehicles
	static int K=0; // set of scenarios
	static int n=0; //set of node


	public static Solution solveMe(Inputs inp, Test t) {
		Solution solutionXpress=new Solution();
		//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
		/*SETS*/
		V=inp.getVehNumber(); // set of vehicles
		n=inp.getNodes().length; //sset of node
		//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

		/*PARAMETERS*/

		//Traveling time
		double [][]TV= new double [n][n];
		for(Edge e:inp.getEdgesDirectory().values()) {
			int indexI=e.getOrigin().getId();
			int indexJ=e.getEnd().getId();
			double tv=e.getCosts();
			TV[indexI][indexJ]=tv;
		}


		//Rewards
		double []U= new double [n];
		for(int i=0; i<n;i++) {
			Node IDnode=inp.getNodes()[i];
			U[i]=IDnode.getProfit();
		}

		try
		{ 
			/*START MODE*/
			XPRB bcl= new XPRB();


			/*VARIABLES*/

			XPRBvar[][][] X =new XPRBvar[n][n][V]; // X(i,j,v)<-Binary variable 1 if the drone. 0 otherwise
			XPRBvar[][] W=new XPRBvar[n][V]; // w(i,v)<-Integer. Auxiliar variable to remove subtour
			XPRBvar[][] Lenght=new XPRBvar[n][V];// Lenght(i,v)<- continue variable to measure the lenght of the route
			XPRBvar[] Reward=new XPRBvar[V];

			/*EQUATIONS VECTOR*/
			XPRBprob p= bcl.newProb("Slots selection");
			initializationVariables(X,W,Lenght,p);

			/*OBJECTIVE FUNCTION*/		
			XPRBexpr lobj = new XPRBexpr();
			for(int vehicle=0;vehicle<V;vehicle++) {
				for(int j=0;j<n;j++) {
					for(int i=0;i<n;i++) {		
						lobj.add(X[i][j][vehicle].mul(U[j]));
					}
				}
			}
			p.setObj(lobj);

			/*Constraints*/

			// i tiene que ser diferente j
			for(int i=0;i<n;i++) {
				for(int j=0;j<n;j++) {
					if(i==j) {
						for(int vehicle=0;vehicle<V;vehicle++) {
							XPRBexpr arcVisit = new XPRBexpr();
							arcVisit.add(X[i][j][vehicle].mul(1));
							p.newCtr("one visit",arcVisit.eql(0));
						}	
					}
				}

			}


			// en caso de que un arco sea visitado. sólo se puede pasar por el arco una vez
			for(int j=1;j<n-1;j++) {
				XPRBexpr arcVisit = new XPRBexpr();
				for(int vehicle=0;vehicle<V;vehicle++) {
					for(int i=0;i<n-1;i++) {
						arcVisit.add(X[i][j][vehicle].mul(1));
					}		
				}
				p.newCtr("one visit",arcVisit.lEql(1));
			}

			// The total of vehicles entering at the depot has to be lower than the available number of vehicles
			for(int vehicle=0;vehicle<V;vehicle++) {
				XPRBexpr enteringDepot = new XPRBexpr();
				for(int i=1;i<n;i++) {// excluding the last depot the depot
					enteringDepot.add(X[i][n-1][vehicle].mul(1));
				}
				p.newCtr("vehicle going from depot",enteringDepot.eql(1));
			}

			// The total of vehicles going out from the depot has to be lower than the available number of vehicles
			for(int vehicle=0;vehicle<V;vehicle++) {
				XPRBexpr goingOutDepot = new XPRBexpr();
				for(int j=1;j<n;j++) {// excluding the last depot the depot
					goingOutDepot.add(X[0][j][vehicle].mul(1));

				}
				p.newCtr("vehicle going from depot",goingOutDepot.eql(1));
			}

			// The total of vehicles going out from the depot close has to be lower than the available number of vehicles
			for(int vehicle=0;vehicle<V;vehicle++) {
				XPRBexpr enteringDepot = new XPRBexpr();
				for(int j=0;j<n;j++) {// excluding the last depot the depot
					enteringDepot.add(X[n-1][j][vehicle].mul(1));
				}
				p.newCtr("vehicle going from depot close",enteringDepot.eql(0));
			}


			// evitar que hayas entradas directas entre los depots
			for(int vehicle=0;vehicle<V;vehicle++) {
				XPRBexpr depotTodepotNotConnection = new XPRBexpr();
				depotTodepotNotConnection.add(X[0][n-1][vehicle].mul(1));
				p.newCtr("depot to depot no connection",depotTodepotNotConnection.eql(0));
			}

			// balance
			for(int vehicle=0;vehicle<V;vehicle++) {
				for(int o=1;o<n-1;o++) {
					XPRBexpr balanace = new XPRBexpr();
					for(int i=0;i<n-1;i++) {
						for(int j=1;j<n;j++) {
							balanace.add(X[i][o][vehicle].add(X[o][j][vehicle].mul(-1)));
						}
					}
					p.newCtr("balance",balanace.eql(0));
				}
			}


			// subtour
			//			for(int vehicle=0;vehicle<V;vehicle++) {
			//				for(int i=1;i<n-1;i++) {
			//					for(int j=1;j<n-1;j++) {
			//						XPRBexpr subtour = new XPRBexpr();
			//						subtour.add(W[i][vehicle].mul(1));
			//						subtour.add(W[j][vehicle].mul(-1));
			//						subtour.add(X[i][j][vehicle].mul(n));
			//						p.newCtr("removing sobtour",subtour.lEql(n-1));
			//					}
			//				}
			//			}

			//////////////////////////////////////////////////////////////////////////////////





			// lineas 116 a la 127


			// cada nodo tiene que visitarse sólo una vez
			//			for(int j=0;j<n-1;j++) {
			//				XPRBexpr depotTodepotNotConnection = new XPRBexpr();
			//				for(int vehicle=0;vehicle<V;vehicle++) {
			//					for(int i=0;i<n-1;i++) {
			//						depotTodepotNotConnection.add(X[i][j][vehicle].mul(1));
			//						p.newCtr("depot to depot no connection",depotTodepotNotConnection.lEql(0));
			//					}
			//				}
			//			}







			// en caso de que un arco sea visitado. sólo se puede pasar por el arco una vez
			//			for(int vehicle=0;vehicle<V;vehicle++) {
			//				for(int i=0;i<n-1;i++) {
			//					XPRBexpr arcVisit = new XPRBexpr();
			//					for(int j=1;j<n-1;j++) {
			//						arcVisit.add(X[i][j][vehicle].mul(1));
			//					}
			//					p.newCtr("one visit",arcVisit.lEql(1));
			//				}
			//			}

			// subtour
			//			for(int vehicle=0;vehicle<V;vehicle++) {
			//				for(int i=0;i<n-1;i++) {
			//					for(int j=0;j<n-1;j++) {
			//						XPRBexpr subtour = new XPRBexpr();
			//						subtour.add(W[i][vehicle].mul(1));
			//						subtour.add(W[j][vehicle].mul(-1));
			//						subtour.add(X[i][j][vehicle].mul(n));
			//						p.newCtr("removing sobtour",subtour.lEql(n-1));
			//					}
			//				}
			//			}


			// maximum driging range
			//			for(int vehicle=0;vehicle<V;vehicle++) {
			//				XPRBexpr drivingRange = new XPRBexpr();
			//				for(int i=0;i<n-1;i++) {
			//					for(int j=0;j<n-1;j++) {
			//						drivingRange.add(X[i][j][vehicle].mul(TV[i][j]));
			//						p.newCtr("driving range",drivingRange.lEql(inp.gettMax()));
			//					}
			//				}
			//			}





			// Extracting information

			//p.print();          /* Problem formulation */
			p.exportProb(XPRB.MPS,"slot"); /* Output the matrix in MPS format */
			p.exportProb(XPRB.LP,"slot");  /* Output the matrix in LP format */
			p.setSense(XPRB.MAXIM); /* Choose the sense of the optimization */
			//p.setSense(XPRB.MAXIM); /* Choose the sense of the optimization */
			p.mipOptimize(""); /* Solve the MIP-problem */
			System.out.println("Objective: " + p.getObjVal()); /* Get objective value */
			System.out.println("Objective: " + p.getObjVal()); /* Get objective value */
			if ((p.getMIPStat() == XPRB.MIP_SOLUTION) || (p.getMIPStat() == XPRB.MIP_OPTIMAL)) {
				printingModel(n,V,p,X);
				System.out.println("Stop "); /* Get objective value */
			}
			else {
				System.out.println("Model not solved");

			}

		}
		catch(IOException e)
		{
			System.err.println(e.getMessage());
			System.exit(1);
		}
		return solutionXpress;
	}




	/////////

	private static void printingModel(int nodesAmount, int vehicleAmount, XPRBprob p, XPRBvar[][][] X) {
		System.out.println("Model solved: " + p.getObjVal());
		/* Print out the solutions found */

		for (int vehicle = 0;vehicle < vehicleAmount; vehicle++) {
			for (int i = 0; i < n; i++) {
				for (int j = 0;j < n; j++) {
					if (X[i][j][vehicle].getSol() > 0) {
						System.out.print(X[i][j][vehicle].getName() + ":" + X[i][j][vehicle].getSol() + " ");
					}
				}
			}
		}

	}




	private static void initializationVariables(XPRBvar[][][] X, XPRBvar[][] W, XPRBvar[][] lenght, XPRBprob model) {

		// Binary variable X
		for(int i=0;i<n;i++) {
			for(int j=0;j<n;j++) {
				for(int vehicle=0;vehicle<V;vehicle++) {
					X[i][j][vehicle]=model.newVar("X_(" + i+","+j +","+vehicle+ ")", XPRB.BV);
				}			
			}		
		}

		// Integer variable W

		for(int j=0;j<n;j++) {
			for(int vehicle=0;vehicle<V;vehicle++) {
				W[j][vehicle]=model.newVar("X_("+ j +","+vehicle+ ")", XPRB.BV);
			}			
		}		

		// lenght variable W

		for(int j=0;j<n;j++) {
			for(int vehicle=0;vehicle<V;vehicle++) {
				lenght[j][vehicle]=model.newVar("X_("+ j +","+vehicle+ ")", XPRB.BV);
			}			
		}	


	}


}
