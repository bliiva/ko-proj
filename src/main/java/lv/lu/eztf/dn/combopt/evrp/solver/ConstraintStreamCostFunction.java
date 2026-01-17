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
                .penalize(HardSoftScore.ONE_HARD)
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
                .penalize(HardSoftScore.ONE_HARD, v -> Math.max(1, v.getPlane().getServicePriority()))
                .asConstraint("Gate type mismatch");
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
                        (v.getType() == VisitType.DEPARTURE && v.getStartTime() < v.getPlane().getScheduledDepartureTime())
                        )
                )
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Must assign gate after scheduled time");
        }

//     public Constraint totalDistance(ConstraintFactory constraintFactory) {
//         return constraintFactory
//                 .forEach(Vehicle.class)
//                 .penalize(HardSoftScore.ONE_SOFT, vehicle -> (int) Math.round(vehicle.getTotalDistance() * 1000))
//                 .asConstraint("Total distance for a vehicle");
//     }

//     // This actually is BAD constraint braking incremental score calculation
//     public Constraint batteryEmpty(ConstraintFactory constraintFactory) {
//         return constraintFactory
//                 .forEach(Vehicle.class)
//                 .filter(vehicle -> vehicle.isBatteryEmpty())
//                 .penalize(HardSoftScore.ONE_HARD)
//                 .asConstraint("Battery empty");
//     }

//     // This actually is BAD constraint braking incremental score calculation
//     public Constraint visitTimeWindowViolated(ConstraintFactory constraintFactory) {
//         return constraintFactory
//                 .forEach(Visit.class)
//                 .filter(visit -> visit.getDepartureTime() > visit.getEndTime())
//                 .penalize(HardSoftScore.ONE_HARD)
//                 .asConstraint("TW violation");
//     }

//     public Constraint visitChargeNegative(ConstraintFactory constraintFactory) {
//         return constraintFactory
//                 .forEach(Visit.class)
//                 .filter(visit -> visit.getVehicleCharge() < 0)
//                 .penalize(HardSoftScore.ONE_HARD)
//                 .asConstraint("Visit with empty battery");
//     }

//     public Constraint depotChargeNegative(ConstraintFactory constraintFactory) {
//         return constraintFactory
//                 .forEach(Visit.class)
//                 .filter(visit -> visit.getNext() == null)
//                 .join(Vehicle.class, equal(Visit::getVehicle, v -> v))
//                 .filter((visit, vehicle) -> visit.getVehicleChargeAfterVisit() -
//                         vehicle.getDischargeSpeed() * visit.getLocation().distanceTo(vehicle.getDepot()) < 0 )
//                 .penalize(HardSoftScore.ONE_HARD)
//                 .asConstraint("Depot with empty battery");
//     }

//     public Constraint depotTimeWindowViolated(ConstraintFactory constraintFactory) {
//         return constraintFactory
//                 .forEach(Visit.class)
//                 .filter(visit -> visit.getNext() == null)
//                 .join(Vehicle.class, equal(Visit::getVehicle, v -> v))
//                 .filter((visit, vehicle) -> visit.getDepartureTime() +
//                         visit.getLocation().timeTo(vehicle.getDepot()) + vehicle.getServiceDurationAtFinish() >
//                         vehicle.getOperationEndingTime())
//                 .penalize(HardSoftScore.ONE_HARD)
//                 .asConstraint("Depot TW violation");
//     }

//     public Constraint costVehicleUsage(ConstraintFactory constraintFactory) {
//         return constraintFactory
//                 .forEach(Vehicle.class)
//                 .filter(vehicle -> !vehicle.getVisits().isEmpty())
//                 .penalize(HardSoftScore.ONE_SOFT, vehicle -> (int) Math.round(vehicle.getCostUsage() * 100))
//                 .asConstraint("Vehicle usage cost");
//     }

//     public Constraint costVehicleTime(ConstraintFactory constraintFactory) {
//         return constraintFactory
//                 .forEach(Visit.class)
//                 .filter(visit -> visit.getNext() == null)
//                 .join(Vehicle.class, equal(Visit::getVehicle, v -> v))
//                 .penalize(HardSoftScore.ONE_SOFT, (visit, vehicle) -> (int) Math.round(
//                         (visit.getDepartureTime() + visit.getLocation().timeTo(vehicle.getDepot()) +
//                         vehicle.getServiceDurationAtFinish() - vehicle.getOperationStartingTime())
//                         * vehicle.getCostHourly() * 100 / 3600))
//                 .asConstraint("Vehicle hourly cost");
//     }

//     public Constraint costInitialEnergy(ConstraintFactory constraintFactory) {
//         return constraintFactory
//                 .forEach(Vehicle.class)
//                 .filter(vehicle -> !vehicle.getVisits().isEmpty())
//                 .penalize(HardSoftScore.ONE_SOFT, vehicle -> (int) Math.round(vehicle.getPriceEnergyDepot() *
//                         100 * vehicle.getCharge()))
//                 .asConstraint("Initial energy cost");
//     }

//     public Constraint costRechargedEnergy(ConstraintFactory constraintFactory) {
//         return constraintFactory
//                 .forEach(ChargingStation.class)
//                 .penalize(HardSoftScore.ONE_SOFT, station -> (int) Math.round(station.getPriceEnergy() *
//                         100 * (station.getVehicleChargeAfterVisit() - station.getVehicleCharge())))
//                 .asConstraint("Recharged energy cost");
//     }

//     public Constraint rewardLeftover(ConstraintFactory constraintFactory) {
//         return constraintFactory
//                 .forEach(ChargingStation.class)
//                 .groupBy(ChargingStation::getVehicle, ConstraintCollectors.max(ChargingStation::getPriceEnergy))
//                 // TODO: expand with cars without charging stations!
//                 .join(Visit.class, equal((v,p)->v, Visit::getVehicle))
//                 .filter((vehicle, price, visit) -> visit.getNext() == null)
//                 .reward(HardSoftScore.ONE_SOFT,(vehicle, maxStationPrice, lastVisit) -> (int) Math.round(Math.max(vehicle.getPriceEnergyDepot(), maxStationPrice) * 100 *
//                                 Math.max(0, lastVisit.getVehicleChargeAfterVisit() - lastVisit.getLocation().distanceTo(vehicle.getDepot()) * vehicle.getDischargeSpeed())))
//                 .asConstraint("Leftover reward");
//     }
}

