
package org.drools.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.drools.demo.model.Measurement;
import org.drools.demo.model.Misurazione;
import org.drools.demo.model.MyImplementation;
import org.drools.demo.model.MyInterface;
import org.drools.demo.utils.ScanOTNs;
import org.jboss.jandex.DotName;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuleTest {
    static final Logger LOG = LoggerFactory.getLogger(RuleTest.class);
    static final KieServices ks = KieServices.get();

    KieContainer kContainer = ks.getKieClasspathContainer();

    @Test
    public void testScanOTNs() throws Exception {
        Set<DotName> report = ScanOTNs.report(kContainer);
        List<String> listOfFQCN = report.stream().map(DotName::toString).collect(Collectors.toList());
        assertThat(listOfFQCN).containsExactlyInAnyOrder(
            Measurement.class.getCanonicalName(),
            Misurazione.class.getCanonicalName(),
            MyInterface.class.getCanonicalName(),
            MyImplementation.class.getCanonicalName());
    }

    @Test
    public void testMeasurementRules() {
        LOG.info("Creating kieBase");
        KieBase kieBase = kContainer.getKieBase();

        LOG.info("There should be rules: ");
        for ( KiePackage kp : kieBase.getKiePackages() ) {
            for (Rule rule : kp.getRules()) {
                LOG.info("kp {} rule {}", kp.toString(), rule.getName());
            }
        }

        LOG.info("Creating kieSession");
        KieSession session = kieBase.newKieSession();

        try {
            LOG.info("Populating globals");
            Set<String> check = new HashSet<String>();
            session.setGlobal("controlSet", check);

            LOG.info("Now running data");

            Measurement mRed = new Measurement("color", "red");
            session.insert(mRed);
            session.fireAllRules();

            Measurement mGreen = new Misurazione("color", "green");
            session.insert(mGreen);
            session.fireAllRules();

            Measurement mBlue = new Measurement("color", "blue");
            session.insert(mBlue);
            session.fireAllRules();

            LOG.info("Final checks");

            assertThat(session.getObjects()).as("Size of object in Working Memory is 3").hasSize(3);
            assertThat(check).contains("red");
            assertThat(check).contains("green");
            assertThat(check).contains("blue");
        } finally {
            session.dispose();
        }
    }

    @Test
    public void testR2() {
        StatelessKieSession kieSession = kContainer.getKieBase().newStatelessKieSession();
        BatchExecutionCommand cmds = CommandFactory.newBatchExecution(Arrays.asList(
            CommandFactory.newInsert(new MyImplementation("asd")),
            CommandFactory.newFireAllRules(),
            CommandFactory.newGetObjects("out")
        ));
        ExecutionResults results = kieSession.execute(cmds);
        Collection<?> outs = (Collection<?>) results.getValue("out");
        assertThat(outs).hasSize(2);
    }
}