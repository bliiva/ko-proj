package lv.lu.eztf.dn.combopt.evrp.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Plane.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Plane {

    private String id;
    private String name;
    private boolean entireGroupMeeting;
    List<String> necessaryGateTypes;
    int servicePriority; // 1 - low, 5 - high

    /**
     * Multiply by {@link TimeGrain#GRAIN_LENGTH_IN_MINUTES} to get duration in minutes.
     */
    private int durationInGrains;

    public Plane() {
    }

    public Plane(String id) {
        this.id = id;
    }

    public Plane(String id, String name) {
        this(id);
        this.name = name;
    }

    public Plane(String id, String name, int durationInGrains) {
        this(id);
        this.name = name;
        this.durationInGrains = durationInGrains;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDurationInGrains() {
        return durationInGrains;
    }

    public void setDurationInGrains(int durationInGrains) {
        this.durationInGrains = durationInGrains;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @JsonIgnore
    public int getRequiredCapacity() {
        return 1;
    }

    @Override
    public String toString() {
        return name;
    }
}