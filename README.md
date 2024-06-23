# mvnz
My [Spring Boot](https://spring.io/projects/spring-boot) implementation of a [MWNZ exercise](https://github.com/MiddlewareNewZealand/evaluation-instructions).

## Up & Running

If you simply want to run the software on your local machine skip to the [Running In Operator Mode](#Running-In-Operator-Mode) section.

## Running In Developer Mode

There are two main requirements to run this program on your local machine:

1. JDK 22 - I recommend using the [BellSoft](https://bell-sw.com/pages/downloads/#jdk-22) Liberica JDK version 22. 
2. Either using the Maven Wrapper Script (recommended) or installing Maven.

If you want to get the application up and running on your machine, use the [Apache Maven
Wrapper](https://maven.apache.org/wrapper/) and the scripts appropriate to your operating
system.

On OSX/macOS the application could be run up from the command line as follows having cloned
this repository:

```
mvnz  (git)-[main]- % ./mvnw spring-boot:run
```

## Running In Operator Mode

### Docker Engine

If you have docker engine running on your machine, you can run up the software as follows.

[There are two images on Docker Hub](https://hub.docker.com/r/nicodewet/mwnz/tags), one for OS/ARCH linux/arm64 and one for 
OS/ARCH linux/amd64. Use the one appropriate to your machine. If you are on a Mac with an M1 processor for example you'll use 
the arm64 image.

#### Running on amd64

```
% docker pull nicodewet/mwnz:latest-amd64
% docker run --rm -it -p8080:8080 nicodewet/mwnz:latest-amd64
```

#### Running on arm64

```
% docker pull nicodewet/mwnz:latest-arm64
% docker run --rm -it -p8080:8080 nicodewet/mwnz:latest-arm64
```

## Quick Exercises

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

This is a record of the decisions made, with motivation, and some fixes that were applied
in order to deliver a working implementation.

## openapi-generator-maven-plugin

An early decision was to copy the specification of the exposed companies API and the xml API into src/main/resources.

This was since I wanted to use the [OpenAPI generator](https://openapi-generator.tech/), via a Maven plugin, to generate both the server 
stub and the client for the respective companies API and xml API.

I decided to copy generated code into src/main and to deactivate the plugin during the subsequent build. I was in two minds about this but 
ultimately given discoveries down the road this is/was a good thing.

### openapi-xml.yaml

The [URL supplied in the OpenAPI specification](https://raw.githubusercontent.com/MiddlewareNewZealand/evaluation-instructions/blob/main) 
did not work for me, returned a 404, so I changed it in my implementation to something that works based on trial and error 
using cURL. 

I needed to fix a couple of additional aspects:

1. The URL would return media type for text/plain, so I had to make the implementation liberal enough to accept this (it would not do so by default).
2. It was unsurprising that the generated XmlCompany model was not going to work, as the generated implementation expected JSON, so I had to create an ActualXmlCompany type with JAXB annotations.
3. I had to modify the generated ApiClient class to use Jaxb2RootElementHttpMessageConverter so that the chosen library (RestTemplate) could deserialize the XML we would receive.
4. I had to fix a tricky Spring Boot XML runtime library dependency issue.
5. The generated code hardcoded the basePath of the XML API, naturally this needed to be attended to in order to replace it with the URL of a test double.

Perhaps one could argue the openapi-xml.yaml specification is incorrect and just stop developing however I could not change the XML files, 
so chose to apply [Postel's Law](https://en.wikipedia.org/wiki/Robustness_principle) and make the implementation liberal.

I've noted the XML Object section of the Open API v3.0.2 specification, as well as the use of application/xml in the openapi-xml.yaml 
specification. Parking that, I'm still curious as to whether Open API Generator would be able to generate an implementation that works so 
that steps 2 to 4 are not necessary.

### Should I continue using OpenAPI Generator? What would I change, if anything?

It's important to remember we inherited two specifications in this implementation. Also, I don't use OpenAPI Generator day in, day out. 
Also, it may well be that I used a suboptimal configuration.

In general though, I do want to know whether a given OpenAPI specification will work for me, is generating code even possible? Is the 
specification valid? It is a measure of quality and care (by whoever produced the specification).

There are some things I would want to do immediately:
1. Remove all the redundant code I've inherited, this could entail using openapi-generator-maven-plugin as intended.
2. Add steps to validate both OpenAPI specifications (as a sanity check, ideally an author would do so, but it's not always the case).
3. Investigate whether I could have done better in terms of using the generator for the XML client
4. Delete the bulk of the dead code in the xml client package, I'd largely start from scratch, I don't think using the generator helps here.

## Robustness and Error Handling

This is a record of what was done to enhance the robustness of the application.

We're using Spring's Aspect Oriented Programming (AOP) abilities and dealing with a subset of potential cases in
RestResponseEntityExceptionHandler. 

In this class we use the Error model as specified in the openapi-companies.yaml specification.

This work is not done though, we need to use the Error model for additional obvious validation errors and naturally
we need to add test cases. As an additional TODO we may want to add appropriate backoff algorithms (ala resilience4j)
but this has not been a higher priority than basic functional correctness in my mind.

Telemetry using a scraping endpoint is not something we're building in right now, but it would be wise if one were to 
take this code to production. The latter would naturally include alerting.

### ProblemDetail - RFC 9457

This is an aside, but as per [this example](https://stackoverflow.com/a/74998726/433900), it's worth remembering Spring provides an RFC
9457 compliant 
[ProblemDetail class](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/ProblemDetail.html) and 
it is used by the framework by default (e.g. when a path parameter uses an incorrect type).

## Browser Component Considerations

The provided companies API is being called from a browser component in the instructions. At present I have not provided headers for 
Cross-Origin Resource Sharing (CORS) in the response to API calls.

As a TODO, I need to consider Cross-Origin Resource Sharing (CORS) when producing a RESTful web API that will be callable from a browser. 
CORS is a security feature implemented by browsers to restrict how resources on one web page can be requested from another domain. If the 
companies API and the client consuming the API are on different domains, CORS needs to be handled properly.

See [this guide that is relevant to Spring Boot](https://spring.io/guides/gs/rest-service-cors).

## Testing

I'm not sure unit testing would be a good fit with the codebase as it is now, not just because we don't have a domain layer, but also
because it would make refactoring harder (e.g. we may want to remove RestTemplate).

I opted to start with [wiremock](https://wiremock.org/) because I'm expecting to do significant 
refactoring given the nature of the generated code and so thought testing layers within the application is not wise.

Please see [CompaniesApiControllerWireMockTests](src/test/java/com/thorgil/mwnz/CompaniesApiControllerWireMockTests.java).

As a TODO, additional test cases should be added to exercise the CompaniesApiController to ensure the Error model is returned in certain 
cases such when the caller use and incorrect type - using Wiremock is unnecessary here.

## Appendix

In this section I'm including reference material to record the journey.

### Docker Imaging Process

I was ultimately interested leveraging [GraalVM JDK](https://www.graalvm.org/)'s ahead-of-time Native Image compilation process but 
abandoned this as I felt there were too many developer experience issues to warrant spending further time on it. Due to time constraints, 
to continue with building a native image with GraalVM it had to work flawlessly with no additional effort on my part.

I ultimately shifted my attention to what I have realised is now the [Spring Quickstart](https://spring.io/quickstart) recommended JDK, the 
BellSoft JDK, and the associated images produced by BellSoft.

#### Iteration 1

The first iteration of building an image used the fastest path to get an image published. I noticed we'd have to make enhancement in time 
and that there are a number of enhancements that should be made ASAP (e.g. producing an image with known vulnerabilities is something I'd 
like to avoid).

The exact publishing process is documented below.

```
% mvn spring-boot:build-image
% docker run -it -p8080:8080 mwnz:0.0.1-SNAPSHOT
% docker image tag mwnz:0.0.1-SNAPSHOT nicodewet/mwnz:latest-amd64
% docker push nicodewet/mwnz:latest-amd64
% docker rmi mwnz:0.0.0-SNAPSHOT
% docker rmi nicodewet/mwnz:latest-amd64
```

This process was slow and produced an amd64 image which is not what I was expecting, so I tried other options.

#### Iteration 2

The second iteration focussed on getting an arm64 image published as quickly as possible. I used a Dockerfile here which differs
from the previous process. I had to park image security concerns and other matters.

```
% ./mvnw clean install
% docker build -t nicodewet/mwnz:latest-arm64 .
% docker push nicodewet/mwnz:latest-arm64
```

The base image was: eclipse-temurin:22.0.1_8-jre-ubi9-minimal

At this stage, I was wondering whether we couldn't do better, I wanted a smaller image size if possible and ideally a 0 vulnerability 
guarantee.

##### Iteration 3 - BellSoft Liberica JRE

The final iteration of the image was to move to using a BellSoft base image utilising the Liberica JRE.

This resulted in a near 50% saving in image size which is compelling.

See [bellsoft/liberica-openjre-alpine-musl](https://github.com/bell-sw/Liberica/tree/master/docker/repos/liberica-openjre-alpine-musl) and
then also [BellSoft's Docker Hub images overview](https://bell-sw.com/blog/bellsoft-s-docker-hub-images-overview/).

Building the amd64 image was done as follows, as an example, 
[there may be better ways](https://docs.docker.com/build/building/multi-platform/).

```
% docker build --platform linux/amd64 .
% docker image tag b26b6f9f0d6910dfc5dd4529fd17292ea2aaf98a82a8b2e085ad949fe12c9bd8 nicodewet/mwnz:latest-amd64
% docker push nicodewet/mwnz:latest-amd64
```

I chose to leverage [Docker Scout](https://www.docker.com/products/docker-scout/) Image analysis on Docker Hub. There are 4 medium 
severity vulnerabilities per image as the time of writing. 

Ideally I want to get those down to 0. I used the Docker Scout interface to study each one. They are  all in C libraries on BusyBox, and 
so I'm parking spending further time on this at this stage. The latter is because  as we have no fixable critical and high vulnerabilities 
to attend to and so are policy compliant.

I will however attend to the remaining supply chain attestation policy violation reported by Docker Scout when time allows.