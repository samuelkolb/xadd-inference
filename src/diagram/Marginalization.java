package diagram;

import pair.TypePair;
import xadd.ExprLib;
import xadd.XADD;

import java.util.Map;
import java.util.Set;

/**
 * Created by samuelkolb on 29/06/16.
 *
 * @author Samuel Kolb
 */
public class Marginalization implements XADDiagram.NodeWalkerObserver<Marginalization.State, XADDiagram> {

	protected class State {
		final Map<String, TypePair<Integer>> domains;

		public State(Map<String, TypePair<Integer>> domains) {
			this.domains = domains;
		}
	}

	private final Map<String, TypePair<Integer>> domains;
	private final Set<String> marginalize;

	/**
	 * Construct the marginalization operation
	 * @param domains    The domains of the variables
	 * @param marginalize	The variables to marginalize
	 */
	public Marginalization(Map<String, TypePair<Integer>> domains, Set<String> marginalize) {
		this.domains = domains;
		this.marginalize = marginalize;
	}

	@Override
	public State getInitial() {
		return new State(domains);
	}

	@Override
	public XADDiagram calculate(int nodeId, XADD.XADDTNode node, State state) {
		return compute(node._expr, state.domains);
	}

	@Override
	public TypePair<State> update(int nodeId, XADD.XADDINode node, State state) {
		return null;
	}

	@Override
	public XADDiagram combine(int nodeId, XADD.XADDINode node, XADDiagram result1, XADDiagram result2) {
		return null;
	}

	protected XADDiagram compute(ExprLib.ArithExpr _expr, Map<String, TypePair<Integer>> domains) {
		return XADDBuild.val(_expr.toString());
	}
}
