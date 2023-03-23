import requests
from bs4 import BeautifulSoup
import cloudscraper

scraper = cloudscraper.create_scraper()  # returns a CloudScraper instance
# Or: scraper = cloudscraper.CloudScraper()  # CloudScraper inherits from requests.Session
print(scraper.get("http://stopandshop.com/product-search/").text)  # => "<!DOCTYPE html><html><head>..."


'''
#get the HTML for the page
URL = "http://stopandshop.com/"


def extract_source(url):
    headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64; rv:50.0) Gecko/20100101 Firefox/50.0'}
    source = requests.get(url, headers=headers).text
    return source


page = extract_source(URL)

print(page)

#instantiate BeautifulSoup html parser object
soup = BeautifulSoup(page.content, "html.parser")

#use find() if know document only has 1 instance of that tag
#find_all() scans ENTIRE doc and returns a list of element

#find html tag with id "ResultsContainer"
results = soup.find_all("div", id="product-grid-cell_name")

print(results)'''

'''
#print(results.prettify())

#within results, get all divs with class name "card-content"
job_elements = results.find_all("div", class_="card-content")

#iterate over each job
for job_element in job_elements:
    title_element = job_element.find("h2", class_="title")
    company_element = job_element.find("h3", class_="company")
    location_element = job_element.find("p", class_="location")

    print(title_element.text.strip()) #call .text to return only text content of HTML that object contains
    print(company_element.text.strip())
    print(location_element.text.strip())
    print()

'''