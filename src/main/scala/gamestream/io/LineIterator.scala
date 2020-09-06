package gamestream.io

import java.nio.file.Path

import scala.io.Source

/**
 * iterator of non-empty lines which closes itself upon reading all lines
 *
 * @param source
 */
final class LineIterator private(source: Source) extends Iterator[String] with AutoCloseable {
  val sourceLines = source.getLines().map(_.trim).filterNot(_.isEmpty)
  private var closed = false
  if (!hasNext) {
    close()
  }

  override def hasNext: Boolean = !isClosed() && sourceLines.hasNext

  override def next(): String = {
    val next = sourceLines.next()
    if (!hasNext) {
      close()
    }
    next
  }

  def isClosed() = closed

  override def close(): Unit = {
    closed = true
    source.close()
  }
}

object LineIterator {
  def apply(data: Path) = new LineIterator(Source.fromFile(data.toFile))

  def apply(data: Iterable[Char]) = new LineIterator(Source.fromIterable(data))
}