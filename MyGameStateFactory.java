package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import javax.annotation.Nonnull;
import java.util.*;
import com.google.common.collect.Lists;
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
			Set<Piece> mrXWin = new HashSet<>();
			mrXWin.add(mrX.piece());
			Set<Move.SingleMove> mrXSingle = makeSingleMoves(setup, detectives, mrX, mrX.location());
			Set<Piece> detWin= new HashSet<>();
			for(Player d : detectives){
				detWin.add(d.piece());
			}
			//detectives win
			if(mrXSingle.isEmpty()){
				return ImmutableSet.copyOf(detWin);
			}
			for(Player d: detectives) {
				if (d.location() == mrX.location()) {
					return ImmutableSet.copyOf(detWin);
				}
			}

			//mrX wins
			if(log.size() >= setup.moves.size()){
				return ImmutableSet.copyOf(mrXWin);
			}
			boolean dNoMove = true;
			for(Player d: detectives){
				if(!makeSingleMoves(setup, detectives, d, d.location()).isEmpty()){
					dNoMove=false;
				}
			}
			if(dNoMove){
				return ImmutableSet.copyOf(mrXWin);
			}
			return ImmutableSet.of();
		}

		//calculate possible single moves
		private static Set<Move.SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {

			//TODO create an empty collection of some sort, say, HashSet to store all the SingleMoves we generate
			Set<Move.SingleMove> moves = new HashSet<>();

			for (int destination : setup.graph.adjacentNodes(source)) {
				// TODO find out if destination is occupied by a detective
				// if the location is occupied, don't add to the collection of moves to return
				boolean free = true;
				for (Player det : detectives) {
					if (destination == det.location()) {
						free = false;
					}
				}

				if (free) {
					for (Transport t : Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))) {
						// TODO find out if the player has the required tickets
						//  if it does, construct a SingleMove and add it the collection of moves to return
						if (t != Transport.FERRY && player.has(t.requiredTicket())){
							moves.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination));
						}
					}
					// TODO consider the rules of secret moves here
					//  add moves to the destination via a secret ticket if there are any left with the player
					if(player.has(SECRET)){
						moves.add(new Move.SingleMove(player.piece(), source, SECRET, destination));
					}
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
						if (destination == det.location() || sMove.destination == det.location()) {
							free = false;
						}
					}
					if (free) {
						for (Transport t : Objects.requireNonNull(setup.graph.edgeValueOrDefault(sMove.destination, destination, ImmutableSet.of()))) {
							//checking that the transport isn't by ferry
							if (t != Transport.FERRY) {
								//if both transport methods are the same, check that there are 2 or more
								if (t.requiredTicket() == sMove.ticket && player.hasAtLeast(t.requiredTicket(), 2)) {
									moves.add(new Move.DoubleMove(player.piece(), source, sMove.ticket, sMove.destination, t.requiredTicket(), destination));

								}
								//if they are different, check that both have 1 or more
								else if ((t.requiredTicket() != sMove.ticket && player.has(t.requiredTicket()) && player.has(sMove.ticket))) {
									moves.add(new Move.DoubleMove(player.piece(), source, sMove.ticket, sMove.destination, t.requiredTicket(), destination));

								}
							}

							//adding single secret ticket combos
							if(player.has(sMove.ticket)&& player.has(SECRET)){
								moves.add(new Move.DoubleMove(player.piece(), source, sMove.ticket, sMove.destination, SECRET, destination));
							}
							if(player.has(t.requiredTicket()) && player.has(SECRET)){
								moves.add(new Move.DoubleMove(player.piece(), source, SECRET, sMove.destination, t.requiredTicket(), destination));
							}
						}

						//adding double secret tickets
						if (player.hasAtLeast(SECRET, 2)) {
							moves.add(new Move.DoubleMove(player.piece(), source, SECRET, sMove.destination, SECRET, destination));
						}
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
			if (!getWinner().isEmpty()) {
				return ImmutableSet.of();
			}

			//detective moves
			boolean mrXTurn = true;
			for (Player det : detectives) {
				if (remaining.contains(det.piece())) {
					moves.addAll(makeSingleMoves(setup, detectives, det, det.location()));
					mrXTurn = false;
				}
			}
			if (mrXTurn) {
				Set<Move.SingleMove> mrXSingle = makeSingleMoves(setup, detectives, mrX, mrX.location());
				moves.addAll(mrXSingle);
				if ((mrX.tickets().get(DOUBLE) > 0) && log.size() < setup.moves.size()-1) {
					moves.addAll(makeDoubleMoves(setup, detectives, mrX, mrX.location(), mrXSingle));
				}
			}
			return ImmutableSet.copyOf(moves);
		}

		@Override
		public GameState advance(Move move) {
			//Set moves = new HashSet<>();
			//moves = getAvailableMoves();
			//if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);

			return move.accept(new Move.Visitor<GameState>() {
				Player mrXUpdate = mrX;
				Set<Piece> remainingUpdate = new HashSet<>();

				@Override
				public GameState visit(Move.SingleMove move) {

					if(move.commencedBy().isMrX()){
						List<LogEntry> logUpdate = new ArrayList<>(log);

						for(Player det : detectives){
							remainingUpdate.add(det.piece());
						}
						mrXUpdate= mrXUpdate.at(move.destination);
						if(setup.moves.get(logUpdate.size())){
							logUpdate.add(LogEntry.reveal(move.ticket, move.destination));
						}else{
							logUpdate.add(LogEntry.hidden(move.ticket));
						}
						mrXUpdate = mrXUpdate.use(move.ticket);

						return new MyGameState(setup, ImmutableSet.copyOf(remainingUpdate), ImmutableList.copyOf(logUpdate), mrXUpdate, detectives);
					}
					if(move.commencedBy().isDetective()){
						List<Player> detectivesUpdate = new ArrayList<>();
						Player detUpdate = null;

						for(Player det : detectives){
							if(det.piece().webColour().equals(move.commencedBy().webColour())){
								detUpdate = det;
							}else{
								detectivesUpdate.add(det);
							}
						}
						for (Player det : detectives) {
							if (remaining.contains(det) && !detUpdate.equals(det)) {
								remainingUpdate.add(det.piece());
							}
						}
						if(remainingUpdate.isEmpty()){
							remainingUpdate.add(mrX.piece());
						}

						detUpdate = detUpdate.use(move.ticket);
						mrXUpdate = mrXUpdate.give(move.ticket);
						detUpdate = detUpdate.at(move.destination);
						detectivesUpdate.add(detUpdate);
						return new MyGameState(setup, ImmutableSet.copyOf(remainingUpdate), log, mrXUpdate, detectivesUpdate);
					}
					return null;

				}

				@Override
				public GameState visit(Move.DoubleMove move) {
					//MRX TURN
					if(move.commencedBy().isMrX()){
						//update mrX location
						mrXUpdate=mrXUpdate.at(move.destination2);
						List<LogEntry> logUpdate = new ArrayList<>(log);
						mrXUpdate=mrXUpdate.use(move.ticket1); //take used tickets away from mrX
						mrXUpdate=mrXUpdate.use(move.ticket2);
						mrXUpdate=mrXUpdate.use(DOUBLE);

						//swap to detectives turn
						for (Player det : detectives) {
							remainingUpdate.add(det.piece());
						}

						if(setup.moves.get(logUpdate.size())){ //check if reveal round
							logUpdate.add(LogEntry.reveal(move.ticket1, move.destination1)); //add first move to log
						}else{
							logUpdate.add(LogEntry.hidden(move.ticket1)); //add first move to log
						}
						if(setup.moves.get(logUpdate.size()))	{ //check if reveal round
							logUpdate.add(LogEntry.reveal(move.ticket2, move.destination2)); //add 2nd move to log
						}else{
							logUpdate.add(LogEntry.hidden(move.ticket2)); //add 2nd move to log
						}
						return new MyGameState(setup, ImmutableSet.copyOf(remainingUpdate), ImmutableList.copyOf(logUpdate), mrXUpdate, detectives);

					}
					//DETECTIVE TURN
					if(move.commencedBy().isDetective()){
						throw new IllegalArgumentException("detective can't use double tickets");
					}
					return new MyGameState(setup, ImmutableSet.copyOf(remainingUpdate), log, mrX, detectives);
				}
			});

		}

		public void chooseMove(@Nonnull Move move){
			// TODO Advance the model with move, then notify all observers of what what just happened.
			//  you may want to use getWinner() to determine whether to send out Event.MOVE_MADE or Event.GAME_OVER
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

			if (setup.graph.nodes().isEmpty()) {
				throw new IllegalArgumentException("graph empty");
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


//			if (!(getPlayers().contains(MRX))) {
//				throw new IllegalArgumentException("no mr x");
//			}

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