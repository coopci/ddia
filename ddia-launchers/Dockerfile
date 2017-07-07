FROM centos-java:latest

Add build/jar/ddia.jar ./bin/
Add  triple-des.key .


CMD ["java", "-classpath", "./bin/ddia.jar", "coopci.ddia.launchers.AllInOneLauncher"]

# docker build -t ddia-all-in-one .

