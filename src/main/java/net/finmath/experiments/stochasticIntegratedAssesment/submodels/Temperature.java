package net.finmath.experiments.stochasticIntegratedAssesment.submodels;

import net.finmath.montecarlo.RandomVariableFactory;
import net.finmath.montecarlo.RandomVariableFromArrayFactory;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.stochastic.RandomVariable;

/**
 * State vector of random variables representing temperature above pre-industrial level.
 * 
 * @author Christian Fries, Lennart Quante
 */
public class Temperature {

	private RandomVariable temperatureOfAtmosphere;
	private RandomVariableFactory randomVariableFactory;
	
	public Temperature(RandomVariable temperature, RandomVariableFactory randomVariableFactory) {
		super();
		this.randomVariableFactory = randomVariableFactory;
		this.temperatureOfAtmosphere = temperature;

	}
	
	public Temperature(RandomVariable temperature) {
		super();
		this.temperatureOfAtmosphere = temperature;
	}
	
	public Temperature(double temperature) {
		super();
		this.randomVariableFactory = new RandomVariableFromArrayFactory();
		this.temperatureOfAtmosphere = randomVariableFactory.createRandomVariable(temperature);
	}

	public Temperature() {
		this(1.07); // observed warming median according to IPCC WGI AR6
	}
	public RandomVariable getTemperatureOfAtmosphere() {
		return temperatureOfAtmosphere;
	}
	
	public Double getExpectedTemperatureOfAtmosphere() {
		return temperatureOfAtmosphere.getAverage();
	}
}
