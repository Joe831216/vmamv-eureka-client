# VMAMV
The VMAMV is a tool for monitoring microservice systems, generating visualized version-based service dependency graphs, and 
providing graph search services. The proposed scheme is called Version-based Microservice Analysis, Monitoring, and 
Visualization (VMAMV). This system automatically detects potential design problems and service anomalies and immediately 
notifies users of problems before or shortly after they occur.
# vmamv-spring-cloud-netflix-eureka-client
The VMAMV system consists of [VMAMVS (vmamv-service)](https://github.com/Joe831216/vmamv-service) and client-side libraries. 
The vmamv-spring-cloud-netflix-eureka-client is the [spring-cloud-netflix-eureka](https://github.com/spring-cloud/spring-cloud-netflix/tree/master/spring-cloud-netflix-eureka-server) 
side library of VMAMV system, which makes the Eureka server possible to automatically register and unregister itself with VMAMVS.
## How to use?
1. Using the the `@EnableVmamvRegister` Annotation on your "application class".
2. Configure the following properties:  
`vmamv.server.url` = The location of VMAMVS.  
`vmamv.system.name` = The name of monitored system.
## Example
* [cinema-eureka](https://github.com/Joe831216/cinema-eureka)
