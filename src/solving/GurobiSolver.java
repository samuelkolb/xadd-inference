package solving;

import gurobi.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GurobiSolver implements LPSolver {

    private class Constraint {
        public final double[] coefficients;
        public final char comparator;
        public final double constant;

        private Constraint(double[] coefficients, char comparator, double constant) {
            this.coefficients = coefficients;
            this.comparator = comparator;
            this.constant = constant;
        }

        @Override
        public String toString() {
            return Arrays.toString(coefficients )+ " " + comparator + " " + constant;
        }
    }

    private double[] objective;
    private double constant;
    private int optimizationType = 0;
    private List<Constraint> constraints = new ArrayList<>();
    private double[] ub;
    private double[] lb;
    private char[] types;

    @Override
    public void setObjective(double[] coefficients, double constant) {
        this.objective = coefficients;
        this.constant = constant;
        this.types = new char[coefficients.length];
        Arrays.fill(types, GRB.CONTINUOUS);
    }

    private Solver.Operation getOperation() {
        if(this.optimizationType == 0) {
            throw new RuntimeException("Optimization type not set");
        }
        return this.optimizationType == GRB.MAXIMIZE ? Solver.Operation.MAX : Solver.Operation.MIN;
    }

    @Override
    public void addSmallerThanConstraint(double[] coefficients, double b) {
        this.constraints.add(new Constraint(coefficients, GRB.LESS_EQUAL, b));
    }

    @Override
    public void addGreaterThanConstraint(double[] coefficients, double b) {
        this.constraints.add(new Constraint(coefficients, GRB.GREATER_EQUAL, b));
    }

    @Override
    public void addEqualConstraint(double[] coefficients, double b) {
        this.constraints.add(new Constraint(coefficients, GRB.EQUAL, b));
    }

    @Override
    public void maximize() {
        this.optimizationType = GRB.MAXIMIZE;
    }

    @Override
    public void minimize() {
        this.optimizationType = GRB.MINIMIZE;
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
        this.types[index] = GRB.INTEGER;
    }

    @Override
    public boolean supportsRedundancyChecking() {
        return false;
    }

    @Override
    public Result solve() {
        try {
            // Setup environment and model
            GRBEnv env = new GRBEnv();
            env.set(GRB.IntParam.OutputFlag, 0);

            GRBModel model = new GRBModel(env);

            // Create variables
            GRBVar[] variables = new GRBVar[this.objective.length];
            for(int i = 0; i < variables.length; i++) {
                double lb = this.lb != null ? this.lb[i] : Double.NEGATIVE_INFINITY;
                double ub = this.ub != null ? this.ub[i] : Double.POSITIVE_INFINITY;
                variables[i] = model.addVar(lb, ub, this.objective[i], this.types[i], "v" + i);
            }

            // Set objective (redundancy with variable declarations)
            GRBLinExpr expr = new GRBLinExpr();
            for(int i = 0; i < variables.length; i++) {
                expr.addTerm(this.objective[i], variables[i]);
            }
            expr.addConstant(this.constant);
            model.setObjective(expr, this.optimizationType);

            // Add constraints
            for(int j = 0; j < this.constraints.size(); j++) {
                Constraint constraint = this.constraints.get(j);
                expr = new GRBLinExpr();
                for(int i = 0; i < variables.length; i++) {
                    expr.addTerm(constraint.coefficients[i], variables[i]);
                }
                model.addConstr(expr, constraint.comparator, constraint.constant, "c" + j);

            }

            // Optimize model
            model.optimize();
            Result result;

            if(model.get(GRB.IntAttr.Status) == GRB.OPTIMAL) {
                double value = model.get(GRB.DoubleAttr.ObjVal);
                // System.out.println("Gurobi obtained value " + value);

                double[] variableValues = new double[variables.length];
                for (int i = 0; i < variables.length; i++) {
                    variableValues[i] = variables[i].get(GRB.DoubleAttr.X);
                }
                result = new SimpleResult(variableValues, value);
            } else if(model.get(GRB.IntAttr.Status) == GRB.INFEASIBLE) {
                // System.out.println("Gurobi obtained infeasibility");
                result = new InfeasibleResult(getOperation());
            } else if(model.get(GRB.IntAttr.Status) == GRB.UNBOUNDED) {
                // System.out.println("Gurobi obtained unboundedness");
                result = new UnboundedResult(getOperation());
            } else if(model.get(GRB.IntAttr.Status) == GRB.INF_OR_UNBD) {
                model.set(GRB.IntParam.DualReductions, 0);
                model.optimize();

                if(model.get(GRB.IntAttr.Status) == GRB.INFEASIBLE) {
                    // System.out.println("Gurobi obtained infeasibility (second run)");
                    result = new InfeasibleResult(getOperation());
                } else if(model.get(GRB.IntAttr.Status) == GRB.UNBOUNDED) {
                    // System.out.println("Gurobi obtained unboundedness (second run)");
                    result = new UnboundedResult(getOperation());
                } else {
                    throw new RuntimeException("Unexpected status (recomputed) " + model.get(GRB.IntAttr.Status));
                }
            } else {
                throw new RuntimeException("Unexpected status " + model.get(GRB.IntAttr.Status));
            }

            // Dispose of model and environment
            model.dispose();
            env.dispose();

            return result;
        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder(
                ((getOperation().isMax() ? "MAX" : "MIN") + Arrays.toString(objective) + " + " + constant) + "\n"
                + "UB bounds: " + Arrays.toString(ub) + "\n"
                + "LB bounds: " + Arrays.toString(lb) + "\n");
        for(Constraint constraint : constraints) {
            res.append(constraint).append("\n");
        }
        return res.toString();
    }
}
