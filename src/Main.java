public class Main {

  public static void main(String[] args) throws Exception
  {
    Evolve e = new Evolve();
    double[] w = e.evolveWeights();
    Controller.doBattle(new ReflexAgent(), new NeuralAgent(w));
    System.out.println();
  }
  
}
