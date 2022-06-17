package net.finmath.experiments.stochasticIntegratedAssesment.submodels;

import java.util.function.BiFunction;

import net.finmath.montecarlo.RandomVariableFactory;
import net.finmath.montecarlo.RandomVariableFromArrayFactory;
import net.finmath.stochastic.RandomVariable;

/**
 * The function that maps (relative) abatement coefficient to (relative) cost.
 * 
 * Function of (time, abatement)
 * Note: Sigma factor is missing here (moved to the outside)
 *
 * @author Christian Fries, Lennart Quante
 */
public class AbatementCostFunction implements BiFunction<Double, RandomVariable, RandomVariable> {

	private RandomVariableFactory randomVariableFactory;
	private double backstopPriceInitial;		// Initial cost for backstop
	private double backstopRate;				// Decay of backstop cost (parameter from original 
	private double abatementExponent;			// Exponent abatement in cost function (GAMS expcost2)

	public AbatementCostFunction(double backstopPriceInitial, double backstopRate, double abatementExponent,RandomVariableFactory randomVariableFactory) {
		super();
		this.backstopPriceInitial = backstopPriceInitial;
		this.backstopRate = backstopRate;
		this.abatementExponent = abatementExponent;
		this.randomVariableFactory = randomVariableFactory;
	}

	public AbatementCostFunction() {
		// Parameters from original paper. Note that the rate is a "per 5 year" rate.
		this(550.0/1000.0, 1-Math.pow(1-0.025,1/5), 2.6,new RandomVariableFromArrayFactory());
	}
	
	public AbatementCostFunction(RandomVariableFactory randomVariableFactory) {
		// Parameters from original paper. Note that the rate is a "per 5 year" rate.
		this(550.0/1000.0, 1-Math.pow(1-0.025,1/5), 2.6,randomVariableFactory);
	}

	@Override
	public RandomVariable apply(Double time, RandomVariable abatement) {
		RandomVariable abatementCost = abatement.pow(abatementExponent).div(abatementExponent).mult(backstopPriceInitial * Math.pow(1-backstopRate, time));
		// alternatively, express the backstopRate as exponential decay
		//		double abatementCost = backstopPriceInitial * Math.exp(-backstopRate * time) * Math.pow(abatement , theta2)/theta2;
		return abatementCost;
	}
}
