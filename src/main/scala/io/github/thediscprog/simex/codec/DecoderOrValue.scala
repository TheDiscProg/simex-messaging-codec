package io.github.thediscprog.simex.codec

import io.github.thediscprog.simex.codec.errors.DecodingError
import io.github.thediscprog.simex.codec.errors.DecodingError.*
import io.github.thediscprog.simexmessaging.messaging.Datum
import DecoderUtil.*

trait DecoderOrValue[A] {
  def decode(datum: Datum): Either[DecodingError, A]

  def decodeOpt(datum: Option[Datum]): Either[DecodingError, A] =
    datum match
      case Some(value) => decode(value)
      case None => Left(MissingDatumError("No datum found"))
}

object DecoderOrValue {

  given valueDecoder[A](using vd: StringDecoder[A]): DecoderOrValue[A] with
    def decode(datum: Datum): Either[DecodingError, A] = {
      val value: Either[DecodingError, String] = extractValueFromDatum(datum)
      value.flatMap(vd.decode)
    }

  given childrenDecoder[A](using sd: SimexDecoder[A]): DecoderOrValue[A] with
    def decode(datum: Datum): Either[DecodingError, A] = {
      val children = extractChildrenFromDatum(datum)
      children.flatMap(sd.decode)
    }
}
