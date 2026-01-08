package io.github.thediscprog.simex.codec

import io.github.thediscprog.simex.codec.EncoderUtil.*
import io.github.thediscprog.simex.codec.helpers.*
import io.github.thediscprog.simexmessaging.messaging.Datum
import io.github.thediscprog.slogic.Xor

import scala.annotation.nowarn
import scala.compiletime.*
import scala.deriving.*
import scala.reflect.ClassTag

trait SimexEncoder[T] {
  def encode(check: Option[String], t: T): Vector[Datum]
}

object SimexEncoder {

  // summon encoders for a type T
  inline def apply[T](using enc: SimexEncoder[T]): SimexEncoder[T] = enc

  inline def summonAll[T <: Tuple]: List[SimexEncoder[?]] =
    inline erasedValue[T] match
      case _: EmptyTuple => Nil
      case _: (h *: t) =>
        summonInline[SimexEncoder[h]] :: summonAll[t]

  @nowarn
  inline def derived[T](using
      m: Mirror.Of[T],
      tags: TupleClassTags[m.MirroredElemTypes]
  ): SimexEncoder[T] =
    new SimexEncoder[T] {

      override def encode(check: Option[String], t: T): Vector[Datum] = {
        val propertyNames = constValueTuple[m.MirroredElemLabels].toList.asInstanceOf[List[String]]
        val elements = t.asInstanceOf[Product].productIterator.toList
        val encoders = summonAll[m.MirroredElemTypes]

        val elementTypes =
          tags().map { case ct: ClassTag[?] =>
            val cls = ct.runtimeClass
            if (cls.isPrimitive)
              ClassType.PRIMITIVE
            else if (isScalaEnumCase(cls))
              ClassType.ENUM
            else if (cls.getConstructors.headOption.exists(_.getParameterCount() > 0))
              ClassType.CASE_CLASS
            else
              ClassType.OTHER
          }

        val properties = propertyNames.zip(elements).zip(encoders)
        val encodedToDatum = properties.zipWithIndex.map { case (((f, v), e), index) =>
          if (isPrimitive(v)) {
            Datum(f, check, Xor.applyLeft(convertPrimitive(v)))
          } else if (isOption(v)) {
            v match
              case None => Datum(f, check, Xor.applyLeft(""))
              case Some(value) =>
                if (isPrimitive(value)) {
                  Datum(f, check, Xor.applyLeft(convertPrimitive(value)))
                } else {
                  throw new RuntimeException(s"Simex Encoder: Cannot handle Property [$f]->[$v]")
                }
          } else if (isEither(v)) {
            v match
              case Left(l) =>
                if (isPrimitive(l)) {
                  Datum(f, check, Xor.applyLeft(convertPrimitive(l)))
                } else {
                  throw new RuntimeException(s"Simex Encoder: Cannot handle Property [$f]->[$v]")
                }
              case Right(r) =>
                if (isPrimitive(r)) {
                  Datum(f, check, Xor.applyLeft(convertPrimitive(r)))
                } else {
                  throw new RuntimeException(s"Simex Encoder: Cannot handle Property [$f]->[$v]")
                }
          } else if (isList(v)) {
            val list = v.asInstanceOf[List[Any]]
            encodeList(f, check, list) match
              case a: Datum => a
              case vec: Vector[Datum] => Datum(f, check, Xor.applyRight(vec))
          } else if (isArray(v)) {
            val list = v.asInstanceOf[Array[Any]].toList
            encodeList(f, check, list) match
              case a: Datum => a
              case vec: Vector[Datum] => Datum(f, check, Xor.applyRight(vec))
          } else {
            // We have come to a subclass
            val encodedType = elementTypes(index)
            val classTypes: List[ClassTag[?]] = tags()
            val classType = classTypes(index)
            if (isEnum(v) || encodedType == ClassType.ENUM) {
              throw new RuntimeException(
                s"Simex Encoder: Cannot handle Enum [$f]->[$v]: Element Type [$encodedType]: Class: [$classType] Encoder:[$e]"
              )
            } else if (encodedType == ClassType.CASE_CLASS) {
              val subClassEncoder = e.asInstanceOf[SimexEncoder[Any]]
              val subClass = subClassEncoder.encode(check, v)
              Datum(f, check, Xor.applyRight(subClass))
            } else {
              // Assume it's an Enum
              val enumEncoder = e.asInstanceOf[SimexEncoder[Any]]
              val data: Vector[Datum] = enumEncoder.encode(check, v)
              val enumVals = data.head.value
              Datum(f, check, enumVals)
            }
          }
        }
        encodedToDatum.toVector
      }

      private def isEnum(a: Any): Boolean =
        a.getClass().isEnum()

      private def isScalaEnumCase(cls: Class[?]): Boolean = {
        val enclosingType: Array[Boolean] = cls.getClasses().map(_.isEnum())
        Option(cls.getEnclosingClass()).exists(_.isEnum)
      }

      private def encodeList(
          property: String,
          check: Option[String],
          list: List[?]
      ): Datum | Vector[Datum] = {
        val (h :: t) = list
        if (isPrimitive(h)) {
          val strList = list.map(value => convertPrimitive(value)).mkString(",")
          Datum(property, check, Xor.applyLeft(s"[$strList]"))
        } else {
          throw new RuntimeException(s"Simex Encoder: Cannot handle Property [$property]->[$list]")
        }
      }
    }

