# Coding Challenge

We would like to have a restful API for our statistics. The main use case for our API is to
calculate realtime statistic from the last 60 seconds. There will be two APIs, one of them is
called every time a transaction is made. It is also the sole input of this rest API. The other one
returns the statistic based of the transactions of the last 60 seconds.

## Getting Started

### Running the application
mvn spring-boot:run

### Running the tests
mvn test


## Available Endpoints

POST /transactions : Every Time a new transaction happened, this endpoint will be called.

GET /statistics    : Returns the statistic based on the transactions which happened in the last 60 seconds.


## Design Explained

### Overall 
#### Entering Valid Transaction
The application initiates final container array (with a size construct according to properties in application.yml) to contain all 
transactions aggregators. Each index in the array represents an individual interval (default to 1000ms = 1 sec. can be modified in application.yml)

Once an incoming transaction passes validation it follows the below scenario:
1. A proper container index is calculated
2. Accoridng to the above index, TransactionStatisticsAggregator is fetched from the container array and there are 3 options:

  (a) TransactionStatisticsAggregator is empty ?  - Aggregating the current transaction details.
    
  (b) TransactionStatisticsAggregator is out of date ?  Resetting aggregator and aggregating the
  current transaction details.
  
  (c) TransactionStatisticsAggregator is valid ? Adding and aggregating the current transaction details
  
#### Producing Statistics
According to the current time, the container provides all valid TransactionStatisticsAggregator(s)
Statistics are calculated by iterating on all valid aggregators. 

### Concurrency Handling Explained
Each TransactionStatisticsAggregator has its own ReadWriteLock (https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/locks/ReadWriteLock.html)
When a TransactionStatisticsAggregator is taken for update a Write lock is granted. When TransactionStatisticsAggregator is taking for observing and statistics a Read lock is requested.
ReadWriteLock technique was chossen due to its mechanism to allow non blocking multiple reads threads. The only time a block will occur is
when a Write lock is granted.

## Time Complexity

### Inserting (POST /transactions)
Will take O(1) as it only put the valid transaction in to the container array (number of operations is constant)

### Producing Statistics (GET /statistics)
Will take O(1):

a. Fetching valid TransactionStatisticsAggregator(s) - O(1) by default will iterate on 60 indices

b. Calculate statistics from valid aggregator(s) - O(1) by default - maximum 60 aggregator(s)

## Space Complexity
Requires O(1) . Container array's size is constant and pre defined and not depended on number of incoming transactions.


## Request/Response Explanied

### POST /transactions

Every Time a new transaction happened, this endpoint will be called.
Body:
{
"amount": 12.3,
"timestamp": 1478192204000
}

Where:
amount - transaction amount
timestamp - transaction time in epoch in millis in UTC time zone (this is not current
timestamp)
Returns: Empty body with either 201 or 204.
201 - in case of success
204 - if transaction is older than 60 seconds
Where:
amount is a double specifying the amount
time is a long specifying unix time format in milliseconds

### GET /statistics
It returns the statistic based on the transactions which happened in the last 60 seconds.

Returns:
{
"sum": 1000,
"avg": 100,
"max": 200,
"min": 50,
"count": 10
}

Where:
sum is a double specifying the total sum of transaction value in the last 60 seconds

avg is a double specifying the average amount of transaction value in the last 60
seconds

max is a double specifying single highest transaction value in the last 60 seconds

min is a double specifying single lowest transaction value in the last 60 seconds

count is a long specifying the total number of transactions happened in the last 60
seconds
