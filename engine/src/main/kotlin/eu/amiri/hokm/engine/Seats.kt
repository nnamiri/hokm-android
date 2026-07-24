package eu.amiri.hokm.engine

/**
 * The four positions at the table. Turn order follows [next]
 * (south → west → north → east), matching Hokm's counter-clockwise play.
 */
enum class Seat(val index: Int) {
    SOUTH(0), WEST(1), NORTH(2), EAST(3);

    /** The seat that plays after this one. */
    val next: Seat get() = entries[(index + 1) % 4]

    /** The team mate sitting opposite. */
    val partner: Seat get() = entries[(index + 2) % 4]

    val team: Team get() = if (index % 2 == 0) Team.ONE else Team.TWO
}

/** The two partnerships: south/north vs. west/east. */
enum class Team {
    ONE, TWO;

    val seats: List<Seat> get() = Seat.entries.filter { it.team == this }
    val opponent: Team get() = if (this == ONE) TWO else ONE
}
