package hgm.poly.pref;

import hgm.poly.ConstrainedPolynomial;
import hgm.poly.integral.OneDimFunction;
import hgm.poly.integral.SymbolicMultiDimPolynomialIntegral;
import hgm.poly.integral.SymbolicOneDimFunctionGenerator;

import java.util.*;

/**
 * Created by Hadi Afshar.
 * Date: 4/04/14
 * Time: 12:25 AM
 *
 * Pre-calculates the integrals...
 *
 * //todo currently does not work properly....DEBUG.....
 */
public class SymbolicGibbsPolytopesSampler extends AbstractPolytopesSampler {
    public static SymbolicGibbsPolytopesSampler makeSampler(PosteriorHandler gph, double minForAllVars, double maxForAllVars, Double[] reusableInitialSample) {
        int varNum = gph.getPolynomialFactory().getAllVars().length;
        double[] cVarMins = new double[varNum];
        double[] cVarMaxes = new double[varNum];
        Arrays.fill(cVarMins, minForAllVars);
        Arrays.fill(cVarMaxes, maxForAllVars);
        return new SymbolicGibbsPolytopesSampler(gph, cVarMins, cVarMaxes, reusableInitialSample);
    }

    Map<String/*var*/, SymbolicCDFListHandler> varToSymbolicIntegralMap;

    public SymbolicGibbsPolytopesSampler(PosteriorHandler gph, double[] cVarMins, double[] cVarMaxes, Double[] reusableInitialSample) {
        super(gph, cVarMins, cVarMaxes, reusableInitialSample);

        //make a map from each feature-var to its symbolic integration (i.e. other vars remain symbolic):
        SymbolicMultiDimPolynomialIntegral symbolicIntegrator = new SymbolicMultiDimPolynomialIntegral();
        String[] allVars = gph.getPolynomialFactory().getAllVars();

        ConstrainedPolynomial[] caseStatements = makeExplicitCaseStatements(gph);


        varToSymbolicIntegralMap = new HashMap<String, SymbolicCDFListHandler>(allVars.length);
        for (String var : allVars) {
            SymbolicOneDimFunctionGenerator[] varCdfGenerators = new SymbolicOneDimFunctionGenerator[caseStatements.length];
            for (int i = 0; i < caseStatements.length; i++) {
                ConstrainedPolynomial caseStatement = caseStatements[i];
                SymbolicOneDimFunctionGenerator symbolicVarCdf = symbolicIntegrator.integrate(caseStatement, var);
                varCdfGenerators[i] = symbolicVarCdf;
            }
            varToSymbolicIntegralMap.put(var, new SymbolicCDFListHandler(varCdfGenerators));
        }
    }

    private ConstrainedPolynomial[] makeExplicitCaseStatements(PosteriorHandler gph) {
        int n = gph.numberOfConstraints();
        List<Boolean> gateMask = new ArrayList<Boolean>(n);
        for (int i = 0; i < n; i++) {
            gateMask.add(null);
        }

        int two2n = (int) Math.pow(2, n);
        final ConstrainedPolynomial[] caseStatements = new ConstrainedPolynomial[two2n];

        for (int i = 0; i < two2n; i++) {
            int ii = i;
            for (int j = 0; j < n; j++) {
                gateMask.set(j, (ii % 2 != 0));
                ii >>= 1;
            }
            caseStatements[i] = gph.makePolytope(gateMask);
        }

        return caseStatements;
    }

    @Override
    protected void sampleSingleContinuousVar(String varToBeSampled, int varIndexToBeSampled, Double[] reusableVarAssign) throws FatalSamplingException {
        double maxVarValue = cVarMaxes[varIndexToBeSampled];
        double minVarValue = cVarMins[varIndexToBeSampled];

        SymbolicCDFListHandler symbolicCDFListHandler = varToSymbolicIntegralMap.get(varToBeSampled);
        OneDimFunction varCDF = symbolicCDFListHandler.instantiate(reusableVarAssign);

        double s = takeSampleFrom1DFunc(varCDF, minVarValue, maxVarValue);

        // here the sample is stored....
        reusableVarAssign[varIndexToBeSampled] = s;
    }

}

class SymbolicCDFListHandler {
    SymbolicOneDimFunctionGenerator[] generators;
    final OneDimFunction[] reusableInstantiatedFunctions;

    SymbolicCDFListHandler(SymbolicOneDimFunctionGenerator[] generators) {
        this.generators = generators;
        reusableInstantiatedFunctions = new OneDimFunction[generators.length];
    }

    //note that each instantiation changes the result of the former due to reused object
    public OneDimFunction instantiate(Double[] varAssign) {
        for (int i = 0; i < generators.length; i++) {
            SymbolicOneDimFunctionGenerator generator = generators[i];
            OneDimFunction segmentCdf = generator.makeFunction(varAssign);
//            if (!segmentCdf.equals(OneDimFunction.ZERO_1D_FUNCTION)) {
            reusableInstantiatedFunctions[i] = segmentCdf;
        }

        return new OneDimFunction() {
            @Override
            public double eval(double var) {
                double result = 0d;
                for (OneDimFunction polyCDF : reusableInstantiatedFunctions) {
                    result += polyCDF.eval(var);
                }

                return result;
            }
        };
    }
}

