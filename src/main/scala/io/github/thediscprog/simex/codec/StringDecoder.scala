package io.github.thediscprog.simex.codec

import io.github.thediscprog.simex.codec.errors.DecodingError
import io.github.thediscprog.simex.codec.errors.DecodingError.*

import scala.reflect.ClassTag
import scala.util.Try

trait StringDecoder[A] {
  def decode(value: String): Either[DecodingError, A]
}

object StringDecoder {

  given StringDecoder[Boolean] with
    def decode(value: String): Either[DecodingError, Boolean] = value match
      case "false" => Right(false)
      case "true" => Right(true)
      case a => Left(StringValueConversionError(a))

  given StringDecoder[Byte] with
    def decode(value: String): Either[DecodingError, Byte] =
      Try(value.toByte).toEither.left
        .map(_ => StringValueConversionError(value))

  given StringDecoder[Short] with
    def decode(value: String): Either[DecodingError, Short] =
      Try(value.toShort).toEither.left
        .map(_ => StringValueConversionError(value))

  given StringDecoder[Int] with
    def decode(value: String): Either[DecodingError, Int] =
      Try(value.toInt).toEither.left
        .map(_ => StringValueConversionError(value))

  given StringDecoder[Long] with
    def decode(value: String): Either[DecodingError, Long] =
      Try(value.toLong).toEither.left
        .map(_ => StringValueConversionError(value))

  given StringDecoder[Float] with
    def decode(value: String): Either[DecodingError, Float] =
      Try(value.toFloat).toEither.left
        .map(_ => StringValueConversionError(value))

  given StringDecoder[Double] with
    def decode(value: String): Either[DecodingError, Double] =
      Try(value.toDouble).toEither.left
        .map(_ => StringValueConversionError(value))

  given StringDecoder[Char] with
    def decode(value: String): Either[DecodingError, Char] =
      if (value.length == 1) {
        Right(value.charAt(0))
      } else {
        Left(StringValueConversionError(value))
      }

  given StringDecoder[String] with
    def decode(value: String): Either[DecodingError, String] = Right(value)

  given StringDecoder[BigDecimal] with
    def decode(value: String): Either[DecodingError, BigDecimal] =
      Try(BigDecimal(value)).toEither.left
        .map(_ => StringValueConversionError(value))

  given StringDecoder[BigInt] with
    def decode(value: String): Either[DecodingError, BigInt] =
      Try(BigInt(value)).toEither.left
        .map(_ => StringValueConversionError(value))

  given [T](using sd: StringDecoder[T]): StringDecoder[Option[T]] with
    def decode(value: String): Either[DecodingError, Option[T]] =
      if value.isEmpty then Right(None)
      else sd.decode(value).map(Some(_))

  given [L, R](using lsd: StringDecoder[L], rsd: StringDecoder[R]): StringDecoder[Either[L, R]] with
    def decode(value: String): Either[DecodingError, Either[L, R]] = {
      val tryRight = rsd.decode(value)
      if (tryRight.isLeft) {
        lsd.decode(value).map(Left(_))
      } else {
        tryRight.map(Right(_))
      }
    }
  given [T](using sd: StringDecoder[T]): StringDecoder[List[T]] with
    def decode(value: String): Either[DecodingError, List[T]] =
      if value.isEmpty() then Right(Nil)
      else {
        val cleanedValue = value.stripPrefix("[").stripSuffix("]")
        cleanedValue
          .split(",")
          .toList
          .foldRight(Right(Nil): Either[DecodingError, List[T]]) { (elem, acc) =>
            for {
              tail <- acc
              head <- sd.decode(elem.trim)
            } yield head :: tail
          }
      }

  given [T](using sd: StringDecoder[T], ct: ClassTag[T]): StringDecoder[Array[T]] with
    def decode(value: String): Either[DecodingError, Array[T]] = {
      val listDecoder = summon[StringDecoder[List[T]]]
      val list = listDecoder.decode(value)
      list.map(_.toArray)
    }

}
