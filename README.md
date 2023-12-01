# Uzum IT Academy | Final Project | Currency Converter

**Task description:**
 - Write a currency converter program. The converter must translate the currency taking into account the current exchange rate (for the day) and the agent commission.

**Project Requirements:**
 - As a source of currency data, *https://cbu.uz/ru/arkhiv-kursov-valyut/* and *https://cbu.uz/ru/arkhiv-kursov-valyut/json/all/2021-12-11/* need to be used.
 - Use Postgresql for storage.
 - Use Java Spring for backend.
 - When the service starts, the currency in the database should be updated to the current day
 - GET methods return JSON
 - POST methods accept JSON
 - The secret key must be stored in a file
 - The currency is displayed in the format up to the 6th decimal place (inclusive).
 - The database and application must run in docker. (OPTIONAL (increased complexity): docker-compose).

**Initial Data:**
 - Secret key is saved to databse from file.
 - Commissions are created and inserted into database for each currency pair (taken from Central Bank's API).
 - Accounts are created and inserted into database for each currency with some initial balance.

**Instructions to run the project:**

First get the project to you device(laptop, desktop) using this git command(make sure git is installled to your device):
```bash
git clone https://github.com/AkobirToshtemirov/currencyconverter.git
```

then go to the project directory and run the project using this command(make sure Docker is installed to your device):
```bash
docker-compose up --build
```

**Commands:**

 - ***GET*** | *http://localhost:8080/api/v1/app/convert?from=USD&to=UZS&amount=150*
   - Returns the amount of currency that the user will receive after conversion (but does not perform the conversion).
   - Currency is transferred from 'from' to 'to', taking into account the current exchange rate and the deduction of an agent commission for each conversion.
   - The commission for each conversion is taken from the database and is 0% by default.
   - At the same time, commissions for UZS -> USD and USD -> UZS may be different.
   - Conversion not from (to) UZS to (from) any other currency occurs through double conversion of UZS and the commission must be deducted twice taking into account different commissions
   - For the base rate, use the Central Bank rate for the current day

 - ***GET*** | *http://localhost:8080/api/v1/app/officialrates?date=2023-11-30&pair=USD/UZS*
    - Returns the official Central Bank rate on the desired day for the desired currency pair.

- ***POST*** | *http://localhost:8080/api/v1/app/convert* **|** Body => {"from":"USD","to":"AED", "amount": "3333"}
    - Performs currency conversion. Debits money from accounts in the currency we issue, charges the currency we accept and additional commissions.
    - If there is not enough money to issue, the conversion is not performed and an error message is returned in JSON format and code 403.
    - If such currency does not exist, returns error message in JSON format with the status code 404.

 - ***POST*** | *http://localhost:8080/api/v1/app/setcommission* **|** Headers add => {"secret-key": "jkhkjh132@lkjjkJk@89jghAj"} **|** Body => {"from":"USD","to":"UZS", "commissionAmount": 0.055}
    - New commission amount is set to currency pair.
    - If the pair does not exist, returns error message in JSON format with the status code 404.
    - The method must check for the presence of a secret key in the header. If the key does not match the secret key, returns error message in JSON format with the status code 403.

    

