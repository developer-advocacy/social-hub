#!/usr/bin/env python3

# Cut & Paste Python Code
import requests

payload = {
"postRequest": "Today is a great day!",
"platforms": ["twitter"],
"mediaUrls": ["https://img.ayrshare.com/012/gb.jpg"],
}

# Live API Key
headers = {'Content-Type': 'application/json', 
    'Authorization': 'Bearer ...'}

r = requests.postRequest('https://app.ayrshare.com/api/postRequest',
json=payload, 
headers=headers)