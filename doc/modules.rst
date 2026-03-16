Modules
=======

IVOACore is organised as a multi-module Gradle project. Each module targets a
specific area of IVOA functionality and can be used independently.

common
------

The ``common`` module provides shared utilities used across the other modules.

**Package:** ``org.javastro.ivoacore.common``

Key classes:

* ``SecurityGuard`` — utility for security-related checks
* ``XMLUtils`` — helpers for working with XML, including XSLT 3.0 support via
  Saxon-HE

dal
---

The ``dal`` module implements parts of `DALI
<https://www.ivoa.net/documents/DALI/>`_ (Data Access Layer Interface) and
`VOSI <https://www.ivoa.net/documents/VOSI/>`_ (VO Support Interfaces).

**Package:** ``org.javastro.ivoacore.vosi``

Key classes:

* ``VOSIResource`` — base interface for VOSI-compliant resources
* ``BaseVOSIResource`` — abstract base implementation of ``VOSIResource``
* ``CapabilityBuilder`` — helper for constructing VOSI capability documents
* ``VOSIProvider`` — provider interface for VOSI services

uws
---

The ``uws`` module implements `UWS
<https://www.ivoa.net/documents/UWS/>`_ (Universal Worker Service), providing
a framework for managing long-running asynchronous jobs.

**Package:** ``org.javastro.ivoacore.uws``

Key classes and packages:

* ``Job`` — represents a UWS job
* ``JobManager`` — manages the lifecycle of UWS jobs
* ``JobFactory`` / ``BaseJobFactory`` — factory interfaces for creating jobs
* ``JobSpecification`` — describes the parameters and behaviour of a job type
* ``UWSCore`` — core UWS service implementation
* ``UWSControl`` — control interface for UWS operations
* ``ExecutionControl`` — manages execution of individual jobs
* ``description/`` — parameter and job description types
* ``environment/`` — execution environment and policy implementations
* ``persist/`` — job persistence (in-memory implementation provided)
* ``webapi/`` — JAX-RS based REST API implementation

tap
---

The ``tap`` module implements `TAP
<https://www.ivoa.net/documents/TAP/>`_ (Table Access Protocol) building on
the UWS module.

**Package:** ``org.javastro.ivoacore.tap``

Key classes:

* ``TAPJob`` — a UWS job implementing a TAP query
* ``TAPJobSpecification`` — specification for TAP jobs
* ``schema/SchemaProvider`` — interface for providing TAP schema information
* ``schema/VODMLSchemaProvider`` — schema provider backed by VO-DML models

pgsphere
--------

The ``pgsphere`` module provides Hibernate integration for `PgSphere
<https://pgsphere.github.io/>`_, the PostgreSQL spherical geometry extension.

**Package:** ``org.javastro.ivoacore.pgsphere``

Key classes:

* ``PgSphereDialect`` — Hibernate dialect with PgSphere type registrations
* ``PgSphereTypes`` — type mappings between PgSphere and Java
* ``types/`` — Java representations of PgSphere geometry types (Point, Circle,
  Ellipse, Polygon, Box)

.. note::

   The ``pgsphere`` module has its own versioning (currently ``0.9-SNAPSHOT``)
   separate from the rest of the project.

Client Libraries
----------------

Client libraries for IVOA protocols are located in the ``clients/`` subdirectory.

clients/registry
~~~~~~~~~~~~~~~~

**Package:** ``org.javastro.ivoacore.client.registry``

A client library for `IVOA Registry
<https://www.ivoa.net/documents/RegistryInterface/>`_ services implementing
the OAI-PMH-based registry interface.

Key classes:

* ``BaseRegistryClient`` — base class for registry clients
* ``BasicOAIClient`` — OAI-PMH client implementation
* ``MinimalRegistryInterface`` — minimal registry query interface
* ``OAIInterface`` — OAI-PMH interface definition

clients/tap
~~~~~~~~~~~

TAP client library (placeholder, under development).

clients/vospace
~~~~~~~~~~~~~~~

`VOSpace <https://www.ivoa.net/documents/VOSpace/>`_ client library
(under development).
