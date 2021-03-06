package ch.epfl.lamp.odds
package inference

import functors.ProbMonad
import internal.OddsIntf

import scala.collection.mutable

/** Simple, rejection sampler for the ODDS language. */
trait RejectionSampling extends OddsIntf with DistMaps {
  this: OddsIntf =>

  /** Concrete random variable type. */
  type Rand[+A] = RandVar[A]

  /** Option-based probability monad for rejection sampling. */
  final case class RandVar[+A](v: Option[A]) extends RandIntf[A]

  /** Concrete probability monad type class. */
  implicit object Rand extends RandInternalIntf {
    @inline def fmap[A, B](mx: Rand[A])(f: A => B): Rand[B] =
      RandVar(mx.v.map(f))
    @inline def unit[A](x: A): Rand[A] = choose(Dist(x -> 1.0))
    @inline def join[A](mmx: Rand[Rand[A]]): Rand[A] =
      mmx.v.getOrElse(_zero)
    @inline override def bind[A, B](mx: Rand[A])(f: A => Rand[B]): Rand[B] =
      mx.v.map(f).getOrElse(_zero)
    @inline def zero[A]: Rand[A] = _zero
    @inline def plus[A](m1: Rand[A], m2: Rand[A]): Rand[A] =
      RandVar(m1.v orElse m2.v)
    @inline def choose[A](xs: Dist[A]): Rand[A] =
      if (xs.isEmpty) _zero else RandVar(Some(xs.nextSample))

    private val _zero: RandVar[Nothing] = RandVar(None)
  }

  /**
   * Approximate the distribution defined by a probabilistic
   * computation.
   *
   * @param samples the number of samples used to approximate the
   *        distribution.
   * @return an approximation of the distribution over the values
   *         of this random variable.
   */
  def sample[A](samples: Int)(x: => Rand[A]): Dist[A] = {

    // ASCII art progress bar
    def progressBar(count: Int, total: Int): String = {
      val bar =
        if (count >= total) "[" + "=" * 32 + ">] "
        else {
          val b: Int = count * 32 / total
          "[" + "=" * b + ">" + " " * (32 - b) + "] "
        }
      bar + count + "/" + total
    }

    var distMap = Dist[A]()
    var solCount = 0;
    var runCount = 0;
    while(runCount < samples) {
      if (runCount % (samples / 1024 + 1) == 0)
        print("\r" + progressBar(runCount, samples))
      for (v <- x.v) {
        distMap += (v, 1.0)
        solCount += 1
      }
      runCount += 1
    }
    println("\r" + progressBar(runCount, samples))
    println("rejection sampler: " + runCount + " samples, " + solCount +
      " solutions.")
    scale(1.0 / samples, distMap)
  }
}
