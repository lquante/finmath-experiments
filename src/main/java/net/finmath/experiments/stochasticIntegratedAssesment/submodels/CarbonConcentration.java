package net.finmath.experiments.stochasticIntegratedAssesment.submodels;

import net.finmath.montecarlo.RandomVariableFactory;
import net.finmath.montecarlo.RandomVariableFromArrayFactory;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.stochastic.RandomVariable;

/**
 * State vector of random variables representing cumulative carbon emissions.
 * 
 * @author Christian Fries, Lennart Quante
 */
public class CarbonConcentration {

	private final RandomVariable carbonConcentrationInAtmosphere;
	private RandomVariableFactory randomVariableFactory;
	
	public CarbonConcentration(double carbonConcentrationInAtmosphere, RandomVariableFactory randomVariableFactory) {
		super();
		this.randomVariableFactory = randomVariableFactory;
		this.carbonConcentrationInAtmosphere = new RandomVariableFromDoubleArray(carbonConcentrationInAtmosphere);

	}

	public CarbonConcentration(RandomVariable carbonConcentrationInAtmosphere) {
		this.carbonConcentrationInAtmosphere = carbonConcentrationInAtmosphere;
	}
	public CarbonConcentration(double carbonConcentrationInAtmosphere) {
		super();
		this.randomVariableFactory = new RandomVariableFromArrayFactory();
		this.carbonConcentrationInAtmosphere = randomVariableFactory.createRandomVariable(carbonConcentrationInAtmosphere);
	}
	

	public CarbonConcentration() {
		this(2390);		// 2390GtC cumulative emissions by IPCC AR6
	}

	
	public RandomVariable getCarbonConcentrationInAtmosphere() {
		return carbonConcentrationInAtmosphere;
	}
	
	public Double getExpectedCarbonConcentrationInAtmosphere() {
		return carbonConcentrationInAtmosphere.getAverage();
	}
}
