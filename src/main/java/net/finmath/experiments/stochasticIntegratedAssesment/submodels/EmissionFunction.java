package net.finmath.experiments.stochasticIntegratedAssesment.submodels;

import java.util.function.BiFunction;

import net.finmath.stochastic.RandomVariable;

/**
 * The function that maps economicOutput to emission at a given time in GtCO2 / year
 * \(
 * 	(t, Y) \mapsto E(t,Y)
 * \)
 * where Y is the GDP.
 * 
 * Note: The emissions is in GtCO2 / year.
 * 
 * Note: The function depends on the time step size
 * TODO Change parameter to per year.
 * 
 * @author Christian Fries
 */
public class EmissionFunction implements BiFunction<Double, RandomVariable, RandomVariable> {


	private final EmissionIntensityFunction emissionIntensityFunction;
	private final double externalEmissionsInitial;
	private final double externalEmissionsDecay;	// per 5Y
	
	private static double annualizedExternalEmissionsDecay = 1-Math.pow(1-0.115, 1/5); //0.115 for 5 years, thus 1-5th_root(1-0.115)

	public EmissionFunction(EmissionIntensityFunction emissionIntensityFunction, double externalEmissionsInitial, double externalEmissionsDecay) {
		super();
		this.emissionIntensityFunction = emissionIntensityFunction;
		this.externalEmissionsInitial = externalEmissionsInitial;
		this.externalEmissionsDecay = externalEmissionsDecay;
	}

	public EmissionFunction(EmissionIntensityFunction emissionIntensityFunction) {
		// Parameters from original model
		this(emissionIntensityFunction, 2.6, annualizedExternalEmissionsDecay);
	}

	@Override
	public RandomVariable apply(Double time, RandomVariable economicOutput) {
		double emissionPerEconomicOutput = emissionIntensityFunction.apply(time);
		double externalEmissions = externalEmissionsInitial * Math.pow(1-externalEmissionsDecay, time);

		return (economicOutput.mult(emissionPerEconomicOutput).add(externalEmissions));
	}
}
