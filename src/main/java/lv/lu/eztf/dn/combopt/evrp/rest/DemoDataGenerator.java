package lv.lu.eztf.dn.combopt.evrp.rest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;

// TODO: Replace these imports with the correct package names for your project.
// For example, if your domain classes are in lv.lu.eztf.dn.combopt.evrp.domain, use:
import lv.lu.eztf.dn.combopt.evrp.domain.Plane;
import lv.lu.eztf.dn.combopt.evrp.domain.Visit;
import lv.lu.eztf.dn.combopt.evrp.domain.Schedule;
import lv.lu.eztf.dn.combopt.evrp.domain.Gate;
import lv.lu.eztf.dn.combopt.evrp.domain.TimeGrain;

@ApplicationScoped
public class DemoDataGenerator {

    public Schedule generateDemoData() {
        Random random = new Random(0);
        Schedule schedule = new Schedule();
        // Time grain
        List<TimeGrain> timeGrains = generateTimeGrain();
        // Rooms
        List<Gate> gates = List.of(
                new Gate("G1", "Gate 1", 1),
                new Gate("G2", "Gate 2", 1),
                new Gate("G3", "Gate 3", 1));
        // Meetings
        List<Plane> planes = generatePlanes(random);
        // Meeting assignments
        List<Visit> visits = generateVisits(planes);
        // Update schedule
        schedule.setGates(gates);
        schedule.setTimeGrains(timeGrains);
        schedule.setPlanes(planes);
        schedule.setVisits(visits);
        return schedule;
    }

    private List<TimeGrain> generateTimeGrain() {
        List<TimeGrain> timeGrains = new ArrayList<>();
        LocalDate currentDate = LocalDate.now().plusDays(1);
        int count = 0;
        while (currentDate.isBefore(LocalDate.now().plusDays(5))) {
            LocalTime currentTime = LocalTime.of(8, 0);
            timeGrains.add(new TimeGrain(String.valueOf(++count), count,
                    LocalDateTime.of(currentDate, currentTime).getDayOfYear(),
                    currentTime.getHour() * 60 + currentTime.getMinute()));
            while (currentTime.isBefore(LocalTime.of(17, 45))) {
                currentTime = currentTime.plusMinutes(15);
                timeGrains.add(new TimeGrain(String.valueOf(++count), count,
                        LocalDateTime.of(currentDate, currentTime).getDayOfYear(),
                        currentTime.getHour() * 60 + currentTime.getMinute()));
            }
            currentDate = currentDate.plusDays(1);
        }
        return timeGrains;
    }

    private List<Plane> generatePlanes(Random random) {
        int count = 0;
        List<Plane> planes = List.of(
                new Plane(String.valueOf(count++), "AA"),
                new Plane(String.valueOf(count++), "BB"),
                new Plane(String.valueOf(count++), "CC"),
                new Plane(String.valueOf(count++), "DD"),
                new Plane(String.valueOf(count++), "EE"),
                new Plane(String.valueOf(count++), "FF"),
                new Plane(String.valueOf(count++), "GG"),
                new Plane(String.valueOf(count++), "HH"),
                new Plane(String.valueOf(count++), "II"),
                new Plane(String.valueOf(count++), "JJ"),
                new Plane(String.valueOf(count++), "KK"),
                new Plane(String.valueOf(count++), "LL"),
                new Plane(String.valueOf(count++), "MM"),
                new Plane(String.valueOf(count++), "NN"),
                new Plane(String.valueOf(count++), "OO"),
                new Plane(String.valueOf(count++), "PP"),
                new Plane(String.valueOf(count++), "QQ"),
                new Plane(String.valueOf(count++), "RR"),
                new Plane(String.valueOf(count++), "SS"),
                new Plane(String.valueOf(count++), "TT"),
                new Plane(String.valueOf(count++), "UU"),
                new Plane(String.valueOf(count++), "VV"),
                new Plane(String.valueOf(count++), "WW"),
                new Plane(String.valueOf(count++), "XX"),
                new Plane(String.valueOf(count), "YY"));
        // Duration
        List<Pair<Float, Integer>> durationGrainsCount = List.of(
                new Pair<>(0.33f, 8),
                new Pair<>(0.33f, 12),
                new Pair<>(0.33f, 16));
        durationGrainsCount.forEach(p -> applyRandomValue((int) (p.key() * planes.size()), planes,
                m -> m.getDurationInGrains() == 0, m -> m.setDurationInGrains(p.value()), random));
        // Ensure there are no empty duration
        planes.stream()
                .filter(m -> m.getDurationInGrains() == 0)
                .forEach(m -> m.setDurationInGrains(8));
        // Attendants
        return planes;
    }

    private List<Visit> generateVisits(List<Plane> planes) {
        return IntStream.range(0, planes.size())
                .mapToObj(i -> new Visit(String.valueOf(i), planes.get(i)))
                .toList();
    }

    private <T> void applyRandomValue(int count, List<T> values, Predicate<T> filter, Consumer<T> consumer, Random random) {
        int size = (int) values.stream().filter(filter).count();
        for (int i = 0; i < count; i++) {
            values.stream()
                    .filter(filter)
                    .skip(size > 0 ? random.nextInt(size) : 0).findFirst()
                    .ifPresent(consumer);
            size--;
            if (size < 0) {
                break;
            }
        }
    }

    private record Pair<K, V>(K key, V value) {
    }
}
