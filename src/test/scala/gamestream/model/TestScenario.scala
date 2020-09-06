package gamestream.model

import java.nio.file.{Path, Paths}

import gamestream.GameApi.hexStrToInt
import gamestream.io.LineIterator
import gamestream.model.TestScenario._

case class TestScenario(input: Path) {
  val states: Seq[ParsedTestFileRow] = LineIterator(input).zipWithIndex.foldLeft(List[ParsedTestFileRow]()) {
    case (eventList, (line, idx)) =>
      val prevState = eventList match {
        case Nil => GameState.empty
        case head :: _ => head.insertResult.newState
      }
      val msg = hexStrToInt(line)
      val event = DataEvent(msg)
      ParsedTestFileRow(idx, line, msg, event, prevState, prevState.update(event)) :: eventList
  }.reverse

  override def toString: String = states.mkString("\n")
}

object TestScenario {

  /**
   * capturing far more detail than we'll need, but can be useful
   *
   * @param lineIndex       the file line number (starting at zero)
   * @param inputText       the text file input
   * @param parsedBinaryMsg our parsed binary result
   * @param event           the parsed DataEvent based on the binary message
   * @param previousState   the game state before this event is applied
   * @param insertResult    the result of applying the state
   */
  case class ParsedTestFileRow(lineIndex: Int,
                               inputText: String,
                               parsedBinaryMsg: Int,
                               event: DataEvent,
                               previousState: GameState,
                               insertResult: GameState.InsertResult) {
    override def toString = {
      s"""$lineIndex: $inputText --> $event"""
    }
  }

  def pathFor(file: String) = Paths.get(getClass.getClassLoader.getResource(file).toURI)

  def sample1Path: Path = pathFor("sample1.txt")

  def sample2Path = pathFor("sample2.txt")

  def sample1 = TestScenario(sample1Path)

  def sample2 = TestScenario(sample2Path)
}