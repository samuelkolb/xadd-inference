package solving;

import java.util.Arrays;

/**
 * Created by samuelkolb on 27/06/16.
 *
 * @author Samuel Kolb
 */
public interface LPSolver {

	interface Result {
		double[] getVariables();
		double getValue();
		boolean isInfeasible();
		boolean isUnbounded();
	}

	class SimpleResult implements Result {
		public final double[] variables;
		public final double value;

		/**
		 * Creates a result
		 * @param variables	The variables
		 * @param value	The value
		 */
		public SimpleResult(double[] variables, double value) {
			this.variables = variables;
			this.value = value;
		}

		@Override
		public double[] getVariables() {
			return this.variables;
		}

		@Override
		public double getValue() {
			return this.value;
		}

		@Override
		public boolean isInfeasible() {
			return false;
		}

		@Override
		public boolean isUnbounded() {
			return false;
		}

		@Override
		public String toString() {
			return Arrays.toString(getVariables()) + ": " + getValue();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			SimpleResult that = (SimpleResult) o;

			if (Double.compare(that.value, value) != 0) return false;
			return Arrays.equals(variables, that.variables);
		}

		@Override
		public int hashCode() {
			int result;
			long temp;
			result = Arrays.hashCode(variables);
			temp = Double.doubleToLongBits(value);
			result = 31 * result + (int) (temp ^ (temp >>> 32));
			return result;
		}
	}

	class InfeasibleResult implements Result {
		public final boolean maximize;

		/**
		 * Initialize an infeasible result
		 * @param operation	The operation
		 */
		public InfeasibleResult(Solver.Operation operation) {
			this.maximize = operation.isMax();
		}

		@Override
		public double[] getVariables() {
			return null;
		}

		@Override
		public double getValue() {
			return maximize ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
		}

		@Override
		public boolean isInfeasible() {
			return true;
		}

		@Override
		public boolean isUnbounded() {
			return false;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			InfeasibleResult that = (InfeasibleResult) o;

			return maximize == that.maximize;
		}

		@Override
		public int hashCode() {
			return (maximize ? 1 : 0);
		}

		@Override
		public String toString() {
			return "Infeasible solution (" + (maximize ? "MAX" : "MIN") + ")";
		}
	}

	class UnboundedResult implements Result {
		public final boolean maximize;

		/**
		 * Initialize an unbounded result
		 * @param operation	The operation
		 */
		public UnboundedResult(Solver.Operation operation) {
			this.maximize = operation.isMax();
		}

		@Override
		public double[] getVariables() {
			return null;
		}

		@Override
		public double getValue() {
			return maximize ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
		}

		@Override
		public boolean isInfeasible() {
			return false;
		}

		@Override
		public boolean isUnbounded() {
			return true;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			UnboundedResult that = (UnboundedResult) o;

			return maximize == that.maximize;
		}

		@Override
		public int hashCode() {
			return (maximize ? 1 : 0);
		}

		@Override
		public String toString() {
			return "Unbounded result (" + (maximize ? "MAX" : "MIN") + ")";
		}
	}

	/**
	 * Sets the objective of a linear solver with a constant term of 0
	 * @param coefficients	The coefficients of the decision variables
	 */
	default void setObjective(double[] coefficients) {
		setObjective(coefficients, 0);
	}

	/**
	 * Sets the objective of a linear solver
	 * @param coefficients	The coefficients of the decision variables
	 * @param constant	The constant term
	 */
	void setObjective(double[] coefficients, double constant);

	/**
	 * Adds a constraint (coefficients * x <= b)
	 * @param coefficients	The coefficients of the decision variables
	 * @param b	The bound
	 */
	void addSmallerThanConstraint(double[] coefficients, double b);

	/**
	 * Adds a constraint (coefficients * x >= b)
	 * @param coefficients	The coefficients of the decision variables
	 * @param b	The bound
	 */
	void addGreaterThanConstraint(double[] coefficients, double b);

	/**
	 * Adds a constraint (coefficients * x = b)
	 * @param coefficients	The coefficients of the decision variables
	 * @param b	The bound
	 */
	void addEqualConstraint(double[] coefficients, double b);

	/**
	 * Sets the LP solver to maximize
	 */
	void maximize();

	/**
	 * Sets the LP solver to minimize
	 */
	void minimize();

	void setUpperBound(double[] ub);

	void setLowerBound(double[] lb);

	/**
	 * Set a variable to be integer (instead of continuous)
	 * @param index	The index of the integer variable
	 */
	void integer(int index);

	/**
	 * Returns if the solver supports redundancy checking
	 * @return True iff the solver supports redundancy checking (with slack checks)
	 */
	boolean supportsRedundancyChecking();

	/**
	 * Solves the LP and returns the optimal solution
	 * @return	The optimal values for the decision variables
	 */
	Result solve();

	static LPSolver getDefault() {
		return new GurobiSolver();
		// return new TestingSolver(Arrays.asList(new GLPKSolver(), new GurobiSolver()));
	}
}
