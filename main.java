import java.util.*;
import java.io.*;
import java.math.*;
import java.lang.*;

/**
 * Send your busters out into the fog to trap ghosts and bring them home!
 **/
class Player {

		public static final int MIN_DISTANCE = 900;
		public static final int MAX_DISTANCE = 1760;

		public static final int FIELD_WIDTH = 16000;
		public static final int FIELD_HEIGHT = 9000;

		public static final int DROPOFF_DISTANCE = 1600;

		public enum Role {
			GHOST(-1),
			HUNTER(0),
			CATCHER(1),
			SUPPORT(2);

			private final int roleId;
			Role(int roleId) {
				this.roleId = roleId;
			}

			public int getRoleId() {
				return this.roleId;
			}

			public static Role getRole(int id) {
				for (Role r: Role.values()) {
					if (r.roleId == id) {
						return r;
					}
				}

				return null;
			}
		}

		public enum BusterState {
			DEFAULT(0),
			CARRYING(1),
			STUNNED(2),
			TRAPPING(3),
			BUSTING(4);

			private final int stateId;
			BusterState(int stateId) {
				this.stateId = stateId;
			}

			public int getStateId() {
				return this.stateId;
			}

			public static BusterState getState(int id) {
				for (BusterState s: BusterState.values()) {
					if (s.stateId == id) {
						return s;
					}
				}

				return null;
			}
		}

		public static class Point {
				public final int x;
				public final int y;

				public Point(int x, int y) {
						this.x = x;
						this.y = y;
				}

				public static double distance(Point p1, Point p2) {
					return Math.hypot((double)(p1.x - p2.x), (double)(p1.y - p2.y));
				}
		}

		public static class Ghost {
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

		public static class Buster {
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

		public static void actionMove(String s) {
			System.out.println(s);
		}

		public static Ghost findById(ArrayList<Ghost> list, int ghostId) {
			for(Ghost target: list) {
				if (target.id == ghostId) {
					return target;
				}
			}

			return null;
		}

		public static Ghost findSmallestGhost(ArrayList<Ghost> list) {
			int temp = 99999;
			int gId = -1;
			for (Ghost target: list) {
				if (target.stamina < temp) {
					temp = target.stamina;
					gId = target.id;
				}
			}
			
			return findById(list, gId);
		}

		public static int random(int min, int max)
		{
			int range = (max - min) + 1;
			return (int)(Math.random() * range) + min;
		}

		public static boolean dropoff(Buster player, int teamId) {
			if (player.role == Role.CATCHER && player.state == BusterState.CARRYING) {
				if (teamId == 0) {
					Point origin = new Point(0, 0);
					if (Point.distance(player.position, origin) <= DROPOFF_DISTANCE) {
						actionMove("RELEASE");
					} else {
						actionMove("MOVE 300 300");
					}
				} else {
					Point origin = new Point(FIELD_WIDTH, FIELD_HEIGHT);
					if (Point.distance(player.position, origin) <= DROPOFF_DISTANCE) {
						actionMove("RELEASE");
					} else {
						actionMove("MOVE 15300 7800");
					}
				}

				return true;
			} else {
				return false;
			}
		}

		public static boolean supporterAttackPhase(Point goal, int teamId, Buster player, ArrayList<Buster> enemies) {
			if (player.role == Role.SUPPORT) {
				Buster target = null;

				for (Buster e: enemies) {
					if (e.role == Role.CATCHER && e.state == BusterState.CARRYING) {
						target = e;
						
						break;
					}
				}

				if (target != null) {
					if (Point.distance(target.position, player.position) <= 1760) {
						actionMove("STUN " + target.id);
					} else {
						int S_TargetX = Math.min(target.position.x - 800, 0);
						int S_TargetY = Math.min(target.position.y - 800, 0);
						
						actionMove("MOVE " + S_TargetX + " " + S_TargetY);
					}
				} else {
					if (teamId == 0) {
						actionMove("MOVE 13500 6400");
					} else {
						actionMove("MOVE 2200 1800");
					}
				}

				return true;
			}

			return false;
		}

