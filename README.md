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

Once the application is running, you can exercise it end-to-end using your favourite tool. I'll use cURL and jq from the command line.

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

## openapi-generator-maven-plugin

An early decision was to copy the specification of the exposed companies API and the xml API into src/main/resources.

This was since I wanted to use the Open API generator, via a Maven plugin, to generate both the server stub and the client for the respective companies API and xml API.

I decided to copy generated code into src/main and to deactivate the plugin during the subsequent build. I was in two minds about this but ultimately given discoveries down the road this is/was a good thing.

## openapi-xml.yaml

The supplied URL did not work for me, returned a 404, so I changed it in my implementation to something that works based on trial and error using cURL. 

I needed to fix a couple of additional aspects:

1. The URL would return media type for text/plain, so I had to make the implementation liberal enough to accept this (it would not do so by default).
2. It was unsurprising that the generated XmlCompany model was not going to work, as it expected JSON, so I had to create an ActualXmlCompany type with JAXB annotations.
3. I had to modify the generated ApiClient class to use Jaxb2RootElementHttpMessageConverter so that the chosen library (RestTemplate) could deserialize the XML we would receive.
4. I had to fix a tricky Spring Boot XML runtime library dependency issue.

Naturally one could argue the openapi-xml.yaml specification is incorrect and just stop developing however I could not change the XML files, so chose to apply Postel's Law and make the implementation
liberal.

## Robustness and Error Handling

This is a record of what was done to enhance the robustness of the application.

At present we don't return the ErrorModel as we should do, as a starter, so I'm fixing this first. 

We would never want to see a stack trace exposed, a bad look.

THIS IS IN PROGRESS - appropriate error handling and the use of the Error model being worked on.