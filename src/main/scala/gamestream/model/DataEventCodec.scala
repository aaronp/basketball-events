package gamestream.model

/**
 * Code for converting between input types and DataEvents
 */
object DataEventCodec {

  /**
   * @param binaryEncodedEventMessage the event message
   * @return a DataEvent from the given binary encoding
   */
  def fromBinary(binaryEncodedEventMessage: Int) = flags.event(binaryEncodedEventMessage)

  private[model] type BitMask[A] = Int => A

  private[model] object BitMask {
    def apply[A](bitmask: String)(parse: Int => A): BitMask[A] = {
      assert(bitmask.size == 31, s"coding/compile-time bug: invalid bitmask size ${bitmask.size}")
      val mask = Integer.parseInt(bitmask, 2)
      val shift = bitmask.reverse.takeWhile(_ == '0').size
      (input: Int) => {
        val masked = input & mask
        val shifted = masked >> shift
        parse(shifted)
      }
    }
  }

  private[model] object flags {
    // @formatter:off
    val pointsScored: BitMask[PointScored]                 = BitMask("0000000000000000000000000000011")(PointScored.apply)
    val whoScored: BitMask[Team]                           = BitMask("0000000000000000000000000000100")(Team.apply)
    val teamTwoTotalPoints: BitMask[TotalScore]            = BitMask("0000000000000000000011111111000")(TotalScore.apply)
    val teamOneTotalPoints: BitMask[TotalScore]            = BitMask("0000000000001111111100000000000")(TotalScore.apply)
    val elapsedMatchTimeSeconds: BitMask[ElapsedMatchTime] = BitMask("1111111111110000000000000000000")(ElapsedMatchTime.apply)
    // @formatter:on
    // the whole event
    val event: BitMask[DataEvent] = (event: Int) => {
      DataEvent(
        scored = flags.pointsScored(event),
        byTeam = flags.whoScored(event),
        team1Score = flags.teamOneTotalPoints(event),
        team2Score = flags.teamTwoTotalPoints(event),
        matchTime = flags.elapsedMatchTimeSeconds(event)
      )
    }
  }

}
