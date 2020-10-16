package alg;
import java.io.Serializable;
import java.util.Comparator;

/**
 * @author Angel A. Juan - ajuanp(@)gmail.com
 * @version 130112
 */
public class Edge implements Serializable
{
    /* INSTANCE FIELDS & CONSTRUCTOR */
	private String key="";
    private Node origin; // origin node
    private Node end; // end node
    private double costs = 0.0; // edge costs
    private double meanCosts = 0.0; // edge costs
    private double savings = 0.0; // edge savings (Clarke & Wright)
    private double classicSavings = 0.0;
    private Route inRoute = null; // route containing this edge (0 if no route assigned)
    private Edge inverseEdge = null; // edge with inverse direction
    private double[] stoCosts = null; // edge costs
            
    public Edge(Node originNode, Node endNode) 
    {   key=originNode.getId()+","+endNode.getId();
    	origin = originNode;
        end = endNode;
    }
    
    public Edge(Edge e){   
    	this.key = e.key;
    	this.origin = e.origin;
    	this.end = e.end; 
    	this.costs = e.costs;
    	this.meanCosts = e.meanCosts; 
    	this.savings = e.savings; 
    	this.classicSavings = e.classicSavings;
    	if(e.inRoute !=null){
         this.inRoute = new Route (e.inRoute);
    	}else{
    		this.inRoute = null;	
    	}
        this.stoCosts = e.stoCosts; 
    }

	

	/* SET METHODS */
    public void setCosts(double c){costs = c;}
    public void setSavings(double s){savings = s;}
    public void setInRoute(Route r){inRoute = r;}
    public void setInverse(Edge e){inverseEdge = e;}
    public void setStoCosts(double[] stoCosts) {this.stoCosts = stoCosts;}
    public void setmeanCosts(double stoCosts) {this.meanCosts = stoCosts;}
    
    /* GET METHODS */
    public Node getOrigin(){return origin;}
    public Node getEnd(){return end;}
    public double getCosts(){return costs;}
    public double getMeanCosts(){return meanCosts;}
    public double getSavings(){return savings;}
    public Route getInRoute(){return inRoute;}
    public Edge getInverseEdge(){return inverseEdge;}
    public double[] getStoCosts() {return stoCosts;}
    public double getStoCosts(int scenario) {return stoCosts[scenario];}
    public double getClassicSavings() {return classicSavings;}
    public String getKey() {return key;}
    
    /* AUXILIARY METHODS */
    
    public double calcCosts()
    {   double X1 = origin.getX();
        double Y1 = origin.getY();
        double X2 = end.getX();
        double Y2 = end.getY();
        double d = Math.sqrt((X2 - X1) * (X2 - X1) + (Y2 - Y1) * (Y2 - Y1));
        return d;
    }
    
    public static double calcCosts(Node origin, Node end)
    {   double X1 = origin.getX();
        double Y1 = origin.getY();
        double X2 = end.getX();
        double Y2 = end.getY();
        double d = Math.sqrt((X2 - X1) * (X2 - X1) + (Y2 - Y1) * (Y2 - Y1));
        return d;
    }


    public double calcSavings(Node origin, Node end, double alpha, Inputs inputs)
    {
        // Costs of origin depot to end node
    	String key1=origin.getId()+","+this.end.getId();
    	Edge e1=inputs.getEdgesDirectory().get(key1);
    	String key2=this.origin.getId()+","+end.getId();
    	Edge e2=inputs.getEdgesDirectory().get(key2);
        double Coj = e1.getCosts();
        // Costs of origin node to end depot
        double Cie = e2.getCosts();
        // Costs of originNode to endNode
        double Cij = costs;
        
        //Return cost depot to savings
        double Sij = Coj + Cie - Cij;
        classicSavings = Sij;
        return alpha*Sij + (1-alpha)*(this.origin.getProfit() + this.end.getProfit());
    }
    
	static final Comparator<Edge> minDistance = new Comparator<Edge>(){
		public int compare(Edge a1, Edge a2){
			if (a1.costs < a2.costs) return 1;
			if (a1.costs > a2.costs) return -1;
			return 0;
			}
		};
    
	static final Comparator<Edge> savingsComp = new Comparator<Edge>(){
		public int compare(Edge a1, Edge a2){
			if (a1.savings < a2.savings) return 1;
			if (a1.savings > a2.savings) return -1;
			return 0;
			}
		};
		
    
    
    
    
    @Override
    public String toString() 
    { 
    	String s = "";
        s = s.concat("\nEdge origin: " + this.getOrigin());
        s = s.concat("\nEdge end: " + this.getEnd());
        s = s.concat("\nEdge costs: " + (this.getCosts()));
        s = s.concat("\nEdge savings: " + (this.getSavings()));
        return s;
    }

}