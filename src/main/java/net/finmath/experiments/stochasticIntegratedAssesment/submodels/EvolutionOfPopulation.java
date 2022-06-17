package net.finmath.experiments.stochasticIntegratedAssesment.submodels;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.finmath.montecarlo.RandomVariableFactory;
import net.finmath.montecarlo.RandomVariableFromArrayFactory;
import net.finmath.stochastic.RandomVariable;

/**
 * the evolution of the population (economy)
 * \(
 * 	L(t_{i+1}) = L(t_{i}) * (L(\infty)/L(t_{i})^{g}
 * \)
 * 
 * 
 * @author Christian Fries, Lennart Quante
 */
public class EvolutionOfPopulation implements BiFunction<Double, RandomVariable, RandomVariable> {

	
	private RandomVariable populationAsymptotic;	 // Asymptotic population (La)
	private Double populationGrowth;		 // Population growth (lg) (in the original model this a per-5Y, could be randomized?!
	private RandomVariableFactory randomVariableFactory;

	public EvolutionOfPopulation(RandomVariable populationAsymptotic, Double populationGrowth) {
		super();
		this.populationAsymptotic = populationAsymptotic;
		this.populationGrowth = populationGrowth;
	}
	
	public EvolutionOfPopulation(double populationAsymptotic, double populationGrowth, RandomVariableFactory randomVariableFactory) {
		super();
		this.randomVariableFactory=randomVariableFactory;
		this.populationAsymptotic=randomVariableFactory.createRandomVariable(populationAsymptotic);
		this.populationGrowth = populationGrowth;
	}

	public EvolutionOfPopulation(double populationAsymptotic, double populationGrowth) {
		super();
		this.randomVariableFactory = new RandomVariableFromArrayFactory();
		this.populationAsymptotic=randomVariableFactory.createRandomVariable(populationAsymptotic);
		this.populationGrowth = populationGrowth;
	}
	
	public EvolutionOfPopulation() {
		this(11500,0.134/5); //original parameter is per 5 year time step, rough estimate
	}
	
	@Override
	public RandomVariable apply(Double time, RandomVariable population) {
		return population.mult((populationAsymptotic.div(population)).pow(populationGrowth*time));
	}
}
