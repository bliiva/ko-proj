package lv.lu.eztf.dn.combopt.evrp.solver;

import java.util.Objects;

import org.jspecify.annotations.NonNull;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import lv.lu.eztf.dn.combopt.evrp.domain.Visit;
import lv.lu.eztf.dn.combopt.evrp.domain.VisitType;

public class ConstraintStreamCostFunction implements ConstraintProvider {
    @Override
    public Constraint @NonNull [] defineConstraints(@NonNull ConstraintFactory constraintFactory) {
        return new Constraint[] {
                arrivalMustFinishBeforeDepartureStarts(constraintFactory), //hard
                gateTypeMismatch(constraintFactory), //hard
                companyTerminalMismatch(constraintFactory), //soft
                totalDelay(constraintFactory), //soft
                mustAssignGateAfterScheduled(constraintFactory) //hard
        };
    }

        public Constraint arrivalMustFinishBeforeDepartureStarts(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Visit.class)
                .filter(v -> v.getType() == VisitType.ARRIVAL)
                .filter(v -> v.getPlane() != null)
                .join(Visit.class, equal(Visit::getPlane))
                .filter((arrival, other) -> other.getType() == VisitType.DEPARTURE)
                .filter((arrival, departure) -> arrival.getEndTime() != null && departure.getStartTime() != null)
                .filter((arrival, departure) -> departure.getStartTime() < arrival.getEndTime())
                .penalize(HardSoftScore.ofHard(3))
                .asConstraint("Arrival must finish before departure starts");
        }

        public Constraint gateTypeMismatch(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Visit.class)
                .filter(v -> v.getPlane() != null && v.getGate() != null)
                .filter(v -> {
                        if (v.getPlane().getNecessaryGateTypes() == null || v.getPlane().getNecessaryGateTypes().isEmpty()) {
                                return false;
                        }
                        return v.getGate().getType() == null || !v.getPlane().getNecessaryGateTypes().contains(v.getGate().getType());
                })
                .penalize(HardSoftScore.ONE_HARD, v -> Math.min(3, v.getPlane().getServicePriority()))
                .asConstraint("Gate type mismatch"); // Penalize if gate type does not match plane necessary gate type. If highter service priority, then higher penalty
        }

        public Constraint companyTerminalMismatch(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Visit.class)
                .filter(v -> v.getPlane() != null && v.getGate() != null)
                .filter(v -> v.getPlane().getCompany() != null)
                .filter(v -> v.getPlane().getCompany().getTerminal() != null && v.getGate().getTerminal() != null)
                .filter(v -> !Objects.equals(v.getPlane().getCompany().getTerminal().getId(), v.getGate().getTerminal().getId()))
                .penalize(HardSoftScore.ONE_SOFT, v -> 100)
                .asConstraint("Company terminal mismatch");
        }

        public Constraint totalDelay(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Visit.class)
                .filter(v -> v.getPlane() != null)
                .filter(v -> v.getDelay() != null && v.getDelay() > 0)
                .penalize(HardSoftScore.ONE_SOFT,
                        v -> Math.toIntExact(v.getDelay() * Math.max(1L, (long) v.getPlane().getServicePriority()) * (v.getType() == VisitType.ARRIVAL ? 3 : 2)))
                .asConstraint("Total delay");
        }

        public Constraint mustAssignGateAfterScheduled(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Visit.class)
                .filter(v -> v.getPlane() != null)
                .filter(v -> v.getPlane().getScheduledArrivalTime() != null && v.getPlane().getScheduledArrivalTime() > 0)
                .filter(v -> v.getPlane().getScheduledDepartureTime() != null && v.getPlane().getScheduledDepartureTime() > 0)
                .filter(v -> v.getGate() != null)
                .filter(v -> v.getStartTime() != null &&
                        (
                        (v.getType() == VisitType.ARRIVAL && v.getStartTime() < v.getPlane().getScheduledArrivalTime())
                        ||
                        (v.getType() == VisitType.DEPARTURE && v.getEndTime() < v.getPlane().getScheduledDepartureTime())
                        )
                )
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Must assign gate after scheduled time"); // Penalize if plane leaves before scheduled time, pasangers are not ready yet
        }
}

