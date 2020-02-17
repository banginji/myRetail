# myRetail

### Instructions for running the service
Two ways to run the service:
- The project can be run using the command. The requirement for this would be running mongodb locally
>./gradlew bootRun

- The project can also be run on docker containers (service and mongodb) locally using the command
>./gradlew clean build && docker-compose up

### Features of the application
The application allows users to look up information of items if they know the item `id`

- If price is available in the data store and name is available in redsky then the response looks like this with a `200` response code
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