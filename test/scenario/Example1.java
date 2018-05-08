package scenario;

import diagram.XADDBuild;
import diagram.XADDiagram;
import org.junit.Test;

import static diagram.XADDBuild.ifThenElse;
import static diagram.XADDBuild.test;
import static diagram.XADDBuild.val;
import static scenario.Example4.x;

/**
 * Created by samuelkolb on 15/03/16.
 *
 * @author Samuel Kolb
 */
public class Example1 {

	@Test
	public void testSum() throws Exception {
		XADDiagram x5 = ifThenElse("x < 5", val("x"), val(0));
		XADDiagram xadd = ifThenElse("x >= 0", x5, ifThenElse("x + 2 >= 0", val("x"), ifThenElse("x + 1 >= 0", x5, val(0))));
		xadd.exportImage("py-dd.dot");
		xadd.plus(xadd).exportImage("py-dd-sum.dot");
	}

	@Test
	public void testConstruction() throws Exception {
		/*
		x_qeq_zero = pool.bool_test(Test("x", ">="))
        x_eq_minus_two = pool.bool_test(Test("x + 2", "="))
        x_leq_minus_1 = pool.bool_test(Test("x + 1", "<="))
        x_leq_five = pool.bool_test(Test("x - 5", "<="))

        p1 = pool.apply(Multiplication, x_qeq_zero, x_leq_five)
        export(Diagram(pool, p1), "p1.dot")

        x_qeq_zero_i = pool.invert(x_qeq_zero)
        export(Diagram(pool, x_qeq_zero_i), "x_qeq_zero_i.dot")
        p2 = pool.apply(Multiplication, x_qeq_zero_i, x_eq_minus_two)

		 */
		XADDiagram p1 = test("x >= 0").and(test("x <= 5"));
		XADDiagram p2 = test("x >= 0").not().and(test("x + 2 >= 0"));
		p1.exportImage("p1.dot");
		p2.exportImage("p2.dot");
		p1.plus(p2).exportImage("construct.dot");
	}
}
