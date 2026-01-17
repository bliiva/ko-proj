package lv.lu.eztf.dn.combopt.evrp;

import ai.timefold.solver.benchmark.api.PlannerBenchmark;
import ai.timefold.solver.benchmark.api.PlannerBenchmarkFactory;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.*;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;
import lombok.extern.slf4j.Slf4j;
import lv.lu.eztf.dn.combopt.evrp.domain.*;
import lv.lu.eztf.dn.combopt.evrp.solver.ConstraintStreamCostFunction;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
public class EVRPapp {
    public static void main(String[] args) {
        // runSolvers();
        runBenchmarker();
        // generateData();
    }

private static void runBenchmarker() {
    // Remove these unused lines
    // PlannerBenchmarkFactory benchmarkFactory = PlannerBenchmarkFactory.createFromSolverConfigXmlResource("solverConfig.xml");
    // PlannerBenchmark benchmark = benchmarkFactory.buildPlannerBenchmark(problem);
    
    PlannerBenchmarkFactory benchmarkFactoryFromXmlConfig =
            PlannerBenchmarkFactory.createFromXmlResource("BenchmarkConfig.xml");
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

        // Location depot = new Location(0l, 0.0, 0.0);
        // plane.setDepot(depot);
        // plane2.setDepot(depot);

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

        // Location loc1 = new Location(1l, 4.0, 0.0);
        // customer1.setLocation(loc1);
        // customer1.setServiceDuration(60 * 15l);
        // customer1.setStartTime(0l);
        // customer1.setEndTime(3600 * 8l);

        // Customer customer2 = new Customer();
        // customer2.setName("Customer RIGHT UPPER");
        // Location loc2 = new Location(2l, 4.0, 3.0);
        // customer2.setLocation(loc2);
        // customer2.setServiceDuration(60 * 15l);
        // customer2.setStartTime(0l);
        // customer2.setEndTime(3600 * 8l);

        // Customer customer3 = new Customer();
        // customer3.setName("Customer LEFT UPPER");
        // Location loc3 = new Location(3l, 0.0, 3.0);
        // customer3.setLocation(loc3);
        // customer3.setServiceDuration(60 * 15l);
        // customer3.setStartTime(0l);
        // customer3.setEndTime(3600 * 8l);

        // Customer customer4 = new Customer();
        // customer4.setName("Customer FAR AWAY");
        // Location loc4 = new Location(3l, 6.0, 6.0);
        // customer4.setLocation(loc4);
        // customer4.setServiceDuration(60 * 15l);
        // customer4.setStartTime(0l);
        // customer4.setEndTime(3600 * 8l);

        // ChargingStation chargingStation = new ChargingStation();
        // chargingStation.setName("Charging Station");
        // Location locCS = new Location(  4l, 3.0, 2.0);
        // chargingStation.setLocation(locCS);
        // chargingStation.setStartTime(0l);
        // chargingStation.setEndTime(3600 * 8l);
        // chargingStation.setChargingPower(3.0);
        // chargingStation.setPriceEnergy(1.5);
        // chargingStation.setNumberOfSlots(2);

        // ChargingStation chargingStation2 = new ChargingStation();
        // chargingStation2.setName("Charging Station 2");
        // chargingStation2.setLocation(locCS);
        // chargingStation2.setStartTime(0l);
        // chargingStation2.setEndTime(3600 * 8l);
        // chargingStation2.setChargingPower(3.0);
        // chargingStation2.setPriceEnergy(1.5);
        // chargingStation2.setNumberOfSlots(2);


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

    // private static Solution generateExample(Integer SCALE) {
    //     Solution problem = new Solution();
    //     problem.setName(LocalDateTime.now().toString()+"_"+SCALE.toString());

    //     Random random = new Random();
    //     Long ID = 0l;
    //     Integer SIZE_OF_MAP = SCALE;
    //     Integer DEPOT_SIZE = 5;
    //     Integer numberOfVehicles = SCALE / 20 + 1;
    //     Double MAX_CHARGE = SIZE_OF_MAP * 2.5;
    //     Double DISCHARGE_SPEED = 1.0;
    //     for (int i = 1; i <= numberOfVehicles; i++) {
    //         Vehicle vehicle = new Vehicle();
    //         vehicle.setRegNr("vehicle-"+i);
    //         problem.getVehicleList().add(vehicle);

    //         if (i - 1 % DEPOT_SIZE == 0) {
    //             depot = new Location(ID,random.nextDouble() * SIZE_OF_MAP,random.nextDouble() * SIZE_OF_MAP);
    //             ID++;
    //             problem.getLocationList().add(depot);
    //         }
    //         vehicle.setDepot(depot);
    //         vehicle.setCharge(MAX_CHARGE);
    //         vehicle.setCostHourly(7.0);
    //         vehicle.setCostUsage(30.0);
    //         vehicle.setDischargeSpeed(DISCHARGE_SPEED);
    //         vehicle.setMaxCharge(MAX_CHARGE);
    //         vehicle.setMaxChargePower(2.0);
    //         vehicle.setOperationStartingTime(0l);
    //         vehicle.setOperationEndingTime(3600 * 8l);
    //         vehicle.setPriceEnergyDepot(1.0);
    //         vehicle.setServiceDurationAtFinish(60 * 10l);
    //         vehicle.setServiceDurationAtStart(60 * 5l);
    //     }
    //     for (int i = 1; i <= SCALE; i++) {
    //         Customer customer1 = new Customer();
    //         customer1.setName("Customer-"+i);
    //         problem.getVisitList().add(customer1);
    //         Location loc1 = new Location(ID,random.nextDouble() * SIZE_OF_MAP,random.nextDouble() * SIZE_OF_MAP);
    //         ID++;
    //         problem.getLocationList().add(loc1);
    //         customer1.setLocation(loc1);
    //         customer1.setServiceDuration(60 * 15l);
    //         customer1.setStartTime(random.nextLong(3) * 3600);
    //         customer1.setEndTime(customer1.getStartTime() + 3600 * 6l);
    //     }

    //     Location locCS = new Location(ID,random.nextDouble() * SIZE_OF_MAP,random.nextDouble() * SIZE_OF_MAP);
    //     ID++;
    //     problem.getLocationList().add(locCS);
    //     for (int i = 1; i <= numberOfVehicles; i++) {
    //         ChargingStation chargingStation = new ChargingStation();
    //         chargingStation.setName("Charging Station-"+i);
    //         chargingStation.setLocation(locCS);
    //         chargingStation.setStartTime(0l);
    //         chargingStation.setEndTime(3600 * 8l);
    //         chargingStation.setChargingPower(3.0);
    //         chargingStation.setPriceEnergy(1.5);
    //         chargingStation.setNumberOfSlots(2);
    //         problem.getVisitList().add(chargingStation);
    //     }

    //     return problem;
    // }

    private static void generateData() {
        // Solution problem1 = generateExample(100);
        /*EVRPsolution problem2 = generateExample(25);
        EVRPsolution problem3 = generateExample(40);
        EVRPsolution problem4 = generateExample(49);
        EVRPsolution problem5 = generateExample(50);*/

        JsonIO jsonIO = new JsonIO();
        // jsonIO.write(problem1, new File("data/problem_100.json"));
        // Check if we can read what we have written
        /*jsonIO.read(new File("data/problem_10.json"));
        jsonIO.write(problem2, new File("data/problem_25.json"));
        jsonIO.write(problem3, new File("data/problem_40.json"));
        jsonIO.write(problem4, new File("data/problem_49.json"));
        jsonIO.write(problem5, new File("data/problem_50.json"));*/
    }
}
