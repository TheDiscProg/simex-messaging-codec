package io.github.thediscprog.simex.codec.errors

import scala.annotation.nowarn

@nowarn
enum DecodingError(msg: String) {

  case NoMatchingProperty(field: String)
      extends DecodingError(s"There was no matching property for $field")
  case StringValueConversionError(value: String)
      extends DecodingError(s"There was error converting $value from String")
  case MissingPrimitiveValueError(msg: String)
      extends DecodingError(s"Primitive value was missing: $msg")
  case MissingChildrenError(msg: String)
      extends DecodingError(s"Nested class values were missing: $msg")
  case MissingDatumError(msg: String)
      extends DecodingError(s"There should have been a datum for $msg")
  case MissingEnumError(msg: String)
      extends DecodingError(s"The enum should have been defined but was not: $msg")
  case TooManyArgumentError(msg: String)
      extends DecodingError(s"There were too many arguments for this enum: $msg")
  case TooFewArgumentError(msg: String)
      extends DecodingError(s"There were not enough arguments for this enum: $msg")
  case ArgumentMisMatchError(msg: String) extends DecodingError(msg)

}
