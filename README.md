# commons-rest

![logo](assets/commons-logo.svg)

![build](https://github.com/rocketbase-io/commons-rest/actions/workflows/ci.yml/badge.svg)
[![Maven Central](https://badgen.net/maven/v/maven-central/io.rocketbase.commons/commons-rest)](https://mvnrepository.com/artifact/io.rocketbase.commons/commons-rest)

Focus on tough problems and not on CRUD that the main focus of commons-rest.
We [@rocketbase.io](https://www.rocketbase.io) develop many microservices and tried many tools and projects. All of them
didn't matched our needs. By using for example [spring-data-rest](https://projects.spring.io/spring-data-rest/) you
loose flexibility when you leave the basic path or by using [jHipster](http://www.jhipster.tech/) you have a full blown
setup with many dependencies. Commons-rest focus on CRUD and leaves flexibility to you as developer. Additionally we've
crafted a yeoman [project and service generator](https://github.com/rocketbase-io/generator-spring-rest-commons) in
order to write less code :)

The implementation bases on spring-boot: mainly on **spring-mvc** and **spring-data**

We believe in separation of Entity and DTO. We go one step forward and separate also the DTO into Read (response
structure) and Write (create/update structure). This has many advantages for example separation of concern and allow
edit by reference id and response with object. Furthermore it improves readability.

Conversion between each object can be automatically generated with [mapstruct](http://mapstruct.org/) see sample-project
for details.

**Features:**

* basic DTOs as per example a missing PageableResult
* custom RuntimeExceptions, ExceptionHandler and BeanValidationExceptions
* abstract CRUD controller also for parent child situations
* abstract CRUD resources to consume REST-Services
* prodivded a [project and service generator](https://github.com/rocketbase-io/generator-spring-rest-commons) via yeoman
* provided a jwt security module that is simply pluggable
  name [commons-auth](https://github.com/rocketbase-io/commons-auth)

## documentation

Within the [wiki-pages](https://github.com/rocketbase-io/commons-rest/wiki) you can find some explanations to the
different classes, helpers and dtos.

## module overview

### commons-rest-api

This module provides some useful runtime exceptions like NotFoundException and basic DTO classes. Mainly ErrorResponse
for rending details to error and also field-validation exceptions (this is handled a BeanValidationExceptionHandler
provided by rest-server) and PageableResult to provide results in a paged wrapper. Additionally, you can find abstract
implements of resources to consume REST-Services within java-code by use of RestTemplate and Jackson.

### commons-rest-server

Containing ExceptionHandlers for common errors like BeanValidationExceptions or the custom NotFoundException. Abstract
classes to implement CRUD SpringRestController. Also a parent child solution is provided.

### commons-errorpage

Simple designed error pages for 400, 401, 403, 404 and 500 error-codes.

### commons-rest-openapi

Library to build rest-clients + react-query hooks with custom annotations and converts.

| property                                     | default                                    | explanation                                                |
|----------------------------------------------|--------------------------------------------|------------------------------------------------------------|
| commons.openapi.generator.base-url           | /api                                       | used as prefix for alle urls                               |
| commons.openapi.generator.group-name         | ModuleApi                                  | client generator added all "methods" within one group      |
| commons.openapi.generator.hook-folder        | hooks                                      | folder within zip                                          |
| commons.openapi.generator.client-folder      | clients                                    | folder within zip                                          |
| commons.openapi.generator.model-folder       | model                                      | folder within zip                                          |
| commons.openapi.generator.model-create       | true                                       | should models get generated                                |
| commons.openapi.generator.model-imports      | _                                          
 unset_                                       | list that will get added to model/index.ts |
| commons.openapi.generator.default-stale-time | 2                                          | default value for infinite + query hook (when value is -1) |

### commons-rest-hashids

Implementation for obfuscatedId interface introduced within the api. Uses [hashids](https://hashids.org/java/) as
library to obfuscate long ids.

| property                | default                              | explanation                                                                                                                                                |
|-------------------------|--------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| hashids.salt            |                                      | salt for hashIds                                                                                                                                           |
| hashids.minHashLength   | 8                                    | min length of hasid                                                                                                                                        |
| hashids.alphabet        | abcdefghijklmnopqrstuvwxyz1234567890 | alphabet of hashid (by default we've skipped uppercase)                                                                                                    |
| hashids.handler.enabled | true                                 | enable/disable ExceptionHandler for ObfuscatedDecodeException                                                                                              |
| hashids.invalid.allowed | false                                | ObfuscatedIdSupport will return NotFound in invalid case. When allowed invalid ObfuscatedId will get inject as Parameter text is available but id is null! |

## commons-rest-logging-aspect

Adds a RequestLoggingAspect that wraps around all RestController Mappings and loggs: method, path, parameter,
duration...

```
GET /api/company ツ username 🕓 61 ms ⮐ find({}) ⮑ PageableResult(totalElements=100, totalPages=4, page=0, pageSize=25, content=[CompanyRead(id=5dc3...
```

Furthermore you can annotate a Service Method with @Loggable and get a duration tracking as well.
**It's only working on Services that are wrappable by aspect**. So you can only use it on Services calls from other
services - no internal calls within the same Service.

```java

@Service
public class SampleService {

    @SneakyThrows
    @Loggable
    public void exampleService(Instant startExecution) {
        TimeUnit.SECONDS.sleep(2);
    }
}
```

```
exampleService(2019-11-07T12:19:08.800Z) ツ username 🕓 2 sec 9 ms
```

| property                      | default | explanation                                                                         |
|-------------------------------|---------|-------------------------------------------------------------------------------------|
| commons.logging.mvc.enabled   | true    | in case you only want to use Loggable Method aspect - disable RestController aspect |
| commons.logging.trim          | true    | trim result                                                                         |
| commons.logging.trimLength    | 100     | trim after string length                                                            |
| commons.logging.duration      | true    | track duration                                                                      |
| commons.logging.audit         | true    | when AuditorAware is present log value of                                           |
| commons.logging.args          | false   | log each args.toString() with trimLength                                            |
| commons.logging.result        | false   | log result.toString() with trimLength                                               |
| commons.logging.query         | true    | add query parameter to url                                                          |
| commons.logging.logLevel      | DEBUG   | level to log a normal hit                                                           |
| commons.logging.errorLogLevel | WARN    | level to log an error hit                                                           |

### how to work within spring-webflux

Normally the RequestLoggerAspect would log only the time of returning a Mono. If you would like to get the time for
processing the request you need to create your own Aspect that could look like this.

```java

@Aspect
public class FluxLogger extends AbstractRequestLogger {


    public FluxLogger(AuditorAware auditorAware, LoggableConfig config) {
        super(auditorAware, config);
    }

    @Around("execution(* *(..)) && @annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public Object wrapMethod(ProceedingJoinPoint point) throws Throwable {
        Method method = ((MethodSignature) point.getSignature())
                .getMethod();

        return this.wrap(point, method);
    }

    private Object wrap(ProceedingJoinPoint point, Method method) throws Throwable {
        long start = System.currentTimeMillis();

        Logger log = getLog(point);
        try {
            Optional<?> currentAuditor = getAuditorAware().getCurrentAuditor();

            Object result = point.proceed();
            if (result instanceof Mono) {
                Mono<?> requestMono = (Mono<?>) result;
                Mono<? extends Tuple2<Long, ?>> elapsed = requestMono.elapsed();

                if (isLogEnabled(log, getConfig().getLogLevel())) {
                    elapsed.subscribe(o -> {
                        Long elapsedTime = o.getT1();
                        afterSuccess(log, point, method, System.currentTimeMillis() - elapsedTime, result, currentAuditor);
                    });
                }
                if (isLogEnabled(log, getConfig().getErrorLogLevel())) {
                    requestMono.toFuture()
                            .exceptionally(throwable -> {
                                logError(point, getConfig(), start, log, throwable);
                                return null;
                            });
                }
            }
            return result;
        } catch (Throwable ex) {
            logError(point, getConfig(), start, log, ex);
            throw ex;
        }
    }

}
```

## commons-rest-sample

Sample spring-boot application to demonstrate the use of the provided commons-rest libraries.

## configuration

This module uses the auto configuration feature of spring-boot-starter so that all necessary beans will get configured
automatically.
Nevertheless you can customize the configuration by the following properties

| property                               | default | explanation                                                                                                   |
|----------------------------------------|---------|---------------------------------------------------------------------------------------------------------------|
| locale.resolver.enabled                | true    | enable/disable default configuration of the LocaleResolver                                                    |
| locale.resolver.default                | en      |                                                                                                               |
| locale.resolver.supported              |         | you can specify a comma separated list of locales                                                             |
| handler.badRequest.enabled             | true    | enable/disable ExceptionHandler for BadRequestException                                                       |
| handler.notFound.enabled               | true    | enable/disable ExceptionHandler for NotFoundException                                                         |
| handler.beanValidation.enabled         | true    | enable/disable ExceptionHandler for MethodArgumentNotValidException (bean validation issues from spring-boot) |
| handler.insufficientPrivileges.enabled | true    | enable/disable ExceptionHandler for InsufficientPrivilegesException                                           |

### The MIT License (MIT)

Copyright (c) 2019 rocketbase.io

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
