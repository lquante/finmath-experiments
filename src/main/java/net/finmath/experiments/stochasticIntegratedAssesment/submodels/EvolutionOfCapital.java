package net.finmath.experiments.stochasticIntegratedAssesment.submodels;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.finmath.stochastic.RandomVariable;

/**
 * the evolution of the capital (economy)
 * \(
 * 	K(t_{i+1}) = (1-delta) K(t_{i}) + investment
 * \)
 * 
 * @author Christian Fries, Lennart Quante
 */
public class EvolutionOfCapital implements BiFunction<RandomVariable, RandomVariable, RandomVariable>{

	private final double capitalDeprecation;

	public EvolutionOfCapital(double capitalDeprecation) {
		super();
		this.capitalDeprecation = capitalDeprecation;
	}

	public EvolutionOfCapital() {
 		// capital deprecation per 1 year: 5tt root of (1-0.1) per 5 years
		this(1-Math.pow(0.9, 1/5));
	}

	@Override
	public RandomVariable apply(RandomVariable capital, RandomVariable investment) {
			return capital.mult(1.0-capitalDeprecation).add(investment);
	};		
}

