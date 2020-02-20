# myRetail

### What this application is about
This application is a poc of a products API which aggregates data (price and title) from multiple sources and returns it to the invoker in JSON format

### Tech Stack
- JDK v13
- Kotlin
- Spring Boot framework
- MongoDB

### Completeness
#### Are problems addressed?
The problem, which is to provide aggregated data to clients, has been addressed by providing an API for the clients to invoke and obtain the data.
They also have the capability to update the price of the item

#### Is it production ready?
The application can be deployed and scaled horizontally as required. The data store used should be decided though because each data store is
tailored for a particular use case and the right one has to be chosen based on a consensus

### Work regarding scaling the code
The application can be containerized and deployed as containers in a cluster which can either grow
in size or run lesser number of containers based on the traffic to the application. For this an appropriate data store has to be
chosen as well to handle the load while keeping in mind the capacity of the redsky servers

### Testing
Unit and integration tests have been written to make sure that the functionality is maintained in the code

### Instructions for running the service
Requirements to run the code
- JDK v13 (not required locally if docker is used)
- Docker

Two ways to run the service:
- The project can be run using the command. The requirement for this would be running mongodb locally
>./gradlew bootRun

- The project can also be run on docker containers (service and mongodb) locally using the command
>./gradlew clean build && docker-compose up

### Example request / response for different cases
The application allows users to look up information of items if they know the item `id`

- If price is available in the data store and name is available in redsky then the response looks like this with a `200` response code
##### Request
```
curl -v http://localhost:8080/product/13860428
```
##### Response
```
{
  "id": 13860428,
  "name": "The Big Lebowski (Blu-ray)",
  "current_price": {
    "value": 1193.33,
    "currency_code": "USD"
  },
  "productErrors": []
}
``` 

- If price is available in the data store but the name is not available in redsky then the response looks like this with a `200` response code
##### Request
```
curl -v http://localhost:8080/product/123
```
##### Response
```
{
  "id": 123,
  "current_price": {
    "value": 14.23,
    "currency_code": "USD"
  },
  "productErrors": [
    {
      "redSkyError": "could not retrieve title from redsky: (Retries exhausted: 3/3)"
    }
  ]
}
```

- If price is not available in the data store but the name is available in redsky then the response looks like this with a `200` response code
##### Request
```
curl -v http://localhost:8080/product/13860427
```
##### Response
```
{
  "id": 13860427,
  "name": "Conan the Barbarian (dvd_video)",
  "productErrors": [
    {
      "productPriceError": "price not found in data store"
    }
  ]
}
```

- If the price is not available in data store and name is not available in redsky then response looks like this with a `404` response code
##### Request
```
curl -v http://localhost:8080/product/1386
```
##### Response
```
{
  "productErrors": [
    {
      "productPriceError": "price not found in data store"
    },
    {
      "redSkyError": "could not retrieve title from redsky: (Retries exhausted: 3/3)"
    }
  ]
}
```

- If something goes wrong with the request (like an exception was thrown or user request is badly formatted) then the response looks like this with a `400` response code
##### Request
```
curl -v http://localhost:8080/product/9hg294g
```
##### Response
```
{
    "error": "bad request"
}
```

- If user needs to update the price information of a product then they can send in a payload with the `id` of the product in the url path
##### Request
```
curl -v -XPUT http://localhost:8080/product/123 -H "Content-type: application/json" -d '{"current_price": {"currency_code": "USD"} }'
```
##### Request Payload format
```
{
  "current_price": {
    "value": 1.1,
    "currency_code": "USD"
  }
}
```
A single field (either `value` or `currency_code`) can be updated