import os
import json
import random
import glob
from typing import overload, Literal


def generate_terminals_companies(
    terminal_count: int,
    company_count: int,
):
    dict_with_data = {}

    dict_with_data["terminalList"] = []
    for i in range(1, terminal_count + 1):
        one_terminal_dict = {"id": f"T{i}"}
        dict_with_data["terminalList"].append(one_terminal_dict)

    dict_with_data["companyList"] = []
    for i in range(1, company_count + 1):
        one_company_dict = {
            "id": f"C{i}",
            "name": f"Company-{i}",
            "terminal": f"T{random.randint(1, terminal_count)}",
        }
        dict_with_data["companyList"].append(one_company_dict)

    return dict_with_data


@overload
def _gate_type_count(gate_type_count: int, regime: Literal["update_count"]) -> int: ...
@overload
def _gate_type_count(
    gate_type_count: int, regime: Literal["select_gate_type"]
) -> str: ...


def _gate_type_count(gate_type_count: int, regime: str) -> int | str:
    uppercase_alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    if regime == "update_count":
        return min(gate_type_count, len(uppercase_alphabet))

    if regime == "select_gate_type":
        return uppercase_alphabet[random.randint(0, gate_type_count - 1)]

    raise ValueError("Invalid regime")


# skew depends on max value - experimentally better
def _skew_towards_min(min_v: float, max_v: float):

    # if max_v is not too big or too small, set it as power
    if max_v > 1 and max_v < 8:
        power = max_v
    # if max_v less than 1, for this to correctly skew towards min, it has to be set higher than 1
    elif max_v <= 1:
        power = 2
    # if max_v too high, then this will not be able to reach values closer to max -> so reduce power
    else:
        power = 5

    # power = (1, 5]

    return random.random() ** (power) * (max_v - min_v) + min_v


# skew got too big with different power
def _skew_towards_max(min_v: float, max_v: float):
    return random.random() ** 0.5 * (max_v - min_v) + min_v


def _skew_towards_average(min_v: float, max_v: float):
    mu = (min_v + max_v) / 2
    sigma = (max_v - min_v) / 6  # ~99.7% within [a, b]
    value = random.gauss(mu, sigma)
    # force to stay in [min, max]
    value = max(min_v, min(max_v, value))
    return value


def generate_gates(
    dict_with_data,
    gate_count: int,
    gate_type_count: int,
    speed_coef_min: float,
    speed_coef_max: float,
    skew_speed_coef_towards: str,
):
    gate_type_count = _gate_type_count(
        gate_type_count=gate_type_count, regime="update_count"
    )

    terminal_count = len(dict_with_data["terminalList"])

    dict_with_data["gateList"] = []
    for i in range(1, gate_count + 1):

        if skew_speed_coef_towards == "min":
            # skew towards minimum value
            speed_coef_used = _skew_towards_min(
                min_v=speed_coef_min, max_v=speed_coef_max
            )
        elif skew_speed_coef_towards == "max":
            # skew towards maximum value (skews less that minimum)
            speed_coef_used = _skew_towards_max(
                min_v=speed_coef_min, max_v=speed_coef_max
            )
        else:
            # normal distribution
            speed_coef_used = _skew_towards_average(
                min_v=speed_coef_min, max_v=speed_coef_max
            )

        one_gate_dict = {
            "id": f"G{i}",
            "type": _gate_type_count(
                gate_type_count=gate_type_count, regime="select_gate_type"
            ),
            "serviceSpeedCoefficient": round(speed_coef_used, ndigits=2),
            "terminal": f"T{random.randint(1, terminal_count)}",
        }
        dict_with_data["gateList"].append(one_gate_dict)

    return dict_with_data


def _generate_times(minutes_on_ground_min: int, minutes_on_ground_max: int):
    # choose arrival freely
    scheduled_arrival_time = random.randint(0, 2000)

    scheduled_departure_time = scheduled_arrival_time + random.randint(
        minutes_on_ground_min, minutes_on_ground_max
    )

    # time window minutes
    delta = scheduled_departure_time - scheduled_arrival_time

    while True:
        # NOTE: limited minimum time for arrival processing dynamically,
        # but it could be hardcoded as well
        service_time_arrival = random.randint(int(delta * 0.3), delta - 1)
        service_time_departure = random.randint(int(delta * 0.3), delta - 1)

        if service_time_arrival + service_time_departure < delta:
            break
    # service_time_arrival = random.randint(int(delta * 0.6), delta)
    # service_time_departure = random.randint(int(delta * 0.6), delta)

    return (
        scheduled_arrival_time,
        scheduled_departure_time,
        service_time_arrival,
        service_time_departure,
    )


