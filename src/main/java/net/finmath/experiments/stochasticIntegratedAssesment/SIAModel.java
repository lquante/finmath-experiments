package net.finmath.experiments.stochasticIntegratedAssesment;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

import net.finmath.experiments.stochasticIntegratedAssesment.submodels.AbatementCostFunction;
import net.finmath.experiments.stochasticIntegratedAssesment.submodels.CarbonConcentration;
import net.finmath.experiments.stochasticIntegratedAssesment.submodels.DamageFromTemperature;
import net.finmath.experiments.stochasticIntegratedAssesment.submodels.EmissionFunction;
import net.finmath.experiments.stochasticIntegratedAssesment.submodels.EmissionIntensityFunction;
import net.finmath.experiments.stochasticIntegratedAssesment.submodels.EvolutionOfCapital;
import net.finmath.experiments.stochasticIntegratedAssesment.submodels.EvolutionOfPopulation;
import net.finmath.experiments.stochasticIntegratedAssesment.submodels.EvolutionOfProductivity;
import net.finmath.experiments.stochasticIntegratedAssesment.submodels.EvolutionOfTemperature;
import net.finmath.experiments.stochasticIntegratedAssesment.submodels.Temperature;
import net.finmath.montecarlo.RandomVariableFromArrayFactory;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.plots.Plots;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

/*
 * Experiment related to developing a stochastic integrated assesment model.
 * 
 * Note: The code makes as many simplifications as possible and can be enhanced module wise.
 * It may still be useful for illustration.
 */
public class SIAModel {
	
	
	/* randomness parameters
	 * to be used for all random variables
	 * TODO: vary seed
	 */
	
	final int numberOfPaths	= 10000;
	final int seed			= 53252;

	final double lastTime = 100;
	final double dt = 1;
	
	RandomVariableFromArrayFactory randomFactory = new RandomVariableFromArrayFactory();

	// Create the time discretization
	final TimeDiscretization timeDiscretization = new TimeDiscretizationFromArray(0.0, (int)(lastTime/dt), dt);


	public static int numberOfTimes = 100;

	/*
	 * Input to this class
	 */
	private final double discountRate;
	private final Function<Double, RandomVariable> abatementFunction;
	private final Function<Double, Double> savingsRateFunction;

	/*
	 * Simulated values - stored for plotting ande analysis
	 */
	private Temperature[] temperature = new Temperature[numberOfTimes];
	private CarbonConcentration[] carbonConcentration = new CarbonConcentration[numberOfTimes];
	private RandomVariable[] gdp = new RandomVariable[numberOfTimes];
	private RandomVariable[] emission = new RandomVariable[numberOfTimes];
	private RandomVariable[] abatement = new RandomVariable[numberOfTimes];
	private RandomVariable[] damage = new RandomVariable[numberOfTimes];
	private RandomVariable[] capital = new RandomVariable[numberOfTimes];
	private RandomVariable[] population = new RandomVariable[numberOfTimes];
	private RandomVariable[] productivity = new RandomVariable[numberOfTimes];
	private RandomVariable[] welfare = new RandomVariable[numberOfTimes];
	private RandomVariable[] value = new RandomVariable[numberOfTimes];

	public SIAModel(double discountRate, Function<Double, RandomVariable> abatementFunction, Function<Double, Double> savingsRateFunction) {
		super();
		this.discountRate = discountRate;
		this.abatementFunction = abatementFunction;
		this.savingsRateFunction = savingsRateFunction;
	}
	
	public RandomVariable[] getValues() {
		synchronized (welfare) {
			this.init();
			return welfare;
		}
	}

	public RandomVariable getValue() {
		synchronized (value) {
			this.init();
			return value[numberOfTimes-1];
		}
	}

