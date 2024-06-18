# mvnz
My Spring-Boot implementation of a [MWNZ exercise](https://github.com/MiddlewareNewZealand/evaluation-instructions).

## Up & Running

If you want to get the application up and running on your machine, use the [Apache Maven
Wrapper](https://maven.apache.org/wrapper/) and the scripts appropriate to your operating
system.

On OSX/macOS the application could be run up from the command line as follows having cloned
this repository:

```
mvnz  (git)-[main]- % ./mvnw spring-boot:run
```

Once the application is running, you can exercise it end-to-end using your favourite tool. We'll use cURL and jq from the command line.

```
% curl --silent http://localhost:8080/v1/companies/1 | jq 
{
  "id": 1,
  "name": "MWNZ",
  "description": "..is awesome"
}
```

```
% curl --silent http://localhost:8080/v1/companies/2 | jq
{
  "id": 2,
  "name": "Other",
  "description": "....is not"
}
```

## Decisions and Fixes

This is a record of the decisions made, with motivtion, and some fixes that were applied
in order to deliver a working implementation.

THIS IS IN PROGRESS.

## Robustness and Error Handling

This is a record of what was done to enhance the robustness of the application.

THIS IS IN PROGRESS - appropriate error handling and the use of the Error model being worked on.