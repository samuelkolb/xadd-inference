package solving;

import lpsolve.LP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestingSolver implements LPSolver {
    private final List<LPSolver> solvers;

    public TestingSolver(List<LPSolver> solvers) {
        this.solvers = new ArrayList<>(solvers);
    }

    @Override
    public void setObjective(double[] coefficients, double constant) {
        for(LPSolver solver : solvers) {
            solver.setObjective(Arrays.copyOf(coefficients, coefficients.length), constant);
        }
    }

    @Override
    public void addSmallerThanConstraint(double[] coefficients, double b) {
        for(LPSolver solver : solvers) {
            solver.addSmallerThanConstraint(Arrays.copyOf(coefficients, coefficients.length), b);
        }
    }

    @Override
    public void addGreaterThanConstraint(double[] coefficients, double b) {
        for(LPSolver solver : solvers) {
            solver.addGreaterThanConstraint(Arrays.copyOf(coefficients, coefficients.length), b);
        }
    }

    @Override
    public void addEqualConstraint(double[] coefficients, double b) {
        for(LPSolver solver : solvers) {
            solver.addEqualConstraint(Arrays.copyOf(coefficients, coefficients.length), b);
        }
    }

    @Override
    public void maximize() {
        for(LPSolver solver : solvers) {
            solver.maximize();
        }
    }

    @Override
    public void minimize() {
        for(LPSolver solver : solvers) {
            solver.minimize();
        }
    }

    @Override
    public void setUpperBound(double[] ub) {
        for(LPSolver solver : solvers) {
            solver.setUpperBound(Arrays.copyOf(ub, ub.length));
        }
    }

    @Override
    public void setLowerBound(double[] lb) {
        for(LPSolver solver : solvers) {
            solver.setLowerBound(Arrays.copyOf(lb, lb.length));
        }
    }

    @Override
    public void integer(int index) {
        for(LPSolver solver : solvers) {
            solver.integer(index);
        }
    }

    @Override
    public boolean supportsRedundancyChecking() {
        return solvers.get(0).supportsRedundancyChecking();
    }

    @Override
    public Result solve() {
        List<Result> results = solvers.stream().map(LPSolver::solve).collect(Collectors.toList());
        Result result = results.get(0);
        for(int i = 1; i < results.size(); i++) {
            if(results.get(i).isInfeasible() != result.isInfeasible() ||
                    (Math.abs(results.get(i).getValue() - result.getValue()) > 0.000001)) {
                String controlName = solvers.get(0).getClass().getSimpleName();
                String solverName = solvers.get(i).getClass().getSimpleName();
                System.out.println(solvers.get(i));
                System.out.format("Solver %s obtained a different result (%s) than %s (%s)\n",
                        solverName, results.get(i), controlName, result);
                System.exit(1);
            }
        }
        return result;
    }
}
