package mcoffin.maester

import scalaz._

import scala.language.higherKinds

object Encoding {
  def decode[M[_], Err, From, To] (
    from: From
  ) (
    implicit monadError: MonadError[M, Err],
    decoder: Decode[Err, From, To]
  ): M[To] = {
    decoder.decode(from)
  }
}