		public static void main(String args[]) {
				Scanner in = new Scanner(System.in);
				int bustersPerPlayer = in.nextInt(); // the amount of busters you control
				int ghostCount = in.nextInt(); // the amount of ghosts on the map
				int myTeamId = in.nextInt(); // if this is 0, your base is on the top left of the map, if it is one, on the bottom right

				int turns = 0;
				int row = (myTeamId == 0 ? 1 : 7);
				int col = (myTeamId == 0 ? 1 : 7);

				int lastX = -1;
				int lastY = -1;

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
							Role entityRole = Role.getRole(in.nextInt()); // -1 for ghosts, 0 for the HUNTER, 1 for the GHOST CATCHER and 2 for the SUPPORT
							int state = in.nextInt(); // For busters: 0=idle, 1=carrying a ghost. For ghosts: remaining stamina points.
							int value = in.nextInt(); // For busters: Ghost id being carried/busted or number of turns left when stunned. For ghosts: number of busters attempting to trap this ghost.

							if (entityRole == Role.GHOST) {
								ghosts.add(new Ghost(x, y, state, entityId, value));
							} else {
								Buster player = new Buster(entityRole, new Point(x, y), entityType, entityId, BusterState.getState(state));

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

					Buster hunter = ourBusters.get(0);
					Buster catcher = ourBusters.get(1);
					Buster supporter = ourBusters.get(2);

					if (ghosts.size() > 0) {
						// we found some
						lastX = random(500, FIELD_WIDTH);
						lastY = random(300, FIELD_HEIGHT); 
						Ghost target = findSmallestGhost(ghosts);

						if (target.stamina > 0) {
							// still alive
							double distance = Point.distance(hunter.position, target.position);
							if (distance > MIN_DISTANCE && distance < MAX_DISTANCE) {
								// ATTACK
								actionMove("BUST " + target.id);
								if (target.stamina <= 10) {
									if (!dropoff(catcher, myTeamId)) {
										actionMove("MOVE " + hunter.position.x + " " + hunter.position.y);
									}
									actionMove("MOVE " + supporter.position.x + " " + supporter.position.y);
								}
								else if (myTeamId == 0) {
									Point goal = new Point(13500, 6400);
									if (!dropoff(catcher, myTeamId)) {
										actionMove("MOVE " + goal.x + " " + goal.y);
									}
									
									if (!supporterAttackPhase(goal, myTeamId, supporter, theirBusters)) {
										actionMove("MOVE " + goal.x + " " + goal.y);
									}
								}
								else if (myTeamId == 1) {
									Point goal = new Point(2200, 1800);
									if (!dropoff(catcher, myTeamId)) {
										actionMove("MOVE 2200 1800");
									}
									
									if (!supporterAttackPhase(goal, myTeamId, supporter, theirBusters)) {
										actionMove("MOVE " + goal.x + " " + goal.y);
									}
								}

							} else {
								int H_TargetX = Math.min(target.position.x - MIN_DISTANCE, 0);
								int H_TargetY = Math.min(target.position.y - MIN_DISTANCE, 0);

								int G_TargetX = Math.min(target.position.x - MAX_DISTANCE, 0);
								int G_TargetY = Math.min(target.position.y - MAX_DISTANCE, 0);

								actionMove("MOVE " + H_TargetX + " " + H_TargetY); //hunter
								if (!dropoff(catcher, myTeamId)) {
									actionMove("MOVE " + G_TargetX + " " + G_TargetY); //ghosthunter
								}
								actionMove("MOVE " + G_TargetX + " " + G_TargetY); //support moves with ghosthunter
							}
						} else {
							// ready to capture
							double distance = Point.distance(catcher.position, target.position);
							if (distance > MIN_DISTANCE && distance < MAX_DISTANCE) {
								actionMove("MOVE " + hunter.position.x + " " + hunter.position.y);
								if (!dropoff(catcher, myTeamId)) {
									actionMove("TRAP " + target.id);
								}
								actionMove("MOVE " + supporter.position.x + " " + supporter.position.y);
							} else {
								int G_TargetX = Math.min(target.position.x - MAX_DISTANCE, 0);
								int G_TargetY = Math.min(target.position.y - MAX_DISTANCE, 0);

								actionMove("MOVE " + hunter.position.x + " " + hunter.position.y);
								if (!dropoff(catcher, myTeamId)) {
									actionMove("MOVE " + G_TargetX + " " + G_TargetY);
								}
								actionMove("MOVE " + supporter.position.x + " " + supporter.position.y);
							}
						}
					} else {
						int x = random(500, FIELD_WIDTH);
						int y = random(300, FIELD_HEIGHT);

						if (lastX < 0 || lastY < 0) {
							lastX = x;
							lastY = y;
						}

						if (hunter.position.x == lastX && hunter.position.y == lastY) {
							lastX = x;
							lastY = y;
						}

						actionMove("MOVE " + lastX + " " + lastY);
						if (!dropoff(catcher, myTeamId)) {
							actionMove("MOVE " + lastX + " " + lastY);
						}
						actionMove("MOVE " + lastX + " " + lastY);
					}

					// Write an action using System.out.println()
					// To debug: System.err.println("Debug messages...");


					// First the HUNTER : MOVE x y | BUST id
					// Second the GHOST CATCHER: MOVE x y | TRAP id | RELEASE
					// Third the SUPPORT: MOVE x y | STUN id | RADAR

					turns++;
				}
		}
}