Overview
========

IVOACore provides a set of modular Java libraries that implement the core
functionality required to build IVOA-compliant services. The libraries target
Java 17 and are built using Gradle.

Design Goals
------------

The libraries are designed with the following goals in mind:

* **Protocol Compliance**: Full implementation of IVOA standards including UWS,
  TAP, DALI, and VOSI.
* **Decoupled from Web Frameworks**: Core logic is kept independent of any
  particular web framework or deployment model, making it easy to integrate into
  any Java service.
* **Microservices Ready**: The architecture supports distributed, asynchronous
  deployments common in modern cloud-native services.
* **Extensible**: Modules can be used independently or combined as needed.

Building
--------

The project is built with Gradle. To build all modules::

   ./gradlew build

To generate the complete documentation (Javadoc + Sphinx)::

   ./gradlew docs

To generate only the aggregated Javadoc::

   ./gradlew aggregateJavadoc

Source Repository
-----------------

The source code is hosted on GitHub at
`https://github.com/Javastro/ivoacore <https://github.com/Javastro/ivoacore>`_.

Versioning
----------

The project uses semantic versioning. Current development releases are published
with the ``-SNAPSHOT`` suffix to the UKSRC Nexus repository.
