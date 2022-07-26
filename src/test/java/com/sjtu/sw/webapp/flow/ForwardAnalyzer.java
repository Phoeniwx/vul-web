package com.sjtu.sw.webapp.flow;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.toolkits.graph.DominatorsFinder;
import soot.toolkits.graph.MHGDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.HashMap;
import java.util.Map;

//public class ForwardAnalyzer {
//}

public class ForwardAnalyzer extends ForwardFlowAnalysis<Unit, FlowSet<Value> > {
    private static final FlowSet<Value> EMPTY_SET = new ArraySparseSet<Value>();
    private final Map<Unit, FlowSet<Value> > unitToGenerateSet;

    public ForwardAnalyzer(UnitGraph graph) {
        super(graph);
        this.unitToGenerateSet = new HashMap<Unit, FlowSet<Value> >(graph.size()*2+1, 0.7f);

        DominatorsFinder<Unit> df = new MHGDominatorsFinder<Unit>(graph);

        for (Unit s : graph) {
            FlowSet<Value> genSet = EMPTY_SET.clone();
            for (Unit dom : df.getDominators(s)) {
                for (ValueBox box : dom.getDefBoxes()) {
                    Value val = box.getValue();
                    if (val instanceof Local) {
                        genSet.add(val, genSet);
                    }
                }
            }
            this.unitToGenerateSet.put(s, genSet);
        }

        doAnalysis();
    }

    @Override
    protected FlowSet<Value> newInitialFlow() {
        return EMPTY_SET.clone();
    }

    /**
     * IN(Start) is the empty set
     **/
    @Override
    protected FlowSet<Value> entryInitialFlow() {
        return EMPTY_SET.clone();
    }

    /**
     * OUT is the same as IN plus the genSet.
     **/
    @Override
    protected void flowThrough(FlowSet<Value> in, Unit unit, FlowSet<Value> out) {
        // perform generation (kill set is empty)
        in.union(unitToGenerateSet.get(unit), out);
    }

    /**
     * All paths == Intersection.
     **/
    @Override
    protected void merge(FlowSet<Value> in1, FlowSet<Value> in2, FlowSet<Value> out) {
        in1.intersection(in2, out);
    }

    @Override
    protected void copy(FlowSet<Value> source, FlowSet<Value> dest) {
        source.copy(dest);
    }
}
