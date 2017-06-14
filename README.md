# DDIA
DDIA is an abbreviation for Don't Do It Again.

This project is a practice of microservice architecture aiming to provide common functionalities as microservices and being easy to add customized microserivce.

Convention: microservices don't depend on each other directly whenever possible, for instance, if a use case involves two or more microservices to complete, the coordinating code should be in the gateway. Exceptions will be explictly documented.
