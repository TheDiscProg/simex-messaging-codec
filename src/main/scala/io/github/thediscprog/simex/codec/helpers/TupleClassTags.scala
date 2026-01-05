package io.github.thediscprog.simex.codec.helpers

import scala.reflect.ClassTag
import scala.deriving.Mirror
import scala.annotation.nowarn

trait TupleClassTags[T <: Tuple] {

  def apply(): List[ClassTag[?]]
}

object TupleClassTags {

  given empty: TupleClassTags[EmptyTuple] with
    def apply() = Nil

  given nonEmpty[H, T <: Tuple](using head: ClassTag[H], tail: TupleClassTags[T]): TupleClassTags[
    H *: T
  ] with
    def apply() = head :: tail()

  @nowarn
  inline def elementTypeInfo[A](using
      m: Mirror.ProductOf[A],
      tags: TupleClassTags[m.MirroredElemTypes]
  ): List[ClassType] =
    tags().map { case ct: ClassTag[?] =>
      val cls = ct.runtimeClass
      if (cls.isPrimitive())
        ClassType.PRIMITIVE
      else if (cls.isEnum())
        ClassType.ENUM
      else if (cls.getConstructors.headOption.exists(_.getParameterCount() > 0))
        ClassType.CASE_CLASS
      else
        ClassType.OTHER
    }
}

enum ClassType {
  case PRIMITIVE, ENUM, CASE_CLASS, OTHER
}
