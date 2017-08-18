package mcoffin.maester

import scalaz._
import Scalaz._

import scala.language.higherKinds

/**
 * Represents the opportunity to decode a To from a From, with the possibility
 * of an error of type Err
 */
trait Decode[Err, From, To] {
  def decode[M[_]](from: From)(implicit monadError: MonadError[M, Err]): M[To]
}

/**
 * Convenience type for representing errors while decoding
 */
case class DecodeException[From](
  expected: String,
  found: From
) extends RuntimeException(s"Expecting $expected but found $found")

object Decode {
  /**
   * Create a new Decode instance from a function that returns a disjunction
   */
  def apply[Err, From, To] (
    f: (From) => \/[Err, To]
  ): Decode[Err, From, To] = new Decode[Err, From, To] {
    def decode[M[_]] (
      from: From
    ) (
      implicit m: MonadError[M, Err]
    ): M[To] = {
      val g = f >>> {
        case \/-(x) => x.point[M]
        case -\/(e) => e.raiseError[M, To]
      }
      g(from)
    }
  }

  /**
   * Create a new Decode instance from a partial function
   */
  def expecting[From, To] (
    expecting: String
  ) (
    pf: PartialFunction[From, To]
  ): Decode[DecodeException[From], From, To] = Decode apply { v =>
    type M[A] = \/[DecodeException[From], A]
    if (pf isDefinedAt v) {
      pf(v).point[M]
    } else {
      DecodeException(expecting, v).raiseError[M, To]
    }
  }
}

package object instances {
  implicit def functorDecode[Err, From] = new Functor[({type λ[A] = Decode[Err, From, A]})#λ] {
    def map[A, B](fa: Decode[Err, From, A])(g: (A) => B) = new Decode[Err, From, B] {
      def dec[M[_]](implicit m: MonadError[M, Err]) =
        fa.decode[M] _ >>> (_ ∘ g)
      def decode[M[_]] (
        from: From
      ) (
        implicit monadError: MonadError[M, Err]
      ): M[B] = dec[M](implicitly)(from)
    }
  }

  implicit def bifunctorDecode[From] = new Bifunctor[({type λ[E, A] = Decode[E, From, A]})#λ] {
    def bimap[A, B, C, D](fab: Decode[A, From, B])(f: (A) => C, g: (B) => D) =
      Decode apply (fab.decode[({type λ[T] = \/[A, T]})#λ](_:From).bimap(f, g))
  }

  implicit def decodeTraverse[F[_], Err, From, To] (
    implicit decoder: Decode[Err, From, To],
    traverse: Traverse[F]
  ) = new Decode[Err, F[From], F[To]] {
    def decode[M[_]] (
      from: F[From]
    ) (
      implicit monadError: MonadError[M, Err]
    ): M[F[To]] = {
      from traverse (decoder.decode[M] _)
    }
  }

  implicit def semigroupDecodeException[From] = new Semigroup[DecodeException[From]] {
    def append(fst: DecodeException[From], snd: => DecodeException[From]) = {
      val expected = s"${fst.expected} or ${snd.expected}"
      DecodeException(expected, fst.found)
    }
  }

  implicit class DecodeWithAccumulatingErrors[Err, From, To] (
    decode: Decode[Err, From, To]
  ) (
    implicit semigroup: Semigroup[Err]
  ) {
    def |||(other: => Decode[Err, From, To]) = Decode apply { (v: From) =>
      type M[A] = \/[Err, A]
      val me = implicitly[MonadError[M, Err]]
      me.handleError(decode.decode[M](v)) { firstError =>
        me.handleError(other.decode[M](v)) { secondError =>
          val e = firstError |+| secondError
          e.raiseError[M, To]
        }
      }
    }
  }
  implicit class DecodeWithDecodeException[From, To] (
    decoder: Decode[DecodeException[From], From, To]
  ) {
    def >=>[A, EE](other: Decode[DecodeException[EE], To, A]): Decode[DecodeException[From], From, A] =
      Decode apply { originalValue =>
        type M[T] = \/[DecodeException[From], T]
        val otherPrime = implicitly[Bifunctor[({type λ[E, T] = Decode[E, To, T]})#λ]].leftMap(other) {
          case DecodeException(expected, _) =>
            DecodeException(expected, originalValue)
        }
        decoder.decode[M](originalValue) >>= (otherPrime.decode[M](_))
      }
  }
}
