package io.github.thediscprog.simex.codec

import io.github.thediscprog.simex.codec.errors.DecodingError
import io.github.thediscprog.simex.codec.errors.DecodingError.*
import io.github.thediscprog.simexmessaging.messaging.Datum

import scala.compiletime.*

object DecoderUtil {

  inline def decodeTuple[Types <: Tuple, Labels <: Tuple](
      map: Map[String, Datum]
  ): Either[DecodingError, List[Any]] =
    inline erasedValue[(Types, Labels)] match
      case _: (EmptyTuple, EmptyTuple) => Right(Nil)
      case _: ((t *: ts), (l *: ls)) =>
        val label = constValue[l].asInstanceOf[String]
        val decoded = summonInline[DecoderOrValue[t]]
          .decodeOpt(map.get(label))

        for {
          head <- decoded
          tail <- decodeTuple[ts, ls](map)
        } yield head :: tail

  def extractValueFromDatum(datum: Datum): Either[DecodingError, String] =
    if (datum.value.isLeft) {
      datum.value.getLeft match
        case Some(value) => Right(value)
        case None => Left(MissingPrimitiveValueError(datum.field))
    } else {
      Left(MissingPrimitiveValueError(datum.field))
    }

  def extractChildrenFromDatum(datum: Datum): Either[DecodingError, Vector[Datum]] =
    if (datum.value.isRight) {
      datum.value.getRight match
        case Some(value) => Right(value)
        case None => Left(MissingChildrenError(s"No value found for ${datum.field}"))
    } else {
      // Some non-case classes can be encoded in the Xor.Left, i.e. enums
      Right(Vector(datum))
    }
}
