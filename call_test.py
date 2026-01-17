import json
import os

import requests

url = os.environ.get("EVRP_URL", "http://localhost:8080/evrp")

# Read JSON file
with open("data/airport_example_20.json", "r") as f:
    data = json.load(f)

# Send POST request
response = requests.post(url, json=data)

# Print response
print(response.text)
