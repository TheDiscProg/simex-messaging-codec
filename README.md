# Simex Messsaging Codecs

Please see the [Simex Wiki](https://github.com/TheDiscProg/simex-messaging/wiki/Wiki-Home-Page) for more information about
simex messaging.
This library provides CODECS for Simex message's `data` field.

As this relies heavily on Scala 3 features, it is not compatible with Scala 2.

## Encoder
The encoder is used to encode your case class into a `Vector[Datum` object.

## Decoder
The decoder is used to decode a `Vector[Datum]` into your case class.
