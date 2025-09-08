IVOA Core
=========

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


