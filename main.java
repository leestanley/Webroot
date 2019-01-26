import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Send your busters out into the fog to trap ghosts and bring them home!
 **/
class Player {
		
		public enum Role {
			GHOST(-1),
			HUNTER(0),
			CATCHER(1),
			SUPPORT(2)
		}

		public enum BusterState {
			DEFAULT(0),
			CARRYING(1),
			STUNNED(2),
			TRAPPING(3),
			BUSTING(4)
		}
		
		public class Point {
				public final int x;
				public final int y;
				
				public Point(int x, int y) {
						this.x = x;
						this.y = y;
				}
		}
		
		public class Ghost {
				public Point position;
				public int stamina;
				public int id;
				public int numberOfAttempts;
				
				public Ghost(Point coord, int stamina, int id, int numberOfBustersTrapping) {
						this.position = new Point(coord.x, coord.y);
						this.stamina = stamina;
						this.id = id;
						this.numberOfAttempts = numberOfBustersTrapping;
				}
				
				public Ghost(int x, int y, int stamina, int id, int numberOfBustersTrapping) {
						this(new Point(x, y), stamina, id, numberOfBustersTrapping);
				}
		}
		
		public class Buster {
			public Role role;
			public Point position;
			public int teamId;
			public int id;
			public BusterState state;
			public int numOfRoundStunned;
			public Ghost target; // only if state == TRAPPING / CARRYING

			public int temp;
			
			public Buster(Role playerRole, Point pos, int teamId, int busterId, BusterState state) {
				this.role = playerRole;
				this.position = new Point(pos.x, pos.y);

				this.teamId = teamId;
				this.id = busterId;
				this.state = state;

				this.temp = -1;
			}
			
			public Buster(Role playerRole, Point pos, int teamId, int busterId, BusterState state, int roundsStunned, Ghost target) {
				this(playerRole, pos, teamId, busterId, state);

				this.numOfRoundStunned = roundsStunned;
				this.target = target;
			}
		}

		public static void print(String s) {
			System.out.println(s);
		}

		public static Ghost findById(ArrayList<Ghost> list, int ghostId) {
			for(Ghost target: list) {
				if (target.id == ghostId) {
					return target;
				}
			}
		}
		
		public static void main(String args[]) {
				Scanner in = new Scanner(System.in);
				int bustersPerPlayer = in.nextInt(); // the amount of busters you control
				int ghostCount = in.nextInt(); // the amount of ghosts on the map
				int myTeamId = in.nextInt(); // if this is 0, your base is on the top left of the map, if it is one, on the bottom right
				
				// game loop
				while (true) {
						int entities = in.nextInt(); // the number of busters and ghosts visible to you
						ArrayList<Ghost> ghosts = new ArrayList<Ghost>();
						ArrayList<Buster> ourBusters = new ArrayList<Buster>();
						ArrayList<Buster> theirBusters = new ArrayList<Buster>();

						for (int i = 0; i < entities; i++) {
								int entityId = in.nextInt(); // buster id or ghost id
								int x = in.nextInt();
								int y = in.nextInt(); // position of this buster / ghost
								int entityType = in.nextInt(); // the team id if it is a buster, -1 if it is a ghost.
								Role entityRole = (Role)in.nextInt(); // -1 for ghosts, 0 for the HUNTER, 1 for the GHOST CATCHER and 2 for the SUPPORT
								int state = in.nextInt(); // For busters: 0=idle, 1=carrying a ghost. For ghosts: remaining stamina points.
								int value = in.nextInt(); // For busters: Ghost id being carried/busted or number of turns left when stunned. For ghosts: number of busters attempting to trap this ghost.
								
								switch (entityRole) {
									case (GHOST):
										ghosts.add(new Ghost(x, y, state, entityId));

										break;
									case (BUSTER):
										Buster player = new Buster(entityRole, new Point(x, y), entityRole, entityId, (BusterState)state);
										
										if (player.state == BusterState.TRAPPING || player.state == BusterState.CARRYING) {
											player.temp = value;
										} else if (player.state == BusterState.STUNNED) {
											player.numOfRoundStunned = value;
										}
										
										if (entityType == myTeamId) {
											ourBusters.add(player);
										} else {
											theirBusters.add(player);
										}

										break;
								}
						}

						for (Buster player: ourBusters) {
							if (player.temp != -1) {
								player.target = findById(ghosts, player.temp);
								player.temp = -1;
							}
						}

						for (Buster player: theirBusters) {
							if (player.temp != -1) {
								player.target = findById(ghosts, player.temp);
								player.temp = -1;
							}
						}

						print(ourBusters.size());

						// Write an action using System.out.println()
						// To debug: System.err.println("Debug messages...");


						// First the HUNTER : MOVE x y | BUST id
						// Second the GHOST CATCHER: MOVE x y | TRAP id | RELEASE
						// Third the SUPPORT: MOVE x y | STUN id | RADAR
					 
				}
		}
}