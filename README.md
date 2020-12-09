# myRetail

### What this application is about
This application is a poc of a products API which aggregates data (price and title) from multiple sources and returns it to the invoker in JSON format

### Tech Stack
- JDK v14
- Kotlin
- GraphQL
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
- JDK v14 (not required locally if docker is used)
- Docker

Two ways to run the service:
- The project can be run using the command. The requirement for this would be running mongodb locally
>./gradlew bootRun

- The project can also be run on docker containers (service and mongodb) locally using the command
>./gradlew clean build && docker-compose up

### Information about the graphql query models in the graphql playground
Documentation for queries and mutations available for the user along with the model details
![Query and mutation](https://github.com/banginji/myRetail/blob/master/images/docs/docs1.png?raw=true)

### Example request / response for different cases
The application allows users to look up information of items if they know the item `id`
The calls can be made to the service either using the graphql playground by navigating to `http://localhost:8080/playground` or by curl
If the playground is used then it gives the documentation on the queries that are allowed with the types

Following are examples of different scenarios with their requests and responses

###### If price is available in the data store and name is available in redsky
##### Request
```
curl -v -XPOST http://localhost:8080/graphql -H 'Content-Type: application/json' -d '{"query":"query { getProductInfo(id: 13860428) { price { currentPrice { value } error } name { name error } id } }"}'
```
##### Response
```
{
  "data": {
    "getProductInfo": {
      "price": {
        "currentPrice": {
          "value": 1193.33
        },
        "error": null
      },
      "name": {
        "name": "The Big Lebowski (Blu-ray)",
        "error": null
      },
      "id": 13860428
    }
  }
}
``` 
##### GraphQL
![Request1](https://github.com/banginji/myRetail/blob/master/images/requests/1-get%20when%20both%20exist.png?raw=true)

###### If price is available in the data store, but the name is not available in redsky
##### Request
```
curl -v -XPOST http://localhost:8080/graphql -H 'Content-Type: application/json' -d '{"query":"query { getProductInfo(id: 123) { price { currentPrice { value } error } name { name error } id } }"}'
```
##### Response
```
{
  "data": {
    "getProductInfo": {
      "price": {
        "currentPrice": {
          "value": 14.23
        },
        "error": null
      },
      "name": {
        "name": null,
        "error": "could not retrieve title from redsky"
      },
      "id": 123
    }
  }
}
```
##### GraphQL
![Request2](https://github.com/banginji/myRetail/blob/master/images/requests/2-get%20when%20red%20sky%20does%20not%20exist.png?raw=true)

###### If price is not available in the data store, but the name is available in redsky
##### Request
```
curl -v -XPOST http://localhost:8080/graphql -H 'Content-Type: application/json' -d '{"query":"query { getProductInfo(id: 13860427) { price { currentPrice { value } error } name { name error } id } }"}'
```
##### Response
```
{
  "data": {
    "getProductInfo": {
      "price": {
        "currentPrice": null,
        "error": "price not found in data store"
      },
      "name": {
        "name": "Conan the Barbarian (DVD)",
        "error": null
      },
      "id": 13860427
    }
  }
}
```
##### GraphQL
![Request3](https://github.com/banginji/myRetail/blob/master/images/requests/3-get%20when%20no%20data%20in%20data%20store.png?raw=true)

###### If the price is not available in data store and name is not available in redsky
##### Request
```
curl -v -XPOST http://localhost:8080/graphql -H 'Content-Type: application/json' -d '{"query":"query { getProductInfo(id: 789) { price { currentPrice { value } error } name { name error } id } }"}'
```
##### Response
```
{
  "data": {
    "getProductInfo": {
      "price": {
        "currentPrice": null,
        "error": "price not found in data store"
      },
      "name": {
        "name": null,
        "error": "could not retrieve title from redsky"
      },
      "id": 789
    }
  }
}
```
##### GraphQL
![Request4](https://github.com/banginji/myRetail/blob/master/images/requests/4-get%20when%20both%20dont%20exist.png?raw=true)

###### The client requests a subset of fields
##### Request
```
curl -v -XPOST http://localhost:8080/graphql -H 'Content-Type: application/json' -d '{"query":"query { getProductInfo(id: 13860427) { price { currentPrice { value } } } }"}'
```
##### Response
```
{
  "data": {
    "getProductInfo": {
      "price": {
        "currentPrice": {
            "value": 1193.33
        }
      }
    }
  }
}
```
##### GraphQL
![Request7](https://github.com/banginji/myRetail/blob/master/images/requests/7-get%20only%20specific%20fields.png?raw=true)

###### If user needs to update the price information of a product then they can send in a payload with the `id` of the product in the url path
##### Request
```
curl 'http://localhost:8080/graphql' -H 'Content-Type: application/json' -d '{"query":"mutation { updateProductInfo(id: 234, updateProductRequest: { newPrice: { value: 129.99 }}) { price { currentPrice { value } error } } }"}'
```
##### Response
```
{
  "data": {
    "updateProductInfo": {
      "price": {
        "currentPrice": {
          "value": 129.99
        },
        "error": null
      }
    }
  }
}
```
##### GraphQL
![Request5](https://github.com/banginji/myRetail/blob/master/images/requests/5-update%20existing.png?raw=true)

A single field (either `value` or `currencyCode`) can be updated

###### If product is not found in data store then error message is displayed in the response
##### Request
```
curl 'http://localhost:8080/graphql' -H 'Content-Type: application/json' -d '{"query":"mutation { updateProductInfo(id: 789, updateProductRequest: { newPrice: { value: 129.99 }}) { price { currentPrice { value } error } } }"}'
```
##### Response
```
{
  "data": {
    "updateProductInfo": {
      "price": {
        "currentPrice": null,
        "error": "price not found in data store"
      }
    }
  }
}
```
##### GraphQL
![Request6](https://github.com/banginji/myRetail/blob/master/images/requests/6-update%20for%20non%20existing.png?raw=true)