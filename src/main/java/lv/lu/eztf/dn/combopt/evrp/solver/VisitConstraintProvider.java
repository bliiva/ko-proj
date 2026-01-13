package lv.lu.eztf.dn.combopt.evrp.solver;

import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.filtering;
import static ai.timefold.solver.core.api.score.stream.Joiners.greaterThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.lessThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.overlapping;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

import lv.lu.eztf.dn.combopt.evrp.domain.Visit;
import lv.lu.eztf.dn.combopt.evrp.domain.Gate;
import lv.lu.eztf.dn.combopt.evrp.domain.TimeGrain;

public class VisitConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                gateConflict(constraintFactory),
                avoidOvertime(constraintFactory),
                requiredGateCapacity(constraintFactory),
                startAndEndOnSameDay(constraintFactory),
                // requiredAndPreferredAttendanceConflict(constraintFactory),
                // preferredAttendanceConflict(constraintFactory),
                // doMeetingsAsSoonAsPossible(constraintFactory),
                // oneBreakBetweenConsecutiveMeetings(constraintFactory),
                // overlappingMeetings(constraintFactory),
                // assignLargerRoomsFirst(constraintFactory),
                // roomStability(constraintFactory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    public Constraint gateConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(Visit.class,
                equal(Visit::getGate),
                overlapping(Visit::getGrainIndex, visit -> visit.getLastTimeGrainIndex() + 1))
                .penalize(HardMediumSoftScore.ONE_HARD,
                        (leftVisit, rightVisit) -> rightVisit.calculateOverlap(leftVisit))
                .asConstraint("Gate conflict");
    }

    public Constraint avoidOvertime(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Visit.class)
                .filter(visit -> visit.getStartingTimeGrain() != null)
                .ifNotExists(TimeGrain.class,
                        equal(Visit::getLastTimeGrainIndex, TimeGrain::getGrainIndex))
                .penalize(HardMediumSoftScore.ONE_HARD, Visit::getLastTimeGrainIndex)
                .asConstraint("Don't go in overtime");
    }

    public Constraint requiredGateCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Visit.class)
                .filter(visit -> visit.getRequiredCapacity() > visit.getGateCapacity())
                .penalize(HardMediumSoftScore.ONE_HARD,
                        visit -> visit.getRequiredCapacity() - visit.getGateCapacity())
                .asConstraint("Required gate capacity");
    }

    public Constraint startAndEndOnSameDay(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Visit.class)
                .filter(visit -> visit.getStartingTimeGrain() != null)
                .join(TimeGrain.class,
                        equal(Visit::getLastTimeGrainIndex, TimeGrain::getGrainIndex),
                        filtering((visit,
                                timeGrain) -> !visit.getStartingTimeGrain().getDayOfYear()
                                        .equals(timeGrain.getDayOfYear())))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Start and end on same day");
    }
    // ************************************************************************
    // Soft constraints
    // ************************************************************************

//     public Constraint doMeetingsAsSoonAsPossible(ConstraintFactory constraintFactory) {
//         return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
//                 .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null)
//                 .penalize(HardMediumSoftScore.ONE_SOFT, MeetingAssignment::getLastTimeGrainIndex)
//                 .asConstraint("Do all meetings as soon as possible");
//     }

//     public Constraint oneBreakBetweenConsecutiveMeetings(ConstraintFactory constraintFactory) {
//         return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
//                 .filter(meetingAssignment -> meetingAssignment.getStartingTimeGrain() != null)
//                 .join(constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
//                         .filter(assignment -> assignment.getStartingTimeGrain() != null),
//                         equal(MeetingAssignment::getLastTimeGrainIndex,
//                                 rightAssignment -> rightAssignment.getGrainIndex() - 1))
//                 .penalize(HardMediumSoftScore.ofSoft(100))
//                 .asConstraint("One TimeGrain break between two consecutive meetings");
//     }

//     public Constraint assignLargerRoomsFirst(ConstraintFactory constraintFactory) {
//         return constraintFactory.forEachIncludingUnassigned(MeetingAssignment.class)
//                 .filter(meetingAssignment -> meetingAssignment.getRoom() != null)
//                 .join(Room.class,
//                         lessThan(MeetingAssignment::getRoomCapacity, Room::getCapacity))
//                 .penalize(HardMediumSoftScore.ONE_SOFT,
//                         (meetingAssignment, room) -> room.getCapacity() - meetingAssignment.getRoomCapacity())
//                 .asConstraint("Assign larger rooms first");
//     }



}
