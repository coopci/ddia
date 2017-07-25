# DDIA
DDIA is an abbreviation for Don't Do It Again.

This project is a practice of microservice architecture aiming to provide common functionalities as microservices and being easy to add customized microserivce.

Convention: microservices don't depend on each other directly whenever possible, for instance, if a use case involves two or more microservices to complete, the coordinating code should be in the gateway. Exceptions will be explictly documented.

简略的使用方法：

ddia-common, ddia-gateway, user-basic, user-relation, b2c-renting 每个都是一个eclipse的java项目。

ddia-common 是基础类库，被其他几个项目依赖。（已经通过eclipse的 Java Build Path/Projects形式配置好了。） 不需要运行。

ddia-gateway 是演示用的gateway，要想正常工作，需要在运行时依赖其他的微服务类项目——换句话说，需要提供相应功能的微服务类项目已经运行起来才行。

user-basic 是微服务类项目。 提供的功能是用户基本属性，注册，登陆等功能。

user-relation  是微服务类项目。 提供的功能是用户之间的跟随关系。

b2c-renting   是微服务类项目。 提供的功能是用户自助租用/归还公司提供的设备。

目前每个微服务都依赖 监听在localhost:27017的mongodb。

最简单的试用流程是用eclipse导入这些项目，以 Run as java application 的形式 运行各个项目里的HttpServer类。 然后就可以用POSTMAN或者普通浏览器看效果了。

使用docker运行的方法:

docker pull coopci/ddia

docker run --net="host" coopci/ddia

docker ps

docker stop

从命令行添加用户并指定密码:

docker run --net="host" coopci/ddia java -classpath ./bin/ddia.jar coopci.ddia.tools.ModifyPassword $用户名 $密码

