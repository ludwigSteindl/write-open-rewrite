package com.gepardec.wor.lord.webservicehelper;

import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;

public class ServiceSearch extends JavaIsoVisitor<ExecutionContext> {
    Accumulator acc;
    public ServiceSearch(Accumulator acc) {
        this.acc = acc;
    }

    @Override
    public J.Package visitPackage(J.Package pkg, ExecutionContext executionContext) {
        String packageName = pkg.getPackageName();
        if (!packageName.startsWith("at.sozvers.stp.lgkk.a02")) {
            return pkg;
        }

        if (acc.getServicePackages().contains(packageName)) {
            return pkg;
        }
        acc.addServicePackage(packageName);

        return pkg;
    }
}
