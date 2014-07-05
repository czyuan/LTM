LTM
===

LTM (Lifelong Topic Model) is an open-source Java package implementing the algorithm proposed in the paper (Chen and Liu, ICML 2014), created by [Zhiyuan (Brett) Chen](http://www.cs.uic.edu/~zchen/). For more details, please refer to [this paper](http://www.cs.uic.edu/~zchen/papers/ICML2014-Zhiyuan(Brett)Chen.pdf).

If you use this package, please cite the paper: __Zhiyuan Chen and Bing Liu. Topic Modeling using Topics from Many Domains, Lifelong Learning and Big Data. In Proceedings of _ICML 2014_, pages 703-711__.

If you have any question or bug report, please send it to Zhiyuan (Brett) Chen (czyuanacm@gmail.com).

## Table of Contents
- [Quick Start](#quickstart)
- [Commandline Arguments](#commandlinearguments)
- [Input and Output](#inputandoutput)
- [](#inputandoutput)

<a name="quickstart"/>
## Quick Start

2 quick start options are available:

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

<a name="commandlinearguments"/>
## Commandline Arguments
The commandline arguments are stored in global.CmdOption. If no argument is provided, the program uses the default arguments. There are several arguments that are subject to change:

1. -i: the path of input domains directory.
2. -o: the path of output model directory.
3. -nthreads: the number of threads used in the program. The program runs in parallel supporting multithreading.
4. -nTopics: the number of topics used in Topic Model for each domain.

<a name="inputandoutput"/>
## Input and Output
### Input
The input directory should contain domain files. For each domain, there should be 2 files (can be opened by text editors):

1. domain.docs: each line contains a list of word ids, representing a document.
2. domain.vocab: mapping from word id (starting from 0) to word.

### Output
The output directory contains topic model results for each learning iteration. LearningIteration 0 is always LDA, i.e., without any knowledge. LearningIteration i with i > 0 is the LTM model. The knowledge used for LearningIteration i is extracted from LearningIteration i - 1, except LearningIteration 0 which is LDA.

Under each learning iteration folder and sub-folder "DomainModels", there are a list of domain folders where each domain folder contains topic model results for each domain. Under each domain folder, there are 6 files (can be opened by text editors):

1. domain.docs: each line contains a list of word ids, representing a document.
2. domain.param: parameter settings.
3. domain.tassign: topic assignment for each word in each document.
4. domain.twdist: topic-word distribution
5. domain.twords: top words under each topic. The columns are separated by '\t' where each column corresponds to each topic.
6. domain.vocab: mapping from word id (starting from 0) to word.



