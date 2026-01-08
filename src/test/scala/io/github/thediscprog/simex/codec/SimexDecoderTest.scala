package io.github.thediscprog.simex.codec

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import TestClasses.*

class SimexDecoderTest extends AnyFlatSpec with Matchers with EitherValues {
  it should "decode to a simple case class consisting of primitives" in {
    val decoder: SimexDecoder[Primitives] = summon[SimexDecoder[Primitives]]

    val result = decoder.decode(primitiveEncoded)

    result.isRight shouldBe true
    result.value shouldBe primitive
  }

  it should "decode to case classes containing enclosing types" in {
    val decoder = summon[SimexDecoder[EnclosedTypes]]

    val result = decoder.decode(enclosedTypesEncoded)

    result.isRight shouldBe true
    result.value.option shouldBe enclosedTypes.option
    result.value.either shouldBe enclosedTypes.either
    result.value.alist shouldBe enclosedTypes.alist
    result.value.anArray.toSeq shouldBe enclosedTypes.anArray.toSeq
  }

  it should "decode nested case classes" in {
    val decoder = summon[SimexDecoder[Parent]]

    val result = decoder.decode(parentEncoded)

    result.isRight shouldBe true
  }

  it should "decode classes with enums" in {
    val decoder = summon[SimexDecoder[EnumClass]]

    val result = decoder.decode(enumClassEncoded)

    result.isRight shouldBe true
    result.value shouldBe enumClass

  }
}
