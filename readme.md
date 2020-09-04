# About
See the [Programming Test pdf](Programming Test.pdf) for the specificatino

# Code Blog

## DataEvent modeling

I first did the binary event codec parsing in gamestream.model.DataEventCodec,
as well as the data structures for the DataEvent.

As there are a lot of ints about, I opted for creating strongly-typed wrappers for scores, elapsed time, etc.
This hopefully gives some readability and type safety at not much cost.

Other options might include phantom types on ints, but I'll not go there.
  

