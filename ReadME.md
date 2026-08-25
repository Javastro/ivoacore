IVOA Core
=========

[![Java CI with Gradle](https://github.com/Javastro/ivoacore/actions/workflows/gradle.yml/badge.svg)](https://github.com/Javastro/ivoacore/actions/workflows/gradle.yml)

The libraries in this collection implement some core ivoa service functionality without being directly coupled to a particular web interface/instance. The design of the libraries tries to take into account that services are likely to be deployed in a distributed asynchronous microservices style environment.



The libraries are

* common - functionality that could be used anywhere
* dal - implements parts of [DALI](https://www.ivoa.net/documents/DALI/) and [VOSI](https://www.ivoa.net/documents/VOSI/)
* uws - implements [UWS](https://www.ivoa.net/documents/UWS/)
* tap - implements [TAP](https://www.ivoa.net/documents/TAP/)

then there are client libraries for the protocols in the [clients](clients) directory.

* [registry](https://www.ivoa.net/documents/RegistryInterface/)
* tap
* [VOSpace](https://www.ivoa.net/documents/VOSpace/)


## Versioning

versioning is managed with the [axion release plugin](https://axion-release-plugin.readthedocs.io/en/latest/)

### published versions
```kotlin
implementation("org.javastro.ivoa.core:common:0.9.0")
implementation("org.javastro.ivoa.core:dal:0.9.0")
implementation("org.javastro.ivoa.core:uws:0.9.0")
implementation("org.javastro.ivoa.core:tap:0.9.0")
implementation("org.javastro.ivoa.core:pgsphere:0.9.1")
implementation("org.javastro.ivoa.core.clients:registry-client:0.9.0")
implementation("org.javastro.ivoa.core.clients:tap-client:0.9.0")
implementation("org.javastro.ivoa.core.clients:vospace-client:0.9.0")

```