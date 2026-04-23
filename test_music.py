import urllib.request
import json

url = "https://itunes.apple.com/search?term=Top+Trending+Indian+Hits&limit=2"
req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
resp = urllib.request.urlopen(req).read()
data = json.loads(resp)
print("Preview URL:", data['results'][0]['previewUrl'])
