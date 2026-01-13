import requests
import json

url = "http://localhost:8080/schedules"

# Read JSON file
with open("data/airport_example.json", "r") as f:
    data = json.load(f)

# Send POST request
response = requests.post(url, json=data)

# Print response
print(response.text)
