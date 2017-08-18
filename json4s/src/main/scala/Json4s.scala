package mcoffin.maester

import instances._

import org.json4s._

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
}
