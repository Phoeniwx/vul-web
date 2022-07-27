package com.sjtu.sw.webapp;
import com.sjtu.sw.webapp.flow.ForwardAnalyzer;

import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Transform;
import soot.toolkits.graph.ExceptionalUnitGraph;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

public class SootTest {
    public static void main(String[] args) {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader) cl).getURLs();
        StringBuilder sb = new StringBuilder();
        for (URL url : urls) {
            String jarpath = url.getFile().substring(1).replaceAll("%20", " ");
            sb.append(jarpath).append(';');
        }
        String classPath = sb.toString();
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
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
        soot.Main.main(new String[]{ "-v", "-cp", classPath,
            "-process-dir",
            "target/webapp-0.0.1-SNAPSHOT/WEB-INF/classes",
            "-process-jar-dir", "target/webapp-0.0.1-SNAPSHOT/WEB-INF/lib",
            "-dump-cfg", "wjtp",
            "com.sjtu.sw.webapp.controller.FileController"});
    }
}
