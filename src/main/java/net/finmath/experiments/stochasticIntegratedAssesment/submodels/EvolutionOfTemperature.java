package net.finmath.experiments.stochasticIntegratedAssesment.submodels;

import java.util.function.BiFunction;

import net.finmath.experiments.LinearAlgebra;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.stochastic.RandomVariable;

/**
 * The evolution of the temperature depending on cumulative emissions using TCRE
 * @author Lennart Quante
 */
public class EvolutionOfTemperature implements BiFunction<Temperature, RandomVariable, Temperature> {

	/**
	 * linearized cumulative emissions - temperature relation based on IPCC AR 6 WGI SPM - fig. 10 and text:
	 * Each 1000 GtCO 2 of cumulative CO 2 emissions is assessed
	 * to likely cause a 0.27°C to 0.63°C increase in global surface temperature with a best estimate of 0.45°C
	 */
	
	
	private static double defaultTransientClimateResponse = 0.45/1000; //expected increase in GMT for 1000 Gt CO2
	
	private double transientClimateResponse;

	/**
	 * @param transientClimateResponse
	 * @param cumulativeEmissions
	 */
	public EvolutionOfTemperature(double transientClimateResponse) {
		super();
		this.transientClimateResponse = transientClimateResponse;
	}

	public EvolutionOfTemperature() {
		this(defaultTransientClimateResponse);
	}

	@Override
	public Temperature apply(Temperature temperature, RandomVariable cumulativeEmissions) {
		RandomVariable temperatureNext = cumulativeEmissions.mult(this.transientClimateResponse);
		return new Temperature(temperatureNext);
	}

}
