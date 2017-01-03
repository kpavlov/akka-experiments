# Experiments with Akka Framework

[![Build Status](https://travis-ci.org/kpavlov/akka-experiments.png?branch=master)](https://travis-ci.org/kpavlov/akka-experiments)

## Experiment 1: Transaction Processing

## Experiment 2: [Expensive producer][ExpensiveProducerApp]

Use Case: Batch processing data from a RDBMS queue.

Akka stream is getting data from producer actor which in turn loads data from mock database queue ([SlowService] emulates RDBMS).

When there is nothing in the DB queue - then [SlowService] returns zero. [SlowServiceActor] schedules next service invocation after big delay.

[SlowService] is invoked by [SlowServiceActor] only when there is a demand from stream.

Trottler slows down a stream to reduce the load.

[ExpensiveProducerApp]: ./src/main/java/com/github/kpavlov/akkabox/stream/expensiveProducer/ExpensiveProducerApp.java
[SlowService]:  ./src/main/java/com/github/kpavlov/akkabox/stream/expensiveProducer/SlowService.java
[SlowServiceActor]: ./src/main/java/com/github/kpavlov/akkabox/stream/expensiveProducer/SlowServiceActor.java