	private void init() {

		/*
		 * Building the model by composing the different functions
		 */
		
		/*
		 * Note: Calling default constructors for the sub-models will initialise the default parameters.
		 */

		/*
		 * State vectors initial values
		 */
		Temperature temperatureInitial = new Temperature(1.07);	
		CarbonConcentration carbonConcentrationInitial = new CarbonConcentration(2390);	// Level of Carbon (GtC)

		/*
		 * Sub-Modules: functional dependencies and evolution
		 */

		// Model that describes the damage on the GBP as a function of the temperature-above-normal
		DamageFromTemperature damageFunction = new DamageFromTemperature();

		EvolutionOfTemperature evolutionOfTemperature = new EvolutionOfTemperature();

		Double forcingExternal = 1.0;

		EmissionIntensityFunction emissionIntensityFunction = new EmissionIntensityFunction();
		EmissionFunction emissionFunction = new EmissionFunction(emissionIntensityFunction);

		// Abatement
		AbatementCostFunction abatementCostFunction = new AbatementCostFunction();

		/*
		 * GDP
		 */
		double K0 = 223;		// Initial Capital
		double L0 = 7403;		// Initial Population
		double A0 = 5.115;		// Initial Total Factor of Productivity 
		double gamma = 0.3;		// Capital Elasticity in Production Function
		double gdpInitial = A0*Math.pow(K0,gamma)*Math.pow(L0/1000,1-gamma);

		// Capital
		EvolutionOfCapital evolutionOfCapital = new EvolutionOfCapital();
		
		// Population
		EvolutionOfPopulation evolutionOfPopulation = new EvolutionOfPopulation();

		// Productivity
		EvolutionOfProductivity evolutionOfProductivity = new EvolutionOfProductivity();

		/*
		 * Set initial values
		 */
		temperature[0] = temperatureInitial;
		carbonConcentration[0] = carbonConcentrationInitial;
		gdp[0] = randomFactory.createRandomVariable(gdpInitial);
		capital[0] = randomFactory.createRandomVariable(K0);
		population[0] = randomFactory.createRandomVariable(L0);
		productivity[0] = randomFactory.createRandomVariable(A0);
		RandomVariable utilityDiscountedSum = randomFactory.createRandomVariable(0);

		/*
		 * Evolve
		 */
		for(int i=0; i<numberOfTimes-1; i++) {

			/*
			 * We are stepping in 5 years (the models are currently hardcoded to dT = 5 year.
			 * The time parameter is currently just the index. (Need to rework this).
			 */
			double time = i;
			double timeStep = 5.0;

			/*
			 * Evolve geo-physical quantities i -> i+1 (as a function of gdb[i])
			 */
			
			temperature[i+1] = evolutionOfTemperature.apply(temperature[i], carbonConcentration[0].getCarbonConcentrationInAtmosphere());
			
			abatement[i] = abatementFunction.apply(time);

			// Note: In the original model the 1/(1-\mu(0)) is part of the emission function. Here we add the factor on the outside
			emission[i+1] = (randomFactory.createRandomVariable(1).sub(abatement[i])).div(randomFactory.createRandomVariable(1).sub(abatement[0])).mult(emissionFunction.apply(time, gdp[i]));

			carbonConcentration[i+1] = new CarbonConcentration(carbonConcentration[0].getCarbonConcentrationInAtmosphere().add(emission[i]));


			/*
			 * Evolve economy i -> i+1 (as a function of temperature[i])
			 */

			// Apply damage to economy
			damage[i] = damageFunction.apply(temperature[i].getTemperatureOfAtmosphere());

			/*
			 * Abatement cost
			 */
			RandomVariable abatementCost = abatementCostFunction.apply(time, abatement[i]);

			/*
			 * Evolve economy to i+1
			 */
			RandomVariable gdpNet = gdp[i].mult(randomFactory.createRandomVariable(1).sub(damage[i])).mult(randomFactory.createRandomVariable(1).sub(abatementCost));

			// Constant from the original model - in the original model this is a time varying control variable.
			double savingsRate = savingsRateFunction.apply(time);	//0.259029014481802;

			RandomVariable consumption = gdpNet.mult(1-savingsRate);
			RandomVariable investment = gdpNet.mult(savingsRate);

			capital[i+1] = evolutionOfCapital.apply(capital[i], investment);

			/*
			 * Evolve population and productivity for next GDP
			 */
			population[i+1] = evolutionOfPopulation.apply(time,population[i]);
			productivity[i+1] = evolutionOfProductivity.apply(time,productivity[i]);

			RandomVariable L = population[i+1];
			RandomVariable A = productivity[i+1];
			gdp[i+1] = A.mult(capital[i+1].pow(gamma)).mult(L.div(1000).pow(1-gamma));

			/*
			 * Calculate utility
			 */
			double alpha = 1.45;           // Elasticity of marginal utility of consumption (GAMS elasmu)
			RandomVariable C = consumption;
			RandomVariable utility = L.mult(C.div(L.div(1000)).pow(1-alpha).div(1-alpha));

			/*
			 * Discounted utility
			 */
			double discountFactor = Math.exp(- discountRate * (time*timeStep));
			welfare[i] = utility.mult(discountFactor);

			utilityDiscountedSum = utilityDiscountedSum.add(welfare[i]);
			value[i+1] = utilityDiscountedSum;
		}
	}
}
