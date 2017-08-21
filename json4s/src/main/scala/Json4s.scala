package mcoffin.maester

import instances._

import org.json4s._

import scalaz._
import Scalaz._

import scala.math.BigDecimal

package object json4s {
  type JDecode[A] = Decode[DecodeException[JValue], JValue, A]

  implicit val decodeBigInt: JDecode[BigInt] =
    Decode.expecting[JValue, BigInt]("integer") {
      case JInt(i) => i
    }

  implicit val decodeInt: JDecode[Int] =
    decodeBigInt >=> Decode.expecting[BigInt, Int]("int") {
      case bi if bi.isValidInt => bi.intValue
    }

  implicit val decodeLong: JDecode[Long] =
    decodeBigInt >=> Decode.expecting[BigInt, Long]("long") {
      case bi if bi.isValidLong => bi.longValue
    }

  implicit val decodeBigDecimal: JDecode[BigDecimal] = {
    val decodeFromInt = decodeBigInt ∘ (_.bigInteger) ∘ (new java.math.BigDecimal(_)) ∘ (new BigDecimal(_))
    decodeFromInt ||| Decode.expecting[JValue, BigDecimal]("decimal") {
      case JDecimal(bd) => bd
      case JDouble(d) => new BigDecimal(new java.math.BigDecimal(d))
    }
  }

  implicit val decodeDouble: JDecode[Double] =
    decodeBigDecimal ∘ (_.doubleValue)

  implicit val decodeFloat: JDecode[Float] =
    decodeBigDecimal ∘ (_.floatValue)

  implicit val decodeString: JDecode[String] =
    Decode.expecting[JValue, String]("string") {
      case JString(str) => str
    }
}
