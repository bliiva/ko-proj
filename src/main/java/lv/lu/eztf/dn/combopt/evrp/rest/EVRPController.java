package lv.lu.eztf.dn.combopt.evrp.rest;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.constraint.Indictment;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lv.lu.eztf.dn.combopt.evrp.domain.EVRPsolution;
import lv.lu.eztf.dn.combopt.evrp.domain.Visit;
import lv.lu.eztf.dn.combopt.evrp.domain.VisitType;
import lv.lu.eztf.dn.combopt.evrp.solver.SimpleIndictmentObject;

@Tag(name = "EVRP", description = "Service to optimize EVRP routes")
@RestController @Slf4j
@RequestMapping("/evrp")
public class EVRPController {
    private final SolverManager<EVRPsolution, String> solverManager;
    private final SolutionManager<EVRPsolution, HardSoftScore> solutionManager;
    private final ConcurrentMap<String, Job> jobIdToJob = new ConcurrentHashMap<>();

    public EVRPController(SolverManager<EVRPsolution, String> solverManager,
                          SolutionManager<EVRPsolution, HardSoftScore> solutionManager) {
        this.solverManager = solverManager;
        this.solutionManager = solutionManager;
    }

    @Operation(summary = "List the job IDs of all submitted EVRPs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of all job IDs.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "array", implementation = String.class))) })
    @GetMapping
    public Collection<String> list() {
        return jobIdToJob.keySet();
    }

    @Operation(summary = "Submit a EVRP to start solving as soon as CPU resources are available.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202",
                    description = "The job ID. Use that ID to get the solution with the other methods.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(implementation = String.class))) })

        @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
        public String solve(@RequestBody EVRPsolution problem) {

                if (problem.getGateList() == null || problem.getGateList().isEmpty()) {
                        throw new EVRPSolverException(null, HttpStatus.BAD_REQUEST, "gateList must not be empty");
                }

                // Ensure gates have non-null visit lists.
                problem.getGateList().forEach(g -> {
                        if (g.getVisits() == null) {
                                g.setVisits(new java.util.ArrayList<>());
                        } else {
                                g.getVisits().clear();
                        }
                });

                // Build 2 visits per plane: ARRIVAL service then DEPARTURE service.
                // We also assign them to a gate initially (round-robin) to satisfy list-variable invariants.
                problem.getVisitList().clear();
                for (int i = 0; i < problem.getPlaneList().size(); i++) {
                        var plane = problem.getPlaneList().get(i);
                        var gate = problem.getGateList().get(i % problem.getGateList().size());

                        Visit arrival = new Visit();
                        arrival.setId(plane.getId() + "-A");
                        arrival.setPlane(plane);
                        arrival.setType(VisitType.ARRIVAL);
                        arrival.setName(plane.getId() + " ARRIVAL");

                        Visit departure = new Visit();
                        departure.setId(plane.getId() + "-D");
                        departure.setPlane(plane);
                        departure.setType(VisitType.DEPARTURE);
                        departure.setName(plane.getId() + " DEPARTURE");

                        problem.getVisitList().addAll(List.of(arrival, departure));
                        gate.getVisits().add(arrival);
                        gate.getVisits().add(departure);
                }

        // Existing solver start code
        String jobId = UUID.randomUUID().toString();
        jobIdToJob.put(jobId, Job.ofEVRPsolution(problem));
        solverManager.solveBuilder()
                .withProblemId(jobId)
                .withProblemFinder(jobId_ -> jobIdToJob.get(jobId).evrpSolution)
                .withBestSolutionConsumer(solution -> jobIdToJob.put(jobId, Job.ofEVRPsolution(solution)))
                .withExceptionHandler((jobId_, exception) -> {
                        jobIdToJob.put(jobId, Job.ofException(exception));
                        log.error("Failed solving jobId ({}).", jobId, exception);
                })
                .run();

        return jobId;
        }



    @Operation(
            summary = "Get the solution and score for a given job ID. This is the best solution so far, as it might still be running or not even started.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The best solution of the EVRP so far.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = EVRPsolution.class))),
            @ApiResponse(responseCode = "404", description = "No EVRP found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorInfo.class))),
            @ApiResponse(responseCode = "500", description = "Exception during solving an EVRP.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorInfo.class)))
    })
    @GetMapping(value = "/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public EVRPsolution getEVRPsolution(
            @Parameter(description = "The job ID returned by the POST method.") @PathVariable("jobId") String jobId) {
        EVRPsolution evrpSolution = getEVRPsolutionAndCheckForExceptions(jobId);
        SolverStatus solverStatus = solverManager.getSolverStatus(jobId);
        evrpSolution.setSolverStatus(solverStatus);
        return evrpSolution;
    }

    @Operation(
            summary = "Get the score analysis for a given job ID. This is the best solution so far, as it might still be running or not even started.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The score analysis of the best solution of the EVRP so far.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = EVRPsolution.class))),
            @ApiResponse(responseCode = "404", description = "No EVRP found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorInfo.class))),
            @ApiResponse(responseCode = "500", description = "Exception during solving an EVRP.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorInfo.class)))
    })
    @GetMapping(value = "/score/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ScoreAnalysis<HardSoftScore> analyze(
            @Parameter(description = "The job ID returned by the POST method.") @PathVariable("jobId") String jobId) {
        EVRPsolution evrpSolution = getEVRPsolutionAndCheckForExceptions(jobId);
        return solutionManager.analyze(evrpSolution);
    }

    @Operation(
            summary = "Get the score indictments for a given job ID. This is the best solution so far, as it might still be running or not even started.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The score indictments of the best solution of the EVRP so far.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = EVRPsolution.class))),
            @ApiResponse(responseCode = "404", description = "No EVRP found.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorInfo.class))),
            @ApiResponse(responseCode = "500", description = "Exception during solving an EVRP.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorInfo.class)))
    })
    @GetMapping(value = "/indictments/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SimpleIndictmentObject> indictments(
            @Parameter(description = "The job ID returned by the POST method.") @PathVariable("jobId") String jobId) {
        EVRPsolution evrpSolution = getEVRPsolutionAndCheckForExceptions(jobId);
        return solutionManager.explain(evrpSolution).getIndictmentMap().entrySet().stream()
                .map(entry -> {
                    Indictment<HardSoftScore> indictment = entry.getValue();
                    return
                            new SimpleIndictmentObject(entry.getKey(), // indicted Object
                                    indictment.getScore(),
                                    indictment.getConstraintMatchCount(),
                                    indictment.getConstraintMatchSet());
                }).collect(Collectors.toList());
    }

    private EVRPsolution getEVRPsolutionAndCheckForExceptions(String jobId) {
        Job job = jobIdToJob.get(jobId);
        if (job == null) {
            throw new EVRPSolverException(jobId, HttpStatus.NOT_FOUND, "No EVRP found.");
        }
        if (job.exception != null) {
            throw new EVRPSolverException(jobId, job.exception);
        }
        return job.evrpSolution;
    }

    private record Job(EVRPsolution evrpSolution, Throwable exception) {

        static Job ofEVRPsolution(EVRPsolution evrpSolution) {
            return new Job(evrpSolution, null);
        }

        static Job ofException(Throwable error) {
            return new Job(null, error);
        }
    }


}
