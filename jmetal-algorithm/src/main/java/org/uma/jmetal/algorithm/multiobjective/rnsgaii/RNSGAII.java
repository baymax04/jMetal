package org.uma.jmetal.algorithm.multiobjective.rnsgaii;

import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.algorithm.multiobjective.rnsgaii.util.PreferenceNSGAII;
import org.uma.jmetal.algorithm.multiobjective.rnsgaii.util.RNSGAIIRanking;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.solutionattribute.Ranking;

import java.util.*;

public class RNSGAII <S extends Solution<?>> extends NSGAII<S> {

  protected List<Double> interestPoint = null;
  protected double epsilon;
  protected PreferenceNSGAII<S> achievementScalarizingFunction;
  /**
   * Constructor
   *
   * @param problem
   * @param maxEvaluations
   * @param populationSize
   * @param crossoverOperator
   * @param mutationOperator
   * @param selectionOperator
   * @param evaluator
   */
  public RNSGAII(Problem<S> problem, int maxEvaluations, int populationSize, CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator, SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator,List<Double> referencePoint, double epsilon) {
    super(problem, maxEvaluations, populationSize, crossoverOperator, mutationOperator, selectionOperator, evaluator);
    interestPoint = referencePoint;
    this.epsilon = epsilon;
    this.achievementScalarizingFunction = this.createUtilityFunction();
  }


  public PreferenceNSGAII<S> createUtilityFunction() {
    List<Double> weights = new ArrayList<>();
    for(int i=0; i<getProblem().getNumberOfObjectives();i++){
      weights.add(1.0d/getProblem().getNumberOfObjectives());

    }
    PreferenceNSGAII<S> aux = new PreferenceNSGAII<S>(weights);
    return aux;
  }
  public void updateReferencePoint(List<Double> newReferencePoints){
    this.interestPoint = newReferencePoints;
  }



  @Override protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
    List<S> jointPopulation = new ArrayList<>();
    jointPopulation.addAll(population);
    jointPopulation.addAll(offspringPopulation);

    Ranking<S> ranking = computeRanking(jointPopulation);

    return preferenceDistanceSelection(ranking);
  }

  @Override public List<S> getResult() {
    return getNonDominatedSolutions(getPopulation());
  }


  protected Ranking<S> computeRanking(List<S> solutionList) {
    Ranking<S> ranking = new RNSGAIIRanking<S>(achievementScalarizingFunction,epsilon,interestPoint);
    ranking.computeRanking(solutionList);

    return ranking;
  }

  protected boolean populationIsNotFull(List<S> population) {
    return population.size() < getMaxPopulationSize();
  }

  protected boolean subfrontFillsIntoThePopulation(Ranking<S> ranking, int rank, List<S> population) {
    return ranking.getSubfront(rank).size() < (getMaxPopulationSize() - population.size());
  }

  protected List<S> preferenceDistanceSelection(Ranking<S> ranking) {

    List<S> population = new ArrayList(this.getPopulationSize());
    int rankingIndex = 0;

    while(this.populationIsNotFull(population)) {
      if (this.subfrontFillsIntoThePopulation(ranking, rankingIndex, population)) {
        this.addRankedSolutionsToPopulation(ranking, rankingIndex, population);
        ++rankingIndex;
      } else {
        this.addLastRankedSolutionsToPopulation(ranking, rankingIndex, population);
      }
    }

    return population;
  }

  public int getPopulationSize() {
    return this.getMaxPopulationSize();
  }
  protected void addRankedSolutionsToPopulation(Ranking<S> ranking, int index, List<S> population) {
    population.addAll(ranking.getSubfront(index));
  }

  protected void addLastRankedSolutionsToPopulation(Ranking<S> ranking, int index, List<S> population) {
    List<S> front = ranking.getSubfront(index);
    int remain = this.getPopulationSize() - population.size();
    population.addAll(front.subList(0, remain));
  }

  public List<Double> getInterestPoint() {
    return interestPoint;
  }

  @Override public String getName() {
    return "RNSGAII" ;
  }

  @Override public String getDescription() {
    return "Nondominated Sorting Genetic Algorithm version II with Reference Point" ;
  }
}

