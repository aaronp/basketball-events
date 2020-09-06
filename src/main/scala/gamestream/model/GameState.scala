package gamestream.model

import gamestream.GameApi
import gamestream.model.GameState.InsertedEvent

/**
 * This is the immutable data structure which will be folded over a stream of [[DataEvent]]s.
 *
 * This implementation will be slow (uses linked list, rebuilds immutable data structures)
 *
 * We can profile/improve it later if needed, but going for correctness first.
 *
 * @param eventsMostRecentFirst
 */
final class GameState private[model](val eventsMostRecentFirst: List[DataEvent]) extends GameApi {

  /**
   * Update the state from this event
   *
   * This will lend itself to folding over a stream, event sourcing, etc.
   *
   * @param event the game event
   * @return the updated state
   */
  def update(event: DataEvent): InsertedEvent = {
    val (before, item, after) = GameState.insertInto(event, eventsMostRecentFirst)(GameState.latestTimeFirst)
    InsertedEvent(before, item, after)
  }

  override def allEvents: Iterable[DataEvent] = eventsMostRecentFirst.reverse
}

object GameState {
  private val latestTimeFirst = implicitly[Ordering[DataEvent]].reverse
  val empty = new GameState(Nil)

  /**
   * No validation - we just honor what we get
   *
   * @param events the data events
   * @return an instance of GameApi
   */
  def apply(events: Iterator[DataEvent]): GameApi = verbatim(events)

  /**
   * Returns a GameStateApi for all the game events without considering duplicates/invalid states.
   *
   * @param events the game events
   * @return a GameStateApi which has all the events
   */
  def verbatim(events: Iterator[DataEvent]): GameState = {
    events.foldLeft(GameState.empty) {
      case (state, event) => state.update(event).newState
    }
  }


  /**
   * Return either a [[GameStateApi]] or an error if the there is invalid data.
   *
   * Basically we're offering up different behavior through these factory methods
   * (e.g. the vanilla
   *
   * @param events the data events
   * @return an instance of GameApi
   */
  def validating(events: Iterator[DataEvent]): Either[EventError, GameApi] = {
    def appendEvent(previousState: GameState, event: DataEvent, eventIndex: Int): Either[EventError, GameState] = {
      previousState.update(event).deduplicate match {
        case result@InsertedEvent(_, _, _) =>
          // and here we do a bix of validation ...
          EventValidation.validate(previousState, event, result.newState) match {
            case None => Right(result.newState)
            case Some(errorMsg) =>
              Left(EventError(event, eventIndex, previousState, errorMsg))
          }
        case DuplicateEvent(_, _, _) =>
          // no need to validate a dropped duplicate event
          Right(previousState)
      }
    }

    // this variant drops duplicate events and rejects invalid scores
    events.zipWithIndex.foldLeft(Right(GameState.empty): Either[EventError, GameState]) {
      case (Right(previousState), (event, eventIndex)) => appendEvent(previousState, event, eventIndex)
      case (left, _) =>
        // if you wanted to drop/ignore this event and continue
        left
    }
  }

  /**
   * Updating a [[GameState.update()]] with a [[DataEvent]] produces an [[InsertResult]]
   *
   * It's modeled this way so the caller can interrogate the result and take decisions such as whether to validate or not
   */
  sealed trait InsertResult {
    def event: DataEvent
    def newState: GameState
    def subsequentEvents: List[DataEvent]
  }

  /**
   * Updating a [[GameState]] from a [[DataEvent]] could just return a [[GameState]],
   * but that throws away some data/information, like where in the list of events the event was
   * inserted.
   *
   * This data structure keeps that detail, which makes it a bit easier to do some checking/validation, like deduplication
   *
   * @param previousEvents   the previous events in the game
   * @param event            the input event
   * @param subsequentEvents any events which came after this event -- the case when events arrive out of order
   */
  final case class InsertedEvent(previousEvents: List[DataEvent], override val event: DataEvent, override val subsequentEvents: List[DataEvent]) extends InsertResult {
    override lazy val newState: GameState = new GameState(previousEvents ++: event +: subsequentEvents)

    /** @return a result which ensures the new event isn't inserted if it was a duplicate
     */
    def deduplicate: InsertResult = {
      require(!previousEvents.contains(event))
      subsequentEvents match {
        case `event` :: tail => DuplicateEvent(previousEvents, event, tail)
        case _ => this
      }
    }
  }

  final case class DuplicateEvent(previousEvents: List[DataEvent], override val event: DataEvent, override val subsequentEvents: List[DataEvent]) extends InsertResult {
    override lazy val newState: GameState = new GameState(previousEvents ++: event +: subsequentEvents)
  }

  /**
   * This isn't going to be quick, but it'll be correct.
   *
   * Insert the item into an already sorted list
   *
   * @param item
   * @param alreadySorted
   * @tparam A
   * @return the sorted list with the item inserted
   */
  private[model] def insertInto[A: Ordering](item: A, alreadySorted: List[A]): (List[A], A, List[A]) = {
    import Ordered._
    val (before: List[A], after: List[A]) = alreadySorted.span(_ < item)
    (before, item, after)
  }
}
