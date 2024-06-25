# mwnz

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

We're using Spring's Aspect Oriented Programming (AOP) abilities and are dealing with a subset of potential cases in RestResponseEntityExceptionHandler. 

In this class we use the Error model as specified in the openapi-companies.yaml specification.

I have used the Error model for additional obvious validation errors and naturally have added test cases. As a TODO we may want to add appropriate backoff algorithms (ala resilience4j) but this has not been a higher priority than basic functional correctness in my mind.

Telemetry using a scraping endpoint is not something we're building in right now, but it would be wise if one were to take this code to production. The latter would naturally include alerting.

### ProblemDetail - RFC 9457

This is an aside, but as per [this example](https://stackoverflow.com/a/74998726/433900), it's worth remembering Spring provides an RFC
9457 compliant 
[ProblemDetail class](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/ProblemDetail.html) and 
it is used by the framework by default (e.g. when a path parameter uses an incorrect type).

## Browser Component Considerations

The provided companies API is being called from a browser component in the instructions.

I have considered Cross-Origin Resource Sharing (CORS) as one should when producing a RESTful web API that will be callable from a browser. 
CORS is a security feature implemented by browsers to restrict how resources on one web page can be requested from another domain. If the 
companies API and the client consuming the API are on different domains, CORS needs to be handled properly.

The following CORS configuration is in place at the time of writing:

1. All origins are allowed (a request to the specific pathPattern can originate from any origin).
2. For the path /v1/companies/* with the * meaning match zero or more characters. See [AntPathMatcher](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/util/AntPathMatcher.html) for authoritative Ant-style path pattern documentation).
3. The time that the preflight response is cached (maxAge) is 30 minutes.

In the context of Cross-Origin Resource Sharing (CORS), "origin" refers to the combination of the protocol (like HTTP or HTTPS), the 
domain (such as www.example.com), and the port (like 80 for HTTP or 443 for HTTPS) from which a web request is made.

For example, if your web page is loaded from https://www.mysite.com:443, this is considered the origin. Any resource request made from 
this web page to another location (like an API) that has a different protocol, domain, or port is considered a request to a different 
origin.

So, our configuration specifies that a web page loaded from any origin can interact with a specific resource in our API without 
explicit permission.

### Experimentation

[This Spring Boot guide](https://spring.io/guides/gs/rest-service-cors) provides sample html and javascript that makes it extremely easy to experiment
with CORS configuration. The sample files, which I have modified and supplied below, simply need to be placed in a top level 
directory named **public**.

index.html
```html
<!DOCTYPE html>
<html>
<head>
    <title>Hello CORS</title>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
    <script src="hello.js"></script>
</head>

<body>
<div>
    <p class="company-id">The ID is </p>
    <p class="company-name">The content is </p>
    <p class="company-description">The content is </p>
</div>
</body>
</html>
```
hello.js
```javascript
$(document).ready(function() {
    $.ajax({
        url: "http://localhost:8080/v1/companies/1"
    }).then(function(data, status, jqxhr) {
       $('.company-id').append(data.id);
       $('.company-name').append(data.content);
       $('.company-description').append(data.description);
       console.log(jqxhr);
    });
});
```
You can run up the sample website as follows:
```
% ./mvnw spring-boot:run -Dspring-boot.run.jvmArguments='-Dserver.port=9001'
```
### CORS Tests

These need to be added as a TODO, the CORS configuration has is present in main class OpenApiGeneratorApplication.

## Testing

I'm not sure unit testing would be a good fit with the codebase as it is now, not just because we don't have a domain layer, but also
because it would make refactoring harder (e.g. we may want to remove RestTemplate).

I opted to start with [wiremock](https://wiremock.org/) because I'm expecting to do significant 
refactoring given the nature of the generated code and so thought testing layers within the application is not wise.

Please see [CompaniesApiControllerWireMockTests](src/test/java/com/thorgil/mwnz/CompaniesApiControllerWireMockTests.java).

Additional test cases have been added to exercise the CompaniesApiController to ensure the Error model is returned in certain 
cases such as when the caller use and incorrect type - using Wiremock is unnecessary here and so I've not used it.

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

### Production Considerations

Here I provide aspects to consider before taking this application to production.

#### Quality Attribute Requirements Gathering

This is a generic consideration, that is to consider the quality attribute requirements of our application in the various context(s) 
where it may provide service. This will help focus our attention.

#### Software Distribution

Though I have utilised GitHub and Docker Hub, neither may be optimal long term. We should also provide a clear versioning strategy
which may entail using semantic versioning.

#### Supply Chain Attestation

This is a security consideration. Software supply chain attestation may be required when supplying the software to third parties
and is a general best practice. I've used Docker Scout and the task here would be to pass the provided policy check.

#### Pipeline Establishment

Building the software from localhost is clearly suboptimal. A suitable CI/CD toolchain should be used.

#### Judiciously Include Spring Boot Production-ready Features

Spring Boot's [Production-ready Features documentation](https://docs.spring.io/spring-boot/reference/actuator/index.html) serves as an 
effective checklist of some aspects to consider before going to production. 

Which elements to focus on first is a matter of context. I would start by focussing on three aspects which I have numbered.

The first listed item is generic.

#### Judiciously Configure Relevant Spring Boot Common Application Properties

Spring Boot auto configures most application properties but also supplies a wealth of 
[Common Application Properties](https://docs.spring.io/spring-boot/appendix/application-properties/index.html).

All of these properties can be modified inside the application's properties file and in turn fed in via environment variables to allow for
dynamic configuration.

##### 1. Observability

Observability refers to making the internal state of a running system visible via the three pillars of logging, metrics and traces. 
Naturally in doing so we would factor in the CIA Triad, referring to the Confidentiality, Integrity and Availability of information.

Spring Boot Actuator provides [endpoints](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html) that can be scraped by 
monitoring systems such as Prometheus and more. The task here is to provide suitable telemetry guidance with associated suitable 
alerting.

I would consider whether investing in leveraging Spring Boot actuator's [support for OpenTelemetry](https://docs.spring.io/spring-boot/reference/actuator/observability.html#actuator.observability.opentelemetry) 
is warranted. 

##### 2. Orchestrator Configuration

There are many cloud native application orchestrators, for example Kubernetes. Aspects such as application liveness and readiness 
via health endpoints and associated configuration is our concern here.

Additional orchestrator considerations is auto scaling. If we are able to scale horizontally the orchestrator would need to know when to 
scale up and down. There are a variety of means of configuring auto scaling, for example in the case of Kubernetes the both 
[Horizontal Pod Autoscaling](https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/) and [KEDA based HTTP workload
autoscaling options](https://keda.sh/docs/2.14/faq/#can-i-scale-http-workloads-with-keda-and-kubernetes) are available.

##### 3. Metrics Integration - Key JVM Metrics

Monitoring essential JVM metrics is crucial for maintaining the performance, stability, and health of server-side Java applications.

The task here is to ensure the key JVM (Java Virtual Machine) metrics are exposed in 
the [Supported Monitoring System](https://docs.spring.io/spring-boot/reference/actuator/metrics.html#actuator.metrics.export).

Some key JVM metrics and the reasons why they are important to monitor are:

1. **Heap Memory Usage**
    - **What to Monitor**: Current usage, maximum usage, and garbage collection (CG) details.
    - **Why**: Helps in identifying memory leaks and understanding memory consumption patterns. Monitoring heap usage and GC activity can 
   prevent OutofMemoryErrors and ensure efficient memory management.
2. **Garbage Collection (GC) Metrics**
   - **What to Monitor**: Number of collections, time spent in GC, and frequency of allocations.
   - **Why**: Excessive GC can lead to performance issues and application pauses. Monitoring GC metrics helps in tuning GC settings and 
   improving application performance.
3. **Thread Count**
   - **What to Monitor**: Current number of active threads, peak number of threads, and deadlocked threads.
   - **Why**: Monitoring threads helps in identifying thread leaks, deadlocks, and ensuring that the application is not overwhelmed by too 
   many threads, which can lead to resource exhaustion.
4. **CPU Usage**
    - **What to Monitor**: CPU usage by the JVM process.
    - **Why**: High CPU usage can indicate performance bottlenecks or inefficient code. Monitoring CPU usage helps in identify and addressing
   such issues.
5. **Class Loading**
   - **What to Monitor**: Number of classes loaded and unloaded, and total classes currently loaded.
   - **Why**: Monitoring class loading helps identify potential class loader memory leaks and understanding application behaviour related to 
   class loading and unloading.
6. **File Descriptors**
   - **What to Monitor**: Number of open file descriptors.
   - **Why**: Monitoring file descriptors is essential to ensure that the application does not exceed the limit imposed by the operating 
   system, which can lead to failures in opening files or network connections.
7. **JVM Uptime**
   - **What to Monitor**: Total uptime of the JVM.
   - **Why**: Provides information about the stability and longevity of the JVM process. Frequent restarts indicate underlying issues that 
   need attention.
8. **Buffer Pools**
   - **What to Monitor**: Usage of direct and mapped buffer pools.
   - **Why**: Monitoring buffer pools helps in understanding the allocation and usage of direct memory, which is not part of the heap but 
   can improve overall memory usage.
9. **Metaspace (or Permanent Generation in older JVMs)**
    - **What to Monitor**: Usage of Metaspace or PermGen memory.
    - **Why**: Metaspace/PermGen space is used for storing class metadata. Monitoring its usage helps in identifying memory leaks related 
   to class loading and ensuring the space is not exhausted.
10. **JVM Flags and System Properties**
    - **What to Monitor**: JVM arguments and system properties.
    - **Why**: Understanding the JVM configuration helps in debugging issues and optimizing performance based on how the JVM is tuned and 
    configured.

#### Embedded Web Server Decision

##### Consider Alternatives

We've used the default thread-per-request model used by Tomcat. It is suitable for many traditional web applications and 
provides a straightforward, familiar programming model. However, for applications requiring high concurrency and efficient resource 
utilization, especially those involving many I/O-bound operations, a non-blocking server like Reactor Netty might be a better 
choice. So, we should consider the trade-offs when it comes to our embedded web server decision.

It could also be that a "serverless" service provider approach 
utilising [Spring Cloud Function](https://spring.io/projects/spring-cloud-function) would be more appropriate and the trade-offs in 
doing so should be considered.

##### Tomcat Configuration

Spring Boot auto-configuration is useful for a quick start however when going to production we should know exactly what this 
default configuration is, what it's impacts are and how to change it if need be. The impacts of the thread-per-request model 
and blocking I/O model need to be understood.

###### Tomcat HTTP Connector Thread Configuration

Irrespective of whether we have a single instance or vertical or horizontal auto scaling strategy, the need to keep a handle on the 
relationship between our current thread pool configuration and the dynamic server load.

Why? Because it's easy to forget about it or not think about in the first instance. In my own business, the e-commerce application that I 
wrote failed to scale when it was at it's most successful in terms of revenue, i.e. **the worst possible time**. It literally meant 
customers failed to enter the store because of the load and they were not happy (even worse they had to phone to ask us what is going on). 
This happened when fronting Tomcat with Apache. We used the default 
[AJP thread pool configuration](https://tomcat.apache.org/tomcat-9.0-doc/config/ajp.html) and essentially forgot this critical 
thread pool even existed - I've never forgotten this experience.

Particularly important thread pool configuration elements are as follows. I'n not going to specify mitigation strategies for brevity’s sake
as there are a number.

1. **MaxThreads**: The maximum number of threads in the pool. Defaults to 200 with Spring Boot and configured via 
the application property [server.tomcat.threads.max](https://docs.spring.io/spring-boot/appendix/application-properties/index.html#application-properties.server.server.tomcat.threads.max).

2. **MinSpareThreads**: The minimum number of idle threads that should be able to handle requests. Defaults to 10 with Spring Boot and 
configured via the application property [server.tomcat.threads.min-spare](https://docs.spring.io/spring-boot/appendix/application-properties/index.html#application-properties.server.server.tomcat.threads.min-spare).

3. **AcceptCount**: The maximum queue length for incoming connection requests when all possible request processing threads are in use. 
Defaults to 100 with Spring Boot and configured via the application property [server.tomcat.accept-count](https://docs.spring.io/spring-boot/appendix/application-properties/index.html#application-properties.server.server.tomcat.accept-count).
When the acceptCount limit is reached, any new connection requests will be immediately rejected. The rejection typically results in the 
client (possibly an end user's web browser) receiving a connection error such as "connection refused" or "connection timed out message", 
depending on how the client's connection handling is implemented.

#### Caching

In this application we use HTTP GET to fetch documents from a downstream server. It may be that we can safely cache the replies
and don't need to call the downstream server upon every companies request. If the former is safe to do we should certainly 
introduce caching as there are both upstream and downstream benefits.

##### Resilience

Though this has been mentioned elsewhere, including a fault tolerance library and judiciously enhancing the application in terms of 
downstream calls should be factored in before going to production. [Resilience4j](https://resilience4j.readme.io/docs/getting-started) is 
an excellent resource with helper modules that can be used to implement particular resilience patterns (e.g. retry).

#### CORS

Rather than allowing requests from any origin, we may want to lock this down to specific origins in the allowedOrigins method
that could be loaded from an external source at runtime.

#### OpenAPI Specification

The generic consideration is how will we distribute the OpenAPI specification to clients. There are also versioning, 
maintenance and supportive documentation consideration. These fall into the broader category of API management 
but here we're focussing on the OpenAPI Specification. Would it be prudent or desirable to provide a Swagger UI 
interface?

#### Spring Boot Production Packaging Recommendations

Spring Boot provides a number of production [Packaging Recommendations](https://docs.spring.io/spring-boot/reference/packaging/index.html).

Immediate recommendations that I would want to implement include, but are not limited to:

- [Unpacking the Executable JAR](https://docs.spring.io/spring-boot/reference/packaging/efficient.html#packaging.efficient.unpacking) - reduces startup time.
- [Layered Docker Image](https://docs.spring.io/spring-boot/reference/packaging/container-images/efficient-images.html#packaging.container-images.efficient-images.layering) - provides several advantages that make it better for development and deployment.

#### Additional Security Considerations

Though we are dealing with a simple application, in a generic sense there are many security controls we have not covered. For example,
we may want to configure HTTPS in production as it provides encryption, data integrity, and authentication for secure communications.