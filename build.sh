# Attention: it may be necessary to copy the content of the repository folder to your local maven repository
# cp repository/org/ow2/ ~/.m2/repository/org/ow2/
# cp repository/com/ebmwebsourcing/ ~/.m2/repository/com/ebmwebsourcing/
mvn clean javadoc:jar source:jar install && mvn eclipse:clean eclipse:eclipse
