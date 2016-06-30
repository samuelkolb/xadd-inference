package diagram;

import pair.TypePair;
import solving.Converter;
import variables.IntegerVar;
import xadd.ExprLib;
import xadd.XADD;

import java.util.HashMap;
import java.util.List;

import static function.Functional.map;

/**
 * Created by samuelkolb on 29/06/16.
 *
 * @author Samuel Kolb
 */
public class Summation implements XADDiagram.NodeWalkerObserver<XADDPath, Double> {

	public static final double DELTA = 1e-8;
	private final List<IntegerVar> variables;

	/**
	 * Creates a summation operation with a set of integer variables.
	 * @param variables	The variables to sum over
	 */
	public Summation(List<IntegerVar> variables) {
		this.variables = variables;
	}

	@Override
	public XADDPath getInitial() {
		return new XADDPath();
	}

	@Override
	public Double calculate(int nodeId, XADD.XADDTNode node, XADDPath state) {
		return sum(this.variables, node._expr, state);
	}

	@Override
	public TypePair<XADDPath> update(int nodeId, XADD.XADDINode node, XADDPath state) {
		return TypePair.make(state.extend(node, false), state.extend(node, true));
	}

	@Override
	public Double combine(int nodeId, XADD.XADDINode node, Double result1, Double result2) {
		return result1 + result2;
	}

	/**
	 * Sum over the given expression for the given variables, taking into account the bounds given by the path.
	 * @param variables	The variables to sum over
	 * @param expression	The expression to sum over
	 * @param path	The path
	 * @return	The total sum
	 */
	public static Double sum(List<IntegerVar> variables, ExprLib.ArithExpr expression, XADDPath path) {
		if(expression instanceof ExprLib.DoubleExpr && ((ExprLib.DoubleExpr) expression)._dConstVal == 0) {
			System.out.println(path + "(zero): " + 0);
			return 0.0;
		}
		List<IntegerVar> vars;
		try {
			vars = map(v -> addBounds(v, path), variables);
		} catch(IntegerVar.InconsistentBoundsException e) {
			System.out.println(path + "(inc) : " + 0);
			return 0.0;
		}
		Converter converter = new Converter(map(IntegerVar::getName, vars));
		double[] coefficients = converter.getLinearCoefficients(expression);
		double constant = converter.getLinearConstant(expression);
		for(int i = 0; i < vars.size(); i++) {
			IntegerVar var = vars.get(i);
			double boundsSize = var.getUpperBound() - var.getLowerBound() + 1;
			constant = constant * boundsSize;
			for(int j = i; j < vars.size(); j++) {
				if(coefficients[j] != 0) {
					if(i == j) {
						int boundsSum = var.getLowerBound() + var.getUpperBound();
						int negativeSize = var.getLowerBound() - var.getUpperBound() - 1;
						//noinspection MagicNumber
						constant += -0.5 * negativeSize * boundsSum * coefficients[j];
						coefficients[j] = 0;
					} else {
						coefficients[j] = coefficients[j] * boundsSize;
					}
				}
			}
		}
		System.out.println(path + ": " + constant);
		return constant;
	}

	private static IntegerVar addBounds(IntegerVar var, XADDPath path) {
		for(ExprLib.CompExpr comparison : path.getDecisions()) {
			double constant = ((ExprLib.DoubleExpr) comparison._rhs)._dConstVal;
			ExprLib.CompOperation operation = comparison._type;
			ExprLib.ArithExpr lhs = comparison._lhs;
			ExprLib.CoefExprPair coefExprPair = lhs.removeVarFromExpr(var.getName());
			Double constantLeft = coefExprPair._expr.evaluate(new HashMap<>());
			constant -= constantLeft;
			constant /= coefExprPair._coef;
			if(coefExprPair._coef < 0) {
				if(operation.equals(ExprLib.CompOperation.GT_EQ)) {
					operation = ExprLib.CompOperation.LT_EQ;
				} else if(operation.equals(ExprLib.CompOperation.GT)) {
					operation = ExprLib.CompOperation.LT;
				} else if(operation.equals(ExprLib.CompOperation.LT_EQ)) {
					operation = ExprLib.CompOperation.GT_EQ;
				} else if(operation.equals(ExprLib.CompOperation.LT)) {
					operation = ExprLib.CompOperation.GT;
				}
			}
			if(operation.equals(ExprLib.CompOperation.GT)) {
				constant += 1;
				operation = ExprLib.CompOperation.GT_EQ;
			} else if(operation.equals(ExprLib.CompOperation.LT)) {
				constant -= 1;
				operation = ExprLib.CompOperation.LT_EQ;
			}
			int intConstant;
			if(operation.equals(ExprLib.CompOperation.GT_EQ)) {
				intConstant = (int) Math.ceil(constant);
				System.out.println(comparison + ", lower bound: " + intConstant);
				var = var.addLowerBound(intConstant);
			} else if(operation.equals(ExprLib.CompOperation.LT_EQ)) {
				intConstant = (int) Math.floor(constant);
				System.out.println(comparison + ", upper bound: " + intConstant);
				var = var.addUpperBound(intConstant);
			} else if(operation.equals(ExprLib.CompOperation.EQ)) {
				if(Math.abs(constant % 1) < DELTA) {
					intConstant = (int) Math.round(constant);
					var = var.addBounds(intConstant, intConstant);
				} else {
					throw new IllegalStateException("Integer variable must be equal to integer number");
				}
			} else {
				throw new IllegalStateException("Unexpected comparison: " + operation);
			}
		}
		return var;
	}
}
