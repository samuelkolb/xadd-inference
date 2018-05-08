package solving;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by samuelkolb on 27/06/16.
 *
 * @author Samuel Kolb
 */
@SuppressWarnings("JavaDoc")
public class LPSolverTest {

	public final static double DELTA = 1e-8;
	// public final static List<LPSolver> solvers = Arrays.asList(new GLPKSolver(), new GurobiSolver());
	public final static List<LPSolver> solvers = Arrays.asList(new GurobiSolver());

	@Test
	public void testFeasibleFiniteInequalities() {
		for(LPSolver solver : solvers) {
			solver.setObjective(new double[]{4, 4}, 2);
			solver.addSmallerThanConstraint(new double[]{2, 1}, 3);
			solver.addSmallerThanConstraint(new double[]{1, 2}, 3);
			solver.maximize();
			LPSolver.Result result = solver.solve();
			assertEquals(10, result.getValue(), DELTA);
			assertArrayEquals(new double[]{1, 1}, result.getVariables(), DELTA);
			assertFalse(result.isInfeasible());
			assertFalse(result.isUnbounded());
		}
	}

	@Test
	public void testInfeasibleInequalities() {
		for(LPSolver solver : solvers) {
			solver.setObjective(new double[]{0, 0});
			solver.addSmallerThanConstraint(new double[]{1, 1}, 1);
			solver.addGreaterThanConstraint(new double[]{1, 1}, 2);
			solver.maximize();
			LPSolver.Result result = solver.solve();
			assertTrue(result.isInfeasible());
		}
	}

	@Test
	public void testUnboundedInequalities() {
		for(LPSolver solver : solvers) {
			solver.setObjective(new double[]{1, 1});
			solver.addGreaterThanConstraint(new double[]{1, 1}, 1);
			solver.maximize();
			LPSolver.Result result = solver.solve();
			assertTrue(result.isUnbounded());
		}
	}
}