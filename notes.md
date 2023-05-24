# re-build in 2023

building the app using relatively current tool chain and library:
- java 8
- deploy in tomcat 8

more details - tested in two environments (around 8 GB memory and 50 GB disk):
1. java 8, tomcat 8, debian 5.10 on Google Cloud

```sh
:~$ java -version
java version "1.8.0_371"
Java(TM) SE Runtime Environment (build 1.8.0_371-b11)
Java HotSpot(TM) 64-Bit Server VM (build 25.371-b11, mixed mode)
:~$ javac -version
javac 1.8.0_371

:~$ sudo java -cp /opt/tomcat/lib/catalina.jar org.apache.catalina.util.ServerInfo
Server version: Apache Tomcat/8.5.88
Server built:   Apr 14 2023 20:16:27 UTC
Server number:  8.5.88.0
OS Name:        Linux
OS Version:     5.10.0-23-cloud-amd64
Architecture:   amd64
JVM Version:    1.8.0_371-b11
JVM Vendor:     Oracle Corporation

:~$ uname -a
Linux develop-machine-java8-1 5.10.0-23-cloud-amd64 #1 SMP Debian 5.10.179-1 (2023-05-12) x86_64 GNU/Linux
```

2. java 8, tomcat 8, ubuntu 23 in VirtualBox

```sh
:~/git/geworkbench-web$ java -version
openjdk version "1.8.0_362"
OpenJDK Runtime Environment (build 1.8.0_362-8u372-ga~us1-0ubuntu1~23.04-b09)
OpenJDK 64-Bit Server VM (build 25.362-b09, mixed mode)
:~/git/geworkbench-web$ javac -version
javac 1.8.0_362

:~/git/geworkbench-web$ sudo java -cp /opt/tomcat/lib/catalina.jar org.apache.catalina.util.ServerInfo
Server version: Apache Tomcat/8.5.88
Server built:   Apr 14 2023 20:16:27 UTC
Server number:  8.5.88.0
OS Name:        Linux
OS Version:     6.2.0-20-generic
Architecture:   amd64
JVM Version:    1.8.0_362-8u372-ga~us1-0ubuntu1~23.04-b09
JVM Vendor:     Private Build

:~/git/geworkbench-web$ uname -a
Linux ***username***-VirtualBox 6.2.0-20-generic #20-Ubuntu SMP PREEMPT_DYNAMIC Thu Apr  6 07:48:48 UTC 2023 x86_64 x86_64 x86_64 GNU/Linux

:~/git/geworkbench-web$ lsb_release -a
No LSB modules are available.
Distributor ID:	Ubuntu
Description:	Ubuntu 23.04
Release:	23.04
Codename:	lunar
```

