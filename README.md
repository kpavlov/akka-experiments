# Experiments with Akka Framework

[![Build Status](https://travis-ci.org/kpavlov/akka-experiments.png?branch=master)](https://travis-ci.org/kpavlov/akka-experiments)

## Expensive producer

com.github.kpavlov.akkabox.stream.expensiveProducer.ExpensiveProducerApp

Akka stream is getting data from producer actor which in turn loads data from mock database queue ([SlowService] emulates RDBMS).

When there is nothing in the DB queue - then [SlowService] returns zero. [SlowServiceActor] schedules next service invocation after big delay.

[SlowService] is invoked by [SlowServiceActor] only when there is a demand from stream.

Trottler slows down a stream to reduce the load.

[ExpensiveProducerApp]: #ExpensiveProducerApp
[SlowService]: #SlowService
[SlowServiceActor]: #SlowServiceActor