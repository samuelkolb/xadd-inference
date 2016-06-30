package variables;

/**
 * Represents a potentially bounded integer variable.
 *
 * @author Samuel Kolb
 */
public class IntegerVar {

	public static class InconsistentBoundsException extends RuntimeException {

		public final String name;

		/**
		 * Creates an inconsistent bounds exception.
		 * @param name	The name of the variable
		 */
		public InconsistentBoundsException(String name) {
			super("Inconsistent bounds for " + name);
			this.name = name;
		}
	}

	private final String name;
	private final int lowerBound;
	private final boolean lowerBounded;
	private final int upperBound;
	private final boolean upperBounded;

	public String getName() {
		return name;
	}

	public boolean isLowerBounded() {
		return lowerBounded;
	}

	public int getLowerBound() {
		if(!isLowerBounded()) {
			throw new UnsupportedOperationException("No lower bound set.");
		}
		return lowerBound;
	}

	public boolean isUpperBounded() {
		return upperBounded;
	}

	public int getUpperBound() {
		if(!isUpperBounded()) {
			throw new UnsupportedOperationException("No upper bound set.");
		}
		return upperBound;
	}

	/**
	 * Constructs an unbounded variable.
	 * @param name	The name of the variable
	 */
	public IntegerVar(String name) {
		this(name, 0, false, 0, false);
	}

	/**
	 * Elaborate constructor.
	 * @param name	The variable name
	 * @param lowerBound	The lower bound
	 * @param lowerBounded	Whether a lower bound exists
	 * @param upperBound	The upper bound
	 * @param upperBounded	Whether an upper bound exists
	 */
	public IntegerVar(String name, int lowerBound, boolean lowerBounded, int upperBound, boolean upperBounded) {
		this.name = name;
		System.out.println(lowerBound + ":" + upperBound);
		if(lowerBounded && upperBounded && lowerBound > upperBound) {
			throw new InconsistentBoundsException(name);
		}
		this.lowerBound = lowerBound;
		this.lowerBounded = lowerBounded;
		this.upperBound = upperBound;
		this.upperBounded = upperBounded;
	}

	/**
	 * Add a lower bounds.
	 * @param lowerBound	The lower bound
	 * @return	A bounded version of this variable
	 */
	public IntegerVar addLowerBound(int lowerBound) {
		int newLowerBound = this.lowerBounded ? Math.max(lowerBound, this.lowerBound) : lowerBound;
		return new IntegerVar(this.name, newLowerBound, true, this.upperBound, this.upperBounded);
	}

	/**
	 * Add an upper bound.
	 * @param upperBound	The upper bound
	 * @return	A bounded version of this variable
	 */
	public IntegerVar addUpperBound(int upperBound) {
		int newUpperBound = this.upperBounded ? Math.min(upperBound, this.upperBound) : upperBound;
		return new IntegerVar(this.name, this.lowerBound, this.lowerBounded, newUpperBound, true);
	}

	/**
	 * Add lower and upper bounds.
	 * @param lowerBound	The lower bound
	 * @param upperBound	The upper bound
	 * @return	A bounded version of this variable
	 */
	public IntegerVar addBounds(int lowerBound, int upperBound) {
		int newLowerBound = this.lowerBounded ? Math.max(lowerBound, this.lowerBound) : lowerBound;
		int newUpperBound = this.upperBounded ? Math.max(upperBound, this.upperBound) : upperBound;
		return new IntegerVar(this.name, newLowerBound, true, newUpperBound, true);
	}

	@Override
	public String toString() {
		return getName() + " in [" + (this.lowerBounded ? this.lowerBound : "-inf") + ", " +
				(this.upperBounded ? this.upperBound : "inf") + "]";
	}
}
