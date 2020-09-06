# About

Some data structures for parsing game events (DataEvent) and exposing game state via GameApi.

![Scala CI](https://github.com/aaronp/basketball-events/workflows/Scala%20CI/badge.svg)

# API

The top-level api is the GameApi, which takes either an Iterable[DataEvent] or a file Path.

The default 'GameApi.apply(evens)' will just honor the events as given (e.g. no deduplication, validation)

The 'GameState.validating(...)' provides an example of a deduplicate/validating API.

These two usages provide a starting point - in practice/production a 'golden source' of data will need to be agreed.

That is, what is the expected quality of service? Deduplicating is easy enough, but the correct course of action when given two events which contradict each other needs clarification. Does the incoming message always win, or should it be dropped? Do we have an 'error channel' on which we can report these things? 

# Core Components

Incoming events are represented by the DataEvent case class, which offers:
```
DataEvent.apply(msg :Int)
```
for decoding from a binary message.

These messages are used to update an immutable GameState data strucutre. GameState.update(event) will ensure out-of-order events are recorded in 
elapsed game-time order, but the return type can be interrogated so as to provide deduplication/validation.

# Design Notes

The binary event codec is found in gamestream.model.DataEventCodec.

Supporting data structures of which DataEvent is comprised are defined in gamestream/model/model.scala

As there are a lot of ints about, the DateEvent uses strongly-typed wrappers for scores, elapsed time, etc.
This hopefully gives some readability and type safety at not much cost.

Other approaches might include phantom types, [tagged types](http://eed3si9n.com/learning-scalaz/Tagged+type.html), or just leaving them as primitives, but for now the simplest thing which works is a basic wrapper type.
  
## Validation/Deduplication

The GameState can be created from a 'GameState.validating' function, which returns either some error data if the events
don't make logical sense or a valid GameState.

All validation is kept in single-purpose functions found in EventValidation. The revision history tracking for this class would provide insight into what business decisions are involved in how to handle inconsistent data.

## Streaming

The logic/data structures here are a nice zero-dependency representation of the problem domain.

The GameState as an immutable data structure with its single 'update(input : DataEvent)' method lends itself to being used within other libraries/frameworks.
For example, it can be easily adapted to the [state monad](https://typelevel.org/cats/datatypes/state.html), which then could be used by e.g. a [Ref.tryModifyState](https://typelevel.org/cats-effect/api/cats/effect/concurrent/Ref.html#tryModifyState[B](state:cats.data.State[A,B]):F[Option[B]]) call.

Essentially in the streaming case (e.g. "update some state as each event arrives"), these data structures should be well-suited to be dropped into libraries that can feed DataEvents to them, such as e.g. [Observable.scan](https://monix.io/api/current/monix/reactive/Observable.html#scan[S](seed:=%3ES)(op:(S,A)=%3ES):monix.reactive.Observable[S]).

# Building 

This project is build with sbt, though a ```test.sh``` is provided at the root of the project for convenience
in addition to the Github Actions build. 

Code test coverage can be found [here](https://github.com/aaronp/basketball-events/suites/1150590372/artifacts/16581452).

# Summary

In general there are a few ways to tackle this problem. This particular solution won't be the most efficient, though hopefully it will be readable
and easily extended. It lends itself to folding the GameState over a stream, so it should just fold in easily to any streaming
library (e.g. scan/fold the GameState over the event stream which then updates an MVar/Ref).