  // Enum Encoder
  @nowarn
  inline def deriveEnum[E](using
      m: Mirror.Of[E],
      tags: TupleClassTags[m.MirroredElemTypes]
  ): SimexEncoder[E] =

    new SimexEncoder[E] {
      override def encode(check: Option[String], t: E): Vector[Datum] =
        inline m match
          case s: Mirror.SumOf[E] =>
            val oridinal = s.ordinal(t)
            val label = constValueTuple[s.MirroredElemLabels](oridinal).toString
            val product = t.asInstanceOf[Product]
            val fieldValues = product.productIterator.toList.mkString(",")
            Vector(Datum(label, check, Xor.applyLeft(s"$label($fieldValues)")))
          case _ => throw new RuntimeException(s"Simex Encoder: Cannot handle $t")

    }

  // Although these should not used, but in order to handle primitives, we have to give encoders...
  given SimexEncoder[Boolean] with
    def encode(check: Option[String], t: Boolean): Vector[Datum] = throw new RuntimeException(
      s"SimexEncoder for Boolean is not required."
    )

  given SimexEncoder[Byte] with
    def encode(check: Option[String], t: Byte): Vector[Datum] = throw new RuntimeException(
      s"SimexEncoder for Byte is not required."
    )

  given SimexEncoder[Short] with
    def encode(check: Option[String], t: Short): Vector[Datum] = throw new RuntimeException(
      s"SimexEncoder for Short is not required."
    )

  given SimexEncoder[Int] with
    def encode(check: Option[String], t: Int): Vector[Datum] = throw new RuntimeException(
      s"SimexEncoder for Int is not required."
    )

  given SimexEncoder[Long] with
    def encode(check: Option[String], t: Long): Vector[Datum] = throw new RuntimeException(
      s"SimexEncoder for Long is not required."
    )

  given SimexEncoder[Float] with
    def encode(check: Option[String], t: Float): Vector[Datum] = throw new RuntimeException(
      s"SimexEncoder for Float is not required."
    )

  given SimexEncoder[Double] with
    def encode(check: Option[String], t: Double): Vector[Datum] = throw new RuntimeException(
      s"SimexEncoder for Double is not required."
    )

  given SimexEncoder[Char] with
    def encode(check: Option[String], t: Char): Vector[Datum] = throw new RuntimeException(
      s"SimexEncoder for Char is not required."
    )

  given SimexEncoder[String] with
    def encode(check: Option[String], t: String): Vector[Datum] = throw new RuntimeException(
      s"SimexEncoder for String is not required."
    )

  given SimexEncoder[BigInt] with
    def encode(check: Option[String], t: BigInt): Vector[Datum] = throw new RuntimeException(
      s"SimexEncoder for BigInt is not required."
    )

  given SimexEncoder[BigDecimal] with
    def encode(check: Option[String], t: BigDecimal): Vector[Datum] = throw new RuntimeException(
      s"SimexEncoder for BigDecimal is not required."
    )

  given optionEncoder[A]: SimexEncoder[Option[A]] with
    def encode(check: Option[String], t: Option[A]): Vector[Datum] = throw new RuntimeException(
      s"SimexEncoder for Option is not required."
    )

  given eitherEncoder[A, B]: SimexEncoder[Either[A, B]] with
    def encode(check: Option[String], t: Either[A, B]): Vector[Datum] = throw new RuntimeException(
      s"SimexEncoder for Either is not required."
    )

  given listEncoder[A]: SimexEncoder[List[A]] with
    def encode(check: Option[String], t: List[A]): Vector[Datum] = throw new RuntimeException(
      s"SimexEncoder for List is not required."
    )

  given arrayEncoder[A]: SimexEncoder[Array[A]] with
    def encode(check: Option[String], t: Array[A]): Vector[Datum] = throw new RuntimeException(
      s"SimexEncoder for Array is not required."
    )
}
