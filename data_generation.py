import os
import json
import random
import glob


def generate_terminals_companies(
    terminal_count,
    company_count,
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


def _gate_type_count(gate_type_count, regime):
    uppercase_alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    if regime == "update_count":
        if gate_type_count > len(uppercase_alphabet):
            return len(uppercase_alphabet)
        else:
            return gate_type_count

    if regime == "select_gate_type":
        return uppercase_alphabet[random.randint(0, gate_type_count - 1)]


def _skew_towards_min(min, max):  # skew depends on max value - experimentally better
    return random.random() ** (max) * (max - min) + min


def _skew_towards_max(min, max):  # skew got too big with different power
    return random.random() ** 0.5 * (max - min) + min


def _skew_towards_average(min, max):
    mu = (min + max) / 2
    sigma = (max - min) / 6  # ~99.7% within [a, b]
    value = random.gauss(mu, sigma)
    # force to stay in [min, max]
    value = max(min, min(max, value))
    return value


def generate_gates(
    dict_with_data,
    gate_count,
    gate_type_count,
    speed_coef_min,
    speed_coef_max,
    skew_speed_coef_towards,
):
    gate_type_count = _gate_type_count(
        gate_type_count=gate_type_count, regime="update_count"
    )

    terminal_count = len(dict_with_data["terminalList"])

    dict_with_data["gateList"] = []
    for i in range(1, gate_count + 1):

        if skew_speed_coef_towards == "min":
            # skew towards minimum value
            speed_coef_used = _skew_towards_min(min=speed_coef_min, max=speed_coef_max)
        elif skew_speed_coef_towards == "max":
            # skew towards maximum value (skews less that minimum)
            speed_coef_used = _skew_towards_max(min=speed_coef_min, max=speed_coef_max)
        else:
            # normal distribution
            speed_coef_used = _skew_towards_average(
                min=speed_coef_min, max=speed_coef_max
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


def _generate_times(minutes_on_ground_min, minutes_on_ground_max):
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

    return (
        scheduled_arrival_time,
        scheduled_departure_time,
        service_time_arrival,
        service_time_departure,
    )


def generate_planes(
    dict_with_data,
    plane_count,
    gate_type_count,
    minutes_on_ground_min,
    minutes_on_ground_max,
):
    dict_with_data["planeList"] = []

    company_count = len(dict_with_data["companyList"])
    service_priority_min, service_priority_max = 1, 5

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
                _skew_towards_min(min=service_priority_min, max=service_priority_max)
            ),
            "necessaryGateTypes": [
                _gate_type_count(
                    gate_type_count=gate_type_count, regime="select_gate_type"
                )
            ],
            "company": f"C{random.randint(1, company_count)}",
        }
        dict_with_data["planeList"].append(one_plane_dict)

    return dict_with_data


def generate_meta_and_save(dict_with_data, data_dir, name=""):
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


if __name__ == "__main__":
    ###############################################
    # TODO specify them all:

    # total counts
    terminal_count = 2
    company_count = 2
    gate_count = 2
    plane_count = 4

    # NOTE: these are fine if no particular interest in going deeper in logic:
    # will increment "airport_auto_example_*.json"
    solution_name = ""
    # gateList = how many different types - planes can use only "their" gate type
    gate_type_count = 2
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

    dict_with_data = generate_planes(
        dict_with_data=dict_with_data,
        plane_count=plane_count,
        gate_type_count=gate_type_count,
        minutes_on_ground_min=minutes_on_ground_min,
        minutes_on_ground_max=minutes_on_ground_max,
    )

    generate_meta_and_save(
        dict_with_data=dict_with_data, data_dir=data_dir, name=solution_name
    )
