package scenario;

import diagram.XADDBuild;
import diagram.XADDiagram;
import org.junit.Test;
import xadd.XADD;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import static diagram.XADDBuild.test;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.*;

/**
 * Created by samuelkolb on 23/03/16.
 *
 * @author Samuel Kolb
 */
@SuppressWarnings("JavaDoc")
public class Example3 {

	public final String EXAMPLE =
			"T	1	0	null\n" +
			"T	2	1	null\n" +
			"E	5	(1 * y) > 0\n" +
			"I	17	5	1	2\n" +
			"E	4	(1 + (-1 * x) + (-1 * y)) > 0\n" +
			"I	19	4	1	17\n" +
			"E	3	(1 + (-1 * y)) > 0\n" +
			"I	27	3	19	2\n" +
			"E	2	((-1 * x) + (1 * y)) > 0\n" +
			"I	28	2	19	27\n" +
			"E	1	(1 * x) > 0\n" +
			"I	29	1	1	28\n" +
			"F	7	5	(#nodes and #decisions)\n";


	private final static XADDiagram
			theory = /*bool("a").and(*/test("x > 0").and(test("x < y").and(test("y < 1")))//)
		//theory = test("x > 0").and(test("y > 0")).and(test("x < 1")).and(test("y < 1")).and(test("x > 0").and(test("x < y").and(test("y < 1")))
			.or(test("x + y < 1").and(test("x > 0")).and(test("y > 0")));//);

	@Test
	public void testOldStyle() throws Exception {
		String part1 =
				"([x > 0]" +
						"	([x < y]" +
						"		([y < 1]" +
						"			([1])" +
						"			([0])" +
						"		)" +
						"		([0])" +
						"	)" +
						"	([0])" +
						")";

		String part2 =
				"([x + y < 1]" +
						"	([x > 0]" +
						"		([y > 0]" +
						"			([1])" +
						"			([0])" +
						"		)" +
						"		([0])" +
						"	)" +
						"	([0])" +
						")";

		String one = "([1])";

		{
			XADD xadd = new XADD();
			{
				int dd1 = xadd.buildCanonicalXADDFromString(part1);
				int dd2 = xadd.buildCanonicalXADDFromString(part2);

				// Take the "OR" for the two diagrams
				int dd = xadd.apply(xadd.apply(dd1, dd2, XADD.SUM), xadd.buildCanonicalXADDFromString(one), XADD.MIN);
				xadd.getGraph(dd).genDotFile("original_old_style.dot");
				xadd.exportXADDToFile(dd, "original_old_style.txt");

				int dd_y = xadd.computeDefiniteIntegral(dd, "y");
				int dd_yx = xadd.computeDefiniteIntegral(dd_y, "x");
				System.out.println("Original YX: " + xadd.evaluate(dd_yx, new HashMap<>(), new HashMap<>()));

				int dd_x = xadd.computeDefiniteIntegral(dd, "x");
				int dd_xy = xadd.computeDefiniteIntegral(dd_x, "y");
				System.out.println("Original XY: " + xadd.evaluate(dd_xy, new HashMap<>(), new HashMap<>()));
			}
			{
				int imported = xadd.importXADDFromFile("original_old_style.txt");
				int imported_y = xadd.computeDefiniteIntegral(imported, "y");
				int imported_yx = xadd.computeDefiniteIntegral(imported_y, "x");
				System.out.println("Imported YX: " + xadd.evaluate(imported_yx, new HashMap<>(), new HashMap<>()));

				int imported_x = xadd.computeDefiniteIntegral(imported, "x");
				int imported_xy = xadd.computeDefiniteIntegral(imported_x, "y");
				System.out.println("Imported XY: " + xadd.evaluate(imported_xy, new HashMap<>(), new HashMap<>()));
			}
		}
		{
			XADD xadd = new XADD();
			int imported = xadd.importXADDFromFile("original_old_style.txt");
			xadd.getGraph(imported).genDotFile("imported_old_style.dot");
			int imported_y = xadd.computeDefiniteIntegral(imported, "y");
			int imported_yx = xadd.computeDefiniteIntegral(imported_y, "x");
			System.out.println("Imported (fresh) YX: " + xadd.evaluate(imported_yx, new HashMap<>(), new HashMap<>()));

			int imported_x = xadd.computeDefiniteIntegral(imported, "x");
			int imported_xy = xadd.computeDefiniteIntegral(imported_x, "y");
			System.out.println("Imported (fresh) XY: " + xadd.evaluate(imported_xy, new HashMap<>(), new HashMap<>()));
		}
	}

	@Test
	public void testIntegrationXY() {
		theory.export("test.txt");
		// Gives 1.0
		theory.exportImage("original.dot");
		assertEquals(0.75, theory.integrate(emptyList(), asList("x", "y")), Example2.DELTA);
	}

	@Test
	public void testIntegrationYX() {
		// Gives 0.75
		assertEquals(0.75, theory.integrate(emptyList(), asList("y", "x")), Example2.DELTA);
	}

	@Test
	public void testDirectIntegrationXY() throws Exception {
		XADD xadd = new XADD();
		int dd = xadd.importXADD(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(EXAMPLE.getBytes()))));
		xadd.getGraph(dd).genDotFile("imported.dot");
		int dd_x = xadd.computeDefiniteIntegral(dd, "x");
		int dd_xy = xadd.computeDefiniteIntegral(dd_x, "y");
		// Gives 1.25
		assertEquals(0.75, xadd.evaluate(dd_xy, new HashMap<>(), new HashMap<>()), Example2.DELTA);
	}

	@Test
	public void testDirectIntegrationYX() throws Exception {
		XADD xadd = new XADD();
		int dd = xadd.importXADD(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(EXAMPLE.getBytes()))));

		//XADDiagram diagram = XADDBuild.test("x < 0").assignWeights(XADDBuild.val("2*x + 5"), XADDBuild.val("3*x + 6"));
		//diagram.export("expressions.txt");

		int dd_y = xadd.computeDefiniteIntegral(dd, "y");
		int dd_yx = xadd.computeDefiniteIntegral(dd_y, "x");
		// Gives 0.75
		assertEquals(0.75, xadd.evaluate(dd_yx, new HashMap<>(), new HashMap<>()), Example2.DELTA);
	}
}
