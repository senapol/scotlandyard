package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
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
			Set<Piece> players = new HashSet<Piece>();
			if(mrX != null) {
				players.add(mrX.piece());
			}
			if(detectives != null) {
				for (int i = 0; i < detectives.size(); i++) {
					players.add(detectives.get(i).piece());
				}
			}
			return ImmutableSet.copyOf(players);
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
			TicketBoard board = new TicketBoard(piece);
			if(getPlayers().contains(piece)) return Optional.of(board);
			else return Optional.empty();
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

		//calculate poss single moves
		private static Set<Move.SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {
			//TODO create an empty collection of some sort to store all the SingleMoves we generate
			Set<Move.SingleMove> moves = new HashSet<>();

			for (int destination : setup.graph.adjacentNodes(source)) {
				//TODO find out if destination is occupied by a detective
				//if location occupied, don't add to collection of moves to return
				boolean free = true;
				for (Player det : detectives) {
					if (destination == det.location()) {
						free = false; break;
					}
				}

				if(free) {
					for (Transport t : Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))) {
						// TODO find out if the player has the required tickets
						//  if it does, construct a SingleMove and add it the collection of moves to return
						if(t != Transport.FERRY && player.tickets().get(t.requiredTicket())>0){
							moves.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination));
						}
					}
				}


				// TODO consider the rules of secret moves here
				//  add moves to the destination via a secret ticket if there are any left with the player
				if(player.tickets().get(SECRET)>0){
					moves.add(new Move.SingleMove(player.piece(), source, SECRET, destination));
				}
			}
			//TODO return the collection of moves
			return moves;
		}

		private static Set<Move.DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source, Set<Move.SingleMove> singleMoves){

			Set<Move.DoubleMove> moves = new HashSet<>();
			//iterating for each possible single move
			for (Move.SingleMove sMove : singleMoves) {
				for (int destination : setup.graph.adjacentNodes(sMove.destination)) {
					//checking if destination is occupied
					boolean free = true;
					for (Player det : detectives) {
						if (destination == det.location()) {
							free = false; break;
						}
					}
					if (free) {
						for (Transport t : Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))) {
							//checking if mrx has the required tickets for both moves
							if (t != Transport.FERRY && (player.tickets().get(t.requiredTicket()) == player.tickets().get(sMove.ticket) && player.tickets().get(t.requiredTicket())>1 || player.tickets().get(t.requiredTicket()) != player.tickets().get(sMove.ticket)&&player.tickets().get(t.requiredTicket()) > 0 && player.tickets().get(sMove.ticket)>0)) {
								moves.add(new Move.DoubleMove(player.piece(), source, sMove.ticket, sMove.destination, t.requiredTicket(), destination));
							}
							//adding single secret ticket combos
							if(player.tickets().get(sMove.ticket)>0 && player.tickets().get(SECRET) > 0){
								moves.add(new Move.DoubleMove(player.piece(), source, sMove.ticket, sMove.destination, SECRET, destination));
							}if(player.tickets().get(t.requiredTicket())>0 && player.tickets().get(SECRET) > 0){
								moves.add(new Move.DoubleMove(player.piece(), source, SECRET, sMove.destination, t.requiredTicket(), destination));
							}
						}
					}

					//adding double secret tickets
					if (player.tickets().get(SECRET) > 1) {
						moves.add(new Move.DoubleMove(player.piece(), source, SECRET, sMove.destination, SECRET, destination));
					}
				}
			}

			// TODO return the collection of moves
			return moves;
		}

		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			//requires us to find ALL moves Players can make for a given game state
			Set<Move> moves = new HashSet<>();

			//detective moves
			if(remaining.contains(mrX.piece())){
				Set<Move.SingleMove> mrXSingle = makeSingleMoves(setup, detectives, mrX, mrX.location());
				moves.addAll(mrXSingle);
				moves.addAll(makeDoubleMoves(setup, detectives, mrX, mrX.location(), mrXSingle));
			}else {
				for (Player det : detectives) {
					moves.addAll(makeSingleMoves(setup, detectives, det, det.location()));
				}
			}

			return ImmutableSet.copyOf(moves);

