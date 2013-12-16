Airline and Travel Agency services
----------------------------------

This folder contains two examples of very simple web services (with fake business logic).
These web services are intended to be used in EE tests.


Building WARs for Airline and Travel Agency services
----------------------------------------------------

```
Airline$ mvn install
TravelAgency$ mvn package
```

WARs are generated on Airline/target and TravelAgency/target folders.


Building JARs for Airline and Travel Agency services
----------------------------------------------------

Edit the pom file to change the "packaging" from "war" to "jar".
After this, it is enough to run ==mvn clean package==. 
Do not run ==mvn install== to avoid messing up EE dependencies.
The JAR file will be generated on the target folder.


Downloadable packages
---------------------

The JARs and WARs packages of Airline and Travel Agency services are available on:

http://valinhos.ime.usp.br:54080/enact_test/v3/


