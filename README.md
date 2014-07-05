LTM
===

LTM (Lifelong Topic Model) is an open-source Java package implementing the algorithm proposed in the paper (Chen and Liu, ICML 2014). For more details, please refer to [this paper](http://www.cs.uic.edu/~zchen/papers/ICML2014-Zhiyuan(Brett)Chen.pdf).

If you use this package, please cite the paper: __Zhiyuan Chen and Bing Liu. Topic Modeling using Topics from Many Domains, Lifelong Learning and Big Data. In Proceedings of _ICML 2014_, pages 703-711__.

If you have any question or bug report, please send it to Zhiyuan (Brett) Chen (czyuanacm@gmail.com).

## Table of Contents
- [Getting Started](#gettingstarted)
- [Commandline Arguments](#commandlinearguments)
- [## Inputs and Outputs](#inputsandoutputs)

## Getting Started

There are 2 ways to run the program:

1. Import the directory into Eclipse (__recommended__).

  _If you get the exception Java.Lang.OutOfMemoryError, please increase the Java heap memory for Eclipse: http://www.mkyong.com/eclipse/eclipse-java-lang-outofmemoryerror-java-heap-space/._
  
2. Use [Maven](http://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)

  a. Build the package.
  ```
  mvn clean package
  ```
  b. Increase the Java heap memory for Maven.
  ```
  export MAVEN_OPTS=-Xmx1024m
  ```
  c. Run the program.
  ```
  mvn exec:java -Dexec.mainClass="launch.MainEntry"
  ```
  
## Commandline Arguments
The commandline arguments are stored in global.CmdOption. If no argument is provided, the program uses the default arguments. There are several arguments that are subject to change:

1. -i: the path of input domains directory.
2. -o: the path of output model directory.
3. -nthreads: the number of threads used in the program. The program runs in parallel supporting multithreading.
4. -nTopics: the number of topics used in Topic Model for each domain.

## Inputs and Outputs




