package com.sjtu.sw.webapp;
import com.sjtu.sw.webapp.flow.ForwardAnalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Transform;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.tools.CFGViewer;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

public class SootTest {
    private static final Logger logger = LoggerFactory.getLogger(SootTest.class);

    public static void main(String[] args) {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader) cl).getURLs();
        StringBuilder sb = new StringBuilder();
        for (URL url : urls) {
            String jarpath = url.getFile().substring(1).replaceAll("%20", " ");
            sb.append(jarpath).append(';');
        }
        String classPath = sb.toString();
        CFGViewer viewer = new CFGViewer();
        Transform cfgTransform = new Transform("jtp.printcfg", viewer);
        cfgTransform.setDeclaredOptions("enabled alt-class-path graph-type ir multipages brief ");
        cfgTransform.setDefaultOptions("enabled alt-class-path: graph-type:BriefUnitGraph ir:jimple multipages:false  brief:false ");
        PackManager.v().getPack("jtp").add(cfgTransform);
        PackManager.v().getPack("jtp").add(
            new Transform("jtp.myTransform", new BodyTransformer() {
                @Override
                protected void internalTransform(Body body, String s, Map<String, String> map) {
                    new ForwardAnalyzer(new ExceptionalUnitGraph(body));
                }
            })
        );
//        soot.tools.CFGViewer.main(new String[]{ "-v", "-cp", classPath,
//            "-process-dir",
//            "target/webapp-0.0.1-SNAPSHOT/WEB-INF/classes",
//            "-process-jar-dir", "target/webapp-0.0.1-SNAPSHOT/WEB-INF/lib",
//            "com.sjtu.sw.webapp.controller.FileController"});
        logger.debug(classPath);
        soot.Main.main(new String[]{ "-cp", classPath,
            "-process-dir",
            "src/main/java",
//            "-process-jar-dir", "target/webapp-0.0.1-SNAPSHOT/WEB-INF/lib",
//            "-f", "J", "-v",
            "com.sjtu.sw.webapp.controller.FileController"});
    }
}
