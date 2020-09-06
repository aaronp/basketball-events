# About

Some data structures for parsing game events (DataEvent) and exposing game state via GameApi.

![Scala CI](https://github.com/aaronp/basketball-events/workflows/Scala%20CI/badge.svg)

# API

The top-level api is the GameApi, which takes either an Iterable[DataEvent] or a file Path.

The default just honors all events, though there is a validating one which demonstrates deduplication and 
some basic validation which could be expounded on.

# Core Components

Incoming events are represented by the DataEvent case class, which offers:

```
DataEvent.apply(msg :Int)
```
for decoding from a binary message.

The incoming messages can be used to update an immutable GameState, which simply ensures events are recorded in 
elapsed game-time order.

The end-user client API is the GameApi, which the GameState also happens to implement.

## DataEvent modeling

I first did the binary event codec parsing in gamestream.model.DataEventCodec,
as well as the data structures for the DataEvent.

As there are a lot of ints about, I opted for creating strongly-typed wrappers for scores, elapsed time, etc.
This hopefully gives some readability and type safety at not much cost.

Other options might include phantom types on ints, but I'll not go there.
  
## Validation/Deduplication

The GameState can be created from a 'GameState.validating' function, which returns either some error data if the events
don't make logical sense or a valid GameState.

This theme can be expanded to deal with streaming events, though that would involve more of a discussion as to what/who
to trust as the source of truth -- should we just take what upstream sends us, even if it disagrees with our validation?

# Building 

This project is build with sbt, though a ```test.sh``` is provided at the root of the project for convenience
in addition to the Github Actions build. 

Code test coverage can be found [here](https://github.com/aaronp/basketball-events/suites/1150590372/artifacts/16581452).

# Summary

There are a few ways to tackle this problem, and this way won't be the most efficient, though hopefully it will be readable
and easily extended. It lends itself to folding the GameState over a stream, so it should just fold in easily to any streaming
library (e.g. scan/fold the GameState over the event stream which then updates an MVar/Ref).

I think this is the first time I actually went full 100% code coverage too. I spent probably a bit more time on this
than I should've, but it was too tempting to just make it 100 when it was in the 90s already.