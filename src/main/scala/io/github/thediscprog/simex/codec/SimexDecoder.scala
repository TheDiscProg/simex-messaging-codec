package io.github.thediscprog.simex.codec

import io.github.thediscprog.simex.codec.errors.DecodingError
import io.github.thediscprog.simexmessaging.messaging.Datum

import scala.annotation.nowarn
import scala.deriving.*

trait SimexDecoder[T] {

  def decode(data: Vector[Datum]): Either[DecodingError, T]
}

object SimexDecoder {

  @nowarn
  inline def derived[T](using
      m: Mirror.ProductOf[T]
  ): SimexDecoder[T] =
    new SimexDecoder[T] {
      import DecoderUtil.*

      override def decode(data: Vector[Datum]): Either[DecodingError, T] = {
        val map = data.map(d => d.field -> d).toMap
        val decoded = decodeTuple[m.MirroredElemTypes, m.MirroredElemLabels](map)
        decoded.map { values =>
          m.fromProduct(Tuple.fromArray(values.toArray))
        }
      }

    }

}
