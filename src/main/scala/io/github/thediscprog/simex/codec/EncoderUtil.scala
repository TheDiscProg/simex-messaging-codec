package io.github.thediscprog.simex.codec

import scala.annotation.nowarn
import scala.deriving.Mirror
import scala.quoted.*

object EncoderUtil {

  inline def isEnum[T](using m: Mirror.SumOf[T]): Boolean =
    ${ isEnumImpl[T]('m) }

  @nowarn
  def isEnumImpl[T: Type](m: Expr[Mirror.Of[T]])(using q: Quotes): Expr[Boolean] = {
    import q.reflect.*
    if (TypeRepr.of[T].typeSymbol.flags.is(Flags.Enum)) '{ true }
    else
      report.error(s"Simex Encoders only work with case classes, primitives and Enums")
      '{ false }
  }

  def isPrimitive(x: Any): Boolean =
    x match
      case _: Boolean => true
      case _: Byte => true
      case _: Short => true
      case _: Int => true
      case _: Long => true
      case _: Float => true
      case _: Double => true
      case _: Char => true
      case _: String => true
      case _: BigDecimal => true
      case _: BigInt => true
      case _ => false

  def convertPrimitive(x: Any): String =
    x match
      case s: String => s
      case _ => x.toString()

  def isOption(x: Any): Boolean =
    x match
      case None => true
      case Some(_) => true
      case _ => false

  def isList(x: Any): Boolean =
    x match
      case xs: List[?] => true
      case _ => false

  def isArray(x: Any): Boolean =
    x match
      case xs: Array[?] => true
      case _ => false

  def isEither(x: Any): Boolean =
    x match
      case Left(_) => true
      case Right(_) => true
      case _ => false

}
