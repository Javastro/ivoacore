API Documentation
=================

The complete Java API reference documentation is generated from source code
comments using Javadoc. It covers all public classes and interfaces in the
IVOACore modules.

`View the Java API Documentation (Javadoc) <javadoc/index.html>`_

The Javadoc is aggregated from all modules:

* ``org.javastro.ivoacore.common`` — shared utilities
* ``org.javastro.ivoacore.vosi`` — DALI/VOSI implementation
* ``org.javastro.ivoacore.uws`` — UWS job framework
* ``org.javastro.ivoacore.tap`` — TAP protocol
* ``org.javastro.ivoacore.pgsphere`` — PgSphere/Hibernate integration
* ``org.javastro.ivoacore.client.registry`` — registry client

Referencing Java Types
----------------------

Within this documentation, Java types from IVOACore can be referenced using
the ``java:`` domain provided by the ``javasphinx`` Sphinx extension. For
example:

.. code-block:: rst

   :java:type:`org.javastro.ivoacore.uws.Job`

Cross-references like this link directly to the relevant section of the
aggregated Javadoc.
