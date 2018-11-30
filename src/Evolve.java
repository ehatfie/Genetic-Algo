import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

class Results{
	ArrayList<Integer> winners;
	ArrayList<Integer> losers;

	Results(ArrayList<Integer> reportedWinners, ArrayList<Integer> reportedLosers){
		this.winners = new ArrayList<>();
		this.losers = new ArrayList<>();

		for(int i = 0; i < reportedWinners.size(); i++){
			this.winners.add(reportedWinners.get(i));
			this.losers.add(reportedLosers.get(i));
		}
	}
}


class Evolve
{
	private static int numWinners;
	private static int totalWinners;
	private static int numCycles;
	private static int populationAmount;
	private static double[][] metaParameters;
	private static Random rand;

	Evolve(){
		numWinners = 0;
		totalWinners = 10;
		numCycles = 10;
		populationAmount = 100;
		metaParameters = new double[populationAmount][5];
		rand = new Random();

		initializeParameters();
	}

	static void initializeParameters(){
		for(int i = 0; i < populationAmount; i++){
			metaParameters[i][0] = 30.0; // mutation rate
			metaParameters[i][1] = 0.1; // deviation amount
			metaParameters[i][2] = 60.0; // survival rate
			metaParameters[i][3] = 0.0; // not used atm
			metaParameters[i][4] = 0.0; // not used atm
		}
	}
	static void initializeParameter(int index){
		// set up for a brand new member
		metaParameters[index][0] = 30.0; // mutation rate
		metaParameters[index][1] = 0.1; // deviation amount
		metaParameters[index][2] = 60.0; // survival rate
		metaParameters[index][3] = 0.0; // not used atm
		metaParameters[index][4] = 0.0; // not used atm
	}



	static double[] evolveWeights()
	{
		Results results;
		Matrix population = new Matrix(100, 291);

		// Create a random initial population
		for(int i = 0; i < populationAmount; i++)
		{
			double[] chromosome = population.row(i);
			for(int j = 0; j < chromosome.length; j++)
				chromosome[j] = 0.03 * rand.nextGaussian();
		}

		// Evolve the population
		// todo: YOUR CODE WILL START HERE.
		//       Please write some code to evolve this population.
		//       (For tournament selection, you will need to call Controller.doBattleNoGui(agent1, agent2).)
		while(numWinners < totalWinners){
			for (int i = 0; i < numCycles; i++){
				if(rand.nextInt(100) < 65){
					mutate(population);
				}
				if(rand.nextInt(100) < 65){
					results = naturalSelection(population);
					repopulate(population, results);
				}
			}
			System.out.println("");
		}

		// Return an arbitrary member from the population
		return population.row(0);
	}

	static void mutate(Matrix population){
		int mutateRoll;
		double mutationDeviation;
		double[] chromosome;

		for(int i = 0; i < populationAmount; i++){
			mutateRoll = rand.nextInt(100);
			if(metaParameters[i][0] <= mutateRoll){
				mutationDeviation = metaParameters[i][1];
				chromosome = population.row(i);
				double val = rand.nextGaussian()*mutationDeviation;
				chromosome[rand.nextInt(chromosome.length)] += val;
			}
		}
	}

	static Results naturalSelection(Matrix population){
		TreeSet<Integer> selectedFighters = new TreeSet<>();
		ArrayList<Integer> winners = new ArrayList<>();
		ArrayList<Integer> losers = new ArrayList<>();
		ArrayList<Integer> fighters = new ArrayList<>();
		int selected;


		for(int i = 0; i < 8; i++){
			do{
				selected = rand.nextInt(populationAmount);
			} while(selectedFighters.contains(selected));
			selectedFighters.add(selected);
			fighters.add(selected);
		}
		int blue = 0, red = 0, outcome = 0;
		for(int i = 0; i < fighters.size()-2; i += 2){
			try {
				blue = fighters.get(i);
				red = fighters.get(i+1);
				outcome = Controller.doBattleNoGui(new NeuralAgent(population.row(blue)), new NeuralAgent(population.row(red)));
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(outcome == -1){
				losers.add(blue);
				winners.add(red);
			} else if (outcome == 1){
				losers.add(red);
				winners.add(blue);
			} else {
				// run a test vs the good ones
			}
		}

		return new Results(winners, losers);
	}

	static void repopulate(Matrix population, Results results){
		int deathRoll, slected, bestMother = -1, mother, father = -1, deadChromosome = -1;
		int randomChoice;
		double[] fatherChrome, motherChrome, replacementChrome = new double[251];
		double similarity = 0, bestSimilarity = 99999.9;

		TreeSet<Integer> nonCandidates = new TreeSet<Integer>();
		TreeSet<Integer> potentialMoms = new TreeSet<>();
		TreeSet<Integer> checkedMoms = new TreeSet<>();
		ArrayList<Integer> fathers = new ArrayList<>();
		ArrayList<Integer> dead = new ArrayList<>();

		Iterator<Integer> loserIterator = results.losers.iterator();
		Iterator<Integer> winnerIterator = results.winners.iterator();

		while(winnerIterator.hasNext()){
			nonCandidates.add(winnerIterator.next());
			nonCandidates.add(loserIterator.next());
		}

		for(int i = 0; i < results.winners.size(); i++) {
			deathRoll = rand.nextInt();
			if (metaParameters[results.winners.get(i)][2] > deathRoll) {
				// if the winner survives
				fathers.add(results.winners.get(i));
				dead.add(results.losers.get(i));
			} else {
				fathers.add(results.losers.get(i));
				dead.add(results.winners.get(i));
			}
		}

			// could be its own function
		for (int i = 0; i < fathers.size(); i++) {
			father = fathers.get(i);
			deadChromosome = dead.get(i);
			// find 5 unique moms
			//====================MAKE A FUNCTION===================================
			for (int j = potentialMoms.size(); j < 5; j++) {
				do {
					mother = rand.nextInt(100);
				} while (nonCandidates.contains(mother) || potentialMoms.contains(mother));
				potentialMoms.add(mother);
			}
			//======================================================================

			//=====================MAKE A FUNCTION==================================
			Iterator<Integer> potentialMomsIterator = potentialMoms.iterator();
			// get similarity
			while (potentialMomsIterator.hasNext()) {
				mother = potentialMomsIterator.next();
				similarity = getSimilarity(population.row(father), population.row(mother));

				if (similarity < bestSimilarity) {
					bestSimilarity = similarity;
					bestMother = mother;
				}
			}
			//======================================================================
			fatherChrome = population.row(father);
			motherChrome = population.row(bestMother);
			potentialMoms.remove(bestMother);

			for (int j = 0; j < fatherChrome.length; j++) {
				randomChoice = rand.nextInt(2);
				if (randomChoice == 1) {
					population.row(deadChromosome)[i] = fatherChrome[i];
				} else {
					population.row(deadChromosome)[i] = motherChrome[i];
				}
			}
		}
	}

	private static double getSimilarity(double[] row1, double[] row2){
		double range, totalRange = 0;

		for(int i = 0; i < row1.length; i++){
			totalRange += Math.abs(row1[i] - row2[i]);
		}
		totalRange = totalRange/row1.length;
		return totalRange/row1.length;
	}



}
