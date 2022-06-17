package net.finmath.experiments.stochasticIntegratedAssesment;

import java.util.Arrays;
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
import net.finmath.montecarlo.RandomVariableFactory;
import net.finmath.montecarlo.RandomVariableFromArrayFactory;
import net.finmath.plots.Plots;
import net.finmath.stochastic.RandomVariable;

/*
 * Experiment related to the DICE model.
 * 
 * Note: The code makes some small simplification: it uses a constant savings rate and a constant external forcings.
 * It may still be useful for illustration.
 */
public class SIAModelExperiment {

	private static int numberOfTimes = 100;
	
	final int numberOfPaths	= 10000;
	final int seed			= 53252;

	final double lastTime = 100;
	final double dt = 1;
	
	RandomVariableFactory randomFactory = new RandomVariableFromArrayFactory();

	/*
	 * Input to this class
	 */
	private final Function<Double, RandomVariable> abatementFunction;

	/*
	 * Simulated values - stored for plotting and analysis
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
	
	

	public SIAModelExperiment(Function<Double, RandomVariable> abatementFunction) {
		super();
		this.abatementFunction = abatementFunction;

	}

	public static void main(String[] args) {

		System.out.println("Timeindex of max abatement \t   Temperature \t   Emission \t   GDP \t   Value");
		System.out.println("_".repeat(79));

		double abatementInitial = 0.03;
		double abatementMax = 1.05;
		for(int abatementSzenario = 0; abatementSzenario <= 30; abatementSzenario++) {
			double abatementIncrease = abatementSzenario*0.1* (abatementMax-abatementInitial);
			double abatementMaxTime = 100 * (abatementMax-abatementInitial) / abatementIncrease;
			
			RandomVariableFactory randomFactory = new RandomVariableFromArrayFactory();

			/*
			 * In this experiment the abatement is not optimized (and neither the savings rate).
			 * We just plot the model values for different abatement functions (and a constant savings rate).
			 */
			Function<Double, RandomVariable> abatementFunction = timeIndex -> randomFactory.createRandomVariable(Math.min(abatementInitial + abatementIncrease * timeIndex/numberOfTimes, abatementMax));

			SIAModelExperiment siaModel = new SIAModelExperiment(abatementFunction);
			siaModel.init();

			System.out.println(String.format("\t %8.4f \t %8.4f \t %8.4f \t %8.4f \t %8.4f", abatementMaxTime,siaModel.temperature[numberOfTimes-1].getExpectedTemperatureOfAtmosphere() ,siaModel.emission[numberOfTimes-1].getAverage(),siaModel.abatement[numberOfTimes-1].getAverage(),siaModel.value[numberOfTimes-1].getAverage()));
			
			//siaModel.carbonConcentration[numberOfTimes-1].getCarbonConcentrationInAtmosphere().getAverage()
			
			if(abatementSzenario%5== 0) {
				
				Plots
				.createScatter(IntStream.range(0, numberOfTimes).mapToDouble(i -> (double)i).toArray(), Arrays.stream(siaModel.temperature).mapToDouble(Temperature::getExpectedTemperatureOfAtmosphere).toArray(), 0, 300, 3)
				.setTitle("temperature (szenario=" + abatementMaxTime + ")").setXAxisLabel("time index").show();
				/*
				
				Plots
				.createScatter(IntStream.range(0, numberOfTimes).mapToDouble(i -> (double)i).toArray(), Arrays.stream(siaModel.emission).mapToDouble(RandomVariable::getAverage).toArray(), 0, 300, 3)
				.setTitle("emission (szenario=" + abatementMaxTime + ")").setXAxisLabel("time index").show();
				Plots
				.createScatter(IntStream.range(0, numberOfTimes).mapToDouble(i -> (double)i).toArray(), Arrays.stream(siaModel.abatement).mapToDouble(RandomVariable::getAverage).toArray(), 0, 300, 3)
				.setTitle("abatement (szenario=" + abatementMaxTime + ")").setXAxisLabel("time index").show();

				Plots
				.createScatter(IntStream.range(0, numberOfTimes).mapToDouble(i -> (double)i).toArray(), Arrays.stream(siaModel.carbonConcentration).mapToDouble(CarbonConcentration::getExpectedCarbonConcentrationInAtmosphere).toArray(), 0, 300, 3)
				.setTitle("carbon (szenario=" + abatementMaxTime + ")").setXAxisLabel("time index").show();
				
				
				Plots
				.createScatter(IntStream.range(0, numberOfTimes).mapToDouble(i -> (double)i).toArray(), siaModel.abatement, 0, 300, 3)
				.setTitle("abatement (szenario=" + abatementMaxTime + ")").setXAxisLabel("time index").show();
				*/
			}
		}
	}

	private void init() {

		/*
		 * Building the model by composing the different functions
		 */
		
		/*
		 * Discount rate
		 * 
		 * r = 0.01	 80.0 
		 * r = 0.03 153.8462
		 */
		double r = 0.03;

		/*
		 * Note: Calling default constructors for the sub-models will initialise the default parameters.
		 */

		/*
		 * State vectors initial values
		 */
		Temperature temperatureInitial = new Temperature(1.07);	
		CarbonConcentration carbonConcentrationInitial = new CarbonConcentration(2370);	// Level of Carbon (GtC)

		/*
		 * Sub-Modules: functional dependencies and evolution
		 */

		// Model that describes the damage on the GBP as a function of the temperature-above-normal
		DamageFromTemperature damageFunction = new DamageFromTemperature();

		EvolutionOfTemperature evolutionOfTemperature = new EvolutionOfTemperature();


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
			 * We are stepping in 1 years (the models are currently hardcoded to dT = 1 year.
			 */
			double time = i;

			/*
			 * Evolve geo-physical quantities i -> i+1 (as a function of gdp[i])
			 */
			
			temperature[i+1] = evolutionOfTemperature.apply(temperature[i], carbonConcentration[i].getCarbonConcentrationInAtmosphere());
			
			abatement[i] = abatementFunction.apply(time);

			// Note: In the original model the 1/(1-\mu(0)) is part of the emission function. Here we add the factor on the outside
			emission[i] = (randomFactory.createRandomVariable(1).sub(abatement[i])).div(randomFactory.createRandomVariable(1).sub(abatement[0])).mult(emissionFunction.apply(time, gdp[i]));

			carbonConcentration[i+1] = new CarbonConcentration(carbonConcentration[i].getCarbonConcentrationInAtmosphere().add(emission[i]));


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
			double savingsRate = 0.259029014481802;	//0.259029014481802;

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
			double discountRate = r;
			
			double discountFactor = Math.exp(- discountRate * (time));
			welfare[i] = utility.mult(discountFactor);

			utilityDiscountedSum = utilityDiscountedSum.add(welfare[i]);
			value[i+1] = utilityDiscountedSum;
			
			abatement[i+1] = abatementFunction.apply(time+1);
			emission[i+1] = emissionFunction.apply(time+1, gdp[i+1]);
		}
		
	}
}
