# Simex Messsaging Codecs

Please see the [Simex Wiki](https://github.com/TheDiscProg/simex-messaging/wiki/Wiki-Home-Page) for more information about
simex messaging.
This library provides CODECS for Simex message's `data` field.

As this relies heavily on Scala 3 features, it is not compatible with Scala 2.

Currently, it's being developed. So, don't used it just yet!!!

## Encoder
The encoder is used to encode your case class into a `Vector[Datum]` object.
The following is a list that is encoded to `Vector[Datum]`:
- Scala primitives
- Option, Either
- Collections such as Array and List
- Case Classes
- Enums
To use it:
```
case class EnclosedTypes(
      option: Option[Int],
      either: Either[Int, String],
      alist: List[Long],
      anArray: Array[String]
  ) derives SimexEncoder
```

Then, in your code:
```
val encoder = summon[SimexEncoder[EnclosedTypes]]
val toEncode = EnclosedTypes(
      option = Option(101),
      either = Right("Result is 102"),
      alist = List(1L, 0L, 3L),
      anArray = Array("one", "two")
    )

val data: Vector[Datum] = encoder.encode(None, toEncode)
```
Now, the `data` can be used to set the Simex's `data` property.
The first argument to encode is a `Option[String]` that sets the `Datum`'s `check` property.

## Decoder
The decoder is used to decode a `Vector[Datum]` into your case class.
