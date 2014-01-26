package ayai.utils

import scala.collection.mutable.ArrayBuffer

import com.artemis.utils.ImmutableBag

class IterableBag[A](bag: ImmutableBag[A]) extends Iterable[A] {
  def iterator: Iterator[A] = new Iterator[A] {
    var index = 0
    def hasNext: Boolean = index < bag.size
    def next = {
      val toReturn = bag.get(index)
      index += 1
      toReturn
    }
  }
}
