package solving;

import function.Architect;
import scpsolver.constraints.Constraint;
import scpsolver.constraints.LinearBiggerThanEqualsConstraint;
import scpsolver.constraints.LinearConstraint;
import scpsolver.constraints.LinearEqualsConstraint;
import scpsolver.constraints.LinearSmallerThanEqualsConstraint;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LinearProgram;
import scpsolver.util.SparseVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by samuelkolb on 27/06/16.
 *
 * @author Samuel Kolb
 */
public class GLPKSolver implements LPSolver {

	private double[] objective;
	private double constant;
	private List<LinearConstraint> inequalities = new ArrayList<>();
	private Solver.Operation operation;
	private double[] ub;
	private double[] lb;
	private boolean[] discrete;

	@Override
	public void setObjective(double[] coefficients, double constant) {
		this.objective = coefficients;
		this.constant = constant;
		this.discrete = new boolean[coefficients.length];
	}

	@Override
	public void addSmallerThanConstraint(double[] coefficients, double b) {
		this.inequalities.add(new LinearSmallerThanEqualsConstraint(coefficients, b, "c" + this.inequalities.size()));
	}

	@Override
	public void addGreaterThanConstraint(double[] coefficients, double b) {
		this.inequalities.add(new LinearBiggerThanEqualsConstraint(coefficients, b, "c" + this.inequalities.size()));
	}

	@Override
	public void addEqualConstraint(double[] coefficients, double b) {
		this.inequalities.add(new LinearEqualsConstraint(coefficients, b, "c" + this.inequalities.size()));
	}

	@Override
	public void maximize() {
		this.operation = Solver.Operation.MAX;
	}

	@Override
	public void minimize() {
		this.operation = Solver.Operation.MIN;
	}

	@Override
	public void setUpperBound(double[] ub) {
		this.ub = ub;
	}

	@Override
	public void setLowerBound(double[] lb) {
		this.lb = lb;
	}

	@Override
	public void integer(int index) {
		this.discrete[index] = true;
	}

	@Override
	public Result solve() {
		LinearProgram lp = new LinearProgram(this.objective);
		if(this.ub != null) {
			lp.setUpperbound(this.ub);
		}
		if(this.lb != null) {
			lp.setLowerbound(this.lb);
		}
		for(int i = 0; i < this.discrete.length; i++) {
			if(this.discrete[i]) {
				lp.setInteger(i);
			}
		}
		this.inequalities.forEach(lp::addConstraint);
		lp.setMinProblem(this.operation.isMin());
		LinearProgramSolver solver = SolverFactory.newDefault();
		double[] values = solver.solve(lp);
		if(values == null) {
			boolean allZero = true;
			for(double d : this.objective) {
				allZero = allZero && d == 0;
			}
			if(!allZero) {
				double[] newObjective = new double[this.objective.length];
				lp.setC(new SparseVector(newObjective));
				if(solver.solve(lp) != null) {
					return new UnboundedResult(this.operation);
				}
			}
			return new InfeasibleResult(this.operation);
		} else {
			return new SimpleResult(values, lp.evaluate(values) + this.constant);
		}
	}
}
