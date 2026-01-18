package lv.lu.eztf.dn.combopt.evrp;

import java.util.List;

import ai.timefold.solver.benchmark.api.PlannerBenchmark;
import ai.timefold.solver.benchmark.api.PlannerBenchmarkFactory;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;
import lombok.extern.slf4j.Slf4j;
import lv.lu.eztf.dn.combopt.evrp.domain.Company;
import lv.lu.eztf.dn.combopt.evrp.domain.EVRPsolution;
import lv.lu.eztf.dn.combopt.evrp.domain.Gate;
import lv.lu.eztf.dn.combopt.evrp.domain.Plane;
import lv.lu.eztf.dn.combopt.evrp.domain.Terminal;
import lv.lu.eztf.dn.combopt.evrp.domain.Visit;
import lv.lu.eztf.dn.combopt.evrp.solver.ConstraintStreamCostFunction;

@Slf4j
public class EVRPapp {
    public static void main(String[] args) {
        runSolvers();
        // runBenchmarker();
        // generateData();
    }

    private static void runBenchmarker() {
        //EVRPsolution problem = createExample();
        PlannerBenchmarkFactory benchmarkFactory = PlannerBenchmarkFactory.createFromSolverConfigXmlResource(
                "solverConfig.xml");
        PlannerBenchmarkFactory benchmarkFactoryFromXmlConfig =
                PlannerBenchmarkFactory.createFromXmlResource("BenchmarkConfig.xml");
        //PlannerBenchmark benchmark = benchmarkFactory.buildPlannerBenchmark(problem);
        PlannerBenchmark benchmark = benchmarkFactoryFromXmlConfig.buildPlannerBenchmark();
        benchmark.benchmarkAndShowReportInBrowser();
    }

    private static void runSolvers() {
        // Run optimizer
        EVRPsolution problem = createExample();
        SolverConfig solverConfigFromXML = SolverConfig.createFromXmlResource("solverConfig.xml");
        //solverConfigFromXML.withTerminationConfig(new TerminationConfig().withSecondsSpentLimit(10l));
        SolverFactory<EVRPsolution> solverFactoryFromConfigXML = SolverFactory.create(solverConfigFromXML);
        //SolverFactory<EVRPsolution> solverFactoryFromConfigXML = SolverFactory.createFromXmlResource("SolverConfig.xml");

        SolverFactory<EVRPsolution> solverFactory = SolverFactory.create(
                new SolverConfig()
                        .withSolutionClass(EVRPsolution.class)
                .withEntityClasses(Gate.class, Visit.class)
                        //.withEasyScoreCalculatorClass(EasyJustDistanceCostFunction.class)
                        .withConstraintProviderClass(ConstraintStreamCostFunction.class)
                        .withTerminationConfig(new TerminationConfig().withDiminishedReturns())
                        .withEnvironmentMode(EnvironmentMode.PHASE_ASSERT));

        /*SolverManager<EVRPsolution, String> solverManager = SolverManager.create(solverFactoryFromConfigXML, new SolverManagerConfig());
        String problemId = UUID.randomUUID().toString();
        SolverJob<EVRPsolution, String> solverJob = solverManager.solve(problemId, problem);
        //SolverJob<EVRPsolution, String> solverJob = solverManager.solveAndListen(problemId, problem, EVRPapp::printExample);
         */

        //try {
        //    EVRPsolution job_solution = solverJob.getFinalBestSolution();
        //} catch (InterruptedException e) {
        //    throw new RuntimeException(e);
        //} catch (ExecutionException e) {
        //    throw new RuntimeException(e);
        //}
        /*solverManager.solveBuilder()
                .withProblemId(UUID.randomUUID().toString())
                .withProblem(problem)
                .withBestSolutionConsumer(EVRPapp::printExample)
                .run();
        solverManager.solveBuilder()
                .withProblemId(UUID.randomUUID().toString())
                .withProblem(problem)
                .run();*/

        Solver<EVRPsolution> solver = solverFactoryFromConfigXML.buildSolver();
        EVRPsolution solution = solver.solve(problem);

        printExample(solution);

        SolutionManager<EVRPsolution, HardSoftScore> solutionManager = SolutionManager.create(solverFactoryFromConfigXML);
        log.info(solutionManager.explain(solution).getSummary());
    }

    private static EVRPsolution createExample() {
        EVRPsolution problem = new EVRPsolution();
        problem.setName("TEST problem");

        Plane plane = new Plane();
        plane.setId("AA");
        Plane plane2 = new Plane();
        plane2.setId("BB");


        Terminal terminal1 = new Terminal();
        terminal1.setId("T1");

        Terminal terminal2 = new Terminal();
        terminal2.setId("T2");

        Terminal terminal3 = new Terminal();
        terminal3.setId("T3");

        Company company1 = new Company();
        company1.setName("Company 1");
        company1.setTerminal(terminal3);

        Company company2 = new Company();
        company2.setName("Company 2");
        company2.setTerminal(terminal2);


        Gate gate1 = new Gate();
        gate1.setId("G1");
        gate1.setType("GATE TYPE 1");
        gate1.setServiceSpeedCoefficient(1.0);
        gate1.setTerminal(terminal1);

        Gate gate2 = new Gate();
        gate2.setId("G2");
        gate2.setType("GATE TYPE 2");
        gate2.setServiceSpeedCoefficient(1.2);
        gate2.setTerminal(terminal2);

        Gate gate3 = new Gate();
        gate3.setId("G3");
        gate3.setType("GATE TYPE 1");
        gate3.setServiceSpeedCoefficient(1.0);
        gate3.setTerminal(terminal3);


        plane.setScheduledArrivalTime(0L);
        plane.setCompany(company1);
        plane.setScheduledDepartureTime(120L);
        plane.setNecessaryGateTypes(List.of("GATE TYPE 1"));
        plane.setServiceTimeArrival(30);
        plane.setServiceTimeDeparture(30);
        plane.setServicePriority(1);

        plane2.setScheduledArrivalTime(60L);
        plane2.setCompany(company2);
        plane2.setScheduledDepartureTime(120L);
        plane2.setNecessaryGateTypes(List.of("GATE TYPE 2"));
        plane2.setServiceTimeArrival(20);
        plane2.setServiceTimeDeparture(20);
        plane2.setServicePriority(2);


        Visit v1 = new Visit();
        v1.setId(plane.getId() + "-V");
        v1.setPlane(plane);
        v1.setName(plane.getId());

        Visit v2 = new Visit();
        v2.setId(plane2.getId() + "-V");
        v2.setPlane(plane2);
        v2.setName(plane2.getId());

        problem.getVisitList().addAll(List.of(v1, v2));
        problem.getGateList().addAll(List.of(gate1, gate2, gate3));
        problem.getPlaneList().addAll(List.of(plane, plane2));

        return problem;
    }

    private static void printExample(EVRPsolution solution) {
        log.info("Printing EVRP solution %s with score %s."
                .formatted(solution.getName(), solution.getScore().toString()));
        for (Gate gate : solution.getGateList()) {
            log.info("Gate %s (%s) schedule:".formatted(gate.getId(), gate.getType()));
            for (Visit visit : gate.getVisits()) {
                String planeId = (visit.getPlane() == null) ? "?" : visit.getPlane().getId();
                Long delay = visit.getDelay();
                log.info("  Flight %s: start=%s end=%s delay=%s"
                        .formatted(planeId, visit.getStartTime(), visit.getEndTime(), delay));
            }
        }
        log.info("===================================================");

    }
}
