package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import javax.annotation.Nonnull;
import java.util.*;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.ALL_PIECES;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.DETECTIVES;

/**
 * cw-model
 * Stage 1: Complete this class
 */

public final class MyGameStateFactory implements Factory<GameState> {
	private GameSetup setup;
	private ImmutableSet<Piece> remaining;
	private ImmutableList<LogEntry> log;
	private Player mrX;
	private List<Player> detectives;

	private final class MyGameState implements GameState {
		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		public List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;


		@Override
		public GameSetup getSetup() { return setup; }

		@Override
		public ImmutableSet<Piece> getPlayers() {
			return ImmutableSet.of();
		}

		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {

			return null;
			};

		@Override
		public Optional<Board.TicketBoard> getPlayerTickets(Piece piece) {

			return getPlayerTickets(piece);
		}

		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {
			return ImmutableList.of();
		}

		@Override
		public ImmutableSet<Piece> getWinner() {
			// empty initially!!
			return ImmutableSet.of();
		}

		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			//requires us to find ALL moves Players can make for a given game state

			return moves;
		}

		@Override
		public GameState advance(Move move) {
			return null;
		}


		private MyGameState(final GameSetup setup,
							final ImmutableSet<Piece> remaining,
							final ImmutableList<LogEntry> log,
							final Player mrX,
							final List<Player> detectives) {

			//CHECK ALL DETECTIVES HAVE DIFFERENT LOCATIONS
			//CHECK DETECTIVES ARE ALL DETECTIVES
			//CHECK MRX IS BLACK
			//NO DUPLICATE GAME PIECES
			//

			if (setup == null) {
				throw new NullPointerException("setup can't be null");
			}
			if (setup.moves.isEmpty()) { throw new IllegalArgumentException("moves is empty!"); }
			if (remaining.contains(null)) {
				throw new NullPointerException("remaining can't be null");
			}
			if (log.contains(null)) {
				throw new NullPointerException("log can't be null");
			}
			if (mrX == null) { //passed
				throw new NullPointerException("mrX can't be null");
			}
			if (!(getPlayers().contains(MRX))) { //passed
				throw new IllegalArgumentException("no mr x");
			}

			if (Collections.frequency(getPlayers(), MRX) > 1) { //passed
				throw new IllegalArgumentException("more than one MRX");
			}

//			if (getPlayers().contains(MRX) && getPlayers().contains(null)) {
//				throw new NullPointerException("null detective");
//			}
//
//			if (getPlayers().size() <= 2 && getPlayers().contains(null)) {
//				throw new NullPointerException("no detectives");
//			}

			if (detectives.isEmpty() || (detectives.contains(null))) { //passed
				throw new NullPointerException("detectives can't be empty or null");
			}


			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
			winner = ImmutableSet.of();
		}
	}

	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
		// TODO

		return new MyGameState(setup, ImmutableSet.of(MRX), ImmutableList.of(), mrX, detectives);

	}
}