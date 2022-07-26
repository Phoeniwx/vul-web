package com.sjtu.sw.webapp;
import com.sjtu.sw.webapp.flow.ForwardAnalyzer;

import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Transform;
import soot.toolkits.graph.ExceptionalUnitGraph;

import java.util.Map;

public class SootTest {
    public static void main(String[] args) {
        PackManager.v().getPack("jtp").add(
            new Transform("jtp.myTransform", new BodyTransformer() {
                @Override
                protected void internalTransform(Body body, String s, Map<String, String> map) {
                    new ForwardAnalyzer(new ExceptionalUnitGraph(body));
                }
            })
        );
        soot.Main.main(new String[]{ "-v",
            "-process-dir",
            "D:\\Dev\\workspace\\webapp\\temp\\targets",
            "-process-jar-dir", "D:\\Dev\\workspace\\webapp\\target\\webapp-0.0.1-SNAPSHOT\\WEB-INF\\lib",
            "com.sjtu.sw.webapp.controller.FileController"});
    }
}
