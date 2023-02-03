package org.drools.demo.utils;

import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.drools.core.base.ClassObjectType;
import org.drools.core.base.ObjectType;
import org.drools.core.reteoo.InitialFactImpl;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.kiesession.rulebase.InternalKnowledgeBase;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.kie.api.runtime.KieContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScanOTNs {
    private static final Logger LOG = LoggerFactory.getLogger(ScanOTNs.class);

    private ScanOTNs() {
        // only utility methods
    }

    public static Set<DotName> report(KieContainer kContainer) throws Exception {
        Index index;
        try (InputStream input = ScanOTNs.class.getResourceAsStream("/META-INF/jandex.idx")) {
            IndexReader reader = new IndexReader(input);
            index = reader.read();
        }
        Set<Class<?>> otnTypes = new LinkedHashSet<>();

        for (String kbName : kContainer.getKieBaseNames()) {
            InternalKnowledgeBase ikb = (InternalKnowledgeBase) kContainer.getKieBase(kbName);
            for (ObjectTypeNode otn : ikb.getRete().getObjectTypeNodes()) {
                ObjectType ot = otn.getObjectType();
                if (ot instanceof ClassObjectType) {
                    Class<?> otnClazz = ((ClassObjectType) ot).getClassType();
                    LOG.debug("otnClazz to index: {}", otnClazz);
                    if (otnClazz.equals(InitialFactImpl.class)) {
                        // do nothing.
                    } else if (otnClazz.equals(Object.class)) {
                        throw new IllegalArgumentException("TODO: for this exercise the rule cannot just patternmarch on Object");
                    } else {
                        otnTypes.add(otnClazz);
                    }
                } else {
                    throw new IllegalArgumentException("TODO: for this exercise the kbase was not forecasted to use DRL declared types");
                }
            }
        }
        Set<DotName> results = new LinkedHashSet<>();
        for (Class<?> c : otnTypes) {
            results.add(DotName.createSimple(c));
            if (c.isInterface()) {
                results.addAll(index.getAllKnownImplementors(c).stream().map(ClassInfo::name).collect(Collectors.toList()));
            } else {
                results.addAll(index.getAllKnownSubclasses(c).stream().map(ClassInfo::name).collect(Collectors.toList()));
            }
        }
        return results;
    }
}
