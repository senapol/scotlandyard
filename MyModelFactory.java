package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.*;
import javax.annotation.Nonnull;
import java.util.*;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import uk.ac.bris.cs.scotlandyard.model.MyGameStateFactory;


/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {
	private List<Model.Observer> observers;
	public MyModelFactory(){
		observers = new ArrayList<>();
	}

	@Nonnull @Override public Model build(GameSetup setup,
										  Player mrX,
										  ImmutableList<Player> detectives) {
		// TODO
		return new Model() {
			@Nonnull
			@Override
			public Board getCurrentBoard() {
				Map<Piece.Detective, Integer> locations = new HashMap<>();
				for (Piece.Detective d : Piece.Detective.values()) {
					for (Player p : detectives) {
						if (p.piece() == d) {
							locations.put(d, p.location());
						}
					}
				}
				Map<ScotlandYard.Ticket, Integer> playerTickets = new HashMap<>();
				//Map<Piece, playerTickets> ticketsMap


				//return new ImmutableBoard(setup,locations, );
				return null;
				//purely for checking what the parameters are:
				// public ImmutableBoard(GameSetup setup,
				//	                      ImmutableMap<Detective, Integer> detectiveLocations,
				//	                      ImmutableMap<Piece, ImmutableMap<Ticket, Integer>> tickets,
				//	                      ImmutableList<LogEntry> mrXTravelLog,
				//	                      ImmutableSet<Piece> winner,
				//	                      ImmutableSet<Move> availableMoves) {
			}

			@Override
			public void registerObserver(@Nonnull Observer observer) {
				if (observer == null) {
					throw new NullPointerException("register observer is null");
				}
				if (!observers.contains(observer)){
					observers.add(observer);
				}
			}

			@Override
			public void unregisterObserver(@Nonnull Observer observer) {
				if (observer == null) {
					throw new NullPointerException("unregister observer is null");
				}
				if (!getObservers().contains(observer)) {
					throw new IllegalArgumentException("illegal unregister observer");
				}
				observers.remove(observer);
			}

			@Nonnull
			@Override
			public ImmutableSet<Observer> getObservers() {
				return ImmutableSet.copyOf(observers);
			}

			@Override
			public void chooseMove(@Nonnull Move move) {

			}
		};
	}
}