//			//mrx moves
//			Set<Move.SingleMove> mrXSingle = makeSingleMoves(setup, detectives, mrX, mrX.location());
//			moves.addAll(mrXSingle);
//			moves.addAll(makeDoubleMoves(setup, detectives, mrX, mrX.location(), mrXSingle));
//			return ImmutableSet.copyOf(moves);
		}

		@Override
		public GameState advance(Move move) {
			this.moves = getAvailableMoves();
			//TODO return a new state from the current GameState and a provided Move
//			if (!moves.contains(move)) {
//				throw new IllegalArgumentException("Illegal move: " + move);
//			}

			//TODO implement different behaviours for applying Single/Double moves
			//e.g. we may need destination or destination2 for enacting moves
			//can access a particular Single or Double move
			//by supplying a Visitor<...> object as a parameter to the move.accept(...) method
			return move.accept(new Move.Visitor<GameState>() {
				@Override
				public GameState visit(Move.SingleMove move) {

					Player mrXUpdate = mrX;
					Set<Piece> remainingUpdate = new HashSet<>();

					if(move.commencedBy().isMrX()){
						List<LogEntry> logUpdate = Lists.newArrayList();
						logUpdate.addAll(log);

						remainingUpdate.add(detectives.get(0).piece());
						mrXUpdate = mrXUpdate.at(move.destination);

						if(setup.moves.get(log.size())){ //check if reveal round
							logUpdate.add(LogEntry.reveal(move.ticket, move.destination)); //add to log
						}else{
							logUpdate.add(LogEntry.hidden(move.ticket)); //add to log
						}
						mrXUpdate = mrXUpdate.use(move.ticket); //take ticket off board

						return new MyGameState(setup, ImmutableSet.copyOf(remainingUpdate), ImmutableList.copyOf(logUpdate), mrXUpdate, detectives);
					}
					if(move.commencedBy().isDetective()) {
						List<Player> detectivesUpdate = new ArrayList<>();
						Player detUpdate = null;
						int id = -1;
						for (Player det : detectives) {
							if (det.piece().equals(remaining.toArray()[0])) {
								detUpdate = det;
								id = detectives.indexOf(det);
							} else {
								detectivesUpdate.add(det);
							}
						}
						detUpdate = Objects.requireNonNull(detUpdate).use(move.ticket);
						mrXUpdate = detUpdate.give(move.ticket);
						remainingUpdate.add(mrX.piece());
						detUpdate = detUpdate.at(move.destination);
						detectivesUpdate.add(detUpdate);
						return new MyGameState(setup, ImmutableSet.copyOf(remainingUpdate), log, mrXUpdate, detectivesUpdate);
					}
					return null;
				}



				@Override
				public GameState visit(Move.DoubleMove move) {
					if(move.commencedBy().isMrX()){
						mrX.at(move.destination2);

					}
					if(move.commencedBy().isDetective()){
						throw new IllegalArgumentException("detective can't use double tickets");
					}
					return new MyGameState(setup, remaining, log, mrX, detectives);
				}
			});

		}



		private MyGameState(final GameSetup setup,
							final ImmutableSet<Piece> remaining,
							final ImmutableList<LogEntry> log,
							final Player mrX,
							final List<Player> detectives) {

			//checks and exceptions

			if (setup == null) {
				throw new NullPointerException("setup can't be null");
			}
			if (setup.moves.isEmpty()) {
				throw new IllegalArgumentException("moves is empty!");
			}
			if (remaining.contains(null)) {
				throw new NullPointerException("remaining can't be null");
			}
			if (log.contains(null)) {
				throw new NullPointerException("log can't be null");
			}
			if (mrX == null) { //passed
				throw new NullPointerException("mrX can't be null");
			}

			if (Collections.frequency(getPlayers(), MRX) > 1) { //passed
				throw new IllegalArgumentException("more than one MRX");
			}

			if (detectives.isEmpty() || (detectives.contains(null))) { //passed
				throw new NullPointerException("detectives can't be empty or null");
			}

			// detectives have secret ticket
			for (Player player : detectives) {
				ImmutableMap<Ticket, Integer> t = player.tickets();
				if (t.get(SECRET) > 0) {
					throw new IllegalArgumentException("detective has secret tickets");
				}
			}

			// detectives have double tickets
			for (Player player : detectives) {
				ImmutableMap<Ticket, Integer> t = player.tickets();
				if (t.get(DOUBLE) > 0) {
					throw new IllegalArgumentException("detective has double tickets");
				}
			}

			//detective overlap
			for (int i=0; i<detectives.size()-1; i++) {
				Player d1 = detectives.get(i);
				Player d2 = detectives.get(i+1);
				if (d1.location() == d2.location()) {
					throw new IllegalArgumentException("detectives overlap");
				}
			}

			//check if graph is empty
			if (setup.graph.nodes().isEmpty()) {
				throw new IllegalArgumentException("graph empty");
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
