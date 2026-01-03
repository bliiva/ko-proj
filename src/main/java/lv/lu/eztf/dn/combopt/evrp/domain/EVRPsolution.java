package lv.lu.eztf.dn.combopt.evrp.domain;

import java.util.ArrayList;
import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolverStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@PlanningSolution
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Slf4j
public class EVRPsolution {
    SolverStatus solverStatus;
    @PlanningScore
    HardSoftScore score;
    String name;
    @PlanningEntityCollectionProperty
    List<Plane> planeList = new ArrayList<>();
    @ValueRangeProvider(id = "visitRange")
    @PlanningEntityCollectionProperty
    List<Visit> visitList = new ArrayList<>();
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "gateRange")
    List<Gate> gateList = new ArrayList<>();
}
