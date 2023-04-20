package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import javax.annotation.Nonnull;
import java.util.*;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;

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
		private final GameSetup setup;
		private final ImmutableSet<Piece> remaining;
		private final ImmutableList<LogEntry> log;
		private final Player mrX;
		public List<Player> detectives;
		private ImmutableSet<Move> moves;
		private final ImmutableSet<Piece> winner;

		private class TicketBoard implements Board.TicketBoard {
			ImmutableMap<Ticket, Integer> tickets;
			Piece piece;
			Ticket ticket;

			public TicketBoard(Piece piece) {
				this.piece = Objects.requireNonNull(piece);
				if (piece.isDetective()) {
					for (int i = 0; i < detectives.size(); i++) {
						if (detectives.get(i).piece().equals(piece)) {
							tickets = detectives.get(i).tickets();
						}
					}
				} else tickets = mrX.tickets();

			}

			public int getCount(Ticket ticket) {
				return tickets.get(ticket);
			}
		}


		@Override
		public GameSetup getSetup() {
			return setup; }

		@Override
		public ImmutableSet<Piece> getPlayers() {
//			Set<Piece> players = new HashSet<Piece>();
//			players.add(mrX.piece());
//			for (int i = 0; i<detectives.size(); i++) {
//				players.add(detectives.get(i).piece());
//			}
//			return ImmutableSet.copyOf(players);
			return ImmutableSet.of();
		}

		@Override
		public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {

			for (Player player : detectives) {
				if (player.piece().equals(detective)) {
					return Optional.of(player.location());
				}
			}
			return Optional.empty();
			}

		@Override
		public Optional<Board.TicketBoard> getPlayerTickets(Piece piece) {
			//if piece exists
			TicketBoard board = new TicketBoard(piece);
			if (board.getCount(TAXI) + board.getCount(BUS) + board.getCount(UNDERGROUND) == 0) {
				return Optional.empty();
			} else return Optional.of(board);
			//else return Optional.empty();
		}

		@Override
		public ImmutableList<LogEntry> getMrXTravelLog() {
			return log;
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
			//detectives having secret or double tickets should throw
			//no detective location overlap

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
//			if (!(getPlayers().contains(MRX))) { //passed
//				throw new IllegalArgumentException("no mr x");
//			}

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
		//remaining is only mrX at beginning
		//log empty at beginning
		//detectives empty at beginning

		return new MyGameState(setup, ImmutableSet.of(MRX), ImmutableList.of(), mrX, detectives);

	}
}