def generate_planes_visits(
    dict_with_data,
    plane_count: int,
    gate_type_count: int,
    minutes_on_ground_min: int,
    minutes_on_ground_max: int,
    generate_visits: bool,
):
    dict_with_data["planeList"] = []

    company_count = len(dict_with_data["companyList"])
    service_priority_min, service_priority_max = 1, 5

    if generate_visits:
        dict_with_data["visitList"] = []
        visit_counter = 1

    for i in range(1, plane_count + 1):
        (
            scheduled_arrival_time,
            scheduled_departure_time,
            service_time_arrival,
            service_time_departure,
        ) = _generate_times(
            minutes_on_ground_min=minutes_on_ground_min,
            minutes_on_ground_max=minutes_on_ground_max,
        )

        one_plane_dict = {
            "id": f"P{i}",
            "scheduledArrivalTime": scheduled_arrival_time,
            "scheduledDepartureTime": scheduled_departure_time,
            "serviceTimeArrival": service_time_arrival,
            "serviceTimeDeparture": service_time_departure,
            "servicePriority": round(
                _skew_towards_min(
                    min_v=service_priority_min, max_v=service_priority_max
                )
                # _skew_towards_max(
                #     min_v=service_priority_min, max_v=service_priority_max
                # )
            ),
            "necessaryGateTypes": [
                _gate_type_count(
                    gate_type_count=gate_type_count, regime="select_gate_type"
                )
                # "Z"
            ],
            "company": f"C{random.randint(1, company_count)}",
        }
        dict_with_data["planeList"].append(one_plane_dict)

        if generate_visits:
            first_visit_dict = {
                "id": f"V{visit_counter}",
                "plane": f"P{i}",
                "type": "ARRIVAL",
                "gate": None,
                "startTime": None,
                "endTime": None,
                "next": None,
                "previous": None,
            }
            visit_counter += 1
            second_visit_dict = {
                "id": f"V{visit_counter}",
                "plane": f"P{i}",
                "type": "DEPARTURE",
                "gate": None,
                "startTime": None,
                "endTime": None,
                "next": None,
                "previous": None,
            }
            visit_counter += 1
            dict_with_data["visitList"].append(first_visit_dict)
            dict_with_data["visitList"].append(second_visit_dict)

    return dict_with_data


def generate_meta_and_save(dict_with_data, data_dir: str, name=""):
    dict_with_data["score"] = None

    if name == "":
        json_files_len = len(
            glob.glob(os.path.join(data_dir, "airport_auto_example_*.json"))
        )
        while True:  # try to find yet non-existing filename
            name = f"airport_auto_example_{json_files_len}"
            save_at_path = os.path.join(data_dir, f"{name}.json")
            if not os.path.exists(save_at_path):
                break
            json_files_len += 1

    dict_with_data["name"] = name
    print(f"Saving data file with name ({name}) to path ({save_at_path})")

    ordered = {
        "score": dict_with_data["score"],
        "name": dict_with_data["name"],
        **{k: v for k, v in dict_with_data.items() if k not in ("score", "name")},
    }

    with open(save_at_path, "w") as f:
        json.dump(ordered, f, indent=2)

    return ordered


# Piemēri:
# 1) airport_auto_example_0.json:  4, 4, 4, 20, (2 gatetypes) - easy, random
# 2) airport_auto_example_1.json:  6, 8, 6, 100 (3 gatetypes) - easy, random
# 3) airport_auto_example_2.json:  12, 20, 40, 400 (8 gatetypes) - "easy", random (varētu rasties "companyTerminalMismatch" mbyyyy)
# -
# 4) airport_auto_example_3.json:  4, 6, 4, 20 (2 gatetypes)+ ģenerē vajadzīgo gate visām lidmašīnām kā "Z" - gateTypeMismatch (necessaryGateTypes -> 197 rinda .py)
# 5) airport_auto_example_4.json:  10, 15, 40, 300 (4 gatetypes) + service time = [1.2 * delta - 2 * delta] -> lieli totalDelay (~143 rinda)
# 6) airport_auto_example_5.json:  10, 18, 50, 500 (4 gatetypes) + augsti priority visiem (~191 rinda servicePriority -> _skew_towards_max() funkcija)

if __name__ == "__main__":
    ###############################################
    # TODO specify them all:

    # total counts
    terminal_count = 10
    company_count = 18
    gate_count = 50
    plane_count = 500
    generate_visits = True

    # NOTE: these are fine if no particular interest in going deeper in logic:
    # will increment "airport_auto_example_*.json"
    solution_name = ""
    # gateList = how many different types - planes can use only "their" gate type
    gate_type_count = 4
    # gateList = minimum speed coef
    speed_coef_min = 0.8
    # gateList = maximum speed coef
    speed_coef_max = 2
    # gateList = [min, max, average] skew towards min value, towards max value or around average
    skew_speed_coef_towards = "min"
    # planeList = how many minutes between scheduledArrivalTime scheduledDepartureTime min and max
    minutes_on_ground_min = 30
    minutes_on_ground_max = 180

    ###############################################

    curr_wor_dir = os.getcwd()
    last_dir = os.path.basename(curr_wor_dir)

    if "ko-proj" != last_dir:
        exit(
            "EXITING. Not in directory: something/.../ko-proj\n Please `cd` to directory /ko-proj"
        )

    data_dir = os.path.join(curr_wor_dir, "data")

    # create terminals and companies
    dict_with_data = generate_terminals_companies(
        terminal_count=terminal_count, company_count=company_count
    )

    # create gates
    dict_with_data = generate_gates(
        dict_with_data=dict_with_data,
        gate_count=gate_count,
        gate_type_count=gate_type_count,
        speed_coef_min=speed_coef_min,
        speed_coef_max=speed_coef_max,
        skew_speed_coef_towards="min",
    )

    dict_with_data = generate_planes_visits(
        dict_with_data=dict_with_data,
        plane_count=plane_count,
        gate_type_count=gate_type_count,
        minutes_on_ground_min=minutes_on_ground_min,
        minutes_on_ground_max=minutes_on_ground_max,
        generate_visits=generate_visits,
    )

    generate_meta_and_save(
        dict_with_data=dict_with_data, data_dir=data_dir, name=solution_name
    )
