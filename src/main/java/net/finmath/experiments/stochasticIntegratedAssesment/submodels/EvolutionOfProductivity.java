package net.finmath.experiments.stochasticIntegratedAssesment.submodels;

import java.util.function.BiFunction;

import net.finmath.montecarlo.RandomVariableFactory;
import net.finmath.montecarlo.RandomVariableFromArrayFactory;
import net.finmath.stochastic.RandomVariable;

/**
 * the evolution of the productivity (economy)
 * \(
 * 	A(t_{i+1}) = A(t_{i}) / (1-ga * \exp(- deltaA * t))
 * \)
 * 
 * TODO: should this also be randomized?
 * @author Christian Fries, Lennart Quante
 */
public class EvolutionOfProductivity implements BiFunction<Double, RandomVariable, RandomVariable> {

	private final double productivityGrowthRate;        	// ga: Initial TFP rate
	private final double productivityGrowthRateDecayRate;	// deltaA: TFP increase rate

	public EvolutionOfProductivity(double productivityGrowthRate, double productivityGrowthRateDecayRate) {
		super();
		this.productivityGrowthRate = productivityGrowthRate;
		this.productivityGrowthRateDecayRate = productivityGrowthRateDecayRate;
	}

	public EvolutionOfProductivity() {
		// Parameters from original model: initial productivity growth 0.076, decaying with 0.005 per 5 years, thus 0.001 per 1 year
		this(0.076, 0.001);
	}

	@Override
	public RandomVariable apply(Double time, RandomVariable productivity) {
		return (productivity.div(1 - productivityGrowthRate).mult(Math.exp(-productivityGrowthRateDecayRate * time)));	
	}
}
