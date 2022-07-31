package com.sjtu.sw.webapp.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.*;
import soot.jimple.internal.JIdentityStmt;
import soot.toolkits.graph.DominatorsFinder;
import soot.toolkits.graph.MHGDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//public class ForwardAnalyzer {
//}


public class ForwardAnalyzer extends ForwardFlowAnalysis<Unit, FlowSet<Value> > {
    private static final Logger logger = LoggerFactory.getLogger(ForwardAnalyzer.class);

    private static final FlowSet<Value> EMPTY_SET = new ArraySparseSet<Value>();
    private final Map<Unit, FlowSet<Value> > unitToGenerateSet;
    private Unit taintBegin;
    private FlowSet<Value> taintedValues = new ArraySparseSet<>();

    public ForwardAnalyzer(UnitGraph graph) {
        super(graph);
        this.unitToGenerateSet = new HashMap<Unit, FlowSet<Value> >(graph.size()*2+1, 0.7f);

        DominatorsFinder<Unit> df = new MHGDominatorsFinder<Unit>(graph);

        for (Unit s : graph) {
            boolean found = false;
            for(ValueBox box:s.getUseBoxes()) {
                Value val = box.getValue();
                if (val instanceof ParameterRef && s instanceof JIdentityStmt &&
                    val.getType().toString().equals("org.springframework.web.multipart.MultipartFile")) {
                    found = true;
//                    logger.debug(s + s.getClass().toString());
                }
            }
            if (found) {
                taintBegin = s;
                taintedValues.add(((JIdentityStmt) s).getLeftOp());
                break;
            }

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

    /**
     * IN(Start) is the empty set
     **/
    @Override
    protected FlowSet<Value> newInitialFlow() {
        return EMPTY_SET.clone();
    }


    @Override
    protected FlowSet<Value> entryInitialFlow() {
        return EMPTY_SET.clone();
    }

    /**
     * OUT is the same as IN plus the genSet.
     **/
    @Override
    protected void flowThrough(FlowSet<Value> in, Unit unit, FlowSet<Value> out) {
        if (unit.equals(taintBegin)) {
            logger.debug("Start tainting");
            in.union(taintedValues, out);
            return;
        }
        if (in.isEmpty()) {
            out.clear();
            return;
        }
        in.union(EMPTY_SET, out);
        boolean isTainted = false;
//        logger.debug("Unit: "+unit+unit.getClass());

        for ( ValueBox box:unit.getUseBoxes()) {
            Value val = box.getValue();
            if (hasTaint(val, in)) {
                logger.debug(String.format("Tainted Value: %s %s", val, val.getClass()));
                isTainted = true;
//                break;
            }
        }
        if (isTainted) {
//            logger.debug("Tainted Unit: "+unit+unit.getClass());
            if (unit instanceof DefinitionStmt) {
                Value left = ((DefinitionStmt) unit).getLeftOp();
                out.add(left);
//                logger.debug("Add taint: "+left+left.getClass());
            }
        }

    }

    /**
     * All paths == Intersection.
     **/
    @Override
    protected void merge(FlowSet<Value> in1, FlowSet<Value> in2, FlowSet<Value> out) {
        in1.union(in2, out);
    }

    @Override
    protected void copy(FlowSet<Value> source, FlowSet<Value> dest) {
        source.copy(dest);
    }

    private boolean hasTaint(Value val, FlowSet<Value> taints) {
        if (taints.contains(val)) {
            return true;
        }
        if (val instanceof Expr) {
            for (ValueBox box: val.getUseBoxes()) {
                if (taints.contains(box.getValue())) {
                    return true;
                }
            }
        }

        for (Value t: taints) {
            if (val.toString().contains(t.toString())) {
                return true;
            }
        }
        return false;
    }
}
